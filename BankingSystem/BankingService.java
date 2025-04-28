package banking;

import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

class Account{
    String accountId;
    int balance;
    int createdAt;
    int outgoing;

    TreeMap<Integer, Integer> balanceLog = new TreeMap<>(); // to find the account balance at any point of time.
        //balanceLog -> time, balance

    public Account(String accountId, int createdAt) {
        this.accountId = accountId;
        this.createdAt = createdAt;
    }
}

class AccountSummary {
    String accountId;
    int outgoing;

    public AccountSummary(String accountId, int outgoing) {
        this.accountId = accountId;
        this.outgoing = outgoing;
    }
}

class ScheduledPayment {
    String id;
    String accountId;
    int amount;
    int executedAt;

    public ScheduledPayment(String id, String accountId, int amount, int executedAt) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.executedAt = executedAt;
    }
}
public class BankingService {

    ReentrantLock lock = new ReentrantLock();
    private final Map<String, Account> accounts = new HashMap<>();
    private final List<ScheduledPayment>  scheduledPayments =  new ArrayList<>();
    private int paymentCounter = 0;


    public boolean createAccount(int timestamp, String accountId) {
        lock.lock();
        try {
            processPayments(timestamp); //Calls this often for all the payments scheduled.
            if(accounts.containsKey(accountId)) return  false;
            accounts.put(accountId,new Account(accountId,timestamp));
            return true;
        }finally {
             lock.unlock();
        }
    }

    public Integer deposit(int timestamp, String accountId, int amount) {
        lock.unlock();

        try {
            processPayments(timestamp);
            Account account = accounts.get(accountId);
            if(account != null) {
                account.balance += amount;
                account.balanceLog.put(timestamp, account.balance);
                return account.balance;
            }
            return null;
        }finally {
             lock.unlock();
        }
    }

    public Integer transfer(int timestamp, String soureId, String destinationId, int amount) {
        lock.lock();
        try {
            processPayments(timestamp);
            Account source = accounts.get(soureId);
            Account destination = accounts.get(destinationId);

            if(source == null || destination == null || soureId.equals(destinationId) || source.balance < amount){
                return null;
            }
            source.balance -= amount;
            source.outgoing += amount;
            source.balanceLog.put(timestamp,source.balance);

            destination.balance += amount;
            destination.balanceLog.put(timestamp, destination.balance);

            return source.balance;
        }finally {
            lock.unlock();
        }
    }

    public List<String> topSpenders (int timestamp, int n) {
        lock.lock();
        try {
            processPayments(timestamp);
            List<AccountSummary> summaries = new ArrayList<>();
            for(Account account : accounts.values()) {
                summaries.add(new AccountSummary(account.accountId, account.outgoing)); //Prepate the account summary.
            }

            summaries.sort((a, b) -> {
                if(b.outgoing != a.outgoing) // same as  cmp != 0 below
                    return Integer.compare(a.outgoing, b.outgoing);
                return a.accountId.compareTo(b.accountId);
            });

         //Using comparator on List.
//        summaries.sort(
//                Comparator.comparingInt((AccountSummary a) -> a.outgoing).
//                        thenComparing(a -> a.accountId)); is only called when the first comparator (on outgoing) considers two objects equal—that is, when s1.outgoing == s2.outgoing:

//Another way of using compare in case of map.sorted
            //   summaries.sorted((a, b) -> {
//           int cmp = Integer.compare(a.outgoing, b.outgoing);
//           return  cmp != 0 ? cmp : a.accountId.compareTo(b.accountId);
//        });

            List<String> result = new ArrayList<>();
            for (int i = 0; i < Math.min(summaries.size(), n); i++){
                AccountSummary s = summaries.get(i);
                result.add(s.accountId + "(" + s.outgoing + ")");
            }
            return  result;
        } finally {
             lock.unlock();
        }
    }

    //schedule payment

    public String schedulePayment (int timestamp, String accountId, int amount, int delay) {
        lock.lock();
        try {
            processPayments(timestamp);
            if (!accounts.containsKey(accountId)) return null;
            paymentCounter++;
            String paymentId = "Payment "+paymentCounter;
            scheduledPayments.add(new ScheduledPayment(paymentId, accountId, amount, timestamp+delay));
            return paymentId;

        }finally {
            lock.unlock();
        }
    }

