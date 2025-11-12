package sv.edu.catolica.taskflow.models;

import java.util.List;
import java.util.Map;

public class TareasResponse {
    private List<Tarea> tareas;
    private Estadisticas estadisticas;

    public TareasResponse() {}

    // Getters and setters
    public List<Tarea> getTareas() { return tareas; }
    public void setTareas(List<Tarea> tareas) { this.tareas = tareas; }

    public Estadisticas getEstadisticas() { return estadisticas; }
    public void setEstadisticas(Estadisticas estadisticas) { this.estadisticas = estadisticas; }

    public static class Estadisticas {
        private int total;
        private Map<String, Integer> counts;
        private double porcentaje_completadas;
        private double porcentaje_progreso_ponderado;

        // Getters and setters
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public Map<String, Integer> getCounts() { return counts; }
        public void setCounts(Map<String, Integer> counts) { this.counts = counts; }

        public double getPorcentaje_completadas() { return porcentaje_completadas; }
        public void setPorcentaje_completadas(double porcentaje_completadas) { this.porcentaje_completadas = porcentaje_completadas; }

        public double getPorcentaje_progreso_ponderado() { return porcentaje_progreso_ponderado; }
        public void setPorcentaje_progreso_ponderado(double porcentaje_progreso_ponderado) { this.porcentaje_progreso_ponderado = porcentaje_progreso_ponderado; }
    }
}
