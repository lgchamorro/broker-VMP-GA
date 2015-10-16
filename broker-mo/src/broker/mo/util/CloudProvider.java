package broker.mo.util;


/**
 * 
 * @author Lino Chamorro
 * 
 */
public class CloudProvider {

	// private final Logger log = Logger.getLogger(getClass());

	Integer k;
	String name;

	public CloudProvider(Integer k, String name) {
		super();
		this.k = k;
		this.name = name;
	}

	String getIndexByName(int index) {
		if (k == index) {
			return name;
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((k == null) ? 0 : k.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudProvider other = (CloudProvider) obj;
		if (k == null) {
			if (other.k != null)
				return false;
		} else if (!k.equals(other.k))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CloudProvider [k=" + k + ", name=" + name + "]";
	}

}
