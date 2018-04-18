import java.util.*;
import java.util.concurrent.Callable;

class Learner implements Comparable<Learner>, Callable<Integer>
{
	public static int NUM_WEIGHTS = 5;
	public static double MIN_WEIGHT = -8;
	public static double MAX_WEIGHT = 2;
	public static double MAX_REWARD_WEIGHT = 20;
	public static int NUM_TETRIS = 1;
	public double[] weights;
	public int fitness = -1;
	
	public Learner (double[] inWeights, int fitness)
	{
		weights = new double[NUM_WEIGHTS];
		for (int i = 0; i < NUM_WEIGHTS; i++)
			weights[i] = inWeights[i];
		this.fitness = fitness;
	}
	
	public Learner (double[] inWeights)
	{
		weights = new double[NUM_WEIGHTS];
		for (int i = 0; i < NUM_WEIGHTS; i++)
			weights[i] = inWeights[i];
	}
	
	public Learner ()
	{
		weights = new double[NUM_WEIGHTS];
		//negative weights for evaluation function , range -100 to 0
		for (int i = 0; i < NUM_WEIGHTS-1; i++)
		{
			weights[i] = Math.random()*(MAX_WEIGHT - MIN_WEIGHT) + MIN_WEIGHT;
		}
		//set positive weight for clearing lines
		weights[NUM_WEIGHTS-1] = Math.random()*MAX_REWARD_WEIGHT;
	}
	
	public Integer call()
	{
		if (fitness < 0)
		{
			fitness = 0;
			for (int i = 0; i < NUM_TETRIS; i++)
			{
				PlayerSkeleton ps = new PlayerSkeleton();
				ps.setWeights(weights);
				fitness += ps.run();
			}
			fitness = fitness/NUM_TETRIS;
		}
			return  fitness;
	}
	
	//compareTo is slightly different from usual
	//this definition is so that collections.sort sorts by descending order
	public int compareTo(Learner other)
	{
		return other.fitness - this.fitness;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(fitness);
		for (int i = 0; i < NUM_WEIGHTS; i++)
		{
			sb.append(" ");
			sb.append(weights[i]);
		}
		return sb.toString();
	}
}