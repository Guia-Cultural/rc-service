package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos")
@IdClass(FavoritesId.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Favorites {

    @Id
    @ManyToOne
    @JoinColumn(name = "idUser")
    private Users usuario;

    @Id
    @ManyToOne
    @JoinColumn(name = "idLoc")
    private Places places;

    @CreationTimestamp
    @Column(name = "data_create", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}