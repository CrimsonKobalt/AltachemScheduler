package model;

public class Production extends Job {
	private int itemId;

	public Production(int itemId) {
		super();
		this.itemId = itemId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	@Override
	public String toString() {
		return "Production [itemId=" + itemId + "]";
	}
}
