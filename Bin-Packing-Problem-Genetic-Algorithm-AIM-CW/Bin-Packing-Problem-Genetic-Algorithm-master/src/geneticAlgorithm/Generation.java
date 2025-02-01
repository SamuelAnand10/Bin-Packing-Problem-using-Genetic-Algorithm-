package geneticAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class Generation {

    private ArrayList<Box> genes;
    private HashSet<Chromosome> population = new HashSet<>();
    private ArrayList<Chromosome> arrayPop;
    private int popSize;
    private int genNumber;
    private int capacity;
    private int quota;
    private final int k = 25;
    private final int crossOverProb = 85;
    private final int mutationProb = 10;
    private Chromosome bestFit;

    public Generation(ArrayList<Box> genes, int capacity, int quota) {
        this.genes = genes;
        this.capacity = capacity;
        this.quota = quota;
        popSize = genes.size();
        arrayPop = new ArrayList<>(popSize);
        bestFit = new Chromosome(capacity, quota, genes); // Initialize bestFit to an empty chromosome
        while (popSize != population.size()) {
            Chromosome chromo = new Chromosome(capacity, quota, genes);
            if (population.add(chromo)) {
                arrayPop.add(chromo);
                if (bestFit.compareTo(chromo) == -1)
                    bestFit = chromo;
            }
        }
        genNumber = 1;
    }

    public void generateNextGen() {
        ArrayList<Chromosome> parents = new ArrayList<>();

        while (parents.size() != popSize) {
            int index = (int) (Math.random() * popSize);
            Chromosome fittestChromosome = arrayPop.get(index);
            for (int i = 0; i < k - 1; i++) {
                index = (int) (Math.random() * popSize);
                if (fittestChromosome.compareTo(arrayPop.get(index)) == -1)
                    fittestChromosome = arrayPop.get(index);
            }
            parents.add(fittestChromosome);
        }

        long seed = System.nanoTime();
        Collections.shuffle(parents, new Random(seed));

        population.clear();
        arrayPop.clear();

        for (int pIndex = 1; pIndex < parents.size() && population.size() != popSize; pIndex += 2) {
            ArrayList<Integer> pChromo1 = parents.get(pIndex - 1).getChromosome();
            ArrayList<Integer> pChromo2 = parents.get(pIndex).getChromosome();
            ArrayList<Integer> chChromo1 = new ArrayList<>(pChromo1);
            ArrayList<Integer> chChromo2 = new ArrayList<>(pChromo2);

            if (crossOverMutationRate(crossOverProb)) {
                int point = getCrossOverPoint();
                for (int i = 0; i < pChromo1.size(); i++) {
                    if (i > point) {
                        int temp = chChromo1.get(i);
                        chChromo1.set(i, chChromo2.get(i));
                        chChromo2.set(i, temp);
                    }
                }
            }

            if (crossOverMutationRate(mutationProb)) {
                int index1 = getCrossOverPoint();
                int index2 = getCrossOverPoint();
                chChromo1.set(index1, chChromo1.get(index1) == 0 ? 1 : 0);
                chChromo2.set(index2, chChromo2.get(index2) == 0 ? 1 : 0);
            }

            Chromosome child1 = new Chromosome(genes, chChromo1, capacity, quota);
            Chromosome child2 = new Chromosome(genes, chChromo2, capacity, quota);

            if (population.add(child1)) {
                arrayPop.add(child1);
            }

            if (population.add(child2)) {
                arrayPop.add(child2);
            }
        }

        setBestFit();
        genNumber++;
    }

    public Chromosome getBestFit() {
        return bestFit;
    }

    public int getGenNumber() {
        return genNumber;
    }

    private int getCrossOverPoint() {
        Random randGen = new Random();
        return randGen.nextInt(genes.size());
    }

    private boolean crossOverMutationRate(int prob) {
        Random randGen = new Random();
        return prob >= randGen.nextInt(101);
    }

    private void setBestFit() {
        for (Chromosome chromo : arrayPop) {
            if (bestFit.compareTo(chromo) == -1)
                bestFit = chromo;
        }
    }
}
