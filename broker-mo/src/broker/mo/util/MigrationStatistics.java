package broker.mo.util;

import java.math.BigDecimal;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class MigrationStatistics {

	/**
	 * All in seconds
	 */
	InstanceType instanceType;
	CloudProvider cloudProvider;
	BigDecimal allocation;
	BigDecimal deAllocation;

	public MigrationStatistics() {
		super();
	}

	public MigrationStatistics(InstanceType instanceType,
			CloudProvider cloudProvider, BigDecimal allocation,
			BigDecimal deallocation) {
		super();
		this.instanceType = instanceType;
		this.cloudProvider = cloudProvider;
		this.allocation = allocation;
		this.deAllocation = deallocation;
	}

	@Override
	public String toString() {
		return "MigrationOverhead [instanceType=" + instanceType.label
				+ ", cloudProvider=" + cloudProvider.name + ", allocation="
				+ allocation + ", deallocation=" + deAllocation + "]";
	}

}
