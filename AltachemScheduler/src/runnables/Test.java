package runnables;

import examples.Reader;
import model.Problem;
import solution.Idle;
import solution.Job;
import solution.Maintenance;
import solution.Production;
import solution.Solution;

public class Test {

	public static void main(String[] args) {

		Problem problem = new Problem("src/examples/toy_inst.txt");
		
		Solution solution = Solution.CreateInitialSolution(problem);
		System.out.println(solution);
	}

}
