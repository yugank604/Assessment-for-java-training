import java.util.*;

/*
 * ================= SMART CAMPUS MANAGEMENT SYSTEM =================
 * This program allows:
 * 1. Registering learners
 * 2. Creating subjects
 * 3. Assigning subjects to learners
 * 4. Viewing data
 * 5. Calculating total fees
 * 
 * Concepts used:
 * - OOP (Classes & Objects)
 * - Collections (HashMap, HashSet)
 * - Multithreading (Runnable)
 * - Exception Handling
 */

// ================= LEARNER CLASS =================
class Learner {
    int learnerId;
    String fullName;
    String emailAddress;

    // Constructor to initialize learner data
    public Learner(int learnerId, String fullName, String emailAddress) {
        this.learnerId = learnerId;
        this.fullName = fullName;
        this.emailAddress = emailAddress;
    }

    // Display learner details
    public String toString() {
        return learnerId + " | " + fullName + " | " + emailAddress;
    }
}

// ================= SUBJECT CLASS =================
class Subject {
    int subjectId;
    String subjectName;
    double price;

    // Constructor
    public Subject(int subjectId, String subjectName, double price) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.price = price;
    }

    // Display subject details
    public String toString() {
        return subjectId + " | " + subjectName + " | ₹" + price;
    }

    // Override equals() to compare subjects using subjectId
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Subject)) return false;
        Subject s = (Subject) obj;
        return this.subjectId == s.subjectId;
    }

    // Override hashCode() for HashSet consistency
    @Override
    public int hashCode() {
        return subjectId;
    }
}

// ================= CUSTOM EXCEPTION =================
class FeeException extends Exception {
    public FeeException(String msg) {
        super(msg);
    }
}

// ================= MULTITHREADING CLASS =================
class EnrollmentTask implements Runnable {

    String learnerName;
    String subjectName;

    // Constructor
    public EnrollmentTask(String learnerName, String subjectName) {
        this.learnerName = learnerName;
        this.subjectName = subjectName;
    }

    // run() method executes when thread starts
    public void run() {
        System.out.println("⏳ Processing enrollment for " + learnerName);

        try {
            // Simulating processing delay
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted!");
        }

        System.out.println("✅ " + learnerName + " enrolled in " + subjectName);
    }
}

// ================= MAIN CLASS =================
public class CampusManager {

    // Scanner for user input
    static Scanner sc = new Scanner(System.in);

    // Data storage using HashMaps
    static HashMap<Integer, Learner> learnerMap = new HashMap<>();
    static HashMap<Integer, Subject> subjectMap = new HashMap<>();
    static HashMap<Integer, HashSet<Subject>> registrationMap = new HashMap<>();

    public static void main(String[] args) {

        // Infinite loop for menu-driven system
        while (true) {

            // Display menu
            System.out.println("\n====== CAMPUS CONTROL PANEL ======");
            System.out.println("1. Register Learner");
            System.out.println("2. Create Subject");
            System.out.println("3. Assign Subject");
            System.out.println("4. View Learners");
            System.out.println("5. View Registrations");
            System.out.println("6. Calculate Total Fee");
            System.out.println("7. Exit");

            int choice;

            // Handle invalid input (like string instead of number)
            try {
                choice = sc.nextInt();
            } catch (Exception e) {
                System.out.println("⚠ Invalid input!");
                sc.nextLine(); // clear buffer
                continue;
            }

            // Perform action based on user choice
            switch (choice) {
                case 1 -> registerLearner();
                case 2 -> createSubject();
                case 3 -> assignSubject();
                case 4 -> viewLearners();
                case 5 -> viewRegistrations();
                case 6 -> calculateFee();
                case 7 -> {
                    System.out.println("👋 Exiting system...");
                    return;
                }
                default -> System.out.println("❌ Invalid choice!");
            }
        }
    }

    // ================= STEP 1: REGISTER LEARNER =================
    static void registerLearner() {

        System.out.print("Enter ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        // Store learner in HashMap
        learnerMap.put(id, new Learner(id, name, email));

        System.out.println("✔ Learner Registered Successfully!");
    }

    // ================= STEP 2: CREATE SUBJECT =================
    static void createSubject() {

        try {
            System.out.print("Enter Subject ID: ");
            int id = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter Subject Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Fee: ");
            double fee = sc.nextDouble();

            // Throw exception if fee is invalid
            if (fee < 0) {
                throw new FeeException("Fee cannot be negative!");
            }

            // Store subject
            subjectMap.put(id, new Subject(id, name, fee));

            System.out.println("✔ Subject Created!");

        } catch (FeeException e) {
            System.out.println("⚠ " + e.getMessage());
        }
    }

    // ================= STEP 3: ASSIGN SUBJECT =================
    static void assignSubject() {

        System.out.print("Enter Learner ID: ");
        int learnerId = sc.nextInt();

        System.out.print("Enter Subject ID: ");
        int subjectId = sc.nextInt();

        // Fetch objects directly using HashMap
        Learner learner = learnerMap.get(learnerId);
        Subject subject = subjectMap.get(subjectId);

        // Validate input
        if (learner == null || subject == null) {
            System.out.println("❌ Invalid details!");
            return;
        }

        /*
         * Synchronization ensures:
         * Only one thread modifies registrationMap at a time
         */
        synchronized (registrationMap) {

            // Create entry if not exists
            registrationMap.putIfAbsent(learnerId, new HashSet<>());

            HashSet<Subject> set = registrationMap.get(learnerId);

            // Prevent duplicate enrollment
            if (set.contains(subject)) {
                System.out.println("⚠ Already enrolled!");
                return;
            }

            // Add subject
            set.add(subject);
        }

        // Multithreading: process enrollment in background
        Thread t = new Thread(new EnrollmentTask(learner.fullName, subject.subjectName));
        t.start();
    }

    // ================= STEP 4: VIEW LEARNERS =================
    static void viewLearners() {
        learnerMap.values().forEach(System.out::println);
    }

    // ================= STEP 5: VIEW REGISTRATIONS =================
    static void viewRegistrations() {
        for (int id : registrationMap.keySet()) {
            System.out.println("Learner ID: " + id);

            for (Subject s : registrationMap.get(id)) {
                System.out.println("  -> " + s);
            }
        }
    }

    // ================= STEP 6: CALCULATE TOTAL FEE =================
    static void calculateFee() {

        System.out.print("Enter Learner ID: ");
        int id = sc.nextInt();

        // Check if learner has any subjects
        if (!registrationMap.containsKey(id)) {
            System.out.println("No registrations found!");
            return;
        }

        double total = 0;

        // Sum all subject fees
        for (Subject s : registrationMap.get(id)) {
            total += s.price;
        }

        System.out.println("💰 Total Fee = ₹" + total);
    }
}