package solution;

import java.util.List;

public class Day {
	//Jobs[i][j] : de job die uitgevoegd wordt door machine i op tijdstip j;
	Job[][] jobs;
	
	//machinestates[i][j] = k; machine i in toestand is om k te produceren in block j
	int[][] machinestates;
	
	int daysSinceLastMaintenance;
	
	boolean parallelwerk;
	boolean nachtshift;
	
	int overtime;
}
