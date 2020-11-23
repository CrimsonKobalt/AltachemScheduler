package solution;

public class Maintenance extends Job {
	
	public Maintenance() {
		super();
	}

	@Override
	public String toString() {
		return "M";
	}
	
	@Override
	public boolean equals(Job job) {
		return this.getClass().equals(job.getClass());
	}
}