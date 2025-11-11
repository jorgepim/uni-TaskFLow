public class RegisterRequest {
    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    private Boolean activo = true;
}

