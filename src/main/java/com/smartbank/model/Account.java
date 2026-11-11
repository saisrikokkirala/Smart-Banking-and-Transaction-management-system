package com.smartbank.model;

/** Concrete account model extending the common BankAccount base class. */
public class Account extends BankAccount {
    private final String accountType;
    private double balance;

    public Account(long accountNumber, String customerName, String accountType, double balance) {
        super(accountNumber, customerName);
        if (accountType == null || accountType.isBlank()) throw new IllegalArgumentException("Choose an account type.");
        if (!Double.isFinite(balance) || balance < 0) throw new IllegalArgumentException("Opening balance cannot be negative.");
        this.accountType = accountType.trim();
        this.balance = balance;
    }
    public String getAccountType() { return accountType; }
    @Override public double getBalance() { return balance; }
    public void credit(double amount) { validateAmount(amount); balance += amount; }
    public void debit(double amount) {
        validateAmount(amount);
        if (amount > balance) throw new IllegalArgumentException("Insufficient balance. Available: ₹" + String.format("%.2f", balance));
        balance -= amount;
    }
    private void validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
    }
}
