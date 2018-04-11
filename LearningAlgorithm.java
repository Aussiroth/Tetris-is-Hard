import java.util.*;
import java.io.*;

public class LearningAlgorithm
{
	public static final int POP_SIZE = 100;
	public static final int NUM_RUNS = 200; //number of runs to learn each time this algo is run
	public static double REPRODUCTION_RATE = 1;
	public static int THREAD_NUM = 20; //maximum number of concurrent threads to run.
	public static final boolean newFile = true;
	public ArrayList<Learner> learners;
	public static double CROSSOVER_RATE = 0.75;
	public static double MUTATION_RATE = 0.001;
	public static final int NUM_GEN = 99; //determines extent of elitism
	public static final int TOURNAMENT_SIZE = 50;

	public LearningAlgorithm ()
	{
		learners = new ArrayList<Learner>();
	}

	public void run () throws IOException
	{
		int totalRuns = 0;
		if (!newFile)
		{
			//read data from file
			Scanner sc = new Scanner(new File("weights.txt"));
			totalRuns = sc.nextInt();
			for (int i = 1; i <= POP_SIZE; i++)
			{
				double[] readWeights = new double[Learner.NUM_WEIGHTS];
				int fitness = sc.nextInt();
				for (int j = 0; j < Learner.NUM_WEIGHTS; j++)
					readWeights[j] = sc.nextDouble();
				learners.add(new Learner(readWeights, fitness));
			}
		}
		else
		{
			//start over with new random population
			for (int i = 0; i < POP_SIZE; i++)
				learners.add(new Learner());
		}
		for (int run = 0; run < NUM_RUNS; run++)
		{
			singleThreadRun();
			Collections.sort(learners);
			System.out.println(run + " " + learners.get(0).fitness);
			/*
			for (Learner l : learners) {
				l.age++;
			}*/
			
			if (run == 0) saveToFile(run+totalRuns, learners);

			Learner[] newGeneration = new Learner[NUM_GEN];
			//generate children through mating
			for (int k = 0; k < (int)(NUM_GEN * REPRODUCTION_RATE); k++)
			{
				newGeneration[k] = reproduce(roulette(), roulette());
			}
			//generate immigrants
			for (int k = (int)(NUM_GEN * REPRODUCTION_RATE); k < NUM_GEN; k++)
			{
				newGeneration[k] = new Learner();
			}
			//kill off last NUM_GEN of the old generation, replace with the new generation
			int i = POP_SIZE-NUM_GEN;
			for (int j = 0; j < newGeneration.length; j++)
			{
				learners.set(i, newGeneration[j]);
				i++;
			}
			//save data to file every 2 runs
			saveToFile(run+totalRuns, learners);
		}
	}

	public Learner roulette() {
		int currTotalFitness = 0;
		for (int i = 0; i < learners.size(); i++) {
			currTotalFitness += learners.get(i).fitness;
		}

		double[] learnersD = new double[learners.size()];
		for (int i = 0; i < learners.size(); i++) {
			learnersD[i] = learners.get(i).fitness / currTotalFitness;
		}

		double randomPoint = Math.random();

		double currPoint = 0;
		int i = 0;
		while (i < learners.size() - 1 && currPoint <= randomPoint) {
			currPoint += learnersD[i];
			i++;
		}

		return learners.get(i);
	}

	public Learner tournamentMating()
	{
		ArrayList<Integer> fittestTwo = tournament();
		return reproduce(learners.get(fittestTwo.get(0)), learners.get(fittestTwo.get(1)));
	}

	//returns 2 integers - the position of the 2 winners of the tournament
	public ArrayList<Integer> tournament()
	{
		ArrayList<Integer> tList = new ArrayList<Integer>();
		//randomly select TOURNAMENT_SIZE people
		for (int i = 0; i < TOURNAMENT_SIZE; i++)
		{
			tList.add((int)(Math.random()*POP_SIZE));
		}
		Collections.sort(tList);
		//since Learners is sorted by fittest, the 2 smallest integers picked are the 2 fittest
		ArrayList<Integer> result = new ArrayList<Integer>();
		result.add(tList.get(0));
		result.add(tList.get(1));
		return result;
	}

