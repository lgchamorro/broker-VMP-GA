package broker.mo.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Set;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class InstanceType {

	Integer j;
	String label;
	int computingCapacity;
	HashMap<CloudProvider, BigDecimal> cloudAndPrices;
	HashMap<CloudProvider, MigrationStatistics> migrationOverhead;

	public InstanceType(Integer j, String label) {
		super();
		this.j = j;
		this.label = label;
		this.cloudAndPrices = new HashMap<CloudProvider, BigDecimal>();
		this.migrationOverhead = new HashMap<CloudProvider, MigrationStatistics>();
	}

	public BigDecimal getPricePerProvider(CloudProvider provider) {
		BigDecimal price = cloudAndPrices.get(provider);
		if (price == null) {
			return new BigDecimal(-1);
		}
		return price;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((j == null) ? 0 : j.hashCode());
		return result;
	}

	public CloudProvider getCheaperProvider() {
		CloudProvider provider = null;
		BigDecimal cheaperPrice = new BigDecimal(Double.MAX_VALUE);
		BigDecimal currentPrice;
		Set<CloudProvider> providers = cloudAndPrices.keySet();
		for (CloudProvider p : providers) {
			currentPrice = cloudAndPrices.get(p);
			if (currentPrice.compareTo(cheaperPrice) < 0) {
				cheaperPrice = currentPrice;
				provider = p;
			}
		}
		return provider;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InstanceType other = (InstanceType) obj;
		if (j == null) {
			if (other.j != null)
				return false;
		} else if (!j.equals(other.j))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InstanceType [j=" + j + ", label=" + label
				+ ", computingCapacity=" + computingCapacity + "]";
	}

}
