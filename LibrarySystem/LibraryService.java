package library;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

enum Status {
    AVAILABLE, ISSUED;
}
class Book {
    private final String isbn;
    private final String author;
    private final String title;
    private Status status = Status.AVAILABLE;

    public Book(String isbn, String title, String author){
        this.isbn = isbn; this.title = title; this.author = author;
    }
    /* getters & mutators */
    public String isbn()  { return isbn; }
    public String title() { return title; }
    public String author(){ return author; }
    public Status status(){ return status; }

    //Mark the book status as Issued or returned, depends on when it is called.
    //Same as parking lot parkVehicle() where we set this.parkedVehicle = parkedVehicle
    public void mark(Status s){
        status = s;
    }

    @Override public String toString(){
        return "%s by %s (%s)".formatted(title, author, status);
    }
}
class User {
    private final String id;
    private final String name;
    private final List<Transaction> tx = new ArrayList<>();

    public User(String id,String name){
        this.id=id; this.name=name;
    }
    public String id(){
        return id;
    }
    public String name(){
        return name;
    }
    public List<Transaction> transactions(){
        return tx;
    }

}
class Transaction {
    enum Type {
        ISSUE, RETURN;
    }
    private final User user;
    private final Book book;
    private final Type type;
    private final LocalDate date  = LocalDate.now();

    Transaction(User u, Book b, Type t){ 
        user=u; book=b; 
        type=t;
    }

    public User user(){ return user; }
    public Book book(){ return book; }
    public Type type(){ return type; }
    public LocalDate date(){ return date; }
}

public class LibraryService {
    private final Map<String, Book> books = new HashMap<>();
    private final Map<String, User> users = new HashMap<>();
    private final List<Transaction> ledger = new ArrayList<>();

    //DB CRUD operations
    public void addBook(Book b) {
        books.put(b.isbn(), b);
    }

    public void addUser(User u) {
        users.put(u.id(), u);
    }

    public Boolean issueBook(String isbn, String userId) {
        Book b = books.get(isbn);
        User u = users.get(userId);
        if(b == null || u == null || b.status() != Status.AVAILABLE) return false;

        b.mark(Status.ISSUED);

        Transaction tx = new Transaction(u, b, Transaction.Type.ISSUE);
        u.transactions().add(tx);
        ledger.add(tx);
        return true;
    }

    public Boolean returnBook(String isbn, String userId) {
        Book b = books.get(isbn);
        User u = users.get(userId);
        if(b == null || u == null || b.status() != Status.ISSUED) return false;

        b.mark(Status.AVAILABLE);

        Transaction tx = new Transaction(u, b, Transaction.Type.RETURN);
        u.transactions().add(tx);
        ledger.add(tx);
        return true;
    }

    //search by title --> Stratergy pattern
    //search by author --> Stratergy pattern
    //Decide the search stratergy and call the search method
    public List<Book> searchBook(String searchQuery, SearchStratergy stratergy) { //it could be search by author or searchy by title
        return stratergy.search(books.values(), searchQuery);
    }

    //Track the issued book per user
//    Walk through the transaction history (ledger) and pick only the “ISSUE” transactions
//    that belong to the requested user.
//    Convert each of those transactions into its Book.
//    Discard books that have already been returned.
//    Return the remaining books as a list.
    public List<Book> booksIssuedTo(String userId) {
        return ledger.stream().filter(
                transaction -> transaction.user().id().equalsIgnoreCase(userId) && transaction.type() == Transaction.Type.ISSUE)
                .map(Transaction::book)
                .filter(book -> book.status() ==  Status.ISSUED)
                .collect(Collectors.toList());
    }
}

interface SearchStratergy {
    public List<Book> search(Collection<Book> book, String searchQuery);
}

 class TitleSearch implements SearchStratergy {

    @Override
    public List<Book> search(Collection<Book> books, String q) {
            return books.stream()
                    .filter(b -> b.title().toLowerCase().contains(q.toLowerCase()))
                    .toList();
        }
}

 class AuthorSearch implements SearchStratergy {

    @Override
    public List<Book> search(Collection<Book> books, String q) {
        return books.stream()
                .filter(b -> b.author().toLowerCase().contains(q.toLowerCase()))
                .toList();
    }
}

class Main {
    public static void main(String[] args) {
        LibraryService lib = new LibraryService();
        /* add sample data */
        lib.addBook(new Book("978-0134685991","Effective Java","Joshua Bloch"));
        lib.addBook(new Book("978-1617294945","Spring in Action","Craig Walls"));
        lib.addBook(new Book("978-0596009205","Head First Java","Kathy Sierra"));
        lib.addUser(new User("u1","Alice"));
        lib.addUser(new User("u2","Bob"));

        /* transactions */
        lib.issueBook("978-0134685991","u1");          // Alice borrows Effective Java
        lib.issueBook("978-1617294945","u1");          // Alice borrows Spring
        lib.returnBook("978-1617294945","u1");     // returns Spring

        /* search */
        System.out.println("-- Search by title contains 'Java' --");
        lib.searchBook("Java", new TitleSearch()).forEach(System.out::println);

        System.out.println("-- Search by author 'walls' --");
        lib.searchBook("walls", new AuthorSearch()).forEach(System.out::println);

        /* issued books per user */
        System.out.println("-- Currently issued to Alice --");
        lib.booksIssuedTo("u1").forEach(System.out::println);
    }
}
