package br.com.nexus.nexus_wms.application.service;

import br.com.nexus.nexus_wms.application.dto.wms.ProductRequestDTO;
import br.com.nexus.nexus_wms.application.dto.wms.ProductResponseDTO;
import br.com.nexus.nexus_wms.domain.entity.wms.Category;
import br.com.nexus.nexus_wms.domain.entity.wms.Product;
import br.com.nexus.nexus_wms.domain.repository.CategoryRepository;
import br.com.nexus.nexus_wms.domain.repository.ProductRepository;
import br.com.nexus.nexus_wms.infrastructure.exception.BusinessException;
import br.com.nexus.nexus_wms.infrastructure.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {
        Product product = getProductOrThrow(id);
        return toResponseDTO(product);
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO dto) {
        try {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new BusinessException("Categoria inválida ou não encontrada."));

            Product product = new Product();
            mapDtoToEntity(dto, product, category);

            product = productRepository.save(product);
            return toResponseDTO(product);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Já existe um produto com o SKU fornecido.");
        }
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        try {
            Product product = getProductOrThrow(id);
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new BusinessException("Categoria inválida ou não encontrada."));

            mapDtoToEntity(dto, product, category);

            product = productRepository.save(product);
            return toResponseDTO(product);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Já existe um produto com o SKU fornecido.");
        }
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProductOrThrow(id);
        if (!product.getStocks().isEmpty()) {
            throw new BusinessException("Não é possível excluir um produto que possui registros de estoque.");
        }
        productRepository.delete(product);
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado para o ID: " + id));
    }

    private void mapDtoToEntity(ProductRequestDTO dto, Product product, Category category) {
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(category);
        product.setAbcCurve(dto.getAbcCurve());
        product.setWeightKg(dto.getWeightKg());
        product.setHeightCm(dto.getHeightCm());
        product.setWidthCm(dto.getWidthCm());
        product.setDepthCm(dto.getDepthCm());
        product.setUnitPrice(dto.getUnitPrice());
        product.setMinStockLevel(dto.getMinStockLevel());
        product.setActive(dto.getActive() != null ? dto.getActive() : true);
    }

    private ProductResponseDTO toResponseDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getAbcCurve(),
                product.getWeightKg(),
                product.getHeightCm(),
                product.getWidthCm(),
                product.getDepthCm(),
                product.getUnitPrice(),
                product.getMinStockLevel(),
                product.getActive(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
