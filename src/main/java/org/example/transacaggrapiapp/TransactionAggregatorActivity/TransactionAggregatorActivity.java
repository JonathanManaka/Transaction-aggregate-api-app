package org.example.transacaggrapiapp.TransactionAggregatorActivity;

import io.temporal.activity.ActivityInterface;
import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;

import java.util.List;


@ActivityInterface
public interface TransactionAggregatorActivity {
    List<Transaction> fetchBankATransactions();
    List<Transaction> fetchBankBTransactions();

    void normalise(Transaction tx);
    void categorise(Transaction tx);
    void save(Transaction tx);
    Boolean isPresent(Transaction tx);

}
