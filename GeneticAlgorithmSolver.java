
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Solves the Job Sequencing problem using a heuristic Genetic Algorithm.
 * Implements Elitism, Crossover Probability, and Mutation Probability.
 */
public class GeneticAlgorithmSolver extends AbstractInvestmentSolver {

    private int populationSize;
    private int maxGenerations;
    private int elitismCount;
    private double crossoverRate;
    private double mutationRate;
    private static final double PENALTY = -5000.0;

    private Random random = new Random();

    /**
     * Default Constructor: Uses the recommended baseline settings. Fulfills the
     * "Quick Run" option so the user doesn't have to type 5 numbers every time.
     */
    public GeneticAlgorithmSolver() {
        this(100, 500, 5, 0.85, 0.05); // Calls the parameterized constructor below
    }

    /**
     * Parameterized Constructor: Allows the user to dynamically inject custom
     * settings.
     */
    public GeneticAlgorithmSolver(int populationSize, int maxGenerations,
            int elitismCount, double crossoverRate, double mutationRate) {

        // Input validation (Rubric 1.6) to prevent the user from crashing the algorithm
        if (populationSize <= elitismCount) {
            throw new IllegalArgumentException("Population must be larger than Elitism count.");
        }
        if (mutationRate < 0 || mutationRate > 1) {
            throw new IllegalArgumentException("Mutation rate must be between 0.0 and 1.0.");
        }

        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.elitismCount = elitismCount;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
    }

    @Override
    public String getAlgorithmName() {
        return "Genetic Algorithm (Approximation)";
    }

    @Override
    public void solve(List<InvestmentProject> projects) {
        long startTime = System.currentTimeMillis();

        if (projects == null || projects.isEmpty()) {
            this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
            return;
        }

        // 1. Find the maximum deadline to determine chromosome length
        int maxDeadline = 0;
        for (InvestmentProject p : projects) {
            if (p.getDeadline() > maxDeadline) {
                maxDeadline = p.getDeadline();
            }
        }

        // 2. Initialize the Population
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(new Chromosome(maxDeadline, projects));
        }

        // 3. The Evolutionary Loop
        for (int generation = 0; generation < maxGenerations; generation++) {

            // Grade the population
            for (Chromosome c : population) {
                c.calculateFitness();
            }

            // Sort by fitness descending (fittest at index 0)
            Collections.sort(population);

            List<Chromosome> nextGeneration = new ArrayList<>();

            // ---------------------------------------------------------
            // ELITISM: Safely copy the top performers to the next generation
            // ---------------------------------------------------------
            for (int i = 0; i < elitismCount; i++) {
                // We add them directly. They are shielded from crossover and mutation.
                nextGeneration.add(population.get(i));
            }

            // ---------------------------------------------------------
            // CROSSOVER & MUTATION: Fill the rest of the population
            // ---------------------------------------------------------
            int matingPoolSize = populationSize / 2; // Select parents from the top 50%

            while (nextGeneration.size() < populationSize) {
                // Select two random parents from the "good" half of the population
                Chromosome parent1 = population.get(random.nextInt(matingPoolSize));
                Chromosome parent2 = population.get(random.nextInt(matingPoolSize));

                Chromosome child;

                // Apply Crossover Probability
                if (random.nextDouble() < crossoverRate) {
                    child = crossover(parent1, parent2, maxDeadline);
                } else {
                    // If no crossover, the child is just an exact clone of parent1
                    child = parent1.cloneChromosome();
                }

                // Apply Mutation Probability to the new child
                mutate(child, projects);

                nextGeneration.add(child);
            }

            population = nextGeneration;
        }

        // 4. Final Evaluation of the very last generation
        for (Chromosome c : population) {
            c.calculateFitness();
        }
        Collections.sort(population);

        // The fittest chromosome after all generations is our answer
        Chromosome bestSolution = population.get(0);

        // 5. Translate the winning Chromosome back into the required Abstract Solver variables
        this.selectedPortfolio = new ArrayList<>();
        this.maxExpectedReturn = 0.0;

