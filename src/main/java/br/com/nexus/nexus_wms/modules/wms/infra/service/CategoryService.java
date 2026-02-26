package br.com.nexus.nexus_wms.modules.wms.infra.service;

import br.com.nexus.nexus_wms.modules.wms.api.dto.CategoryRequestDTO;
import br.com.nexus.nexus_wms.modules.wms.api.dto.CategoryResponseDTO;
import br.com.nexus.nexus_wms.modules.wms.domain.entity.Category;
import br.com.nexus.nexus_wms.modules.wms.domain.repository.CategoryRepository;
import br.com.nexus.nexus_wms.core.exception.BusinessException;
import br.com.nexus.nexus_wms.core.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long id) {
        Category category = getCategoryOrThrow(id);
        return toResponseDTO(category);
    }

    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        try {
            Category category = new Category();
            category.setName(dto.getName());
            category.setDescription(dto.getDescription());

            category = categoryRepository.save(category);
            return toResponseDTO(category);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Já existe uma categoria com este nome.");
        }
    }

    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {
        try {
            Category category = getCategoryOrThrow(id);
            category.setName(dto.getName());
            category.setDescription(dto.getDescription());

            category = categoryRepository.save(category);
            return toResponseDTO(category);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Já existe uma categoria com este nome.");
        }
    }

    @Transactional
    public void delete(Long id) {
        Category category = getCategoryOrThrow(id);
        if (!category.getProducts().isEmpty()) {
            throw new BusinessException("Não é possível excluir uma categoria que possui produtos vinculados.");
        }
        categoryRepository.delete(category);
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada para o ID: " + id));
    }

    private CategoryResponseDTO toResponseDTO(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
