package broker.mo.core;

import java.math.BigDecimal;

import broker.mo.util.ExtraInputs;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class Individual implements Comparable<Individual> {

	int n;
	String[] instanceName;
	int[] instanceCapacity;
	BigDecimal[][] prices;
	String[] providers;
	Individual prevSolution;
	ExtraInputs extraInputs;
	/**
	 * current solution. j = VMi. output[0][j]: InstanceType. output[1][j]:
	 * cloudprovider
	 */
	int[][] output;
	BigDecimal[] migrationOverhead;
	BigDecimal[] migrationTime;
	BigDecimal[][] downtimeStatistics;

	/**
	 * Total Infrastructure Capacity
	 */
	int tic;
	/**
	 * Total Infrastructure Capacity menos overhead
	 */
	BigDecimal ticWithOverhead;

	/**
	 * Total Infrastructure Price
	 */
	BigDecimal tip;
	BigDecimal mc;
	/**
	 * aux output structures
	 */
	int[] instanceSummary;
	BigDecimal[][][] instancePerProviderSummary;

	/**
	 * Load Balance
	 */
	BigDecimal[] loc;
	/**
	 * objective function
	 */
	boolean optimizeTIC;
	boolean optimizeTIP;
	boolean optimizeMC;

	public Individual(int[][] output) {
		super();
		this.output = output;
		ticWithOverhead = new BigDecimal(0);
		mc = new BigDecimal(0);
	}

	public void resetIndividual(int[][] output) {
		this.output = output;
		ticWithOverhead = new BigDecimal(0);
		mc = new BigDecimal(0);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Individual [tic=" + tic + ", mc=" + mc
				+ ", ticWithOverhead=" + ticWithOverhead + ", tip=" + tip
				+ ", LOC[");
		for (int i = 0; i < loc.length; i++) {
			sb.append(loc[i]);
			if (i < loc.length - 1) {
				sb.append(", ");
			}
		}
		sb.append("]]");
		return sb.toString();
	}

	@Override
	public int compareTo(Individual individual) {
		if (individual == null) {
			return 1;
		}
		if (optimizeTIC) {
			if (this.ticWithOverhead.compareTo(individual.ticWithOverhead) > 0) {
				return 1;
			} else if (this.ticWithOverhead
					.compareTo(individual.ticWithOverhead) < 0) {
				return -1;
			} else if (this.ticWithOverhead
					.compareTo(individual.ticWithOverhead) == 0) {
				// son iguales en TIC, comparar por precio
				if (this.tip.compareTo(individual.tip) < 0) {
					return 1;
				} else if (this.tip.compareTo(individual.tip) > 0) {
					return -1;
				}
				return 0;
			}
		}
		if (optimizeTIP) {
			if (this.tip.compareTo(individual.tip) > 0) {
				return 1;
			} else if (this.tip.compareTo(individual.tip) < 0) {
				return -1;
			} else if (this.tip.compareTo(individual.tip) == 0) {
				// son iguales en TIP, comparar por precio
				if (this.ticWithOverhead.compareTo(individual.ticWithOverhead) < 0) {
					return -1;
				} else if (this.ticWithOverhead
						.compareTo(individual.ticWithOverhead) > 0) {
					return 1;
				}
				return 0;
			}
		}
		if (optimizeMC) {
			if (this.mc.compareTo(individual.mc) > 0) {
				return 1;
			} else if (this.mc.compareTo(individual.mc) < 0) {
				return -1;
			} else if (this.mc.compareTo(individual.mc) == 0) {
				// son iguales en MC, comparar por precio
				if (this.tip.compareTo(individual.tip) < 0) {
					return 1;
				} else if (this.tip.compareTo(individual.tip) > 0) {
					return -1;
				}
				return 0;
			}
		}
		return -2;
	}

}
