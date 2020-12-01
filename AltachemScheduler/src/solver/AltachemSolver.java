package solver;

import java.util.Random;

import model.Problem;
import solution.Evaluation;
import solution.OverStockException;
import solution.ScheduleException;
import solution.Solution;

public class AltachemSolver {
	private final AltachemListener listener;
	
	public AltachemSolver(AltachemListener listener) {
		this.listener = listener;
	}
	
	//step-counting hill-climbing.
	public Solution solve(Problem problem) {
		Solution bestSolution = null;
		Solution carryOver = null;
		Solution solution = null;
		
		Evaluation.configureEvaluation(problem);
		
		//meta settings -----------------------------------------
		
		int MAX_IDLE = 100000;
		int L = 1000;
		
		//initial solution --------------------------------------
		
		solution = Solution.CreateInitialSolution(problem);
		try {
			System.out.println("solution.evaluate(): " + solution.evaluate());
			System.out.println("solution.evaluateIntermediateSolution: " + solution.evaluateIntermediateSolution());
		} catch (OverStockException e1) {
			System.err.println("initial solution cannot be constructed due to stock-overflow.");
			return null;
		}
		bestSolution = solution;
		carryOver = solution;
		listener.improved(bestSolution);
		
		
		// [meta] init ------------------------------------------
		
		int idle = 0;
		int count = 0;
		double bound = solution.getCost();
		
		//loop --------------------------------------------------
		int maxIterations = 1000000;
		while(maxIterations >= 0) {
			
			//double currentCost = bestSolution.getTempCost();
			double currentCost = bestSolution.getCost();
			
			//bestSolution.printSchedule();
			//copy the old solution
			solution = new Solution(carryOver);
			
			//move
			solution.executeRandomSwap();
			
			//compile and recalculate
			try {
				solution.constructSchedule();
			} catch (ScheduleException se) {
				idle++;
				maxIterations--;
				continue;
			}
			
			//System.out.println("before evaluation...");
			//bestSolution.printSchedule();
			
			try {
				solution.evaluateIntermediateSolution();
				solution.evaluate();
			} catch (OverStockException e) {
				idle++;
				maxIterations--;
				continue;
			}
			
			//System.out.println("post evaluation...");
			//solution.printSchedule();
			
			//double newCost = solution.getTempCost();
			double newCost = solution.getCost();
			
			// [meta] accept? -----------------------------------
			if(newCost <= currentCost || newCost < bound) {
				idle = 0;
				carryOver = solution;
				if(solution.getTempCost() < bestSolution.getTempCost()) {
					bestSolution = solution;
					listener.improved(bestSolution);
				}
			} else {
				idle++;
				//no need to revert: a new solution is recompiled every time.
			}
			
			// [meta] update ------------------------------------
			
			count++;
			if(count % L == 0) {
				bound = carryOver.getCost();
			}
			
			//stop? ---------------------------------------------
			
			if(idle >= MAX_IDLE) {
				break;
			}
			
			maxIterations--;
		}
		
		//finished ----------------------------------------------
		
		//bestSolution.printSchedule();
		
		return bestSolution;
	}
}
