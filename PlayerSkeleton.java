import java.util.*;
import java.io.*;

public class PlayerSkeleton {

	public double[] weights;

	public PlayerSkeleton()
	{
		//initialise default weights here
		//weight[0] is row transitions
		//weight[1] is col transitions
		//weight[2] is holes
		//weight[3] is wells
		//weight[4] is max column height
		//weight[5] is the reward for clearing lines
		//default initialisations
		weights = new double[6];
		weights[0] = -7.323298208568506;
		weights[1] = -5.79715846115661;
		weights[2] = -4.194861543999046;
		weights[3] = -3.908041863765;
		weights[4] = -7.193861810393222;
		weights[5] = 8.580230809919557;
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
	
	
	//implement this function to have a working 
	//This function performs a pickmove but with 2-ply search. 
	//The idea is as described in the report.
	public int pickMoveLookahead(State s, int[][] legalMoves) 
	{
		int bestMove = 0;
		double maxSoFar = Integer.MIN_VALUE;
		for (int i = 0; i < legalMoves.length; i++)
		{
			NextState ns = new NextState(s.getField(), s.getTop(), s.getNextPiece(), 0);
			ns.makeMove(i);
			double currValue = 0;
			//Since we don't know the next piece, try each of the pieces, then average fitness across every possible piece
			for (int j = 0; j < State.N_PIECES; j++)
			{
				currValue += lookaheadMove(ns, j);
			}
			currValue/=State.N_PIECES;
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
	//This function was used to implement 2-ply search as described in the report.
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
	
	//run this to have no UI show the game being played
	public int run()
	{
		State s = new State();
		for (int i = 0;!s.hasLost(); i++)
		{
			s.makeMove(this.pickMove(s, s.legalMoves()));
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		return s.getRowsCleared();
	}
	
	//run this to have UI show the game being played
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
				Thread.sleep(0);
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
		//if is lost, then return minimum possible value
		if (ns.hasLost())
			return Integer.MIN_VALUE;
		heuristic += weights[0] * ns.getRowTransition();
		heuristic += weights[1] * ns.getColTransition();
		heuristic += weights[2] * ns.getHoles();
		heuristic += weights[3] * ns.wellFeature();
		heuristic += weights[4] * ns.getMaxColumnHeight();
		heuristic += weights[5] * ns.getRowsCleared();
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

class NextState {
	public static final int COLS = 10;
	public static final int ROWS = 21;
	public static final int N_PIECES = 7;

	public boolean lost = false;
	
	//current turn
	private int turn = 0;
	private int cleared = 0;
	
	//each square in the grid - int means empty - other values mean the turn it was placed
	private int[][] field = new int[ROWS][COLS];
	//top row+1 of each column
	//0 means empty
	private int[] top = new int[COLS];
	
	
	//number of next piece
	protected int nextPiece;
	
	//all legal moves - first index is piece type - then a list of 2-length arrays
	protected static int[][][] legalMoves = new int[N_PIECES][][];
	
	//indices for legalMoves
	public static final int ORIENT = 0;
	public static final int SLOT = 1;
	
	//possible orientations for a given piece type
	protected static int[] pOrients = {1,2,4,4,4,2,2};
	
	//the next several arrays define the piece vocabulary in detail
	//width of the pieces [piece ID][orientation]
	protected static int[][] pWidth = {
			{2},
			{1,4},
			{2,3,2,3},
			{2,3,2,3},
			{2,3,2,3},
			{3,2},
			{3,2}
	};
	//height of the pieces [piece ID][orientation]
	private static int[][] pHeight = {
			{2},
			{4,1},
			{3,2,3,2},
			{3,2,3,2},
			{3,2,3,2},
			{2,3},
			{2,3}
	};
	private static int[][][] pBottom = {
		{{0,0}},
		{{0},{0,0,0,0}},
		{{0,0},{0,1,1},{2,0},{0,0,0}},
		{{0,0},{0,0,0},{0,2},{1,1,0}},
		{{0,1},{1,0,1},{1,0},{0,0,0}},
		{{0,0,1},{1,0}},
		{{1,0,0},{0,1}}
	};
	private static int[][][] pTop = {
		{{2,2}},
		{{4},{1,1,1,1}},
		{{3,1},{2,2,2},{3,3},{1,1,2}},
		{{1,3},{2,1,1},{3,3},{2,2,2}},
		{{3,2},{2,2,2},{2,3},{1,2,1}},
		{{1,2,2},{3,2}},
		{{2,2,1},{2,3}}
	};
	
	//initialize legalMoves
	{
		//for each piece type
		for(int i = 0; i < N_PIECES; i++) {
			//figure number of legal moves
			int n = 0;
			for(int j = 0; j < pOrients[i]; j++) {
				//number of locations in this orientation
				n += COLS+1-pWidth[i][j];
			}
			//allocate space
			legalMoves[i] = new int[n][2];
			//for each orientation
			n = 0;
			for(int j = 0; j < pOrients[i]; j++) {
				//for each slot
				for(int k = 0; k < COLS+1-pWidth[i][j];k++) {
					legalMoves[i][n][ORIENT] = j;
					legalMoves[i][n][SLOT] = k;
					n++;
				}
			}
		}
	}
	
	
	public int[][] getField() {
		return field;
	}

	public int[] getTop() {
		return top;
	}

    public static int[] getpOrients() {
        return pOrients;
    }
    
    public static int[][] getpWidth() {
        return pWidth;
    }

    public static int[][] getpHeight() {
        return pHeight;
    }

    public static int[][][] getpBottom() {
        return pBottom;
    }

    public static int[][][] getpTop() {
        return pTop;
    }


	public int getNextPiece() {
		return nextPiece;
	}
	
	public boolean hasLost() {
		return lost;
	}
	
	public int getRowsCleared() {
		return cleared;
	}
	
	public int getTurnNumber() {
		return turn;
	}
	
	//constructor
	public NextState() {
	}
	
	//Populating the nextState field
	public NextState(int[][] grid, int[] oldTop, int nPiece)
	{
		for (int r = 0; r < ROWS; r++)
		{
			for (int c = 0; c < COLS; c++)
			{
				field[r][c] = grid[r][c];
			}
		}
		for (int c = 0; c < COLS; c++)
			top[c] = oldTop[c];
		nextPiece = nPiece;
	}
	
	//Constructor overload for lookahead
	public NextState(int[][] grid, int[] oldTop, int nPiece, int rCleared)
	{
		for (int r = 0; r < ROWS; r++)
		{
			for (int c = 0; c < COLS; c++)
			{
				field[r][c] = grid[r][c];
			}
		}
		for (int c = 0; c < COLS; c++)
			top[c] = oldTop[c];
		nextPiece = nPiece;
		cleared = rCleared;
	}
	
	//gives legal moves for 
	public int[][] legalMoves() {
		return legalMoves[nextPiece];
	}
	
	//gives legal moves for input piece
	public int[][] legalMoves(int n) {
		return legalMoves[n];
	}
	
	//make a move based on the move index - its order in the legalMoves list
	public void makeMove(int move) {
		makeMove(legalMoves[nextPiece][move]);
	}
	
	//make a move based on an array of orient and slot
	public void makeMove(int[] move) {
		makeMove(move[ORIENT],move[SLOT]);
	}
	
	//returns false if you lose - true otherwise
	public boolean makeMove(int orient, int slot) {
		turn++;
		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) 
		{
			try
			{
				height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
			}
			catch (ArrayIndexOutOfBoundsException oobe)
			{
				System.out.println(slot + " " + c + " " + nextPiece + " " + orient + " " + c);
			}
		}
		
		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			lost = true;
			return false;
		}

		
		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			
			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = turn;
			}
		}
		
		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}
		
		int rowsCleared = 0;
		
		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				cleared++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}

