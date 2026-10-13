package com.example.factory.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class MaintenanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String type;
    private String description;
    private LocalDate date;
    private double cost;
    private String status; // Pending, Completed
    
    @ManyToOne
    private Machine machine;
    
    @ManyToOne
    private Worker worker;
}
