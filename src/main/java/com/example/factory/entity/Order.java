package com.example.factory.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "customer_order")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String customerName;
    private String contact;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private String status; // Pending, Processing, Completed, Cancelled
    private double totalAmount;
    
    @ManyToOne
    private Product product;
    
    private int quantity;
}
