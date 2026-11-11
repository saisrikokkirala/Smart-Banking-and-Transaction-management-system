package com.smartbank.service;

import com.smartbank.model.Account;
import com.smartbank.model.Transaction;
import com.smartbank.storage.ExcelStorageService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class BankingService {
    private final ExcelStorageService storage;
    private final Map<Long, Account> accounts = new LinkedHashMap<>();

    public BankingService(ExcelStorageService storage) throws IOException {
        this.storage=storage;
        for(Account a:storage.loadAccounts()) accounts.put(a.getAccountNumber(),a);
    }
    public List<Account> getAccounts(){ return new ArrayList<>(accounts.values()); }
    public Account getAccount(long number){ Account a=accounts.get(number); if(a==null)throw new IllegalArgumentException("Account not found."); return a; }

    public Account createAccount(String name,String type,double openingBalance) throws IOException {
        if(name==null||name.isBlank())throw new IllegalArgumentException("Enter the customer's name.");
        long number=generateAccountNumber();
        Account account=new Account(number,name,type,openingBalance);
        accounts.put(number,account);
        try { storage.saveAccount(account); }
        catch(IOException ex){accounts.remove(number);throw ex;}
        record(account,"ACCOUNT OPENED",openingBalance,"Opening balance");
        return account;
    }
    private long generateAccountNumber(){
        long n=1000000000L + new Random().nextInt(900000000);
        while(accounts.containsKey(n)) n++;
        return n;
    }
    public void deposit(long number,double amount) throws IOException {
        Account a=getAccount(number); a.credit(amount); persistAndRecord(a,"DEPOSIT",amount,"Cash deposit");
    }
    public void withdraw(long number,double amount) throws IOException {
        Account a=getAccount(number); a.debit(amount); persistAndRecord(a,"WITHDRAWAL",amount,"Cash withdrawal");
    }
    public void transfer(long from,long to,double amount) throws IOException {
        if(from==to)throw new IllegalArgumentException("Choose two different accounts.");
        Account source=getAccount(from), destination=getAccount(to);
        source.debit(amount); destination.credit(amount);
        storage.saveAccount(source); storage.saveAccount(destination);
        record(source,"TRANSFER SENT",amount,"Transfer to A/C " + to);
        record(destination,"TRANSFER RECEIVED",amount,"Transfer from A/C " + from);
    }
    private void persistAndRecord(Account a,String type,double amount,String description) throws IOException {
        storage.saveAccount(a); record(a,type,amount,description);
    }
    private void record(Account a,String type,double amount,String description) throws IOException {
        storage.saveTransaction(new Transaction(UUID.randomUUID().toString().substring(0,8).toUpperCase(), LocalDateTime.now(), a.getAccountNumber(), type, amount, description, a.getBalance()));
    }
    public List<Transaction> getTransactions(long accountNumber) throws IOException {
        List<Transaction> result=new ArrayList<>();
        for(Transaction t:storage.loadTransactions()) if(t.getAccountNumber()==accountNumber)result.add(t);
        return result;
    }
    public List<Transaction> getAllTransactions() throws IOException { return storage.loadTransactions(); }
    public String getStorageLocation(){return storage.getFilePath().toString();}
}
