package com.rotacultural.rcservice.dtos.feedbacks;

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

    private Double assessment;
    private String commentary;


    private LocalDateTime dateCreate;
}
