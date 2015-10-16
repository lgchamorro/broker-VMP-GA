package broker.mo.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class LoadInputs {

	final Logger log = Logger.getLogger(getClass());

	List<CloudProvider> cloudProviders;
	List<InstanceType> instanceTypes;
	List<MigrationStatistics> migrationStatistics;
	ExtraInputs extraInputs;

	public void processInput(String filePath) {
		List<String> file = this.readFile(filePath);
		// load providers
		cloudProviders = loadCloudProviders(file);
		// load instances types
		instanceTypes = loadInstanceTypesAndPrices(file, cloudProviders);
		// load overhead
		migrationStatistics = loadMigrationOverhead(file, cloudProviders,
				instanceTypes);
		// load aditional inputs
		extraInputs = loadAditionalInputs(file);
	}

	private List<CloudProvider> loadCloudProviders(List<String> file) {
		log.debug("Loading providers..");
		List<CloudProvider> providers = new ArrayList<CloudProvider>();
		CloudProvider cp;
		// k index for cloud, begin in 0
		int index = 0;
		for (String line : file) {
			if (line.trim().contains(DCSConstants.PROVIDERS_BEGIN)) {
				continue;
			}
			if (line.trim().contains(DCSConstants.PROVIDERS_END)) {
				break;
			}
			if (line != null && line.trim().length() > 0) {
				cp = new CloudProvider(index++, line.trim());
				log.debug("Adding " + cp);
				providers.add(cp);
			}
		}
		log.debug(providers.size() + " providers loaded!");
		return providers;
	}

	private List<InstanceType> loadInstanceTypesAndPrices(List<String> file,
			List<CloudProvider> cloudProviders) {
		log.debug("Loading instancesTypes..");
		int instanceIndex = 0;
		List<InstanceType> instanceTypeList = new ArrayList<InstanceType>();
		InstanceType instanceType;
		String[] split;
		CloudProvider provider;
		int providerIndex = 0;
		boolean readingInstances = false;
		boolean readingPrices = false;
		for (String line : file) {
			if (line.trim().contains(DCSConstants.INSTANCE_TYPES_BEGIN)) {
				readingInstances = true;
				continue;
			}
			if (line.trim().contains(DCSConstants.INSTANCE_TYPES_END)) {
				break;
			}
			if (line != null && line.trim().length() > 0 && readingInstances) {
				split = line.split("\\s+");
				if (split.length < 2) {
					continue;
				}
				// load labels
				if (split[1].trim().contains(DCSConstants.LABEL)) {
					for (int i = 2; i < split.length; i++) {
						instanceType = new InstanceType(instanceIndex++,
								split[i]);
						instanceTypeList.add(instanceType);
					}
					continue;
				}
				// load capacity
				if (split[1].trim().contains(DCSConstants.CAPACITY)) {
					for (int i = 2; i < split.length; i++) {
						instanceType = instanceTypeList.get(i - 2);
						instanceType.computingCapacity = Integer
								.parseInt(split[i]);
						log.debug("Adding " + instanceType);
					}
					continue;
				}
				// load prices per cloud
				if (line.trim().contains(DCSConstants.PRICES)) {
					readingPrices = true;
					continue;
				}
				if (readingPrices) {
					String providerName = split[1];
					provider = cloudProviders.get(providerIndex++);
					if (!providerName.equals(provider.name)) {
						log.error("Provider expected: " + provider.name
								+ ", provider found: " + providerName);
						return null;
					}
					for (int i = 2; i < split.length; i++) {
						instanceType = instanceTypeList.get(i - 2);
						if (!split[i].contains(DCSConstants.NOT_APPLICABLE)) {
							BigDecimal price = new BigDecimal(split[i].trim());
							log.debug("Price: " + price + ", " + instanceType
									+ ", " + provider);
							instanceType.cloudAndPrices.put(provider, price);
						} else {
							log.warn(instanceType + " N/A for provider "
									+ provider);
						}
					}
				}
			}
		}
		log.debug(instanceTypeList.size() + " instances types loaded!");
		return instanceTypeList;
	}

	private List<MigrationStatistics> loadMigrationOverhead(List<String> file,
			List<CloudProvider> cloudProviders, List<InstanceType> instanceTypes) {
		log.debug("Loading migration overhead..");
		List<MigrationStatistics> overheadList = new ArrayList<MigrationStatistics>();
		MigrationStatistics migrationOverhead;
		InstanceType instanceType;
		CloudProvider provider;
		boolean readingOverhead = false;
		String[] split;
		Iterator<String> it = file.iterator();
		String line;
		String[] allocLine;
		String[] deAllocLine;
		int providerIndex = 0;
		while (it.hasNext()) {
			line = it.next();
			if (line.trim().contains(DCSConstants.OVERHEAD_BEGIN)) {
				readingOverhead = true;
				continue;
			}
			if (line.trim().contains(DCSConstants.OVERHEAD_END)) {
				break;
			}
			if (line != null && line.trim().length() > 0 && readingOverhead) {
				split = line.split("\\s+");
				if (split.length < 2) {
					continue;
				}
				// labels
				if (split[1].trim().contains(DCSConstants.LABEL)) {
					// validate IT position
				}
				// overhead by cloud provider
				if (split.length == 2) {
					String providerName = split[1];
					provider = cloudProviders.get(providerIndex++);
					if (!providerName.equals(provider.name)) {
						log.error("Provider expected: " + provider.name
								+ ", provider found: " + providerName);
						return null;
					}
					allocLine = it.next().split("\\s+");
					deAllocLine = it.next().split("\\s+");
					for (int i = 2; i < allocLine.length; i++) {
						instanceType = instanceTypes.get(i - 2);
						if (!allocLine[i].contains(DCSConstants.NOT_APPLICABLE)) {
							migrationOverhead = new MigrationStatistics();
							migrationOverhead.allocation = new BigDecimal(
									allocLine[i]);
							migrationOverhead.deAllocation = new BigDecimal(
									deAllocLine[i]);
							migrationOverhead.cloudProvider = provider;
							migrationOverhead.instanceType = instanceType;
							log.debug(migrationOverhead);
							overheadList.add(migrationOverhead);
							instanceType.migrationOverhead.put(provider,
									migrationOverhead);
						} else {
							log.warn(instanceType
									+ " overhead N/A for provider " + provider);
						}
					}
				}
			}
		}
		log.debug(overheadList.size() + " migration overhead added!");
		return overheadList;
	}

	private ExtraInputs loadAditionalInputs(List<String> file) {
		log.debug("Loading constraints..");
		int index = 0;
		boolean readingConstraints = false;
		ExtraInputs adInputs = new ExtraInputs();
		for (String line : file) {
			if (line.trim().contains(DCSConstants.RESTRICTION_BEGIN)) {
				readingConstraints = true;
				continue;
			}
			if (line.trim().contains(DCSConstants.RESTRICTION_END)) {
				break;
			}
			if (!readingConstraints) {
				continue;
			}
			String[] splitAux = line.split("\\s+");
			if (splitAux.length > 1) {
				String[] split = splitAux[1].split("=");
				if (split[0].contains(DCSConstants.VIRTUAL_MACHINES_TO_DEPLOY)) {
					adInputs.virtualMachineToDeploy = new BigDecimal(split[1]);
					log.debug(DCSConstants.VIRTUAL_MACHINES_TO_DEPLOY + ": "
							+ adInputs.virtualMachineToDeploy);
					index++;
				}
				if (split[0].contains(DCSConstants.CAPACITY_THRESHOLD)) {
					adInputs.capacityThreshold = new BigDecimal(split[1]);
					log.debug(DCSConstants.CAPACITY_THRESHOLD + ": "
							+ adInputs.capacityThreshold);
					index++;
				}
				if (split[0].contains(DCSConstants.BUDGET)) {
					adInputs.budget = new BigDecimal(split[1]);
					log.debug(DCSConstants.BUDGET + ": " + adInputs.budget);
					index++;
				}
				if (split[0].contains(DCSConstants.LOAD_BALANCE_MIN)) {
					adInputs.loadBalanceMin = new BigDecimal(split[1]);
					log.debug(DCSConstants.LOAD_BALANCE_MIN + ": "
							+ adInputs.loadBalanceMin);
					index++;
				}
				if (split[0].contains(DCSConstants.LOAD_BALANCE_MAX)) {
					adInputs.loadBalanceMax = new BigDecimal(split[1]);
					log.debug(DCSConstants.LOAD_BALANCE_MAX + ": "
							+ adInputs.loadBalanceMax);
					index++;
				}
				if (split[0].contains(DCSConstants.MAX_REPAIR)) {
					adInputs.maxRepair = new Integer(split[1]);
					log.debug(DCSConstants.MAX_REPAIR + ": "
							+ adInputs.maxRepair);
					index++;
				}
				if (split[0].contains(DCSConstants.SCENARIO)) {
					adInputs.scenario = Integer.parseInt(split[1]);
					log.debug(DCSConstants.SCENARIO + ": " + adInputs.scenario);
					index++;
				}
				if (split[0].contains(DCSConstants.PRINT_OVERHEAD)) {
					adInputs.printOverhead = split[1]
							.contains(DCSConstants.TRUE);
					log.debug(DCSConstants.PRINT_OVERHEAD + ": "
							+ adInputs.printOverhead);
					index++;
				}
			}
		}
		log.debug(index + " constraints loaded!");
		//
		log.debug("Loading aditional inputs");
		index = 0;
		boolean readingFunction = false;
		for (String line : file) {
			if (line.trim().contains(DCSConstants.FUNCTION_BEGIN)) {
				readingFunction = true;
				continue;
			}
			if (line.trim().contains(DCSConstants.FUNCTION_END)) {
				break;
			}
			if (!readingFunction) {
				continue;
			}
			String[] splitAux = line.split("\\s+");
			if (splitAux.length > 1) {
				String[] split = splitAux[1].split("=");
				if (split[0].contains(DCSConstants.POPULATION)) {
					adInputs.population = new BigDecimal(split[1]);
					log.debug(DCSConstants.POPULATION + ": "
							+ adInputs.population);
					index++;
				}
				if (split[0].contains(DCSConstants.GENERATIONS)) {
					adInputs.generations = new BigDecimal(split[1]);
					log.debug(DCSConstants.GENERATIONS + ": "
							+ adInputs.generations);
					index++;
				}
				if (split[0].contains(DCSConstants.OPTIMIZE_TIC)) {
					adInputs.optimizeTIC = split[1].contains(DCSConstants.TRUE);
					log.debug(DCSConstants.OPTIMIZE_TIC + ": "
							+ adInputs.optimizeTIC);
					index++;
				}
				if (split[0].contains(DCSConstants.OPTIMIZE_TIP)) {
					adInputs.optimizeTIP = split[1].contains(DCSConstants.TRUE);
					log.debug(DCSConstants.OPTIMIZE_TIP + ": "
							+ adInputs.optimizeTIP);
					index++;
				}
				if (split[0].contains(DCSConstants.OPTIMIZE_MC)) {
					adInputs.optimizeMC = split[1].contains(DCSConstants.TRUE);
					log.debug(DCSConstants.OPTIMIZE_MC + ": "
							+ adInputs.optimizeMC);
					index++;
				}
			}
		}
		log.debug("Aditional inputs loaded!");
		return adInputs;
	}

	private List<String> readFile(String filePath) {
		BufferedReader br = null;
		String lineStr = "";
		List<String> fileReaded = new ArrayList<String>();
		try {
			log.info("Reading [" + filePath + "]");
			br = new BufferedReader(new FileReader(filePath));
			int rowCount = 0;
			while ((lineStr = br.readLine()) != null) {
				rowCount++;
				fileReaded.add(lineStr);
			}
			log.info(rowCount + " lines readed");
		} catch (Exception e) {
			log.error("Reading file", e);
			return null;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("Closing file", e);
					return null;
				}
			}
		}
		return fileReaded;
	}

	// **********************************************************
	public String[] getProviders() {
		String[] providers = new String[cloudProviders.size()];
		for (CloudProvider cp : cloudProviders) {
			providers[cp.k] = cp.name;
		}
		return providers;
	}

	public String[] getInstanceName() {
		String[] instanceName = new String[instanceTypes.size()];
		for (InstanceType instanceType : instanceTypes) {
			instanceName[instanceType.j] = instanceType.label;
		}
		return instanceName;
	}

	public int[] getInstanceCapacity() {
		int[] instanceCapacity = new int[instanceTypes.size()];
		for (InstanceType instanceType : instanceTypes) {
			instanceCapacity[instanceType.j] = instanceType.computingCapacity;
		}
		return instanceCapacity;
	}

	public BigDecimal[][] getPrices() {
		int l = instanceTypes.size();
		int k = cloudProviders.size();
		BigDecimal[][] prices = new BigDecimal[k][l];
		for (CloudProvider cp : cloudProviders) {
			for (InstanceType instanceType : instanceTypes) {
				prices[cp.k][instanceType.j] = instanceType
						.getPricePerProvider(cp);
			}
		}
		return prices;
	}

	public BigDecimal[][] getMigrationStatisticsOverhead() {
		int l = instanceTypes.size();
		int k = cloudProviders.size();
		BigDecimal[][] migrationSatisticsOverhead = new BigDecimal[k * 2][l];
		for (int i = 0; i < k * 2; i++) {
			for (int j = 0; j < l; j++) {
				migrationSatisticsOverhead[i][j] = new BigDecimal(-1);
			}
		}
		for (MigrationStatistics m : migrationStatistics) {
			migrationSatisticsOverhead[m.cloudProvider.k * 2][m.instanceType.j] = m.allocation;
			migrationSatisticsOverhead[m.cloudProvider.k * 2 + 1][m.instanceType.j] = m.deAllocation;
		}
		return migrationSatisticsOverhead;
	}

	public BigDecimal[] getUserRequirements() {
		BigDecimal[] clientRequirements = new BigDecimal[4];
		int index = 0;
		clientRequirements[index++] = extraInputs.virtualMachineToDeploy;
		clientRequirements[index++] = extraInputs.budget;
		clientRequirements[index++] = extraInputs.loadBalanceMin;
		clientRequirements[index++] = extraInputs.capacityThreshold;
		return clientRequirements;
	}

	public ExtraInputs getExtraInputs() {
		return extraInputs;
	}

}
