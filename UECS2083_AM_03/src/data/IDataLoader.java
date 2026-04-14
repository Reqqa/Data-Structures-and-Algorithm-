package data;

import java.util.List;

import model.InvestmentProject;

/**
 * Interface defining the contract for various data ingestion methods. Enables
 * polymorphism over file reading, random generation, or future database
 * connections.
 */
public interface IDataLoader {

    /**
     * Loads and parses investment projects into a list.
     *
     * @return A List of instantiated InvestmentProject objects.
     */
    List<InvestmentProject> loadData();
}
