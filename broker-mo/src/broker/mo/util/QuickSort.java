package broker.mo.util;

import java.math.BigDecimal;

/**
 * 
 * @author Lino Gabriel
 * 
 */
public class QuickSort {

	private BigDecimal array[][];
	private int length;
	private boolean optimizeTIC;
	private boolean optimizeTIP;
	private boolean optimizeMC;

	public void sort(BigDecimal[][] inputArr, boolean optimizeTIC,
			boolean optimizeTIP, boolean optimizeMC) {
		if (inputArr == null || inputArr.length == 0) {
			return;
		}
		if (optimizeTIC == false && optimizeTIP == false) {
			System.err
					.println("optimizeTIC and optimizeTIP both can't be false");
			return;
		}
		this.array = inputArr;
		length = inputArr.length;
		this.optimizeTIC = optimizeTIC;
		this.optimizeTIP = optimizeTIP;
		this.optimizeMC = optimizeMC;
		quickSort(0, length - 1);
	}

	private void quickSort(int lowerIndex, int higherIndex) {
		int i = lowerIndex;
		int j = higherIndex;
		// calculate pivot number, I am taking pivot as middle index number
		int pivotIndex = lowerIndex + ((higherIndex - lowerIndex) / 2);
		// Divide into two arrays
		while (i <= j) {
			if (optimizeTIC || optimizeMC) {
				/**
				 * In each iteration, we will identify a number from left side
				 * which is greater then the pivot value, and also we will
				 * identify a number from right side which is less then the
				 * pivot value. Once the search is done, then we exchange both
				 * numbers.
				 */
				while (isLessCapacity(pivotIndex, i)) {
					i++;
					if (i == array.length) {
						break;
					}
				}
				while (isGreaterCapacity(pivotIndex, j)) {
					j--;
				}
			} else if (optimizeTIP) {
				/**
				 * In each iteration, we will identify a number from left side
				 * which is less then the pivot value, and also we will identify
				 * a number from right side which is greater then the pivot
				 * value. Once the search is done, then we exchange both
				 * numbers.
				 */
				while (isLessCapacityPerPrice(pivotIndex, i)) {
					i++;
				}
				while (isGreaterCapacityPerPrice(pivotIndex, j)) {
					j--;
				}
			}
			if (i <= j) {
				exchangeNumbers(i, j);
				// move index to next position on both sides
				i++;
				j--;
			}
		}
		// call quickSort() method recursively
		if (lowerIndex < j) {
			quickSort(lowerIndex, j);
		}
		if (i < higherIndex) {
			quickSort(i, higherIndex);
		}
	}

	@SuppressWarnings("unused")
	private boolean isGreaterPricePerCapacity(int pivotIndex, int compIndex) {
		// ordenar precio por unidad de capacidad
		BigDecimal pivotPricePerCap = array[pivotIndex][0];
		BigDecimal pivotPrice = array[pivotIndex][4];
		BigDecimal compPricePerCap = array[compIndex][0];
		BigDecimal compPrice = array[compIndex][4];
		if (pivotPricePerCap.doubleValue() > compPricePerCap.doubleValue()) {
			return true;
		} else if (pivotPricePerCap.doubleValue() < compPricePerCap
				.doubleValue()) {
			return false;
		} else {
			/*
			 * si los indices de precio por unidad de capacidad son iguales,
			 * comparar precio. el que tiene mayor precio, es mayor
			 */
			if (pivotPrice.doubleValue() > compPrice.doubleValue()) {
				return true;
			} else if (pivotPrice.doubleValue() < compPrice.doubleValue()) {
				return false;
			}
		}
		return false;
	}

