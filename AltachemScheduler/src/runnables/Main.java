package runnables;

import java.io.File;

import model.Problem;
import solution.Solution;
import solver.AltachemListener;
import solver.AltachemListenerImpl;
import solver.AltachemSolver;

public class Main {

	public static void main(String[] args) {
		
		String filename = "src/examples/toy_inst.txt";
		String outputFilename = filename.substring(0, filename.length()-4) + "_sol.txt";
		
		Problem problem = new Problem(filename);
		
		AltachemListener listener = new AltachemListenerImpl();
		AltachemSolver solver = new AltachemSolver(listener);
		
		Solution solution = solver.solve(problem);
		
		solution.write(outputFilename);
	}

}
