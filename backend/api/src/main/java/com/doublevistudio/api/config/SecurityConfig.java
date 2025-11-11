package com.doublevistudio.api.config;

import com.doublevistudio.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// --- Importaciones de CORS ---
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      // --- 1. AÑADIMOS LA CONFIGURACIÓN DE CORS ---
      .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))

      // --- Tu configuración existente ---
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/register", "/api/login", "/h2-console/**").permitAll()
        .anyRequest().authenticated()
      )
      .headers(headers -> headers.frameOptions(frame -> frame.disable()));

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  // --- 2. AÑADIMOS EL BEAN DE CONFIGURACIÓN DE CORS ---
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Permite peticiones de CUALQUIER origen (para desarrollo)
    configuration.setAllowedOriginPatterns(List.of("*"));

    // Permite todos los métodos HTTP que tu app React/móvil usará
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    // Permite todas las cabeceras (importante para "Authorization")
    configuration.setAllowedHeaders(List.of("*"));

    // Permite que se envíen credenciales (como el token)
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // Aplica esta configuración a TODAS las rutas de tu API
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}