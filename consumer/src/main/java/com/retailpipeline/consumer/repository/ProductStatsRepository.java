package com.retailpipeline.consumer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.retailpipeline.consumer.entity.ProductStats;

public interface ProductStatsRepository extends JpaRepository<ProductStats, String> {
}
