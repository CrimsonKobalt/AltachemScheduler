package runnables;

import model.Problem;
import solver.AltachemListener;
import solver.AltachemSolver;

public class MultiThreadedTimeGated {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputFilePath = args[0];
		String outputFilePath = args[1];
		
		int seed = Integer.parseInt(args[2]);
		
		int timeLimitInMinutes = Integer.parseInt(args[3]);
		
		int maxThreads = Integer.parseInt(args[4]);

	}

}

class ProblemThread implements Runnable {
	private Problem problem;
	private AltachemListener listener;
	
	public ProblemThread(Problem problem, AltachemListener listener) {
		this.problem = problem;
		this.listener = listener;
	}

	@Override
	public void run() {
		AltachemSolver solver = new AltachemSolver(listener);
		solver.solve(problem);		
	}
	
}