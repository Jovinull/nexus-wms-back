package br.com.nexus.nexus_wms.application.dto.auth;

import br.com.nexus.nexus_wms.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String accessToken;
    private Long expiresIn;
    private String name;
    private UserRole role;

}
