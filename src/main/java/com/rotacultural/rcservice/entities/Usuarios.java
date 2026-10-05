package com.rotacultural.rcservice.entities;

import com.rotacultural.rcservice.entities.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;

    @Size(max = 150)
    @NotBlank
    private String nome;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(length = 255, nullable = false)
    private String senha;

    @Size(max = 11)
    private String cep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Size(max = 500)
    private String bio;

    @CreationTimestamp
    @Column(name = "data_create", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "usuario")
    private List<Viagens> viagens;
}
