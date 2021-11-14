package thesis_logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	
	// String representation
	private static String jsonrep;
	
	public static void createLogger(String name, int id){	
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
		
		jsonrep = null;
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
	
	public static boolean printJSONFile(String name) {
		File dir = new File("logs");
		if(!dir.isDirectory()) {
			dir.mkdir();
		}
		File file = new File("logs\\" + name);
		System.out.println("printing file to " + file.getAbsolutePath());
		try (BufferedWriter wr = new BufferedWriter(new FileWriter(file))) {
			if(jsonrep == null) {
				jsonrep = getJSONString();
			}
			wr.write(jsonrep);
		} catch (IOException e) {
			return wasLogged;
		}
		wasLogged = true;
		return wasLogged;
	}
	
	public static boolean printJSONFile() {
		return printJSONFile(MHName + "_" + runId + ".json");
	}
	
	public static String getJSONString() {
		StringBuilder sb = new StringBuilder("{");
		
		//Metaheuristic Name
		sb.append("\n\t\"metaheuristic-name\": " + MHName + ",");
		
		//Metaheuristic params
		if(!MHparam_names.isEmpty()) {
			sb.append("\n\t\"algo-params\": {");
			for(int i = 0; i < MHparam_names.size(); i++) {
				sb.append("\n\t\t\""+ MHparam_names.get(i) + "\": " + MHparam_vals.get(i) + ",");
			}
			//remove last ","
			sb.setLength(sb.length() - 1);
			sb.append("},");
		}
		
		//run id
		sb.append("\n\t\"run-id\": "+ runId + ",");
		
		//current objvals
		sb.append("\n\t\"current-objvals\": [");
		for(double val : objVals_curr) {
			sb.append("\n\t\t" + val + ",");
		}
		//remove last ","
		sb.setLength(sb.length() - 1);
		sb.append("],");
		
		//best-solution-values
		sb.append("\n\t\"best-solution-values\": [");
		for(double val : objVals_best) {
			sb.append("\n\t\t" + val + ",");
		}
		//remove last ","
		sb.setLength(sb.length() - 1);
		sb.append("],");
		
		//objval-info
		sb.append("\n\t\"objval-info\": [");
		for(Boolean[] val : objVals_info) {
			sb.append("\n\t\t[");
			for(boolean infoval : val) {
				sb.append(infoval + ", ");
			}
			//remove last ","
			sb.setLength(sb.length() - 2);
			sb.append("],");
		}
		//remove last ","
		sb.setLength(sb.length() - 1);
		sb.append("],");
		
		
		//operator timings
		sb.append("\n\t\"operator-timings\": [");
		for(double val : operator_timings) {
			sb.append("\n\t\t" + val + ",");
		}
		//remove last ","
		sb.setLength(sb.length() - 1);
		sb.append("],");
		
		//evaluation timings
		sb.append("\n\t\"evaluation-timings\": [");
		for(double val : evaluation_timings) {
			sb.append("\n\t\t" + val + ",");
		}
		//remove last ","
		sb.setLength(sb.length() - 1);
		sb.append("],");
		
		//total-step-timings
		sb.append("\n\t\"total-step-timings\": [");
		for(double val : total_step_timings) {
			sb.append("\n\t\t" + val + ",");
		}
		//remove last ","
		sb.setLength(sb.length() - 1);
		sb.append("],");
		
		//operator-names
		sb.append("\n\t\"operator-names\": [");
		for(String val : operator_names) {
			sb.append("\n\t\t" + val + ",");
		}
		//remove last ","
		sb.setLength(sb.length() - 1);
		sb.append("],");
		
		//operator-params
		sb.append("\n\t\"operator-params\": [");
		for(int[] val : operator_params) {
			sb.append("\n\t\t[");
			for(int infoval : val) {
				sb.append(infoval + ", ");
			}
			//remove last ","
			sb.setLength(sb.length() - 2);
			sb.append("],");
		}
		//remove last ","
		sb.setLength(sb.length() - 1);
		sb.append("],");
		
		//operator-names
		sb.append("\n\t\"current-thresholds\": [");
		for(double val : current_thresholds) {
			sb.append("\n\t\t" + val + ",");
		}
		//remove last ","
		sb.setLength(sb.length() - 1);
		sb.append("]");
		
		
		//close json-object
		sb.append("\n}");
		return sb.toString();
	}
}
