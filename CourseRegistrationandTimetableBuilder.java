import java.util.Scanner;

public class CourseRegistrationandTimetableBuilder {

    // ---------- Constants ----------
    static final int MAX_COURSES = 20;
    static final int MAX_REGISTERED = 10;
    static final int MAX_CREDITS = 24;
    static final String[] WEEK_DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    static Scanner sc = new Scanner(System.in);

    // ---------- Course data (parallel 1D arrays) ----------
    static String[] courseId = new String[MAX_COURSES];
    static String[] courseName = new String[MAX_COURSES];
    static int[] credits = new int[MAX_COURSES];
    static String[] day = new String[MAX_COURSES];
    static String[] time = new String[MAX_COURSES];
    static int courseCount = 0;

    // ---------- Registered courses (stores course indexes) ----------
    static int[] registered = new int[MAX_REGISTERED];
    static int registeredCount = 0;

    // ---------- Input helpers ----------
    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    // Returns the properly capitalised weekday name, or null if invalid
    static String matchDay(String input) {
        for (String d : WEEK_DAYS) {
            if (d.equalsIgnoreCase(input)) {
                return d;
            }
        }
        return null;
    }

    // ---------- Search helpers ----------
    static int searchCourse(String id) {
        for (int i = 0; i < courseCount; i++) {
            if (courseId[i].equalsIgnoreCase(id)) {
                return i;
            }
        }
        return -1;
    }

    static boolean isRegistered(int courseIndex) {
        for (int i = 0; i < registeredCount; i++) {
            if (registered[i] == courseIndex) {
                return true;
            }
        }
        return false;
    }

    static int totalCredits() {
        int total = 0;
        for (int i = 0; i < registeredCount; i++) {
            total += credits[registered[i]];
        }
        return total;
    }

    static boolean hasClash(int newIndex) {
        for (int i = 0; i < registeredCount; i++) {
            int e = registered[i];
            if (day[e].equalsIgnoreCase(day[newIndex]) && time[e].equalsIgnoreCase(time[newIndex])) {
                return true;
            }
        }
        return false;
    }

    // ---------- Features ----------
    static void addCourse() {
        if (courseCount >= MAX_COURSES) {
            System.out.println("Course limit reached.");
            return;
        }

        System.out.println("\n===== ADD COURSE =====");

        String id = readText("Enter Course ID: ");
        if (searchCourse(id) != -1) {
            System.out.println("A course with this ID already exists.");
            return;
        }

        String name = readText("Enter Course Name: ");

        int cr = readInt("Enter Credits: ");
        if (cr <= 0 || cr > MAX_CREDITS) {
            System.out.println("Credits must be between 1 and " + MAX_CREDITS + ".");
            return;
        }

        String d = matchDay(readText("Enter Day (Monday-Friday): "));
        if (d == null) {
            System.out.println("Invalid day. Use Monday to Friday.");
            return;
        }

        String t = readText("Enter Time (e.g. 9:00): ");

        courseId[courseCount] = id;
        courseName[courseCount] = name;
        credits[courseCount] = cr;
        day[courseCount] = d;
        time[courseCount] = t;
        courseCount++;

        System.out.println("Course added successfully.");
    }

    static void displayCourses() {
        System.out.println("\n===== AVAILABLE COURSES =====");

        if (courseCount == 0) {
            System.out.println("No courses available.");
            return;
        }

        for (int i = 0; i < courseCount; i++) {
            System.out.println((i + 1) + ". " + courseId[i] + " - " + courseName[i]
                    + " - " + credits[i] + " Credits - " + day[i] + " " + time[i]);
        }
    }

    static void registerCourse() {
        if (courseCount == 0) {
            System.out.println("No courses available.");
            return;
        }
        if (registeredCount >= MAX_REGISTERED) {
            System.out.println("You cannot register more than " + MAX_REGISTERED + " courses.");
            return;
        }

        displayCourses();

        int index = searchCourse(readText("\nEnter Course ID to register: "));

        if (index == -1) {
            System.out.println("Course not found.");
        } else if (isRegistered(index)) {
            System.out.println("Course is already registered.");
        } else if (totalCredits() + credits[index] > MAX_CREDITS) {
            System.out.println("Registration failed.");
            System.out.println("Maximum credit limit is " + MAX_CREDITS + ".");
        } else if (hasClash(index)) {
            System.out.println("Registration failed.");
            System.out.println("Timetable clash detected.");
        } else {
            registered[registeredCount++] = index;
            System.out.println("Course registered successfully.");
        }
    }

    static void displayRegisteredCourses() {
        System.out.println("\n===== REGISTERED COURSES =====");

        if (registeredCount == 0) {
            System.out.println("No courses registered.");
            return;
        }

        for (int i = 0; i < registeredCount; i++) {
            int c = registered[i];
            System.out.println((i + 1) + ". " + courseId[c] + " - " + courseName[c]
                    + " - " + credits[c] + " Credits");
        }
        System.out.println("Total Credits: " + totalCredits());
    }

    static void displayTimetable() {
        System.out.println("\n===== WEEKLY TIMETABLE =====");

        for (String d : WEEK_DAYS) {
            System.out.println("\n" + d);
            boolean found = false;

            for (int i = 0; i < registeredCount; i++) {
                int c = registered[i];
                if (day[c].equalsIgnoreCase(d)) {
                    System.out.println(time[c] + " - " + courseName[c] + " (" + courseId[c] + ")");
                    found = true;
                }
            }

            if (!found) {
                System.out.println("No Classes");
            }
        }
    }

    static void dropCourse() {
        if (registeredCount == 0) {
            System.out.println("No courses registered.");
            return;
        }

        displayRegisteredCourses();

        int index = searchCourse(readText("\nEnter Course ID to drop: "));

        int position = -1;
        for (int i = 0; i < registeredCount; i++) {
            if (registered[i] == index) {
                position = i;
                break;
            }
        }

        if (index == -1 || position == -1) {
            System.out.println("Course is not registered.");
            return;
        }

        // Shift remaining entries left
        for (int i = position; i < registeredCount - 1; i++) {
            registered[i] = registered[i + 1];
        }
        registeredCount--;

        System.out.println("Course dropped successfully.");
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" COURSE REGISTRATION & TIMETABLE BUILDER");
        System.out.println("==============================================");

        int choice;

        do {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Add Course");
            System.out.println("2. Display Available Courses");
            System.out.println("3. Register Course");
            System.out.println("4. View Registered Courses");
            System.out.println("5. View Timetable");
            System.out.println("6. Drop Course");
            System.out.println("7. Exit");

            choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1: addCourse(); break;
                case 2: displayCourses(); break;
                case 3: registerCourse(); break;
                case 4: displayRegisteredCourses(); break;
                case 5: displayTimetable(); break;
                case 6: dropCourse(); break;
                case 7: System.out.println("\nThank you for using the system!"); break;
                default: System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 7);

        sc.close();
    }
}
    

