package br.com.nexus.nexus_wms.modules.wms.infra.service;

import br.com.nexus.nexus_wms.modules.wms.api.dto.StockMovementDTO;
import br.com.nexus.nexus_wms.modules.wms.domain.entity.Product;
import br.com.nexus.nexus_wms.modules.wms.domain.entity.Stock;
import br.com.nexus.nexus_wms.modules.wms.domain.entity.WarehouseLocation;
import br.com.nexus.nexus_wms.modules.wms.domain.repository.ProductRepository;
import br.com.nexus.nexus_wms.modules.wms.domain.repository.StockRepository;
import br.com.nexus.nexus_wms.modules.wms.domain.repository.WarehouseLocationRepository;
import br.com.nexus.nexus_wms.core.exception.BusinessException;
import br.com.nexus.nexus_wms.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseLocationRepository locationRepository;

    public StockService(StockRepository stockRepository, ProductRepository productRepository,
            WarehouseLocationRepository locationRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public void receiveStock(StockMovementDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

        WarehouseLocation location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada."));

        // Se fosse cenário real com suporte a data de validade, procuraria
        // uma posicao exata do mesmo lote/validade lá. Aqui validaremos só por Produto
        // e Lugar por simplicidade

        Optional<Stock> existingStockOpt = stockRepository.findByProductId(dto.getProductId()).stream()
                .filter(s -> s.getLocation().getId().equals(dto.getLocationId()))
                .findFirst();

        if (existingStockOpt.isPresent()) {
            Stock stock = existingStockOpt.get();
            stock.setQuantity(stock.getQuantity() + dto.getQuantity());
            stockRepository.save(stock);
        } else {
            Stock novoLote = new Stock();
            novoLote.setProduct(product);
            novoLote.setLocation(location);
            novoLote.setQuantity(dto.getQuantity());
            novoLote.setBatchNumber(dto.getBatchNumber());
            stockRepository.save(novoLote);
        }
    }

    @Transactional
    public void removeStock(StockMovementDTO dto) {
        // Encontra todo o volume que tem do produto no endereço
        Optional<Stock> existingStockOpt = stockRepository.findByProductId(dto.getProductId()).stream()
                .filter(s -> s.getLocation().getId().equals(dto.getLocationId()))
                .findFirst();

        if (existingStockOpt.isEmpty()) {
            throw new BusinessException("Não há estoque deste produto nesta localização.");
        }

        Stock stock = existingStockOpt.get();

        if (stock.getQuantity() < dto.getQuantity()) {
            throw new BusinessException("Quantidade insuficiente de estoque. Disponível: " + stock.getQuantity());
        }

        stock.setQuantity(stock.getQuantity() - dto.getQuantity());

        // Regra adicional: Se zerou, deleta o registro físico para liberar a
        // prateleira.
        if (stock.getQuantity() == 0) {
            stockRepository.delete(stock);
        } else {
            stockRepository.save(stock);
        }
    }
}
