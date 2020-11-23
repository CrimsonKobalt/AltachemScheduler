package runnables;

import examples.Reader;
import model.Problem;
import solution.Idle;
import solution.Job;
import solution.Maintenance;
import solution.Solution;

public class Test {

	public static void main(String[] args) {

		Problem problem = new Problem("src/examples/toy_inst.txt");
		
		Idle idle1 = new Idle();
		Job idle2 = new Idle();
		Maintenance maintenance = new Maintenance();
		
		System.out.println(!idle1.equals(maintenance));
		System.out.println(!idle1.equals(idle2));
		System.out.println(idle2.equals(new Idle()));
		
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
		System.out.println(solution);
	}

}
