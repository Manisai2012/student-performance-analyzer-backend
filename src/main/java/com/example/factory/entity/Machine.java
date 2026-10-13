package com.example.factory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String type;
    private String department;
    private String location;
    private String status; // Running, Stopped, Maintenance
    private double temperature;
    private int operatingHours;
    private int productionCount;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    
    public String getHealthStatus() {
        if ("Stopped".equalsIgnoreCase(status) || temperature > 80.0) {
            return "Warning";
        }
        return "Normal";
    }
}
