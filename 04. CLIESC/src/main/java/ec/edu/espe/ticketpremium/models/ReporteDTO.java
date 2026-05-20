package ec.edu.espe.ticketpremium.models;

import java.math.BigDecimal;

public class ReporteDTO {
    private Integer codigoPartido;
    private String nombrePartido;
    private String codigoLocalidad;
    private Integer cantidadTotalVendida;
    private BigDecimal totalRecaudado;

    public Integer getCodigoPartido() {
        return codigoPartido;
    }

    public void setCodigoPartido(Integer codigoPartido) {
        this.codigoPartido = codigoPartido;
    }

    public String getNombrePartido() {
        return nombrePartido;
    }

    public void setNombrePartido(String nombrePartido) {
        this.nombrePartido = nombrePartido;
    }

    public String getCodigoLocalidad() {
        return codigoLocalidad;
    }

    public void setCodigoLocalidad(String codigoLocalidad) {
        this.codigoLocalidad = codigoLocalidad;
    }

    public Integer getCantidadTotalVendida() {
        return cantidadTotalVendida;
    }

    public void setCantidadTotalVendida(Integer cantidadTotalVendida) {
        this.cantidadTotalVendida = cantidadTotalVendida;
    }

    public BigDecimal getTotalRecaudado() {
        return totalRecaudado;
    }

    public void setTotalRecaudado(BigDecimal totalRecaudado) {
        this.totalRecaudado = totalRecaudado;
    }
}