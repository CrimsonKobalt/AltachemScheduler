package solution;

public class Changeover extends Job {
	private int fromItemId;
	private int toItemId;
	
	private static boolean[][] isLargeChangeover;

	public Changeover(int fromItemId, int toItemId) {
		super();
		this.fromItemId = fromItemId;
		this.toItemId = toItemId;
	}

	public int getFromItemId() {
		return fromItemId;
	}

	public void setFromItemId(int fromItemId) {
		this.fromItemId = fromItemId;
	}

	public int getToItemId() {
		return toItemId;
	}

	public void setToItemId(int toItemId) {
		this.toItemId = toItemId;
	}
	public boolean isSame(Changeover co) {
		if(this.fromItemId==co.fromItemId && this.toItemId==co.toItemId) {
			return true;
		}else {
			return false;
		}
	}
	public boolean isChangeover(Job job) {
		return this.getClass()==job.getClass();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fromItemId;
		result = prime * result + toItemId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Changeover other = (Changeover) obj;
		if (fromItemId != other.fromItemId)
			return false;
		if (toItemId != other.toItemId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Changeover [fromItemId=" + fromItemId + ", toItemId=" + toItemId + "]";
	}
	
	public Changeover maakChangeover(int fromId, int toId) {
		if(isLargeChangeover[fromId][toId]) {
			//return new LargeChangeover
		}
		//return normal changeover
		return null;
	}
}
