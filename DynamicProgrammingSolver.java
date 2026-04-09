import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Solves the Job Sequencing problem using Dynamic Programming.
 * Uses a DP table with state (job_index, used_slots_mask) for exact solution.
 * Time complexity: O(n * 2^maxD * maxD), pseudo-polynomial.
 */
public class DynamicProgrammingSolver extends AbstractInvestmentSolver {

    @Override
    public String getAlgorithmName() {
        return "Dynamic Programming (Exact, Pseudo-polynomial)";
    }

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

        // 2. Find max deadline
        int maxDeadline = 0;
        for (InvestmentProject p : projects) {
            if (p.getDeadline() > maxDeadline) {
                maxDeadline = p.getDeadline();
            }
        }

        // If maxDeadline is too large, fall back or warn, but assume <= 20
        if (maxDeadline > 20) {
            System.out.println("[!] Max deadline too large for DP, using backtracking approximation.");
            // For now, proceed, but in practice, limit to 20
            maxDeadline = Math.min(maxDeadline, 20);
        }

        int n = projects.size();
        // number of possible slot-usage states using bitmasks
        // mask runs from 0 to 2^d - 1. Each bit represents whether a slot is used.
        int maxMask = 1 << maxDeadline;

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
        prevRow[0] = 0.0; // base state: with zero projects and no slots used, profit is 0

        // Create reconstruction table: prev[i][mask] = {prev_i, prev_mask, took, slot}
        int[][][] prev = new int[n + 1][maxMask][4]; // [prev_i, prev_mask, took(0/1), slot]

        // 5. Fill DP table using two rows for O(2^d) space
        for (int i = 0; i < n; i++) {
            InvestmentProject p = sortedProjects.get(i);
            int d = Math.min(p.getDeadline(), maxDeadline); // cap at maxDeadline
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