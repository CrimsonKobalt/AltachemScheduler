package solution;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
	List<ProductionOrder> orders;

	public static Solution CreateInitialSolution(Problem problem) {
		Solution solution = new Solution();

		solution.problem = problem;

		solution.horizon = new Day[problem.getHorizon()];

		//no production scheduled
		solution.orders = new ArrayList<ProductionOrder>();

		//convert this initial solution to a feasible schedule.
		solution.scheduleMaintenances(problem);

		//fix the nightshifts: == minAmountofConsecutiveNightShifts - amountOfPreviousNightShifts
		int additionalNightShiftsNeeded = problem.getMinimumConsecutiveNightShifts() - problem.getPastConsecutiveNightShifts();
		if (additionalNightShiftsNeeded > 0) {
			for (int i = 0; i < additionalNightShiftsNeeded; i++) {
				solution.horizon[i].setNachtshift(true);
			}
		}

		return solution;
	}

	public Solution() {
		//STUB
	}

	/**
	 * copy constructor to be used when creating a new solution for the algorithm (local search): creates a solution skeleton
	 * that will be modified by the local search swaps and afterwards have its schedule compiled with the constructSchedule()-method
	 * @param solution old solution
	 */
	public Solution(Solution solution) {
		//link the problem
		this.problem = solution.problem;
		//define the horizon
		this.horizon = new Day[solution.horizon.length];

		//remember the previously decided order of items to be produced (block per block)
		this.orders = new ArrayList<ProductionOrder>();
		for (ProductionOrder oldOrder : solution.orders) {
			this.orders.add(new ProductionOrder(oldOrder));
		}

		//schedule the maintenances...
		solution.scheduleMaintenances(problem);

		//remember the decisions previously made: these can now potentially be changed.
		for (int i = 0; i < this.horizon.length; i++) {
			this.horizon[i].nachtshift = solution.horizon[i].nachtshift;
			this.horizon[i].overtime = solution.horizon[i].overtime;
			this.horizon[i].parallelwerk = solution.horizon[i].parallelwerk;
		}
	}
	
	public void constructSchedule() {
		//TODO: Brecht/Bente?
	}
	
	public void swapParallelWork(int randomInt) {
		//TODO: use the timeHorizon.length to choose a day to swap
	}
	
	public void swapOvertime(int randomInt1, int randomInt2) {
		//TODO: use the timehorizon.length & randomInt1 to choose a day
		//		and randomInt2 to select how many hours of overtime to schedule (using the startOfOvertime/endOfOvertime indices)
	}
	
	public void swapNightShift(int randomInt) {
		//TODO: use randomInt to swap a certain block to/from nightshift
		//		turn off the entire nightshift if hit?
		//		start a consecutive nightshift block from this day?
		//		OR add an extra day to the hit nightshift/try to take a day away from it (can't go less than 10, if so: remove the nightshift in its entirity)
	}
	
	public void addMachineOrder(int randomInt1, int randomInt2, int randomInt3) {
		//TODO: create a random machineOrder & add it to the end
	}
	
	public void addCountToOrder(int randomInt) {
		//TODO: add 1 extra block to the selected (randomly) machineorder
	}
	
	public void removeCountOrder(int randomInt) {
		//TODO: remove 1 block from the selected (randomly) machineorder
	}
	
	public void swapOrders(int randomInt1, int randomInt2) {
		//TODO: swap two MachineOrder items from the orders-list.
	}
	
	public void changeMachineForOrder(int randomInt1, int randomInt2) {
		//TODO: select a random MachineOrder-block & swap the machine it is to be produced on.
	}
	
	public List<ProductionOrder> getProductionOrders() {
		return this.orders;
	}

	public void scheduleMaintenances(Problem problem) {

		for (int i = 0; i < horizon.length; i++) {
			horizon[i] = new Day(problem.amountOfMachines(), problem.getBlocksPerDay(), problem.getItems().length);
		}

		int[] lastMaintenanceIndices = new int[problem.amountOfMachines()];

		for (int i = 0; i < lastMaintenanceIndices.length; i++) {
			lastMaintenanceIndices[i] = -1 * problem.getMachines()[i].getDaysPastWithoutMaintenance() - 1;
		}

		Day.setLastMaintenanceDays(lastMaintenanceIndices);

		for (int i = 0; i < horizon.length; i++) {
			for (int j = 0; j < problem.getMachines().length; j++) {
				//moet er vandaag ten laatste gescheduled worden?
				if (i - Day.lastMaintenanceDayIndex[j] > problem.getMachines()[j].getMaxDaysWithoutMaintenance()) {
					//zo ja, schedule.
					//mogelijks dat er niet scheduled kan worden: probeer het de dag ervoor te schedulen.
					this.horizon = scheduleMaintenance(this.horizon, j, i, problem.getMachines()[j].getMaintenanceDurationInBlocks(), problem.getTechnicianStartIndex(), problem.getTechnicianStopIndex());
				}
			}
		}
	}

	public static Day[] scheduleMaintenance(Day[] horizon, int machineIndex, int dayIndex, int duration, int techStart, int techStop) {
		boolean possible = true;

		for (int i = techStart; i < techStop + 1 - techStart; i++) {
			possible = true;
			//horizon[index].getJobs().length == aantalMachines
			for (int m = 0; m < horizon[dayIndex].getJobs().length; m++) {
				for (int j = 0; j < duration; j++) {
					Job job = horizon[dayIndex].getJobs()[m][i + j];
					if (!job.equals(new Idle())) {
						possible = false;
						break;
					}
				}
			}
			if (possible) {
				for (int j = 0; j < duration; j++) {
					horizon[dayIndex].getJobs()[machineIndex][i + j] = new Maintenance();
					Day.lastMaintenanceDayIndex[machineIndex] = dayIndex;
				}
				return horizon;
			}
		}
		return scheduleMaintenance(horizon, machineIndex, dayIndex - 1, duration, techStart, techStop);
	}

	//calls the calculateStock() function: this recalculates the stock levels. If this is unwanted, please remove.
	private double evaluate() {
		this.calculateStock();
		Evaluation.configureEvaluation(this.problem);
		return Evaluation.calculateObjectiveFunction(this);
	}

	//can be ignored if stock is kept another way.
	private void calculateStock() {
		//get stock from history
		int[] stockHistory = new int[problem.getItems().length];
		for (int i = 0; i < stockHistory.length; i++) {
			stockHistory[i] = this.problem.getItems()[i].getQuantityInStock();
		}
		//recursive function to fix stock
		this.calculateStock(0, stockHistory);
	}

	//start this off with dayIndex == 0 and previousStockLevels as the int[] history (as received in input)
	private void calculateStock(int dayIndex, int[] previousStockLevels) {
		Day thisDay = this.horizon[dayIndex];
		//stock starts off with the same level as previous day
		for (int i = 0; i < thisDay.stock.length; i++) {
			thisDay.stock[i] = previousStockLevels[i];
		}
		//add the production of each item to the respective stock level
		for (Item item : this.problem.getItems()) {
			Production forItem = new Production(item.getItemId());
			//add all production for that item
			for (int i = 0; i < thisDay.jobs.length; i++) {
				for (int j = 0; j < thisDay.jobs[0].length; j++) {
					if (thisDay.jobs[i][j].equals(forItem)) {
						//the machine produces this many items in 1 block of production, so add that.
						thisDay.stock[item.getItemId()] += this.problem.getMachines()[i].getItemEfficiencies()[item.getItemId()];
					}
				}
			}
		}
		//subtract the shipped amounts (if any)
		for (Request r : thisDay.shippedToday) {
			for (int i = 0; i < r.getAmountsRequested().length; i++) {
				thisDay.stock[i] -= r.getAmountsRequested()[i];
				assert thisDay.stock[i] >= 0 : "stock for item " + i + " under 0";
				assert thisDay.stock[i] <= this.problem.getItems()[i].getMaxAllowedInStock() : "stock for item " + i + " over allowed level: " + thisDay.stock[i];
			}
		}
		//end
		if (dayIndex == this.horizon.length - 1) return;
		calculateStock(dayIndex + 1, thisDay.stock);
	}

	public void write(String filename) {
		try {
			FileWriter fileWriter = new FileWriter(filename);
			fileWriter.write(this.toString());
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Instance_name: ");
		sb.append(this.problem.getInstanceName());
		sb.append("\n");
		sb.append("Cost: ");
		sb.append(String.format(Locale.ENGLISH, "%.2f", this.evaluate()));
		sb.append("\n");
		for(int i=0; i<horizon.length; i++) {
			sb.append("#Day " + i + "\n");
			sb.append(horizon[i]);
		}
		return sb.toString();
	}
}
