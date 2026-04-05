
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvDataLoader implements IDataLoader {

    private String filename;

    public CsvDataLoader(String filename) {
        this.filename = filename;
    }

    @Override
    public List<InvestmentProject> loadData() {
        List<InvestmentProject> loadedProjects = new ArrayList<>();
        File file = new File(filename);

        try (Scanner fileScanner = new Scanner(file)) {
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine(); // Skip header
            }

            int lineNumber = 2;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");

                if (parts.length == 6) {
                    try {
                        String id = parts[0].trim();
                        String name = parts[1].trim();
                        String sector = parts[2].trim();
                        double profit = Double.parseDouble(parts[3].trim());
                        int jobs = Integer.parseInt(parts[4].trim());
                        int deadline = Integer.parseInt(parts[5].trim());

                        loadedProjects.add(new InvestmentProject(id, name, sector, profit, jobs, deadline));

                    } catch (IllegalArgumentException e) {
                        System.out.println("[!] Data error on line " + lineNumber + ": " + e.getMessage());
                    }
                }
                lineNumber++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("[!] Error: The file '" + filename + "' was not found.");
        }
        return loadedProjects;
    }
}
