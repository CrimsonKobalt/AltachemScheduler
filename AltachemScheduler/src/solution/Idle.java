package solution;

public class Idle extends Job {

	public Idle() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "IDLE";
	}
	
	@Override
	public boolean equals(Job job) {
		return this.getClass().equals(job.getClass());
	}

}
