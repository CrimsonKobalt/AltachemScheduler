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
			solution.evaluate();
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
		while(true) {
			
			//double currentCost = bestSolution.getTempCost();
			double currentCost = bestSolution.getCost();
			
			//copy the old solution
			solution = new Solution(carryOver);
			
			//move
			solution.executeRandomSwap();
			
			//compile and recalculate
			try {
				solution.constructSchedule();
			} catch (ScheduleException se) {
				idle++;
				continue;
			}
			
			try {
				solution.evaluate();
			} catch (OverStockException e) {
				idle++;
				continue;
			}
			
			//double newCost = solution.getTempCost();
			double newCost = solution.getCost();
			
			// [meta] accept? -----------------------------------
			if(newCost <= currentCost || newCost < bound) {
				carryOver = solution;
				if(solution.getCost() < bestSolution.getCost()) {
					idle = 0;
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
		}
		
		//finished ----------------------------------------------
		
		return bestSolution;
	}
}
