package broker.mo.util;

import java.math.BigDecimal;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class ExtraInputs {

	// restrictions
	public BigDecimal virtualMachineToDeploy;
	public BigDecimal capacityThreshold;
	public BigDecimal budget;
	public BigDecimal loadBalanceMin;
	public BigDecimal loadBalanceMax;
	// population
	public BigDecimal population;
	// generations
	public BigDecimal generations;
	// function optimize
	public boolean optimizeTIC;
	public boolean optimizeTIP;
	public boolean optimizeMC;
	//
	public int maxRepair;
	public int scenario;
	public boolean printOverhead;

}
