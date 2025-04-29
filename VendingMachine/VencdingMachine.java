package vendingmachine;

import java.util.HashMap;
import java.util.Map;

// Strategy Pattern for Payment
interface PaymentStrategy {
    void addPayment(float amount);
    float getBalance();
    void resetBalance();
    void processPayment(float amount);
}

class CoinPayment implements PaymentStrategy {
    private float balance = 0;

    @Override
    public void addPayment(float amount) {
        balance += amount;
    }

    @Override
    public float getBalance() {
        return balance;
    }

    @Override
    public void resetBalance() {
        balance = 0;
    }

    @Override
    public void processPayment(float amount) {
        balance -= amount;
    }
}

class CardPayment implements PaymentStrategy {
    private float balance = 0;

    @Override
    public void addPayment(float amount) {
        balance += amount;
    }

    @Override
    public float getBalance() {
        return balance;
    }

    @Override
    public void resetBalance() {
        balance = 0;
    }

    @Override
    public void processPayment(float amount) {
        balance -= amount;
    }
}

// Observer Pattern for Display
interface DisplayObserver {
    void update(String message);
}

class Display implements DisplayObserver {
    @Override
    public void update(String message) {
        System.out.println("Display: " + message);
    }

    public void displayPrice(float price) {
        System.out.println("Price: " + price);
    }

    public void displayBalance(float balance) {
        System.out.println("Balance: " + balance);
    }
}

// Factory Method for Item creation
abstract class Item {
    private String code;
    private float price;
    private int quantity;

    public Item(String code, float price, int quantity) {
        this.code = code;
        this.price = price;
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public float getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void decrementQuantity() {
        quantity--;
    }
}

class SnackItem extends Item {
    public SnackItem(String code, float price, int quantity) {
        super(code, price, quantity);
    }
}

class DrinkItem extends Item {
    public DrinkItem(String code, float price, int quantity) {
        super(code, price, quantity);
    }
}

// Factory for Item creation
class ItemFactory {
    public static Item createItem(String type, String code, float price, int quantity) {
        if (type.equalsIgnoreCase("snack")) {
            return new SnackItem(code, price, quantity);
        } else if (type.equalsIgnoreCase("drink")) {
            return new DrinkItem(code, price, quantity);
        } else {
            throw new IllegalArgumentException("Invalid item type");
        }
    }
}

// Dispenser class
class Dispenser {
    public void dispenseItem(Item item) {
        System.out.println("Dispensing item: " + item.getCode());
    }
}

// Coin Acceptor
class CoinAcceptor {
    public void acceptCoin(float coinValue, PaymentStrategy paymentStrategy) {
        paymentStrategy.addPayment(coinValue);
    }
}

// Card Reader
class CardReader {
    public void readCard(float amount, PaymentStrategy paymentStrategy) {
        paymentStrategy.addPayment(amount);
    }
}

// Vending Machine with State and Observer
class VendingMachine {
    private ItemDatabase itemDatabase;
    private PaymentStrategy paymentStrategy;
    private Dispenser dispenser;
    private Display display;
    private DisplayObserver displayObserver;

    public VendingMachine(DisplayObserver displayObserver) {
        itemDatabase = new ItemDatabase();
        dispenser = new Dispenser();
        this.displayObserver = displayObserver;
    }

    public void addItem(Item item) {
        itemDatabase.addItem(item);
    }

    public void selectItem(String itemCode) {
        Item item = itemDatabase.getItem(itemCode);
        if (item == null) {
            displayObserver.update("Invalid item code");
            return;
        }
        if (item.getQuantity() == 0) {
            displayObserver.update("Item out of stock");
            return;
        }
        displayObserver.update("Price: " + item.getPrice());
    }

    public void insertCoin(float coinValue) {
        if (paymentStrategy instanceof CoinPayment) {
            paymentStrategy.addPayment(coinValue);
            displayObserver.update("Balance: " + paymentStrategy.getBalance());
        } else {
            displayObserver.update("Invalid payment method.");
        }
    }

    public void insertCard(float amount) {
        if (paymentStrategy instanceof CardPayment) {
            paymentStrategy.addPayment(amount);
            displayObserver.update("Balance: " + paymentStrategy.getBalance());
        } else {
            displayObserver.update("Invalid payment method.");
        }
    }

    public void cancelTransaction() {
        paymentStrategy.resetBalance();
        displayObserver.update("Transaction canceled");
    }

    public void completeTransaction(String itemCode) {
        Item item = itemDatabase.getItem(itemCode);
        if (item == null) {
            displayObserver.update("Invalid item code");
            return;
        }
        if (item.getQuantity() == 0) {
            displayObserver.update("Item out of stock");
            return;
        }
        if (paymentStrategy.getBalance() < item.getPrice()) {
            displayObserver.update("Insufficient funds");
            return;
        }
        paymentStrategy.processPayment(item.getPrice());
        item.decrementQuantity();
        dispenser.dispenseItem(item);
        displayObserver.update("Transaction complete");
    }

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
}

// Item Database
class ItemDatabase {
    private Map<String, Item> items = new HashMap<>();

    public void addItem(Item item) {
        items.put(item.getCode(), item);
    }

    public Item getItem(String code) {
        return items.get(code);
    }
}

// Main class
public class Main {
    public static void main(String[] args) {
        Display display = new Display();
        VendingMachine vendingMachine = new VendingMachine(display);

        // Add items to the vending machine
        vendingMachine.addItem(ItemFactory.createItem("snack", "S1", 1.5f, 10));
        vendingMachine.addItem(ItemFactory.createItem("drink", "D1", 2.5f, 5));

        // Select item
        vendingMachine.selectItem("S1");

        // Set payment strategy (coin)
        vendingMachine.setPaymentStrategy(new CoinPayment());
        vendingMachine.insertCoin(1.0f);
        vendingMachine.insertCoin(0.5f);

        // Complete the transaction
        vendingMachine.completeTransaction("S1");

        // Cancel the transaction
        vendingMachine.cancelTransaction();
    }
}
