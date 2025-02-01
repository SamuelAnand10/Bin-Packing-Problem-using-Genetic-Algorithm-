package geneticAlgorithm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Chromosome implements Comparable<Chromosome>{

	private ArrayList<Box> boxes;
	private ArrayList<Integer> chromosome;
	private float fitness ;
	private int capacity;
	private int quota;
	
	private int sumValues;
	private int sumWeights;
	
	public Chromosome(int capacity, int quota, ArrayList<Box> boxes){
		this.boxes = boxes;
		this.capacity = capacity;
		this.quota = quota;
		chromosome = new ArrayList<>();
		fillChromosome();
		setChromosome();
	}
	
	public Chromosome(ArrayList<Box> boxes, ArrayList<Integer> chromosome, int capacity, int quota){
		this.chromosome = chromosome;
		this.boxes = boxes;
		this.capacity = capacity;
		this.quota = quota;
		setFitness();
	}
	
	public void fillChromosome(){
		for(int i = 0; i < boxes.size(); i++){
			chromosome.add(0);
		}
	}
	
	public ArrayList<Integer> getChromosome(){
		return new ArrayList<>(chromosome);
	}
	
	private void setChromosome(){
		int n = randomNumberOfBoxes();
		for(int i = 0; i < n; i++){
			chromosome.set(i, 1);
		}
		Collections.shuffle(chromosome);
		setFitness();
	}
	
	private void setFitness() {
		// Calculate fitness based on the number of bins used
		fitness = calculateNumberOfBinsUsed();
	}
	
	private int calculateNumberOfBinsUsed() {
		int binsUsed = 0;
		int currentWeight = 0;
	
		for (int i = 0; i < chromosome.size(); i++) {
			if (chromosome.get(i) == 1) {
				if (currentWeight + boxes.get(i).getWeight() <= capacity) {
					// Add the box to the current bin
					currentWeight += boxes.get(i).getWeight();
				} else {
					// Start a new bin if the current box exceeds the capacity
					binsUsed++;
					currentWeight = boxes.get(i).getWeight();
				}
			}
		}
	
		// Increment bins used if there's any remaining weight
		if (currentWeight > 0) {
			binsUsed++;
		}
	
		// Return the inverse of binsUsed to minimize the number of bins used
		return -binsUsed;
	}
	
	private int randomNumberOfBoxes(){
		Random randGen = new Random();
		return randGen.nextInt(boxes.size() + 1);
	}
	
	public int getSumValues(){
		return sumValues;
	}
	
	public int getSumWeights(){
		return sumWeights;
	}
	
	@Override
	public int hashCode(){
		int result = 0;
		for(int i = 0; i < chromosome.size(); i++){
			if(chromosome.get(i) == 1)
				result += 1;
			}
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof Chromosome))
			return false;
		Chromosome other = (Chromosome) obj;
		if (this == other)
			return true;
		if (this.chromosome.size() != other.chromosome.size())
			return false;
		for (int i = 0; i < this.chromosome.size(); i++){
			if (this.chromosome.get(i) != other.chromosome.get(i))
				return false;
		}
		return true;
	}
	
	
	@Override
	public int compareTo(Chromosome other) {
		if (this.fitness > other.fitness)
			return 1;
		else if (this.fitness < other.fitness)
			return -1;
		else
			return 0;
	}
	
	public String toString(){
		StringBuilder chromo = new StringBuilder("--------------------------------------------------------------\n");
		chromo.append("ID \t\t Values \t\t Weights \n");
		chromo.append("--------------------------------------------------------------\n");
		sumValues = 0;
		sumWeights = 0;
		for (int i = 0; i < chromosome.size(); i++){
			if(chromosome.get(i) == 1){
				chromo.append("\t\t").append(boxes.get(i).getValue()).append("\t\t\t").append(boxes.get(i).getWeight()).append("\n");
				sumValues += boxes.get(i).getValue();
				sumWeights += boxes.get(i).getWeight();
			}
		}
		chromo.append("--------------------------------------------------------------\n");
		chromo.append("Sum:\t\t").append(sumValues).append("\t\t\t").append(sumWeights).append("\n");
		chromo.append("--------------------------------------------------------------\n");
		return chromo.toString();
	}
}
