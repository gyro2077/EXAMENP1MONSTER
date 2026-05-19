from rich.console import Console
from rich.panel import Panel
from rich.table import Table
from rich.text import Text
from rich.align import Align
from rich import box

console = Console()


def print_banner():
    banner = Text()
    banner.append("\n", style="bold")
    banner.append("  ████████╗██╗██╗     ███████╗    ████████╗███████╗██████╗ ███╗   ███╗██╗███╗   ██╗ █████╗ ██╗     \n", style="bold #00b4d8")
    banner.append("  ╚══██╔══╝██║██║     ██╔════╝    ╚══██╔══╝██╔════╝██╔══██╗████╗ ████║██║████╗  ██║██╔══██╗██║     \n", style="bold #0096c7")
    banner.append("     ██║   ██║██║     █████╗         ██║   █████╗  ██████╔╝██╔████╔██║██║██╔██╗ ██║███████║██║     \n", style="bold #0077b6")
    banner.append("     ██║   ██║██║     ██╔══╝         ██║   ██╔══╝  ██╔══██╗██║╚██╔╝██║██║██║╚██╗██║██╔══██║██║     \n", style="bold #023e8a")
    banner.append("     ██║   ██║███████╗███████╗       ██║   ███████╗██║  ██║██║ ╚═╝ ██║██║██║ ╚████║██║  ██║███████╗\n", style="bold #03045e")
    banner.append("     ╚═╝   ╚═╝╚══════╝╚══════╝       ╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝\n", style="bold #03045e")
    banner.append("\n", style="bold")
    banner.append("              Sistema de Compra de Boletos - Arquitectura de Software\n", style="italic #48cae4")
    console.print(Align.center(banner))
    console.print()


def print_login_error(mensaje: str):
    panel = Panel(
        Text(mensaje, style="bold red"),
        title="[red]Error de Autenticacion[/red]",
        border_style="red",
        box=box.ROUNDED,
    )
    console.print(panel)


def print_partidos(partidos: list[dict]):
    table = Table(
        title="[bold cyan]Partidos Disponibles[/bold cyan]",
        box=box.ROUNDED,
        header_style="bold white on blue",
    )
    table.add_column("Codigo", style="cyan", justify="center", width=8)
    table.add_column("Partido", style="green", justify="left")
    table.add_column("Lugar", style="yellow", justify="left")
    table.add_column("Fecha", style="magenta", justify="center")

    for p in partidos:
        table.add_row(
            str(p["codigo"]),
            p["nombre"],
            p["lugar"],
            p["fecha"],
        )

    console.print()
    console.print(table)
    console.print()


def print_localidades(localidades: list[dict]):
    table = Table(
        title="[bold cyan]Localidades Disponibles[/bold cyan]",
        box=box.ROUNDED,
        header_style="bold white on blue",
    )
    table.add_column("Codigo", style="cyan", justify="center", width=16)
    table.add_column("Precio", style="green", justify="right", width=12)
    table.add_column("Disponibilidad", style="yellow", justify="center", width=16)

    for loc in localidades:
        table.add_row(
            loc["codigoLocalidad"],
            f"${loc['precio']:.2f}",
            str(loc["disponibilidad"]),
        )

    console.print()
    console.print(table)
    console.print()


def print_factura(compra: dict):
    panel_content = Text()
    panel_content.append(f"Factura #{compra['facturaId']}\n\n", style="bold white")
    panel_content.append(f"Cliente : {compra['clienteNombre']}\n", style="cyan")
    panel_content.append(f"Cedula  : {compra['clienteCedula']}\n", style="cyan")
    panel_content.append(f"Fecha   : {compra['fechaCompra']}\n\n", style="cyan")
    panel_content.append(f"Partido      : {compra['nombrePartido']}\n", style="green")
    panel_content.append(f"Localidad    : {compra['codigoLocalidad']}\n", style="green")
    panel_content.append(f"Cantidad     : {compra['cantidad']}\n", style="green")
    panel_content.append(f"Precio Unit. : ${compra['precioUnitario']:.2f}\n\n", style="green")
    panel_content.append("─" * 30 + "\n", style="dim")
    panel_content.append(f"{'Subtotal':<20} ${compra['subtotal']:.2f}\n", style="yellow")
    panel_content.append(f"{'IVA (15%)':<20} ${compra['iva']:.2f}\n", style="yellow")
    panel_content.append(f"{'TOTAL':<20} ${compra['total']:.2f}\n", style="bold white")

    panel = Panel(
        Align.left(panel_content),
        title="[bold green]Compra Exitosa[/bold green]",
        border_style="green",
        box=box.DOUBLE,
    )
    console.print()
    console.print(panel)
    console.print()


def print_compra_error(mensaje: str):
    panel = Panel(
        Text(mensaje, style="bold red"),
        title="[red]Error en la Compra[/red]",
        border_style="red",
        box=box.ROUNDED,
    )
    console.print(panel)


def print_reporte(reporte: dict):
    filas = reporte["filas"]
    nombre_partido = reporte["nombrePartido"]

    if not filas:
        panel = Panel(
            Text("No se encontraron ventas para este partido.", style="yellow"),
            title="[yellow]Reporte de Ventas[/yellow]",
            border_style="yellow",
            box=box.ROUNDED,
        )
        console.print(panel)
        return

    table = Table(
        title=f"[bold cyan]Reporte: {nombre_partido}[/bold cyan]",
        box=box.ROUNDED,
        header_style="bold white on blue",
    )
    table.add_column("Localidad", style="cyan", justify="center")
    table.add_column("Vendidos", style="green", justify="center")
    table.add_column("Total Recaudado", style="yellow", justify="right")

    total_vendidos = 0
    total_recaudado = 0.0

    for fila in filas:
        table.add_row(
            fila["codigoLocalidad"],
            str(fila["cantidadTotalVendida"]),
            f"${fila['totalRecaudado']:.2f}",
        )
        total_vendidos += fila["cantidadTotalVendida"]
        total_recaudado += fila["totalRecaudado"]

    table.add_row(
        "[bold white]TOTAL[/bold white]",
        f"[bold green]{total_vendidos}[/bold green]",
        f"[bold yellow]${total_recaudado:.2f}[/bold yellow]",
    )

    console.print()
    console.print(table)
    console.print()
