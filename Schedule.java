public class Schedule implements Comparable<Schedule> {

    //Class File
    private String id;
    private String projectName;
    private String sector;
    private double profit;       // Investment_RM_Billion
    private int    jobsCreated;  // Jobs_Created
    private int    deadline;     // Deadline_Slot

    //Setters 
    public Schedule(String id, String projectName, String sector,
                    double profit, int jobsCreated, int deadline) {
        this.id          = id;
        this.projectName = projectName;
        this.sector      = sector;
        this.profit      = profit;
        this.jobsCreated = jobsCreated;
        this.deadline    = deadline;
    }

    // Getters
    public String getId()          { return id; }
    public String getProjectName() { return projectName; }
    public String getSector()      { return sector; }
    public double getProfit()      { return profit; }
    public int    getJobsCreated() { return jobsCreated; }
    public int    getDeadline()    { return deadline; }


    @Override
    public int compareTo(Schedule other) {
        return Double.compare(other.profit, this.profit);
    }

    @Override
    public String toString() {
        return String.format("%-5s | %-32s | %-15s | RM%6.1f bil | Slot %d | Jobs: %,d",
                id, projectName, sector, profit, deadline, jobsCreated);
    }
}