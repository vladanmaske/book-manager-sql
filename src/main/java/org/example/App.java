package org.example;

public class App {

    public static void main( String[] args ) {
        Library library = Library.getInstance();
        Librarian librarian = new Librarian(library);
        librarian.openLibrary();
        //librarian.closeLibrary();
    }
}