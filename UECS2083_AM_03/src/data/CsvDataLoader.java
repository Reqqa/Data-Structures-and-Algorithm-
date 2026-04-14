package data;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import model.InvestmentProject;


public class CsvDataLoader implements IDataLoader {

    private final String filename;

    public CsvDataLoader(String filename) {
        this.filename = filename;
    }

    @Override
    public List<InvestmentProject> loadData() {
        List<InvestmentProject> loadedProjects = new ArrayList<>();

        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            System.out.println("[!] Error: '" + filename + "' is not a valid CSV file.");
            System.out.println("[!] Please ensure the filename ends with '.csv'");
            return loadedProjects;
        }

        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
         Scanner fileScanner = (is != null) ? new Scanner(is) : null) {

            if (is == null) {
                System.out.println("[!] Error: The file '" + filename + "' was not found on the classpath.");
                return loadedProjects;
            }

            // SUGGESTION 2: Validate Header
            if (fileScanner.hasNextLine()) {
                String header = fileScanner.nextLine().toLowerCase();
                // Simple keyword check to ensure it's the right type of file
                if (!header.contains("job_id") || !header.contains("investment_rm_billion") || !header.contains("deadline_slot")) {
                    System.out.println("[!] Warning: The CSV header does not match the expected schema.");
                    System.out.println("[!] Expected columns related to ID, Profit, and Deadline.");
                    System.out.println("[!] Attempting to parse data anyway...\n");
                }
            }

            int lineNumber = 2;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                // Skip empty lines to prevent false positive errors
                if (line.trim().isEmpty()) {
                    lineNumber++;
                    continue;
                }

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
                        System.out.println("[!] Data format error on line " + lineNumber + ": " + e.getMessage());
                    }
                } else {
                    // SUGGESTION 3: Explicitly warn about column count mismatches
                    System.out.println("[!] Structural error on line " + lineNumber
                            + ": Expected 6 columns, but found " + parts.length + ".");
                }
                lineNumber++;
            }
        } catch (Exception e) {
            System.out.println("[!] Error: The file '" + filename + "' was not found.");
        }
        return loadedProjects;
    }
}
