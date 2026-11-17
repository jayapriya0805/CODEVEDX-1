package com.library;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Scanner;

public class LibraryManagementSystem {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("       LIBRARY MANAGEMENT SYSTEM");
        System.out.println("======================================");

        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Database connected successfully!");

            boolean running = true;
            while (running) {
                showMenu();
                int choice = readInt("Enter your choice: ");

                switch (choice) {
                    case 1 -> addBook(connection);
                    case 2 -> viewAvailableBooks(connection);
                    case 3 -> issueBook(connection);
                    case 4 -> returnBook(connection);
                    case 5 -> viewAllBooks(connection);
                    case 6 -> viewIssuedBooks(connection);
                    case 0 -> {
                        running = false;
                        System.out.println("Thank you for using Library Management System!");
                    }
                    default -> System.out.println("Invalid choice. Please choose 0 to 6.");
                }
            }
        } catch (SQLException e) {
            System.out.println("\nDatabase connection or operation failed: " + e.getMessage());
            System.out.println("Check MySQL Server, database.sql and config.properties.");
        }
    }

    private static void showMenu() {
        System.out.println("\n--------------- MENU ---------------");
        System.out.println("1. Add Book");
        System.out.println("2. View Available Books");
        System.out.println("3. Issue Book");
        System.out.println("4. Return Book");
        System.out.println("5. View All Books");
        System.out.println("6. View Issued Books");
        System.out.println("0. Exit");
        System.out.println("------------------------------------");
    }

    private static void addBook(Connection connection) throws SQLException {
        System.out.println("\n--- ADD BOOK ---");
        String title = readText("Enter book title: ");
        String author = readText("Enter author name: ");
        int copies = readPositiveInt("Enter number of copies: ");

        String sql = "INSERT INTO books (title, author, total_copies, available_copies) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setInt(3, copies);
            statement.setInt(4, copies);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    System.out.println("Book added successfully! Book ID: " + keys.getInt(1));
                } else {
                    System.out.println("Book added successfully!");
                }
            }
        }
    }

    private static void viewAvailableBooks(Connection connection) throws SQLException {
        System.out.println("\n--- AVAILABLE BOOKS ---");
        String sql = "SELECT book_id, title, author, total_copies, available_copies "
                + "FROM books WHERE available_copies > 0 ORDER BY book_id";
        displayBooks(connection, sql, "No books are currently available.");
    }

    private static void viewAllBooks(Connection connection) throws SQLException {
        System.out.println("\n--- ALL BOOKS ---");
        String sql = "SELECT book_id, title, author, total_copies, available_copies "
                + "FROM books ORDER BY book_id";
        displayBooks(connection, sql, "No books found. Add a book first.");
    }

    private static void displayBooks(Connection connection, String sql, String emptyMessage)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            boolean found = false;
            System.out.printf("%-8s %-30s %-22s %-12s %-12s%n",
                    "ID", "TITLE", "AUTHOR", "TOTAL", "AVAILABLE");
            System.out.println("--------------------------------------------------------------------------------");

            while (result.next()) {
                found = true;
                System.out.printf("%-8d %-30s %-22s %-12d %-12d%n",
                        result.getInt("book_id"),
                        shorten(result.getString("title"), 28),
                        shorten(result.getString("author"), 20),
                        result.getInt("total_copies"),
                        result.getInt("available_copies"));
            }

            if (!found) {
                System.out.println(emptyMessage);
            }
        }
    }

    private static void issueBook(Connection connection) throws SQLException {
        System.out.println("\n--- ISSUE BOOK ---");
        int bookId = readPositiveInt("Enter book ID: ");
        String borrower = readText("Enter borrower name: ");

        String selectSql = "SELECT available_copies FROM books WHERE book_id = ? FOR UPDATE";
        String insertSql = "INSERT INTO book_issues "
                + "(book_id, borrower_name, issue_date, status) VALUES (?, ?, ?, 'ISSUED')";
        String updateSql = "UPDATE books SET available_copies = available_copies - 1 "
                + "WHERE book_id = ? AND available_copies > 0";

        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);

            int available;
            try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                select.setInt(1, bookId);
                try (ResultSet result = select.executeQuery()) {
                    if (!result.next()) {
                        connection.rollback();
                        System.out.println("Book ID not found.");
                        return;
                    }
                    available = result.getInt("available_copies");
                }
            }

            if (available <= 0) {
                connection.rollback();
                System.out.println("No copies are available for this book.");
                return;
            }

            try (PreparedStatement insert = connection.prepareStatement(
                    insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insert.setInt(1, bookId);
                insert.setString(2, borrower);
                insert.setDate(3, Date.valueOf(LocalDate.now()));
                insert.executeUpdate();

                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (keys.next()) {
                        System.out.println("Book issued successfully! Issue ID: " + keys.getInt(1));
                    } else {
                        System.out.println("Book issued successfully!");
                    }
                }
            }

            try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                update.setInt(1, bookId);
                update.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static void returnBook(Connection connection) throws SQLException {
        System.out.println("\n--- RETURN BOOK ---");
        int issueId = readPositiveInt("Enter issue ID: ");

        String selectSql = "SELECT book_id, status FROM book_issues WHERE issue_id = ? FOR UPDATE";
        String returnSql = "UPDATE book_issues SET return_date = ?, status = 'RETURNED' "
                + "WHERE issue_id = ? AND status = 'ISSUED'";
        String updateBookSql = "UPDATE books SET available_copies = available_copies + 1 "
                + "WHERE book_id = ? AND available_copies < total_copies";

        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);

            int bookId;
            String status;
            try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                select.setInt(1, issueId);
                try (ResultSet result = select.executeQuery()) {
                    if (!result.next()) {
                        connection.rollback();
                        System.out.println("Issue ID not found.");
                        return;
                    }
                    bookId = result.getInt("book_id");
                    status = result.getString("status");
                }
            }

            if (!"ISSUED".equalsIgnoreCase(status)) {
                connection.rollback();
                System.out.println("This book has already been returned.");
                return;
            }

            try (PreparedStatement updateIssue = connection.prepareStatement(returnSql)) {
                updateIssue.setDate(1, Date.valueOf(LocalDate.now()));
                updateIssue.setInt(2, issueId);
                updateIssue.executeUpdate();
            }

            try (PreparedStatement updateBook = connection.prepareStatement(updateBookSql)) {
                updateBook.setInt(1, bookId);
                updateBook.executeUpdate();
            }

            connection.commit();
            System.out.println("Book returned successfully!");
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static void viewIssuedBooks(Connection connection) throws SQLException {
        System.out.println("\n--- CURRENTLY ISSUED BOOKS ---");
        String sql = "SELECT i.issue_id, i.book_id, b.title, i.borrower_name, "
                + "i.issue_date, i.status FROM book_issues i "
                + "JOIN books b ON i.book_id = b.book_id "
                + "WHERE i.status = 'ISSUED' ORDER BY i.issue_id";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            boolean found = false;
            System.out.printf("%-10s %-8s %-28s %-22s %-14s%n",
                    "ISSUE ID", "BOOK ID", "TITLE", "BORROWER", "ISSUE DATE");
            System.out.println("--------------------------------------------------------------------------------");

            while (result.next()) {
                found = true;
                System.out.printf("%-10d %-8d %-28s %-22s %-14s%n",
                        result.getInt("issue_id"),
                        result.getInt("book_id"),
                        shorten(result.getString("title"), 26),
                        shorten(result.getString("borrower_name"), 20),
                        result.getDate("issue_date"));
            }

            if (!found) {
                System.out.println("There are no currently issued books.");
            }
        }
    }

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty. Try again.");
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Please enter a number greater than zero.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static String shorten(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
