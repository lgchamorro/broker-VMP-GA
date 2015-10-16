package broker.mo.core;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import broker.mo.util.LoadInputs;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class BorrarCloudBrokerMainScenarioIII {

	final Logger log = Logger.getLogger(getClass());

	public static void main(String[] args) {
		BorrarCloudBrokerMainScenarioIII cloudBroker = new BorrarCloudBrokerMainScenarioIII();
		if (args == null ||

		args.length < 1) {
			cloudBroker.log.error("Missing arguments");
			return;
		}
		cloudBroker.log.debug("BEGIN");
		LoadInputs loadInputs;
		CloudBrokerInstance instance = new CloudBrokerInstance();
		loadInputs = new LoadInputs();
		loadInputs.processInput(args[0]);
		NSGA nsga = new NSGA();
		for (int i = 0; i < 56; i++) {
			// load problem input
			if (!instance.init(loadInputs.getProviders(),
					loadInputs.getInstanceName(),
					loadInputs.getInstanceCapacity(), loadInputs.getPrices(),
					loadInputs.getMigrationStatisticsOverhead(),
					loadInputs.getUserRequirements(),
					loadInputs.getExtraInputs())) {
				cloudBroker.log.error("Initializing parameters");
				return;
			}
			if (i == 0) {
				instance.printInput();
			}
			// run NSGA
			nsga.runNSGA(instance, i);
			// print output for ti
			cloudBroker.log.info("####### t: " + i + " #######");
			instance.printOutput();
			cloudBroker.log.info("###########################");
			loadInputs.getExtraInputs().budget = loadInputs.getExtraInputs().budget
					.add(new BigDecimal(1));
		}
	}
}
