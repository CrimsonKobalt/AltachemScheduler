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
		listener.improved(bestSolution);
		
		
		// [meta] init ------------------------------------------
		
		int idle = 0;
		int count = 0;
		double bound = solution.getTempCost();
		
		//loop --------------------------------------------------
		
		while(true) {
			
			double currentCost = bestSolution.getTempCost();
			
			//copy the old solution
			solution = new Solution(bestSolution);
			
			//move
			Random random = new Random();
			solution.addMachineOrder(random.nextInt(), random.nextInt());
			
			//compile and recalculate
			try {
				solution.constructSchedule();
			} catch (ScheduleException se) {
				idle++;
				se.printStackTrace();
			}
			
			try {
				solution.evaluateIntermediateSolution();
			} catch (OverStockException e) {
				idle++;
				e.printStackTrace();
				continue;
			}
			
			double newCost = solution.getTempCost();
			
			// [meta] accept? -----------------------------------
			if(newCost < currentCost || newCost < bound) {
				System.out.println("newCost:" + newCost);
				System.out.println("currentCost: " + currentCost);
				idle = 0;
				System.out.println("bestCost: " + bestSolution.getTempCost());
				if(solution.getTempCost() < bestSolution.getTempCost()) {
					bestSolution = solution;
					listener.improved(bestSolution);
					continue;
				}
			} else {
				idle++;
				//no need to revert: a new solution is recompiled every time.
			}
			
			// [meta] update ------------------------------------
			
			count++;
			if(count % L == 0) {
				bound = bestSolution.getTempCost();
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
