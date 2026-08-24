package com.retailpipeline.consumer.repository;

import com.retailpipeline.consumer.entity.ProductStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStatsRepository extends JpaRepository<ProductStats, String> {}
