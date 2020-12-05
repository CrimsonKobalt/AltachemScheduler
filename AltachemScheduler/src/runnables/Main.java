package runnables;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import model.Problem;
import solution.OverStockException;
import solution.Solution;
import solver.AltachemListener;
import solver.AltachemListenerImpl;
import solver.AltachemSolver;

public class Main {
	
	private static final String NEWLINE = System.getProperty("line.separator");

	public static void main(String[] args) {
		
		String instanceName = "toy_inst.txt";
		String filename = "src/examples/" + instanceName;
		String outputFilename = "src/foundresults/" + instanceName.substring(0, instanceName.length()-4) + "_sol.txt";

		Problem problem = new Problem(filename);

		//solveAndValidate(problem, filename, outputFilename);
		analyze(problem, filename, outputFilename, 5);

	}

	public static double solveAndValidate(Problem problem, String filename, String outputFilename){

		AltachemListener listener = new AltachemListenerImpl();
		AltachemSolver solver = new AltachemSolver(listener);

		Solution solution = solver.solve(problem);

		System.out.println("Done solving... finishing up main-class & printing output-file...");
		if(solution == null) {
			System.out.println("null returned from solver...");
		}

		double result = 0;
		try {
			result = solution.evaluate();
		} catch (OverStockException e) {
			e.printStackTrace();
			System.out.println("Error evaluating function: overstockException caught: this should never be able to be thrown though.");
		}

		//solution.printSchedule();

		solution.write(outputFilename);

		try{
			System.out.println(run("java", "-jar", "src/examples/AspValidator.jar", "-i", filename, "-s", outputFilename));
			return result;
		}catch (IOException ioException){
			System.out.println("An error occurred.");
			ioException.printStackTrace();
		}

		return -1;
	}

	public static void analyze(Problem problem, String filename, String outputFilename, int count){

		//voer meerdere keren uit + onthoud costs en times
		ArrayList<Long> times = new ArrayList<>();
		ArrayList<Double> costs = new ArrayList<>();
		for (int i = 0; i<count; i++){
			int attempt = i+1;
			System.out.println("Attempt " + attempt);
			long startTime = System.currentTimeMillis();
			costs.add(solveAndValidate(problem, filename, outputFilename));
			long endTime = System.currentTimeMillis();
			times.add(endTime-startTime);
		}

		Collections.sort(times);
		OptionalDouble timeAverage = times.stream().mapToDouble(a -> a).average();
		if (timeAverage.isPresent())
			System.out.println("Average Time: " + timeAverage.getAsDouble()/1000/60 + " min");
		System.out.println("Min Time: " + times.get(0)/1000/60 + " min");
		System.out.println("Max Time: " + times.get(times.size()-1)/1000/60 + " min");

		Collections.sort(costs);
		OptionalDouble costAverage = costs.stream().mapToDouble(a -> a).average();
		if (costAverage.isPresent())
			System.out.println("Average Cost: " + costAverage.getAsDouble());
		System.out.println("Min Cost: " + costs.get(0));
		System.out.println("Max Cost: " + costs.get(costs.size()-1));
	}
	
	public static String run(String... command) throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true).directory(new File(System.getProperty("user.home") + "/git/AltachemSchedulerProposal/AltachemScheduler"));
        System.out.println(pb.directory());
        Process process = pb.start();
        StringBuilder result = new StringBuilder(80);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            while (true)
            {
                String line = in.readLine();
                if (line == null)
                    break;
                result.append(line).append(NEWLINE);
            }
        }
        return result.toString();
    }

}
