import java.util.*;
import java.io.*;

public class PlayerSkeleton {

	public double[] weights;

	public PlayerSkeleton()
	{
		//initialise default weights here
		//weights 0 is total column height
		//weights 1 is differences in adjacent column heights
		//weight 2 is maximum column height
		//weight 3 is number of holes
		//weight 4 is blocks on holes
		//weight 5 is row transitions
		//weight 6 is col transitions
		//weight 7 is reward for clearing
		//default initialisations
		try
		{
			Scanner sc = new Scanner(new File("weights.txt"));
			weights = new double[5];
			sc.nextInt();
			sc.nextInt();
			for (int i = 0; i < 5; i++)
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
			ns.makeMove(i); //Make move for each legal move
			double currValue = getHeuristic(ns);
			if (currValue > maxSoFar)
			{
				maxSoFar = currValue;
				bestMove = i;
			}
		}
		return bestMove;
	}
	
	
	//implement this function to have a working system
	public int pickMoveLookahead(State s, int[][] legalMoves) {
		int bestMove = 0;
		double maxSoFar = Integer.MIN_VALUE;
		for (int i = 0; i < legalMoves.length; i++)
		{
			NextState ns = new NextState(s.getField(), s.getTop(), s.getNextPiece(), 0);
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
	
	//This function takes in a NextState and a piece number.
	//It finds the best move to make and returns the heuristic value of the resulting state.
	public double lookaheadMove(NextState ns, int piece)
	{
		int[][] legalMoves = ns.legalMoves(piece);
		double maxSoFar = Integer.MIN_VALUE;
		for (int i = 0; i < legalMoves.length; i++)
		{
			NextState las = new NextState(ns.getField(), ns.getTop(), piece, ns.getRowsCleared());
			las.makeMove(i);
			double currValue = getHeuristic(las);
			if (currValue > maxSoFar)
			{
				maxSoFar = currValue;
			}
		}
		return maxSoFar;
	}
	
	public int run()
	{
		State s = new State();
		for (int i = 0;!s.hasLost(); i++)
		{
			s.makeMove(this.pickMove(s, s.legalMoves()));
		}
		System.out.println(s.getRowsCleared());
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
		//if is lost, then return huge negative value
		if (ns.hasLost())
			return Integer.MIN_VALUE;
		heuristic += weights[0] * ns.getRowTransition();
		heuristic += weights[1] * ns.getColTransition();
		heuristic += weights[2] * ns.getHoles();
		heuristic += weights[3] * ns.wellFeature();
		heuristic += weights[4] * ns.getRowsCleared();
		//get col height heuristic
		/*for (int i = 0; i < 10; i++)
		{
			heuristic += weights[0] * ns.getColumnHeight(i);
		}
		//get adjacent col height diff heuristic
		for (int i = 0; i < 9; i++)
		{
			heuristic += weights[1] * ns.getColumnHeightDiff(i);
		}
		//get max col height heuristic
		//heuristic += weights[2] * ns.getMaxColumnHeight();
		//get holes heuristic
		heuristic += weights[3] * ns.getHoles();
		heuristic += weights[4] * ns.getBlocksOnHoles();
		heuristic += weights[5] * ns.getRowTransition();
		heuristic += weights[6] * ns.getColTransition();
		//get cleared rows heuristic
		heuristic += weights[7] * ns.getRowsCleared();
		*/
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
