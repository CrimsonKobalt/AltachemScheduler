package solution;

public class ProductionOrder {
	private int itemId;
	private int amountOfBlocks;

	public ProductionOrder(int itemId, int amountOfBlocks) {
		super();
		this.itemId = itemId;
		this.amountOfBlocks = amountOfBlocks;
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

	@Override
	public String toString() {
		return "ProductionOrder [itemId=" + itemId + ", amountOfBlocks=" + amountOfBlocks + "]";
	}

}
