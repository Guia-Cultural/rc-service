package com.rotacultural.rcservice.DTOs.places;

import com.rotacultural.rcservice.entities.enums.Price;
import com.rotacultural.rcservice.entities.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull
    @Positive
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

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    @NotBlank
    @Size(max = 150)
    private String tempoAberto;

    @NotNull
    private Price price;

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    private Status status;
}
