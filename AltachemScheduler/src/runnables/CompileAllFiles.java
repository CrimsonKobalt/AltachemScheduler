package runnables;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Problem;
import solution.OverStockException;
import solution.Solution;
import solver.AltachemListener;
import solver.AltachemListenerImpl;
import solver.AltachemSolver;

public class CompileAllFiles {
	private static final String NEWLINE = System.getProperty("line.separator");

	public static void main(String[] args) {
		
		List<String> instances = new ArrayList<>();
		instances.add("toy_inst.txt");
		instances.add("A_10_10_30.txt");
		instances.add("A_10_10_60.txt");
		instances.add("A_10_15_30.txt");
		instances.add("A_10_15_60.txt");
		instances.add("A_20_15_30.txt");
		instances.add("A_20_15_60.txt");
		instances.add("A_20_25_30.txt");
		instances.add("A_20_25_60.txt");
		instances.add("A_40_100_30.txt");
		instances.add("A_40_100_60.txt");
		
		for(String instanceName: instances) {
			
			String filename = "src/examples/" + instanceName;
			String outputFilename = "src/foundresults/" + instanceName.substring(0, instanceName.length()-4) + "_sol.txt";
			
			Problem problem = new Problem(filename);
			
			AltachemListener listener = new AltachemListenerImpl();
			AltachemSolver solver = new AltachemSolver(listener);
			
			Solution solution = solver.solve(problem);
			
			System.out.println("Done solving... finishing up main-class & printing output-file...");
			if(solution == null) {
				System.out.println("null returned from solver...");
			}
			
			try {
				solution.evaluate();
			} catch (OverStockException e) {
				e.printStackTrace();
				System.out.println("Error evaluating function: overstockException caught: this should never be able to be thrown though.");
			}
			
			solution.printSchedule();
			
			solution.write(outputFilename);
			
			try {
	            System.out.println(run("java", "-jar", "src/examples/AspValidator.jar", "-i", filename, "-s", outputFilename));
	
			}catch (IOException ioException){
	            System.out.println("An error occurred.");
	            ioException.printStackTrace();
			}
		}
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
