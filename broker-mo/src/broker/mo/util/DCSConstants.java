package broker.mo.util;

import java.math.BigDecimal;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class DCSConstants {

	public static final String PROVIDERS_BEGIN = "PROVIDERS-BEGIN";
	public static final String PROVIDERS_END = "PROVIDERS-END";
	public static final String INSTANCE_TYPES_BEGIN = "INSTANCE-TYPES-BEGIN";
	public static final String INSTANCE_TYPES_END = "INSTANCE-TYPES-END";
	public static final String LABEL = "LABEL";
	public static final String CAPACITY = "CAPACITY";
	public static final String PRICES = "PRICES";
	public static final String OVERHEAD_BEGIN = "OVERHEAD-BEGIN";
	public static final String OVERHEAD_END = "OVERHEAD-END";
	public static final String ALLOCATION = "ALLOC";
	public static final String DEALLOCATION = "DEALLOC";
	public static final String RESTRICTION_BEGIN = "RESTRICTIONS-BEGIN";
	public static final String RESTRICTION_END = "RESTRICTIONS-END";
	public static final String VIRTUAL_MACHINES_TO_DEPLOY = "VIRTUAL-MACHINES-TO-DEPLOY";
	public static final String CAPACITY_THRESHOLD = "CAPACITY-THRESHOLD";
	public static final String BUDGET = "BUDGET";
	public static final String LOAD_BALANCE_MIN = "LOAD-BALANCE-MIN";
	public static final String LOAD_BALANCE_MAX = "LOAD-BALANCE-MAX";
	public static final String MAX_REPAIR = "MAX-REPAIR";
	public static final String NOT_APPLICABLE = "N/A";
	public static final String FUNCTION_BEGIN = "FUNCTION-BEGIN";
	public static final String POPULATION = "POPULATION";
	public static final String GENERATIONS = "GENERATIONS";
	public static final String SCENARIO = "SCENARIO";
	public static final String PRINT_OVERHEAD = "PRINT-OVERHEAD";
	public static final String OPTIMIZE_TIC = "OPTIMIZE-TIC";
	public static final String OPTIMIZE_TIP = "OPTIMIZE-TIP";
	public static final String OPTIMIZE_MC = "OPTIMIZE-MC";
	public static final String FUNCTION_END = "FUNCTION-END";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	//
	public static final int MAX_RANDOM = 1000;
	public static final String SEPARATOR = ";";
	public static final BigDecimal HOUR_IN_SECONDS = new BigDecimal(3600);

}
