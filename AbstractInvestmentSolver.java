
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

        // 2. Calculate and Print Unselected Jobs
        System.out.println("Unselected Projects:");

        // Because of equals/hashCode, we can pass the whole list directly into a HashSet
        Set<InvestmentProject> selectedSet = new HashSet<>(selectedPortfolio);

        List<InvestmentProject> unselected = new ArrayList<>();
        for (InvestmentProject proj : allProjects) {
            // O(1) object lookup
            if (!selectedSet.contains(proj)) {
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
        System.out.printf("Total Expected Return: RM %.1f Billion\n", maxExpectedReturn);
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
