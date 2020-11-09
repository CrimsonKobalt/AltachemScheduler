package model;

public class Problem {
	private Machine[] machines;
	private Item[] items;
	private int numberOfDays;
	private Request[] requests;
	
	private int blocksPerDay;
	private int technicianStartIndex;
	private int technicianStopIndex;
	private int lastDayShiftIndex;
	private int lastOvertimeIndex;
	
	private int previousNightShifts;
	
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
}