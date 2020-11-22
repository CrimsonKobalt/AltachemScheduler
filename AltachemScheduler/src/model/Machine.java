package model;

import java.util.Arrays;

public class Machine {
	private int machineId;
	private int lastItemIdProduced;
	private int daysPastWithoutMaintenance;
	private int maxDaysWithoutMaintenance;
	private int maintenanceDurationInBlocks;
	
	private int[] itemEfficiencies = null;
	
	public Machine() {
		//EMPTY CONSTRUCTOR, is filled with setters.
	}

	public String getValuesInString() {
		return "[" + this.machineId + ", " + this.lastItemIdProduced + ", " + this.daysPastWithoutMaintenance + ", " + this.maxDaysWithoutMaintenance + ", " + this.maintenanceDurationInBlocks + "]";
	}
	
	public String getItemEfficienciesInString() {
		return Arrays.toString(itemEfficiencies);
	}

	
	//GETTERS & SETTERS
	public int getMachineId() {
		return machineId;
	}

	public void setMachineId(int machineId) {
		this.machineId = machineId;
	}

	public int getLastItemIdProduced() {
		return lastItemIdProduced;
	}

	public void setLastItemIdProduced(int lastItemIdProduced) {
		this.lastItemIdProduced = lastItemIdProduced;
	}

	public int getDaysPastWithoutMaintenance() {
		return daysPastWithoutMaintenance;
	}

	public void setDaysPastWithoutMaintenance(int daysPastWithoutMaintenance) {
		this.daysPastWithoutMaintenance = daysPastWithoutMaintenance;
	}

	public int getMaxDaysWithoutMaintenance() {
		return maxDaysWithoutMaintenance;
	}

	public void setMaxDaysWithoutMaintenance(int maxDaysWithoutMaintenance) {
		this.maxDaysWithoutMaintenance = maxDaysWithoutMaintenance;
	}

	public int getMaintenanceDurationInBlocks() {
		return maintenanceDurationInBlocks;
	}

	public void setMaintenanceDurationInBlocks(int maintenanceDurationInBlocks) {
		this.maintenanceDurationInBlocks = maintenanceDurationInBlocks;
	}
	
	public int[] getItemEfficiencies() {
		return itemEfficiencies;
	}

	public void setItemEfficiencies(int[] itemEfficiency) {
		this.itemEfficiencies = itemEfficiency;
	}
}
