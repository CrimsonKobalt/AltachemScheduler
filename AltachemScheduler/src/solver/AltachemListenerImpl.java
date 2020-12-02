package solver;

import solution.Solution;

public class AltachemListenerImpl implements AltachemListener{
	private static double bestSolutionCost;
	private static Solution nextBestSolution;
	private static Solution result;
	
	public AltachemListenerImpl() {
		if(result == null) {
			bestSolutionCost = Double.MAX_VALUE;
		}
	}

	@Override
	public synchronized void improved(Solution solution) {
		if(solution.getCost() < bestSolutionCost) {
			System.out.print("improved solution found: \n\t\tcost: ");
			System.out.println(solution.getCost());
			result = solution;
			nextBestSolution = result;
			bestSolutionCost = solution.getCost();
		}
	}

	public static synchronized double getBestResult() {
		return bestSolutionCost;
	}
	
	public static synchronized Solution getNextBestSolution() {
		return nextBestSolution;
	}
	
	public static synchronized Solution getBestSolution() {
		return result;
	}

	public synchronized void writeBestSolution(String outputFileName) {
		if(result == null) {
			System.out.println("result is null, cannot write...");
		} else {
			result.write(outputFileName);
		}
	}

}
