package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
@IdClass(FavoritesId.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Favorites {

    @Id
    @ManyToOne
    @JoinColumn(name = "idUser")
    private Users user;

    @Id
    @ManyToOne
    @JoinColumn(name = "idLoc")
    private Places place;

    @CreationTimestamp
    @Column(name = "date_create", nullable = false, updatable = false)
    private LocalDateTime dateCreate;
}