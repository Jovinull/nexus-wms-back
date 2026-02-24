package br.com.nexus.nexus_wms.domain.entity.order;

import br.com.nexus.nexus_wms.domain.entity.BaseEntity;
import br.com.nexus.nexus_wms.domain.entity.wms.Product;
import br.com.nexus.nexus_wms.domain.entity.wms.Stock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_picking_list_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PickingListItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picking_list_id", nullable = false)
    private PickingList pickingList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "pick_sequence", nullable = false)
    private Integer pickSequence = 0;

    @Column(nullable = false)
    private Boolean picked = false;
}
