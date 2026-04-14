package algorithm;
import java.util.List;

import model.InvestmentProject;

/**
 * Interface defining the standard behavior for all investment scheduling algorithms.
 */
public interface IInvestmentAlgorithm {
    
    /**
     * Executes the scheduling algorithm to find the optimal sequence of projects.
     * @param projects The list of available investment projects.
     */
    void solve(List<InvestmentProject> projects);
    
    /**
     * Retrieves the name of the algorithm.
     * @return The algorithm name as a String.
     */
    String getAlgorithmName();
}