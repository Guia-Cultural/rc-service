package com.rotacultural.rcservice.dtos.places;

import com.rotacultural.rcservice.entities.enums.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceStatusReq {
    @NotNull(message = "Status é obrigatório")
    private Status status;

    @Size(max = 500)
    private String rejectionReason;
}
