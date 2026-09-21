package com.rotacultural.rcservice.DTOs.Locais;

import com.rotacultural.rcservice.entities.enums.Preco;
import com.rotacultural.rcservice.entities.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocReq {
    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotBlank
    @Size(max = 500)
    private String descricao;

    private Long catId;

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

    private BigDecimal latitude;

    private BigDecimal longitude;

    @NotBlank
    @Size(max = 150)
    private String tempoAberto;

    private Preco preco;

    private long userId;

    private Status status;
}
