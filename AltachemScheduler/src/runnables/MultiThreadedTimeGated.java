package runnables;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Problem;
import solution.Solution;
import solver.AltachemListener;
import solver.AltachemListenerImpl;
import solver.AltachemSolver;
import thesis_logger.ThesisLogger;

public class MultiThreadedTimeGated {
	
	private static final String NEWLINE = System.getProperty("line.separator");

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputFilePath = args[0];
		String outputFilePath = args[1];
		
		try {
			int seed = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			System.err.println("NumberFormatException caught when setting seed. Please enter an integer value.");
			System.exit(1);
		}
		
		final int timeLimitInMinutes = Integer.parseInt(args[3]);
		
		final int maxThreads = Integer.parseInt(args[4]);

		ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);
		System.out.println("Creating solver with time-out=" + timeLimitInMinutes + " minutes & thread-count= "+maxThreads+"...");
		
		Problem problem = new Problem(inputFilePath);

		for(int i=0; i<maxThreads; i++) {
			threadPool.submit(new ProblemThread(problem, new AltachemListenerImpl(), threadPool));
		}
		
		new java.util.Timer().schedule( 
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		            	System.out.println("Time expired. Shutting down threads...");
		                threadPool.shutdownNow();
		                System.exit(0);
		            }
		        }, 
		        //minute * 60 = seconds; seconds *1000 = milliseconds
		        1000*60*timeLimitInMinutes - 5000
		);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public synchronized void run() {
                threadPool.shutdownNow();
				Solution solved = AltachemListenerImpl.getBestSolution();
				System.out.println("Ending program...");
				System.out.println("\tProgram executed " + ProblemThread.getThreadsCount() + " times given " + timeLimitInMinutes + " minutes and " + maxThreads + " threads.");
				System.out.println("Solution report:");
				System.out.println("\tOutput file: " + outputFilePath);
				
				if (solved != null) {
					System.out.println("\tBest solution: " + String.format(Locale.ENGLISH, "%.2f", solved.getCost()));
					solved.write(outputFilePath);
					/*
					try {
						System.out.println(runCommand("java", "-jar", "src/examples/AspValidator.jar", "-i",
								inputFilePath, "-s", outputFilePath));
					} catch (IOException ioe) {
						System.out.println("IOException when trying to validate result...");
					} */
				} else {
					System.err.println("No solution found. Please validate inputs and try again.");
				}
			}
		});
	}
	
	public static String runCommand(String... command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true).directory(null);
        Process process = pb.start();
        StringBuilder result = new StringBuilder(80);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (true) {
                String line = in.readLine();
                if (line == null)
                    break;
                result.append(line).append(NEWLINE);
            }
        }
        return result.toString();
    }                                                                

}

class ProblemThread implements Runnable {
	private static int identifier = 0;
	
	private Problem problem;
	private AltachemListener listener;
	private ExecutorService exec;
	private int id;
	
	public ProblemThread(Problem problem, AltachemListener listener, ExecutorService exec) {
		this.problem = problem;
		this.listener = listener;
		this.exec = exec;
		this.id = ++identifier;
	}

	@Override
	public void run() {
		try {
			AltachemSolver solver = new AltachemSolver(listener);
			ThesisLogger.createLogger("SCHC", this.id);
			Solution solved = solver.solve(problem);
			if(solved.getCost() == 0) {
				System.exit(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			exec.submit(new ProblemThread(problem, new AltachemListenerImpl(), exec));
		}		
	}
	
	public static int getThreadsCount() {
		return ProblemThread.identifier;
	}
}