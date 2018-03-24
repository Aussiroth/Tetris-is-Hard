import java.util.*;
import java.io.*;

public class PlayerSkeleton {

	public double[] weights;

	public PlayerSkeleton()
	{
		//initialise default weights here
		//weights = new double[22];
		//weights 0 to 9 are column heights
		//weights 10 to 18 are differences in adjacent column heights
		//weight 19 is maximum column heights
		//weight 20 is number of holes
		//weight 21 is reward for clearing.
		//default initialisations
		try
		{
			Scanner sc = new Scanner(new File("weights.txt"));
			weights = new double[22];
			sc.nextInt();
			sc.nextInt();
			for (int i = 0; i < 22; i++)
				weights[i] = sc.nextDouble();
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("File not found.");
			System.exit(1);
		}
	}
	
	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int bestMove = 0;
		double maxSoFar = Integer.MIN_VALUE;
		for (int i = 0; i < legalMoves.length; i++)
		{
			NextState ns = new NextState(s.getField(), s.getTop(), s.getNextPiece());
			ns.makeMove(i);
			double currValue = getHeuristic(ns);
			if (currValue > maxSoFar)
			{
				maxSoFar = currValue;
				bestMove = i;
			}
		}
		return bestMove;
	}
	
	public int run()
	{
		State s = new State();
		//max out at 5000 moves so that when the weights are very fit it will not take forever to find fitness
		for (int i = 0; i < 5000 && !s.hasLost(); i++)
		{
			s.makeMove(this.pickMove(s, s.legalMoves()));
		}
		return s.getRowsCleared();
	}
	
	public void runNormal()
	{
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
	public static void main(String[] args) {
		PlayerSkeleton ps = new PlayerSkeleton();
		System.out.println(ps.run());
		//ps.runNormal();
	}
	
	public double getHeuristic(NextState ns)
	{
		double heuristic = 0;
		//get col height heuristic
		for (int i = 0; i < 10; i++)
		{
			heuristic += weights[i] * ns.getColumnHeight(i);
		}
		//get adjacent col height diff heuristic
		for (int i = 0; i < 9; i++)
		{
			heuristic += weights[i+10] * ns.getColumnHeightDiff(i);
		}
		//get max col height heuristic
		heuristic += weights[19] * ns.getMaxColumnHeight();
		//get holes heuristic
		heuristic += weights[20] * ns.getHoles();
		//get cleared rows heuristic
		heuristic += weights[21] * ns.getRowsCleared();
		return heuristic;
	}
	
	public void printGrid(NextState ns)
	{
		int[][] grid = ns.getField();
		for (int i = grid.length-1; i >= 0; i--)
		{
			for (int j = 0; j < grid[i].length; j++)
			{
				System.out.print(grid[i][j] + " " );
			}
			System.out.println();
		}
	}
	
	public void setWeights(double[] newWeights)
	{
		for (int i = 0; i < newWeights.length; i++)
			weights[i] = newWeights[i];
	}
	
}
