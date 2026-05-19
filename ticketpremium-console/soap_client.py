from datetime import datetime
from zeep import Client
from zeep.exceptions import Fault

WSDL_URL = "http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService?wsdl"

MESES = {
    1: "Ene", 2: "Feb", 3: "Mar", 4: "Abr", 5: "May", 6: "Jun",
    7: "Jul", 8: "Ago", 9: "Sep", 10: "Oct", 11: "Nov", 12: "Dic",
}


def parse_fecha(fecha) -> str:
    if fecha is None:
        return "Por definir"
    if isinstance(fecha, datetime):
        mes = MESES.get(fecha.month, str(fecha.month))
        hora = ""
        if fecha.hour is not None:
            hora = f" {fecha.hour:02d}:{fecha.minute:02d}"
        return f"{fecha.day:02d}-{mes}-{fecha.year}{hora}"
    if hasattr(fecha, "year") and hasattr(fecha, "month") and hasattr(fecha, "day"):
        dia = fecha.day
        mes = MESES.get(fecha.month, str(fecha.month))
        anio = fecha.year
        hora = ""
        if hasattr(fecha, "hour") and fecha.hour is not None:
            hora = f" {fecha.hour:02d}:{getattr(fecha, 'minute', 0):02d}"
        return f"{dia:02d}-{mes}-{anio}{hora}"
    if isinstance(fecha, dict):
        try:
            dia = fecha.get("day", "")
            mes = MESES.get(fecha.get("month", 0), str(fecha.get("month", "")))
            anio = fecha.get("year", "")
            return f"{dia}-{mes}-{anio}"
        except Exception:
            pass
    texto = str(fecha).strip()
    if texto and texto not in ("None", "null", ""):
        return texto
    return "Por definir"


class TicketPremiumClient:
    def __init__(self):
        self.client = Client(WSDL_URL)
        self.service = self.client.service
        self._username = None
        self._password = None
        self._cliente_id = None

    def login(self, username: str, password: str) -> dict:
        resultado = self.service.login(username=username, password=password)
        if resultado.exitoso:
            self._username = username
            self._password = password
            self._cliente_id = resultado.clienteId
        return {
            "exitoso": resultado.exitoso,
            "mensaje": resultado.mensaje,
            "username": resultado.username,
            "clienteId": resultado.clienteId,
        }

    def is_logged_in(self) -> bool:
        return self._username is not None

    def get_partidos(self) -> list[dict]:
        partidos = self.service.listarPartidosDisponibles()
        resultado = []
        for p in partidos:
            resultado.append({
                "codigo": p.codigo,
                "equipoLocal": p.equipoLocal,
                "equipoVisita": p.equipoVisita,
                "fecha": parse_fecha(p.fecha),
                "lugar": p.lugar,
                "nombre": f"{p.equipoLocal} vs {p.equipoVisita}",
            })
        return resultado

    def get_localidades(self, codigo_partido: int) -> list[dict]:
        localidades = self.service.listarLocalidadesDisponibles(
            codigoPartido=codigo_partido
        )
        resultado = []
        for loc in localidades:
            resultado.append({
                "codigoLocalidad": loc.codigoLocalidad,
                "disponibilidad": loc.disponibilidad,
                "precio": float(loc.precio),
                "etiqueta": f"{loc.codigoLocalidad} - ${float(loc.precio):.2f} ({loc.disponibilidad} disp.)",
            })
        return resultado

    def comprar_boletos(
        self,
        codigo_partido: int,
        codigo_localidad: str,
        cantidad: int,
    ) -> dict:
        if not self.is_logged_in():
            return {"exitoso": False, "mensaje": "No hay sesion activa"}

        resultado = self.service.comprarBoletos(
            username=self._username,
            password=self._password,
            codigoPartido=codigo_partido,
            codigoLocalidad=codigo_localidad,
            cantidad=cantidad,
        )
        return {
            "exitoso": resultado.exitoso,
            "mensaje": resultado.mensaje,
            "facturaId": resultado.facturaId,
            "clienteNombre": resultado.clienteNombre,
            "clienteCedula": resultado.clienteCedula,
            "fechaCompra": parse_fecha(resultado.fechaCompra),
            "subtotal": float(resultado.subtotal),
            "iva": float(resultado.iva),
            "total": float(resultado.total),
            "codigoPartido": resultado.codigoPartido,
            "nombrePartido": resultado.nombrePartido,
            "codigoLocalidad": resultado.codigoLocalidad,
            "cantidad": resultado.cantidad,
            "precioUnitario": float(resultado.precioUnitario),
        }

    def generar_reporte(self, codigo_partido: int) -> dict:
        reportes = self.service.generarReporteVentas(codigoPartido=codigo_partido)
        nombre_partido = ""
        filas = []
        for r in reportes:
            nombre_partido = r.nombrePartido
            filas.append({
                "codigoPartido": r.codigoPartido,
                "nombrePartido": r.nombrePartido,
                "codigoLocalidad": r.codigoLocalidad,
                "cantidadTotalVendida": r.cantidadTotalVendida,
                "totalRecaudado": float(r.totalRecaudado),
            })
        return {
            "nombrePartido": nombre_partido,
            "filas": filas,
        }
