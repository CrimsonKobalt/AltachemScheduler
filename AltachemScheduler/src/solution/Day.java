package solution;

import java.util.List;

public class Day {
	// Jobs[i][j] : de job die uitgevoegd wordt door machine i op tijdstip j;
	Job[][] jobs;

	boolean parallelwerk;
	boolean nachtshift;
	int overtime;
	
	static int[] lastMaintenanceDayIndex = null;

	public Day(int aantalMachines, int aantalBlokkenPerDag) {
		jobs = new Job[aantalMachines][aantalBlokkenPerDag];
		for(int i=0; i<aantalMachines; i++) {
			for(int j=0; j<aantalBlokkenPerDag; j++) {
				jobs[i][j] = new Idle();
			}
		}
		
		this.parallelwerk = false;
		this.nachtshift = false;
		this.overtime = 0;
	}

	public Job[][] getJobs() {
		return jobs;
	}

	public void setJobs(Job[][] jobs) {
		this.jobs = jobs;
	}

	public boolean isParallelwerk() {
		return parallelwerk;
	}

	public void setParallelwerk(boolean parallelwerk) {
		this.parallelwerk = parallelwerk;
	}

	public boolean isNachtshift() {
		return nachtshift;
	}

	public void setNachtshift(boolean nachtshift) {
		this.nachtshift = nachtshift;
	}

	public int getOvertime() {
		return overtime;
	}

	public void setOvertime(int overtime) {
		this.overtime = overtime;
	}

	public static int[] getLastMaintenanceDays() {
		return Day.lastMaintenanceDayIndex;
	}
	
	public static void setLastMaintenanceDays(int[] indices) {
		Day.lastMaintenanceDayIndex = indices;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int b=0;b<jobs[0].length;b++) {
			sb.append(b);
			for(int m=0;m<jobs.length;m++) {
				sb.append(";" + jobs[m][b]);				
			}
			sb.append("\n");
		}
		sb.append("#Shipped request ids\n");
		sb.append("-1\n");
		sb.append("#Night shift\n");
		if(this.nachtshift) {
			sb.append("1\n");
		} else {
			sb.append("0\n");
		}
		return sb.toString();
	}
}
