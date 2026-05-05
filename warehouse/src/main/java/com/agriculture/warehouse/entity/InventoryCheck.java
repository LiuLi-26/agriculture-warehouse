package com.agriculture.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "inventory_check")
public class InventoryCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "check_no", unique = true, nullable = false)
    private String checkNo;  // 盘点单号

    @Column(name = "check_date")
    private LocalDate checkDate;  // 盘点日期

    @Column(name = "product_id")
    private Long productId;  // 商品ID

    @Column(name = "location_id")
    private Long locationId;  // 货位ID

    @Column(name = "system_quantity")
    private Integer systemQuantity;  // 系统库存

    @Column(name = "actual_quantity")
    private Integer actualQuantity;  // 实际库存

    private Integer difference;  // 差异数量

    @Column(length = 20)
    private String status;  // PENDING, COMPLETED, CONFIRMED

    private String remark;  // 备注

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}