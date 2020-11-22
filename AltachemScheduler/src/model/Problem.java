package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Problem {
	private String outputfile;
	private String instanceName;
	
	private Machine[] machines;
	private Item[] items;
	private int numberOfDays;
	private Request[] requests;
	
	private int blocksPerDay;
	private int technicianStartIndex;
	private int technicianStopIndex;
	private int lastDayShiftIndex;
	private int lastOvertimeIndex;
	
	private int minimumConsecutiveNightShifts;
	private int pastConsecutiveNightShifts;
	
	private double costOfOvertimePerBlock;
	private double costOfNightShift;
	private double costOfParallelDay;
	private double costPerItemUnderMinimumStock;
	
	/*
	 *  per line:
	 *  	ITEMID | MACHINE_1_Prod/hr | MACHINE_2_Prod/hr | MACHINE 3....
	 */
	private int[][] machineEfficiencies;
	//kan evt als statische variabele in klasse Changeover opgeslaan worden?
	private boolean[][] isLargeChangeover;
	//kan evt als statische variabele in klasse Changeover opgeslaan worden?
	//of toch niet want dit is relevant voor scheduling...
	private int[][] changeoverDurations;
	
	public int amountOfMachines() {
		return machines.length;
	}
	
	public Problem() {
		System.out.println("empty problem constructor called...");
	}
	
	public Problem(String filename) {
		try {
			File file = new File(filename);
			Scanner sc = new Scanner(file);
			//temp, should be overwritten in real instances using the .setOutputFile(String)-function.
			this.outputfile = filename + "_out";
			
			//instance name
            this.instanceName = sc.nextLine().substring(15);
            //number of machines
            this.machines = new Machine[Integer.parseInt(sc.nextLine().substring(20))];
            //number of different items
            this.items = new Item[Integer.parseInt(sc.nextLine().substring(27))];
            //number of days
            this.numberOfDays = Integer.parseInt(sc.nextLine().substring(16));
            
            //number of requests
            this.requests = new Request[Integer.parseInt(sc.nextLine().substring(20))];
            //init requests
            for(int i=0; i<requests.length; i++) {
            	this.requests[i] = new Request(i);
            }
            
            //number of blocks per day
            this.blocksPerDay = Integer.parseInt(sc.nextLine().substring(26));    
            //index of block e () == index of first block of technician shift
            this.technicianStartIndex = Integer.parseInt(sc.nextLine().substring(18));
            //index of block l () == index of last block of technician shift
            this.technicianStopIndex = Integer.parseInt(sc.nextLine().substring(18));
            //index of block s () == index of last block of day shift
            this.lastDayShiftIndex = Integer.parseInt(sc.nextLine().substring(18));
            //index of block o () == index of last possible block of overtime
            this.lastOvertimeIndex = Integer.parseInt(sc.nextLine().substring(18));

            //minimum consecutive days with night shift
            this.minimumConsecutiveNightShifts = Integer.parseInt(sc.nextLine().substring(40));
            //past consecutive days with night shift
            this.pastConsecutiveNightShifts = Integer.parseInt(sc.nextLine().substring(41));

            //cost of overstime per overtime
            this.costOfOvertimePerBlock = Double.parseDouble(sc.nextLine().substring(22));
            //cost of nightshift per night
            this.costOfNightShift = Double.parseDouble(sc.nextLine().substring(24));
            //cost of parallel task per parallel
            this.costOfParallelDay = Double.parseDouble(sc.nextLine().substring(27));
            //penalty per item under minimum level per stock
            this.costPerItemUnderMinimumStock = Double.parseDouble(sc.nextLine().substring(42));
            
            //fix the Machine[] machines
            String infoMachines = sc.nextLine();
            for (int i=0; i<machines.length; i++){
                String[] machineData = sc.nextLine().split(" ");
                machines[i] = new Machine();
                machines[i].setMachineId(Integer.parseInt(machineData[0]));
                machines[i].setLastItemIdProduced(Integer.parseInt(machineData[1]));
                machines[i].setDaysPastWithoutMaintenance(Integer.parseInt(machineData[2]));
                machines[i].setMaxDaysWithoutMaintenance(Integer.parseInt(machineData[3]));
                machines[i].setMaintenanceDurationInBlocks(Integer.parseInt(machineData[4]));
            }
            
            //fix the Item[] items
            String infoItems = sc.nextLine();
            for (int i=0; i<items.length; i++){
                String[] itemData = sc.nextLine().split(" ");
                items[i] = new Item();
                items[i].setItemId(Integer.parseInt(itemData[0]));
                items[i].setCostPerItem(Double.parseDouble(itemData[1]));
                items[i].setQuantityInStock(Integer.parseInt(itemData[2]));
                items[i].setMinAllowedInStock(Integer.parseInt(itemData[3]));
                items[i].setMaxAllowedInStock(Integer.parseInt(itemData[4]));
            }
            
            //fix MachineEfficiencies[][]
            String infoMachineEfficienties = sc.nextLine();
            this.machineEfficiencies = new int[items.length][machines.length+1];
            for (int i=0; i<items.length; i++){
                String[] machineEfficientiesData = sc.nextLine().split(" ");
                for (int j=0; j<machines.length+1; j++){
                    this.machineEfficiencies[i][j] = Integer.parseInt(machineEfficientiesData[j]);
                }
            }
            //fix it so that machines get this info
            for( int i=0; i<this.machines.length; i++) {
            	this.machines[i].setItemEfficiencies(Problem.getColumn(machineEfficiencies, i+1));
            }
            
            //fix isLargeChangeover[][]
            String infoDescription = sc.nextLine();
            this.isLargeChangeover = new boolean[items.length][items.length];
            for (int i=0; i<items.length; i++){
                String[] isLargeChangeoverData = sc.nextLine().split(" ");
                for (int j=0; j<items.length; j++)
                    isLargeChangeover[i][j] = Integer.parseInt(isLargeChangeoverData[j]) != 0;
            }
            
            //fix changeoverDurations[][]
            String infoSetupDuration = sc.nextLine();
            this.changeoverDurations = new int[items.length][items.length];
            for (int i=0; i<items.length; i++){
                String[] machineSetupDurationData = sc.nextLine().split(" ");
                for (int j=0; j<items.length; j++)
                    this.changeoverDurations[i][j] = Integer.parseInt(machineSetupDurationData[j]);
            }
            
            //Shipping day matrix [#request X #shipping_day]. [i,j] is 1 if request i can be shipped on day j, 0 otherwise
            String infoShippingDays = sc.nextLine();
            boolean[][] shippingDays = new boolean[this.requests.length][this.numberOfDays];
            for (int i=0; i<this.requests.length; i++){
                String[] shippingDaysData = sc.nextLine().split(" ");
                for (int j=0; j<numberOfDays; j++) {
                	shippingDays[i][j] = Integer.parseInt(shippingDaysData[j]) != 0;
                }
                this.requests[i].setShippingDays(shippingDays[i]);
            }
            
          //Requested items matrix [#request X #items]. position [i,j] represents: number of items j requested by request i
            String infoRequestedItems = sc.nextLine();
            int[][] requestedItems = new int[this.requests.length][this.items.length];
            for (int i=0; i<this.requests.length; i++){
                String[] requestedItemsData = sc.nextLine().split(" ");
                for (int j=0; j<items.length; j++) {
                	requestedItems[i][j] = Integer.parseInt(requestedItemsData[j]);
                }
                this.requests[i].setAmountsRequested(requestedItems[i]);                    
            }
            
            System.out.println("Problem read from file.");
		} catch (FileNotFoundException e) {
			System.err.println("File \"" + filename + "\" not found.");
			e.printStackTrace();
		}
	}
	
	public void printRequestedItems() {
		for(Request r: this.requests) {
			System.out.print("Request " + r.getId() +": ");
			System.out.println(r.requestedItemsToString());
		}
	}
	
	public void printShippingDays() {
		for(Request r: this.requests) {
			System.out.print("Request " + r.getId() +": ");
			System.out.println(r.shippingDaysToString());
		}
	}
	
	public void printMachineInputs() {
		for(Machine m: this.machines) {
			System.out.print("Machine " + m.getMachineId() +": ");
			System.out.println(m.getValuesInString());
		}
	}
	
	public void printItemEfficiencies() {
		for(Machine m: this.machines) {
			System.out.print("Machine " + m.getMachineId() +": ");
			System.out.println(m.getItemEfficienciesInString());
		}
	}
	
	public static int[] getColumn(int[][] matrix, int column) {
	    return IntStream.range(0, matrix.length)
	        .map(i -> matrix[i][column]).toArray();
	}
}