package src.geneticAlgorithm;

/*Represents the box: 
 * 	ID
 * 	Weight
 * 	Value			*/

public class Box{
	
	private int weight;
	private int value;
	
	public Box(int w, int v){
		weight = w;
		value = v;
	}
	
	public int getWeight(){
		return weight;
	}
	
	public int getValue(){
		return value;
	}

}
