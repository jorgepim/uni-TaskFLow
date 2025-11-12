package sv.edu.catolica.taskflow.models;

public class ProyectoSimple {

    private int id;
    private String titulo;

    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }

    // ¡Importante! Esto es lo que se mostrará en el Spinner.
    @Override
    public String toString() {
        return titulo;
    }

}
