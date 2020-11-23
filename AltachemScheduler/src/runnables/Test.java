package runnables;

import examples.Reader;
import model.Problem;
import solution.Solution;

public class Test {

	public static void main(String[] args) {

		Problem problem = new Problem("src/examples/toy_inst.txt");
		
		System.out.println();
		problem.printShippingDays();
		System.out.println();
		problem.printRequestedItems();
		System.out.println();
		System.out.println();
		problem.printMachineInputs();
		System.out.println();
		problem.printItemEfficiencies();
		
		Solution solution = Solution.CreateInitialSolution(problem);
	}

}
