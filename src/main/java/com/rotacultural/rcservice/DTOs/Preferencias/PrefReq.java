package com.rotacultural.rcservice.DTOs.Preferencias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrefReq {
    private long tripId;
    private long catId;
}
