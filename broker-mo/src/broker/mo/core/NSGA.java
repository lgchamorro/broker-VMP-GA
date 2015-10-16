package broker.mo.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import broker.mo.util.DCSUtil;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class NSGA {

	private final Logger log = Logger.getLogger(getClass());

	CloudBrokerInstance instance;
	List<Individual> knownFeasibleSolution;
	/*
	 * Best solution
	 */
	Individual pc;

	void runNSGA(CloudBrokerInstance instance, int run) {
		boolean optimizeTIC = instance.extraInputs.optimizeTIC;
		boolean optimizeTIP = instance.extraInputs.optimizeTIP;
		boolean optimizeMC = instance.extraInputs.optimizeMC;
		/*
		 * almacenar solucion anterior para comparar con la solucion actual
		 */
		if (run > 0) {
			instance.prevSolution = instance.outputSolution;
		}
		knownFeasibleSolution = new ArrayList<Individual>();
		int n = instance.extraInputs.virtualMachineToDeploy.intValue();
		int l = instance.instanceName.length;
		int m = instance.providers.length;
		this.instance = instance;
		int populationCount = instance.extraInputs.population.intValue();
		int generations = instance.extraInputs.generations.intValue();
		boolean isFirstGeneration = true;
		// first generation
		List<Individual> initialPopulation = this.getFirstGeneration(
				populationCount, n, l, m);
		List<Individual> initialPopulationRepaired = null;
		/*
		 * La poblacion es reparada segun la funcion objetivo a optimizar
		 */
		if (optimizeTIC) {
			instance.setPricePerCapacity();
			initialPopulationRepaired = this.repairPopulationOptimizeTIC(
					initialPopulation, populationCount, n, l, m,
					isFirstGeneration);
		} else if (optimizeTIP) {
			instance.setCapacityPerPrice();
			initialPopulationRepaired = this.repairPopulationOptimizeTIP(
					initialPopulation, populationCount, n, l, m,
					isFirstGeneration);
		} else if (optimizeMC) {
			instance.setOverheadIndex();
			initialPopulationRepaired = this.repairPopulationOptimizeMC(
					initialPopulation, populationCount, n, l, m,
					isFirstGeneration);
		}
		if (initialPopulationRepaired == null) {
			log.error("Population can't be repaired.. end");
			instance.solutionFound = false;
			return;
		}
		//
		if (optimizeTIC) {
			pc = getBestIndividualTIC(initialPopulationRepaired);
		} else if (optimizeTIP) {
			pc = getBestIndividualTIP(initialPopulationRepaired);
		} else if (optimizeMC) {
			pc = getBestIndividualMC(initialPopulationRepaired);
		}
		int t = 0;
		List<Individual> pt = initialPopulationRepaired;
		List<Individual> qt1 = null;
		List<Individual> qt2 = null;
		int generationCount = 0;
		isFirstGeneration = false;
		while (t < generations) {
			log.debug("*** Generation: [" + (generationCount++) + "] ***");
			qt1 = crossOver(pt);
			qt1 = mutate(qt1);
			/*
			 * La poblacion es reparada segun la funcion objetivo a optimizar
			 */
			if (optimizeTIC) {
				qt2 = repairPopulationOptimizeTIC(qt1, populationCount, n, l,
						m, isFirstGeneration);
			} else if (optimizeTIP) {
				qt2 = repairPopulationOptimizeTIP(qt1, populationCount, n, l,
						m, isFirstGeneration);
			} else if (optimizeMC) {
				qt2 = repairPopulationOptimizeMC(qt1, populationCount, n, l, m,
						isFirstGeneration);
			}
			if (qt2 == null) {
				log.error("Population can't be repaired.. end");
				instance.solutionFound = false;
				return;
			}
			qt2.add(pc);// Pt U Pc
			if (optimizeTIC) {
				pc = getBestIndividualTIC(qt2);
				pt = discardWorstSolutionTIC(qt2);
			} else if (optimizeTIP) {
				pc = getBestIndividualTIP(qt2);
				pt = discardWorstSolutionTIP(qt2);
			} else if (optimizeMC) {
				pc = getBestIndividualMC(qt2);
				pt = discardWorstSolutionMC(qt2);
			}
			t++;
			System.gc();
		}
		instance.solutionFound = true;
		instance.outputSolution = pc;
	}

	Individual getBestIndividualTIC(List<Individual> population) {
		Individual best = null;
		BigDecimal ticMax = new BigDecimal(0);
		BigDecimal ticAux;
		for (Individual i : population) {
			ticAux = i.ticWithOverhead;
			if (ticMax.compareTo(ticAux) < 0) {
				best = i;
				ticMax = ticAux;
			} else if (ticMax.compareTo(ticAux) == 0
					&& best.tip.compareTo(i.tip) > 0) {
				// si ambos tic son iguales, se comprara por TIP, si i tiene
				// menor TIP es mejor
				best = i;
				ticMax = ticAux;
			}
		}
		log.debug("Getting best individual: " + best);
		return best;
	}

	Individual getBestIndividualTIP(List<Individual> population) {
		log.debug("Getting best individual");
		Individual best = null;
		BigDecimal tipMin = new BigDecimal(999999);
		BigDecimal tipAux;
		for (Individual i : population) {
			tipAux = i.tip;
			if (tipMin.compareTo(tipAux) > 0) {
				best = i;
				tipMin = tipAux;
			} else if (tipMin.compareTo(tipAux) == 0 && best.tic < i.tic) {
				// si ambos tip son iguales, se comprara por TIC, si i tiene
				// mayor TIC es mejor
				best = i;
				tipMin = tipAux;
			}
		}
		return best;
	}

	Individual getBestIndividualMC(List<Individual> population) {
		log.debug("Getting best individual");
		Individual best = null;
		BigDecimal mcMin = new BigDecimal(999999);
		BigDecimal mcAux;
		for (Individual i : population) {
			mcAux = i.mc;
			if (mcMin.compareTo(mcAux) > 0) {
				best = i;
				mcMin = mcAux;
			} else if (mcMin.compareTo(mcAux) == 0
					&& best.tip.compareTo(i.tip) > 0) {
				// si ambos tic son iguales, se comprara por TIP, si i tiene
				// menor TIP es mejor
				best = i;
				mcMin = mcAux;
			}
		}
		log.debug("best solution " + best);
		return best;
	}

	List<Individual> getFirstGeneration(int populationCount, int n, int l, int m) {
		List<Individual> population = new ArrayList<Individual>();
		Individual individual;
		for (int i = 0; i < populationCount; i++) {
			individual = generateRandomIndividual(populationCount, n, l, m);
			population.add(individual);
			log.debug(i + " - " + individual.toString());
		}
		return population;
	}

	Individual generateRandomIndividual(int populationCount, int n, int l, int m) {
		Individual individual = new Individual(this.generateRandomSolution(n,
				l, m, instance.prices));
		setIndividualsParameters(individual);
		return individual;
	}

	/**
	 * reparacion de poblacion cuando la funcion objetivo es TIC
	 * 
	 * @param population
	 * @param populationCount
	 * @param n
	 * @param l
	 * @param m
	 * @return
	 */
	List<Individual> repairPopulationOptimizeTIC(List<Individual> population,
			int populationCount, int n, int l, int m, boolean isFirstGeneration) {
		List<Individual> repairedPopulation = new ArrayList<Individual>();
		/*
		 * first solution of first generation
		 */
		boolean isFirstSolution;
		if (isFirstGeneration) {
			isFirstSolution = true;
		} else {
			isFirstSolution = false;
		}
		for (Individual individual : population) {
			int maxRepair = instance.extraInputs.maxRepair;
			boolean isOkTip = false;
			boolean isOkLoc = false;
			while (true) {
				if (!isFeasibleSolutionTIP(individual)) {
					individual = repairTIP(individual, isFirstSolution);
					/*
					 * first solution only can be forced to repair
					 */
					if (individual == null && isFirstSolution && maxRepair > 0) {
						log.info("Generating random solution again");
						individual = generateRandomIndividual(populationCount,
								n, l, m);
						maxRepair--;
						continue;
					} else if (individual == null) {
						/*
						 * No se logro reparar la solucion, finaliza la
						 * ejecucion
						 */
						log.error("Solution can't be repaired TIPmin even forcing.. end of execution");
						return null;
					}
				} else {
					isOkTip = true;
				}
				if (!isFeasibleSolutionLoc(individual)) {
					individual = repairLOCmin(individual);
					maxRepair--;
					// /*
					// * first solution only can be forced to repair
					// */
					// if (individual == null && isFirstSolution) {
					// log.info("Generating solution again");
					// individual = generateRandomIndividual(populationCount,
					// n, l, m);
					// maxRepair = instance.extraInputs.maxRepair;
					// continue;
					// } else if (individual == null) {
					// /*
					// * No se logro reparar la solucion, finaliza la
					// * ejecucion
					// */
					// log.error("Solution can't be repaired LOC min even forcing.. end of execution");
					// return null;
					// }
				} else {
					isOkLoc = true;
				}
				if (isOkTip && isOkLoc) {
					break;
				} else {
					isOkTip = false;
					isOkLoc = false;
				}
			}
			knownFeasibleSolution.add(individual);
			repairedPopulation.add(individual);
			isFirstSolution = false;
		}
		population = null;
		return repairedPopulation;
	}

	/**
	 * reparacion de poblacion cuando la funcion objetivo es TIP
	 * 
	 * @param population
	 * @param populationCount
	 * @param n
	 * @param l
	 * @param m
	 * @return
	 */
	List<Individual> repairPopulationOptimizeTIP(List<Individual> population,
			int populationCount, int n, int l, int m, boolean isFirstGeneration) {
		List<Individual> repairedPopulation = new ArrayList<Individual>();
		/*
		 * first solution of first generation
		 */
		boolean isFirstSolution;
		if (isFirstGeneration) {
			isFirstSolution = true;
		} else {
			isFirstSolution = false;
		}
		for (Individual individual : population) {
			int maxRepair = instance.extraInputs.maxRepair;
			boolean isOkTic = false;
			boolean isOkLoc = false;
			while (true) {
				if (!isFeasibleSolutionTIC(individual)) {
					individual = repairTIC(individual, isFirstSolution);
					/*
					 * first solution only can be forced to repair
					 */
					if (individual == null && isFirstSolution && maxRepair > 0) {
						log.info("Generating random solution again");
						individual = generateRandomIndividual(populationCount,
								n, l, m);
						maxRepair--;
						continue;
					} else if (individual == null) {
						/*
						 * No se logro reparar la solucion, finaliza la
						 * ejecucion
						 */
						log.error("Solution can't be repaired TICmin even forcing.. end of execution");
						return null;
					}
				} else {
					isOkTic = true;
				}
				if (!isFeasibleSolutionLoc(individual)) {
					individual = repairLOCmin(individual);
					/*
					 * first solution only can be forced to repair
					 */
					if (individual == null && isFirstSolution) {
						log.info("Generating solution again");
						individual = generateRandomIndividual(populationCount,
								n, l, m);
						maxRepair = instance.extraInputs.maxRepair;
						continue;
					} else if (individual == null) {
						/*
						 * No se logro reparar la solucion, finaliza la
						 * ejecucion
						 */
						log.error("Solution can't be repaired LOCmin even forcing.. end of execution");
						return null;
					}
				} else {
					isOkLoc = true;
				}
				if (isOkTic && isOkLoc) {
					break;
				} else {
					isOkTic = false;
					isOkLoc = false;
				}
			}
			knownFeasibleSolution.add(individual);
			repairedPopulation.add(individual);
			isFirstSolution = false;
		}
		population = null;
		return repairedPopulation;
	}

	/**
	 * reparacion de poblacion cuando la funcion objetivo es MC
	 * 
	 * @param population
	 * @param populationCount
	 * @param n
	 * @param l
	 * @param m
	 * @return
	 */
	List<Individual> repairPopulationOptimizeMC(List<Individual> population,
			int populationCount, int n, int l, int m, boolean isFirstGeneration) {
		List<Individual> repairedPopulation = new ArrayList<Individual>();
		/*
		 * first solution of first generation
		 */
		boolean isFirstSolution;
		if (isFirstGeneration) {
			isFirstSolution = true;
		} else {
			isFirstSolution = false;
		}
		for (Individual individual : population) {
			int maxRepair = instance.extraInputs.maxRepair;
			boolean isOkTip = false;
			boolean isOkLoc = false;
			while (true) {
				if (!isFeasibleSolutionTIP(individual)) {
					individual = repairTIP_MC(individual, isFirstSolution);
					/*
					 * first solution only can be forced to repair
					 */
					if (individual == null && isFirstSolution && maxRepair > 0) {
						log.info("Generating random solution again");
						individual = generateRandomIndividual(populationCount,
								n, l, m);
						maxRepair--;
						continue;
					} else if (individual == null) {
						/*
						 * No se logro reparar la solucion, finaliza la
						 * ejecucion
						 */
						log.error("Solution can't be repaired TIPmin even forcing.. end of execution");
						return null;
					}
				} else {
					isOkTip = true;
				}
				if (!isFeasibleSolutionLoc(individual)) {
					individual = repairLOCmin(individual);
					/*
					 * first solution only can be forced to repair
					 */
					if (individual == null && isFirstSolution) {
						log.info("Generating solution again");
						individual = generateRandomIndividual(populationCount,
								n, l, m);
						maxRepair = instance.extraInputs.maxRepair;
						continue;
					} else if (individual == null) {
						/*
						 * No se logro reparar la solucion, finaliza la
						 * ejecucion
						 */
						log.error("Solution can't be repaired LOC min even forcing.. end of execution");
						return null;
					}
				} else {
					isOkLoc = true;
				}
				if (isOkTip && isOkLoc) {
					break;
				} else {
					isOkTip = false;
					isOkLoc = false;
				}
			}
			knownFeasibleSolution.add(individual);
			repairedPopulation.add(individual);
			isFirstSolution = false;
		}
		population = null;
		return repairedPopulation;
	}

	int[][] generateRandomSolution(int n, int l, int m, BigDecimal[][] prices) {
		log.debug("Generating random individual");
		int[][] output = new int[2][n];
		int instanceType;
		int cloudProvider;
		double price;
		boolean isValid;
		for (int i = 0; i < n; i++) {
			isValid = false;
			do {
				instanceType = DCSUtil.getRandom(l);
				cloudProvider = DCSUtil.getRandom(m);
				price = prices[cloudProvider][instanceType].doubleValue();
				if (price > 0) {
					isValid = true;
				} else {
					log.trace("Discarding unfeasible random");
				}
			} while (!isValid);
			output[0][i] = instanceType;
			output[1][i] = cloudProvider;
		}
		return output;
	}

	/**
	 * Repair TIP constraints
	 * 
	 * @param i
	 * @param isFirstSolution
	 * @return
	 */
	Individual repairTIP(Individual i, boolean isFirstSolution) {
		log.debug("Repairing TIP solution. " + i);
		int n = instance.extraInputs.virtualMachineToDeploy.intValue();
		HashMap<Integer, Integer> notVisited = new HashMap<Integer, Integer>();
		for (int j = 0; j < n; j++) {
			notVisited.put(new Integer(j), new Integer(j));
		}
		int g;
		int j;
		int k;
		int[] fit;
		int maxRepair = instance.extraInputs.maxRepair;
		while (n > 0) {
			//
			g = getVMNotVisited(notVisited);
			j = i.output[0][g];
			k = i.output[1][g];
			//
			fit = this.selectPricePerCapacityUnit(instance.prices[k][j], j, k);
			i.output[0][g] = fit[0];
			i.output[1][g] = fit[1];
			setIndividualsParameters(i);
			if (isFeasibleSolutionTIP(i)) {
				log.debug("Solution repaired! TIP." + i);
				return i;
			} else {
				n--;
				/*
				 * Forzar solamente si es la primera solucion
				 */
				if (n == 0 && maxRepair > 0 && isFirstSolution) {
					log.warn("Forcing to repair solution TIP");
					n = instance.extraInputs.virtualMachineToDeploy.intValue();
					notVisited.clear();
					// notVisited = new HashMap<Integer, Integer>();
					for (int aux = 0; aux < n; aux++) {
						notVisited.put(new Integer(aux), new Integer(aux));
					}
					maxRepair--;
				} else if (n == 0 && maxRepair == 0) {
					break;
				}
			}
		}
		/*
		 * Ningun cambio realizado
		 */
		notVisited = null;
		if (n == 0) {
			log.debug("Getting known solution for TIP");
			if (knownFeasibleSolution.size() == 0) {
				log.error("Solution can't be repaired for TIP constraints");
				return null;
			}
			int random = DCSUtil.getRandom(knownFeasibleSolution.size());
			i = knownFeasibleSolution.get(random);
		}
		return i;
	}

	/**
	 * repair TIC constraints
	 * 
	 * @param i
	 * @param isFirstSolution
	 * @return
	 */
	Individual repairTIC(Individual i, boolean isFirstSolution) {
		log.debug("Repairing TIC solution. " + i);
		int n = instance.extraInputs.virtualMachineToDeploy.intValue();
		HashMap<Integer, Integer> notVisited = new HashMap<Integer, Integer>();
		for (int j = 0; j < n; j++) {
			notVisited.put(new Integer(j), new Integer(j));
		}
		int g;
		int j;
		int k;
		int[] fit;
		int maxRepair = instance.extraInputs.maxRepair;
		while (n > 0) {
			//
			g = getVMNotVisited(notVisited);
			j = i.output[0][g];
			k = i.output[1][g];
			//
			fit = this.selectCapacityPerPriceUnit(instance.instanceCapacity[j],
					j, k);
			i.output[0][g] = fit[0];
			i.output[1][g] = fit[1];
			setIndividualsParameters(i);
			if (isFeasibleSolutionTIC(i)) {
				log.debug("Solution repaired! TIC." + i);
				return i;
			} else {
				n--;
				/*
				 * Forzar solamente si es la primera solucion
				 */
				if (n == 0 && maxRepair > 0 && isFirstSolution) {
					log.debug("Forcing to repair solution TIC");
					n = instance.extraInputs.virtualMachineToDeploy.intValue();
					notVisited.clear();
					// notVisited = new HashMap<Integer, Integer>();
					for (int aux = 0; aux < n; aux++) {
						notVisited.put(new Integer(aux), new Integer(aux));
					}
					maxRepair--;
				} else if (n == 0 && maxRepair == 0) {
					break;
				}
			}
		}
		/*
		 * Ningun cambio realizado
		 */
		notVisited = null;
		if (n == 0) {
			log.debug("Getting known solution for TIC");
			if (knownFeasibleSolution.size() == 0) {
				log.error("Solution can't be repaired for TIC constraints");
				return null;
			}
			int random = DCSUtil.getRandom(knownFeasibleSolution.size());
			i = knownFeasibleSolution.get(random);
		}
		return i;
	}

	/**
	 * Repair LOC constraints
	 * 
	 * @param i
	 * @return
	 */
	Individual repairLOCmin(Individual i) {
		boolean optimizeTIC = instance.extraInputs.optimizeTIC;
		boolean optimizeTIP = instance.extraInputs.optimizeTIP;
		boolean optimizeMC = instance.extraInputs.optimizeMC;
		log.debug("Repairing LOCmin solution. " + i);
		int n = instance.extraInputs.virtualMachineToDeploy.intValue();
		int kMax;
		int kMin;
		int vmi = -1;
		boolean forcing = false;
		while (n > 0) {
			kMax = DCSUtil.getMax(i.loc);
			kMin = DCSUtil.getMin(i.loc);
			if (optimizeTIC || optimizeMC) {
				vmi = this.selectRandomVmPrice(i, kMax, kMin);
			}
			if (optimizeTIP) {
				vmi = this.selectRandomVmCapacity(i, kMax, kMin);
			}
			if (vmi < 0) {
				// get random VM
				vmi = this.selectRandomVmFromKmax(i, kMax, kMin);
				forcing = true;
				// no se encontro ninguna vm para migrar desde kMax
				// n = 0;
				// break;
			}
			// migrar vm de kMax a kMin
			i.output[1][vmi] = kMin;
			log.debug("Migrating.. vm: " + vmi + " from provider: " + kMax
					+ " to provider: " + kMin);
			setIndividualsParameters(i);
			if (forcing) {
				log.debug("Forcing migration. LOCmin " + i);
				return i;
			}
			if (isFeasibleSolutionLoc(i)) {
				log.debug("Solution repaired!. LOCmin " + i);
				return i;
			} else {
				n--;
			}
		}
		/*
		 * Ninguna migracion fue realizada
		 */
		if (n == 0) {
			log.debug("LOCmin can't be repaired" + i);
			log.debug("Getting known feasible solution for LOC");
			if (knownFeasibleSolution.size() == 0) {
				log.error("Solution can't be repaired for LOC constraints");
				return null;
			}
			int random = DCSUtil.getRandom(knownFeasibleSolution.size());
			i = knownFeasibleSolution.get(random);
		}
		return i;
	}

	/**
	 * Repair TIP constraints
	 * 
	 * @param i
	 * @param isFirstSolution
	 * @return
	 */
	Individual repairTIP_MC(Individual i, boolean isFirstSolution) {
		log.debug("Repairing TIP_MC solution. " + i);
		int n = instance.extraInputs.virtualMachineToDeploy.intValue();
		HashMap<Integer, Integer> notVisited = new HashMap<Integer, Integer>();
		for (int j = 0; j < n; j++) {
			notVisited.put(new Integer(j), new Integer(j));
		}
		int g;
		int j;
		int k;
		int[] fit;
		int maxRepair = instance.extraInputs.maxRepair;
		while (n > 0) {
			//
			g = getVMNotVisited(notVisited);
			j = i.output[0][g];
			k = i.output[1][g];
			//
			fit = this.selectOverheadIndex(instance.prices[k][j], j, k);
			i.output[0][g] = fit[0];
			i.output[1][g] = fit[1];
			setIndividualsParameters(i);
			if (isFeasibleSolutionTIP(i)) {
				log.debug("Solution repaired! TIP." + i);
				return i;
			} else {
				n--;
				/*
				 * Forzar solamente si es la primera solucion
				 */
				if (n == 0 && maxRepair > 0 && isFirstSolution) {
					log.warn("Forcing to repair solution TIP");
					n = instance.extraInputs.virtualMachineToDeploy.intValue();
					notVisited.clear();
					// notVisited = new HashMap<Integer, Integer>();
					for (int aux = 0; aux < n; aux++) {
						notVisited.put(new Integer(aux), new Integer(aux));
					}
					maxRepair--;
				} else if (n == 0 && maxRepair == 0) {
					break;
				}
			}
		}
		/*
		 * Ningun cambio realizado
		 */
		if (n == 0) {
			log.warn("Getting known solution for TIP");
			if (knownFeasibleSolution.size() == 0) {
				log.error("Solution can't be repaired for TIP constraints");
				return null;
			}
			int random = DCSUtil.getRandom(knownFeasibleSolution.size());
			i = knownFeasibleSolution.get(random);
		}
		notVisited.clear();
		return i;
	}

	int getVMNotVisited(HashMap<Integer, Integer> notVisited) {
		int random = DCSUtil.getRandom(notVisited.size());
		Iterator<Integer> it = notVisited.keySet().iterator();
		Integer key = null;
		Integer valueSelected = null;
		int n = 0;
		while (it.hasNext()) {
			key = it.next();
			if (n++ == random) {
				valueSelected = notVisited.get(key);
				break;
			}
		}
		notVisited.remove(key);
		return valueSelected.intValue();
	}

	/**
	 * Method for repair LOCmin when TIC or MC is objective funcion
	 * 
	 * @param i
	 * @param kMax
	 * @param kMin
	 * @return
	 */
	int selectRandomVmPrice(Individual i, int kMax, int kMin) {
		int vmSelected = -1;
		int count = 0;
		/*
		 * cargar en una estructura auxiliar las instancias que se despliegan en
		 * kMax
		 */
		int vms = instance.extraInputs.virtualMachineToDeploy.intValue();
		for (int j = 0; j < vms; j++) {
			if (i.output[1][j] == kMax) {
				count++;
			}
		}
		/*
		 * x = VMi. j = Provider k
		 */
		int[][] aux = new int[2][count];
		/*
		 * count es el indice de la estructura auxiliar
		 */
		count = 0;
		for (int j = 0; j < vms; j++) {
			if (i.output[1][j] == kMax) {
				aux[0][count] = j;
				aux[1][count++] = i.output[1][j];
			}
		}
		/*
		 * estructura auxiliar cargada
		 */
		int n = count;
		boolean[] visited = new boolean[n];
		for (int j = 0; j < visited.length; j++) {
			visited[j] = false;
		}
		int g;
		int vmi;
		/*
		 * instance Type
		 */
		int j;
		/*
		 * precio de ejecutar en el proveedor mas cargado
		 */
		BigDecimal priceMaxLoad;
		/*
		 * precio de ejecutar en el proveedor menos cargado
		 */
		BigDecimal priceMinLoad;
		while (n > 0) {
			do {
				g = DCSUtil.getRandom(visited.length);
				vmi = aux[0][g];
			} while (visited[g]);
			j = i.output[0][vmi];
			priceMaxLoad = instance.prices[kMax][j];
			priceMinLoad = instance.prices[kMin][j];
			/*
			 * La instancia seleccionada no se puede ejecutar en el proveedor
			 * seleccionado
			 */
			if (priceMinLoad.doubleValue() < 0) {
				n--;
				visited[g] = true;
				continue;
			}
			/*
			 * El precio de ejecutar en kMax mas el margen de TIPmax - TIP es
			 * igual o menor que ejecutar en kMin
			 */
			BigDecimal priceGap = priceMaxLoad.add(instance.extraInputs.budget
					.subtract(i.tip));
			if (priceMinLoad.compareTo(priceGap) < 1) {
				vmSelected = vmi;
				break;
			} else {
				/*
				 * No lo es, buscar otra instancia para migrar
				 */
				n--;
				visited[g] = true;
			}
		}
		aux = null;
		return vmSelected;
	}

	int selectRandomVmFromKmax(Individual i, int kMax, int kMin) {
		int vmSelected = -1;
		int count = 0;
		/*
		 * cargar en una estructura auxiliar las instancias que se despliegan en
		 * kMax
		 */
		int vms = instance.extraInputs.virtualMachineToDeploy.intValue();
		for (int j = 0; j < vms; j++) {
			if (i.output[1][j] == kMax) {
				count++;
			}
		}
		/*
		 * x = VMi. j = Provider k
		 */
		int[][] aux = new int[2][count];
		/*
		 * count es el indice de la estructura auxiliar
		 */
		count = 0;
		for (int j = 0; j < vms; j++) {
			if (i.output[1][j] == kMax) {
				aux[0][count] = j;
				aux[1][count++] = i.output[1][j];
			}
		}
		/*
		 * estructura auxiliar cargada
		 */
		int n = count;
		boolean[] visited = new boolean[n];
		for (int j = 0; j < visited.length; j++) {
			visited[j] = false;
		}
		int g;
		int vmi;
		/*
		 * instance Type
		 */
		int j;
		/*
		 * precio de ejecutar en el proveedor menos cargado
		 */
		BigDecimal priceMinLoad;
		while (n > 0) {
			do {
				g = DCSUtil.getRandom(visited.length);
				vmi = aux[0][g];
			} while (visited[g]);
			j = i.output[0][vmi];
			priceMinLoad = instance.prices[kMin][j];
			/*
			 * La instancia seleccionada no se puede ejecutar en el proveedor
			 * seleccionado
			 */
			if (priceMinLoad.doubleValue() < 0) {
				n--;
				visited[g] = true;
				continue;
			}
			vmSelected = vmi;
			break;
		}
		aux = null;
		return vmSelected;
	}

	/**
	 * Method for repair LOCmin when TIP is objective funcion
	 * 
	 * @param i
	 * @param kMax
	 * @param kMin
	 * @return
	 */
	int selectRandomVmCapacity(Individual i, int kMax, int kMin) {
		int vmSelected = -1;
		int count = 0;
		/*
		 * cargar en una estructura auxiliar las instancias que se despliegan en
		 * kMax
		 */
		int vms = instance.extraInputs.virtualMachineToDeploy.intValue();
		for (int j = 0; j < vms; j++) {
			if (i.output[1][j] == kMax) {
				count++;
			}
		}
		/*
		 * x = VMi. j = Provider k
		 */
		int[][] aux = new int[2][count];
		/*
		 * count es el indice de la estructura auxiliar
		 */
		count = 0;
		for (int j = 0; j < vms; j++) {
			if (i.output[1][j] == kMax) {
				aux[0][count] = j;
				aux[1][count++] = i.output[1][j];
			}
		}
		/*
		 * estructura auxiliar cargada
		 */
		int n = count;
		boolean[] visited = new boolean[n];
		for (int j = 0; j < visited.length; j++) {
			visited[j] = false;
		}
		int g;
		int vmi;
		/*
		 * instance Type
		 */
		int j;
		/*
		 * price
		 */
		BigDecimal price;
		while (n > 0) {
			do {
				g = DCSUtil.getRandom(visited.length);
				vmi = aux[0][g];
			} while (visited[g]);
			j = i.output[0][vmi];
			price = instance.prices[kMin][j];
			/*
			 * La instancia seleccionada no se puede ejecutar en el proveedor
			 * seleccionado
			 */
			if (price.doubleValue() < 0) {
				n--;
				visited[g] = true;
				continue;
			}
			vmSelected = vmi;
			break;
		}
		aux = null;
		return vmSelected;

	}

	boolean isFeasibleSolutionTIC(Individual individual) {
		BigDecimal ticTmp = new BigDecimal(individual.tic);
		if (ticTmp.compareTo(instance.extraInputs.capacityThreshold) < 0) {
			log.debug("Unfeasible solution. TICmin constraints not reached. TIC: "
					+ individual.tic
					+ ", capacityThreshold: "
					+ instance.extraInputs.capacityThreshold);
			return false;
		} else {
			return true;
		}
	}

	boolean isFeasibleSolutionTIP(Individual individual) {
		if (individual.tip.compareTo(instance.extraInputs.budget) > 0) {
			log.debug("Unfeasible solution. TIP constraints exceeds. TIP: "
					+ individual.tip + ", budget: "
					+ instance.extraInputs.budget);
			return false;
		} else {
			return true;
		}
	}

	boolean isFeasibleSolutionLoc(Individual individual) {
		BigDecimal locMin = instance.extraInputs.loadBalanceMin;
		int n = instance.extraInputs.virtualMachineToDeploy.intValue();
		double load;
		double loadIndex;
		for (int i = 0; i < individual.loc.length; i++) {
			load = individual.loc[i].doubleValue();
			loadIndex = load / (double) n;
			if (loadIndex < locMin.doubleValue()) {
				log.debug("Unfeasible solution. LOCmin constraints. LOC: "
						+ loadIndex + ", LOCmin: " + locMin.doubleValue()
						+ ", load: " + individual.loc[i] + ", provider:" + i);
				return false;
			}
		}
		return true;
	}

	/**
	 * Method for repair TIP when TIC is objective funcion
	 * 
	 * @param actualPrice
	 * @param actInstanceType
	 * @param actProvider
	 * @return
	 */
	int[] selectPricePerCapacityUnit(BigDecimal actualPrice,
			int actInstanceType, int actProvider) {
		int[] fit = new int[2];
		fit[0] = actInstanceType;
		fit[1] = actProvider;
		BigDecimal sum = null;
		BigDecimal parcSum = null;
		BigDecimal[][] pricePerCapacity = instance.pricePerCapacity;
		for (int i = 0; i < pricePerCapacity.length; i++) {
			if (pricePerCapacity[i][4].compareTo(actualPrice) < 0) {
				if (parcSum == null) {
					parcSum = pricePerCapacity[i][0];
				} else {
					parcSum = parcSum.add(pricePerCapacity[i][0]);
					// parcSum = parcSum.add(pricePerCapacity[i][2]);
				}
			}
		}
		if (parcSum != null) {
			BigDecimal random = new BigDecimal(Math.random());
			parcSum = parcSum.multiply(random);
		} else {
			parcSum = new BigDecimal(0);
		}
		for (int i = 0; i < pricePerCapacity.length; i++) {
			if (pricePerCapacity[i][4].compareTo(actualPrice) < 0) {
				if (sum == null) {
					sum = pricePerCapacity[i][0];
				} else {
					sum = sum.add(pricePerCapacity[i][0]);
				}
				// sum = sum.add(pricePerCapacity[i][2]);
				if (sum.compareTo(parcSum) >= 0) {
					fit[0] = pricePerCapacity[i][1].intValue();
					fit[1] = pricePerCapacity[i][3].intValue();
					break;
				}
			}
		}
		pricePerCapacity = null;
		return fit;
	}

	/**
	 * Method for repair TIC when TIP is objective funcion
	 * 
	 * @param actualCapacity
	 * @param actInstanceType
	 * @param actProvider
	 * @return
	 */
	int[] selectCapacityPerPriceUnit(int actualCapacity, int actInstanceType,
			int actProvider) {
		int[] fit = new int[2];
		fit[0] = actInstanceType;
		fit[1] = actProvider;
		BigDecimal sum = null;
		BigDecimal parcSum = null;
		BigDecimal[][] capacityPerPrice = instance.capacityPerPrice;
		BigDecimal actualCapacityBig = new BigDecimal(actualCapacity);
		for (int i = 0; i < capacityPerPrice.length; i++) {
			if (capacityPerPrice[i][2].compareTo(actualCapacityBig) > 0) {
				if (parcSum == null) {
					parcSum = capacityPerPrice[i][0];
				} else {
					parcSum = parcSum.add(capacityPerPrice[i][0]);
					// parcSum = parcSum.add(capacityPerPrice[i][2]);
				}
			}
		}
		BigDecimal random = new BigDecimal(Math.random());
		parcSum = parcSum.multiply(random);
		for (int i = 0; i < capacityPerPrice.length; i++) {
			if (capacityPerPrice[i][2].compareTo(actualCapacityBig) > 0) {
				if (sum == null) {
					sum = capacityPerPrice[i][0];
				} else {
					sum = sum.add(capacityPerPrice[i][0]);
				}
				// sum = sum.add(capacityPerPrice[i][2]);
				if (sum.compareTo(parcSum) >= 0) {
					fit[0] = capacityPerPrice[i][1].intValue();
					fit[1] = capacityPerPrice[i][3].intValue();
					break;
				}
			}
		}
		return fit;
	}

	/**
	 * Method for repair TIP when MC is objective funcion
	 * 
	 * @param actualPrice
	 * @param actInstanceType
	 * @param actProvider
	 * @return
	 */
	int[] selectOverheadIndex(BigDecimal actualPrice, int actInstanceType,
			int actProvider) {
		int[] fit = new int[2];
		fit[0] = actInstanceType;
		fit[1] = actProvider;
		BigDecimal sum = null;
		BigDecimal parcSum = null;
		BigDecimal[][] overheadIndex = instance.overheadIndex;
		for (int i = 0; i < overheadIndex.length; i++) {
			if (overheadIndex[i][4].compareTo(actualPrice) < 0) {
				if (parcSum == null) {
					parcSum = overheadIndex[i][0];
				} else {
					parcSum = parcSum.add(overheadIndex[i][0]);
				}
			}
		}
		BigDecimal random = new BigDecimal(Math.random());
		parcSum = parcSum.multiply(random);
		for (int i = 0; i < overheadIndex.length; i++) {
			if (overheadIndex[i][4].compareTo(actualPrice) < 0) {
				if (sum == null) {
					sum = overheadIndex[i][0];
				} else {
					sum = sum.add(overheadIndex[i][0]);
				}
				if (sum.compareTo(parcSum) >= 0) {
					fit[0] = overheadIndex[i][1].intValue();
					fit[1] = overheadIndex[i][3].intValue();
					break;
				}
			}
		}
		return fit;
	}

	/**
	 * Discard worst solution when TIC is objective function
	 * 
	 * @param population
	 * @return
	 */
	List<Individual> discardWorstSolutionTIC(List<Individual> population) {
		BigDecimal minTic = null;
		int worstSolutionIndex = -1;
		/*
		 * identificar peor solucion
		 */
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (minTic == null
					|| individual.ticWithOverhead.compareTo(minTic) < 0) {
				worstSolutionIndex = i;
				minTic = individual.ticWithOverhead;
			}
		}
		return this.discard(population, worstSolutionIndex);
	}

	/**
	 * Discard worst solution when TIP is objective function
	 * 
	 * @param population
	 * @return
	 */
	List<Individual> discardWorstSolutionTIP(List<Individual> population) {
		BigDecimal maxTip = null;
		int worstSolutionIndex = -1;
		/*
		 * identificar peor solucion
		 */
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (maxTip == null || individual.tip.compareTo(maxTip) > 0) {
				worstSolutionIndex = i;
				maxTip = individual.tip;
			}
		}
		return this.discard(population, worstSolutionIndex);
	}

	/**
	 * Discard worst solution when MC is objective function
	 * 
	 * @param population
	 * @return
	 */
	List<Individual> discardWorstSolutionMC(List<Individual> population) {
		BigDecimal maxMC = null;
		int worstSolutionIndex = -1;
		/*
		 * identificar peor solucion
		 */
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (maxMC == null || individual.mc.compareTo(maxMC) > 0) {
				worstSolutionIndex = i;
				maxMC = individual.mc;
			}
		}
		return this.discard(population, worstSolutionIndex);
	}

	List<Individual> discard(List<Individual> population, int worstSolutionIndex) {
		List<Individual> selectedIndividual = new ArrayList<Individual>();
		/*
		 * descartar peor solucion
		 */
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (i != worstSolutionIndex) {
				selectedIndividual.add(individual);
			} else {
				log.debug("Discarding worst solution: " + individual);
			}
		}
		population = null;
		return selectedIndividual;
	}

	List<Individual> crossOver(List<Individual> population) {
		List<Individual> selectedIndividual = new ArrayList<Individual>();
		Individual parent1;
		Individual parent2;
		Individual child1;
		Individual child2;
		int[][] output1;
		int[][] output2;
		int crossPoint;
		int n = instance.extraInputs.virtualMachineToDeploy.intValue();
		int i = 0;
		if (population.size() % 2 != 0) {
			selectedIndividual.add(rouletteSelection(population));
		}
		for (; i < (population.size() / 2); i++) {
			parent1 = rouletteSelection(population);
			parent2 = rouletteSelection(population);
			/*
			 * probabilidad de cruzamiento del 50%
			 */
			int random = DCSUtil.getRandom(100);
			if (random % 2 == 0) {
				crossPoint = DCSUtil.getRandom(n);
			} else {
				crossPoint = n;
			}
			output1 = new int[2][n];
			output2 = new int[2][n];
			for (int j = 0; j < n; j++) {
				output1[0][j] = parent1.output[0][j];
				output1[1][j] = parent1.output[1][j];
				output2[0][j] = parent2.output[0][j];
				output2[1][j] = parent2.output[1][j];
			}
			if (crossPoint != n) {
				log.debug("Crossing..");
				for (int j = crossPoint; j < n; j++) {
					output1[0][j] = parent2.output[0][j];
					output1[1][j] = parent2.output[1][j];
					output2[0][j] = parent1.output[0][j];
					output2[1][j] = parent1.output[1][j];
				}
			}
			child1 = new Individual(output1);
			setIndividualsParameters(child1);
			child2 = new Individual(output2);
			setIndividualsParameters(child2);
			selectedIndividual.add(child1);
			selectedIndividual.add(child2);

		}
		population = null;
		return selectedIndividual;
	}

	Individual rouletteSelection(List<Individual> population) {
		boolean optimizeTIC = instance.extraInputs.optimizeTIC;
		boolean optimizeTIP = instance.extraInputs.optimizeTIP;
		boolean optimizeMC = instance.extraInputs.optimizeMC;
		BigDecimal index = null;
		if (optimizeTIC) {
			// Order by TIC desc
			Collections.reverse(population);
		} else if (optimizeTIP || optimizeMC) {
			// Order by TIP or MC asc
			Collections.sort(population);
		}
		BigDecimal parcSum = new BigDecimal(0);
		for (Individual individual : population) {
			if (optimizeTIC) {
				index = individual.ticWithOverhead;
			} else if (optimizeTIP) {
				index = individual.tip;
			} else if (optimizeMC) {
				index = individual.mc;
			}
			parcSum = parcSum.add(index);
		}
		double random = Math.random();
		parcSum = parcSum.multiply(new BigDecimal(random));
		// Select by roullette
		Individual i = null;
		BigDecimal sum = new BigDecimal(0);
		for (Individual individual : population) {
			if (optimizeTIC) {
				index = individual.ticWithOverhead;
			} else if (optimizeTIP) {
				index = individual.tip;
			} else if (optimizeMC) {
				index = individual.mc;
			}
			sum = sum.add(index);
			if (sum.compareTo(parcSum) >= 0) {
				i = individual;
				break;
			}
		}
		return i;
	}

	List<Individual> mutate(List<Individual> population) {
		List<Individual> individualMutated = new ArrayList<Individual>();
		int instanceType;
		int cloudProvider;
		double price;
		boolean isValid;
		int l = instance.instanceName.length;
		int m = instance.providers.length;
		int n = instance.extraInputs.virtualMachineToDeploy.intValue();
		int vm;
		for (Individual individual : population) {
			if (mutationProbability()) {
				vm = DCSUtil.getRandom(n);
				log.debug("Mutating. vm: " + vm + ", " + individual);
				isValid = false;
				do {
					instanceType = DCSUtil.getRandom(l);
					cloudProvider = DCSUtil.getRandom(m);
					price = instance.prices[cloudProvider][instanceType]
							.doubleValue();
					if (price > 0) {
						isValid = true;
					}
				} while (!isValid);
				individual.output[0][vm] = instanceType;
				individual.output[1][vm] = cloudProvider;
				setIndividualsParameters(individual);
				log.debug("Mutated! " + individual);
			}
			individualMutated.add(individual);
		}
		population = null;
		return individualMutated;
	}

	boolean mutationProbability() {
		int n = instance.extraInputs.virtualMachineToDeploy.intValue();
		double probability = (double) 1 / (double) n;
		double random = Math.random();
		if (random <= probability) {
			return true;
		}
		return false;
	}

	void setIndividualsParameters(Individual individual) {
		individual.tic = instance.getTIC(individual.output);
		individual.n = instance.extraInputs.virtualMachineToDeploy.intValue();
		individual.downtimeStatistics = instance.downtimeStatistics;
		individual.tip = instance.getTIP(individual.output);
		individual.loc = instance.getLOC(individual.output);
		individual.extraInputs = instance.extraInputs;
		individual.providers = instance.providers;
		individual.instanceName = instance.instanceName;
		individual.instanceCapacity = instance.instanceCapacity;
		individual.prices = instance.prices;
		individual.instanceSummary = instance.getInstanceSummary(individual);
		if (instance.extraInputs.optimizeMC) {
			individual.migrationOverhead = instance
					.getMigrationOverhead(individual);
			individual.mc = instance.getMC(individual);
		} else if (instance.prevSolution != null) {
			individual.migrationTime = instance.getMigrationTime(individual);
			individual.migrationOverhead = instance
					.getMigrationOverhead(individual);
			individual.mc = instance.getMC(individual);
			individual.prevSolution = instance.prevSolution;
			individual.extraInputs = instance.extraInputs;
		}
		individual.instancePerProviderSummary = instance
				.getInstancePerProviderSummary(individual);
		BigDecimal ticTmp = new BigDecimal(individual.tic);
		individual.ticWithOverhead = ticTmp.subtract(individual.mc);
		if (individual.ticWithOverhead == null) {
			log.debug("is null");
		}
	}

}
