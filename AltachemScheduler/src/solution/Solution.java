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
		
		solution.problem = problem;
		
		solution.horizon = new Day[problem.getHorizon()];
		
		//no production scheduled
		solution.orderPerMachine = new ArrayList<List<ProductionOrder>>();
		for(Machine m: problem.getMachines()) {
			solution.orderPerMachine.add(new ArrayList<ProductionOrder>());
		}
		
		//convert this initial solution to a feasible solution.
		solution.scheduleMaintenances(problem);
		
		//fix the nightshifts: == minAmountofConsecutiveNightShifts - amountOfPreviousNightShifts
		int additionalNightShiftsNeeded = problem.getMinimumConsecutiveNightShifts() - problem.getPastConsecutiveNightShifts();
		if(additionalNightShiftsNeeded > 0) {
			for(int i=0; i<additionalNightShiftsNeeded; i++) {
				solution.horizon[i].setNachtshift(true);
			}
		}
		
		return solution;
	}
	
	public void scheduleMaintenances(Problem problem) {
		
		for(int i=0; i<horizon.length; i++) {
			horizon[i] = new Day(problem.amountOfMachines(), problem.getBlocksPerDay());
		}
		
		int[] lastMaintenanceIndices = new int[problem.amountOfMachines()];
		
		for(int i=0; i<lastMaintenanceIndices.length; i++) {
			lastMaintenanceIndices[i] = -1 * problem.getMachines()[i].getDaysPastWithoutMaintenance() - 1;
		}
		
		Day.setLastMaintenanceDays(lastMaintenanceIndices);
		
		for(int i=0; i<horizon.length; i++) {
			for(int j=0; j<problem.getMachines().length; j++) {
				//moet er vandaag ten laatste gescheduled worden?
				if(i - Day.lastMaintenanceDayIndex[j] > problem.getMachines()[j].getMaxDaysWithoutMaintenance()) {
					//zo ja, schedule.
					//mogelijks dat er niet scheduled kan worden: probeer het de dag ervoor te schedulen.
					this.horizon = scheduleMaintenance(this.horizon, j, i, problem.getMachines()[j].getMaintenanceDurationInBlocks(), problem.getTechnicianStartIndex(), problem.getTechnicianStopIndex());
				}
			}
		}
	}
	
	public static Day[] scheduleMaintenance(Day[] horizon, int machineIndex, int dayIndex, int duration, int techStart, int techStop) {	
		boolean possible = true;
		
		for(int i=techStart; i<techStop+1-techStart; i++) {
			possible = true;
			//horizon[index].getJobs().length == aantalMachines
			for(int m=0; m<horizon[dayIndex].getJobs().length; m++) {
				for(int j=0; j<duration; j++) {
					Job job = horizon[dayIndex].getJobs()[m][i+j];
					if(!job.equals(new Idle())) {
						possible = false;
						break;
					}
				}
			}
			if(possible) {
				for(int j=0; j<duration; j++) {
					horizon[dayIndex].getJobs()[machineIndex][i+j] = new Maintenance();
					Day.lastMaintenanceDayIndex[machineIndex] = dayIndex;
				}
				return horizon;
			}
		}
		return scheduleMaintenance(horizon, machineIndex, dayIndex-1, duration, techStart, techStop);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Instance_name: ");
		sb.append(this.problem.getInstanceName());
		sb.append("\n");
		sb.append("Cost: ");
		sb.append(Evaluation.calculateObjectiveFunction(this));
		sb.append("\n");
		for(int i=0; i<horizon.length; i++) {
			sb.append("#Day " + i + ":\n");
			sb.append(horizon[i]);
		}
		return sb.toString();
	}
}
