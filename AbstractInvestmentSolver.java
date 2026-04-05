
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractInvestmentSolver implements IInvestmentAlgorithm {

    protected double maxExpectedReturn;
    protected List<InvestmentProject> selectedPortfolio;
    protected long executionTimeInMilliseconds;

    public AbstractInvestmentSolver() {
        this.maxExpectedReturn = 0.0;
        this.selectedPortfolio = new ArrayList<>();
        this.executionTimeInMilliseconds = 0;
    }

    /**
     * TEMPLATE METHOD PATTERN (Rubric 1.1B): Defines the skeleton of the
     * algorithm's output. Subclasses can inject their own specific behaviors
     * via the printAlgorithmSpecificMetrics() hook without overriding this
     * entire method.
     */
    public void displayResults(List<InvestmentProject> allProjects) {
        if (allProjects == null) {
            System.out.println("==================================================");
            System.out.println("[!] Error: Cannot display results. Provided project dataset is null.");
            System.out.println("==================================================\n");
            return; // Exit early
        }

        System.out.println("==================================================");
        System.out.println("Algorithm: " + getAlgorithmName());
        System.out.println("Execution Time: " + executionTimeInMilliseconds + " ms");

        printAlgorithmSpecificMetrics();

        System.out.println("==================================================");

        // ---------------------------------------------------------
        // EXECUTIVE SUMMARY (Suggestion 2)
        // ---------------------------------------------------------
        int maxPossibleDeadline = allProjects.stream().mapToInt(InvestmentProject::getDeadline).max().orElse(0);
        int slotsUsed = selectedPortfolio.size();
        double utilization = (maxPossibleDeadline > 0) ? ((double) slotsUsed / maxPossibleDeadline) * 100 : 0.0;

        System.out.printf("[ Executive Summary ]\n");
        System.out.printf("  - Projects Selected : %d out of %d\n", slotsUsed, allProjects.size());
        System.out.printf("  - Timeline Utilized : %.1f%% (%d/%d slots)\n", utilization, slotsUsed, maxPossibleDeadline);
        System.out.printf("  - Total Exp. Return : RM %.1f Billion\n", maxExpectedReturn);
        System.out.println("--------------------------------------------------");

        // ---------------------------------------------------------
        // SELECTED PORTFOLIO WITH HEADERS (Suggestions 1 & 3)
        // ---------------------------------------------------------
        System.out.println("Selected Portfolio Sequence:");

        if (selectedPortfolio.isEmpty()) {
            System.out.println("  No projects selected.");
        } else {
            // Print the Column Headers
            System.out.printf("  %-5s | %-32s | %-15s | %-14s | %-12s | %s\n",
                    "ID", "Project Name", "Sector", "Expected Profit", "Deadline", "Jobs Created");
            System.out.println("  " + "-".repeat(110)); // Divider line

            for (InvestmentProject proj : selectedPortfolio) {
                // If it has an assigned slot, show the timeline arrow (e.g., Slot  1 -> PRJ...)
                if (proj.getAssignedSlot() > 0) {
                    System.out.printf("Slot %2d -> %s\n", proj.getAssignedSlot(), proj.toString());
                } else {
                    System.out.println("          " + proj.toString()); // Indent for alignment if no slot
                }
            }
        }

        System.out.println("--------------------------------------------------");

        // ---------------------------------------------------------
        // UNSELECTED JOBS
        // ---------------------------------------------------------
        System.out.println("Unselected Projects:");

        // Because of equals/hashCode, we can pass the whole list directly into a HashSet
        Set<InvestmentProject> selectedSet = new HashSet<>(selectedPortfolio);
        List<InvestmentProject> unselected = new ArrayList<>();

        for (InvestmentProject proj : allProjects) {
            if (!selectedSet.contains(proj)) {
                unselected.add(proj);
            }
        }

        if (unselected.isEmpty()) {
            System.out.println("  None! All projects were successfully scheduled.");
        } else {
            // Print Headers for unselected as well
            System.out.printf("  %-5s | %-32s | %-15s | %-14s | %-12s | %s\n",
                    "ID", "Project Name", "Sector", "Expected Profit", "Deadline", "Jobs Created");
            System.out.println("  " + "-".repeat(110));

            for (InvestmentProject proj : unselected) {
                // We don't need the Slot -> arrow for unselected jobs
                System.out.println("          " + proj.toString());
            }
        }

        System.out.println("==================================================\n");
    }

    /**
     * HOOK METHOD: Subclasses can optionally override this to inject custom
     * console output (e.g., GA tuning parameters) into the displayResults
     * template.
     */
    protected void printAlgorithmSpecificMetrics() {
        // Default implementation does nothing. 
        // Exact algorithms like Backtracking can just ignore this.
    }

    // Getters
    public double getMaxExpectedReturn() {
        return maxExpectedReturn;
    }

    public long getExecutionTime() {
        return executionTimeInMilliseconds;
    }
}
