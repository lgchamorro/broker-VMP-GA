package broker.mo.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import broker.mo.util.LoadInputs;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class CloudBrokerMainScenarioIIv2 {

	final Logger log = Logger.getLogger(getClass());

	public static void main(String[] args) {
		CloudBrokerMainScenarioIIv2 cloudBroker = new CloudBrokerMainScenarioIIv2();
		if (args == null || args.length < 1) {
			cloudBroker.log.error("Missing arguments");
			return;
		}
		cloudBroker.log.debug("BEGIN");
		int runs = Integer.parseInt(args[0]);
		List<Run> solutionsRunned = new ArrayList<Run>();
		List<Individual> solutions;
		for (int r = 0; r < runs; r++) {
			Run run = new Run();
			run.runNo = r;
			solutions = new ArrayList<Individual>();
			cloudBroker.log.info("run: " + r);
			for (int i = 1; i < args.length; i++) {
				NSGA nsga = new NSGA();
				CloudBrokerInstance instance = new CloudBrokerInstance();
				LoadInputs loadInputs = new LoadInputs();
				loadInputs.processInput(args[i]);
				// load problem input
				if (!instance.init(loadInputs.getProviders(),
						loadInputs.getInstanceName(),
						loadInputs.getInstanceCapacity(),
						loadInputs.getPrices(),
						loadInputs.getMigrationStatisticsOverhead(),
						loadInputs.getUserRequirements(),
						loadInputs.getExtraInputs())) {
					cloudBroker.log.error("Initializing parameters");
					return;
				}
				instance.printInput();
				// run NSGA
				nsga.runNSGA(instance, 0);
				// add output for ti
				solutions.add(instance.getOutput());
			}
			// add to run
			run.solutions = solutions;
			solutionsRunned.add(run);
		}
		cloudBroker.log.info("END of excecutions\n\n");
		int count = 0;
		for (Run run : solutionsRunned) {
			printOutputCSV(count++, run.solutions);
		}
	}

	public static void printOutputCSV(int run, List<Individual> solutions) {
		CloudBrokerMain cloudBroker = new CloudBrokerMain();
		CloudBrokerInstance instance = new CloudBrokerInstance();
		//
		int i = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("\n\nRun,t,Population,Generation,TIC,TIC_OVERHEAD,TIP\n");
		for (Individual individual : solutions) {
			sb.append(run + "," + i + ","
					+ instance.getCSVPopulationGenerationTicTip(individual)
					+ "\n");
			i++;
		}
		cloudBroker.log.info(sb.toString());
		//
		i = 0;
		sb = new StringBuilder();
		sb.append("\n\nRun,t,LOC\n");
		for (Individual individual : solutions) {
			sb.append(run + "," + i + "," + instance.getCSVLoc(individual)
					+ "\n");
			i++;
		}
		cloudBroker.log.info(sb.toString());
		//
		i = 0;
		sb = new StringBuilder();
		sb.append("\n\nRun,t,Generation,Population,Provider,Instance,Capacity,Price,Qty,"
				+ "TotalCapacity,MigTime,MigOverhead,TotalPrice\n");
		for (Individual individual : solutions) {
			sb.append(instance.getCSVInstancePerProviderSummary(run, i,
					individual) + "\n");
			i++;
		}
		cloudBroker.log.info(sb.toString());
	}
}
