package com.yuanzhang.econexus.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pm_customer")
public class Customer {
    @Id
    @UuidGenerator
    @Column(name = "customer_index", nullable = false)
    private String customerIndex;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_code", nullable = false, unique = true)
    private String customerCode;

    @Column(name = "area_code")
    private String areaCode;

}