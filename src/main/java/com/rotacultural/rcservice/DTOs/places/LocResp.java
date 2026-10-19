package com.rotacultural.rcservice.DTOs.places;

import com.rotacultural.rcservice.entities.enums.Price;
import com.rotacultural.rcservice.entities.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocResp {
    private Long id;
    private String nome;
    private String descricao;
    private Long catId;
    private String endereco;
    private String cidade;
    private String estado;
    private String pais;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String tempoAberto;
    private Price price;
    private Long userId;
    private Status status;
    private LocalDateTime dataCriacao;
}
