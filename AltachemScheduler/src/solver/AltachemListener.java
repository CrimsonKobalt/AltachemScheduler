package solver;

import solution.Solution;

public interface AltachemListener {
	
	public void improved(Solution solution);
	
	public void doFinal(Solution solution);
}
