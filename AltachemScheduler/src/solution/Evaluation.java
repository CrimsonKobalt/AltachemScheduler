package solution;

import java.util.ArrayList;
import java.util.List;

import model.Problem;
import model.Request;

public class Evaluation {
	private static double nightshiftCost;
	private static double parallelCost;
	private static double overtimeBlockCost;
	private static double underStockPenaltyCost;
	private static double[] itemRevenue;
	
	private static int[] minStock;
	
	private static double value = Double.MAX_VALUE;
	
	public static double calculateObjectiveFunction(Solution solution) {
		double result = 0;
		List<Request> shippedRequests = new ArrayList<>();
		
		for(Day d: solution.horizon) {
			//add night shift cost
			if(d.nachtshift) result += Evaluation.nightshiftCost;
			//add parallel cost
			if(d.parallelwerk) result += Evaluation.parallelCost;
			//add overtime cost
			if(d.overtime > 0) result += (d.overtime * Evaluation.overtimeBlockCost);
			//add stock cost
			for(int i=0; i<d.stock.length; i++) {
				if(d.stock[i] < minStock[i]) result += Evaluation.underStockPenaltyCost;
			}
			//add requests to shippedRequests if they have been shipped
			for(Request r : d.shippedToday) {
				shippedRequests.add(r);
			}
		}
		
		//add unshipped requests cost
		for(Request r: solution.problem.getRequests()) {
			if(!shippedRequests.contains(r)) {
				for(int itemId = 0; itemId < r.getAmountsRequested().length; itemId++) {
					result += (r.getAmountsRequested()[itemId] * itemRevenue[itemId]);
				}
			}	
		}
		
		//check for not-full nightshifts
		Evaluation.value = result;
		return result;
	}
	
	public static void configureEvaluation(Problem problem) {
		Evaluation.nightshiftCost = problem.getCostOfNightShift();
		Evaluation.parallelCost = problem.getCostOfParallelDay();
		Evaluation.overtimeBlockCost = problem.getCostOfOvertimePerBlock();
		Evaluation.underStockPenaltyCost = problem.getCostPerItemUnderMinimumStock();
		
		Evaluation.itemRevenue = new double[problem.getItems().length];	
		Evaluation.minStock = new int[problem.getItems().length];
		for(int i=0; i<itemRevenue.length; i++) {
			Evaluation.itemRevenue[i] = problem.getItems()[i].getCostPerItem();
			Evaluation.minStock[i] = problem.getItems()[i].getMinAllowedInStock();
		}
	}
	
	public static double getResult(){
		return Evaluation.value;
	}
}
