package ec.edu.espe.ticketpremium.models;

import java.time.LocalDateTime;

public class PartidoDTO {
    private Integer codigo;
    private String equipoLocal;
    private String equipoVisita;
    private LocalDateTime fecha;
    private String lugar;

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getEquipoLocal() {
        return equipoLocal;
    }

    public void setEquipoLocal(String equipoLocal) {
        this.equipoLocal = equipoLocal;
    }

    public String getEquipoVisita() {
        return equipoVisita;
    }

    public void setEquipoVisita(String equipoVisita) {
        this.equipoVisita = equipoVisita;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getNombrePartido() {
        return equipoLocal + " vs " + equipoVisita;
    }
}