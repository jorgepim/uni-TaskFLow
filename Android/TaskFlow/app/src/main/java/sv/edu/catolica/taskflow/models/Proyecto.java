package sv.edu.catolica.taskflow.models;

import java.util.Map;

public class Proyecto {
    private int id;
    private String titulo;
    private String descripcion;
    private int total_tareas;
    private Map<String, Integer> tasks_by_estado;
    private double percent_completed;
    private int assigned_users_count;
    private String fecha_creacion;

    public Proyecto() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getTotal_tareas() { return total_tareas; }
    public void setTotal_tareas(int total_tareas) { this.total_tareas = total_tareas; }

    public Map<String, Integer> getTasks_by_estado() { return tasks_by_estado; }
    public void setTasks_by_estado(Map<String, Integer> tasks_by_estado) { this.tasks_by_estado = tasks_by_estado; }

    public double getPercent_completed() { return percent_completed; }
    public void setPercent_completed(double percent_completed) { this.percent_completed = percent_completed; }

    public int getAssigned_users_count() { return assigned_users_count; }
    public void setAssigned_users_count(int assigned_users_count) { this.assigned_users_count = assigned_users_count; }

    public String getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(String fecha_creacion) { this.fecha_creacion = fecha_creacion; }
}
