import java.util.*;

class Learner implements Comparable<Learner>, Runnable
{
	public static int NUM_WEIGHTS = 8;
	public static double MIN_WEIGHT = -10;
	public static double MAX_WEIGHT = 0;
	public static double MAX_REWARD_WEIGHT = 10;
	public static int NUM_TETRIS = 1;
	public double[] weights;
	public int fitness = -1;
	public int age = 0;

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

	public void run()
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
	}

	//compareTo is slightly different from usual
	//this definition is so that collections.sort sorts by descending order
	public int compareTo(Learner other)
	{
		if (other.age == this.age) return other.fitness - this.fitness;
		return this.age - other.age;
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
