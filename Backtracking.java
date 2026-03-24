import java.util.*;
public class Backtracking {
    private List<Schedule> bestResult  = new ArrayList<>();
    private double         bestProfit  = 0;
    private int            totalJobs   = 0;

    public List<Schedule> solve(List<Schedule> jobs) {
        bestResult = new ArrayList<>();
        bestProfit = 0;
        totalJobs  = 0;

        int maxDeadline = jobs.stream()
                .mapToInt(Schedule::getDeadline).max().orElse(0);

        boolean[] slotFilled = new boolean[maxDeadline + 1];
        List<Schedule> current = new ArrayList<>();

        backtrack(jobs, 0, slotFilled, current, 0);

        for (Schedule s : bestResult) totalJobs += s.getJobsCreated();


        bestResult.sort(Comparator.comparingInt(Schedule::getDeadline));
        return bestResult;
    }
    private void backtrack(List<Schedule> jobs, int index,
                           boolean[] slotFilled, List<Schedule> current,
                           double currentProfit) {

        if (index == jobs.size()) {
            if (currentProfit > bestProfit) {
                bestProfit = currentProfit;
                bestResult = new ArrayList<>(current);
            }
            return;
        }

        Schedule job = jobs.get(index);


        int assignedSlot = findLatestFreeSlot(slotFilled, job.getDeadline());
        if (assignedSlot != -1) {
            slotFilled[assignedSlot] = true;  
            current.add(job);                

            backtrack(jobs, index + 1, slotFilled, current,
                      currentProfit + job.getProfit());


            current.remove(current.size() - 1);
            slotFilled[assignedSlot] = false;
        }


        backtrack(jobs, index + 1, slotFilled, current, currentProfit);
    }

    private int findLatestFreeSlot(boolean[] slotFilled, int deadline) {
        for (int s = deadline; s >= 1; s--) {
            if (s < slotFilled.length && !slotFilled[s]) return s;
        }
        return -1;
    }

    public double getTotalProfit()      { return bestProfit; }
    public int    getTotalJobsCreated() { return totalJobs; }
    public String getAlgorithmName()    {
        return "Backtracking Algorithm";
    }
}