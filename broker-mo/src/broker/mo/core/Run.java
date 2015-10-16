package broker.mo.core;

import java.util.List;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class Run {

	int runNo;
	List<Individual> solutions;

	public Run() {
		super();
	}

	public Run(int run, List<Individual> solutions) {
		super();
		this.runNo = run;
		this.solutions = solutions;
	}

}
