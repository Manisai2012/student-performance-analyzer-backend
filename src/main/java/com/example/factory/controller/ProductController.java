package com.example.factory.controller;

import com.example.factory.entity.Product;
import com.example.factory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductRepository repository;

    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Product create(@RequestBody Product entity) {
        return repository.save(entity);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product entityDetails) {
        entityDetails.setId(id);
        return repository.save(entityDetails);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
