package thesis_logger;

import java.util.ArrayList;
import java.util.List;

public class ThesisLogger {
	private static boolean wasLogged;
	
	private static String MHName;
	private static int runId;
	private static List<String> MHparam_names;
	private static List<Integer> MHparam_vals;
	
	private static List<Double> objVals_curr;
	private static List<Double> objVals_best; 
	private static List<Boolean[]> objVals_info;
	
	private static List<Double> operator_timings;
	private static List<Double> evaluation_timings;
	private static List<Double> total_step_timings;
	
	private static List<String> operator_names;
	private static List<int[]> operator_params;

	// current thresholds
	private static List<Double> current_thresholds;
	
	public static void writeLogger() {
		System.out.println("Write this logger to a textfile");
	}
	
	public static void createLogger(String name, int id){
		if(!wasLogged){
			System.out.println("Writing out logger to a default path");
			writeLogger();
		}
		
		MHName = name;
		runId = id;
		wasLogged = false;
		MHparam_names = new ArrayList<>();
		MHparam_vals = new ArrayList<>();
		
		System.out.println("Initialising "+ MHName +"-logger for run " + runId);
		
		objVals_curr = new ArrayList<>();
		objVals_best = new ArrayList<>();
		objVals_info = new ArrayList<>();
		
		operator_timings = new ArrayList<>();
		evaluation_timings = new ArrayList<>();
		total_step_timings = new ArrayList<>();
		
		operator_names = new ArrayList<>();
		operator_params = new ArrayList<>();
		
		current_thresholds = new ArrayList<>();
	}
	
	public static void logOperation(String opname, double optiming, int...params) {
		operator_names.add(opname);
		operator_params.add(params);
		operator_timings.add(optiming);
	}
	
	public static void logEvaluation(double currentval, double evaltiming) {
		objVals_curr.add(currentval);
		evaluation_timings.add(evaltiming);
	}
	
	public static void logAcceptance(double currentbest, double currentthreshold, boolean isAccepted, boolean isImproving, boolean isFeasible) {
		objVals_best.add(currentbest);
		current_thresholds.add(currentthreshold);
		
		Boolean[] valInfo = {isAccepted, isImproving, isFeasible};
		objVals_info.add(valInfo);
	}
	
	public static void addMHparam(String name, int val) {
		MHparam_names.add(name);
		MHparam_vals.add(val);
	}
	
	public static void logStepTime(double steptime) {
		total_step_timings.add(steptime);
	}
	
	public static String getJSONString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("\n\t\"metaheuristic-name\": " + MHName + ",");
		if(!MHparam_names.isEmpty()) {
			sb.append("\n\t\"algo-params\": [");
			for(int i = 0; i < MHparam_names.size(); i++) {
				sb.append("{\n\t\t\""+ MHparam_names.get(i) + "\": " + MHparam_vals.get(i) + "}, ");
			}
			sb.append("],");
		}
		sb.append("\n\t\"run-id\": "+ runId + ",");
		sb.append("\n\t\"current-objvals\": [");
		for(double val : objVals_curr) {
			sb.append("\n\t\t" + val + ",");
		}
		//remove last ","
		sb.setLength(sb.length() - 1);
		sb.append("],");
		
		return sb.toString();
	}
}
