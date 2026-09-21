package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class Feedbacks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFeed;

    @ManyToOne
    @JoinColumn(name = "idUser")
    private Usuarios usuario;

    @ManyToOne
    @JoinColumn(name = "idLoc")
    private Locais locais;

    @NotNull
    @Min(0)
    @Max(5)
    private Double avaliacao;

    @Size(max = 500)
    private String comentario;

    @CreationTimestamp
    @Column(name = "data_create")
    private LocalDateTime dataCriacao;

}
