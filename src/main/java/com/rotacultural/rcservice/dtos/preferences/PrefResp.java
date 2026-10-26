
package com.rotacultural.rcservice.dtos.preferences;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrefResp {

    private Long id;
    private Long tripId;
    private Long catId;
}
