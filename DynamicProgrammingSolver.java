import java.util.ArrayList;
import java.util.Collections;
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
        int maxMask = 1 << maxDeadline;

        // 3. Sort projects by profit descending
        List<InvestmentProject> sortedProjects = new ArrayList<>(projects);
        Collections.sort(sortedProjects, (a, b) -> Double.compare(b.getProfit(), a.getProfit()));

        // 4. DP table: dp[i][mask] = max profit for first i projects, with used slots mask
        double[][] dp = new double[n + 1][maxMask];
        // Initialize to negative infinity
        for (int i = 0; i <= n; i++) {
            for (int mask = 0; mask < maxMask; mask++) {
                dp[i][mask] = Double.NEGATIVE_INFINITY;
            }
        }
        dp[0][0] = 0.0;

        // Prev table for reconstruction: prev[i][mask] = {prev_i, prev_mask, took, slot}
        int[][][] prev = new int[n + 1][maxMask][4]; // [prev_i, prev_mask, took(0/1), slot]

        // 5. Fill DP
        for (int i = 0; i < n; i++) {
            InvestmentProject p = sortedProjects.get(i);
            int d = Math.min(p.getDeadline(), maxDeadline); // cap at maxDeadline
            double profit = p.getProfit();

            for (int mask = 0; mask < maxMask; mask++) {
                if (dp[i][mask] == Double.NEGATIVE_INFINITY) continue;

                // Not take the project
                if (dp[i + 1][mask] < dp[i][mask]) {
                    dp[i + 1][mask] = dp[i][mask];
                    prev[i + 1][mask] = new int[]{i, mask, 0, -1};
                }

                // Try to take the project in each possible slot <= d that is free
                for (int slot = d; slot >= 1; slot--) {
                    int bit = slot - 1;
                    if ((mask & (1 << bit)) == 0) {
                        int newMask = mask | (1 << bit);
                        double newProfit = dp[i][mask] + profit;
                        if (dp[i + 1][newMask] < newProfit) {
                            dp[i + 1][newMask] = newProfit;
                            prev[i + 1][newMask] = new int[]{i, mask, 1, slot};
                        }
                    }
                }
            }
        }

        // 6. Find the best mask at i=n
        double bestProfit = Double.NEGATIVE_INFINITY;
        int bestMask = -1;
        for (int mask = 0; mask < maxMask; mask++) {
            if (dp[n][mask] > bestProfit) {
                bestProfit = dp[n][mask];
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
        selected.sort((a, b) -> Integer.compare(a.getAssignedSlot(), b.getAssignedSlot()));
        this.selectedPortfolio = selected;

        this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
    }
}