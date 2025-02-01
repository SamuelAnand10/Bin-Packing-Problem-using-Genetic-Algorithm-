package geneticAlgorithm;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

	private static ArrayList<Box> boxes = new ArrayList<Box>();
	private static int capacity;
	private static int quota;
	private static String title;
	
	private static void findSolution(){
		//Generate initial population
		Generation gen = new Generation(boxes,capacity, quota);
		Chromosome bestFit = gen.getBestFit();
		
		//Check if initial population contains a solution
		boolean termination = terminationCriteria(bestFit,0);

		//Else generate next generations till solution is found
		while(!termination){
			gen.generateNextGen();
			bestFit = gen.getBestFit();

			termination = terminationCriteria(bestFit,gen.getGenNumber());
		}
		System.out.println();
		if(bestFit.getSumValues()>=quota && bestFit.getSumWeights()<=capacity){
			System.out.println("\t\tSOLUTION FOUND!!");
		}
		else{
			System.out.println("\t\tTERMINATED SEARCH!!");
			System.out.println("BEST FIT: ");
		}
		System.out.println(bestFit);
		System.out.println("Generation Found: "+ gen.getGenNumber());
		System.out.println("*****************************************************************************************\n\n");
	}
	/*
	 * Determines whether to terminate
	 * @arg1 is chromosome which is the fittest
	 * @arg2 is the generation number
	 */
	private static boolean terminationCriteria(Chromosome bestFit, int genNumber){
		boolean termination = false;
		
		//Termination criteria where values >= quota and weights<=capacity(ideal would be capacity==weights)
		if(bestFit.getSumValues()>=quota && bestFit.getSumWeights()<=capacity)
			termination = true;
		
		else if(genNumber>=100000){
			termination = true;
		}
		else
			termination = false;
		return termination;
	}
	
	
	public static void main(String[] args) {
		int[] count = {43, 47, 48, 56, 55};
		int x = 0;
		try (Scanner file = new Scanner(new FileReader("../BPP.txt"))) {
			while (file.hasNextLine()) {
				System.out.println("*****************************************************************************************");
	
				String title = file.nextLine();
				capacity = file.nextInt();
				quota = file.nextInt();
	
				System.out.println("\t Test:" + title + "\tQuota: " + quota + "\t\t Capacity: " + capacity);
	
	
				for (int i = 0; i < count[x]; i++) {
					// Check if there's another line available
					if (file.hasNextLine()) {
						try {
							int value = file.nextInt();
							int weight = file.nextInt();
							boxes.add(new Box(weight, value));
						} catch (InputMismatchException e) {
							// Handle invalid input format
							System.err.println("Invalid input format. Skipping line. Count:" + i);
						}
					} else {
						// If no more lines available, break the loop
						break;
					}
				}
	
				if (!boxes.isEmpty()) {
					findSolution();
					boxes.clear(); // Clear the list only if it's not empty
				}
				x++;
				file.nextLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

	

}
