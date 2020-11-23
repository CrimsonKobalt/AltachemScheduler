package solution;

import java.util.ArrayList;
import java.util.List;

import model.Problem;
import model.Request;

public class Evaluation {
	private double nightshiftCost;
	private double parallelCost;
	private double overtimeBlockCost;
	private double underStockPenaltyCost;
	private double[] itemRevenue;
	
	private int[] minStock;
	
	public double calculateObjectiveFunction(Solution solution) {
		double result = 0;
		List<Request> shippedRequests = new ArrayList<>();
		
		for(Day d: solution.horizon) {
			//add night shift cost
			if(d.nachtshift) result += this.nightshiftCost;
			//add parallel cost
			if(d.parallelwerk) result += this.nightshiftCost;
			//add overtime cost
			if(d.overtime > 0) result += (d.overtime * this.overtimeBlockCost);
			//add stock cost
			for(int i=0; i<d.stock.length; i++) {
				if(d.stock[i] < minStock[i]) result += this.underStockPenaltyCost;
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
		return result;
	}
	
	public Evaluation(Problem problem) {
		this.nightshiftCost = problem.getCostOfNightShift();
		this.parallelCost = problem.getCostOfParallelDay();
		this.overtimeBlockCost = problem.getCostOfOvertimePerBlock();
		this.underStockPenaltyCost = problem.getCostPerItemUnderMinimumStock();
		
		this.itemRevenue = new double[problem.getItems().length];	
		this.minStock = new int[problem.getItems().length];
		for(int i=0; i<itemRevenue.length; i++) {
			itemRevenue[i] = problem.getItems()[i].getCostPerItem();
			minStock[i] = problem.getItems()[i].getMinAllowedInStock();
		}
	}
	
	
}
