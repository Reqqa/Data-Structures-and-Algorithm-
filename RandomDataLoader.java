
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomDataLoader implements IDataLoader {

    private final int count;

    public RandomDataLoader(int count) {
        this.count = count;
    }

    @Override
    public List<InvestmentProject> loadData() {
        List<InvestmentProject> randomProjects = new ArrayList<>();
        Random rand = new Random();
        String[] sectors = {"Technology", "Real Estate", "Healthcare", "Energy", "Finance"};

        for (int i = 1; i <= count; i++) {
            String id = "PRJ" + String.format("%03d", i);
            String name = "Initiative " + (char) (rand.nextInt(26) + 'A') + rand.nextInt(100);
            String sector = sectors[rand.nextInt(sectors.length)];

            double profit = 1.0 + (49.0 * rand.nextDouble());
            profit = Math.round(profit * 10.0) / 10.0;

            int jobs = 100 + rand.nextInt(4900);
            int maxDeadline = Math.max(3, count / 2);
            int deadline = 1 + rand.nextInt(maxDeadline);

            randomProjects.add(new InvestmentProject(id, name, sector, profit, jobs, deadline));
        }
        return randomProjects;
    }
}
