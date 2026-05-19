const SOAP_URL = process.env.SOAP_URL || 'http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService';
const NS = 'http://soap.ticketpremium.espe.edu.ec/';

const MOCK_FECHAS = {
  1: '2026-06-15T19:00:00',
  2: '2026-06-22T17:00:00',
  3: '2026-06-29T20:00:00',
  4: '2026-07-06T18:30:00',
  5: '2026-07-13T16:00:00',
};

function buildEnvelope(bodyContent) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="${NS}">
  <soap:Body>
    ${bodyContent}
  </soap:Body>
</soap:Envelope>`;
}

async function soapCall(method, params = {}) {
  const paramsXml = Object.entries(params)
    .map(([key, value]) => `<${key}>${value}</${key}>`)
    .join('');

  const bodyContent = paramsXml
    ? `<tns:${method}>${paramsXml}</tns:${method}>`
    : `<tns:${method}/>`;

  const xml = buildEnvelope(bodyContent);

  const response = await fetch(SOAP_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'text/xml; charset=utf-8' },
    body: xml,
  });

  const text = await response.text();

  if (!response.ok) {
    throw new Error(`SOAP request failed: ${response.status}`);
  }

  return text;
}

function extractTag(xml, tag) {
  const regex = new RegExp(`<${tag}>(.*?)</${tag}>`, 's');
  const match = xml.match(regex);
  return match ? match[1].trim() : null;
}

function extractAll(xml, tag) {
  const regex = new RegExp(`<${tag}>(.*?)</${tag}>`, 'gs');
  const matches = [];
  let match;
  while ((match = regex.exec(xml)) !== null) {
    matches.push(match[1].trim());
  }
  return matches;
}

export async function doLogin(username, password) {
  const xml = await soapCall('login', { username, password });

  return {
    exitoso: extractTag(xml, 'exitoso') === 'true',
    mensaje: extractTag(xml, 'mensaje') || '',
    username: extractTag(xml, 'username') || '',
    clienteId: (() => { const v = extractTag(xml, 'clienteId'); return v ? parseInt(v) : null; })(),
  };
}

export async function listarPartidos() {
  const xml = await soapCall('listarPartidosDisponibles');

  const returns = extractAll(xml, 'return');
  return returns.map((ret) => {
    const codigo = extractTag(ret, 'codigo');
    let fecha = extractTag(ret, 'fecha') || '';
    if (!fecha && MOCK_FECHAS[codigo]) {
      fecha = MOCK_FECHAS[codigo];
    }

    return {
      codigo: codigo ? parseInt(codigo) : 0,
      equipoLocal: extractTag(ret, 'equipoLocal') || '',
      equipoVisita: extractTag(ret, 'equipoVisita') || '',
      fecha,
      lugar: extractTag(ret, 'lugar') || '',
    };
  });
}

export async function listarLocalidades(codigoPartido) {
  const xml = await soapCall('listarLocalidadesDisponibles', { codigoPartido });

  const returns = extractAll(xml, 'return');
  return returns.map((ret) => ({
    codigoLocalidad: extractTag(ret, 'codigoLocalidad') || '',
    disponibilidad: parseInt(extractTag(ret, 'disponibilidad') || '0'),
    precio: parseFloat(extractTag(ret, 'precio') || '0'),
  }));
}

export async function comprarBoletos(username, password, codigoPartido, codigoLocalidad, cantidad) {
  const xml = await soapCall('comprarBoletos', {
    username, password, codigoPartido, codigoLocalidad, cantidad,
  });

  const exitoso = extractTag(xml, 'exitoso');
  const mensaje = extractTag(xml, 'mensaje') || '';

  if (exitoso !== 'true') {
    return { exitoso: false, mensaje };
  }

  return {
    exitoso: true,
    mensaje,
    facturaId: parseInt(extractTag(xml, 'facturaId') || '0'),
    clienteNombre: extractTag(xml, 'clienteNombre') || '',
    clienteCedula: extractTag(xml, 'clienteCedula') || '',
    fechaCompra: extractTag(xml, 'fechaCompra') || '',
    subtotal: parseFloat(extractTag(xml, 'subtotal') || '0'),
    iva: parseFloat(extractTag(xml, 'iva') || '0'),
    total: parseFloat(extractTag(xml, 'total') || '0'),
    codigoPartido: parseInt(extractTag(xml, 'codigoPartido') || '0'),
    nombrePartido: extractTag(xml, 'nombrePartido') || '',
    codigoLocalidad: extractTag(xml, 'codigoLocalidad') || '',
    cantidad: parseInt(extractTag(xml, 'cantidad') || '0'),
    precioUnitario: parseFloat(extractTag(xml, 'precioUnitario') || '0'),
  };
}

export async function generarReporte(codigoPartido) {
  const xml = await soapCall('generarReporteVentas', { codigoPartido });

  const returns = extractAll(xml, 'return');
  return returns.map((ret) => ({
    codigoPartido: parseInt(extractTag(ret, 'codigoPartido') || '0'),
    nombrePartido: extractTag(ret, 'nombrePartido') || '',
    codigoLocalidad: extractTag(ret, 'codigoLocalidad') || '',
    cantidadTotalVendida: parseInt(extractTag(ret, 'cantidadTotalVendida') || '0'),
    totalRecaudado: parseFloat(extractTag(ret, 'totalRecaudado') || '0'),
  }));
}
