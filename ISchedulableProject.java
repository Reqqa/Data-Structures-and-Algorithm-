
/**
 * Interface abstracting the core attributes required for the Job Sequencing Problem.
 * Any class implementing this can be scheduled by our algorithms.
 */
public interface ISchedulableProject {

    String getId();

    int getDeadline();

    double getProfit();
}
