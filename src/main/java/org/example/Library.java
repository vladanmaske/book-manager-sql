package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Library {
    private static final String COMMA = ",";
    private static final String PATH_BOOKS = "src/main/resources/books.csv";
    private static final String PATH_USERS = "src/main/resources/users.csv";
    private static Library library;
    private final ConnectionManager connectionManager;

    private Library() {
        connectionManager = ConnectionManager.getInstance();
        createTables();
        putBooksIntoLibraryDB();
        putUsersIntoLibraryDB();
    }

    private void createTables() {
        try {
            String usersTable = "CREATE TABLE `users` (\n" +
                    "  `id` int NOT NULL AUTO_INCREMENT,\n" +
                    "  `name` varchar(45) NOT NULL,\n" +   //TODO umesto 45 moze i neki drugi broj
                    "  `password` varchar(45) NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ");";
            String booksTable = "CREATE TABLE `books` (\n" +
                    "  `id` int NOT NULL AUTO_INCREMENT,\n" +
                    "  `title` varchar(45) NOT NULL,\n" +
                    "  `author` varchar(45) NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ");";
            String borrowedItemsTable = "CREATE TABLE `borrowed_items` (\n" +
                    "  `b_id` int NOT NULL AUTO_INCREMENT,\n" +
                    "  `user_id` varchar(45) NOT NULL,\n" +
                    "  `book_id` varchar(45) NOT NULL,\n" +
                    "  PRIMARY KEY (`b_id`)\n" +
                    ");";
            PreparedStatement usersStatement = connectionManager.getConnection()
                    .prepareStatement(usersTable);
            usersStatement.executeUpdate();
            PreparedStatement booksStatement = connectionManager.getConnection()
                    .prepareStatement(booksTable);
            booksStatement.executeUpdate();
            PreparedStatement borrowedItemsStatement = connectionManager.getConnection()
                    .prepareStatement(borrowedItemsTable);
            borrowedItemsStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Library getInstance() {
        if (Objects.nonNull(library)) {
            return library;
        } else {
            return new Library();
        }
    }

    private void putBooksIntoLibraryDB() {
        System.out.println("Putting books into library...");
        List<Book> booksFromCSVFile = readBooksFromCSV();
        for (Book b : booksFromCSVFile) {
            try {
                PreparedStatement preparedStatement = connectionManager.getConnection().prepareStatement("INSERT INTO books (title, author) VALUES (?,?)");
                preparedStatement.setString(1, b.getTitle());
                preparedStatement.setString(2, b.getAuthor());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void putUsersIntoLibraryDB() {
        System.out.println("Putting users into library...");
        List<User> usersFromCSV = readUsersFromCSV();
        for (User u : usersFromCSV) {
            try {
                PreparedStatement preparedStatement = connectionManager.getConnection().prepareStatement("INSERT INTO users (name, password) VALUES (?,?)");
                preparedStatement.setString(1, u.getName());
                preparedStatement.setString(2, u.getPassword());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Book> readBooksFromCSV() {
        List<Book> booksFromFile = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(PATH_BOOKS), StandardCharsets.UTF_8)) { //TODO procitaj vise o UTF_8
            String line = br.readLine();

            while (line != null) {
                String[] data = line.split(COMMA);
                Book book = createBook(data);
                booksFromFile.add(book);
                line = br.readLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return booksFromFile;
    }

    private List<User> readUsersFromCSV() {
        List<User> usersFromFile = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(PATH_USERS), StandardCharsets.UTF_8)) {
            String line = br.readLine();

            while (line != null) {
                String[] data = line.split(COMMA);
                User user = createUser(data);
                usersFromFile.add(user);
                line = br.readLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return usersFromFile;
    }

    private User createUser(String[] data) {
        String name = data[0];
        String password = data[1];

        return new User(name, password);
    }

    private Book createBook(String[] data) {
        String title = data[0];
        String author = data[1];

        return new Book(title, author);
    }

    public List<Book> getAvailableBooks() {
        List<Book> availableBooks = new ArrayList<>();
        try {
            String joinStatement = "SELECT id, title, author\n" +
                    "FROM books\n" +
                    "LEFT JOIN borrowed_items\n" +
                    "ON books.id = borrowed_items.book_id\n" +
                    "WHERE b_id IS NULL;";
            PreparedStatement preparedStatement = connectionManager.getConnection().prepareStatement(joinStatement);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");

                Book book = new Book(id, title, author);
                availableBooks.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableBooks;
    }

    public void lendBook(int bookId, int userId) {
        try {
            PreparedStatement preparedStatement = connectionManager.getConnection().prepareStatement("INSERT INTO borrowed_items (user_id, book_id) VALUES (?,?)");
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, bookId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUserFromLibrary(String username, String pass) {
        try {
            PreparedStatement preparedStatement = connectionManager.getConnection().prepareStatement(
                    "SELECT * FROM users WHERE name=?");
            preparedStatement.setString(1, username);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String password = rs.getString("password");
                User user = new User(id, name, password);
                if (user.getPassword().equals(pass)) {
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Book> getAllBooksForUser(int userId) {
        List<Book> userBooks = new ArrayList<>();
        try {
            String joinStatement = "SELECT id, title, author\n" +
                    "FROM books\n" +
                    "INNER JOIN borrowed_items\n" +
                    "ON books.id = borrowed_items.book_id\n" +
                    "WHERE user_id = ?;";
            PreparedStatement preparedStatement = connectionManager.getConnection().prepareStatement(joinStatement);
            preparedStatement.setInt(1, userId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");

                Book book = new Book(id, title, author);
                userBooks.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userBooks;
    }

    public void returnBook(int bookId, int userId) {
        try {
            PreparedStatement preparedStatement = connectionManager.getConnection().prepareStatement("DELETE FROM borrowed_items WHERE user_id = ? AND book_id = ?");
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, bookId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User createUser(String currentUserName, String currentUserPassword) {
        User user = null;
        try {
            PreparedStatement preparedStatement = connectionManager.getConnection().prepareStatement("INSERT INTO users (name, password) VALUES (?,?)");
            preparedStatement.setString(1, currentUserName);
            preparedStatement.setString(2, currentUserPassword);
            preparedStatement.executeUpdate();

            user = getUserFromLibrary(currentUserName,currentUserPassword);  //treba nam id usera iz baze ali to baza generise
        } catch (SQLException e) {
            System.out.println("Couldn't create user.");
        }
        return user;
    }

    public boolean checkIfUserNameExists(String name) {
        try {
            PreparedStatement preparedStatement = connectionManager.getConnection().prepareStatement(
                    "SELECT * FROM users WHERE name=?");
            preparedStatement.setString(1, name);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
           e.printStackTrace();
        }
        return false;
    }
}