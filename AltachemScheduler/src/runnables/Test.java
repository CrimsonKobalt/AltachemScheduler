package runnables;

import model.Problem;
import solution.Evaluation;
import solution.Idle;
import solution.Job;
import solution.Maintenance;
import solution.OverStockException;
import solution.Production;
import solution.Solution;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {
	
	private static final String NEWLINE = System.getProperty("line.separator");

	public static void main(String[] args) {

        List<String> filenames = new ArrayList<>();
        filenames.add("toy_inst.txt");
        filenames.add("A_10_10_30.txt");
        filenames.add("A_10_10_60.txt");
        filenames.add("A_10_15_30.txt");
        filenames.add("A_10_15_60.txt");
        filenames.add("A_20_15_30.txt");
        filenames.add("A_20_15_60.txt");
        filenames.add("A_20_25_30.txt");
        filenames.add("A_20_25_60.txt");
        filenames.add("A_40_100_30.txt");
        filenames.add("A_40_100_60.txt");

        for (String filename : filenames){
        	String filepath = "src/examples/" + filename;
            String outputFilename = "src/foundresults/"+ filename.substring(0, filename.length()-4) + "_sol.txt";
            Problem problem = new Problem(filepath);
            Evaluation.configureEvaluation(problem);
            Solution solution = Solution.CreateInitialSolution(problem);
            try {
				solution.evaluate();
			} catch (OverStockException e) {
				e.printStackTrace();
			}
            
            solution.write(outputFilename);

            // check with validator
            try{
                    System.out.println(run("java", "-jar", "src/examples/AspValidator.jar", "-i", filepath, "-s", outputFilename));
                    // + " -l src/examples/validatorOutput.txt"

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