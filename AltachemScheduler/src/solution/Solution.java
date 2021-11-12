package solution;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import model.Item;
import model.Machine;
import model.Problem;
import model.Request;
import thesis_logger.ThesisLogger;

public class Solution {
	Day[] horizon;//

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

	private final Random random = new Random();

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
		
		solution.calculateStock();

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
		this.scheduleMaintenances(problem);

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
			
			if(lastProduced != po.getItemId()) {
				Changeover co = new Changeover(lastProduced, po.getItemId());
				while(!isDayWithoutSameChangeover(co, currentDay, currentBlock)) {
					currentDay++;
					if(currentDay>=horizon.length) {
						throw new ScheduleException();
					}
				}
				
				int consecutiveBlocks = 0; //wordt terug op nul gezet als er een production gevonden wordt
				int startBlock = currentBlock;	
				List<Integer> maintenanceBlocks = new ArrayList<>(); //bijhouden om achteraf de changoverblocks in te vullen
				
				boolean isLargeCo = problem.getIsLargeChangeover()[lastProduced][po.getItemId()];
				if(!isLargeCo) { 				
					
					while(consecutiveBlocks<coDuration) {
						
						if(horizon[currentDay].parallelwerk || currentBlock>problem.getLastDayShiftIndex()) { 
							//PARALLELWERK (mag niet parallel met largeChangeover or maintenance
							boolean noLargeCoOrMainten = true;
							for(int m=0;m<problem.getMachines().length;m++) {
								if(new Changeover(1, 0).isChangeover(horizon[currentDay].jobs[m][currentBlock])){
									Changeover lco = (Changeover) horizon[currentDay].jobs[m][currentBlock];
									if(problem.getIsLargeChangeover()[lco.getFromItemId()][lco.getToItemId()]) {
										if(noLargeCoOrMainten) {
											maintenanceBlocks.add(currentBlock);
										}
										noLargeCoOrMainten = false;
										if(m==currentMachine) {
											startBlock = currentBlock + 1;
											consecutiveBlocks = 0;
											maintenanceBlocks.clear();
										}
										
									}
								}else if(new Maintenance().isMaintenance(horizon[currentDay].jobs[m][currentBlock])){
									if(noLargeCoOrMainten) {
										maintenanceBlocks.add(currentBlock);
									}
									noLargeCoOrMainten = false;
								}
							}
							if(noLargeCoOrMainten) {
								if(new Idle().equals(horizon[currentDay].jobs[currentMachine][currentBlock])) {
									consecutiveBlocks++;
								}else if(new Production(1).isProduction(horizon[currentDay].jobs[currentMachine][currentBlock])) {
									consecutiveBlocks= 0;
									startBlock = currentBlock + 1;	
									maintenanceBlocks.clear();
								}else if(new Changeover(1,0).isChangeover(horizon[currentDay].jobs[currentMachine][currentBlock])) {
									//Kan nog small changeover zijn
									consecutiveBlocks = 0;
									startBlock = currentBlock + 1;
									maintenanceBlocks.clear();
								}
							}
							currentBlock++;
							if(currentBlock >= (problem.getLastDayShiftIndex() + horizon[currentDay].getOvertime())
									&& !horizon[currentDay].isNachtshift()) {
								currentBlock = 0;
								currentDay++;
								if(currentDay>=horizon.length) {
									throw new ScheduleException();
								}
								while(!isDayWithoutSameChangeover(co, currentDay, currentBlock)) {
									currentDay++;
									if(currentDay>=horizon.length) {
										throw new ScheduleException();
									}
								}
								
								startBlock = 0; 
								consecutiveBlocks = 0;
								maintenanceBlocks.clear();
							}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
								currentBlock = 0;
								currentDay++;
								startBlock = 0; 
								consecutiveBlocks = 0;
								if(currentDay>=horizon.length) {
									throw new ScheduleException();
								}
								while(!isDayWithoutSameChangeover(co, currentDay, currentBlock)) {
									currentDay++;
									if(currentDay>=horizon.length) {
										throw new ScheduleException();
									}
								}
								maintenanceBlocks.clear();
							}
						}else { // GEEN PARALLELWERK
							boolean bothIdle = true;
							for(int m=0;m<problem.getMachines().length;m++) {
								if(new Idle().equals(horizon[currentDay].jobs[m][currentBlock])) {				
									if(bothIdle) {
										bothIdle =true;
									}								
								}else if(new Production(1).isProduction(horizon[currentDay].jobs[m][currentBlock])) { //maakt niet uit welk item
									consecutiveBlocks = 0;
									startBlock = currentBlock +1;
									maintenanceBlocks.clear();
									bothIdle = false;
								}else if(new Changeover(1, 0).isChangeover(horizon[currentDay].jobs[m][currentBlock])){
									consecutiveBlocks= 0;
									startBlock = currentBlock + 1;
									maintenanceBlocks.clear();
									bothIdle = false;
								}else {
									if(bothIdle) {
										maintenanceBlocks.add(currentBlock);
									}
									bothIdle = false;							
									
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
								while(!isDayWithoutSameChangeover(co, currentDay, currentBlock)) {
									currentDay++;
									if(currentDay>=horizon.length) {
										throw new ScheduleException();
									}
									
								}
								startBlock = 0; 
								consecutiveBlocks = 0;
								maintenanceBlocks.clear();
							}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
								currentBlock = 0;
								currentDay++;
								if(currentDay>=horizon.length) {
									throw new ScheduleException();
								}
								while(!isDayWithoutSameChangeover(co, currentDay, currentBlock)) {
									currentDay++;
									if(currentDay>=horizon.length) {
										throw new ScheduleException();
									}
								}
								startBlock = 0;
								consecutiveBlocks = 0;
								maintenanceBlocks.clear();
							}
						}
					}
					
						
					}else{
						// Large Changeover -> niet-parallel
						//System.out.println("large changeover" + co.getFromItemId() + ", " + co.getToItemId());
						while(consecutiveBlocks<coDuration) {
							if(currentBlock>=problem.getTechnicianStartIndex() && currentBlock<=problem.getTechnicianStopIndex()) {
								//System.out.println("current Block: " + currentBlock);
									boolean bothIdle = true;
									for(int m=0;m<problem.getMachines().length;m++) {
										if(new Idle().equals(horizon[currentDay].jobs[m][currentBlock])) {				
											if(bothIdle) {
												bothIdle =true;
											}										
										}else if(new Production(1).isProduction(horizon[currentDay].jobs[m][currentBlock])) { //maakt niet uit welk item
											consecutiveBlocks = 0;
											startBlock = currentBlock +1;
											maintenanceBlocks.clear();
											bothIdle = false;
										}else if(new Changeover(1, 0).isChangeover(horizon[currentDay].jobs[m][currentBlock])){
											consecutiveBlocks= 0;
											startBlock = currentBlock + 1;
											maintenanceBlocks.clear();
											bothIdle = false;
										}
										else {
											if(bothIdle) {
												maintenanceBlocks.add(currentBlock);
											}
											bothIdle = false;										
											
										}
									}
									if(bothIdle) {
										consecutiveBlocks++;
										
									}
							}else {
								startBlock = currentBlock+1;
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
								while(!isDayWithoutSameChangeover(co, currentDay, currentBlock))  {
									currentDay++;
									if(currentDay>=horizon.length) {
										throw new ScheduleException();
									}
								}
								startBlock = 0; 
								consecutiveBlocks = 0;
								maintenanceBlocks.clear();
							}else if(currentBlock>=problem.getBlocksPerDay() && horizon[currentDay].isNachtshift()) {
								currentBlock = 0;
								currentDay++;
								if(currentDay>=horizon.length) {
									throw new ScheduleException();
								}
								while(!isDayWithoutSameChangeover(co, currentDay, currentBlock)) {
									currentDay++;
									if(currentDay>=horizon.length) {
										throw new ScheduleException();
									}
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
							//System.out.println(horizon[currentDay].jobs[po.getMachineId()][b]);
						}					
					}	
			}
							
				//start producing
				int blocksProduced = 0;
				while(blocksProduced<po.getAmountOfBlocks()) {
					//System.out.println("in while producing");
					if(horizon[currentDay].parallelwerk || currentBlock>problem.getLastDayShiftIndex()) { //PARALLELWERK
						//Mag niet parallel met maintenance||largeCo
						boolean isThereMaintenanceOrLargeChangover = false;
						for(int m=0;m<problem.getMachines().length;m++) {
							if(new Maintenance().isMaintenance(horizon[currentDay].getJobs()[m][currentBlock])) {
								isThereMaintenanceOrLargeChangover = true;
							}else if(new Changeover(1,0).isChangeover(horizon[currentDay].getJobs()[m][currentBlock])) {
								Changeover temp = (Changeover) horizon[currentDay].getJobs()[m][currentBlock];
								if(problem.getIsLargeChangeover()[temp.getFromItemId()][temp.getToItemId()]) {
									isThereMaintenanceOrLargeChangover = true;
								}
							}
						}
						if(!isThereMaintenanceOrLargeChangover) {
							if(new Idle().equals(horizon[currentDay].jobs[currentMachine][currentBlock])) {
								blocksProduced++;
								horizon[currentDay].setJob(new Production(po.getItemId()), currentMachine, currentBlock);
								//System.out.println(horizon[currentDay].jobs[currentMachine][currentBlock]);
							}
						}

					}else { //GEEN PARALLELWERK
						boolean bothIdle = true;
						for(int m=0;m<problem.getMachines().length;m++) {
							if(!new Idle().equals(horizon[currentDay].jobs[m][currentBlock])) {
								bothIdle = false;
							}
						}
						if(bothIdle) {
							blocksProduced++;
							horizon[currentDay].setJob(new Production(po.getItemId()), currentMachine, currentBlock);
							//System.out.println(horizon[currentDay].jobs[currentMachine][currentBlock]);
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
		
		/*
		//fix overtime
			for(Day d: this.horizon) {
				if(d.overtime > 0) {
					boolean used = false;
					for(Machine m: this.problem.getMachines()) {
						for(int i=this.problem.getLastDayShiftIndex()+1; i<this.problem.getLastOvertimeIndex()+1; i++) {
							if(!d.jobs[m.getMachineId()][i].equals(new Idle())) {
								used = true;
								break;
							}
						}
						if(used) {
							break;
						}
					}
					if(!used) {
						d.overtime = 0;
					}
				}
			}
		
			//fix parallelwork
			for(Day d: this.horizon) {
				if(d.parallelwerk) {
					boolean inparallel = false;
					for(int i=0; i<this.problem.getLastDayShiftIndex()+1; i++) {
						boolean blockInUse = false;
						for(Machine m: this.problem.getMachines()) {
							if(!d.jobs[m.getMachineId()][i].equals(new Idle())) {
								if(blockInUse == true) {
									inparallel = true;
									break;
								} else {
									blockInUse = true;
								}
							}
						}
						if(inparallel) {
							break;
						}
					}
					if(!inparallel) {
						d.parallelwerk = false;
					}
				}
			}
		*/	
		
			calculateStock();
		
			//handleRquests()
			List<Request> requests = new ArrayList<>();
			requests.addAll(requestOrder);
			int day = 0;
			while(day < this.horizon.length) {
				Iterator<Request> reqit = requests.iterator();
				while(reqit.hasNext()) {
					Request r = reqit.next();
					if (r.isShippingDay(day)) {
						boolean enoughStock = true;
						for (int i = 0; i < r.getAmountsRequested().length; i++) {
							if (r.getAmountsRequested()[i] > this.horizon[day].stock[i]) {
								enoughStock = false;
								break;
							}
						}
						if (enoughStock) {
							for (int i = day; i < this.horizon.length; i++) {
								for (int itemId = 0; itemId < this.problem.getItems().length; itemId++) {
									this.horizon[i].getStock()[itemId] -= r.getAmountsRequested()[itemId];
								}
							}
							this.horizon[day].shippedToday.add(r);
							//verwijder deze request uit de requests-list op een iteratief-veilige manier.
							reqit.remove();
						} 
					}
				}
				day++;
			}
	}
	
	public void printSchedule() {
		for(int i=0; i<this.horizon.length; i++) {
			Day d = this.horizon[i];
			System.out.println("#Day " + i);
			for(int b=0; b<this.problem.getBlocksPerDay(); b++) {
				System.out.println(b + "; " + d.jobs[0][b] + "; "+d.jobs[1][b]);
			}
		}
	}
	
	//START SWAPS
	public void executeRandomSwap() {

		switch (random.nextInt(10)){
			//basisswaps
			case 0: swapParallelWork();	break;
			case 1: swapNightShift(); break;
			case 2: swapOvertime(); break;
			case 3: swapOrders(); break;
			case 4: swapRequestOrder(); break;
			case 5:
				for (int i=0; i<random.nextInt(3)+1; i++)
					incrementOrderCount();
				break;
			case 6:
				for (int i=0; i<random.nextInt(3)+1; i++)
					decrementOrderCount();
				break;
			case 7: changeMachineForOrders(); break;
			case 8: changeItemForOrder(); break;
			case 9:
				for (int i=0; i<random.nextInt(3)+1; i++)
					addMachineOrder();
				break;

			//grotere kansen

			default: break;
		}
		
	}

	public void swapParallelWork() {
		//System.out.println("swapParallelWork");
		long starttime = System.currentTimeMillis();
		int param = random.nextInt(horizon.length);
		this.horizon[param].setParallelwerk(!this.horizon[param].parallelwerk);
		long timepassed = System.currentTimeMillis() - starttime;
		
		ThesisLogger.logOperation("swapParallelWork", timepassed, param);
	}

	public void swapOvertime() {
		//System.out.println("swapOvertime");

		long starttime = System.currentTimeMillis();
		int index = random.nextInt(horizon.length);
		if (!horizon[index].isNachtshift()){
			switch (random.nextInt(3)){
				//select random overtime
				case 0:
					this.horizon[index].setOvertime(random.nextInt(problem.getLastOvertimeIndex()-problem.getLastDayShiftIndex()));
					break;
				//increment overtime
				case 1:
					if (horizon[index].getOvertime() < problem.getLastOvertimeIndex()-problem.getLastDayShiftIndex())
						horizon[index].setOvertime(horizon[index].getOvertime()+1);
					break;
				//decrement overtime
				case 2:
					if (horizon[index].getOvertime() > 0)
						horizon[index].setOvertime(horizon[index].getOvertime()-1);
					break;
			}
		}
		// wordt meer opgeroepen indien er al veel night shifts zijn
		else
			replaceNightshiftWithOvertime();
		
		long timepassed = System.currentTimeMillis() - starttime;
		ThesisLogger.logOperation("swapOvertime", timepassed, index);
	}
	
	public void swapNightShift() {
		//System.out.println("swapNightShift");
		
		long starttime = System.currentTimeMillis();
		int index = random.nextInt(horizon.length);
		//indien random gekozen dag al een night shift is
		if (horizon[index].isNachtshift()){

			//controleer waar bestaande night shift begint en eindigt
			int startNightshift = index;
			while(startNightshift >= 0 && horizon[startNightshift].isNachtshift()) {
				startNightshift--;
			}
			startNightshift++;

			int endNightshift = index;
			while(endNightshift < horizon.length && horizon[endNightshift].isNachtshift())
				endNightshift++;
			endNightshift--;

			int consecutiveNightshifts = endNightshift-startNightshift+1;

			//random dag toevoegen of verwijderen (verwijder alle night shifts indien minder dan minimum dagen)
			//nightshifts toevoegen
			if (random.nextBoolean()){

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
				else if (random.nextBoolean() && startNightshift > 0) {
					horizon[startNightshift-1].setNachtshift(true);
					horizon[startNightshift-1].setOvertime(0);
				}
				//achteraan toevoegen
				else if (endNightshift < horizon.length - 1){
					horizon[endNightshift+1].setNachtshift(true);
					horizon[endNightshift+1].setOvertime(0);
				}
			}

			//nightshifts verwijderen
			else{
				//indien kleiner dan minimum => alles verwijderen
				if (consecutiveNightshifts == problem.getMinimumConsecutiveNightShifts()){
					//alles verwijderen
					for (int i=startNightshift; i<=endNightshift; i++)
						if(!isSteadyNightshift(i)) {
							horizon[i].setNachtshift(false);
						}
				}
				//indien groter dan 2 keer minimum => ook dagen in midden kunnen verwijderd worden
				else if (consecutiveNightshifts > 2*problem.getMinimumConsecutiveNightShifts()){
					switch (random.nextInt(3)){
						//verwijder dag in midden
						case 0:
							//kies random dag vanuit midden
							int bound = consecutiveNightshifts - 2*problem.getMinimumConsecutiveNightShifts();
							int randomInt = random.nextInt(bound);
							int deleteIndex = startNightshift + problem.getMinimumConsecutiveNightShifts() + randomInt;
							if (!isSteadyNightshift(deleteIndex))
								horizon[deleteIndex].setNachtshift(false);
							break;
						//enkel eerste dag verwijderen
						case 1:
							if (!isSteadyNightshift(startNightshift))
								horizon[startNightshift].setNachtshift(false);
							break;
						//enkel laatste dag verwijderen
						case 2:
							if(!isSteadyNightshift(endNightshift))
								horizon[endNightshift].setNachtshift(false);
							break;
					}

				}
				// enkel dagen voorraan en achteraan kunnen verwijderd worden
				else{
					//enkel eerste dag verwijderen
					if (random.nextBoolean() && !isSteadyNightshift(startNightshift))
						horizon[startNightshift].setNachtshift(false);
					//enkel laatste dag verwijderen
					else if(!isSteadyNightshift(endNightshift))
						horizon[endNightshift].setNachtshift(false);
				}
			}
		}
		else{
			//TODO: gekozen dag is al random, extra random nodig om beginnen toe te voegen van x dagen voor deze dag?
			//voeg minimum dagen night shift toe vanaf huidige dag
			for (int i=0; i < problem.getMinimumConsecutiveNightShifts(); i++){
				if (index+i >= horizon.length)
					break;
				this.horizon[index+i].setNachtshift(true);
				this.horizon[index+i].setOvertime(0);
			}
		}

		
		long timepassed = System.currentTimeMillis() - starttime;
		ThesisLogger.logOperation("swapNightShift", timepassed, index);
	}

	//enkel uitgevoerd door swapOvertime()
	public void replaceNightshiftWithOvertime(){
		//System.out.println("replaceNightShiftWithOvertime");

		long starttime = System.currentTimeMillis();
		int index = random.nextInt(horizon.length);

		//zoek random night shift
		//counter zorgt dat programma niet vastloopt indien nergens night shift is
		int counter = 0;
		while (!horizon[index].isNachtshift() && counter < 10){
			index = random.nextInt(horizon.length);
			counter++;
		}

		//controleer waar night shift begint en eindigt
		int startNightshift = index;
		while (startNightshift >= 0 && horizon[startNightshift].isNachtshift()) {
			startNightshift--;
		}
		startNightshift++;

		int endNightshift = index;
		while (endNightshift < horizon.length && horizon[endNightshift].isNachtshift())
			endNightshift++;
		endNightshift--;

		int consecutiveNightshifts = endNightshift - startNightshift + 1;

		int caseChoice = -1;

		//bepaal welke mogelijkheden uit switch case mogen voorkomen
		//indien kleiner dan minimum => alles vervangen (enkel laatste optie)
		if (consecutiveNightshifts == problem.getMinimumConsecutiveNightShifts())
			caseChoice = 4;

		//indien groter dan 2 keer minimum => ook dagen in midden kunnen vervangen worden (opties 0 tem 6)
		else if (consecutiveNightshifts > 2*problem.getMinimumConsecutiveNightShifts())
			caseChoice = random.nextInt(7);

		// enkel dagen voorraan en achteraan kunnen vervangen worden (opties 0 tem 4)
		else
			caseChoice = random.nextInt(5);


		switch (caseChoice){
			//enkel eerste dag vervangen
			case 0:
				/*
				if (!isSteadyNightshift(startNightshift)){
					horizon[startNightshift].setNachtshift(false);
					//altijd vervangen door max overtime, kan later door andere swaps nog aangepast worden
					horizon[startNightshift].setOvertime(problem.getLastOvertimeIndex()-problem.getLastDayShiftIndex());
				}
				break;
				 */
			//alle eerste dagen vervangen
			case 1:
				int bound = endNightshift - problem.getMinimumConsecutiveNightShifts();
				for (int i=startNightshift; i<bound; i++){
					if (!isSteadyNightshift(i)){
						horizon[i].setNachtshift(false);
						//altijd vervangen door max overtime, kan later door andere swaps nog aangepast worden
						horizon[i].setOvertime(problem.getLastOvertimeIndex()-problem.getLastDayShiftIndex());
					}
				}
				break;
			//enkel laatste dag verwijderen
			case 2:
				/*
				if (!isSteadyNightshift(endNightshift)){
					horizon[endNightshift].setNachtshift(false);
					//altijd vervangen door max overtime, kan later door andere swaps nog aangepast worden
					horizon[endNightshift].setOvertime(problem.getLastOvertimeIndex()-problem.getLastDayShiftIndex());
				}
				break;
				 */

			//alle laatste dagen vervangen
			case 3:
				for (int i=startNightshift+problem.getMinimumConsecutiveNightShifts(); i<=endNightshift; i++){
					if (!isSteadyNightshift(i)){
						horizon[i].setNachtshift(false);
						//altijd vervangen door max overtime, kan later door andere swaps nog aangepast worden
						horizon[i].setOvertime(problem.getLastOvertimeIndex()-problem.getLastDayShiftIndex());
					}
				}
				break;
			case 4:
				//alles vervangen
				for (int i=startNightshift; i<=endNightshift; i++){
					if(!isSteadyNightshift(i)) {
						horizon[i].setNachtshift(false);
						//altijd vervangen door max overtime, kan later door andere swaps nog aangepast worden
						horizon[i].setOvertime(problem.getLastOvertimeIndex()-problem.getLastDayShiftIndex());
					}
				}
				break;
			//vervangen 1 dag in midden
			case 5:
				/*
				//kies random dag vanuit midden
				bound = consecutiveNightshifts - 2*problem.getMinimumConsecutiveNightShifts();
				int randomInt = random.nextInt(bound);
				int replaceIndex = startNightshift + problem.getMinimumConsecutiveNightShifts() + randomInt;
				if (!isSteadyNightshift(replaceIndex)){
					horizon[replaceIndex].setNachtshift(false);
					//altijd vervangen door max overtime, kan later door andere swaps nog aangepast worden
					horizon[replaceIndex].setOvertime(problem.getLastOvertimeIndex()-problem.getLastDayShiftIndex());
				}
				break;
				 */
			//vervangen alle dagen in midden
			case 6:
				//kies random dag vanuit midden
				bound = consecutiveNightshifts - 2*problem.getMinimumConsecutiveNightShifts();
				int replaceIndex = startNightshift + problem.getMinimumConsecutiveNightShifts();
				for (int i=0; i<bound; i++){
					if (!isSteadyNightshift(replaceIndex+i)){
						horizon[replaceIndex+i].setNachtshift(false);
						//altijd vervangen door max overtime, kan later door andere swaps nog aangepast worden
						horizon[replaceIndex+i].setOvertime(problem.getLastOvertimeIndex()-problem.getLastDayShiftIndex());
					}
				}
				break;
		}
		
		long timepassed = System.currentTimeMillis() - starttime;
		ThesisLogger.logOperation("replaceNightShiftWithOvertime", timepassed, index);
	}

	public void addMachineOrder() {
		//System.out.println("addMachineOrder");
		//create a random machineOrder with size == 1 & add it to the end.
		//TODO: is het beter om hier direct meerdere blokken toe te voegen?
		long starttime = System.currentTimeMillis();
		int itemid = random.nextInt(problem.getItems().length);
		int machineid = random.nextInt(problem.amountOfMachines());
		int amountOfBlocks = random.nextInt(3)+1;
		orders.add(new ProductionOrder(itemid, machineid, amountOfBlocks));
		long timepassed = System.currentTimeMillis() - starttime;
		ThesisLogger.logOperation("addMachineOrder", timepassed, itemid, machineid, amountOfBlocks);
	}
	
	public void incrementOrderCount() {
		//indien geen productionorders => vervangen door add
		if (orders.isEmpty())
			addMachineOrder();

		//voeg block toe aan random ProductionOrder
		else {
			long starttime = System.currentTimeMillis();
			int index = random.nextInt(orders.size());
			orders.get(index).incrementAmountOfBlocks();
			long timepassed = System.currentTimeMillis() - starttime;
			ThesisLogger.logOperation("incrementOrderCount", timepassed, index);
		}
	}
	
	public void decrementOrderCount() {

		//indien geen productionorders => vervangen door add
		if (orders.isEmpty())
			addMachineOrder();

		//neem block weg van random ProductionOrder
		else {
			long starttime = System.currentTimeMillis();
			int index = random.nextInt(orders.size());
			orders.get(index).decrementAmountOfBlocks();
			if(orders.get(index).getAmountOfBlocks() == 0) {
				orders.remove(index);
			}
			long timepassed = System.currentTimeMillis() - starttime;
			ThesisLogger.logOperation("decrementOrderCount", timepassed, index);
		}
	}

	public void swapOrders() {
		//System.out.println("swapOrders");
		//indien geen productionorders => vervangen door add
		if (orders.isEmpty() || orders.size() == 1)
			addMachineOrder();

		//swap indexes of 2 random orders
		else{
			//zelfde index => nieuw random getal
			long starttime = System.currentTimeMillis();
			int index1 = random.nextInt(orders.size());
			int index2 = random.nextInt(orders.size());

			while (index1 == index2)
				index1 = random.nextInt(orders.size());

			Collections.swap(this.orders, index1, index2);
			long timepassed = System.currentTimeMillis() - starttime;
			ThesisLogger.logOperation("swapOrders", timepassed, index1, index2);
		}

	}
	
	public void changeMachineForOrders() {
		//System.out.println("changeMachineForOrders");
		//indien geen productionorders zijn => vervangen door add
		if (orders.isEmpty())
			addMachineOrder();

		//change machineId of random order
		else {
			long starttime = System.currentTimeMillis();
			int orderindex = random.nextInt(orders.size());
			int machineid = random.nextInt(problem.amountOfMachines());
			orders.get(orderindex).setMachineId(machineid);
			long timepassed = System.currentTimeMillis() - starttime;
			ThesisLogger.logOperation("changeMachineForOrder", timepassed, orderindex, machineid);
		}	
	}
	
	public void changeItemForOrder() {
		//System.out.println("changeItemForOrders");
		//indien geen productionorders zijn => vervangen door add
		if (orders.isEmpty())
			addMachineOrder();

		//change itemId of random order
		else {
			long starttime = System.currentTimeMillis();
			int orderid = random.nextInt(orders.size());
			int itemid = random.nextInt(problem.getItems().length);
			orders.get(orderid).setItemId(itemid);
			long timepassed = System.currentTimeMillis() - starttime;
			ThesisLogger.logOperation("changeItemForOrder", timepassed, orderid, itemid);
		}
	}
	
	//wissel 2 requests van plaats in array
	public void swapRequestOrder() {
		//System.out.println("swapRequestOrder");
		//zelfde index => nieuw random getal
		long starttime = System.currentTimeMillis();
		int index1 = random.nextInt(problem.getRequests().length);
		int index2 = random.nextInt(problem.getRequests().length);

		while (index1 == index2)
			index1 = random.nextInt(problem.getRequests().length);

		Collections.swap(requestOrder, index1, index2);
		long timepassed = System.currentTimeMillis() - starttime;
		ThesisLogger.logOperation("swapRequestOrder", timepassed, index1, index2);
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
	
		public boolean isDayWithoutSameChangeover(Changeover co, int currentDay, int currentBlock)	{					
				
			for(int b=0;b<problem.getBlocksPerDay();b++) {
				for(int m=0;m<problem.getMachines().length;m++) {
					if(co.isChangeover(horizon[currentDay].jobs[m][b])) {
						Changeover changeover = (Changeover) horizon[currentDay].jobs[m][b];
						if(co.isSame(changeover)) {						
							return false;								
						}
					}
				}
			}
			return true;
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
