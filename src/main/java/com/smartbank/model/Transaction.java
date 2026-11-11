package com.smartbank.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private final String transactionId;
    private final LocalDateTime dateTime;
    private final long accountNumber;
    private final String type;
    private final double amount;
    private final String description;
    private final double balanceAfter;

    public Transaction(String transactionId, LocalDateTime dateTime, long accountNumber, String type,
                       double amount, String description, double balanceAfter) {
        this.transactionId = transactionId; this.dateTime = dateTime; this.accountNumber = accountNumber;
        this.type = type; this.amount = amount; this.description = description; this.balanceAfter = balanceAfter;
    }
    public String getTransactionId() { return transactionId; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getDateText() { return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")); }
    public long getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public double getBalanceAfter() { return balanceAfter; }
}
