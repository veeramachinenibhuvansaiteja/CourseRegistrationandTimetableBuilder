import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/*
 * COURSE REGISTRATION & TIMETABLE BUILDER   (one file, short version)
 *
 * WHERE EACH CO IS USED (search for the tag, e.g. "CO4", to find it)
 *   CO1  variables, constants, expressions, Scanner input, output ... readInt(), main()
 *   CO2  if-else, switch, do-while, for, while ........................ main(), register(), showTimetable()
 *   CO3  methods, recursion, 1-D and 2-D arrays ....................... totalCredits(), DAYS, timetable
 *   CO4  classes, constructors, inheritance, polymorphism, exceptions . Course, LabCourse, register()
 *   CO5  StringBuilder, String methods, sorting, file I/O ............. showTimetable(), sortCourses(), save(), load()
 *   CO6  collections, lambda, streams, Optional ....................... courses list, find(), reports()
 */
public class CourseRegistration {

    // ---------- CO1: constants and variables ----------
    static final int MAX_CREDITS = 24;
    static final String FILE = "courses.txt";
    static Scanner sc = new Scanner(System.in);

    // ---------- CO3: 1-D arrays (days, time slots) and 2-D array (timetable) ----------
    static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    static final String[] TIMES = {"9:00", "10:00", "11:00", "12:00", "2:00"};
    static Course[][] timetable = new Course[DAYS.length][TIMES.length];

    // ---------- CO6: collections (List with generics) ----------
    static ArrayList<Course> courses = new ArrayList<>();
    static ArrayList<Course> registered = new ArrayList<>();

    // ================= CO4: CLASSES, ENCAPSULATION, INHERITANCE =================
    static class Course implements Comparable<Course> {      // CO5: Comparable (used for sorting)
        private String id, name, day;                        // CO4: private fields (encapsulation)
        private int credits, slot;

        Course(String id, String name, int credits, String day, int slot) {
            if (dayIndex(day) < 0 || slot < 0 || slot >= TIMES.length || credits < 1 || credits > 6)
                throw new IllegalArgumentException("Invalid course data");   // CO4: throw
            this.id = id; this.name = name; this.credits = credits; this.day = day; this.slot = slot;
        }
        Course(String id, String name, String day, int slot) {   // CO4: constructor overloading
            this(id, name, 3, day, slot);                        // default 3 credits
        }
        String getId() { return id; }
        String getName() { return name; }
        int getCredits() { return credits; }
        String getDay() { return day; }
        int getSlot() { return slot; }
        int getHours() { return credits; }                       // 1 credit = 1 hour a week
        public int compareTo(Course o) { return id.compareTo(o.id); }
        public String toString() { return id + " - " + name + " - " + credits + " credits - " + day + " " + TIMES[slot]; }
    }

    static class LabCourse extends Course {                      // CO4: inheritance
        LabCourse(String id, String name, int credits, String day, int slot) { super(id, name, credits, day, slot); }
        @Override int getHours() { return getCredits() * 2; }    // CO4: polymorphism (overriding)
        @Override public String toString() { return super.toString() + " [Lab]"; }
    }

    // ================= CO4: CUSTOM EXCEPTIONS =================
    static class ClashException extends Exception {
        ClashException(String msg) { super(msg); }
    }
    static class CreditLimitException extends Exception {
        CreditLimitException(String msg) { super(msg); }
    }

    // ================= CO1 / CO2: INPUT HELPERS =================
    static int readInt(String prompt) {
        while (true) {                                           // CO2: loop until the input is valid
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {                  // CO4: handling an exception
                System.out.println("Please enter a number.");
            }
        }
    }

