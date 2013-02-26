package de.wwu.geosoftii.grp5;

/**
 * This class will calculate the median of a given array. 
 * Furthermore it checks if one or more values are possible outliers.
 * 
 * @author KFries
 * @version 1.0
 *
 */

public class RunningMedian {
	
	/**
	 * Method to sort an array.
	 * Quicksort is used to check each value of the array and sort it.
	 * 
	 * @param x		the array to be sorted
	 */
	
	public RunningMedian(){
		
	}
	
	public void sort(double[] x) {
		qSort(x, 0, x.length-1);
	}
	
	/**
	 * Quicksort core method.
	 * 
	 * @param x			the array to be sorted
	 * @param left		the number at the first position of the array
	 * @param right		the number at the last position of the array
	 */
	
	public void qSort(double[] x, int left, int right) {
		if (left < right) {
			int i = partition(x, left, right);
			qSort(x,left,i-1);
			qSort(x,i+1,right);
		}
	}
	
	/**
	 * Partition method of the quicksort algorithm.
	 * 
	 * @param x			the array to be sorted
	 * @param left		the number at the first position of the array
	 * @param right		the number at the last position of the array
	 * @return
	 */
	
	public int partition(double[] x, int left, int right) {
		double pivot = x[right];
		double help;
		int i = left;
		int j = right-1;
		
		while(i<=j) {
			if (x[i] > pivot) {
				// switch x[i] and x[j]
				help = x[i];
				x[i] = x[j];
				x[j] = help;
				j--;
			} else i++;
		}
		// switch x[i] and x[right]
		help = x[i];
		x[i] = x[right];
		x[right] = help;
		
		return i;
	}
	
	
	/**
	 * Method to calculate the median.
	 * 
	 * @param Array		the array in which the median shall be calculated
	 * @return			returns the median of the given array
	 */
	
	 public double Median(double[] Array) {
		double Median;
		
		if (Array.length % 2 == 0)							// even numbers
			Median = ((Array[Array.length / 2] + Array[(Array.length / 2) - 1]) / 2);
		else Median = Array[((Array.length + 1) / 2) - 1];	// odd numbers
		
		return Median;
	}
	
	 /**
	  * Method to get the running median without using a window.
	  * 
	  * @param Array	the array for which the running median shall be calculated.
	  */
	 
	public void RunningMedian(double[] Array) {	
		int i = 0;
		double[] jArray;
		double RunningMedian;
		
		while (i < Array.length) {
			jArray = new double [i+1];					
				for	(int m = 0; m <= i; m++)		// initialising help array jArray
					jArray[m] = Array[m];
			sort(jArray);							// sort jArray
			RunningMedian = Median(jArray);
													
			System.out.println("Median at step "+(i+1)+": "+RunningMedian);	// give median
			
			i++;
			}
	}
	
	/**
	 * Method to get the median in a given window and calculate possible outliers.
	 * 
	 * @param Array	the array for which the running median and outliers shall be identified.
	 */
	
	public void RunningWindow(double[] Array) {
		int windowSize = 4;
		int i = windowSize;
		int step = 0;
		double[] jArray;
		double RunningMedian;
		double quartile25, quartile75;
		double maxBorder, minBorder;
		double IQR;
		
		while (i <= Array.length) {
			jArray = new double[windowSize];
				for (int m = i - windowSize; m < i; m++)	// initialising help array jArray
					jArray[m-step] = Array[m];				// we need to subtract the value of step from m to write it to the right position
			sort(jArray);									// sort jArray
			RunningMedian = Median(jArray);
			
			System.out.println("Median at step "+(step+1)+": "+RunningMedian);
			
			quartile25 = jArray[(int) Math.ceil(jArray.length * 0.25) - 1];		// calculate value of the 25% quartile
			quartile75 = jArray[(int) Math.ceil(jArray.length * 0.75) - 1];		// calculate value of the 75% quartile
			IQR = quartile75 - quartile25;										// calculate value of the interquartile range
			maxBorder = RunningMedian + 1.5 * IQR;								// calculate the maximum border for outlier detection
			minBorder = RunningMedian - 1.5 * IQR;								// calculate the minimum border for outlier detection
			
			for (int m = i - windowSize; m < i; m++)
				if (jArray[m-step] > maxBorder || jArray[m-step] < minBorder)
					System.out.println("\tPossible Outlier: "+jArray[m-step]);	// check each value inside the window if it could be an outlier
			
			step++;
			i++;
		}
			
		
	}
	
	
	
}