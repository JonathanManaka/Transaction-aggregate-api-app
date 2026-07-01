package org.example.transacaggrapiapp.repository;

import org.example.transacaggrapiapp.model.SourceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceTransactionRepository extends JpaRepository<SourceTransaction, Long> {

    List<SourceTransaction> findBySource(String source);
}
