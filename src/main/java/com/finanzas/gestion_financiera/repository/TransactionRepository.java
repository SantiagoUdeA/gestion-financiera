package com.finanzas.gestion_financiera.repository;


import com.finanzas.gestion_financiera.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUsuarioId(Long usuarioId);
}