    static String readText(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // ================= CO3: METHODS, RECURSION, ARRAYS =================
    static int dayIndex(String day) {                            // linear search in a 1-D array
        for (int i = 0; i < DAYS.length; i++)
            if (DAYS[i].equalsIgnoreCase(day)) return i;         // CO5: String comparison
        return -1;
    }

    static int totalCredits(int i) {                             // CO3: RECURSION
        if (i == registered.size()) return 0;                    // base case
        return registered.get(i).getCredits() + totalCredits(i + 1);   // recursive case
    }

    // CO6: Optional - a search result that may be empty (no null checks)
    static Optional<Course> find(ArrayList<Course> list, String id) {
        return list.stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst();
    }

    // ================= FEATURES =================
    static void addCourse() {
        int type = readInt("Type (1 = Theory, 2 = Lab): ");
        String id = readText("Course ID: ").toUpperCase();
        String name = readText("Course name: ");
        int credits = readInt("Credits (1-6): ");
        String day = readText("Day (Monday-Friday): ");
        int slot = readInt("Time slot (1-5 = 9:00, 10:00, 11:00, 12:00, 2:00): ") - 1;

        if (find(courses, id).isPresent()) {                     // CO2: if condition
            System.out.println("That ID already exists.");
            return;
        }
        try {
            courses.add(type == 2 ? new LabCourse(id, name, credits, day, slot)
                                  : new Course(id, name, credits, day, slot));
            System.out.println("Course added.");
        } catch (IllegalArgumentException e) {                   // CO4: catch the exception thrown in the constructor
            System.out.println(e.getMessage());
        }
    }

    static void showCourses(ArrayList<Course> list) {
        if (list.isEmpty()) System.out.println("Nothing to show.");
        for (Course c : list) System.out.println("  " + c);      // CO2: enhanced for loop, CO4: toString()
    }

    static void check(Course c) throws ClashException, CreditLimitException {   // CO4: throws
        if (totalCredits(0) + c.getCredits() > MAX_CREDITS)
            throw new CreditLimitException("Credit limit of " + MAX_CREDITS + " exceeded.");
        if (timetable[dayIndex(c.getDay())][c.getSlot()] != null)
            throw new ClashException("Clash with " + timetable[dayIndex(c.getDay())][c.getSlot()].getId());
    }

    static void register() {
        Optional<Course> found = find(courses, readText("Course ID to register: "));
        if (found.isEmpty()) { System.out.println("Course not found."); return; }
        Course c = found.get();
        if (registered.contains(c)) { System.out.println("Already registered."); return; }
        try {
            check(c);                                            // may throw either exception
            registered.add(c);
            timetable[dayIndex(c.getDay())][c.getSlot()] = c;    // CO3: store in 2-D array
            System.out.println("Registered " + c.getId());
        } catch (ClashException | CreditLimitException e) {      // CO4: multi-catch
            System.out.println("Registration failed: " + e.getMessage());
        } finally {                                              // CO4: finally always runs
            System.out.println("Total credits now: " + totalCredits(0) + " / " + MAX_CREDITS);
        }
    }

    static void drop() {
        Optional<Course> found = find(registered, readText("Course ID to drop: "));
        if (found.isPresent()) {
            Course c = found.get();
            registered.remove(c);
            timetable[dayIndex(c.getDay())][c.getSlot()] = null;
            System.out.println("Dropped " + c.getId());
        } else {
            System.out.println("That course is not registered.");
        }
    }

    static void showTimetable() {
        StringBuilder sb = new StringBuilder();                  // CO5: StringBuilder
        sb.append(String.format("%-7s", "Time"));                // CO1: formatted output
        for (String d : DAYS) sb.append(String.format("| %-11s", d));
        sb.append("\n");
        for (int t = 0; t < TIMES.length; t++) {                 // CO2: nested loops over the 2-D array
            sb.append(String.format("%-7s", TIMES[t]));
            for (int d = 0; d < DAYS.length; d++)
                sb.append(String.format("| %-11s", timetable[d][t] == null ? "-" : timetable[d][t].getId()));
            sb.append("\n");
        }
        System.out.print(sb);
    }

    static void sortCourses() {
        int mode = readInt("Sort by 1 = ID, 2 = name, 3 = credits (high to low): ");
        if (mode == 1) Collections.sort(courses);                                          // CO5: Comparable
        else if (mode == 2) courses.sort(Comparator.comparing(Course::getName));           // CO5: Comparator
        else courses.sort(Comparator.comparingInt(Course::getCredits).reversed());
        showCourses(courses);
    }

    static void search() {
        String key = readText("Search word in course name: ").toLowerCase();
        showCourses(courses.stream()
                .filter(c -> c.getName().toLowerCase().contains(key))     // CO5: String search, CO6: stream + lambda
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    // ================= CO5: FILE HANDLING =================
    static void save() {
        StringBuilder sb = new StringBuilder();
        for (Course c : courses)
            sb.append(c instanceof LabCourse ? "L" : "T").append(",").append(c.getId()).append(",")
              .append(c.getName()).append(",").append(c.getCredits()).append(",")
              .append(c.getDay()).append(",").append(c.getSlot()).append("\n");
        try {
            Files.writeString(Path.of(FILE), sb.toString());
            System.out.println("Saved to " + FILE);
        } catch (IOException e) {
            System.out.println("Could not save the file.");
        }
    }

    static void load() {
        try {
            for (String line : Files.readAllLines(Path.of(FILE))) {
                String[] p = line.split(",");                              // CO5: split()
                try {
                    if (find(courses, p[1]).isPresent()) continue;
                    int cr = Integer.parseInt(p[3]), slot = Integer.parseInt(p[5]);
                    courses.add(p[0].equals("L") ? new LabCourse(p[1], p[2], cr, p[4], slot)
                                                 : new Course(p[1], p[2], cr, p[4], slot));
                } catch (RuntimeException e) {                             // bad line: skip it
                    System.out.println("Skipped bad line: " + line);
                }
            }
            System.out.println("Loaded. Courses now: " + courses.size());
        } catch (IOException e) {
            System.out.println("File not found. Save first.");
        }
    }

    // ================= CO6: LAMBDAS, STREAMS, COLLECTIONS =================
    static void reports() {
        Predicate<Course> heavy = c -> c.getCredits() >= 3;                // CO6: functional interface + lambda

        String names = courses.stream().filter(heavy)                      // CO6: stream pipeline
                .map(Course::getName).sorted()                             // method reference
                .collect(Collectors.joining(", "));                        // Collector
        System.out.println("Courses with 3+ credits: " + names);

        long count = courses.stream().filter(heavy).count();
        int total = courses.stream().mapToInt(Course::getCredits).sum();
        System.out.println("Count: " + count + ", total credits in catalog: " + total);

        Map<String, Long> perDay = courses.stream()                        // CO6: Map + groupingBy
                .collect(Collectors.groupingBy(Course::getDay, Collectors.counting()));
        for (String d : DAYS)
            if (perDay.containsKey(d)) System.out.println("  " + d + ": " + perDay.get(d) + " course(s)");

        Set<Integer> distinct = new TreeSet<>();                           // CO6: Set (sorted, no duplicates)
        for (Course c : courses) distinct.add(c.getCredits());
        System.out.println("Different credit values: " + distinct);

        int hours = registered.stream().mapToInt(Course::getHours).sum();  // CO4 polymorphism inside a stream
        System.out.println("Weekly class hours of registered courses: " + hours);
    }

    static void sampleData() {
        courses.add(new Course("CS101", "Java Programming", 4, "Monday", 0));
        courses.add(new Course("MA101", "Engineering Maths", 4, "Tuesday", 1));
        courses.add(new Course("PH101", "Physics", "Wednesday", 0));            // default 3 credits
        courses.add(new LabCourse("CS102L", "Java Lab", 2, "Friday", 3));
        courses.add(new Course("CS103", "Data Structures", 3, "Monday", 0));    // clashes with CS101
        System.out.println("Sample data loaded.");
    }

    // ================= CO1 + CO2: MAIN MENU =================
    public static void main(String[] args) {
        int choice;
        do {                                                     // CO2: do-while loop
            System.out.println("""

                ===== MENU =====
                1 Add course        2 Show courses     3 Register
                4 Drop course       5 Timetable        6 Sort
                7 Search            8 Save to file     9 Load from file
                10 Reports          11 Sample data     12 Exit""");
            choice = readInt("Choice: ");
            switch (choice) {                                    // CO2: switch
                case 1: addCourse(); break;
                case 2: showCourses(courses); break;
                case 3: register(); break;
                case 4: drop(); break;
                case 5: showTimetable(); break;
                case 6: sortCourses(); break;
                case 7: search(); break;
                case 8: save(); break;
                case 9: load(); break;
                case 10: reports(); break;
                case 11: sampleData(); break;
                case 12: System.out.println("Bye!"); break;
                default: System.out.println("Invalid choice.");
            }
        } while (choice != 12);
    }
}
