package com.example.factory.controller;

import com.example.factory.entity.Worker;
import com.example.factory.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
@CrossOrigin(origins = "*")
public class WorkerController {

    @Autowired
    private WorkerRepository repository;

    @GetMapping
    public List<Worker> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Worker create(@RequestBody Worker entity) {
        return repository.save(entity);
    }

    @PutMapping("/{id}")
    public Worker update(@PathVariable Long id, @RequestBody Worker entityDetails) {
        entityDetails.setId(id);
        return repository.save(entityDetails);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
