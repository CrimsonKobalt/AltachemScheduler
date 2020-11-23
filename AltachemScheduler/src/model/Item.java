package model;

public class Item {
	private int itemId;
	private double costPerItem;
	private int quantityInStock;
	private int minAllowedInStock;
	private int maxAllowedInStock;

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public double getCostPerItem() {
		return costPerItem;
	}

	public void setCostPerItem(double costPerItem) {
		this.costPerItem = costPerItem;
	}

	public int getQuantityInStock() {
		return quantityInStock;
	}

	public void setQuantityInStock(int quantityInStock) {
		this.quantityInStock = quantityInStock;
	}

	public int getMinAllowedInStock() {
		return minAllowedInStock;
	}

	public void setMinAllowedInStock(int minAllowedInStock) {
		this.minAllowedInStock = minAllowedInStock;
	}

	public int getMaxAllowedInStock() {
		return maxAllowedInStock;
	}

	public void setMaxAllowedInStock(int maxAllowedInStock) {
		this.maxAllowedInStock = maxAllowedInStock;
	}
}
