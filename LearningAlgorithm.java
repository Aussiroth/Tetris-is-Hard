import java.util.*;
import java.io.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LearningAlgorithm
{
	public static final int POP_SIZE = 100;
	public static final int MAX_MUTATION_RATE = 100; //value for 100% chance of mutation occuring
	public static final int NUM_RUNS = 200; //number of runs to learn each time this algo is run
	public static int TOURNAMENT_SIZE = 8; //size of tournament for tournament mating algorithm
	public static int MUTATION_RATE = 10; //mutation rate out of MAX_MUTATION_RATE
	public static double MUTATION_AMOUNT = 0.2; //fraction of original range to mutate by
	public static int NUM_GEN = 50; //number of new pop introduced in each generation
	public static double REPRODUCTION_RATE = 1.0;
	public static int THREAD_NUM = 100; //maximum number of concurrent threads to run.
	public static final boolean newFile = true;
	public ArrayList<Learner> learners;


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
			if (run+totalRuns >= 3)
			{
				REPRODUCTION_RATE = 0.9;
			}
			if (run + totalRuns >= 6)
			{
				REPRODUCTION_RATE = 0.7;
				NUM_GEN = 60;
			}
			if (run + totalRuns >= 8)
			{
				REPRODUCTION_RATE = 0.5;
				NUM_GEN = 70;
			}
			executorRun();
			//singleThreadRun();
			Collections.sort(learners);
			System.out.println(run + " " + learners.get(0).fitness);
			Learner[] newGeneration = new Learner[NUM_GEN];
			//generate children through mating
			for (int k = 0; k < (int)(NUM_GEN * REPRODUCTION_RATE); k++)
			{
				newGeneration[k] = tournamentMating();
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
			if (run % 2 == 0)
				saveToFile(run+totalRuns, learners);
		}
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
		int crossoverPoint = (int)(Math.random()*Learner.NUM_WEIGHTS);
		double[] newW = new double[Learner.NUM_WEIGHTS];
		/*for (int i = 0; i < crossoverPoint; i++)
		{
			newW[i] = first.weights[i];
		}
		for (int i = crossoverPoint; i < Learner.NUM_WEIGHTS; i++)
		{
			newW[i] = second.weights[i];
		}*/
		for (int i = 0; i < Learner.NUM_WEIGHTS; i++)
		{
			int selection = (int)Math.random()*2;
			if (selection == 0)
				newW[i] = first.weights[i];
			else
				newW[i] = second.weights[i];
		}
		//perform mutation here
		mutate(newW);
		return new Learner(newW);
	}

	/*
	Takes in a Learner and mutates its weights.
	Each weight is randomly mutated with chance MUTATION_RATE/MAX_MUTATION_RATE
	*/
	public void mutate(double[] weights)
	{
		for (int i = 0; i < Learner.NUM_WEIGHTS; i++)
		{
			int mutationChance = (int)(Math.random()*MAX_MUTATION_RATE);
			if (mutationChance < MUTATION_RATE)
			{
				//randomly mutate the value by up to +/-25% of the initial range
				weights[i] += Math.random()*(Learner.MAX_WEIGHT - Learner.MIN_WEIGHT)*MUTATION_AMOUNT - 0.5*MUTATION_AMOUNT*(Learner.MAX_WEIGHT - Learner.MIN_WEIGHT);
			}
		}
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
	/*
	public void singleThreadRun()
	{
		for (int i = 0; i < POP_SIZE; i++)
		{
			learners.get(i).run();
		}
	}*/

	public void executorRun() {

		List<Future<Integer>> fitnessLevels = new ArrayList<>();

		ExecutorService exec = Executors.newFixedThreadPool(THREAD_NUM);

		for (int i = 0; i < learners.size(); i++) {
			Future<Integer> f = exec.submit(learners.get(i));
			fitnessLevels.add(f);
		}

    for (int i = 0; i < fitnessLevels.size(); i++) {
			try {
				Integer f = fitnessLevels.get(i).get();
				learners.get(i).fitness = f;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
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
	}*/

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
