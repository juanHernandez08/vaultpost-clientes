package co.vaultpost.clientes.modelo;

import java.time.LocalDateTime;

/**
 * Representa un cliente de una agencia en Vaultpost (tabla clients).
 * Un cliente es la marca o negocio cuyas redes sociales gestiona la agencia.
 */
public class Cliente {

    private String id;
    private String companyId;
    private String nombre;
    private String logoUrl;
    private LocalDateTime fechaCreacion;
    private int totalCuentas;

    public Cliente() {
    }

    public Cliente(String companyId, String nombre, String logoUrl) {
        this.companyId = companyId;
        this.nombre = nombre;
        this.logoUrl = logoUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public int getTotalCuentas() { return totalCuentas; }
    public void setTotalCuentas(int totalCuentas) { this.totalCuentas = totalCuentas; }

    @Override
    public String toString() {
        return String.format("%-38s %-25s %d cuenta(s)", id, nombre, totalCuentas);
    }
}
