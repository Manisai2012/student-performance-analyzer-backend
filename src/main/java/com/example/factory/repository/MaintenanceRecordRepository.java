package com.example.factory.repository;
import com.example.factory.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {}
