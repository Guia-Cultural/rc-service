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
    private Preco preco;
    private Long userId;
    private Status status;
    private LocalDateTime dataCriacao;
}
