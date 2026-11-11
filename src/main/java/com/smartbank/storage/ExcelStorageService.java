package com.smartbank.storage;

import com.smartbank.model.Account;
import com.smartbank.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/** Excel file persistence for this classroom banking simulation. */
public class ExcelStorageService {
    private final Path file = Paths.get("data", "SmartBank.xlsx");
    private static final String ACCOUNTS = "Accounts";
    private static final String TRANSACTIONS = "Transactions";

    public ExcelStorageService() throws IOException { ensureWorkbook(); }
    public Path getFilePath() { return file.toAbsolutePath(); }

    private synchronized Workbook openWorkbook() throws IOException {
        Files.createDirectories(file.getParent());
        if (!Files.exists(file) || Files.size(file) == 0) {
            try (Workbook wb = new XSSFWorkbook()) { createSheets(wb); writeWorkbook(wb); }
        }
        try (InputStream in = Files.newInputStream(file)) { return WorkbookFactory.create(in); }
        catch (Exception ex) {
            throw new IOException("Cannot read " + file.toAbsolutePath() + ". Close Excel if it is open, then remove the damaged file and restart. Details: " + ex.getMessage(), ex);
        }
    }

    private void ensureWorkbook() throws IOException {
        Files.createDirectories(file.getParent());
        if (!Files.exists(file) || Files.size(file) == 0) {
            try (Workbook wb = new XSSFWorkbook()) { createSheets(wb); writeWorkbook(wb); }
        } else {
            try (Workbook wb = openWorkbook()) { if (wb.getSheet(ACCOUNTS) == null || wb.getSheet(TRANSACTIONS) == null) {
                if (wb.getSheet(ACCOUNTS) == null) createAccountsSheet(wb);
                if (wb.getSheet(TRANSACTIONS) == null) createTransactionsSheet(wb);
                writeWorkbook(wb);
            }}
        }
    }

    private void createSheets(Workbook wb) { createAccountsSheet(wb); createTransactionsSheet(wb); }
    private void createAccountsSheet(Workbook wb) {
        Sheet s = wb.createSheet(ACCOUNTS); Row h = s.createRow(0);
        String[] headers = {"Account Number", "Customer Name", "Account Type", "Balance"};
        for (int i=0;i<headers.length;i++) h.createCell(i).setCellValue(headers[i]);
        for (int i=0;i<headers.length;i++) s.setColumnWidth(i, i == 1 ? 7000 : 5000);
    }
    private void createTransactionsSheet(Workbook wb) {
        Sheet s = wb.createSheet(TRANSACTIONS); Row h = s.createRow(0);
        String[] headers = {"Transaction ID", "Date Time", "Account Number", "Type", "Amount", "Description", "Balance After"};
        for (int i=0;i<headers.length;i++) h.createCell(i).setCellValue(headers[i]);
        for (int i=0;i<headers.length;i++) s.setColumnWidth(i, 5500);
    }
    private void writeWorkbook(Workbook wb) throws IOException {
        Path temp = file.resolveSibling("SmartBank.tmp.xlsx");
        try (OutputStream out = Files.newOutputStream(temp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) { wb.write(out); }
        try { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException ex) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
    }

    public synchronized List<Account> loadAccounts() throws IOException {
        List<Account> result = new ArrayList<>();
        try (Workbook wb = openWorkbook()) {
            Sheet s = wb.getSheet(ACCOUNTS);
            for (int i=1;i<=s.getLastRowNum();i++) {
                Row r=s.getRow(i); if (r==null || r.getCell(0)==null) continue;
                result.add(new Account((long)r.getCell(0).getNumericCellValue(), text(r.getCell(1)), text(r.getCell(2)), number(r.getCell(3))));
            }
        }
        return result;
    }
    public synchronized void saveAccount(Account account) throws IOException {
        try (Workbook wb = openWorkbook()) {
            Sheet s=wb.getSheet(ACCOUNTS); Row target=null;
            for(int i=1;i<=s.getLastRowNum();i++){ Row r=s.getRow(i); if(r!=null && r.getCell(0)!=null && (long)r.getCell(0).getNumericCellValue()==account.getAccountNumber()){target=r;break;} }
            if(target==null) target=s.createRow(Math.max(1,s.getLastRowNum()+1));
            target.createCell(0).setCellValue(account.getAccountNumber()); target.createCell(1).setCellValue(account.getCustomerName());
            target.createCell(2).setCellValue(account.getAccountType()); target.createCell(3).setCellValue(account.getBalance());
            writeWorkbook(wb);
        }
    }
    public synchronized void saveTransaction(Transaction t) throws IOException {
        try (Workbook wb=openWorkbook()) {
            Sheet s=wb.getSheet(TRANSACTIONS); Row r=s.createRow(Math.max(1,s.getLastRowNum()+1));
            r.createCell(0).setCellValue(t.getTransactionId()); r.createCell(1).setCellValue(t.getDateTime().toString());
            r.createCell(2).setCellValue(t.getAccountNumber()); r.createCell(3).setCellValue(t.getType());
            r.createCell(4).setCellValue(t.getAmount()); r.createCell(5).setCellValue(t.getDescription()); r.createCell(6).setCellValue(t.getBalanceAfter());
            writeWorkbook(wb);
        }
    }
    public synchronized List<Transaction> loadTransactions() throws IOException {
        List<Transaction> result=new ArrayList<>();
        try(Workbook wb=openWorkbook()){
            Sheet s=wb.getSheet(TRANSACTIONS);
            for(int i=1;i<=s.getLastRowNum();i++){ Row r=s.getRow(i); if(r==null||r.getCell(0)==null)continue;
                result.add(new Transaction(text(r.getCell(0)), LocalDateTime.parse(text(r.getCell(1))), (long)number(r.getCell(2)), text(r.getCell(3)), number(r.getCell(4)), text(r.getCell(5)), number(r.getCell(6))));
            }
        }
        result.sort(Comparator.comparing(Transaction::getDateTime).reversed()); return result;
    }
    private String text(Cell c){ return c==null ? "" : c.getCellType()==CellType.STRING ? c.getStringCellValue() : new DataFormatter().formatCellValue(c); }
    private double number(Cell c){ return c==null ? 0 : c.getNumericCellValue(); }
}
