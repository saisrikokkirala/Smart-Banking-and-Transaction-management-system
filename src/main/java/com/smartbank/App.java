
package com.smartbank;

import com.smartbank.model.Account;
import com.smartbank.model.Transaction;
import com.smartbank.service.BankingService;
import com.smartbank.storage.ExcelStorageService;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.function.Function;

public class App extends Application {

    private BankingService bank;
    private Stage stage;
    private BorderPane root;

    private TableView<Account> accountTable;
    private TableView<Transaction> transactionTable;

    @Override
    public void start(Stage stage) {

        this.stage = stage;

        try {
            bank = new BankingService(new ExcelStorageService());
        } catch (Exception ex) {
            showError("Could not start SmartBank", ex.getMessage());
            return;
        }

        root = new BorderPane();
        root.setTop(header());
        root.setLeft(sidebar());

        Scene scene = new Scene(root, 1180, 760);

        var css = getClass().getResource("/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("SmartBank | Banking & Transaction Management");
        stage.setMinWidth(980);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();

        // showDashboard() updates root directly.
        showDashboard();
    }

    // ---------------- HEADER ----------------

    private HBox header() {

        Label brand = new Label("✦  SMARTBANK");
        brand.getStyleClass().add("brand");

        Label sub = new Label("BANKING & TRANSACTION MANAGEMENT");
        sub.getStyleClass().add("subtle");

        VBox left = new VBox(3, brand, sub);

        HBox bar = new HBox(left);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(18, 26, 18, 26));
        bar.getStyleClass().add("header");

        return bar;
    }

    // ---------------- SIDEBAR ----------------

    private VBox sidebar() {

        VBox side = new VBox(10);
        side.setPadding(new Insets(24, 14, 20, 14));
        side.setPrefWidth(205);
        side.getStyleClass().add("sidebar");

        Label menu = new Label("MAIN MENU");
        menu.getStyleClass().add("side-heading");

        side.getChildren().add(menu);

        addNav(side, "Overview", this::showDashboard);
        addNav(side, "Create Account", this::showCreateAccount);
        addNav(side, "Deposit", this::showDeposit);
        addNav(side, "Withdraw", this::showWithdraw);
        addNav(side, "Transfer", this::showTransfer);
        addNav(side, "Transactions", this::showTransactions);
        addNav(side, "Statements", this::showStatements);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label footer = new Label("DEMO PROJECT • EXCEL STORAGE");
        footer.getStyleClass().add("subtle");

        side.getChildren().addAll(spacer, footer);

        return side;
    }

    private void addNav(VBox side, String name, Runnable action) {

        Button button = new Button(name);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> action.run());

