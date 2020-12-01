package solver;

import solution.Solution;

public class AltachemListenerImpl implements AltachemListener{
	private static double bestSolutionCost;
	private static Solution result;
	
	public AltachemListenerImpl() {
		if(result == null) {
			bestSolutionCost = Double.MAX_VALUE;
		}
	}

	@Override
	public synchronized void improved(Solution solution) {
		System.out.print("improved solution found: \n\t\tcost: ");
		System.out.println(solution.getCost());
	}
	
	public synchronized void doFinal(Solution solution) {
		System.out.print("ending at improved solution: \n\t\tcost: ");
		System.out.println(solution.getCost());
		
		if(solution.getCost() < bestSolutionCost) {
			result = solution;
			bestSolutionCost = solution.getCost();
		}
	}

	public static synchronized double getBestResult() {
		return bestSolutionCost;
	}

	public synchronized void writeBestSolution(String outputFileName) {
		if(result == null) {
			System.out.println("result is null, cannot write...");
		} else {
			result.write(outputFileName);
		}
	}

}
