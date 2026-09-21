package com.rotacultural.rcservice.DTOs.Feedbacks;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbacksResp {
    private long id;
    private long userId;
    private long locId;

    private Double avaliacao;
    private String comentario;


    private LocalDateTime dataCriacao;
}
