
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract base class that provides shared functionality and state for all
 * specific scheduling algorithms.
 */
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
     * Shared method to print the final scheduled sequence, unselected jobs, and
     * total profit. * DATA STRUCTURE JUSTIFICATION (Rubric 1.3A): A HashSet is
     * utilized here to identify unselected projects. While List.removeAll()
     * operates in O(N^2) time by repeatedly searching the list, adding selected
     * IDs to a HashSet allows for O(1) lookups. This reduces the entire
     * unselected filtering process to O(N) time complexity.
     *
     * @param allProjects The original dataset passed into the algorithm.
     */
    public void displayResults(List<InvestmentProject> allProjects) {
        System.out.println("==================================================");
        System.out.println("Algorithm: " + getAlgorithmName());
        System.out.println("Execution Time: " + executionTimeInMilliseconds + " ms");
        System.out.println("==================================================");

        // 1. Print Selected Jobs
        System.out.println("Selected Portfolio Sequence:");
        if (selectedPortfolio.isEmpty()) {
            System.out.println("  No projects selected.");
        } else {
            for (InvestmentProject proj : selectedPortfolio) {
                System.out.println("  " + proj.toString());
            }
        }

        System.out.println("--------------------------------------------------");

        // 2. Calculate and Print Unselected Jobs using HashSet for O(1) lookups
        System.out.println("Unselected Projects:");

        // Map the IDs of selected projects for instant lookup
        Set<String> selectedIds = new HashSet<>();
        for (InvestmentProject selectedProj : selectedPortfolio) {
            selectedIds.add(selectedProj.getId());
        }

        List<InvestmentProject> unselected = new ArrayList<>();
        for (InvestmentProject proj : allProjects) {
            // O(1) check instead of O(N) check
            if (!selectedIds.contains(proj.getId())) {
                unselected.add(proj);
            }
        }

        if (unselected.isEmpty()) {
            System.out.println("  None! All projects were successfully scheduled.");
        } else {
            for (InvestmentProject proj : unselected) {
                System.out.println("  " + proj.toString());
            }
        }

        System.out.println("--------------------------------------------------");

        // 3. Print Total Profit
        System.out.printf("Total Expected Return: RM %.1f Billion\n", maxExpectedReturn);
        System.out.println("==================================================\n");
    }

    // Getters
    public double getMaxExpectedReturn() {
        return maxExpectedReturn;
    }

    public long getExecutionTime() {
        return executionTimeInMilliseconds;
    }
}
