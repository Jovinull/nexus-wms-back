package br.com.nexus.nexus_wms.modules.tms.api.dto;

import br.com.nexus.nexus_wms.modules.tms.domain.entity.CnhCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverRequestDTO {

    @NotBlank(message = "O nome completo é obrigatório")
    private String fullName;

    @NotBlank(message = "A CNH é obrigatória")
    @Size(max = 20, message = "A CNH deve ter no máximo 20 caracteres")
    private String cnh;

    @NotNull(message = "A categoria da CNH é obrigatória")
    private CnhCategory cnhCategory;

    @Size(max = 30, message = "O telefone não pode exceder 30 caracteres")
    private String phone;

    private Boolean active;
}
