package br.com.nexus.nexus_wms.modules.iam.api.dto;

import br.com.nexus.nexus_wms.modules.iam.domain.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private String refreshToken;

}
