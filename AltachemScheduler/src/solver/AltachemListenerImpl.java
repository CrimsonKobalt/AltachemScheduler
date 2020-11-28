package solver;

import solution.Solution;

public class AltachemListenerImpl implements AltachemListener{

	@Override
	public void improved(Solution solution) {
		System.out.print("improved solution found: \n\t\tcost: ");
		System.out.println(solution.getCost());
	}

}
