package sv.edu.catolica.taskflow.models;

import java.util.List;

public class User {
    private int id;
    private String nombre;
    private String email;
    private boolean activo;
    private String fecha_creacion;
    private List<Role> roles;

    public User() {}

    public User(String nombre, String email, String password, boolean activo) {
        this.nombre = nombre;
        this.email = email;
        this.activo = activo;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(String fecha_creacion) { this.fecha_creacion = fecha_creacion; }

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    public boolean isAdmin() {
        if (roles != null) {
            for (Role role : roles) {
                if ("ADMIN".equals(role.getNombre())) {
                    return true;
                }
            }
        }
        return false;
    }
}
