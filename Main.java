import java.io.*;
import java.util.*;

public class Main {
    static Scanner scanner = new Scanner(System.in);

    //Load Dataset.csv
    public static List<Schedule> loadCSV(String filepath) {
        List<Schedule> dataset = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;
                if (lineNum == 1) continue;

                String[] f = line.split(",");
                if (f.length < 6) {
                    System.out.println("  [!] Row " + lineNum + ": expected 6 fields, got "
                            + f.length + ". Skipped.");
                    continue;
                }

                String id          = f[0].trim();
                String projectName = f[1].trim();
                String sector      = f[2].trim();
                double profit      = Double.parseDouble(f[3].trim());
                int    jobsCreated = Integer.parseInt(f[4].trim());
                int    deadline    = Integer.parseInt(f[5].trim());

                if (profit <= 0) {
                    System.out.println("  [!] Row " + lineNum + ": profit must be > 0");
                    continue;
                }
                if (deadline <= 0) {
                    System.out.println("  [!] Row " + lineNum + ": deadline must be >= 1");
                    continue;
                }

                dataset.add(new Schedule(id, projectName, sector, profit, jobsCreated, deadline));
            }
            br.close();
            System.out.println("  Loaded " + dataset.size() + " records from: " + filepath);

        } catch (FileNotFoundException e) {
            System.out.println("  ERROR: File not found — " + filepath);
        } catch (NumberFormatException e) {
            System.out.println("  ERROR: Invalid number in CSV — " + e.getMessage());
        } catch (IOException e) {
            System.out.println("  ERROR: " + e.getMessage());
        }
        return dataset;
    }

    // Random Data Mixer
    static final String[] SECTORS = {
        "Manufacturing", "Services", "Property", "Trade",
        "Finance", "Green Energy", "Primary", "Digital"
    };
    static final String[] PROJ_NAMES = {
        "Solar Farm", "Data Centre", "Highway Upgrade", "Port Expansion",
        "Factory Build", "Bank Branch", "Tech Hub", "Mining Site",
        "Water Treatment", "Power Grid", "Rail Link", "Logistics Park",
        "EV Battery Plant", "Semiconductor Fab", "Tourism Resort"
    };

    public static List<Schedule> generateRandom(int count,
            double minP, double maxP, int minD, int maxD, int minJ, int maxJ) {
        List<Schedule> dataset = new ArrayList<>();
        Random rnd = new Random();
        List<String> names = new ArrayList<>(Arrays.asList(PROJ_NAMES));
        Collections.shuffle(names);

        for (int i = 0; i < count; i++) {
            String id      = "J" + (i + 1);
            String name    = (i < names.size()) ? names.get(i) : "Project " + (i + 1);
            String sector  = SECTORS[rnd.nextInt(SECTORS.length)];
            double profit  = Math.round((minP + (maxP - minP) * rnd.nextDouble()) * 10.0) / 10.0;
            int    jobs    = minJ + rnd.nextInt(maxJ - minJ + 1);
            int    deadline = minD + rnd.nextInt(maxD - minD + 1);
            dataset.add(new Schedule(id, name, sector, profit, jobs, deadline));
        }
        System.out.println("  Generated " + count + " random investment projects.");
        return dataset;
    }

    //Type check
    static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(scanner.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.println("  Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid — enter a whole number.");
            }
        }
    }

    static double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(scanner.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.println("  Please enter a value between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid — enter a decimal number.");
            }
        }
    }

    //print Output
    static void printOutput(String algoName,
                            List<Schedule> all,
                            List<Schedule> selected,
                            double totalProfit,
                            int totalJobsCreated) {

        // Build unselected list
        Set<String> selIds = new HashSet<>();
        for (Schedule s : selected) selIds.add(s.getId());
        List<Schedule> unselected = new ArrayList<>();
        for (Schedule s : all)
            if (!selIds.contains(s.getId())) unselected.add(s);

        String D  = "=".repeat(82);
        String D2 = "-".repeat(82);

        // 3a — All job details
        System.out.println("\n" + D);
        System.out.println("  3a. ALL JOB DETAILS");
        System.out.println(D);
        System.out.printf("  %-5s  %-32s  %-15s  %12s  %5s  %10s%n",
                "ID", "Project Name", "Sector", "Profit(RM B)", "Slot", "Jobs");
        System.out.println("  " + D2);
        for (Schedule s : all)
            System.out.printf("  %-5s  %-32s  %-15s  %12.1f  %5d  %,10d%n",
                    s.getId(), s.getProjectName(), s.getSector(),
                    s.getProfit(), s.getDeadline(), s.getJobsCreated());
        System.out.println(D);

        // 3b — Selected sequence
        System.out.println("\n" + D);
        System.out.println("  ALGORITHM : " + algoName);
        System.out.println(D);
        System.out.println("  3b. SELECTED JOB SEQUENCE (sorted by deadline slot):");
        System.out.println("  " + D2);
        System.out.printf("  %-5s  %-32s  %-15s  %12s  %5s  %10s%n",
                "ID", "Project Name", "Sector", "Profit(RM B)", "Slot", "Jobs");
        System.out.println("  " + D2);
        if (selected.isEmpty()) {
            System.out.println("  (No jobs could be scheduled)");
        } else {
            List<Schedule> display = new ArrayList<>(selected);
            display.sort(Comparator.comparingInt(Schedule::getDeadline));
            for (Schedule s : display)
                System.out.printf("  %-5s  %-32s  %-15s  %12.1f  %5d  %,10d%n",
                        s.getId(), s.getProjectName(), s.getSector(),
                        s.getProfit(), s.getDeadline(), s.getJobsCreated());
        }

        // 3c — Total profit
        System.out.println("  " + D2);
        System.out.printf("  %-55s  %12.1f%n", "3c. TOTAL PROFIT (RM Billion):", totalProfit);
        System.out.printf("  %-55s  %,12d%n",  "    TOTAL JOBS CREATED:",        totalJobsCreated);
        System.out.println(D);

        // 3d — Unselected jobs
        System.out.println("\n" + D);
        System.out.println("  3d. UNSELECTED (SKIPPED) JOBS:");
        System.out.println("  " + D2);
        if (unselected.isEmpty()) {
            System.out.println("  (All jobs were successfully scheduled)");
        } else {
            System.out.printf("  %-5s  %-32s  %12s  %5s  %-28s%n",
                    "ID", "Project Name", "Profit(RM B)", "Slot", "Reason");
            System.out.println("  " + D2);
            for (Schedule s : unselected)
                System.out.printf("  %-5s  %-32s  %12.1f  %5d  %-28s%n",
                        s.getId(), s.getProjectName(),
                        s.getProfit(), s.getDeadline(), "No free slot <= deadline");
        }
        System.out.println(D);
    }

    //Main
    public static void main(String[] args) {

        System.out.println("=".repeat(82));
        System.out.println("  Job Sequencing Solution");
        System.out.println("  Malaysia MIDA Investment Projects");
        System.out.println("=".repeat(82));

        //Choose data source
        List<Schedule> data = new ArrayList<>();
        boolean dataLoaded = false;

        while (!dataLoaded) {
            System.out.println("\n" + "-".repeat(82));
            System.out.println("  Choose an option");
            System.out.println("-".repeat(82));
            System.out.println("  [1] Load from dataset.csv");
            System.out.println("  [2] Generate random data");
            System.out.println("  [0] Exit");
            System.out.print("  Choice: ");
            String input = scanner.nextLine().trim();

            switch (input) {

                case "1":
                    // Load from dataset.csv in same directory as Main.java
                    System.out.println("\n  Loading data from dataset.csv...");
                    data = loadCSV("dataset.csv");
                    if (data.isEmpty()) {
                        System.out.println("  No data loaded. Please check dataset.csv.");
                    } else {
                        dataLoaded = true;
                    }
                    break;

                case "2":
                    // Generate random investment data with user-specified ranges
                    System.out.println();
                    int    n    = readInt   ("  Number of projects     (2-20)        : ", 2, 20);
                    double minP = readDouble("  Min profit  RM bil     (0.1-10)      : ", 0.1, 10.0);
                    double maxP = readDouble("  Max profit  RM bil     (" + minP + "-200) : ", minP, 200.0);
                    int    minD = readInt   ("  Min deadline slot       (1-5)         : ", 1, 5);
                    int    maxD = readInt   ("  Max deadline slot       (" + minD + "-10)   : ", minD, 10);
                    int    minJ = readInt   ("  Min jobs created        (100-1000)    : ", 100, 1000);
                    int    maxJ = readInt   ("  Max jobs created        (" + minJ + "-100000): ", minJ, 100000);
                    data = generateRandom(n, minP, maxP, minD, maxD, minJ, maxJ);
                    dataLoaded = true;
                    break;

                case "0":
                    System.out.println("\n  Goodbye!");
                    scanner.close();
                    return;

                default:
                    System.out.println("  Invalid choice. Please enter 1, 2, or 0.");
                    break;
            }
        }

        //Select and run algorithm
        boolean running = true;
        while (running) {

            System.out.println("\n" + "-".repeat(82));
            System.out.println("  Select Algorithm");
            System.out.println("-".repeat(82));
            System.out.println("  [1] Backtracking Algorithm");
            System.out.println("  [2] Genetic Algorithm");
            System.out.println("  [3] Dynamic Programming");
            System.out.println("  [4] Greedy + Disjoint Set / Union Find");
            System.out.println("  [0] Exit");
            System.out.print("  Choice: ");
            String algoInput = scanner.nextLine().trim();

            switch (algoInput) {

                case "1":
                    Backtracking bt = new Backtracking();
                    List<Schedule> btResult = bt.solve(data);
                    printOutput(bt.getAlgorithmName(), data, btResult,
                                bt.getTotalProfit(), bt.getTotalJobsCreated());
                    break;

                case "2":
                    System.out.println("  [!] Genetic Algorithm — not yet implemented.");
                    break;

                case "3":
                    System.out.println("  [!] Dynamic Programming — not yet implemented.");
                    break;

                case "4":
                    System.out.println("  [!] Greedy + Disjoint Set / Union Find — not yet implemented.");
                    break;

                case "0":
                    running = false;
                    System.out.println("\n  Goodbye!");
                    break;

                default:
                    System.out.println("  Invalid choice. Please enter 0, 1, 2, 3, or 4.");
                    break;
            }

            // Ask to run again only after an algorithm was selected (not after invalid or exit)
            if (running && (algoInput.equals("1") || algoInput.equals("2")
                         || algoInput.equals("3") || algoInput.equals("4"))) {
                System.out.print("\n  Run again? (yes/no): ");
                running = scanner.nextLine().trim().toLowerCase().startsWith("y");
            }
        }

        scanner.close();
    }
}