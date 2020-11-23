package runnables;

import model.Problem;
import solution.Idle;
import solution.Job;
import solution.Maintenance;
import solution.Production;
import solution.Solution;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {

	public static void main(String[] args) {

        List<String> filenames = new ArrayList<>();
        filenames.add("src/examples/toy_inst.txt");
        filenames.add("src/examples/A_10_10_30.txt");
        filenames.add("src/examples/A_10_10_60.txt");
        filenames.add("src/examples/A_10_15_30.txt");
        filenames.add("src/examples/A_10_15_60.txt");
        filenames.add("src/examples/A_20_15_30.txt");
        filenames.add("src/examples/A_20_15_60.txt");
        filenames.add("src/examples/A_20_25_30.txt");
        filenames.add("src/examples/A_20_25_60.txt");
        filenames.add("src/examples/A_40_100_30.txt");
        filenames.add("src/examples/A_40_100_60.txt");

        for (String filename : filenames){
            String outputFilename = filename.substring(0, filename.length()-4) + "_sol.txt";
            Problem problem = new Problem(filename);
            Solution solution = Solution.CreateInitialSolution(problem);
            solution.write(outputFilename);

            // check with validator
            try{
                    Process proc = Runtime.getRuntime().exec("java -jar src/examples/AspValidator.jar -i " + filename + " -s " + outputFilename + " -l src/examples/validatorOutput.txt");

                    System.out.println("Validator Output for file: " + filename);

                    // Read the output from the command
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                            System.out.println(s);
                    }

                    // Read any errors from the attempted command
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                    while ((s = stdError.readLine()) != null) {
                            System.out.println(s);
                    }

                    System.out.println();

            }catch (IOException ioException){
                    System.out.println("An error occurred.");
                    ioException.printStackTrace();
            }

        }

	}

}
