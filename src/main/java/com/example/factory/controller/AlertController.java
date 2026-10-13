package com.example.factory.controller;

import com.example.factory.entity.Machine;
import com.example.factory.entity.Product;
import com.example.factory.repository.MachineRepository;
import com.example.factory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired private MachineRepository machineRepo;
    @Autowired private ProductRepository productRepo;

    @GetMapping
    public List<Map<String, String>> getAlerts() {
        List<Map<String, String>> alerts = new ArrayList<>();
        
        long idCounter = 1;
        for (Machine m : machineRepo.findAll()) {
            if (m.getTemperature() > 80.0) {
                alerts.add(createAlert(idCounter++, "Critical", "Machine " + m.getName() + " is overheating (" + m.getTemperature() + "C)", "High Temperature"));
            }
            if ("Stopped".equalsIgnoreCase(m.getStatus())) {
                alerts.add(createAlert(idCounter++, "Warning", "Machine " + m.getName() + " is currently stopped.", "Machine Stopped"));
            }
        }
        
        for (Product p : productRepo.findAll()) {
            if (p.getStockQuantity() < p.getMinStockLevel()) {
                alerts.add(createAlert(idCounter++, "Warning", "Product " + p.getName() + " stock (" + p.getStockQuantity() + ") is below minimum (" + p.getMinStockLevel() + ").", "Low Stock"));
            }
        }
        
        return alerts;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> resolveAlert(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("status", "Resolved");
        response.put("message", "Alert #" + id + " resolved successfully.");
        return ResponseEntity.ok(response);
    }

    private Map<String, String> createAlert(long id, String severity, String description, String type) {
        Map<String, String> alert = new HashMap<>();
        alert.put("id", String.valueOf(id));
        alert.put("severity", severity);
        alert.put("description", description);
        alert.put("type", type);
        alert.put("status", "Unresolved");
        alert.put("date", java.time.LocalDate.now().toString());
        return alert;
    }
}
