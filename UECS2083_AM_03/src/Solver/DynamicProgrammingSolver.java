package Solver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import algorithm.*;
import model.*;
import exception.*;

/**
 * Solves the Job Sequencing problem using Dynamic Programming with bitmask-based state space.
 * 
Complexity & Memory Safety
The algorithm runs in O(n × 2^D × D) time and uses O(n × 2^D) space, 
where n is the number of projects and D is the maximum deadline.To avoid memory issues, a safe maximum deadline is calculated based on available system memory, 
with a safety margin.This removes fixed limits and allows the program to adapt to different systems
If the deadline exceeds the safe limit, the user can adjust the input, choose another algorithm, 
or continue with potential memory risk.
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
     * Computes the maximum safe deadline based on available JVM memory.
     *
     * Uses: D ≤ log₂(availableMemory / (16 + 16*n)), with a 10% safety margin.
     * Clamped to {@value #ABSOLUTE_MAX_DEADLINE}.
     *
     * @param numProjects number of projects
     * @return safe maximum deadline within memory limits
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
     * Solves job sequencing using dynamic programming with a bitmask.
     *
     * Check if the deadline is within safe memory limits. If not, the user can
     * modify input, switch algorithms, or continue at risk. Otherwise runs DP with
     * O(2^D) space and O(n × 2^D × D) time.
     *
     * @param projects list of projects (not null or empty)
     * @throws UserInputModificationException if user chooses to modify input
     * @see #computeSafeMaxDeadline(int)
     */
    @Override
    public void solve(List<InvestmentProject> projects) {
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
            
            Scanner scanner = null;
            
            try{
                scanner = new Scanner(System.in);
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
                    this.selectedPortfolio = backtracker.getSelectedPortfolio();
                    this.actualAlgorithmName = backtracker.getAlgorithmName();
                    this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
                    return;
                } else if (choice.equals("3")) {
                    System.out.println("[INFO] Proceeding with Greedy DSU Solver (approximate, fast)...");
                    System.out.println("==================================================\n");
                    GreedyDSUSolver greedy = new GreedyDSUSolver();
                    greedy.solve(projects);
                    this.maxExpectedReturn = greedy.getMaxExpectedReturn();
                    this.selectedPortfolio = greedy.getSelectedPortfolio();
                    this.actualAlgorithmName = greedy.getAlgorithmName();
                    this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
                    return;
                } else if (choice.equals("4")) {
                    System.out.println("[INFO] Proceeding with Genetic Algorithm Solver (approximate, adaptive)...");
                    System.out.println("==================================================\n");
                    GeneticAlgorithmSolver genetic = new GeneticAlgorithmSolver();
                    genetic.solve(projects);
                    this.maxExpectedReturn = genetic.getMaxExpectedReturn();
                    this.selectedPortfolio = genetic.getSelectedPortfolio();
                    this.actualAlgorithmName = genetic.getAlgorithmName();
                    this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
                    return;
                } else {
                    System.out.println("[ERROR] Invalid choice. Proceeding with Backtracking Solver by default.");
                    BacktrackingSolver backtracker = new BacktrackingSolver();
                    backtracker.solve(projects);
                    this.maxExpectedReturn = backtracker.getMaxExpectedReturn();
                    this.selectedPortfolio = backtracker.getSelectedPortfolio();
                    this.actualAlgorithmName = backtracker.getAlgorithmName();
                    this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
                    return;
                }
            } finally {
                if (scanner != null) {
                    scanner.close();
                }   
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

        // 4. DP table: use two arrays for space optimization to O(2^d)
        double[] prevRow = new double[maxMask];
        double[] currRow = new double[maxMask];
        for (int mask = 0; mask < maxMask; mask++) {
            prevRow[mask] = Double.NEGATIVE_INFINITY;
            currRow[mask] = Double.NEGATIVE_INFINITY;
        }
        prevRow[0] = 0.0;

        int[][][] prev = new int[n + 1][maxMask][4]; 

        // 5. Fill DP table using two rows for O(2^d) space
        for (int i = 0; i < n; i++) {
            InvestmentProject p = sortedProjects.get(i);
            int d = p.getDeadline();
            double profit = p.getProfit();

            for (int mask = 0; mask < maxMask; mask++) {
                currRow[mask] = Double.NEGATIVE_INFINITY;
            }

            for (int mask = 0; mask < maxMask; mask++) {
                if (prevRow[mask] == Double.NEGATIVE_INFINITY) continue;

                if (currRow[mask] < prevRow[mask]) {
                    currRow[mask] = prevRow[mask];
                    prev[i + 1][mask] = new int[]{i, mask, 0, -1};
                }

                for (int slot = d; slot >= 1; slot--) {
                    int bit = slot - 1;
                    if ((mask & (1 << bit)) == 0) {
                        int newMask = mask | (1 << bit);
                        double newProfit = prevRow[mask] + profit;
                        if (currRow[newMask] < newProfit) {
                            currRow[newMask] = newProfit;
                            prev[i + 1][newMask] = new int[]{i, mask, 1, slot};
                        }
                    }
                }
            }

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
            int[] info = prev[currentI][currentMask];
            int prevI = info[0];
            int prevMask = info[1];
            int took = info[2];
            int slot = info[3];

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
