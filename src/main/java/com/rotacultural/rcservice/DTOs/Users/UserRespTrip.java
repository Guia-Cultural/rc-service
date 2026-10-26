
package com.rotacultural.rcservice.DTOs.Users;

import com.rotacultural.rcservice.DTOs.Trips.TripResp;
import com.rotacultural.rcservice.entities.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRespTrip {

    private Long id;
    private String name;
    private String email;
    private String cep;
    private Role role;
    private String bio;
    private LocalDateTime dateCreate;
    private List<TripResp> trips;
}
