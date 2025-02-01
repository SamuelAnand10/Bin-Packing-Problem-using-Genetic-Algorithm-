package geneticAlgorithm;
import java.util.*;
import java.io.File;
import java.util.concurrent.*;

public class BinPackingGA {
    private static final int POPULATION_SIZE = 100;
    private static final double CROSSOVER_RATE = 0.7;
    private static final double MUTATION_RATE = 0.4;
    private static final int TOURNAMENT_SIZE = 10;
    private static final int BIN_CAPACITY = 10000;
    private static final int MAX_GENERATIONS = 100;
    private static final double ELITISM_RATE = 0.05;
    private static final int MAX_CONSECUTIVE_STAGNATION = 20;
    private static final int MIN_GENERATIONS = 200;

    // Memoization cache for fitness function
    private static Map<String, Integer> fitnessCache = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            List<Test> tests = loadTests("../BPP.txt");
            System.out.println("Total tests loaded: " + tests.size());

            for (Test test : tests) {
                System.out.println("Test Name: " + test.testName);
                List<int[]> population = initializePopulation(test.items.size());

                int generation = 0;
                int bestFitness = Integer.MAX_VALUE;
                int consecutiveStagnantGenerations = 0;
                while ((generation < MAX_GENERATIONS && consecutiveStagnantGenerations < MAX_CONSECUTIVE_STAGNATION)
                        || generation < MIN_GENERATIONS) {
                    population = evolvePopulation(population, test.items);
                    int[] bestSolution = findBestSolution(population, test.items);
                    int currentBestFitness = fitnessFunction(bestSolution, test.items);
                    if (currentBestFitness < bestFitness) {
                        bestFitness = currentBestFitness;
                        consecutiveStagnantGenerations = 0;
                    } else {
                        consecutiveStagnantGenerations++;
                    }
                    generation++;
                }

                int[] bestSolution = findBestSolution(population, test.items);
                printSolution(bestSolution, test.items);
            }
        } catch (Exception e) {
            System.out.println("Error loading tests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Test> loadTests(String filePath) throws Exception {
        List<Test> tests = new ArrayList<>();
        Scanner scanner = new Scanner(new File(filePath));

        while (scanner.hasNextLine()) {
            String testName = scanner.nextLine().trim();
            if (testName.isEmpty()) {
                throw new Exception("Test name cannot be empty.");
            }

            if (!scanner.hasNextInt()) {
                throw new Exception("Invalid file format in BPP.txt: Missing total number of items after test name '" + testName + "'");
            }
            int totalItems = scanner.nextInt();

            if (!scanner.hasNextInt()) {
                throw new Exception("Invalid file format in BPP.txt: Missing bin capacity after total number of items.");
            }
            int binCapacity = scanner.nextInt();

            List<Integer> items = new ArrayList<>();
            while (scanner.hasNextInt()) {
                int weight = scanner.nextInt();
                if (!scanner.hasNextInt()) {
                    throw new Exception("Invalid file format in BPP.txt: Missing quantity for item weight " + weight);
                }
                int quantity = scanner.nextInt();
                for (int i = 0; i < quantity; i++) {
                    items.add(weight);
                }
            }

            tests.add(new Test(testName, totalItems, binCapacity, items));
            scanner.nextLine();
        }

        scanner.close();
        return tests;
    }

    private static List<int[]> initializePopulation(int itemSize) {
        List<int[]> population = new ArrayList<>(POPULATION_SIZE);
        Random random = new Random();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            int[] chromosome = new int[itemSize];
            for (int j = 0; j < itemSize; j++) {
                chromosome[j] = random.nextInt(2);
            }
            population.add(chromosome);
        }

        return population;
    }

    private static List<int[]> evolvePopulation(List<int[]> population, List<Integer> items) {
        List<int[]> newPopulation = new ArrayList<>(POPULATION_SIZE);

        int eliteCount = (int) (POPULATION_SIZE * ELITISM_RATE);
        List<int[]> elites = selectElites(population, items, eliteCount);
        newPopulation.addAll(elites);

        while (newPopulation.size() < POPULATION_SIZE) {
            int[] parent1 = selectParent(population, items);
            int[] parent2 = selectParent(population, items);
            while (Arrays.equals(parent1, parent2)) {
                parent2 = selectParent(population, items); // Ensure distinct parents
            }
            int[] child = crossover(parent1, parent2);
            mutate(child);
            shuffleBins(child, items); // Pass items for shuffling
            newPopulation.add(child);
        }

        return newPopulation;
    }

    private static void shuffleBins(int[] chromosome, List<Integer> items) {
        Random random = new Random();
        for (int i = 0; i < chromosome.length; i++) {
            int j = random.nextInt(chromosome.length);
            int temp = chromosome[i];
            chromosome[i] = chromosome[j];
            chromosome[j] = temp;
            // Swap corresponding items
            int tempItem = items.get(i);
            items.set(i, items.get(j));
            items.set(j, tempItem);
        }
    }

    private static List<int[]> selectElites(List<int[]> population, List<Integer> items, int eliteCount) {
        List<int[]> elites = new ArrayList<>(eliteCount);
        List<int[]> sortedPopulation = new ArrayList<>(population);
        sortedPopulation.sort(Comparator.comparingInt(individual -> fitnessFunction(individual, items)));
        elites.addAll(sortedPopulation.subList(0, eliteCount));
        return elites;
    }

    private static int[] selectParent(List<int[]> population, List<Integer> items) {
        Random random = new Random();
        int index1 = random.nextInt(population.size());
        for (int i = 1; i < TOURNAMENT_SIZE; i++) {
            int index = random.nextInt(population.size());
            if (fitnessFunction(population.get(index), items) < fitnessFunction(population.get(index1), items)) {
                index1 = index;
            }
        }
        return population.get(index1);
    }

    private static int[] crossover(int[] parent1, int[] parent2) {
        Random random = new Random();
        int[] child = new int[parent1.length];

        for (int i = 0; i < parent1.length; i++) {
            if (random.nextDouble() < 0.5) {
                child[i] = parent1[i];
            } else {
                child[i] = parent2[i];
            }
        }

        return child;
    }

    private static void mutate(int[] chromosome) {
        Random random = new Random();
        for (int i = 0; i < chromosome.length; i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                chromosome[i] = 1 - chromosome[i]; // Flip the bit
            }
        }
    }

    private static int fitnessFunction(int[] chromosome, List<Integer> items) {
        // Check if fitness value is memoized
        String key = Arrays.toString(chromosome);
        if (fitnessCache.containsKey(key)) {
            return fitnessCache.get(key);
        }

        int totalWastedSpace = 0;
        int currentBinWeight = 0;

        for (int i = 0; i < chromosome.length; i++) {
            int itemWeight = items.get(i);
            if (currentBinWeight + itemWeight > BIN_CAPACITY) {
                totalWastedSpace += (BIN_CAPACITY - currentBinWeight);
                currentBinWeight = 0;
            }
            currentBinWeight += itemWeight;
        }

        if (currentBinWeight > 0) {
            totalWastedSpace += (BIN_CAPACITY - currentBinWeight);
        }

        // Penalize cases where an item cannot fit into any bin
        if (totalWastedSpace == items.size() * BIN_CAPACITY) {
            totalWastedSpace += BIN_CAPACITY;
        }

        // Memoize the fitness value
        fitnessCache.put(key, totalWastedSpace);

        return totalWastedSpace;
    }

    private static void printSolution(int[] solution, List<Integer> items) {
        System.out.println("Best Solution:");
        int currentBinWeight = 0;
        int binIndex = 0;
        List<List<Integer>> bins = new ArrayList<>();
        bins.add(new ArrayList<>());
        long[] binTimes = new long[solution.length]; // Array to store time taken for each bin
        long startTime = System.nanoTime(); // Start time

        for (int i = 0; i < solution.length; i++) {
            int itemWeight = items.get(i);
            if (currentBinWeight + itemWeight > BIN_CAPACITY) {
                long binEndTime = System.nanoTime(); // End time for current bin
                binTimes[binIndex] = binEndTime - startTime; // Store time taken for current bin
                System.out.println("Bin " + (binIndex + 1) + " Items: " + bins.get(binIndex) + " Time: " + binTimes[binIndex] + " ns");
                bins.add(new ArrayList<>());
                binIndex++;
                currentBinWeight = 0;
                startTime = System.nanoTime(); // Start time for next bin
            }
            bins.get(binIndex).add(itemWeight);
            currentBinWeight += itemWeight;
        }

        if (!bins.isEmpty()) {
            long binEndTime = System.nanoTime(); // End time for last bin
            binTimes[binIndex] = binEndTime - startTime; // Store time taken for last bin
            System.out.println("Bin " + (binIndex + 1) + " Items: " + bins.get(binIndex) + " Time: " + binTimes[binIndex] + " ns");
        }
    }

    private static int[] findBestSolution(List<int[]> population, List<Integer> items) {
        int[] best = population.get(0);
        int bestFitness = fitnessFunction(best, items);

        for (int[] individual : population) {
            int fitness = fitnessFunction(individual, items);
            if (fitness < bestFitness) {
                best = individual;
                bestFitness = fitness;
            }
        }

        return best;
    }
}


class Test {
    String testName;
    int totalItems;
    int binCapacity;
    List<Integer> items;

    public Test(String testName, int totalItems, int binCapacity, List<Integer> items) {
        this.testName = testName;
        this.totalItems = totalItems;
        this.binCapacity = binCapacity;
        this.items = items;
    }
}
