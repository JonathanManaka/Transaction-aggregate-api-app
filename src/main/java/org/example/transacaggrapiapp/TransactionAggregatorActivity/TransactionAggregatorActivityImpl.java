package org.example.transacaggrapiapp.TransactionAggregatorActivity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transacaggrapiapp.aggregator.TransactionNormaliser;
import org.example.transacaggrapiapp.categoriser.TransactionCategoriser;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.repository.TransactionRepository;
import org.example.transacaggrapiapp.source.MockBankAClient;
import org.example.transacaggrapiapp.source.MockBankBClient;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class TransactionAggregatorActivityImpl implements TransactionAggregatorActivity {

    private MockBankAClient bankAClient;
    private MockBankBClient bankBClient;
    private TransactionNormaliser normaliser;
    private TransactionCategoriser categoriser;
    private TransactionRepository transactionRepository;

    @Override
    public List<Transaction> fetchBankATransactions() {
        return bankAClient.fetchTransactions();
    }

    @Override
    public List<Transaction> fetchBankBTransactions() {
        return bankBClient.fetchTransactions();
    }

    @Override
    public void normalise(Transaction tx) {
        normaliser.normalise(tx);
    }

    @Override
    public void categorise(Transaction tx) {
        categoriser.categoriseAndSet(tx);
    }

    @Override
    public void save(Transaction tx) {
        transactionRepository.save(tx);
    }

    @Override
    public Boolean isPresent(Transaction tx) {
        return transactionRepository.findByExternalIdAndSource(tx.getExternalId(), tx.getSource()).isPresent();
    }
}
