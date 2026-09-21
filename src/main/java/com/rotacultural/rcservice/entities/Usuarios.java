package com.rotacultural.rcservice.entities;

import com.rotacultural.rcservice.entities.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idUser;

    @Size(max = 150)
    @NotBlank
    private String nome;

    @Size(max = 150)
    @NotBlank
    private String email;

    @NotBlank
    private String senha;

    @Size(max = 11)
    private String cep;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Size(max = 500)
    private String bio;

    @CreationTimestamp
    @Column(name = "data_created", nullable = false, updatable = false)
    private LocalDateTime dataCreated;
}
