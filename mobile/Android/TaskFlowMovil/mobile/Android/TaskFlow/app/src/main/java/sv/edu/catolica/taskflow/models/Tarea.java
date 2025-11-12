package sv.edu.catolica.taskflow.models;

import java.util.List;

public class Tarea {
    private int id;
    private String titulo;
    private String descripcion;
    private String fecha_vencimiento;
    private String estado;
    private int proyecto_id;
    private User asignado;
    private User creado_por;
    private String fecha_creacion;
    private List<Comentario> comentarios;

    public Tarea() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFecha_vencimiento() { return fecha_vencimiento; }
    public void setFecha_vencimiento(String fecha_vencimiento) { this.fecha_vencimiento = fecha_vencimiento; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getProyecto_id() { return proyecto_id; }
    public void setProyecto_id(int proyecto_id) { this.proyecto_id = proyecto_id; }

    public User getAsignado() { return asignado; }
    public void setAsignado(User asignado) { this.asignado = asignado; }

    public User getCreado_por() { return creado_por; }
    public void setCreado_por(User creado_por) { this.creado_por = creado_por; }

    public String getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(String fecha_creacion) { this.fecha_creacion = fecha_creacion; }

    public List<Comentario> getComentarios() { return comentarios; }
    public void setComentarios(List<Comentario> comentarios) { this.comentarios = comentarios; }
}
