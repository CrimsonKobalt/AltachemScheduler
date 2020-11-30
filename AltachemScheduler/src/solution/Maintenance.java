package solution;

public class Maintenance extends Job {
	
	public Maintenance() {
		super();
	}

	@Override
	public String toString() {
		return "M";
	}
	public boolean isMaintenance(Job job) {
		return this.getClass()==job.getClass();
	}
	@Override
	public boolean equals(Job job) {
		return this.getClass().equals(job.getClass());
	}
}