package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos")
@IdClass(FavoritosId.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Favoritos {

    @Id
    @ManyToOne
    @JoinColumn(name = "idUser")
    private Usuarios usuario;

    @Id
    @ManyToOne
    @JoinColumn(name = "idLoc")
    private Locais locais;

    @CreationTimestamp
    @Column(name = "data_create", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}