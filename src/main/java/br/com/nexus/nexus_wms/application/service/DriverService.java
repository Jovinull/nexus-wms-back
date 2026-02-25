package br.com.nexus.nexus_wms.application.service;

import br.com.nexus.nexus_wms.application.dto.tms.DriverRequestDTO;
import br.com.nexus.nexus_wms.application.dto.tms.DriverResponseDTO;
import br.com.nexus.nexus_wms.domain.entity.tms.Driver;
import br.com.nexus.nexus_wms.domain.repository.DriverRepository;
import br.com.nexus.nexus_wms.infrastructure.exception.BusinessException;
import br.com.nexus.nexus_wms.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional
    public DriverResponseDTO create(DriverRequestDTO dto) {
        if (driverRepository.findByCnh(dto.getCnh()).isPresent()) {
            throw new BusinessException("Já existe um motorista cadastrado com esta CNH.");
        }

        Driver driver = new Driver();
        mapDtoToEntity(dto, driver);

        if (dto.getActive() == null) {
            driver.setActive(true);
        }

        driver = driverRepository.save(driver);
        return toResponseDTO(driver);
    }

    @Transactional(readOnly = true)
    public Page<DriverResponseDTO> findAll(Pageable pageable) {
        return driverRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public DriverResponseDTO findById(Long id) {
        Driver driver = getDriverOrThrow(id);
        return toResponseDTO(driver);
    }

    @Transactional
    public DriverResponseDTO update(Long id, DriverRequestDTO dto) {
        Driver driver = getDriverOrThrow(id);

        driverRepository.findByCnh(dto.getCnh())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException("Já existe outro motorista usando esta CNH.");
                    }
                });

        mapDtoToEntity(dto, driver);

        if (dto.getActive() != null) {
            driver.setActive(dto.getActive());
        }

        driver = driverRepository.save(driver);
        return toResponseDTO(driver);
    }

    @Transactional
    public void delete(Long id) {
        Driver driver = getDriverOrThrow(id);
        driverRepository.delete(driver);
    }

    private Driver getDriverOrThrow(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado para o ID: " + id));
    }

    private void mapDtoToEntity(DriverRequestDTO dto, Driver driver) {
        driver.setFullName(dto.getFullName());
        driver.setCnh(dto.getCnh());
        driver.setCnhCategory(dto.getCnhCategory());
        driver.setPhone(dto.getPhone());
    }

    private DriverResponseDTO toResponseDTO(Driver driver) {
        return new DriverResponseDTO(
                driver.getId(),
                driver.getFullName(),
                driver.getCnh(),
                driver.getCnhCategory(),
                driver.getPhone(),
                driver.getActive(),
                driver.getCreatedAt(),
                driver.getUpdatedAt());
    }
}
