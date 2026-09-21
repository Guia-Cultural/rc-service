package com.rotacultural.rcservice.entities;

import com.rotacultural.rcservice.entities.enums.Preco;
import com.rotacultural.rcservice.entities.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Locais {
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
    private Categorias categoria;

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

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @NotBlank
    @Size(max = 150)
    private String tempoAberto;

    @Enumerated(EnumType.STRING)
    private Preco preco;

    @ManyToOne
    @JoinColumn(name ="idUser")
    private Usuarios usuario;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Column(name = "data_create")
    private LocalDateTime dataCadastro;





}
