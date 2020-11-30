package solution;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
	
	List<Request> requestOrder;
	
	//calculated cost
	private double cost;
	//calculated cost -- intermediate
	private double tempCost;

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
		solution.tempCost = Double.MAX_VALUE;
		
		solution.requestOrder = new ArrayList<>();
		for(int i=0; i<problem.getRequests().length; i++) {
			solution.requestOrder.add(problem.getRequests()[i]);
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
		for(int i=0; i<this.horizon.length; i++) {
			this.horizon[i] = new Day(problem.amountOfMachines(), problem.getBlocksPerDay(), problem.getItems().length);
		}

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
		
		this.requestOrder = new ArrayList<>();
		for(Request req : solution.requestOrder) {
			this.requestOrder.add(req);
		}
		
		this.cost = Double.MAX_VALUE;
		this.tempCost = Double.MAX_VALUE;
	}
	
	public void constructSchedule() throws ScheduleException {
		//TODO: 1) construct schedule
		//TODO: 2) gebruik die calculateStock() functie
		//TODO: 3) overloop de requests in requestOrder en probeer ze zo vroeg mogelijk te shippen.
		//(geen rekening houden met te veel stock constraint: dit wordt in Evaluation gedaan)
		
		
		//TODO: Brecht/Bente?
		//even kort over nagedacht al: let op dat (dezelfde) changeovers nooit 2x per dag mogen plaatsvinden, let op dat 1 changeover (als die meerdere blokken inneemt)
		//niet gespreid mag liggen over meerdere dagen.
		//het enige dat tussen 2 blokken van dezelfde changeover mag liggen is het volgende: een maintenance/idle-block.
		
		List<Integer[]> currentData = new ArrayList<>(); //data per machine
		for(int m=0;m<problem.getMachines().length;m++) {
			Integer[] machineDate = new Integer[3];
			machineDate[0] = 0; //current day
			machineDate[1] = 0; //current block
			machineDate[2] = problem.getMachines()[m].getLastItemIdProduced(); //last item produced
			currentData.add(machineDate);
		}
		for(ProductionOrder po : orders) {
			int currentMachine = po.getMachineId();
			//System.out.println("Machine: " + currentMachine);
			int currentBlock = currentData.get(currentMachine)[1];
			int currentDay = currentData.get(currentMachine)[0];
			int lastProduced = currentData.get(currentMachine)[2];
			int coDuration = problem.getChangeoverDurations()[lastProduced][po.getItemId()];
			
			
			Changeover co = new Changeover(lastProduced, po.getItemId());
			searchDayWithoutSameChangeover(co, currentDay, currentBlock);
			
			int consecutiveBlocks = 0; //wordt terug op nul gezet als er een production gevonden wordt
			int startBlock = currentBlock;	
			List<Integer> maintenanceBlocks = new ArrayList<>(); //bijhouden om achteraf de changoverblocks in te vullen
			
			boolean isLargeCo = problem.getIsLargeChangeover()[lastProduced][po.getItemId()];
			if(!isLargeCo) { 				
				
				while(consecutiveBlocks<coDuration) {
					
					if(horizon[currentDay].parallelwerk || currentBlock>problem.getLastDayShiftIndex()) { 
						//PARALLELWERK
						if(new Idle().equals(horizon[currentDay].jobs[currentMachine][currentBlock])) {
							consecutiveBlocks++;
						}else if(new Production(1).isProduction(horizon[currentDay].jobs[currentMachine][currentBlock])) {
							consecutiveBlocks= 0;
							startBlock = currentBlock + 1;
						}else if(new Changeover(1, 0).isChangeover(horizon[currentDay].jobs[currentMachine][currentBlock])){
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
							searchDayWithoutSameChangeover(co, currentDay, currentBlock);
							startBlock = 0; 
							consecutiveBlocks = 0;
							if(currentDay>=horizon.length) {
								throw new ScheduleException();
							}
							maintenanceBlocks.clear();
						}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							startBlock = 0; 
							consecutiveBlocks = 0;
							if(currentDay>=horizon.length) {
								throw new ScheduleException();
							}
							searchDayWithoutSameChangeover(co, currentDay, currentBlock);
							maintenanceBlocks.clear();
						}
					}else { // GEEN PARALLELWERK
						boolean bothIdle = false;
						for(int m=0;m<problem.getMachines().length;m++) {
							if(new Idle().equals(horizon[currentDay].jobs[m][currentBlock])) {				
								
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
							if(currentDay>=horizon.length) {
								throw new ScheduleException();
							}
							searchDayWithoutSameChangeover(co, currentDay, currentBlock);
							startBlock = 0; 
							consecutiveBlocks = 0;
							maintenanceBlocks.clear();
						}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							if(currentDay>=horizon.length) {
								throw new ScheduleException();
							}
							searchDayWithoutSameChangeover(co, currentDay, currentBlock);
							startBlock = 0;
							consecutiveBlocks = 0;
							maintenanceBlocks.clear();
						}
					}
				}
				
					
				}else{
					// Large Changeover
					
					while(consecutiveBlocks<coDuration) {
						if(currentBlock>=problem.getTechnicianStartIndex() && currentBlock<=problem.getTechnicianStopIndex()) {
							
							if(horizon[currentDay].parallelwerk || currentBlock>problem.getLastDayShiftIndex()) { 
								//PARALLELWERK
								if(new Idle().equals(horizon[currentDay].jobs[currentMachine][currentBlock])) {
									consecutiveBlocks++;
								}else if(new Production(1).isProduction(horizon[currentDay].jobs[currentMachine][currentBlock])) {
									consecutiveBlocks= 0;
									startBlock = currentBlock + 1;
								}else {
									maintenanceBlocks.add(currentBlock);
								}
								
							}else { // GEEN PARALLELWERK
								boolean bothIdle = false;
								for(int m=0;m<problem.getMachines().length;m++) {
									if(new Idle().equals(horizon[currentDay].jobs[m][currentBlock])) {				
										
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
						}
						
								
						currentBlock++;
						//Changeover moet in dezelfde dag voltooid worden
						if(currentBlock >= (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
								&& !horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							searchDayWithoutSameChangeover(co, currentDay, currentBlock);
							if(currentDay>=horizon.length) {
								throw new ScheduleException();
							}
							startBlock = 0; 
							consecutiveBlocks = 0;
							maintenanceBlocks.clear();
						}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
							currentBlock = 0;
							currentDay++;
							searchDayWithoutSameChangeover(co, currentDay, currentBlock);
							if(currentDay>=horizon.length) {
								throw new ScheduleException();
							}
							startBlock = 0;
							consecutiveBlocks = 0;
							maintenanceBlocks.clear();
						}
					}
				}	
					//changeoverBlocks toekennen
				for(int b = startBlock; b < startBlock+coDuration+maintenanceBlocks.size(); b++) {
					if(maintenanceBlocks.contains(b)) {
						//Do nothing
					}else {
						horizon[currentDay].setJob(co, po.getMachineId(), b);
						System.out.println(horizon[currentDay].jobs[po.getMachineId()][b]);
					}					
				}					
				//start producing
				int blocksProduced = 0;
				while(blocksProduced<po.getAmountOfBlocks()) {
					System.out.println("in while producing");
					if(horizon[currentDay].parallelwerk || currentBlock>problem.getLastDayShiftIndex()) { //PARALLELWERK
						if(new Idle().equals(horizon[currentDay].jobs[currentMachine][currentBlock])) {
							blocksProduced++;
							horizon[currentDay].setJob(new Production(po.getItemId()), currentMachine, currentBlock);
							System.out.println(horizon[currentDay].jobs[currentMachine][currentBlock]);
						}else {
							//Do nothing
						}
					}else { //GEEN PARALLELWERK
						boolean bothIdle = true;
						for(int m=0;m<problem.getMachines().length;m++) {
							if(!new Idle().equals(horizon[currentDay].jobs[currentMachine][currentBlock])) {
								bothIdle = false;
							}
						}
						if(bothIdle) {
							blocksProduced++;
							horizon[currentDay].setJob(new Production(po.getItemId()), currentMachine, currentBlock);
							System.out.println(horizon[currentDay].jobs[currentMachine][currentBlock]);
						}else {
							//Do nothing
						}
					}
					currentBlock++;
					if(currentBlock > (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
							&& !horizon[currentDay].isNachtshift()) {
						currentBlock = 0;
						currentDay++;
						if(currentDay>=horizon.length) {
							throw new ScheduleException();
						}
					}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
						currentBlock = 0;
						currentDay++;
						if(currentDay>=horizon.length) {
							throw new ScheduleException();
						}
					}
				}
				currentData.get(currentMachine)[1] = currentBlock;
				currentData.get(currentMachine)[0] = currentDay;
				currentData.get(currentMachine)[2] = po.getItemId();
				
			}//End of orders
			
		
			calculateStock();
		
			//handleRquests()
			List<Request> requests = new ArrayList<>();
			int day = 0;
			for(Request request : requests) {				
				boolean shipped = false;
				while(!shipped) {
					for(int d=day;d<horizon.length;d++) {
						if(request.isShippingDay(d)) {
							boolean enoughStock = true;
							for(int i=0;i<request.getAmountsRequested().length;i++) {
								if(request.getAmountsRequested()[i]>horizon[day].stock[i]) {
									enoughStock = false;
								}
							}
							if(enoughStock) {
								
								for(int n=day;n<horizon.length;n++) {
									for(int i=0;i<request.getAmountsRequested().length;i++) {
										horizon[n].stock[i] -= request.getAmountsRequested()[i];
									}
								}
								shipped=true;
								horizon[day].shippedToday.add(request);
								break;
							}
						}
					}
				}
			}
			
	}
	
	//START SWAPS
	public void executeRandomSwap() {
		Random random = new Random();
		int randomInt = random.nextInt(9);
		int randPosInt = random.nextInt();
		randPosInt = Math.abs(randPosInt);
		int randPosInt2 = random.nextInt();
		randPosInt2 = Math.abs(randPosInt2);
		//System.out.println(randPosInt + "\t" + randPosInt2);
		//TODO: allemaal voorkomend met zelfde kans of kansen aanpassen (vb minder proberen voor night shift?)

		switch (randomInt){
			case 0: swapParallelWork(randPosInt); break;
			case 1: swapNightShift(randPosInt, random.nextBoolean(), random.nextBoolean()); break;
			case 2: swapOvertime(randPosInt, randPosInt2); break;
			case 3: swapOrders(randPosInt, randPosInt2); break;
			case 4: swapRequestOrder(randPosInt, randPosInt2); break;
			case 5: incrementOrderCount(randPosInt); break;
			case 6: decrementOrderCount(randPosInt); break;
			case 7: changeMachineForOrders(randPosInt, randPosInt2); break;
			case 8: changeItemForOrder(randPosInt, randPosInt2); break;
			default: break;
		}
		
	}
	public void swapParallelWork(int randomInt) {
		System.out.println("swapParallelWork");
		this.horizon[randomInt % this.horizon.length].setParallelwerk(!this.horizon[randomInt % this.horizon.length].parallelwerk);
	}

	public void swapOvertime(int randomInt1, int randomInt2) {
		System.out.println("swapOvertime");
		//TODO: zorg dat die niet kan als we op een nachtshift vallen
		// ---> geef in de plaats een nachtshiftswap op die dag door ofzo.
		//use the timehorizon.length & randomInt1 to choose a day
		//use randomInt2 to select how many hours of overtime to schedule (using the startOfOvertime/endOfOvertime indices)
		int maxUrenOvertime = this.problem.getLastOvertimeIndex() - this.problem.getLastDayShiftIndex();
		if(!horizon[randomInt1 % this.horizon.length].nachtshift) { //random dag mag geen nightshift zijn
			this.horizon[randomInt1 % this.horizon.length].setOvertime(randomInt2 % maxUrenOvertime);
		}		
	}
	
	public void swapNightShift(int randomInt, boolean randomBool1, boolean randomBool2) {
		System.out.println("swapNightShift");
		//TODO: rekening houden met die eerste dagen
		// 		-> als de history het nodig acht dat er in het begin nachtshifts zijn mag je die niet uitzetten.
		//TODO: nightshifts mogen optreden aan het einde van de periode ook & moeten hiet niet per se hun volledige periode uitzitten.
		// hoe brengen we dit in rekening?

		int index = randomInt % this.horizon.length;
		//TODO: worden dagen na horizon behandeld in constructschedule?
		//indien random gekozen dag al een night shift is
		if (horizon[index].isNachtshift()){

			//controleer waar bestaande night shift begint en eindigt
			int startNightshift = index;
			while(horizon[startNightshift].isNachtshift() && startNightshift >= 0)
				startNightshift--;
			startNightshift++;

			//TODO: wat indien night shift doorlopen na horizon
			int endNightshift = index;
			while(horizon[endNightshift].isNachtshift() && endNightshift < horizon.length)
				endNightshift++;
			endNightshift--;

			int consecutiveNightshifts = endNightshift-startNightshift+1;

			//random dag toevoegen of verwijderen (verwijder alle night shifts indien minder dan 10 dagen)
			//nightshifts toevoegen
			if (randomBool1){

				//indien voorraan toevoegen enige mogelijkheid
				if (endNightshift == horizon.length-1 && startNightshift > 0) {
					horizon[startNightshift-1].setNachtshift(true);
					horizon[startNightshift-1].setOvertime(0);
				}
					
				

				//indien achteraan toevoegen enige mogelijkheid
				else if (startNightshift == 0 && endNightshift < horizon.length-1) {
					horizon[endNightshift+1].setNachtshift(true);
					horizon[endNightshift+1].setOvertime(0);
				}
					

				//voorraan en achteraan toevoegen is mogelijk
				//voorraan toevoegen
				else if (randomBool2) {
					horizon[startNightshift-1].setNachtshift(true);
					horizon[startNightshift-1].setOvertime(0);
				}
					
				

				//achteraan toevoegen
				else {
					horizon[endNightshift+1].setNachtshift(true);
					horizon[endNightshift+1].setOvertime(0);
				}
					

			}

			//nightshifts verwijderen
			else{

				//TODO: wat indien night shift doorlopen na horizon
				if (consecutiveNightshifts == 10){
					//alles verwijderen
					for (int i=startNightshift; i<=endNightshift; i++)
						if(!isSteadyNightshift(i)) {
							horizon[i].setNachtshift(false);
						}
						
				}
				else{
					//enkel eerste dag verwijderen
					if (randomBool2 && !isSteadyNightshift(startNightshift))
						horizon[startNightshift].setNachtshift(false);
					//enkel laatste dag verwijderen
					else if(!isSteadyNightshift(endNightshift))
						horizon[endNightshift].setNachtshift(false);
				}
			}
		}
		else{
			//TODO: gekozen dag is al random, extra random nodig om beginnen toe te voegen van x dagen voor deze dag?
			//voeg 10 dagen night shift toe vanaf huidige dag
			for (int i=0; i < 10; i++){
				if (index+i >= horizon.length)
					break;
				this.horizon[index+i].setNachtshift(true);
				this.horizon[index+i].setOvertime(0);
			}

		}
	}
	
	public void addMachineOrder(int randomInt1, int randomInt2) {
		System.out.println("addMachineOrder");
		//create a random machineOrder with size == 1 & add it to the end.
		//TODO: is het beter om hier direct meerdere blokken toe te voegen? hmmm...
		randomInt1 = Math.abs(randomInt1);
		randomInt2 = Math.abs(randomInt2);
		int machine = randomInt2 % this.problem.amountOfMachines();
		//System.out.println("Amount of machines: " + this.problem.amountOfMachines());
		//System.out.println(randomInt2 % this.problem.amountOfMachines() + ", " + machine);
		ProductionOrder po = new ProductionOrder(randomInt1 % this.problem.getItems().length, machine, 1);
		this.orders.add(po);
	}
	
	public void incrementOrderCount(int randomInt) {

		//indien geen productionorders => vervangen door add
		if (orders.isEmpty())
			addMachineOrder(randomInt, new Random().nextInt());

		//voeg block toe aan random ProductionOrder
		else
			this.orders.get(randomInt % this.orders.size()).incrementAmountOfBlocks();
	}
	
	public void decrementOrderCount(int randomInt) {

		//indien geen productionorders => vervangen door add
		if (orders.isEmpty())
			addMachineOrder(randomInt, new Random().nextInt());

		//neem block weg van random ProductionOrder
		else
			this.orders.get(randomInt % this.orders.size()).decrementAmountOfBlocks();

	}

	public void swapOrders(int randomInt1, int randomInt2) {
		System.out.println("swapOrders");
		//indien geen productionorders => vervangen door add
		if (orders.isEmpty() || orders.size() == 1)
			addMachineOrder(randomInt1, randomInt2);

		//swap indexes of 2 random orders
		else{
			//zelfde index => nieuw random getal
			while (randomInt1 == randomInt2)
				randomInt1 = new Random().nextInt();

			Collections.swap(this.orders, randomInt1 % this.orders.size(), randomInt2 % this.orders.size());
		}

	}
	
	public void changeMachineForOrders(int randomInt1, int randomInt2) {
		System.out.println("changeMachineForOrders");
		//indien geen productionorders zijn => vervangen door add
		if (orders.isEmpty())
			addMachineOrder(randomInt1, randomInt2);

		//change machineId of random order
		else
			this.orders.get(randomInt1 % this.orders.size()).setMachineId(randomInt2 % this.problem.amountOfMachines());
	}
	
	public void changeItemForOrder(int randomInt1, int randomInt2) {
		System.out.println("changeItemForOrders");
		//indien geen productionorders zijn => vervangen door add
		if (orders.isEmpty())
			addMachineOrder(randomInt1, randomInt2);

		//change itemId of random order
		else
			this.orders.get(randomInt1 % this.orders.size()).setItemId(randomInt2 % this.problem.getItems().length);
	}
	
	//wissel 2 requests van plaats in array
	public void swapRequestOrder(int randomInt1, int randomInt2) {
		System.out.println("swapRequestOrder");
		int index1 = randomInt1%problem.getRequests().length;
		int index2 = randomInt2%problem.getRequests().length;

		//zelfde index => nieuw random getal
		while (index1 == index2) {
			index2 = Math.abs(new Random().nextInt()) % problem.getRequests().length;
		}

		Request[] requests = problem.getRequests();
		Request temp = requests[index2];
		requests[index2] = requests[index1];
		requests[index1] = temp;
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
					if (!new Idle().equals(job)) {
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
	
	public void configureEvaluation(Problem problem) {
		Evaluation.configureEvaluation(problem);
	}

	public double evaluate() throws OverStockException{
		this.cost = Evaluation.calculateObjectiveFunction(this);
		return this.cost;
	}
	
	public double getCost() {
		if(this.cost == Double.MAX_VALUE) {
			System.out.println("encoutered situation where cost has not been calculated...");
		} 
		return this.cost;
	}
	
	//tempCost!
	public double evaluateIntermediateSolution() throws OverStockException {
		this.tempCost = Evaluation.calculateIntermediateObjectiveFunction(this);
		return this.tempCost;
	}
	
	public double getTempCost() {
		if(this.tempCost == Double.MAX_VALUE) {
			System.out.println("tempCost has not yet been calculated...");
		}
		return this.tempCost;
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
		//end
		if (dayIndex == this.horizon.length - 1) return;
		calculateStock(dayIndex + 1, thisDay.stock);
	}
	//This method increase days until no duplicate changeover
	public void searchDayWithoutSameChangeover(Changeover co, int currentDay, int currentBlock) throws ScheduleException{		
		boolean foundDayForChangeover = false; //check if changeover already occurs on this day
		while(!foundDayForChangeover) {	
			foundDayForChangeover = true;
			for(int b=0;b<problem.getBlocksPerDay();b++) {
				for(int m=0;m<problem.getMachines().length;m++) {
					if(new Changeover(0,1).equals(horizon[currentDay].jobs[m][b])) {
						if(co.isSame((Changeover) horizon[currentDay].jobs[m][b])) {
							currentDay++;
							currentBlock = 0;
							if(currentDay>=horizon.length) {
								throw new ScheduleException();
							}								
							foundDayForChangeover = false;
						}
					}
				}
			}
			
		}
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
		sb.append(String.format(Locale.ENGLISH, "%.2f", this.cost));
		sb.append("\n");
		for(int i=0; i<horizon.length; i++) {
			sb.append("#Day " + i + "\n");
			sb.append(horizon[i]);
		}
		return sb.toString();
	}
	public Day[] getHorizon() {
		return horizon;
	}
	public boolean isSteadyNightshift(int dayIndex) {
		if(problem.getPastConsecutiveNightShifts()>0) {
			int remainingNS = problem.getMinimumConsecutiveNightShifts() - problem.getPastConsecutiveNightShifts();
			if(remainingNS > dayIndex) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}
}