    public Integer getBalance(int timestamp, String accountId, int timeAt) {
        lock.lock();
        try {
            processPayments(timestamp);
            Account acc  =  accounts.get(accountId);
            int closeTime =  acc.balanceLog.floorKey(timeAt);
            return acc.balanceLog.get(closeTime);
        }
        finally {
            lock.unlock();
        }
    }

    private void processPayments(int currentTimestamp) {
        List<ScheduledPayment> newQueue = new ArrayList<>();
        for(ScheduledPayment payment : scheduledPayments){
           if (payment.executedAt <= currentTimestamp) {
               Account acc = accounts.get(payment.accountId);
               acc.balance -= payment.amount;
               acc.balanceLog.put(currentTimestamp, acc.balance);
           } else {
               newQueue.add(payment); //New List to store all the future payments.
           }
        }
        scheduledPayments.clear();// clear all the paid and future payments.
        scheduledPayments.addAll(newQueue); //Add all the future payments in one shot.
    }

    public boolean cancelPayment(int timestamp, String accountId, String paymentId) {
        lock.lock();
        try {
            processPayments(timestamp);
            Iterator<ScheduledPayment> iterator = scheduledPayments.iterator();
            while (iterator.hasNext()) {
                ScheduledPayment p = iterator.next();
                if (p.id.equals(paymentId)) {
                    if (!p.accountId.equals(accountId)) return false;
                    iterator.remove();
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean mergeAccounts(int timestamp, String accountId1, String accountId2) {
        lock.lock();
        try {
            processPayments(timestamp);
            if (accountId1.equals(accountId2)) return false;
            Account acc1 = accounts.get(accountId1);
            Account acc2 = accounts.get(accountId2);
            if (acc1 == null || acc2 == null) return false;
            acc1.balance += acc2.balance;
            acc1.outgoing += acc2.outgoing;
            acc1.balanceLog.put(timestamp, acc1.balance);
            for (ScheduledPayment p : scheduledPayments) {
                if (p.accountId.equals(accountId2)) {
                    p.accountId = accountId1;
                }
            }
            accounts.remove(accountId2);
            return true;
        } finally {
            lock.unlock();
        }
    }

}

class Main {
    public static void main(String[] args) {
        BankingService bankingSystem = new BankingService();
        // Create accounts
        System.out.println("Creating Accounts:");
        System.out.println(bankingSystem.createAccount(1, "A1")); // true
        System.out.println(bankingSystem.createAccount(2, "A2")); // true
        System.out.println(bankingSystem.createAccount(2, "A1")); // false (already exists)
        // Deposits
        System.out.println("\nDepositing:");
        System.out.println("A1 Balance after deposit: " + bankingSystem.deposit(3, "A1", 100)); // 100
        System.out.println("A2 Balance after deposit: " + bankingSystem.deposit(3, "A2", 200)); // 200
        // Transfers
        System.out.println("\nTransferring:");
        System.out.println("A1 Balance after transferring 50 to A2: " + bankingSystem.transfer(4, "A1", "A2", 50)); // 50
        // Get Balance
        System.out.println("\nChecking Balances at different times:");
        System.out.println("A1 balance at time 4: " + bankingSystem.getBalance(5, "A1", 4)); // 50
        System.out.println("A2 balance at time 4: " + bankingSystem.getBalance(5, "A2", 4)); // 250
        // Schedule Payment
        System.out.println("\nScheduling Payment from A2:");
        String paymentId = bankingSystem.schedulePayment(6, "A2", 100, 2);
        System.out.println("Scheduled payment ID: " + paymentId);
        // Process payments (simulate future time)
        System.out.println("\nBalance after scheduled payment (simulate time 9):");
        bankingSystem.deposit(9, "A2", 0); // trigger process
        System.out.println("A2 balance: " + bankingSystem.getBalance(9, "A2", 9)); // should deduct 100 from scheduled
        // Cancel Payment (should fail as already processed)
        System.out.println("\nCancelling payment after execution:");
        System.out.println("Cancel result: " + bankingSystem.cancelPayment(10, "A2", paymentId)); // false
        // Merge accounts
        System.out.println("\nMerging accounts A1 and A2:");
        System.out.println("Merge result: " + bankingSystem.mergeAccounts(11, "A1", "A2")); // true
        System.out.println("A1 balance after merge: " + bankingSystem.getBalance(11, "A1", 11)); // merged balance
        // Top Spenders
        System.out.println("\nTop Spenders:");
        System.out.println(bankingSystem.topSpenders(12, 2)); // should show top 2 spenders

    }
}