	private boolean isGreaterCapacity(int pivotIndex, int compIndex) {
		// ordenar capacidad
		BigDecimal pivotPricePerCap = array[pivotIndex][0];
		BigDecimal pivotCapacity = array[pivotIndex][2];
		BigDecimal compPricePerCap = array[compIndex][0];
		BigDecimal compCapacity = array[compIndex][2];
		if (pivotCapacity.intValue() > compCapacity.intValue()) {
			return true;
		} else if (pivotCapacity.intValue() < compCapacity.intValue()) {
			return false;
		} else {
			/*
			 * si las capacidades son iguales, comparar indice de precio por
			 * capacidad. el que tiene mayor precio, es mayor
			 */
			if (pivotPricePerCap.doubleValue() > compPricePerCap.doubleValue()) {
				return true;
			} else if (pivotPricePerCap.doubleValue() < compPricePerCap
					.doubleValue()) {
				return false;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean isLessPricesPerCapacity(int pivotIndex, int compIndex) {
		// ordenar precio por unidad de capacidad
		BigDecimal pivotPricePerCap = array[pivotIndex][0];
		BigDecimal pivotPrice = array[pivotIndex][2];
		BigDecimal compPricePerCap = array[compIndex][0];
		BigDecimal compPrice = array[compIndex][2];
		if (pivotPrice.intValue() < compPrice.intValue()) {
			return true;
		} else if (pivotPricePerCap.doubleValue() > compPricePerCap
				.doubleValue()) {
			return false;
		} else {
			/*
			 * si los indices de precio por unidad de capacidad son iguales,
			 * comparar precio. el que tiene menor precio, es menor
			 */
			if (pivotPricePerCap.doubleValue() < compPricePerCap.doubleValue()) {
				return true;
			} else if (pivotPrice.intValue() > compPrice.intValue()) {
				return false;
			}
		}
		return false;
	}

	private boolean isLessCapacity(int pivotIndex, int compIndex) {
		// ordenar capacidad
		BigDecimal pivotPricePerCap = array[pivotIndex][0];
		BigDecimal pivotCapacity = array[pivotIndex][2];
		BigDecimal compPricePerCap = array[compIndex][0];
		BigDecimal compCapacity = array[compIndex][2];
		if (pivotCapacity.intValue() < compCapacity.intValue()) {
			return true;
		} else if (pivotCapacity.intValue() > compCapacity.intValue()) {
			return false;
		} else {
			/*
			 * si las capacidades son iguales, comparar indice de precio por
			 * unidad de capacidad, el que tiene menor es menor
			 */
			if (pivotPricePerCap.doubleValue() < compPricePerCap.doubleValue()) {
				return true;
			} else if (pivotPricePerCap.doubleValue() > compPricePerCap
					.doubleValue()) {
				return false;
			}
		}
		return false;
	}

	private boolean isGreaterCapacityPerPrice(int pivotIndex, int compIndex) {
		// ordenar capacidad por unidad de precio
		BigDecimal pivotCapPerPrice = array[pivotIndex][0];
		BigDecimal pivotCap = array[pivotIndex][2];
		BigDecimal compCapPerPrice = array[compIndex][0];
		BigDecimal compCap = array[compIndex][2];
		// if (pivotCapPerPrice.doubleValue() > compCapPerPrice.doubleValue()) {
		// return true;
		// } else if (pivotCapPerPrice.doubleValue() < compCapPerPrice
		// .doubleValue()) {
		// return false;
		// } else {
		// /*
		// * si los indices de capacidad por unidad de precio son iguales,
		// * comparar capacidad. el que tiene mayor capacidad, es mayor
		// */
		// if (pivotCap.doubleValue() > compCap.doubleValue()) {
		// return true;
		// } else if (pivotCap.doubleValue() < compCap.doubleValue()) {
		// return false;
		// }
		// }
		// TODO comparar por precio
		if (pivotCap.doubleValue() > compCap.doubleValue()) {
			return true;
		} else if (pivotCap.doubleValue() < compCap.doubleValue()) {
			return false;
		} else {
			/*
			 * si las capacidades son iguales, comparar indices, el que tiene
			 * mayor indice es mejor
			 */
			if (pivotCapPerPrice.doubleValue() > compCapPerPrice.doubleValue()) {
				return true;
			} else if (pivotCapPerPrice.doubleValue() < compCapPerPrice
					.doubleValue()) {
				return false;
			}
		}
		return false;
	}

	private boolean isLessCapacityPerPrice(int pivotIndex, int compIndex) {
		// ordenar capacidad por unidad de precio
		BigDecimal pivotCapPerPrice = array[pivotIndex][0];
		BigDecimal pivotCap = array[pivotIndex][2];
		BigDecimal compCapPerPrice = array[compIndex][0];
		BigDecimal compCap = array[compIndex][2];
		// if (pivotCapPerPrice.doubleValue() < compCapPerPrice.doubleValue()) {
		// return true;
		// } else if (pivotCapPerPrice.doubleValue() > compCapPerPrice
		// .doubleValue()) {
		// return false;
		// } else {
		// /*
		// * si los indices de capacidad por unidad de precio son iguales,
		// * comparar capacidad. el que tiene menor capacidad, es menor
		// */
		// if (pivotCap.doubleValue() < compCap.doubleValue()) {
		// return true;
		// } else if (pivotCap.doubleValue() > compCap.doubleValue()) {
		// return false;
		// }
		// }
		// TODO comparar por precio
		if (pivotCap.doubleValue() < compCap.doubleValue()) {
			return true;
		} else if (pivotCap.doubleValue() > compCap.doubleValue()) {
			return false;
		} else {
			/*
			 * si las capacidades son iguales, comparar indice, el que tiene
			 * menor indice, es menor
			 */
			if (pivotCapPerPrice.doubleValue() < compCapPerPrice.doubleValue()) {
				return true;
			} else if (pivotCapPerPrice.doubleValue() > compCapPerPrice
					.doubleValue()) {
				return false;
			}
		}
		return false;
	}

	private void exchangeNumbers(int i, int j) {
		BigDecimal indexTemp = array[i][0];
		BigDecimal instanceTypeTemp = array[i][1];
		BigDecimal capacityTemp = array[i][2];
		BigDecimal cloudTemp = array[i][3];
		BigDecimal priceTemp = array[i][4];
		array[i][0] = array[j][0];
		array[i][1] = array[j][1];
		array[i][2] = array[j][2];
		array[i][3] = array[j][3];
		array[i][4] = array[j][4];
		array[j][0] = indexTemp;
		array[j][1] = instanceTypeTemp;
		array[j][2] = capacityTemp;
		array[j][3] = cloudTemp;
		array[j][4] = priceTemp;
	}

}
