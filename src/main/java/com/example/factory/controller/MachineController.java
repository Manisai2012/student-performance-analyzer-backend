package com.example.factory.controller;

import com.example.factory.entity.Machine;
import com.example.factory.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
@CrossOrigin(origins = "*")
public class MachineController {

    @Autowired
    private MachineRepository repository;

    @GetMapping
    public List<Machine> getAllMachines() {
        return repository.findAll();
    }

    @PostMapping
    public Machine addMachine(@RequestBody Machine machine) {
        return repository.save(machine);
    }

    @PutMapping("/{id}")
    public Machine updateMachine(@PathVariable Long id, @RequestBody Machine machineDetails) {
        Machine machine = repository.findById(id).orElseThrow(() -> new RuntimeException("Machine not found"));
        machine.setName(machineDetails.getName());
        machine.setStatus(machineDetails.getStatus());
        machine.setProductionCount(machineDetails.getProductionCount());
        machine.setTemperature(machineDetails.getTemperature());
        return repository.save(machine);
    }

    @DeleteMapping("/{id}")
    public void deleteMachine(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
