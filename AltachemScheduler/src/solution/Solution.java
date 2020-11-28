package solution;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	
	//calculated cost
	private double cost;

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
		
		solution.cost = Double.MAX_VALUE;

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
		
		this.cost = Double.MAX_VALUE;
	}
	
	public void constructSchedule() {
		//TODO: Brecht/Bente?
		//even kort over nagedacht al: let op dat (dezelfde) changeovers nooit 2x per dag mogen plaatsvinden, let op dat 1 changeover (als die meerdere blokken inneemt)
		//niet gespreid mag liggen over meerdere dagen.
		//het enige dat tussen 2 blokken van dezelfde changeover mag liggen is het volgende: een maintenance/idle-block.
		
		List<Changeover> allChangeoversOnDay = new ArrayList<>(); //om te kijken of changeover al geweest is op deze dag
		int index = 0;
		int blocksProduced = 0;		
		ProductionOrder currentOrder = orders.get(index);
		int nextProduce = currentOrder.getItemId();
		int lastProduced = problem.getMachines()[currentOrder.getMachineId()].getLastItemIdProduced();
		int currentDay = 0;
		int currentBlock = 0;
		while(currentDay<horizon.length) {
			if(!horizon[currentDay].parallelwerk) { //GEEN PARALLELWERK
				
				//Zoek blocks zonder production om changeover te plannen (maintenance mag er wel tussenzitten)
				if(problem.getMachines()[currentOrder.getMachineId()].getLastItemIdProduced() != currentOrder.getItemId()) {				
					
					Changeover co = new Changeover(lastProduced, nextProduce);
					for(Changeover c : allChangeoversOnDay) {
						if(c.isSame(co)) { //Als changeover al geweest is, kies nieuwe order
							index++;
							currentOrder = orders.get(index);
							nextProduce = currentOrder.getItemId();
						}
					}
					int coDuration = problem.getChangeoverDurations()[lastProduced][nextProduce];
					boolean isLargeCo = problem.getIsLargeChangeover()[lastProduced][nextProduce];
					
					if(!isLargeCo) { 
						int consecutiveBlocks = 0; //wordt terug op nul gezet als er een production gevonden wordt
						boolean bothIdle = true;
						int startBlock = currentBlock;	
						List<Integer> maintenanceBlocks = new ArrayList<>(); //bijhouden om achteraf de changoverblocks in te vullen
						while(consecutiveBlocks<coDuration) {
							for(int m=0;m<problem.getMachines().length;m++) {
								if(horizon[currentDay].jobs[m][currentBlock].equals(new Idle())) {				
									
									bothIdle =true;
								}else if(new Production(1).isProduction(horizon[currentDay].jobs[m][currentBlock])) { //maakt niet uit welk item
									consecutiveBlocks = 0;
									startBlock = currentBlock +1;
									bothIdle = false;
								}else {
									bothIdle = false;
									maintenanceBlocks.add(currentBlock);
									
								}
							}
							if(bothIdle) {
								consecutiveBlocks++;
								
							}
								
							currentBlock++;
							//Changeover moet in dezelfde dag voltooid worden
							if(currentBlock >= (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
									&& !horizon[currentDay].isNachtshift()) {
								currentBlock = 0;
								currentDay++;
								startBlock = 0; 
								consecutiveBlocks = 0;
								maintenanceBlocks.clear();
								allChangeoversOnDay.clear();
							}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
								currentBlock = 0;
								currentDay++;
								startBlock = 0;
								consecutiveBlocks = 0;
								maintenanceBlocks.clear();
								allChangeoversOnDay.clear();
							}

						}
						//blocks zijn gevonden -> toekennen
						for(int b = startBlock; b < startBlock+coDuration+maintenanceBlocks.size(); b++) {
							if(maintenanceBlocks.contains(b)) {
								//Do nothing
							}else {
								horizon[currentDay].jobs[currentOrder.getMachineId()][b] = co;
								allChangeoversOnDay.add(co);
							}
							allChangeoversOnDay.add(co);
						}
					}else { //isLargeChangeover
				
						int consecutiveBlocks = 0; //wordt terug op nul gezet als er een production gevonden wordt
						boolean bothIdle = true;
						int startBlock = currentBlock;	
						List<Integer> maintenanceBlocks = new ArrayList<>(); //bijhouden om achteraf de changoverblocks in te vullen
						while(consecutiveBlocks<coDuration) {
							if(currentBlock>=problem.getTechnicianStartIndex() && currentBlock<=problem.getTechnicianStopIndex()) {
								for(int m=0;m<problem.getMachines().length;m++) {
									if(horizon[currentDay].jobs[m][currentBlock].equals(new Idle())) {		
										bothIdle =true;
									}else if(new Production(1).isProduction(horizon[currentDay].jobs[m][currentBlock])) { //maakt niet uit welk item
										consecutiveBlocks = 0;
										startBlock = currentBlock +1;
										bothIdle = false;
									}else {
										bothIdle = false;
										maintenanceBlocks.add(currentBlock);
										
									}
								}
								if(bothIdle) {
									consecutiveBlocks++;
									
								}
							}						
							currentBlock++;
							//Changeover moet in dezelfde dag voltooid worden
							if(currentBlock >= (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
									&& !horizon[currentDay].isNachtshift()) {
								currentBlock = 0;
								currentDay++;
								startBlock = 0; 
								consecutiveBlocks = 0;
								maintenanceBlocks.clear();
								allChangeoversOnDay.clear();
							}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
								currentBlock = 0;
								currentDay++;
								startBlock = 0;
								consecutiveBlocks = 0;
								maintenanceBlocks.clear();
								allChangeoversOnDay.clear();
							}

						}
						//blocks zijn gevonden -> toekennen
						for(int b = startBlock; b < startBlock+coDuration+maintenanceBlocks.size(); b++) {
							if(maintenanceBlocks.contains(b)) {
								//Do nothing
							}else {
								horizon[currentDay].jobs[currentOrder.getMachineId()][b] = co;
								allChangeoversOnDay.add(co);
							}
							allChangeoversOnDay.add(co);
						}
					}
					
				}
				//start producing
				while(blocksProduced < currentOrder.getAmountOfBlocks()) {
					boolean allIdle = true;
					for(int m=0; m<problem.getMachines().length; m++) {
						if(horizon[currentDay].jobs[m][currentBlock].equals(new Idle())) {
							allIdle = false;
						}
					}if(allIdle) {
						horizon[currentDay].jobs[currentOrder.getMachineId()][currentBlock] = new Production(currentOrder.getItemId());
						blocksProduced++;
						currentBlock++;
						if(currentBlock >= (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
								&& !horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
						}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
						}
					}else {
						currentBlock++;
						if(currentBlock >= (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
								&& !horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
						}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
					}
				}
				//end producing
				index++;
				blocksProduced = 0;
				currentOrder = orders.get(index);
				lastProduced = nextProduce;
				nextProduce = currentOrder.getItemId();
			}
			
		}else {
			//WEL PARALLELWERK
			//Zoek blocks zonder production om changeover te plannen (maintenance mag er wel tussenzitten)
			if(problem.getMachines()[currentOrder.getMachineId()].getLastItemIdProduced() != currentOrder.getItemId()) {				
				
				int currentMachineId = currentOrder.getMachineId();
				Changeover co = new Changeover(lastProduced, nextProduce);
				for(Changeover c : allChangeoversOnDay) {
					if(c.isSame(co)) { //Als changeover al geweest is, kies nieuwe order
						index++;
						currentOrder = orders.get(index);
						nextProduce = currentOrder.getItemId();
					}
				}
				int coDuration = problem.getChangeoverDurations()[lastProduced][nextProduce];
				boolean isLargeCo = problem.getIsLargeChangeover()[lastProduced][nextProduce];
				
				if(!isLargeCo) { 
					int consecutiveBlocks = 0; //wordt terug op nul gezet als er een production gevonden wordt
					int startBlock = currentBlock;	
					List<Integer> maintenanceBlocks = new ArrayList<>(); //bijhouden om achteraf de changoverblocks in te vullen
					
					while(consecutiveBlocks<coDuration) {
						if(horizon[currentDay].jobs[currentMachineId][currentBlock].equals(new Idle())) {
							consecutiveBlocks++;
						}else if(new Production(1).isProduction(horizon[currentDay].jobs[currentMachineId][currentBlock])) {
							consecutiveBlocks= 0;
							startBlock = currentBlock + 1;
						}else {
							maintenanceBlocks.add(currentBlock);
						}
						currentBlock++;
						if(currentBlock >= (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
								&& !horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
							maintenanceBlocks.clear();
						}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
							maintenanceBlocks.clear();
						}
					}
					//blocks zijn gevonden -> toekennen
					for(int b = startBlock; b < startBlock+coDuration+maintenanceBlocks.size(); b++) {
						if(maintenanceBlocks.contains(b)) {
							//Do nothing
						}else {
							horizon[currentDay].jobs[currentOrder.getMachineId()][b] = co;
							allChangeoversOnDay.add(co);
						}
						allChangeoversOnDay.add(co);
					}
				}else {
					//Need technician
					int consecutiveBlocks = 0; //wordt terug op nul gezet als er een production gevonden wordt
					int startBlock = currentBlock;	
					List<Integer> maintenanceBlocks = new ArrayList<>(); //bijhouden om achteraf de changoverblocks in te vullen
					
					while(consecutiveBlocks<coDuration) {
						if(currentBlock>=problem.getTechnicianStartIndex() && currentBlock<=problem.getTechnicianStopIndex()) {
							if(horizon[currentDay].jobs[currentMachineId][currentBlock].equals(new Idle())) {
								consecutiveBlocks++;
							}else if(new Production(1).isProduction(horizon[currentDay].jobs[currentMachineId][currentBlock])) {
								consecutiveBlocks= 0;
								startBlock = currentBlock + 1;
							}else {
								maintenanceBlocks.add(currentBlock);
							}
						}						
						currentBlock++;
						if(currentBlock >= (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
								&& !horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
							maintenanceBlocks.clear();
						}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
							maintenanceBlocks.clear();
						}
					}
					//blocks zijn gevonden -> toekennen
					for(int b = startBlock; b < startBlock+coDuration+maintenanceBlocks.size(); b++) {
						if(maintenanceBlocks.contains(b)) {
							//Do nothing
						}else {
							horizon[currentDay].jobs[currentOrder.getMachineId()][b] = co;
							allChangeoversOnDay.add(co);
						}
						allChangeoversOnDay.add(co);
					}
				}
				//Start producing
				while(blocksProduced < currentOrder.getAmountOfBlocks()) {
					if(horizon[currentDay].jobs[currentMachineId][currentBlock].equals(new Idle())) {						
						horizon[currentDay].jobs[currentOrder.getMachineId()][currentBlock] = new Production(currentOrder.getItemId());
						blocksProduced++;
						currentBlock++;
						if(currentBlock >= (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
								&& !horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
						}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
						}
					}else {
						currentBlock++;
						if(currentBlock >= (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
								&& !horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
						}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							allChangeoversOnDay.clear();
						}
					}
				}
				//end producing
				index++;
				blocksProduced = 0;
				currentOrder = orders.get(index);
				lastProduced = nextProduce;
				nextProduce = currentOrder.getItemId();
			}
		}
	}
	}
	
	//START SWAPS
	public void executeRandomSwap() {
		//TODO: Bente?
		
	}
	public void swapParallelWork(int randomInt) {
		this.horizon[randomInt % this.horizon.length].setParallelwerk(!this.horizon[randomInt % this.horizon.length].parallelwerk);
	}
	
	public void swapOvertime(int randomInt1, int randomInt2) {
		//TODO: use the timehorizon.length & randomInt1 to choose a day
		//		and randomInt2 to select how many hours of overtime to schedule (using the startOfOvertime/endOfOvertime indices)
		int maxUrenOvertime = this.problem.getLastOvertimeIndex() - this.problem.getLastDayShiftIndex();
		this.horizon[randomInt1 % this.horizon.length].setOvertime(randomInt2 % maxUrenOvertime);
	}
	
	public void swapNightShift(int randomInt) {
		//TODO Bente kies maar iets, maar zorg ervoor dat alle mogelijkheden nog kunnen optreden he.
		//TODO: use randomInt to swap a certain block to/from nightshift
		//		turn off the entire nightshift if hit?
		//		start a consecutive nightshift block from this day?
		//		OR add an extra day to the hit nightshift/try to take a day away from it (can't go less than 10, if so: remove the nightshift in its entirity)
		//		nightshifts mogen optreden aan het einde van de periode ook & moeten hiet niet per se hun volledige periode uitzitten.
					//hoe brengen we dit in rekening?
	}
	
	public void addMachineOrder(int randomInt1, int randomInt2) {
		//create a random machineOrder with size == 1 & add it to the end.
		//TODO: is het beter om hier direct meerdere blokken toe te voegen? hmmm...
		ProductionOrder po = new ProductionOrder(randomInt1 % this.problem.getItems().length, randomInt2 % this.problem.amountOfMachines(), 1);
		this.orders.add(po);
	}
	
	public void addCountToOrder(int randomInt) {
		//TODO: Bente: wat als er geen productionorders zijn? -> vervangen door add...?
		this.orders.get(randomInt % this.orders.size()).incrementAmountOfBlocks();
	}
	
	public void removeCountOrder(int randomInt) {
		//TODO: Bente: zelfde als hierboven
		this.orders.get(randomInt % this.orders.size()).decrementAmountOfBlocks();
	}
	
	public void swapOrders(int randomInt1, int randomInt2) {
		//TODO: Bente: zelfde als hierboven
		if(randomInt1 == randomInt2) randomInt2++;
		Collections.swap(this.orders, randomInt1 % this.orders.size(), randomInt2 % this.orders.size());
	}
	
	public void changeMachineForOrder(int randomInt1, int randomInt2) {
		//TODO: Bente: zelfde als hierboven
		this.orders.get(randomInt1 % this.orders.size()).setMachineId(randomInt2 % this.problem.amountOfMachines()); 
	}
	
	public void changeItemForOrder(int randomInt1, int randomInt2) {
		//TODO: Bente?
		this.orders.get(randomInt1 % this.orders.size()).setItemId(randomInt2 % this.problem.getItems().length); 
	}
	
	//TODO: mss in de lijst van requests 2 van plaats wisselen?
	public void swapRequestOrder(int randomInt1, int randomInt2) {
		//TODO: Bente?
	}
	//END SWAPS
	
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
	public double evaluate() {
		this.calculateStock();
		Evaluation.configureEvaluation(this.problem);
		this.cost = Evaluation.calculateObjectiveFunction(this);
		return this.cost;
	}
	
	public double getCost() {
		if(this.cost == Double.MAX_VALUE) {
			System.out.println("encoutered situation where cost has not been calculated...");
		} 
		return this.cost;
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
