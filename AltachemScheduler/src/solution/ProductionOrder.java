package solution;

public class ProductionOrder {
	private int itemId;
	private int machineId;
	private int amountOfBlocks;

	public ProductionOrder(int itemId, int machineId, int amountOfBlocks) {
		super();
		this.itemId = itemId;
		this.machineId = machineId;
		this.amountOfBlocks = amountOfBlocks;
	}
	
	public ProductionOrder(ProductionOrder copy) {
		super();
		this.itemId = copy.itemId;
		this.machineId = copy.machineId;
		this.amountOfBlocks = copy.amountOfBlocks;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getAmountOfBlocks() {
		return amountOfBlocks;
	}

	public void setAmountOfBlocks(int amountOfBlocks) {
		this.amountOfBlocks = amountOfBlocks;
	}

	public int getMachineId() {
		return this.itemId;
	}
	
	public void setMachineId(int Id) {
		this.machineId = Id;
	}
	
	public void incrementAmountOfBlocks() {
		this.amountOfBlocks++;
	}
	
	public void decrementAmountOfBlocks() {
		this.amountOfBlocks--;
	}
}
