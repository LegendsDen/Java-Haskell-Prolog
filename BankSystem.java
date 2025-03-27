import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

// User class representing a customer
class User {
    public final long acc_no;
    private int balance;
    private final ReentrantLock lock = new ReentrantLock(); // Lock for thread safety

    public User(long acc_no, int cash) {
        this.acc_no = acc_no;
        this.balance = cash;
    }
    // Deposit method with locking to ensure thread safety
    public void deposit(int amount) {
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }
    // Withdraw method with locking to ensure thread safety
    public void withdraw(int amount) {
        lock.lock();
        try {
            if (balance >= amount) {
                balance -= amount;
            }
        } finally {
            lock.unlock();
        }
    }

    public int getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public ReentrantLock getLock() {
        return lock;
    }
}

// Updater class handles various banking transactions
class Updater implements Runnable {
    private final int request;
    private final long source, destination;
    private final int cash;
    private static final long M = 10000000000L;

    public Updater(int request, long source, long destination, int cash) {
        this.request = request;
        this.source = source;
        this.destination = destination;
        this.cash = cash;
    }

    public void run() {
        int sb = (int) (source / M);
        int db = (int) (destination / M);

        ConcurrentHashMap<Long, User> sourceBranch = BankSystem.Bank_branch.get(sb);
        ConcurrentHashMap<Long, User> destBranch = BankSystem.Bank_branch.get(db);

        if (sourceBranch == null || destBranch == null) return;

        switch (request) {
            case 0: // Balance Check
                User user = sourceBranch.get(source);
                if (user != null) {
                    user.getBalance();
                }
                break;
                case 1: // Deposit
                user = sourceBranch.get(source);
                if (user != null) {
                    user.deposit(cash);
                    System.out.println("Deposit: Account " + source + " deposited " + cash);
                }
                break;

            case 2: // Withdraw
                user = sourceBranch.get(source);
                if (user != null) {
                    int beforeBalance = user.getBalance();
                    user.withdraw(cash);
                    int afterBalance = user.getBalance();
                    System.out.println("Withdraw: Account " + source + " withdrew " + cash +
                            " | Before: " + beforeBalance + " | After: " + afterBalance);
                }
                break;
            case 3: // Transfer Money (Fixed)
                User srcUser = sourceBranch.get(source);
                User destUser = destBranch.get(destination);
                
                if (srcUser == null || destUser == null) return;

                boolean srcLock = false, destLock = false;
                try {
                    srcLock = srcUser.getLock().tryLock(100, TimeUnit.MILLISECONDS);
                    destLock = destUser.getLock().tryLock(100, TimeUnit.MILLISECONDS);

                    if (srcLock && destLock && srcUser.getBalance() >= cash) {
                        int srcBefore = srcUser.getBalance();
                        int destBefore = destUser.getBalance();
                        srcUser.withdraw(cash);
                        destUser.deposit(cash);

                        System.out.println("Transfer: " + cash + " from Account " + source + " to " + destination +
                        " | Source Before: " + srcBefore + " | Source After: " + srcUser.getBalance() +
                        " | Destination Before: " + destBefore + " | Destination After: " + destUser.getBalance());

                        
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    if (srcLock) srcUser.getLock().unlock();
                    if (destLock) destUser.getLock().unlock();
                }
                break;
            case 4: // Add Customer
                sourceBranch.put(source, new User(source, cash));
                System.out.println("New Customer: Account " + source + " created with balance " + cash);
               
                break;
                case 5: // Delete Customer
                boolean accountFound = false;
            
                // Check all branches for the account
                for (Map.Entry<Integer, ConcurrentHashMap<Long, User>> entry : BankSystem.Bank_branch.entrySet()) {
                    ConcurrentHashMap<Long, User> branch = entry.getValue();
            
                    if (branch.containsKey(source)) {
                        user = branch.remove(source);
                        accountFound = true;
                        System.out.println("Delete Customer: Account " + source + " removed from Branch " + entry.getKey());
                        break;
                    }
                }
            
                if (!accountFound) {
                    System.out.println("Delete Customer: Account " + source + " not found in any branch.");
                }
                break;
            
            case 6: // Transfer Account to another Branch
                user = sourceBranch.remove(source);
                if (user != null) {
                    destBranch.put(destination, new User(destination, user.getBalance()));
                    System.out.println("Transfer Account: Account " + source + " moved to branch of Account " + destination);
                }
                break;
        }
    }
}

// Main Bank System
class BankSystem {
    public static final ConcurrentHashMap<Integer, ConcurrentHashMap<Long, User>> Bank_branch = new ConcurrentHashMap<>();
    private static final long M = 10000000000L;

    static {
        for (int i = 0; i < 10; i++) {
            Bank_branch.put(i, new ConcurrentHashMap<>());
        }
    }

    public static void init() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            int branch = i;
            executorService.execute(() -> {
                Random rand = new Random();
                for (int j = 1; j <= 10000; j++) {
                    long acc_no = branch * M + j;
                    int cash = rand.nextInt(9000) + 1000;
                    Bank_branch.get(branch).put(acc_no, new User(acc_no, cash));
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

   

    public static int getRequestType() {
        double r = Math.random();
        if (r <= 0.3) return 0;
        if (r <= 0.53) return 1;
        if (r <= 0.76) return 2;
        if (r <= 0.99) return 3;
        if (r <= 0.993) return 4;
        if (r <= 0.996) return 5;
        return 6;
    }

    public static void main(String[] args) {
        long no_of_requests = Long.parseLong(args[0]);
        init();



        ExecutorService[] branchExecutors = new ExecutorService[10];
        for (int i = 0; i < 10; i++) {
            branchExecutors[i] = Executors.newFixedThreadPool(10);
        }

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < no_of_requests; i++) {
            int request = getRequestType();
            int cash = new Random().nextInt(900) + 100;
            int sb = new Random().nextInt(10);
            int db = new Random().nextInt(10);
            long source = sb * M + new Random().nextInt(10000) + 1;
            long destination = db * M + new Random().nextInt(10000) + 1;

            branchExecutors[sb].execute(new Updater(request, source, destination, cash));
        }

        for (ExecutorService executor : branchExecutors) {
            executor.shutdown();
        }
        for (ExecutorService executor : branchExecutors) {
            while (!executor.isTerminated()) {}
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + "ms");

       
    }
} 