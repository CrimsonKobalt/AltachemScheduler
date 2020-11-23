package solution;

import java.util.ArrayList;
import java.util.List;

import model.Request;

public class Day {
	// Jobs[i][j] : de job die uitgevoegd wordt door machine i op tijdstip j;
	Job[][] jobs;

	boolean parallelwerk;
	boolean nachtshift;
	int overtime;
	
	static int[] lastMaintenanceDayIndex = null;
	
	int[] stock;
	List<Request> shippedToday;

	public Day(int aantalMachines, int aantalBlokkenPerDag, int aantalItems) {
		jobs = new Job[aantalMachines][aantalBlokkenPerDag];
		for(int i=0; i<aantalMachines; i++) {
			for(int j=0; j<aantalBlokkenPerDag; j++) {
				jobs[i][j] = new Idle();
			}
		}
		
		this.parallelwerk = false;
		this.nachtshift = false;
		this.overtime = 0;
		
		stock = new int[aantalItems];
		shippedToday = new ArrayList<Request>();
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
		if(this.shippedToday.isEmpty()) {
			sb.append("-1");
		} else {
			for(int i=0; i<this.shippedToday.size()-1; i++) {
				sb.append(this.shippedToday.get(i).getId() + ";");
			}
			sb.append(this.shippedToday.get(this.shippedToday.size() - 1).getId());
		}
		sb.append("\n");
		sb.append("#Night shift\n");
		if(this.nachtshift) {
			sb.append("1\n");
		} else {
			sb.append("0\n");
		}
		return sb.toString();
	}
	
	public void setStock(int[] stock) {
		this.stock = stock;
	}
	
	public int[] getStock() {
		return this.stock;
	}
}