		return true;
	}
	
	public int getColumnHeight(int col)
	{
		return top[col];
	}
	
	//difference between col and col+1.
	public int getColumnHeightDiff(int col)
	{
		return Math.abs(top[col] - top[col+1]);
	}
	
	public int getMaxColumnHeight()
	{
		int maxH = 0;
		for (int i = 0; i < COLS; i++)
			maxH = Math.max(maxH, top[i]);
		return maxH;
	}
	
	public double getHoles()
	{
		
		int[] top = getTop();
		
		int numHoles = 0;
		for (int j = 0;  j < COLS;  j++) 
		{
			if (top[j] != 0) {
				for (int i = top[j] - 1;  i >= 0;  i--) 
				{
					if (field[i][j] == 0) {
						numHoles++;
					}
				}
			}
		}
		return (double) numHoles * 10;
	}
	
	//For each column, go to every row from bottom. Find the first hole and count number of blocks on this hole
	public int getBlocksOnHoles(){
	    int blocksOnHole = 0;
	    int [] topOfEachColumn = getTop();
	    for(int col = 0; col < COLS; col++)
		{
	        boolean foundHole = false;
	        for(int row = 0; row <= topOfEachColumn[col]; row++)
			{
	           if(!foundHole && field[row][col] == 0)
			   {
	               foundHole = true;
               }
               else if(foundHole && field[row][col] != 0)
			   {
	               blocksOnHole++;
               }
            }
        }
	    return blocksOnHole;
    }
	
	public double getRowTransition()
	{
		int rowTransitions = 0;
		int lastCell = 1;
		for (int i = 0;  i < ROWS;  i++) {
			for (int j = 0;  j < COLS;  j++) {
				if ((field[i][j] == 0) != (lastCell == 0)) {
					rowTransitions++;
				}
				lastCell = field[i][j];
			}
			if (lastCell == 0) rowTransitions++;
		}
		return (double) rowTransitions;
	}
	
	public double getColTransition()
	{
		int colTransitions = 0;
		for (int j = 0;  j < State.COLS;  j++) {
			for (int i = top[j] - 2;  i >= 0;  i--) {
				if ((field[i][j] == 0) != (field[i + 1][j] == 0)) {
					colTransitions++;
				}
			}
			if (field[0][j] == 0 && top[j] > 0) colTransitions++;
		}
		return (double) colTransitions;
	}
	
	public double wellFeature()
	{
		int wellSum = 0;
		for (int j = 0;  j < COLS;  j++) {
			for (int i = ROWS -1;  i >= 0;  i--) {
				if (field[i][j] == 0) {
					if (j == 0 || field[i][j - 1] != 0) {
						if (j == State.COLS - 1 || field[i][j + 1] != 0) {
							int wellHeight = i - top[j] + 1;
							wellSum += wellHeight * (wellHeight + 1) / 2;
						}
					}
				} else {
					break;
				}
			}
		}
		return (double) wellSum;
	}
	
}


