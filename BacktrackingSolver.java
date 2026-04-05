
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Solves the Job Sequencing problem using a Backtracking algorithm. Explores
 * all valid combinations of projects to guarantee the absolute maximum return.
 */
public class BacktrackingSolver extends AbstractInvestmentSolver {

    @Override
    public String getAlgorithmName() {
        return "Backtracking Algorithm";
    }

    @Override
    public void solve(List<InvestmentProject> projects) {
        long startTime = System.currentTimeMillis();

        // 1. Reset state in case the solver is run multiple times
        this.maxExpectedReturn = 0.0;
        this.selectedPortfolio = new ArrayList<>();

        if (projects == null || projects.isEmpty()) {
            this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
            return;
        }

        // 2. Find the maximum deadline to size our time slot tracker
        int maxDeadline = 0;
        double totalAvailableProfit = 0.0;
        for (InvestmentProject project : projects) {
            if (project.getDeadline() > maxDeadline) {
                maxDeadline = project.getDeadline();
            }
            totalAvailableProfit += project.getProfit();
        }

        // Array to track which time slots are currently occupied (1-indexed for simplicity)
        boolean[] timeSlots = new boolean[maxDeadline + 1];
        List<InvestmentProject> currentSelection = new ArrayList<>();

        // 3. Optimization: Sort the projects by profit descending. 
        // This heuristic helps the backtracking algorithm find highly profitable paths early.
        /* Collections.sort() is preferred over a PriorityQueue (Max-Heap) for this algorithm.
         * 1. Access Pattern: Backtracking and DP require frequent, indexed access (O(1)) 
         * to iterate through the dataset via projects.get(i).
         * 2. PriorityQueue Limitation: While a PriorityQueue is great for dynamically polling 
         * the max value in O(log N), it does not allow O(1) random access. Polling every 
         * item out of a queue to build a sorted array still takes O(N log N).
         * 3. Efficiency: Collections.sort() sorts the array in-place in O(N log N) using Timsort,
         * giving us a fully sorted structure with O(1) index lookups, perfectly fitting 
         * the needs of our search tree algorithms.
         */
        List<InvestmentProject> sortedProjects = new ArrayList<>(projects);
        Collections.sort(sortedProjects);

        // 4. Begin the recursive search
        findOptimalSchedule(sortedProjects, 0, currentSelection, 0.0, timeSlots, totalAvailableProfit);

        // Sort the final chosen portfolio chronologically by deadline for clean output
        this.selectedPortfolio.sort(java.util.Comparator.comparingInt(InvestmentProject::getAssignedSlot));

        this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
    }

    /**
     * Recursive helper method that explores the decision tree.
     */
    private void findOptimalSchedule(List<InvestmentProject> projects, int currentIndex,
            List<InvestmentProject> currentSelection,
            double currentProfit, boolean[] timeSlots, double remainingProfit) {

        // If the best-case scenario for this branch cannot beat our current best, kill the branch.
        if (currentProfit + remainingProfit <= this.maxExpectedReturn) {
            return;
        }

        // Base Case: We have made a decision (include or exclude) for every project
        if (currentIndex == projects.size()) {
            // If this branch yielded a higher profit than our previous best, save it!
            if (currentProfit > this.maxExpectedReturn) {
                this.maxExpectedReturn = currentProfit;
                // Deep copy the current selection so it doesn't get overwritten as we backtrack
                this.selectedPortfolio = new ArrayList<>(currentSelection);
            }
            return;
        }

        InvestmentProject currentProject = projects.get(currentIndex);

        // ==========================================
        // BRANCH 1: EXCLUDE the current project
        // ==========================================
        double nextRemainingProfit = remainingProfit - currentProject.getProfit();
        findOptimalSchedule(projects, currentIndex + 1, currentSelection, currentProfit, timeSlots, nextRemainingProfit);

        // ==========================================
        // BRANCH 2: INCLUDE the current project
        // ==========================================
        int deadline = currentProject.getDeadline();
        int slotFound = -1;

        // Ensure we don't try to access an index larger than our timeSlots array
        int maxSlotToSearch = Math.min(deadline, timeSlots.length - 1);

        // Find the latest available slot on or before the deadline (Greedy slotting)
        for (int i = maxSlotToSearch; i > 0; i--) {
            if (!timeSlots[i]) {
                slotFound = i;
                break;
            }
        }

        // If a valid slot exists, we can proceed down the "Include" branch
        if (slotFound != -1) {
            // Apply the choice
            timeSlots[slotFound] = true;
            currentProject.setAssignedSlot(slotFound);
            currentSelection.add(currentProject);

            // Recurse deeper into the tree with this project included
            findOptimalSchedule(projects, currentIndex + 1, currentSelection,
                    currentProfit + currentProject.getProfit(), timeSlots, nextRemainingProfit);

            // BACKTRACK: Undo the choice so other branches can explore this slot/state
            timeSlots[slotFound] = false;
            currentSelection.remove(currentSelection.size() - 1);
            // Now your comment is 100% accurate because the local currentProfit was never reassigned!
        }
    }
}
