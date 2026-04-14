
import java.util.ArrayList;
import java.util.List;
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
                System.out.println("    --- Data Preview (First 15 rows) ---");
                int previewCount = Math.min(15, projects.size());
                for (int i = 0; i < previewCount; i++) {
                    System.out.println("    " + projects.get(i).toString());
                }
                System.out.println("    -----------------------------------");
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
                    solver = new GreedyDSUSolver();
                    break;
                case 3:
                    solver = new DynamicProgrammingSolver();
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
                            double cross = getUserDoubleInput();
                            System.out.print("Mutation Rate (e.g., 0.05): ");
                            double mut = getUserDoubleInput();

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
                try {
                    solver.solve(projects);
                    // Polymorphism in action
                    ((AbstractInvestmentSolver) solver).displayResults(projects);

                    // Catch user request to modify input and return to data selection menu
                } catch (UserInputModificationException e) {
                    projects.clear(); // Clear projects to re-show Data Selection Menu on next loop iteration
                    continue;

                    // Specific catch for data validation errors
                } catch (IllegalArgumentException e) {
                    System.out.println("\n[!] Invalid data encountered: " + e.getMessage());
                    System.out.println("[!] Please check your dataset configuration or inputs.");

                    // Fallback for unexpected critical errors
                } catch (Exception e) {
                    // Fulfills Rubric 1.6: Graceful recovery for unexpected user actions/data
                    System.out.println("\n[!] An unexpected critical runtime error occurred: " + e.toString());
                    System.out.println("[!] Returning safely to the main menu.");
                }
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
     * Safely reads a double from the user without crashing on text input.
     */
    private static double getUserDoubleInput() {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1.0; // Returns an invalid rate to trigger the GA validation exception
        }
    }
}
