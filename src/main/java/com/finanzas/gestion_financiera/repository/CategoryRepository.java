package com.finanzas.gestion_financiera.repository;


import com.finanzas.gestion_financiera.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}