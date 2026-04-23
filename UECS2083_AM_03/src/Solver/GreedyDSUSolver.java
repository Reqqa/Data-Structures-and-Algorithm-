package Solver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

import algorithm.AbstractInvestmentSolver;
import model.InvestmentProject;

/**
 * Greedy solution for Job Sequencing using DSU to speed up slot lookup.
 * Jobs are sorted by profit, and DSU helps find the latest free slot.
 * Time: O(n log n) due to sorting; DSU operations are near constant.
 */
public class GreedyDSUSolver extends AbstractInvestmentSolver {

    // DSU parent array (1-indexed)
    // parent[i] == i -> slot i is free
    // parent[i] < i  -> check earlier slot
    // parent[0] = 0  -> no slot available (sentinel)
    private int[] parent;

    @Override
    public String getAlgorithmName() {
        return "Greedy + Disjoint Set Union (Path Compression)";
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

        // 2. Find max deadline to size the parent array
        int maxDeadline = 0;
        for (InvestmentProject p : projects) {
            if (p.getDeadline() > maxDeadline) {
                maxDeadline = p.getDeadline();
            }
        }

        // 3. Initialise DSU: every slot points to itself (all slots are free initially)
        initParent(maxDeadline);

        // 4. Sort projects by profit (descending)
        List<InvestmentProject> sortedProjects = new ArrayList<>(projects);
        Collections.sort(sortedProjects);

        // 5. Greedy assignment: DSU find() replaces the O(n) boolean array scan
        for (InvestmentProject project : sortedProjects) {

            int availableSlot = find(project.getDeadline());

            if (availableSlot == 0) {
                continue; // no slot available for this project, skip it
            }

            // Copy project and assign slot
            InvestmentProject scheduled = new InvestmentProject(project);
            scheduled.setAssignedSlot(availableSlot);

            this.selectedPortfolio.add(scheduled);
            this.maxExpectedReturn += scheduled.getProfit();

            // Mark slot as used so next lookup moves to earlier slot
            parent[availableSlot] = availableSlot - 1;
        }

        // 6. Sort selected portfolio by assigned slot for clean output
        this.selectedPortfolio.sort(
                Comparator.comparingInt(InvestmentProject::getAssignedSlot));

        // 7. Record execution time
        this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
    }

    private void initParent(int maxDeadline) {
        parent = new int[maxDeadline + 1];
        for (int i = 0; i <= maxDeadline; i++) {
            parent[i] = i;
        }
    }

     /**
     * Finds the latest free slot for x.
     * Follows pointers to earlier slots if needed.
     * Path compression makes future lookups faster.
     */
    private int find(int x) {
        if (parent[x] == x) {
            return x; 
        }
        parent[x] = find(parent[x]); // Path compression
        return parent[x];
    }

    @Override
    protected void printAlgorithmSpecificMetrics() {
        System.out.println("DSU Configuration:");
        System.out.println("  - Slot Lookup     : find() with Path Compression");
        System.out.println("  - Union Strategy  : parent[slot] = slot - 1 (fixed direction)");
        System.out.println("  - Union by Rank   : Not applicable (merges are unidirectional)");
        System.out.println("  - Time Complexity : O(n log n) — sort dominates");
        System.out.println("  - DSU Operations  : O(n * \u03b1(n)) \u2248 O(n) effectively constant");
        System.out.println("  - Space Complexity: O(n + D) where D = max deadline");
    }
}