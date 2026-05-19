import sys
from InquirerPy import inquirer
from InquirerPy.validator import NumberValidator

from soap_client import TicketPremiumClient
from ui import (
    console,
    print_banner,
    print_login_error,
    print_partidos,
    print_localidades,
    print_factura,
    print_compra_error,
    print_reporte,
)


def do_login(client: TicketPremiumClient) -> bool:
    console.print()
    username = inquirer.text(
        message="Usuario:",
        default="MONSTER",
    ).execute()

    password = inquirer.secret(
        message="Contrasena:",
    ).execute()

    resultado = client.login(username, password)

    if resultado["exitoso"]:
        console.print(f"\n[bold green]Bienvenido, {resultado['username']}![/bold green]\n")
        return True
    else:
        print_login_error(resultado["mensaje"])
        return False


def flujo_comprar(client: TicketPremiumClient):
    console.print("\n[bold underline cyan]=== Comprar Boletos ===[/bold underline cyan]\n")

    partidos = client.get_partidos()
    if not partidos:
        console.print("[yellow]No hay partidos disponibles.[/yellow]\n")
        return

    print_partidos(partidos)

    partido_choices = [
        {"name": f"{p['codigo']} - {p['nombre']} ({p['lugar']})", "value": p}
        for p in partidos
    ]

    partido_sel = inquirer.select(
        message="Seleccione un partido:",
        choices=partido_choices,
    ).execute()

    localidades = client.get_localidades(partido_sel["codigo"])
    if not localidades:
        console.print("[yellow]No hay localidades disponibles para este partido.[/yellow]\n")
        return

    print_localidades(localidades)

    localidad_choices = [
        {"name": loc["etiqueta"], "value": loc}
        for loc in localidades
    ]

    localidad_sel = inquirer.select(
        message="Seleccione una localidad:",
        choices=localidad_choices,
    ).execute()

    cantidad = inquirer.number(
        message="Cantidad de boletos:",
        default=1,
        min_allowed=1,
        max_allowed=localidad_sel["disponibilidad"],
        validate=NumberValidator(),
    ).execute()

    console.print("\n[bold yellow]Procesando compra...[/bold yellow]\n")

    try:
        resultado = client.comprar_boletos(
            codigo_partido=partido_sel["codigo"],
            codigo_localidad=localidad_sel["codigoLocalidad"],
            cantidad=cantidad,
        )

        if resultado["exitoso"]:
            print_factura(resultado)
        else:
            print_compra_error(resultado["mensaje"])
    except Exception as e:
        print_compra_error(f"Error de conexion: {e}")


def flujo_reporte(client: TicketPremiumClient):
    console.print("\n[bold underline cyan]=== Reporte de Ventas ===[/bold underline cyan]\n")

    partidos = client.get_partidos()
    if not partidos:
        console.print("[yellow]No hay partidos disponibles.[/yellow]\n")
        return

    print_partidos(partidos)

    partido_choices = [
        {"name": f"{p['codigo']} - {p['nombre']}", "value": p}
        for p in partidos
    ]

    partido_sel = inquirer.select(
        message="Seleccione un partido para el reporte:",
        choices=partido_choices,
    ).execute()

    console.print("\n[bold yellow]Generando reporte...[/bold yellow]\n")

    try:
        reporte = client.generar_reporte(codigo_partido=partido_sel["codigo"])
        print_reporte(reporte)
    except Exception as e:
        console.print(f"[bold red]Error al generar reporte: {e}[/bold red]\n")


def main():
    print_banner()

    client = TicketPremiumClient()

    while True:
        if not client.is_logged_in():
            if not do_login(client):
                reintentar = inquirer.confirm(
                    message="Intentar de nuevo?",
                    default=True,
                ).execute()
                if not reintentar:
                    console.print("[yellow]Saliendo...[/yellow]\n")
                    sys.exit(0)
            continue

        opcion = inquirer.select(
            message="Menu Principal:",
            choices=[
                {"name": "Comprar Boletos", "value": "comprar"},
                {"name": "Ver Reporte de Ventas", "value": "reporte"},
                {"name": "Salir", "value": "salir"},
            ],
        ).execute()

        if opcion == "comprar":
            flujo_comprar(client)
        elif opcion == "reporte":
            flujo_reporte(client)
        elif opcion == "salir":
            console.print("\n[bold green]Gracias por usar TicketPremium. Adios![/bold green]\n")
            sys.exit(0)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        console.print("\n[yellow]Interrumpido por el usuario. Saliendo...[/yellow]\n")
        sys.exit(0)
    except Exception as e:
        console.print(f"\n[bold red]Error inesperado: {e}[/bold red]\n")
        sys.exit(1)
