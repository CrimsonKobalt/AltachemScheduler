package runnables;

import java.io.File;

import model.Problem;
import solution.OverStockException;
import solution.Solution;
import solver.AltachemListener;
import solver.AltachemListenerImpl;
import solver.AltachemSolver;

public class Main {

	public static void main(String[] args) {
		
		String instanceName = "toy_inst.txt";
		String filename = "src/examples/" + instanceName;
		String outputFilename = "src/foundresults/" + instanceName.substring(0, instanceName.length()-4) + "_sol.txt";
		
		Problem problem = new Problem(filename);
		
		AltachemListener listener = new AltachemListenerImpl();
		AltachemSolver solver = new AltachemSolver(listener);
		
		Solution solution = solver.solve(problem);
		
		System.out.println("Done solving... finishing up main-class & printing output-file...");
		if(solution == null) {
			return;
		}
		
		try {
			solution.evaluate();
		} catch (OverStockException e) {
			e.printStackTrace();
			System.out.println("Error evaluating function: overstockException caught: this should never be able to be thrown though.");
		}
		
		solution.write(outputFilename);
	}

}
