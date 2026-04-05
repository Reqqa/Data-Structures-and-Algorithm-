
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

        // Move the list OUTSIDE the loop so it persists between algorithm runs
        List<InvestmentProject> projects = new ArrayList<>();

        while (running) {

            // ---------------------------------------------------------
            // PHASE 1: DATA SELECTION (Only runs if the dataset is empty)
            // ---------------------------------------------------------
            if (projects.isEmpty()) {
                System.out.println("\n[ Data Selection Menu ]");
                System.out.println("1. Read dataset from external CSV file");
                System.out.println("2. Generate random dataset");
                System.out.println("0. Exit program");
                System.out.print("Enter your choice: ");

                int dataChoice = getUserInput();
                IDataLoader dataLoader = null; // Use the interface polymorphically!

                if (dataChoice == 0) {
                    running = false;
                    System.out.println("Exiting system. Goodbye!");
                    break;
                } else if (dataChoice == 1) {
                    System.out.print("Enter the filename (e.g., dataset.csv): ");
                    String filename = scanner.nextLine();
                    dataLoader = new CsvDataLoader(filename);
                } else if (dataChoice == 2) {
                    System.out.print("Enter the number of projects to generate: ");
                    int count = getUserInput();
                    if (count > 0) {
                        dataLoader = new RandomDataLoader(count);
                    } else {
                        System.out.println("[!] Invalid number. Returning to menu.");
                        continue;
                    }
                } else {
                    System.out.println("[!] Invalid choice. Please select 1, 2, or 0.");
                    continue;
                }

                // Execute the polymorphic load method
                if (dataLoader != null) {
                    projects = dataLoader.loadData();
                }

                if (projects == null || projects.isEmpty()) {
                    System.out.println("[!] No valid data loaded. Please try again.");
                    continue;
                }
                System.out.println("[+] Successfully loaded " + projects.size() + " projects.");
            }

            // ---------------------------------------------------------
            // PHASE 2: ALGORITHM SELECTION
            // ---------------------------------------------------------
            System.out.println("\n[ Algorithm Selection Menu ] --- " + projects.size() + " Projects Loaded");
            System.out.println("1. Backtracking Algorithm (Exact, O(2^n))");
            System.out.println("2. Greedy + Union-Find (Heuristic, Fast)");
            System.out.println("3. Dynamic Programming (Exact, Pseudo-polynomial)");
            System.out.println("4. Genetic Algorithm (Approximation)");
            System.out.println("----------------------------------------");
            System.out.println("8. View Original Dataset");
            System.out.println("9. Load Different Dataset");
            System.out.println("0. Exit program");
            System.out.print("Select action: ");

            int algoChoice = getUserInput();
            IInvestmentAlgorithm solver = null;

            if (algoChoice == 0) {
                running = false;
                System.out.println("Exiting system. Goodbye!");
                break;
            } else if (algoChoice == 9) {
                projects.clear(); // Emptying the list forces the Data Selection Menu to reappear
                System.out.println("[+] Dataset cleared.");
                continue;
            } else if (algoChoice == 8) {
                System.out.println("\n[ Original Dataset ]");
                for (InvestmentProject p : projects) {
                    System.out.println(p.toString());
                }
                continue; // Return to Algorithm menu
            }

            // Process Algorithm Selection
            switch (algoChoice) {
                case 1:
                    solver = new BacktrackingSolver();
                    break;
                case 2:
                    System.out.println("[!] Greedy algorithm not yet implemented.");
                    break;
                case 3:
                    System.out.println("[!] DP algorithm not yet implemented.");
                    break;
                case 4:
                    System.out.print("Run with default GA settings (1) or Custom settings (2)? ");
                    int gaChoice = getUserInput();

                    if (gaChoice == 2) {
                        try {
                            System.out.println("--- Advanced GA Configuration ---");
                            System.out.print("Population Size (e.g., 100): ");
                            int pop = getUserInput();
                            System.out.print("Max Generations (e.g., 500): ");
                            int gen = getUserInput();
                            System.out.print("Elitism Count (e.g., 5): ");
                            int elite = getUserInput();
                            System.out.print("Crossover Rate (e.g., 0.85): ");
                            double cross = Double.parseDouble(scanner.nextLine().trim());
                            System.out.print("Mutation Rate (e.g., 0.05): ");
                            double mut = Double.parseDouble(scanner.nextLine().trim());

                            solver = new GeneticAlgorithmSolver(pop, gen, elite, cross, mut);
                        } catch (Exception e) {
                            System.out.println("[!] Invalid custom input. Defaulting to standard GA settings.");
                            solver = new GeneticAlgorithmSolver();
                        }
                    } else {
                        solver = new GeneticAlgorithmSolver();
                    }
                    break;
                default:
                    System.out.println("[!] Invalid algorithm selection.");
            }

            // ---------------------------------------------------------
            // PHASE 3: EXECUTION
            // ---------------------------------------------------------
            if (solver != null) {
                System.out.println("\nExecuting " + solver.getAlgorithmName() + "...");
                solver.solve(projects);

                // Polymorphism in action
                ((AbstractInvestmentSolver) solver).displayResults(projects);
            }
        }
        scanner.close();
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================
    /**
     * Safely reads an integer from the user without crashing on text input.
     */
    private static int getUserInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1; // Return an invalid option to trigger default switch cases
        }
    }

    /**
     * Reads and parses project data from a CSV file.
     */
    private static List<InvestmentProject> readFromFile(String filename) {
        List<InvestmentProject> loadedProjects = new ArrayList<>();
        File file = new File(filename);

        try (Scanner fileScanner = new Scanner(file)) {
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine(); // Skip header
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

                        loadedProjects.add(new InvestmentProject(id, name, sector, profit, jobs, deadline));

                    } catch (IllegalArgumentException e) {
                        System.out.println("[!] Data error on line " + lineNumber + ": " + e.getMessage());
                    }
                }
                lineNumber++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("[!] Error: The file '" + filename + "' was not found.");
        }

        return loadedProjects;
    }

    /**
     * Generates a realistic random dataset.
     */
    private static List<InvestmentProject> generateRandomData(int count) {
        List<InvestmentProject> randomProjects = new ArrayList<>();
        Random rand = new Random();
        String[] sectors = {"Technology", "Real Estate", "Healthcare", "Energy", "Finance"};

        for (int i = 1; i <= count; i++) {
            String id = "PRJ" + String.format("%03d", i);
            String name = "Initiative " + (char) (rand.nextInt(26) + 'A') + rand.nextInt(100);
            String sector = sectors[rand.nextInt(sectors.length)];

            double profit = 1.0 + (49.0 * rand.nextDouble());
            profit = Math.round(profit * 10.0) / 10.0;

            int jobs = 100 + rand.nextInt(4900);
            int maxDeadline = Math.max(3, count / 2);
            int deadline = 1 + rand.nextInt(maxDeadline);

            randomProjects.add(new InvestmentProject(id, name, sector, profit, jobs, deadline));
        }
        return randomProjects;
    }
}
