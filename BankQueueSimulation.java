import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BankQueueSimulation {
    // Constants
    private static final LocalDateTime SIMULATION_START = LocalDateTime.of(2025, 3, 23, 9, 0);
    private static final LocalDateTime SIMULATION_END = LocalDateTime.of(2025, 3, 23, 17, 0);
    private static final double ARRIVAL_RATE = 40.0 / 60.0; // 40 customers per hour converted to per minute
    private static final double SERVICE_RATE = 1.0 / 4.0;   // 4 minutes per customer on average
    private static final long SIMULATION_SPEED = 100;       // 1 real second = 100 simulation minutes

    // Time periods with different teller counts
    private static final List<TimePeriod> TIME_PERIODS = List.of(
            new TimePeriod(
                    LocalDateTime.of(2025, 3, 23, 9, 0),
                    LocalDateTime.of(2025, 3, 23, 11, 0),
                    2, "Morning (9-11)"
            ),
            new TimePeriod(
                    LocalDateTime.of(2025, 3, 23, 11, 0),
                    LocalDateTime.of(2025, 3, 23, 14, 0),
                    4, "Peak (11-2)"
            ),
            new TimePeriod(
                    LocalDateTime.of(2025, 3, 23, 14, 0),
                    LocalDateTime.of(2025, 3, 23, 17, 0),
                    3, "Afternoon (2-5)"
            )
    );

    // Shared resources
    private static final BlockingQueue<Customer> customerQueue = new LinkedBlockingQueue<>();
    private static final List<Customer> allCustomers = new CopyOnWriteArrayList<>();
    private static final List<Customer> completedCustomers = new CopyOnWriteArrayList<>();
    private static final AtomicInteger customerIdCounter = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, List<Double>> waitingTimesByPeriod = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<Double>> systemTimesByPeriod = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<Integer>> queueLengthsByPeriod = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> allTellersBusyCountByPeriod = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> observationCountByPeriod = new ConcurrentHashMap<>();
    
    // Simulation clock
    private static final AtomicReference<LocalDateTime> simulationClock = new AtomicReference<>(SIMULATION_START);
    private static final AtomicBoolean simulationRunning = new AtomicBoolean(true);
    private static long simulationStartTime;
    
    public static void main(String[] args) throws Exception {
        // Initialize period statistics collections
        for (TimePeriod period : TIME_PERIODS) {
            waitingTimesByPeriod.put(period.getName(), Collections.synchronizedList(new ArrayList<>()));
            systemTimesByPeriod.put(period.getName(), Collections.synchronizedList(new ArrayList<>()));
            queueLengthsByPeriod.put(period.getName(), Collections.synchronizedList(new ArrayList<>()));
            allTellersBusyCountByPeriod.put(period.getName(), new AtomicInteger(0));
            observationCountByPeriod.put(period.getName(), new AtomicInteger(0));
        }
        
        // Add overall categories
        waitingTimesByPeriod.put("Overall", Collections.synchronizedList(new ArrayList<>()));
        systemTimesByPeriod.put("Overall", Collections.synchronizedList(new ArrayList<>()));
        queueLengthsByPeriod.put("Overall", Collections.synchronizedList(new ArrayList<>()));
        allTellersBusyCountByPeriod.put("Overall", new AtomicInteger(0));
        observationCountByPeriod.put("Overall", new AtomicInteger(0));
        
        // Start simulation clock
        simulationStartTime = System.currentTimeMillis();
        
        // Start the clock thread
        Thread clockThread = new Thread(BankQueueSimulation::runSimulationClock);
        clockThread.setDaemon(true);
        clockThread.start();
        
        // Start customer arrival process
        Thread arrivalThread = new Thread(BankQueueSimulation::customerArrivalProcess);
        arrivalThread.setDaemon(true);
        arrivalThread.start();

        // Start statistics collection process
        Thread statsThread = new Thread(BankQueueSimulation::collectStatistics);
        statsThread.setDaemon(true);
        statsThread.start();
        
        // Create and start teller threads based on the initial period
        List<Teller> activeTellers = new ArrayList<>();
        int initialTellerCount = getTellersCount(SIMULATION_START);
        CountDownLatch simulationLatch = new CountDownLatch(1);
        
        for (int i = 0; i < initialTellerCount; i++) {
            Teller teller = new Teller(i);
            activeTellers.add(teller);
            Thread tellerThread = new Thread(teller);
            tellerThread.setDaemon(true);
            tellerThread.start();
        }
        
        // Monitor time periods and adjust teller count as needed
        Thread tellerManagerThread = new Thread(() -> manageTellers(activeTellers, simulationLatch));
        tellerManagerThread.setDaemon(true);
        tellerManagerThread.start();
        System.out.println(8);
        
        // Wait until the simulation is complete
        simulationLatch.await();
        
        // Calculate and display final metrics
        calculateAndDisplayMetrics();
    }
    
    private static void runSimulationClock() {
        while (simulationRunning.get()) {
            long elapsedMillis = System.currentTimeMillis() - simulationStartTime;
            long simulatedMinutes = (elapsedMillis * SIMULATION_SPEED) / 1000;
            
            LocalDateTime currentTime = SIMULATION_START.plusMinutes(simulatedMinutes);
            simulationClock.set(currentTime);
            
            if (currentTime.isAfter(SIMULATION_END)) {
                simulationRunning.set(false);
            }
            
            try {
                Thread.sleep(100); // Update clock every 100ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
    
    private static void customerArrivalProcess() {
        Random random = new Random();
        
        while (simulationRunning.get()) {
            LocalDateTime currentTime = simulationClock.get();
            
            // Check if we're still within business hours
            if (currentTime.isAfter(SIMULATION_END)) {
                break;
            }
            
            // Generate time until next arrival using exponential distribution
            double interArrivalTime = -Math.log(1 - random.nextDouble()) / ARRIVAL_RATE;
            
            try {
                // Sleep to simulate time between arrivals (adjusted for simulation speed)
                Thread.sleep((long)(interArrivalTime * 1000 / SIMULATION_SPEED));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            
            // Only create customer if simulation is still running
            if (simulationRunning.get()) {
                // Get current simulation time after sleep
                currentTime = simulationClock.get();
                if (currentTime.isBefore(SIMULATION_END)) {
                    int customerId = customerIdCounter.getAndIncrement();
                    Customer customer = new Customer(customerId, currentTime);
                    
                    // Add to queue and stats
                    customerQueue.add(customer);
                    allCustomers.add(customer);
                    
                    // Debugging
                    if (customerId % 20 == 0) {
                        String period = getPeriodName(currentTime);
                        System.out.printf("Customer %d arrived at %s during %s%n", 
                                customerId, currentTime.toString(), period);
                    }
                }
            }
        }
    }
    
    private static void collectStatistics() {
        while (simulationRunning.get() || !customerQueue.isEmpty()) {
            LocalDateTime currentTime = simulationClock.get();
            String periodName = getPeriodName(currentTime);
            
            if (periodName.equals("Outside hours")) {
                // System.err.println(69);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                continue;
            }
            
            // Record queue length
            int queueLength = customerQueue.size();
            queueLengthsByPeriod.get(periodName).add(queueLength);
            queueLengthsByPeriod.get("Overall").add(queueLength);
            
            // Increment observation count
            observationCountByPeriod.get(periodName).incrementAndGet();
            observationCountByPeriod.get("Overall").incrementAndGet();
            
            try {
                Thread.sleep(500); // Collect stats every 0.5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
    
    private static void manageTellers(List<Teller> activeTellers, CountDownLatch simulationLatch) {
        int currentTellerCount = activeTellers.size();
        
        while (simulationRunning.get() || !customerQueue.isEmpty()) {

            LocalDateTime currentTime = simulationClock.get();
            
            // Check if we need to adjust the number of tellers
            int requiredTellers = getTellersCount(currentTime);
            if(requiredTellers==0)System.err.println(currentTime);
            if(requiredTellers==0)System.err.println(simulationRunning.get());
            if(requiredTellers==0)System.err.println(activeTellers.size());
            if(requiredTellers==0)System.out.println(customerQueue);

            
            if (requiredTellers != currentTellerCount) {
                System.out.printf("Adjusting tellers from %d to %d at %s%n", 
                        currentTellerCount, requiredTellers, currentTime.toString());
                if(requiredTellers==0)System.err.println(69);
                if (requiredTellers > currentTellerCount) {
                    // Add more tellers
                    for (int i = currentTellerCount; i < requiredTellers; i++) {
                        Teller teller = new Teller(i);
                        activeTellers.add(teller);
                        Thread tellerThread = new Thread(teller);
                        tellerThread.setDaemon(true);
                        tellerThread.start();
                    }
                } else {
                    if(requiredTellers==0)System.err.println(89);
                    
                    // Remove tellers (they'll finish their current customer and then stop)
                    for (int i = currentTellerCount - 1; i >= requiredTellers; i--) {
                        Teller teller = activeTellers.get(i);
                        teller.stopServing(); // Tell teller to stop accepting new customers
                    }
                    
                    if(requiredTellers==0)System.out.println(customerQueue);
                    for (int i = currentTellerCount - 1; i >= requiredTellers; i--) {
                        Teller teller = activeTellers.get(i);
                        while (teller.isBusy()) {
                            try {
                                Thread.sleep(50); // Wait briefly before checking again
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        activeTellers.remove(i);
                    }
                }
                
                currentTellerCount = requiredTellers;
            }
            
            // Update all-tellers-busy stats
            int busyTellers = 0;
            for (Teller teller : activeTellers) {
                if (teller.isBusy()) {
                    busyTellers++;
                }
            }
            if(requiredTellers==0)System.err.println(99);
            if(requiredTellers==0)System.out.println(customerQueue);

            
            String periodName = getPeriodName(currentTime);
            if (!periodName.equals("Outside hours")) {
                if (busyTellers == currentTellerCount && currentTellerCount > 0) {
                    allTellersBusyCountByPeriod.get(periodName).incrementAndGet();
                    allTellersBusyCountByPeriod.get("Overall").incrementAndGet();
                }
            }
            if(requiredTellers==0)System.err.println(49);
            if(requiredTellers==0)System.out.println(customerQueue);

            
            // Check if simulation is complete
            if (!simulationRunning.get()) {
                // Make sure all tellers have completed their work
                boolean allTellersIdle = true;
                for (Teller teller : activeTellers) {
                    if (teller.isBusy()) {
                        allTellersIdle = false;
                        break;
                    }
                }
                
                if (allTellersIdle) {
                    simulationLatch.countDown();
                    return;
                }
            }
            if(requiredTellers==0)System.err.println(19);
            if(requiredTellers==0)System.out.println(customerQueue);

            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if(requiredTellers==0)System.err.println(9);
            if(requiredTellers==0)System.out.println(customerQueue);
        }
      

        
        simulationLatch.countDown();
    }
    
    private static void calculateAndDisplayMetrics() {
        System.out.println("\n========== SIMULATION RESULTS ==========\n");
        
        // Process all periods including Overall
        List<String> periodNames = new ArrayList<>(waitingTimesByPeriod.keySet());
        
        for (String periodName : periodNames) {
            System.out.println("===== " + periodName + " =====");
            
            // Calculate metrics
            List<Double> waitingTimes = waitingTimesByPeriod.get(periodName);
            List<Double> systemTimes = systemTimesByPeriod.get(periodName);
            List<Integer> queueLengths = queueLengthsByPeriod.get(periodName);
            int allTellersBusyCount = allTellersBusyCountByPeriod.get(periodName).get();
            int observationCount = observationCountByPeriod.get(periodName).get();
            System.out.println(allTellersBusyCount);
            System.out.println(observationCount);
            
            // Average waiting time
            double avgWaitingTime = waitingTimes.isEmpty() ? 0 : 
                    waitingTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            
            // Average time in system
            double avgSystemTime = systemTimes.isEmpty() ? 0 : 
                    systemTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            
            // Average queue length
            double avgQueueLength = queueLengths.isEmpty() ? 0 : 
                    queueLengths.stream().mapToDouble(Integer::doubleValue).average().orElse(0);
            
            // Probability of all tellers being busy
            double allTellersBusyProb = observationCount > 0 ? 
                    (double) allTellersBusyCount / observationCount : 0;
            
            // Calculate teller utilization for this period
            double tellerUtilization = calculateTellerUtilizationForPeriod(periodName);
            
            // Display metrics
            System.out.printf("Average waiting time in queue: %.2f minutes%n", avgWaitingTime);
            System.out.printf("Average time in system (waiting + service): %.2f minutes%n", avgSystemTime);
            System.out.printf("Average number of customers in queue: %.2f%n", avgQueueLength);
            System.out.printf("Utilization rate of tellers: %.2f%%%n", tellerUtilization * 100);
            System.out.printf("Probability of all tellers being busy: %.2f%%%n", allTellersBusyProb * 100);
            System.out.printf("Total customers served: %d%n", waitingTimes.size());
            System.out.println();
        }
        
        System.out.println("Total customers arrived: " + allCustomers.size());
        System.out.println("Total customers completed: " + completedCustomers.size());
    }
    
    private static double calculateTellerUtilizationForPeriod(String periodName) {
        if (periodName.equals("Overall")) {
            // Calculate overall utilization across all periods
            long totalBusyTime = 0;
            long totalAvailableTime = 0;
            
            for (Customer customer : completedCustomers) {
                totalBusyTime += Duration.between(customer.getServiceStartTime(), customer.getDepartureTime()).toMinutes();
            }
            
            for (TimePeriod period : TIME_PERIODS) {
                long periodDuration = Duration.between(period.getStart(), period.getEnd()).toMinutes();
                totalAvailableTime += periodDuration * period.getTellerCount();
            }
            
            return totalAvailableTime > 0 ? (double) totalBusyTime / totalAvailableTime : 0;
        } else {
            // Calculate utilization for specific period
            TimePeriod period = TIME_PERIODS.stream()
                    .filter(p -> p.getName().equals(periodName))
                    .findFirst()
                    .orElse(null);
            
            if (period == null) {
                return 0;
            }
            
            long totalBusyTime = 0;
            
            for (Customer customer : completedCustomers) {
                // Only count customers who were served during this period
                LocalDateTime serviceStart = customer.getServiceStartTime();
                LocalDateTime serviceEnd = customer.getDepartureTime();
                
                if (serviceStart != null && serviceEnd != null && 
                    serviceStart.isBefore(period.getEnd()) && 
                    serviceEnd.isAfter(period.getStart())) {
                    
                    // Clip times to period boundaries
                    LocalDateTime effectiveStart = serviceStart.isBefore(period.getStart()) ? period.getStart() : serviceStart;
                    LocalDateTime effectiveEnd = serviceEnd.isAfter(period.getEnd()) ? period.getEnd() : serviceEnd;
                    
                    totalBusyTime += Duration.between(effectiveStart, effectiveEnd).toMinutes();
                }
            }
            
            long periodDuration = Duration.between(period.getStart(), period.getEnd()).toMinutes();
            long totalAvailableTime = periodDuration * period.getTellerCount();
            
            return totalAvailableTime > 0 ? (double) totalBusyTime / totalAvailableTime : 0;
        }
    }
    
    private static int getTellersCount(LocalDateTime time) {
        for (TimePeriod period : TIME_PERIODS) {
            if (!time.isBefore(period.getStart()) && time.isBefore(period.getEnd())) {
                return period.getTellerCount();
            }
        }
        return 0; // Outside of business hours
    }
    
    private static String getPeriodName(LocalDateTime time) {
        for (TimePeriod period : TIME_PERIODS) {
            if (!time.isBefore(period.getStart()) && time.isBefore(period.getEnd())) {
                return period.getName();
            }
        }
        return "Outside hours";
    }

    // Teller class
    static class Teller implements Runnable {
        private final int id;
        private volatile boolean busy = false;
        private volatile boolean serving = true;
        private Customer currentCustomer = null;
        
        public Teller(int id) {
            this.id = id;
        }
        
        @Override
        public void run() {
            Random random = ThreadLocalRandom.current();
            
            while (serving || busy) {
                try {
                    // Try to get a customer from the queue with a timeout
                    Customer customer = customerQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                    
                    if (customer != null) {
                        // Start serving the customer
                        LocalDateTime currentTime = simulationClock.get();
                        customer.setServiceStartTime(currentTime);
                        customer.setServedByTeller(id);
                        busy = true;
                        currentCustomer = customer;
                        
                        // Record waiting time
                        double waitingTime = Duration.between(customer.getArrivalTime(), currentTime).toMinutes();
                        String periodName = getPeriodName(customer.getArrivalTime());
                        
                        if (!periodName.equals("Outside hours")) {
                            waitingTimesByPeriod.get(periodName).add(waitingTime);
                            waitingTimesByPeriod.get("Overall").add(waitingTime);
                        }
                        
                        // Generate service time from exponential distribution
                        double serviceTimeMinutes = -Math.log(1 - random.nextDouble()) / SERVICE_RATE;
                        
                        // Sleep to simulate service time (adjusted for simulation speed)
                        Thread.sleep((long)(serviceTimeMinutes * 1000 / SIMULATION_SPEED));
                        
                        // Record customer departure
                        LocalDateTime departureTime = simulationClock.get();
                        customer.setDepartureTime(departureTime);
                        
                        // Calculate total time in system
                        double totalTime = Duration.between(customer.getArrivalTime(), departureTime).toMinutes();
                        
                        // Update stats
                        if (!periodName.equals("Outside hours")) {
                            systemTimesByPeriod.get(periodName).add(totalTime);
                            systemTimesByPeriod.get("Overall").add(totalTime);
                        }
                        
                        // Add to completed customers
                        completedCustomers.add(customer);
                        
                        // Mark as no longer busy
                        busy = false;
                        currentCustomer = null;
                    } else {
                        // No customer available, just wait
                        busy = false;
                        currentCustomer = null;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        
        public boolean isBusy() {
            return busy;
        }
        
        public void stopServing() {
            this.serving = false;
        }
    }

    // Customer class
    static class Customer {
        private final int id;
        private final LocalDateTime arrivalTime;
        private LocalDateTime serviceStartTime = null;
        private LocalDateTime departureTime = null;
        private int servedByTeller = -1;
        
        public Customer(int id, LocalDateTime arrivalTime) {
            this.id = id;
            this.arrivalTime = arrivalTime;
        }
        
        public int getId() {
            return id;
        }
        
        public LocalDateTime getArrivalTime() {
            return arrivalTime;
        }
        
        public LocalDateTime getServiceStartTime() {
            return serviceStartTime;
        }
        
        public void setServiceStartTime(LocalDateTime serviceStartTime) {
            this.serviceStartTime = serviceStartTime;
        }
        
        public LocalDateTime getDepartureTime() {
            return departureTime;
        }
        
        public void setDepartureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
        }
        
        public int getServedByTeller() {
            return servedByTeller;
        }
        
        public void setServedByTeller(int servedByTeller) {
            this.servedByTeller = servedByTeller;
        }
    }

    // TimePeriod class
    static class TimePeriod {
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final int tellerCount;
        private final String name;
        
        public TimePeriod(LocalDateTime start, LocalDateTime end, int tellerCount, String name) {
            this.start = start;
            this.end = end;
            this.tellerCount = tellerCount;
            this.name = name;
        }
        
        public LocalDateTime getStart() {
            return start;
        }
        
        public LocalDateTime getEnd() {
            return end;
        }
        
        public int getTellerCount() {
            return tellerCount;
        }
        
        public String getName() {
            return name;
        }
    }
}