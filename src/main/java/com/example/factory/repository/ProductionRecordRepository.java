package com.example.factory.repository;
import com.example.factory.entity.ProductionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductionRecordRepository extends JpaRepository<ProductionRecord, Long> {}
