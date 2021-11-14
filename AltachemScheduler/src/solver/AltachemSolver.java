package solver;

import model.Problem;
import solution.Evaluation;
import solution.OverStockException;
import solution.ScheduleException;
import solution.Solution;

import thesis_logger.ThesisLogger;

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
		ThesisLogger.addMHparam("MAX_IDLE", MAX_IDLE);
		ThesisLogger.addMHparam("bound counter limit L_c", L);
		
		//initial solution --------------------------------------
		
		long step_start = System.currentTimeMillis();
		solution = Solution.CreateInitialSolution(problem);
		long eval_start = System.currentTimeMillis();
		try {
			solution.evaluate();
		} catch (OverStockException e1) {
			System.err.println("initial solution cannot be constructed due to stock-overflow.");
			return null;
		}
		long time_eval = System.currentTimeMillis() - eval_start;
		ThesisLogger.logEvaluation(solution.getCost(), time_eval);
		bestSolution = solution;
		carryOver = solution;
		listener.improved(bestSolution);
		ThesisLogger.logAcceptance(solution.getCost(), solution.getCost(), true, true, true);
		long step_time = System.currentTimeMillis() - step_start;
		ThesisLogger.logStepTime(step_time);
		
		// [meta] init ------------------------------------------
		
		int idle = 0;
		int count = 0;
		double bound = solution.getCost();
		
		
		//loop --------------------------------------------------
		while(true) {
			
			//double currentCost = bestSolution.getTempCost();
			step_start = System.currentTimeMillis();
			double currentCost = bestSolution.getCost();
			
			//copy the old solution
			solution = new Solution(carryOver);
			
			//move
			solution.executeRandomSwap();
			
			//compile and recalculate
			eval_start = System.currentTimeMillis();
			try {
				solution.constructSchedule();
			} catch (ScheduleException se) {
				idle++;
				time_eval = System.currentTimeMillis() - eval_start;
				ThesisLogger.logEvaluation(-1, time_eval);
				ThesisLogger.logAcceptance(bestSolution.getCost(), bound, false, false, false);
				step_time = System.currentTimeMillis() - step_start;
				ThesisLogger.logStepTime(step_time);
				continue;
			}
			
			try {
				solution.evaluate();
			} catch (OverStockException e) {
				idle++;
				time_eval = System.currentTimeMillis() - eval_start;
				ThesisLogger.logEvaluation(-1, time_eval);
				ThesisLogger.logAcceptance(bestSolution.getCost(), bound, false, false, false);
				step_time = System.currentTimeMillis() - step_start;
				ThesisLogger.logStepTime(step_time);
				continue;
			}
			time_eval = System.currentTimeMillis() - eval_start;
			
			//double newCost = solution.getTempCost();
			double newCost = solution.getCost();
			
			// [meta] accept? -----------------------------------
			boolean wasAccepted = false;
			boolean wasImproving = false;
			if(newCost <= currentCost || newCost < bound) {
				carryOver = solution;
				wasAccepted = true;
				if(solution.getCost() < bestSolution.getCost()) {
					idle = 0;
					bestSolution = solution;
					listener.improved(bestSolution);
					wasImproving = true;
				}
			} else {
				idle++;
				//no need to revert: a new solution is recompiled every time.
			}
			
			ThesisLogger.logAcceptance(bestSolution.getCost(), bound, wasAccepted, wasImproving, true);
			step_time = System.currentTimeMillis() - step_start;
			ThesisLogger.logStepTime(step_time);
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
		
		if(!ThesisLogger.printJSONFile()) {
			System.out.println("File could not be written.");
			System.exit(-1);
		}
		return bestSolution;
	}
}
