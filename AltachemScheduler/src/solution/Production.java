package solution;

public class Production extends Job {
	private int itemId;

	public Production(int itemId) {
		this.itemId = itemId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public boolean isProduction(Job job) {
		if(!this.getClass().equals(job.getClass())) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String toString() {
		return "Production [itemId=" + itemId + "]";
	}
	
	@Override
	public boolean equals(Job job) {
		if(!this.getClass().equals(job.getClass())) {
			return false;
		} else {
			Production p = (Production) job;
			return this.itemId == p.getItemId();
		}
	}
}
