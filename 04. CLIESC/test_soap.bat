@echo off
echo ========================================
echo TEST SOAP - Ticket Premium
echo ========================================
echo Server: 209.145.48.25:8086
echo.

echo [1] Verificando si el servidor esta corriendo...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://209.145.48.25:8086/ 2>nul
if errorlevel 1 (
    echo ERROR: No se puede conectar a 209.145.48.25:8086
    echo.
    echo Asegurate de que el servidor este corriendo.
    echo.
)

echo.
echo [2] Verificando WSDL del servicio...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService?wsdl 2>nul

echo.
echo [3] Enviando peticion SOAP de Login...
echo.

set XML=<?xml version="1.0" encoding="UTF-8"?^><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/"^><soap:Body^><tns:login^><username^>MONSTER</username^><password^>MONSTER9</password^></tns:login^></soap:Body^></soap:Envelope^>

curl -X POST -H "Content-Type: text/xml; charset=utf-8" -H "SOAPAction: \"http://soap.ticketpremium.espe.edu.ec/login\"" -d "%XML%" http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService

echo.
echo ========================================
echo Fin del test
echo ========================================
pause