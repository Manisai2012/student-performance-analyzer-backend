package com.example.factory.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class ProductionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate date;
    private int targetQuantity;
    private int actualQuantity;
    private int rejectedQuantity;
    
    @ManyToOne
    private Machine machine;
    
    @ManyToOne
    private Product product;
}
