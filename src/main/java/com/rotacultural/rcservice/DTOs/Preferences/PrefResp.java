
package com.rotacultural.rcservice.DTOs.Preferences;

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
