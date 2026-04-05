
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  Financial Investment Allocation System");
        System.out.println("==================================================");

        boolean running = true;

        while (running) {
            List<InvestmentProject> projects = new ArrayList<>();

            // ---------------------------------------------------------
            // STEP 1: Handle Data Input
            // ---------------------------------------------------------
            System.out.println("\n[ Data Selection Menu ]");
            System.out.println("1. Read dataset from external CSV file");
            System.out.println("2. Generate random dataset");
            System.out.println("0. Exit program");
            System.out.print("Enter your choice: ");

            int dataChoice = getUserInput();

            if (dataChoice == 0) {
                running = false;
                System.out.println("Exiting system. Goodbye!");
                continue;
            } else if (dataChoice == 1) {
                System.out.print("Enter the filename (e.g., dataset.csv): ");
                String filename = scanner.nextLine();
                projects = readFromFile(filename);
            } else if (dataChoice == 2) {
                System.out.print("Enter the number of projects to generate: ");
                int count = getUserInput();
                if (count > 0) {
                    projects = generateRandomData(count);
                } else {
                    System.out.println("[!] Invalid number. Returning to menu.");
                    continue;
                }
            } else {
                System.out.println("[!] Invalid choice. Please select 1, 2, or 0.");
                continue;
            }

            // If data loading failed or was empty, restart the loop
            if (projects == null || projects.isEmpty()) {
                System.out.println("[!] No valid data loaded. Please try again.");
                continue;
            }

            System.out.println("[+] Successfully loaded " + projects.size() + " projects.");

            // ---------------------------------------------------------
            // STEP 2: Algorithm Selection
            // ---------------------------------------------------------
            System.out.println("  Select Algorithm");
            System.out.println("  1 Backtracking Algorithm");
            System.out.println("  2 Greedy + Disjoint Set / Union Find");
            System.out.println("  3 Dynamic Programming");
            System.out.println("  4 Genetic Algorithm");
            System.out.println("  0 Exit");
            System.out.print("  Enter choice: ");

            int algoChoice = getUserInput();
            IInvestmentAlgorithm solver = null;

            switch (algoChoice) {
                case 1:
                    solver = new BacktrackingSolver();
                    break;
                case 2:
                    // solver = new GreedySolver(); 
                    System.out.println("[!] Greedy algorithm not yet implemented.");
                    break;
                case 3:
                    // solver = new DynamicProgrammingSolver();
                    System.out.println("[!] DP algorithm not yet implemented.");
                    break;
                case 4:
                    // solver = new GeneticAlgorithmSolver();
                    break;
                default:
                    System.out.println("[!] Invalid algorithm selection.");
            }
            // ---------------------------------------------------------
            // STEP 3: Execute and Display
            // ---------------------------------------------------------
            if (solver != null) {
                System.out.println("\n[ Original Dataset ]");
                for (InvestmentProject p : projects) {
                    System.out.println(p.toString());
                }

                System.out.println("\nExecuting " + solver.getAlgorithmName() + "...");
                solver.solve(projects);

                // Pass the original projects list so it can calculate the unselected ones!
                ((AbstractInvestmentSolver) solver).displayResults(projects);
            }
        }
        scanner.close();
    }

    // =========================================================
    // HELPER METHODS FOR ROBUSTNESS
    // =========================================================
    /**
     * Safely reads an integer from the user without crashing on text input.
     */
    private static int getUserInput() {
        try {
            int input = Integer.parseInt(scanner.nextLine().trim());
            return input;
        } catch (NumberFormatException e) {
            return -1; // Return an invalid option to trigger the default switch cases
        }
    }

    /**
     * Reads and parses project data from a CSV file.
     */
    private static List<InvestmentProject> readFromFile(String filename) {
        List<InvestmentProject> loadedProjects = new ArrayList<>();
        File file = new File(filename);

        try (Scanner fileScanner = new Scanner(file)) {
            // Skip the header row if your CSV has one
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }

            int lineNumber = 2;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");

                if (parts.length == 6) {
                    try {
                        String id = parts[0].trim();
                        String name = parts[1].trim();
                        String sector = parts[2].trim();
                        double profit = Double.parseDouble(parts[3].trim());
                        int jobs = Integer.parseInt(parts[4].trim());
                        int deadline = Integer.parseInt(parts[5].trim());

                        // The constructor will throw an exception if data is negative
                        loadedProjects.add(new InvestmentProject(id, name, sector, profit, jobs, deadline));
                    } catch (IllegalArgumentException e) {
                        System.out.println("[!] Data error on line " + lineNumber + ": " + e.getMessage() + ". Skipping project.");
                    }
                }
                lineNumber++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("[!] Error: The file '" + filename + "' was not found in the project directory.");
        }

        return loadedProjects;
    }

    /**
     * Generates a realistic random dataset for testing scalability.
     */
    private static List<InvestmentProject> generateRandomData(int count) {
        List<InvestmentProject> randomProjects = new ArrayList<>();
        Random rand = new Random();
        String[] sectors = {"Technology", "Real Estate", "Healthcare", "Energy", "Finance"};

        for (int i = 1; i <= count; i++) {
            String id = "PRJ" + String.format("%03d", i);
            String name = "Initiative " + (char) (rand.nextInt(26) + 'A') + rand.nextInt(100);
            String sector = sectors[rand.nextInt(sectors.length)];

            // Generate profit between 1.0 and 50.0 RM Billion
            double profit = 1.0 + (49.0 * rand.nextDouble());
            // Round to 1 decimal place
            profit = Math.round(profit * 10.0) / 10.0;

            int jobs = 100 + rand.nextInt(4900); // 100 to 5000 jobs

            // Random deadline between 1 and a dynamic upper limit based on dataset size
            int maxDeadline = Math.max(3, count / 2);
            int deadline = 1 + rand.nextInt(maxDeadline);

            randomProjects.add(new InvestmentProject(id, name, sector, profit, jobs, deadline));
        }
        return randomProjects;
    }
}
