package com.smartbank.model;

/** Base class demonstrating inheritance for different bank account types. */
public abstract class BankAccount {
    private final long accountNumber;
    private final String customerName;

    protected BankAccount(long accountNumber, String customerName) {
        if (accountNumber <= 0) throw new IllegalArgumentException("Account number must be positive.");
        if (customerName == null || customerName.isBlank()) throw new IllegalArgumentException("Customer name is required.");
        this.accountNumber = accountNumber;
        this.customerName = customerName.trim();
    }
    public long getAccountNumber() { return accountNumber; }
    public String getCustomerName() { return customerName; }
    public abstract double getBalance();
}
