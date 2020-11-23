package runnables;

import examples.Reader;
import model.Problem;
import solution.Idle;
import solution.Job;
import solution.Maintenance;
import solution.Production;
import solution.Solution;

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
            Problem problem = new Problem(filename);
            Solution solution = Solution.CreateInitialSolution(problem);
            solution.write(filename.substring(0, filename.length()-4) + "_sol.txt");
        }
	}

}
