package com.example.factory.controller;

import com.example.factory.entity.Order;
import com.example.factory.entity.Product;
import com.example.factory.repository.OrderRepository;
import com.example.factory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Order> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Order create(@RequestBody Order entity) {
        return repository.save(entity);
    }

    @PutMapping("/{id}")
    public Order update(@PathVariable Long id, @RequestBody Order entityDetails) {
        Optional<Order> existingOrderOpt = repository.findById(id);
        if (existingOrderOpt.isPresent()) {
            Order existingOrder = existingOrderOpt.get();
            // Check if status is changing to Completed
            if (!"Completed".equalsIgnoreCase(existingOrder.getStatus()) && "Completed".equalsIgnoreCase(entityDetails.getStatus())) {
                if (entityDetails.getProduct() != null && entityDetails.getProduct().getId() != null) {
                    Optional<Product> pOpt = productRepository.findById(entityDetails.getProduct().getId());
                    if (pOpt.isPresent()) {
                        Product p = pOpt.get();
                        if (p.getStockQuantity() >= entityDetails.getQuantity()) {
                            p.setStockQuantity(p.getStockQuantity() - entityDetails.getQuantity());
                            productRepository.save(p);
                        } else {
                            throw new RuntimeException("Insufficient stock for product " + p.getName());
                        }
                    }
                }
            }
        }
        entityDetails.setId(id);
        return repository.save(entityDetails);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