        for (int i = 0; i < bestSolution.genes.length; i++) {
            InvestmentProject proj = bestSolution.genes[i];
            // Only add valid, non-null projects that didn't violate constraints
            if (proj != null && i < proj.getDeadline() && !selectedPortfolio.contains(proj)) {
                this.selectedPortfolio.add(proj);
                this.maxExpectedReturn += proj.getProfit();
            }
        }

        this.executionTimeInMilliseconds = System.currentTimeMillis() - startTime;
    }

    // ==========================================
    // GA Helper Methods
    // ==========================================
    /**
     * Mates two parent schedules by splitting them down the middle.
     */
    private Chromosome crossover(Chromosome parent1, Chromosome parent2, int maxLength) {
        Chromosome child = new Chromosome(maxLength);
        int midpoint = maxLength / 2;

        for (int i = 0; i < maxLength; i++) {
            if (i < midpoint) {
                child.genes[i] = parent1.genes[i];
            } else {
                child.genes[i] = parent2.genes[i];
            }
        }
        return child;
    }

    /**
     * Introduces random changes to prevent the population from stagnating. Only
     * applied to non-elite children.
     */
    private void mutate(Chromosome child, List<InvestmentProject> allProjects) {
        for (int i = 0; i < child.genes.length; i++) {
            // Apply Mutation Probability to each individual time slot
            if (random.nextDouble() < mutationRate) {
                // 50% chance to wipe the slot, 50% chance to swap with a random project
                if (random.nextBoolean()) {
                    child.genes[i] = null;
                } else {
                    child.genes[i] = allProjects.get(random.nextInt(allProjects.size()));
                }
            }
        }
    }

    // ==========================================
    // Inner Class: Chromosome (The DNA)
    // ==========================================
    private class Chromosome implements Comparable<Chromosome> {

        InvestmentProject[] genes;
        double fitness;

        // Constructor for empty children (used in crossover)
        public Chromosome(int length) {
            this.genes = new InvestmentProject[length];
            this.fitness = 0.0;
        }

        // Constructor for Random Generation (Initial Population)
        public Chromosome(int length, List<InvestmentProject> allProjects) {
            this.genes = new InvestmentProject[length];
            this.fitness = 0.0;
            // Randomly fill the timeline slots
            for (int i = 0; i < length; i++) {
                if (random.nextDouble() > 0.3) { // 70% chance to put a project in a slot
                    this.genes[i] = allProjects.get(random.nextInt(allProjects.size()));
                } else {
                    this.genes[i] = null; // 30% chance to leave it empty
                }
            }
        }

        /**
         * Creates an exact, deep copy of this chromosome. Crucial for bypassing
         * crossover when the probability check fails.
         */
        public Chromosome cloneChromosome() {
            Chromosome clone = new Chromosome(this.genes.length);
            System.arraycopy(this.genes, 0, clone.genes, 0, this.genes.length);
            clone.fitness = this.fitness;
            return clone;
        }

        /**
         * Evaluates how "good" this schedule is using soft constraints
         * (Penalties).
         */
        public void calculateFitness() {
            fitness = 0.0;
            Set<String> seenIds = new HashSet<>();

            for (int i = 0; i < genes.length; i++) {
                InvestmentProject proj = genes[i];
                if (proj == null) {
                    continue;
                }

                // Penalty 1: Duplicate projects (You can't fund the same project twice)
                if (seenIds.contains(proj.getId())) {
                    fitness += PENALTY;
                    continue;
                }
                seenIds.add(proj.getId());

                // Penalty 2: Missed Deadline 
                // Array index 0 represents Slot 1. If index >= deadline, it is late.
                if (i >= proj.getDeadline()) {
                    fitness += PENALTY;
                } else {
                    // Valid placement: Reward it with the profit
                    fitness += proj.getProfit();
                }
            }
        }

        /**
         * Sorts descending so the highest fitness score is at index 0.
         */
        @Override
        public int compareTo(Chromosome other) {
            return Double.compare(other.fitness, this.fitness);
        }
    }
}
