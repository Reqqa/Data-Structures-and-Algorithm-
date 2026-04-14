import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Solves the Job Sequencing problem using Dynamic Programming with bitmask-based state space.
 * 
 * <h2>Complexity & Memory Safety</h2>
 * <ul>
 *   <li><strong>Time:</strong> O(n × 2^D × D) where n = projects, D = max deadline (pseudo-polynomial)</li>
 *   <li><strong>Space:</strong> O(n × 2^D) for DP and reconstruction tables</li>
 * </ul>
 * 
 * <p>The maximum safe deadline is computed dynamically ({@link #computeSafeMaxDeadline(int)}) based on
 * available JVM heap memory using the formula: D ≤ log₂(availableMemory / (16 + 16*n))
 * with 10% safety margin. This eliminates fixed magic numbers and adapts to runtime system resources.
 * 
 * <p>When input deadline exceeds the computed safe limit, the solver prompts the user to either
 * modify input, choose a different algorithm, or accept the memory risk.
 */

public class DynamicProgrammingSolver extends AbstractInvestmentSolver {
    /**
     * Theoretical maximum deadline to prevent integer overflow.
     * The actual safe limit is computed dynamically via {@link #computeSafeMaxDeadline(int)}.
     */
    private static final int ABSOLUTE_MAX_DEADLINE = 25;
    
    /** Tracks the actual algorithm used when fallback occurs. */
    private String actualAlgorithmName = null;

    @Override
    public String getAlgorithmName() {
        if (actualAlgorithmName != null) {
            return actualAlgorithmName;
        }
        return "Dynamic Programming (Exact, Pseudo-polynomial)";
    }

    /**
     * Dynamically computes the maximum safe deadline based on available JVM heap memory.
     * 
     * <p>Formula: D ≤ log₂(availableMemory / (16 + 16*n))
     * <br>Memory per state = (16 + 16*n) bytes, where n = number of projects.
     * <br>Applies 10% safety margin; clamps to {@value #ABSOLUTE_MAX_DEADLINE}.
     * 
     * @param numProjects the number of projects
     * @return the maximum safe deadline that won't exceed available heap memory
     */
    private int computeSafeMaxDeadline(int numProjects) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long availableMemory = maxMemory - usedMemory;
        long safeMemory = (long) (availableMemory * 0.9);
        long memoryPerState = 16 + (16L * numProjects);
        long maxStates = safeMemory / memoryPerState;
        int maxD = (maxStates > 0) ? (int) Math.floor(Math.log(maxStates) / Math.log(2)) : 0;

        // Clamp to absolute maximum to prevent overflow
        return Math.min(maxD, ABSOLUTE_MAX_DEADLINE);
    }

    /**
     * Solves the job sequencing problem using dynamic programming with bitmask state representation.
     * 
     * <p>Validates that input deadline is within safe memory bounds computed dynamically.
     * If exceeded, prompts user to modify input, switch algorithms ({@link BacktrackingSolver},
     * {@link GreedyDSUSolver}, or {@link GeneticAlgorithmSolver}), or proceed at risk.
     * Otherwise executes optimal DP with O(2^D) memory and O(n × 2^D × D) time.
     * 
     * @param projects the list of investment projects; cannot be null or empty
     * @throws UserInputModificationException if maxDeadline exceeds safe limit and user chooses option 1 (modify input)
     * @see #computeSafeMaxDeadline(int) for dynamic memory-based deadline calculation
     */
    @Override
    public void solve(List<InvestmentProject> projects) {
        // records start time to measure execution duration
        long startTime = System.currentTimeMillis();

        // 1. Reset state
        this.maxExpectedReturn = 0.0;
        this.selectedPortfolio = new ArrayList<>();

        if (projects == null || projects.isEmpty()) {
            this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
            return;
        }

        // 2. Find max deadline and project count
        int n = projects.size();
        int maxDeadline = 0;
        for (InvestmentProject p : projects) {
            if (p.getDeadline() > maxDeadline) {
                maxDeadline = p.getDeadline();
            }
        }

        // Dynamically compute the safe maximum deadline based on available JVM memory
        int safeMaxDeadline = computeSafeMaxDeadline(n);

        // If deadline exceeds what memory allows, ask user what to do
        if (maxDeadline > safeMaxDeadline) {
            System.out.println("==================================================");
            System.out.println("[WARNING] Maximum deadline constraint violated!");
            System.out.println(String.format(
                "  Input maxDeadline: %d\n" +
                "  Safe memory limit: %d\n" +
                "  Reason: Exponential state space O(2^D) = %,d states\n" +
                "  Available heap: %.1f MB\n",
                maxDeadline, safeMaxDeadline, (long)Math.pow(2, maxDeadline),
                (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / (1024.0 * 1024.0)
            ));
            System.out.println("Select one of the following options:");
            System.out.println("  1. Modify your input (exit and restart with new data)");
            System.out.println("  2. Continue with BacktrackingSolver (guaranteed optimal, slower)");
            System.out.println("  3. Continue with GreedyDSUSolver (approximate, fast)");
            System.out.println("  4. Continue with GeneticAlgorithmSolver (approximate, adaptive)");
            System.out.print("Enter your choice (1-4): ");
            
            Scanner scanner = new Scanner(System.in);
            String choice = scanner.nextLine().trim();
            
            if (choice.equals("1")) {
                System.out.println("[INFO] Exiting solver. Please modify your input and try again.");
                System.out.println("==================================================\n");
                throw new UserInputModificationException(
                    String.format("Max deadline %d exceeds safe limit %d. User requested input modification.", 
                    maxDeadline, safeMaxDeadline)
                );
            } else if (choice.equals("2")) {
                System.out.println("[INFO] Proceeding with BacktrackingSolver (guaranteed optimal)...");
                System.out.println("==================================================\n");
                BacktrackingSolver backtracker = new BacktrackingSolver();
                backtracker.solve(projects);
                this.maxExpectedReturn = backtracker.getMaxExpectedReturn();
                this.selectedPortfolio = backtracker.selectedPortfolio;
                this.actualAlgorithmName = backtracker.getAlgorithmName();
                this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
                return;
            } else if (choice.equals("3")) {
                System.out.println("[INFO] Proceeding with Greedy DSU Solver (approximate, fast)...");
                System.out.println("==================================================\n");
                GreedyDSUSolver greedy = new GreedyDSUSolver();
                greedy.solve(projects);
                this.maxExpectedReturn = greedy.getMaxExpectedReturn();
                this.selectedPortfolio = greedy.selectedPortfolio;
                this.actualAlgorithmName = greedy.getAlgorithmName();
                this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
                return;
            } else if (choice.equals("4")) {
                System.out.println("[INFO] Proceeding with Genetic Algorithm Solver (approximate, adaptive)...");
                System.out.println("==================================================\n");
                GeneticAlgorithmSolver genetic = new GeneticAlgorithmSolver();
                genetic.solve(projects);
                this.maxExpectedReturn = genetic.getMaxExpectedReturn();
                this.selectedPortfolio = genetic.selectedPortfolio;
                this.actualAlgorithmName = genetic.getAlgorithmName();
                this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
                return;
            } else {
                System.out.println("[ERROR] Invalid choice. Proceeding with Backtracking Solver by default.");
                BacktrackingSolver backtracker = new BacktrackingSolver();
                backtracker.solve(projects);
                this.maxExpectedReturn = backtracker.getMaxExpectedReturn();
                this.selectedPortfolio = backtracker.selectedPortfolio;
                this.actualAlgorithmName = backtracker.getAlgorithmName();
                this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
                return;
            }
        }

        int maxMask = (int) Math.pow(2, maxDeadline);

        // 3. Sort projects by profit descending
        List<InvestmentProject> sortedProjects = new ArrayList<>(projects);
        Collections.sort(sortedProjects, new Comparator<InvestmentProject>() {
            @Override
            public int compare(InvestmentProject a, InvestmentProject b) {
                return Double.compare(b.getProfit(), a.getProfit());
            }
        });
        // -> this makes the DP more likely to consider high-profit choices first.

        // 4. DP table: use two arrays for space optimization to O(2^d)
        double[] prevRow = new double[maxMask];
        double[] currRow = new double[maxMask];
        // Initialize to negative infinity
        for (int mask = 0; mask < maxMask; mask++) {
            prevRow[mask] = Double.NEGATIVE_INFINITY;
            currRow[mask] = Double.NEGATIVE_INFINITY;
        }
        prevRow[0] = 0.0;

        int[][][] prev = new int[n + 1][maxMask][4]; 

        // 5. Fill DP table using two rows for O(2^d) space
        for (int i = 0; i < n; i++) {
            InvestmentProject p = sortedProjects.get(i);
            int d = p.getDeadline(); // deadline already validated to be <= MAX_BITMASK_DEADLINE
            double profit = p.getProfit();

            // Reset currRow to negative infinity
            for (int mask = 0; mask < maxMask; mask++) {
                currRow[mask] = Double.NEGATIVE_INFINITY;
            }

            // explore each mask for the previous state
            for (int mask = 0; mask < maxMask; mask++) {
                if (prevRow[mask] == Double.NEGATIVE_INFINITY) continue;

                // Op1: Do not take the project
                // record that this state came from not choosing project 
                if (currRow[mask] < prevRow[mask]) {
                    currRow[mask] = prevRow[mask];
                    prev[i + 1][mask] = new int[]{i, mask, 0, -1};
                }

                // Op2: Try to take the project in each possible slot <= d that is free
                // record that this state came from taking the project in this slot
                for (int slot = d; slot >= 1; slot--) {
                    int bit = slot - 1;

                    // Shift "1" to the position of "bit", Check if that position in "mask" is 0
                    // Check if slot is free
                    if ((mask & (1 << bit)) == 0) {
                        int newMask = mask | (1 << bit); // mark slot as used
                        double newProfit = prevRow[mask] + profit; // profit if we take this project
                        if (currRow[newMask] < newProfit) {
                            currRow[newMask] = newProfit;
                            prev[i + 1][newMask] = new int[]{i, mask, 1, slot}; // record that we took project i in slot "slot"
                        }
                    }
                }
            }

            // Swap rows: prevRow now becomes the current row for next iteration
            double[] temp = prevRow;
            prevRow = currRow;
            currRow = temp;
        }

        // 6. Find the best mask at i=n (now in prevRow)
        double bestProfit = Double.NEGATIVE_INFINITY;
        int bestMask = -1;
        for (int mask = 0; mask < maxMask; mask++) {
            if (prevRow[mask] > bestProfit) {
                bestProfit = prevRow[mask];
                bestMask = mask;
            }
        }

        this.maxExpectedReturn = bestProfit;

        // 7. Reconstruct the selected portfolio
        List<InvestmentProject> selected = new ArrayList<>();
        int currentI = n;
        int currentMask = bestMask;
        while (currentI > 0) {
            int[] info = prev[currentI][currentMask]; // load the reconstruction record
            int prevI = info[0];
            int prevMask = info[1];
            int took = info[2];
            int slot = info[3];

            // if this project was selected, copy it and assign the chosen slot
            if (took == 1) {
                InvestmentProject p = sortedProjects.get(prevI);
                InvestmentProject copy = new InvestmentProject(p);
                copy.setAssignedSlot(slot);
                selected.add(copy);
            }

            currentI = prevI;
            currentMask = prevMask;
        }

        // Sort selected by assigned slot
        Collections.sort(selected, new Comparator<InvestmentProject>() {
            @Override
            public int compare(InvestmentProject a, InvestmentProject b) {
                return Integer.compare(a.getAssignedSlot(), b.getAssignedSlot());
            }
        });
        this.selectedPortfolio = selected;

        this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
    }
}