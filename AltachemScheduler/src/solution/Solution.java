package solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Item;
import model.Machine;
import model.Problem;
import model.Request;

public class Solution {
	Day[] horizon;
	
	//identiek aan het probleem: dit is gewoon info: hier dient niet aan verandert te worden.
	//deze worden letterlijk gekopieerd.
	Problem problem;
	
	//lijst met volgorde van items die we produceren + hoe veel blokken we dit doen
	//orderPerMachine.get(i) is de volgorde voor machine i;
	List<List<ProductionOrder>> orderPerMachine;
	
	public static Solution CreateInitialSolution(Problem problem) {
		Solution solution = new Solution();
		
		solution.horizon = new Day[problem.getHorizon()];
		
		//no production scheduled
		solution.orderPerMachine = new ArrayList<List<ProductionOrder>>();
		for(Machine m: problem.getMachines()) {
			solution.orderPerMachine.add(new ArrayList<ProductionOrder>());
		}
		
		//convert this initial solution to a feasible solution.
		solution.scheduleMaintenances(problem);
		
		return solution;
	}
	
	public void scheduleMaintenances(Problem problem) {
		
		for(int i=0; i<horizon.length; i++) {
			horizon[i] = new Day(problem.amountOfMachines(), problem.getBlocksPerDay());
		}
		
		int[] lastMaintenanceIndices = new int[problem.amountOfMachines()];
		
		for(int i=0; i<lastMaintenanceIndices.length; i++) {
			lastMaintenanceIndices[i] = -1 * problem.getMachines()[i].getDaysPastWithoutMaintenance();
		}
		
		System.out.println(Arrays.toString(lastMaintenanceIndices));
	}
}
