package broker.mo.util;

import java.math.BigDecimal;

/**
 * 
 * @author Lino Chamorro
 * 
 */
public class DCSUtil {

	public static int getRandomInt() {
		return (int) (Math.random() * DCSConstants.MAX_RANDOM);
	}

	public static int getRandom(int n) {
		return (int) (DCSUtil.getRandomInt() % n);
	}

	public static int getMax(BigDecimal[] p) {
		int max = 0;
		int j = -1;
		for (int i = 0; i < p.length; i++) {
			if (p[i].intValue() > max) {
				max = p[i].intValue();
				j = i;
			}
		}
		return j;
	}

	public static int getMin(BigDecimal[] p) {
		int min = 999999;
		int j = -1;
		for (int i = 0; i < p.length; i++) {
			if (p[i].intValue() < min) {
				min = p[i].intValue();
				j = i;
			}
		}
		return j;
	}

}
