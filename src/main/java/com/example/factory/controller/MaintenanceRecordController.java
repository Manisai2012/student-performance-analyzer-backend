package com.example.factory.controller;

import com.example.factory.entity.MaintenanceRecord;
import com.example.factory.repository.MaintenanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenancerecords")
@CrossOrigin(origins = "*")
public class MaintenanceRecordController {

    @Autowired
    private MaintenanceRecordRepository repository;

    @GetMapping
    public List<MaintenanceRecord> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public MaintenanceRecord create(@RequestBody MaintenanceRecord entity) {
        return repository.save(entity);
    }

    @PutMapping("/{id}")
    public MaintenanceRecord update(@PathVariable Long id, @RequestBody MaintenanceRecord entityDetails) {
        entityDetails.setId(id);
        return repository.save(entityDetails);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
