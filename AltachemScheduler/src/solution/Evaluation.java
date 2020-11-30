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
	private static int[] maxStock;
	
	public static double calculateObjectiveFunction(Solution solution) throws OverStockException{
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
				if(d.stock[i] > maxStock[i]) throw new OverStockException();
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
		if(solution.horizon[solution.horizon.length - 1].nachtshift) {
			int extraNightShiftsToPay = solution.problem.getMinimumConsecutiveNightShifts();
			int count = extraNightShiftsToPay;
			if(extraNightShiftsToPay >= solution.horizon.length) {
				System.out.println("Gemene input: minAantalConsecutiveNightShifts > horizon.length");
			}
			for(int i=1; i<count; i++) {
				if(solution.horizon[solution.horizon.length-i].nachtshift) {
					extraNightShiftsToPay--;
				} else {
					break;
				}
			}
			result += extraNightShiftsToPay * Evaluation.nightshiftCost;
		}
		
		//done
		return result;
	}
	
	public static void configureEvaluation(Problem problem) {
		Evaluation.nightshiftCost = problem.getCostOfNightShift();
		Evaluation.parallelCost = problem.getCostOfParallelDay();
		Evaluation.overtimeBlockCost = problem.getCostOfOvertimePerBlock();
		Evaluation.underStockPenaltyCost = problem.getCostPerItemUnderMinimumStock();
		
		Evaluation.itemRevenue = new double[problem.getItems().length];	
		Evaluation.minStock = new int[problem.getItems().length];
		Evaluation.maxStock = new int[problem.getItems().length];
		for(int i=0; i<itemRevenue.length; i++) {
			Evaluation.itemRevenue[i] = problem.getItems()[i].getCostPerItem();
			Evaluation.minStock[i] = problem.getItems()[i].getMinAllowedInStock();
			Evaluation.maxStock[i] = problem.getItems()[i].getMaxAllowedInStock();
		}
	}

	public static double calculateIntermediateObjectiveFunction(Solution solution) throws OverStockException{
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
				if(d.stock[i] > maxStock[i]) throw new OverStockException();
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
		
		//look how close we came to fullfilling the first unfullfilled order.
		Request r = null;
		for(Request req: solution.requestOrder) {
			if(!shippedRequests.contains(req)) {
				System.out.println("we get to this point!");
				r = req;
				break;
			}
		}
		if(r != null) {
			//zoek de laatst mogelijke shippingdag
			int dayIndex = 0;
			for(int i=solution.horizon.length-1; i>=0; i++) {
				if(r.isShippingDay(i)) {
					System.out.println("final shipping day found");
					dayIndex = i;
					break;
				}
			}
			System.out.println("Day " + dayIndex + ", request: " + r.getId());
			//trek van de cost af:
				//voor elk item dat nodig is voor de request te kunnen verschepen, de hoeveelheid in stock op die dag * de revenue per item voor dat item.
			for(int itemId = 0; itemId < r.getAmountsRequested().length; itemId++) {
				int amountToSubtract = 0;
				if(r.getAmountsRequested()[itemId] - solution.horizon[dayIndex].stock[itemId] > 0) {
					amountToSubtract = solution.horizon[dayIndex].stock[itemId];
				} else {
					amountToSubtract = r.getAmountsRequested()[itemId];
				}
				System.out.println("subtracting: " + amountToSubtract * Evaluation.itemRevenue[itemId]);
				result -= ( amountToSubtract * Evaluation.itemRevenue[itemId] );
			}
		}
		
		//check for not-full nightshifts
		if(solution.horizon[solution.horizon.length - 1].nachtshift) {
			int extraNightShiftsToPay = solution.problem.getMinimumConsecutiveNightShifts();
			int count = extraNightShiftsToPay;
			if(extraNightShiftsToPay >= solution.horizon.length) {
				System.out.println("minAantalConsecutiveNightShifts > horizon.length");
			}
			for(int i=1; i<count; i++) {
				if(solution.horizon[solution.horizon.length-i].nachtshift) {
					extraNightShiftsToPay--;
				} else {
					break;
				}
			}
			result += extraNightShiftsToPay * Evaluation.nightshiftCost;
		}
		
		//done
		return result;
	}
}
