import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

// User class representing a customer
class User {
	public long acc_no;
	public int cash;
	private final ReentrantLock lock = new ReentrantLock();

	public User(long acc_no, int cash) {
		this.acc_no = acc_no;
		this.cash = cash;
	}

	public void setBalance(int amount) {
		lock.lock();
		try {
			this.cash = amount;
		} finally {
			lock.unlock();
		}
	}
}

// Updater class handling transactions
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
		List<User> sourceBranch = GnB.Bank_branch[sb];
		List<User> destBranch = GnB.Bank_branch[db];

		if (sourceBranch == null || destBranch == null)
			return;

		switch (request) {
			case 0: // Deposit
				for (User u : sourceBranch) {
					if (u.acc_no == source) {
						u.setBalance(u.cash + cash);
						System.out.println("Deposit: " + source + " Cash: " + cash);
						return;
					}
				}
				break;
			case 1: // Withdraw
				for (User u : sourceBranch) {
					if (u.acc_no == source && u.cash >= cash) {
						u.setBalance(u.cash - cash);
						System.out.println("Withdraw: " + source + " Cash: " + cash);
						return;
					}
				}
				break;
			case 2: // Transfer
				User srcUser = null, destUser = null;
				for (User u : sourceBranch) {
					if (u.acc_no == source) {
						srcUser = u;
						break;
					}
				}
				for (User u : destBranch) {
					if (u.acc_no == destination) {
						destUser = u;
						break;
					}
				}
				if (srcUser != null && destUser != null && srcUser.cash >= cash) {
					srcUser.setBalance(srcUser.cash - cash);
					destUser.setBalance(destUser.cash + cash);
					System.out.println("Transfer: " + source + " -> " + destination + " Cash: " + cash);
				}
				break;
		}
	}
}

// Main GnB class
class GnB {
	public static final List<User>[] Bank_branch = new List[10];
	private static final long M = 10000000000L;
	private static final ReentrantLock[] Listlocks = new ReentrantLock[10];
	private static final int[] Last_acc_no = new int[10];

	static {
		for (int i = 0; i < 10; i++) {
			Bank_branch[i] = Collections.synchronizedList(new LinkedList<>());
			Listlocks[i] = new ReentrantLock();
		}
	}

	public static void init() {
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 10; i++) {
			int branch = i;
			executorService.execute(() -> {
				for (int j = 1; j <= 10000; j++) {
					long acc_no = branch * M + j;
					int cash = new Random().nextInt(9000) + 1000;
					Bank_branch[branch].add(new User(acc_no, cash));
				}
			});
		}
		executorService.shutdown();
		while (!executorService.isTerminated()) {
		}
		Arrays.fill(Last_acc_no, 10000);
	}

	public static int random_request() {
		double r = Math.random();
		if (r <= 0.33)
			return 0;
		if (r <= 0.66)
			return 1;
		if (r <= 0.99)
			return 2;
		if (r <= 0.993)
			return 3;
		if (r <= 0.996)
			return 4;
		return 5;
	}

	public static void main(String[] args) {
		long no_of_requests = Long.parseLong(args[0]);
		init();
		ExecutorService executor = Executors.newFixedThreadPool(10);

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < no_of_requests; i++) {
			int request = random_request();
			int cash = new Random().nextInt(900) + 100;
			int sb = new Random().nextInt(10);
			int db = new Random().nextInt(10);
			long source = sb * M + new Random().nextInt(1000) + 1;
			long destination = db * M + new Random().nextInt(1000) + 1;
			executor.execute(new Updater(request, source, destination, cash));
		}

		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Execution time: " + (endTime - startTime) + "ms");
	}
}
