# SmartBank — PS-09 Banking & Transaction Management System

A JavaFX desktop banking simulation using Java OOP and an Excel workbook (`data/SmartBank.xlsx`) for local persistence. This is a classroom/demo application, not production banking software.

## Requirements
- JDK 21
- Apache Maven 3.8+
- Internet access on the first Maven run to download JavaFX and Apache POI dependencies

## Run on macOS
1. Extract this project folder.
2. Open the extracted `SmartBankFresh` folder in VS Code.
3. In the VS Code terminal, check `java -version` and `mvn -version`.
4. Run:
   ```bash
   mvn clean javafx:run
   ```
5. The Excel workbook is created automatically at `data/SmartBank.xlsx` relative to the folder from which Maven is run.

## Features
- Create accounts with generated unique 10-digit account numbers
- Savings and Current account types
- Deposit and withdraw with amount validation and insufficient-funds protection
- Transfer between two different accounts
- Dashboard with account list and combined balance
- Transaction history per account
- Export a selected account statement to a separate `.xlsx` file
- Accounts and transactions persist in the `Accounts` and `Transactions` sheets

## Project structure
```text
SmartBankFresh/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/com/smartbank/
        │   ├── App.java
        │   ├── model/
        │   │   ├── BankAccount.java
        │   │   ├── Account.java
        │   │   └── Transaction.java
        │   ├── service/
        │   │   └── BankingService.java
        │   └── storage/
        │       └── ExcelStorageService.java
        └── resources/
            └── styles.css
```

## Notes
- Do not open `data/SmartBank.xlsx` in Excel while the app is writing to it.
- Keep the `data` folder with the project to retain saved demo data.
- If you intentionally want a fresh demo, close the app and delete `data/SmartBank.xlsx`; it will be recreated on next launch.
- The demo does not implement real authentication, encryption, interest, regulatory controls, or concurrent multi-user banking.