	public Learner consecutiveMating(int k)
	{
		Learner firstParent = learners.get(k*2);
		Learner secondParent = learners.get(k*2+1);
		return weightedReproduce(firstParent, secondParent);
	}

	/*
	Takes in 2 Learners.
	Returns a new Learner with weights equal to the average of each weight of the parent
	*/
	public Learner weightedReproduce(Learner first, Learner second)
	{
		double[] newW = new double[Learner.NUM_WEIGHTS];
		//child takes half from first parent, half from second
		for (int i = 0; i < Learner.NUM_WEIGHTS; i++)
		{
			double currW = first.weights[i] + second.weights[i];
			currW = currW/2.0;
			newW[i] = currW;
		}
		mutate(newW);
		return new Learner(newW);
	}

	/*
	Takes in 2 learners.
	Picks a random point in the weights. The new Learner has weights equal to the first parent up until the cutoff point.
	Then the weights equal to the second parent afterwards.
	*/
	public Learner reproduce(Learner first, Learner second)
	{
		double crossoverChance = Math.random();
		int crossoverPoint = 0;
		if (crossoverChance < CROSSOVER_RATE) crossoverPoint = (int)(Math.random()*Learner.NUM_WEIGHTS);
		if (first.fitness > second.fitness) crossoverPoint = Learner.NUM_WEIGHTS;

		double[] newW = new double[Learner.NUM_WEIGHTS];
		for (int i = 0; i < crossoverPoint; i++)
		{
			newW[i] = first.weights[i];
		}
		for (int i = crossoverPoint; i < Learner.NUM_WEIGHTS; i++)
		{
			newW[i] = second.weights[i];
		}
		mutate(newW);
		return new Learner(newW);
	}

	/*
	Takes in a Learner and mutates its weights.
	Each weight is randomly mutated with chance MUTATION_RATE/MAX_MUTATION_RATE
	*/
	public void mutate(double[] weights)
	{
		for (int i = 0; i < Learner.NUM_WEIGHTS - 1; i++)
		{
			double mutationChance = Math.random();
			if (mutationChance < MUTATION_RATE)
			{
				weights[i] = Math.random()*(Learner.MAX_WEIGHT - Learner.MIN_WEIGHT) + Learner.MIN_WEIGHT;
			}
		}
		weights[Learner.NUM_WEIGHTS - 1] = Math.random() * (Learner.MAX_REWARD_WEIGHT);
	}

	public void saveToFile(int runs, ArrayList<Learner> learners) throws FileNotFoundException
	{
		PrintWriter out = new PrintWriter("weights.txt");
		out.println(runs);
		for (int j = 0; j < POP_SIZE; j++)
		{
			out.println(learners.get(j).toString());
		}
		out.close();
	}

	public void singleThreadRun()
	{
		for (int i = 0; i < POP_SIZE; i++)
		{
			learners.get(i).run();
		}
	}

	public void multiThreadRun()
	{
		Thread[] threads = new Thread[THREAD_NUM];
		int i = 0;
		while (i < POP_SIZE)
		{
			for (int j = 0; j < THREAD_NUM; j++)
			{
				threads[j] = new Thread(learners.get(i));
				threads[j].start();
				i++;
			}
			for (int j = 0; j < THREAD_NUM; j++)
			{
				try
				{
					threads[j].join();
				}
				catch (InterruptedException ie)
				{
					System.out.println("Exception when joining thread. " + ie.getMessage());
				}
			}
		}
	}

	public static void main(String[] args)
	{
		LearningAlgorithm la = new LearningAlgorithm();
		try
		{
			la.run();
		}
		catch (IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
	}
}
