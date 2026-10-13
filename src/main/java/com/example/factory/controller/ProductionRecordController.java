package com.example.factory.controller;

import com.example.factory.entity.ProductionRecord;
import com.example.factory.repository.ProductionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productionrecords")
@CrossOrigin(origins = "*")
public class ProductionRecordController {

    @Autowired
    private ProductionRecordRepository repository;

    @GetMapping
    public List<ProductionRecord> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public ProductionRecord create(@RequestBody ProductionRecord entity) {
        return repository.save(entity);
    }

    @PutMapping("/{id}")
    public ProductionRecord update(@PathVariable Long id, @RequestBody ProductionRecord entityDetails) {
        entityDetails.setId(id);
        return repository.save(entityDetails);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
