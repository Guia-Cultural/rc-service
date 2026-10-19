package com.rotacultural.rcservice.entities;

import com.rotacultural.rcservice.entities.enums.Priece;
import com.rotacultural.rcservice.entities.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Places {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLoc;

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotBlank
    @Size(max = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCat")
    private Categories categoria;

    @NotBlank
    @Size(max = 150)
    private String endereco;

    @NotBlank
    @Size(max = 100)
    private String cidade;

    @NotBlank
    @Size(max = 150)
    private String estado;

    @NotBlank
    @Size(max = 100)
    private String pais;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @NotBlank
    @Size(max = 150)
    private String tempoAberto;

    @Enumerated(EnumType.STRING)
    private Priece priece;

    @ManyToOne(optional = false)
    @JoinColumn(name = "criado_por", nullable = false)
    private Users criadoPor;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Column(name = "data_create", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;




}