        side.getChildren().add(button);
    }

    // ---------------- COMMON PAGE ----------------

    private VBox page(String title, String description) {

        Label heading = new Label(title);
        heading.getStyleClass().add("page-title");

        Label desc = new Label(description);
        desc.getStyleClass().add("subtle");

        VBox box = new VBox(7, heading, desc);
        box.setPadding(new Insets(28, 30, 18, 30));

        return box;
    }

    private VBox formPage(String title, String description) {

        VBox box = new VBox(18);
        box.getChildren().add(page(title, description));
        box.setPadding(new Insets(0, 30, 30, 30));

        return box;
    }

    private VBox formCard() {

        VBox card = new VBox(14);
        card.getStyleClass().add("panel");
        card.setPadding(new Insets(24));
        card.setMaxWidth(650);

        return card;
    }

    private VBox field(String label, Control control) {

        Label name = new Label(label);
        name.getStyleClass().add("field-label");

        control.setMaxWidth(Double.MAX_VALUE);

        return new VBox(7, name, control);
    }

    private Button primary(String text) {

        Button button = new Button(text);
        button.getStyleClass().add("primary-button");

        return button;
    }

    // ---------------- DASHBOARD ----------------

    private void showDashboard() {

        VBox content = new VBox(20);

        content.getChildren().add(
            page("Overview", "Welcome to your SmartBank workspace.")
        );

        double totalBalance = bank.getAccounts()
            .stream()
            .mapToDouble(Account::getBalance)
            .sum();

        HBox stats = new HBox(
            15,
            statCard("TOTAL ACCOUNTS",
                String.valueOf(bank.getAccounts().size())),
            statCard("TOTAL BALANCE", money(totalBalance)),
            statCard("STORAGE", "Excel workbook")
        );

        stats.setPadding(new Insets(0, 30, 0, 30));
        content.getChildren().add(stats);

        Label heading = new Label("Customer Accounts");
        heading.getStyleClass().add("section-title");

        accountTable = createAccountTable();

        VBox card = new VBox(14, heading, accountTable);
        card.getStyleClass().add("panel");
        card.setPadding(new Insets(20));

        VBox.setVgrow(accountTable, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);
        VBox.setVgrow(content, Priority.ALWAYS);

        content.getChildren().add(card);
        content.setPadding(new Insets(0, 0, 25, 0));

        root.setCenter(content);

        refreshAccounts();
    }

    private VBox statCard(String title, String value) {

        Label heading = new Label(title);
        heading.getStyleClass().add("stat-label");

        Label amount = new Label(value);
        amount.getStyleClass().add("stat-value");

        VBox card = new VBox(10, heading, amount);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(20));

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    // ---------------- ACCOUNT TABLE ----------------

    private TableView<Account> createAccountTable() {

        TableView<Account> table = new TableView<>();

        table.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        table.getColumns().add(
            column("Account Number",
                a -> String.valueOf(a.getAccountNumber()))
        );

        table.getColumns().add(
            column("Customer Name", Account::getCustomerName)
        );

        table.getColumns().add(
            column("Account Type", Account::getAccountType)
        );

        table.getColumns().add(
            column("Available Balance",
                a -> money(a.getBalance()))
        );

        return table;
    }

    private <T> TableColumn<T, String> column(
        String title,
        Function<T, String> function
    ) {

        TableColumn<T, String> column = new TableColumn<>(title);

        column.setCellValueFactory(data ->
            new SimpleStringProperty(
                function.apply(data.getValue())
            )
        );

        return column;
    }

    private void refreshAccounts() {

        if (accountTable != null) {
            accountTable.setItems(
                FXCollections.observableArrayList(bank.getAccounts())
            );
        }
    }

    // ---------------- CREATE ACCOUNT ----------------

    private void showCreateAccount() {

        VBox content = formPage(
            "Create Customer Account",
            "Register a new customer and set an opening balance."
        );

        VBox card = formCard();

        TextField name = new TextField();
        name.setPromptText("Enter full customer name");

        ComboBox<String> type = new ComboBox<>(
            FXCollections.observableArrayList("Savings", "Current")
        );
        type.setPromptText("Select account type");

        TextField opening = new TextField("0");
        opening.setPromptText("Opening balance");

        Button create = primary("Create Account");

        Label result = new Label();

        card.getChildren().addAll(
            field("Customer name", name),
            field("Account type", type),
            field("Opening balance (₹)", opening),
            create,
            result
        );

        create.setOnAction(e -> {

            try {

                if (name.getText().isBlank()) {
                    throw new IllegalArgumentException(
                        "Enter the customer's name."
                    );
                }

                if (type.getValue() == null) {
                    throw new IllegalArgumentException(
                        "Select an account type."
                    );
                }

                Account account = bank.createAccount(
                    name.getText().trim(),
                    type.getValue(),
                    amount(opening.getText())
                );

                result.setText(
                    "Account created successfully! Account number: "
                    + account.getAccountNumber()
                );

                name.clear();
                opening.setText("0");
                type.setValue(null);

                refreshAccounts();

            } catch (Exception ex) {
                showError("Account creation failed", ex.getMessage());
            }
        });

        content.getChildren().add(card);
        root.setCenter(content);
    }

    // ---------------- DEPOSIT ----------------

    private void showDeposit() {

        showMoneyOperation(
            "Deposit Money",
            "Add money to a customer's account.",
            true
        );
    }

    // ---------------- WITHDRAW ----------------

    private void showWithdraw() {

        showMoneyOperation(
            "Withdraw Money",
            "Withdraw money only when sufficient balance is available.",
            false
        );
    }

    private void showMoneyOperation(
        String title,
        String description,
        boolean deposit
    ) {

        VBox content = formPage(title, description);
        VBox card = formCard();

        ComboBox<Account> choice = accountCombo();

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount in ₹");

        Button go = primary(
            deposit ? "Deposit" : "Withdraw"
        );

        Label result = new Label();

        card.getChildren().addAll(
            field("Select account", choice),
            field("Amount (₹)", amountField),
            go,
            result
        );

        go.setOnAction(e -> {

            try {

                if (choice.getValue() == null) {
                    throw new IllegalArgumentException(
                        "Please select an account."
                    );
                }

                long accountNumber =
                    choice.getValue().getAccountNumber();

                double value = amount(amountField.getText());

                if (deposit) {
                    bank.deposit(accountNumber, value);
                } else {
                    bank.withdraw(accountNumber, value);
                }

                result.setText(
                    (deposit ? "Deposit" : "Withdrawal")
                    + " successful. New balance: "
                    + money(bank.getAccount(accountNumber).getBalance())
                );

                amountField.clear();
                refreshAccounts();

                choice.setItems(
                    FXCollections.observableArrayList(bank.getAccounts())
                );

            } catch (Exception ex) {
                showError(
                    deposit ? "Deposit failed" : "Withdrawal failed",
                    ex.getMessage()
                );
            }
        });

        content.getChildren().add(card);
        root.setCenter(content);
    }

    // ---------------- TRANSFER ----------------

    private void showTransfer() {

        VBox content = formPage(
            "Transfer Money",
            "Move funds between two SmartBank accounts."
        );

        VBox card = formCard();

        ComboBox<Account> from = accountCombo();
        ComboBox<Account> to = accountCombo();

        TextField amountField = new TextField();
        amountField.setPromptText("Enter transfer amount");

        Button go = primary("Transfer Funds");

        Label result = new Label();

        card.getChildren().addAll(
            field("From account", from),
            field("To account", to),
            field("Amount (₹)", amountField),
            go,
            result
        );

        go.setOnAction(e -> {

            try {

                if (from.getValue() == null || to.getValue() == null) {
                    throw new IllegalArgumentException(
                        "Please select both accounts."
                    );
                }

                long sender = from.getValue().getAccountNumber();
                long receiver = to.getValue().getAccountNumber();

                if (sender == receiver) {
                    throw new IllegalArgumentException(
                        "Sender and receiver accounts must be different."
                    );
                }

                double value = amount(amountField.getText());

                bank.transfer(sender, receiver, value);

                result.setText(
                    "Transfer completed successfully!"
                );

                amountField.clear();
                refreshAccounts();

                from.setItems(
                    FXCollections.observableArrayList(bank.getAccounts())
                );

                to.setItems(
                    FXCollections.observableArrayList(bank.getAccounts())
                );

            } catch (Exception ex) {
                showError("Transfer failed", ex.getMessage());
            }
        });

        content.getChildren().add(card);
        root.setCenter(content);
    }

    // ---------------- ACCOUNT COMBOBOX ----------------

    private ComboBox<Account> accountCombo() {

        ComboBox<Account> combo = new ComboBox<>(
            FXCollections.observableArrayList(bank.getAccounts())
        );

        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setPromptText("Choose an account");

        combo.setConverter(new javafx.util.StringConverter<Account>() {

            @Override
            public String toString(Account account) {

                if (account == null) {
                    return "";
                }

                return account.getCustomerName()
                    + " — "
                    + account.getAccountNumber()
                    + " ("
                    + money(account.getBalance())
                    + ")";
            }

            @Override
            public Account fromString(String value) {
                return null;
            }
        });

        return combo;
    }

    // ---------------- TRANSACTION HISTORY ----------------

    private void showTransactions() {

        VBox content = formPage(
            "Transaction History",
            "View deposits, withdrawals and transfers for an account."
        );

        VBox card = formCard();

        ComboBox<Account> choice = accountCombo();

        Button load = primary("Load Transactions");

        transactionTable = createTransactionTable();

        card.getChildren().addAll(
            field("Select account", choice),
            load,
            transactionTable
        );

        load.setOnAction(e -> {

            try {

                if (choice.getValue() == null) {
                    throw new IllegalArgumentException(
                        "Select an account first."
                    );
                }

                transactionTable.setItems(
                    FXCollections.observableArrayList(
                        bank.getTransactions(
                            choice.getValue().getAccountNumber()
                        )
                    )
                );

            } catch (Exception ex) {
                showError(
                    "Could not load transactions",
                    ex.getMessage()
                );
            }
        });

        VBox.setVgrow(transactionTable, Priority.ALWAYS);

        content.getChildren().add(card);
        root.setCenter(content);
    }

    private TableView<Transaction> createTransactionTable() {

        TableView<Transaction> table = new TableView<>();

        table.setPrefHeight(400);

        table.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        table.getColumns().add(
            column("Date", Transaction::getDateText)
        );

        table.getColumns().add(
            column("Type", Transaction::getType)
        );

        table.getColumns().add(
            column("Amount", t -> money(t.getAmount()))
        );

        table.getColumns().add(
            column("Description", Transaction::getDescription)
        );

        table.getColumns().add(
            column("Balance After", t -> money(t.getBalanceAfter()))
        );

        return table;
    }

    // ---------------- ACCOUNT STATEMENTS ----------------

    private void showStatements() {

        VBox content = formPage(
            "Account Statements",
            "Export an account's transaction statement to a separate Excel file."
        );

        VBox card = formCard();

        ComboBox<Account> choice = accountCombo();

        Button export = primary("Export Excel Statement");

        Label status = new Label(
            "Choose an account to create its statement."
        );

        card.getChildren().addAll(
            field("Select account", choice),
            export,
            status
        );

        export.setOnAction(e -> {

            try {

                if (choice.getValue() == null) {
                    throw new IllegalArgumentException(
                        "Select an account first."
                    );
                }

                FileChooser chooser = new FileChooser();

                chooser.setTitle("Save Account Statement");

                chooser.setInitialFileName(
                    "Statement_"
                    + choice.getValue().getAccountNumber()
                    + ".xlsx"
                );

                chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                        "Excel Workbook",
                        "*.xlsx"
                    )
                );

                File file = chooser.showSaveDialog(stage);

                if (file == null) {
                    return;
                }

                exportStatement(choice.getValue(), file);

                status.setText(
                    "Statement saved: " + file.getAbsolutePath()
                );

            } catch (Exception ex) {
                showError(
                    "Statement export failed",
                    ex.getMessage()
                );
            }
        });

        content.getChildren().add(card);
        root.setCenter(content);
    }

    private void exportStatement(Account account, File file)
        throws Exception {

        List<Transaction> transactions =
            bank.getTransactions(account.getAccountNumber());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Account Statement");

            String[][] rows = {
                {"SMARTBANK ACCOUNT STATEMENT"},
                {"Account Number",
                    String.valueOf(account.getAccountNumber())},
                {"Customer Name", account.getCustomerName()},
                {"Account Type", account.getAccountType()},
                {"Current Balance", String.valueOf(account.getBalance())},
                {},
                {"Date", "Type", "Amount", "Description", "Balance After"}
            };

            for (int i = 0; i < rows.length; i++) {

                Row row = sheet.createRow(i);

                for (int j = 0; j < rows[i].length; j++) {
                    row.createCell(j).setCellValue(rows[i][j]);
                }
            }

            int rowNumber = 7;

            for (Transaction transaction : transactions) {

                Row row = sheet.createRow(rowNumber++);

                row.createCell(0).setCellValue(
                    transaction.getDateText()
                );

                row.createCell(1).setCellValue(
                    transaction.getType()
                );

                row.createCell(2).setCellValue(
                    transaction.getAmount()
                );

                row.createCell(3).setCellValue(
                    transaction.getDescription()
                );

                row.createCell(4).setCellValue(
                    transaction.getBalanceAfter()
                );
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream output =
                    new FileOutputStream(file)) {

                workbook.write(output);
            }
        }
    }

    // ---------------- VALIDATION ----------------

    private double amount(String value) {

        try {

            double number = Double.parseDouble(value.trim());

            if (!Double.isFinite(number) || number <= 0) {
                throw new NumberFormatException();
            }

            return number;

        } catch (Exception ex) {

            throw new IllegalArgumentException(
                "Enter a valid amount greater than zero."
            );
        }
    }

    private String money(double value) {
        return String.format("₹%,.2f", value);
    }

    // ---------------- ERROR MESSAGE ----------------

    private void showError(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("SmartBank");
        alert.setHeaderText(title);

        alert.setContentText(
            message == null
                ? "An unexpected error occurred."
                : message
        );

        alert.showAndWait();
    }

    @Override
    public void stop() {
        // Application cleanup can be added here if required.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
