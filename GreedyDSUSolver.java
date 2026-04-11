import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Solves the Job Sequencing problem using a Greedy algorithm accelerated by
 * a Disjoint Set Union (DSU) data structure with Path Compression.
 * Time complexity: O(n log n) — dominated by sort. DSU find() runs in O(α(n))
 * amortised per call, where α is the inverse Ackermann function (~constant).
 */
public class GreedyDSUSolver extends AbstractInvestmentSolver {

    // DSU parent array (1-indexed). parent[i] = i means slot i is free.
    // Slot 0 is the sentinel — find() returning 0 means no free slot exists.
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

        // 3. Initialise DSU — every slot points to itself (all slots free)
        initParent(maxDeadline);

        // 4. Sort projects by profit descending
        // InvestmentProject.compareTo() already defines profit-descending order
        List<InvestmentProject> sortedProjects = new ArrayList<>(projects);
        Collections.sort(sortedProjects);

        // 5. Greedy assignment — DSU find() replaces the O(n) boolean array scan
        for (InvestmentProject project : sortedProjects) {

            // find() returns the latest free slot at or before this deadline.
            // Returns 0 (sentinel) if no slot is available.
            int availableSlot = find(project.getDeadline());

            if (availableSlot == 0) {
                continue; // no slot within deadline — project is unselected
            }

            // Deep copy before mutation — prevents shared-state bugs across runs
            InvestmentProject scheduled = new InvestmentProject(project);
            scheduled.setAssignedSlot(availableSlot);

            this.selectedPortfolio.add(scheduled);
            this.maxExpectedReturn += scheduled.getProfit();

            // Mark this slot as used — next find() on this slot jumps to slot-1
            parent[availableSlot] = availableSlot - 1;
        }

        // 6. Sort selected portfolio by assigned slot for clean output
        this.selectedPortfolio.sort(
                java.util.Comparator.comparingInt(InvestmentProject::getAssignedSlot));

        // 7. Record execution time
        this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
    }

    // Initialises the parent array. Each slot points to itself — meaning all
    // slots are free. Slot 0 is pre-set as sentinel by the same loop.
    private void initParent(int maxDeadline) {
        parent = new int[maxDeadline + 1];
        for (int i = 0; i <= maxDeadline; i++) {
            parent[i] = i;
        }
    }

    /**
     * Path-compressed DSU find. Returns the latest free slot reachable from x.
     * Every node along the path is pointed directly at the root (path compression),
     * achieving O(α(n)) amortised per call.
     * Union-by-rank is intentionally omitted — merges always go in one fixed
     * direction (used slot → slot below), so rank balancing has no benefit here.
     */
    private int find(int x) {
        if (parent[x] == x) {
            return x; // x is free (or sentinel 0)
        }
        // Recurse and compress: point x directly at the final root
        parent[x] = find(parent[x]);
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