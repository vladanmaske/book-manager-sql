package org.example;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Librarian {

    private final Scanner input;
    private final Library library;
    private User currentUser;

    public Librarian(Library library) {
        this.library = library;
        this.input = new Scanner(System.in);
    }

    public void openLibrary() {
        System.out.println("Hello! I'll be your AI librarian.");
        System.out.println("- If you already have a membership press 1");
        System.out.println("- If you want to become a member press 2");
        int press = 0;
        try {
            press = input.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("You broke the system!!!");
        }

        if (press == 1) {
            for (int i = 0; i < 3; i++) {
                System.out.println("What is your name?");
                String currentUserName = input.next().trim();

                System.out.println("Your password?");
                String currentUserPassword = input.next().trim();

                currentUser = library.getUserFromLibrary(currentUserName, currentUserPassword);

                if (currentUser == null) {
                    System.out.println("Please try again!");
                } else {
                   break;
                }
            }
            if (currentUser != null) {
                System.out.println(currentUser.getName() + ", you are successfully logged in.");
                serviceUser();
            } else {
                openLibrary();
            }
        } else if (press == 2) {
            System.out.println("Let's create a membership card for you...");
            System.out.println("What is your name?");
            String currentUserName = input.next().trim();
            boolean nameExist = library.checkIfUserNameExists(currentUserName);
            if (nameExist) {
                System.out.println("User with this name already exist.");
                System.out.println("Did you want to log in?");
                openLibrary();
            }
            System.out.println("Your password?");
            String currentUserPassword = input.next().trim();

            currentUser = library.createUser(currentUserName, currentUserPassword);
            System.out.println("You are officially a member of this library!");
            serviceUser();
        } else {
            System.out.println("Are you unable to follow simple instructions?");
        }
    }

    private void serviceUser() {
        while (true) {
            System.out.println("- For returning a book press 1");
            System.out.println("- For borrowing a book press 2");
            System.out.println("- For talking to the operator press 3");
            System.out.println("- For logging with another user press 4");
            System.out.println("- For exiting the library press 5");
            int press = 0;
            try {
                press = input.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Enter a number!");
            }

            if (press == 1) {          //mozda bolje da stavim switch
                returnBooksOption();
            } else if (press == 2) {
                borrowBooksOption();
            } else if (press == 3) {
                operatorOption();
            } else if (press == 4) {
                System.out.println("Thank you for using our library!");
                currentUser = null;
                openLibrary();
            } else if (press == 5) {
                System.out.println("Thank you for using our library!");
                closeLibrary();
            } else {
                System.out.println("Are you unable to follow simple instructions?");
            }
        }
    }

    private void operatorOption() {
        System.out.println("Please wait for the next free operator.");
        do {
            System.out.print(".");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                closeLibrary();
            }
        } while (true);
    }

    private void borrowBooksOption() {  //TODO proveri da li korisnik ima vec tri pozajmljene knjige. Ako ima, ne daj mu nove knjige i obavesti ga da mora da vrati neku
        System.out.println("Which of these books do you want to borrow?");
        List<Book> availableBooks = library.getAvailableBooks();
        for (Book b : availableBooks) {
            System.out.println(b);
        }
        System.out.println("Enter the id of the book to borrow:");
        input.nextLine();
        int bookId = input.nextInt();  //moze da baci exception
        Book book = null;
        for (Book b : availableBooks) {
            if (b.getId() == bookId) {
                book = b;
                break;
            }
        }

        if (book == null) {
            System.out.println("You can choose only from the available books!");
        } else {
            System.out.println("We have found the book in the library!");
            library.lendBook(book.getId(), currentUser.getId());
            System.out.println("You have successfully borrowed the book!");
        }
    }

    private void returnBooksOption() {
        List<Book> borrowed = library.getAllBooksForUser(currentUser.getId());
        if (borrowed.size() == 0) {
            System.out.println("You have no borrowed books!");
        } else {
            System.out.println("These are the books you have borrowed: ");
            borrowed.forEach(System.out::println); //TODO napravi for-each petlju umesto ovoga

            System.out.println("Enter the id of the book to return:");
            input.nextLine();
            int bookId = input.nextInt();  //TODO catchuj moguci exception

            Book book = null;
            for (Book b : borrowed) {
                if (b.getId() == bookId) {
                    book = b;
                    break;
                }
            }
           if (book != null) {
                library.returnBook(book.getId(), currentUser.getId());
                System.out.println("You have successfully returned the book!");
            } else {
                System.out.println("You want to return the wrong book!");
            }
        }
    }

    private void closeLibrary() {
        System.out.println("Library is closing!");
        input.close();
        System.exit(0);
    }
}