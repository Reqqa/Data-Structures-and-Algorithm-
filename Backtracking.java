//To be changed
import java.util.*;
public class Backtracking {

    //State 
    private List<Schedule> bestResult  = new ArrayList<>();
    private double         bestProfit  = 0;
    private int            totalJobs   = 0;

    //Initialize 
    public List<Schedule> solve(List<Schedule> jobs) {
        bestResult = new ArrayList<>();
        bestProfit = 0;
        totalJobs  = 0;

        int maxDeadline = jobs.stream()
                .mapToInt(Schedule::getDeadline).max().orElse(0);

        boolean[] slotFilled = new boolean[maxDeadline + 1];
        List<Schedule> current = new ArrayList<>();

        backtrack(jobs, 0, slotFilled, current, 0);

        // Compute totals for the best result found
        for (Schedule s : bestResult) totalJobs += s.getJobsCreated();

        // Sort by deadline for clean display
        bestResult.sort(Comparator.comparingInt(Schedule::getDeadline));
        return bestResult;
    }

    // ── Recursive backtracking ────────────────────────────────────────────────
    private void backtrack(List<Schedule> jobs, int index,
                           boolean[] slotFilled, List<Schedule> current,
                           double currentProfit) {

        // Base case — processed all jobs: check if this subset is best so far
        if (index == jobs.size()) {
            if (currentProfit > bestProfit) {
                bestProfit = currentProfit;
                bestResult = new ArrayList<>(current);
            }
            return;
        }

        Schedule job = jobs.get(index);

        // ── BRANCH 1: INCLUDE this job (if a valid slot exists) ───────────────
        int assignedSlot = findLatestFreeSlot(slotFilled, job.getDeadline());
        if (assignedSlot != -1) {
            slotFilled[assignedSlot] = true;   // mark slot
            current.add(job);                  // include job

            backtrack(jobs, index + 1, slotFilled, current,
                      currentProfit + job.getProfit());

            // Backtrack — undo the inclusion
            current.remove(current.size() - 1);
            slotFilled[assignedSlot] = false;
        }

        // ── BRANCH 2: EXCLUDE this job ────────────────────────────────────────
        backtrack(jobs, index + 1, slotFilled, current, currentProfit);
    }

    // ── Find latest free slot <= deadline (returns -1 if none) ───────────────
    private int findLatestFreeSlot(boolean[] slotFilled, int deadline) {
        for (int s = deadline; s >= 1; s--) {
            if (s < slotFilled.length && !slotFilled[s]) return s;
        }
        return -1;
    }

    // ── Result accessors ──────────────────────────────────────────────────────
    public double getTotalProfit()      { return bestProfit; }
    public int    getTotalJobsCreated() { return totalJobs; }
    public String getAlgorithmName()    {
        return "Backtracking Algorithm";
    }
}