package com.example.factory.controller;

import com.example.factory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired private MachineRepository machineRepo;
    @Autowired private WorkerRepository workerRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private OrderRepository orderRepo;

    @GetMapping
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalMachines", machineRepo.count());
        summary.put("totalWorkers", workerRepo.count());
        summary.put("totalProducts", productRepo.count());
        summary.put("totalOrders", orderRepo.count());
        
        // Mock some dynamic values based on basic counts for demo purposes
        summary.put("runningMachines", machineRepo.findAll().stream().filter(m -> "Running".equalsIgnoreCase(m.getStatus())).count());
        summary.put("workersOnDuty", workerRepo.findAll().stream().filter(w -> "Present".equalsIgnoreCase(w.getAttendanceStatus())).count());
        
        return summary;
    }
}
