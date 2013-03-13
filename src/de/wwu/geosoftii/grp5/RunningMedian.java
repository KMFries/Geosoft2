package de.wwu.geosoftii.grp5;

import java.util.ArrayList;

/**
 * This class will calculate the median of a given array. 
 * Furthermore it checks if one or more values are possible outliers.
 * 
 * @author KFries and sven
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
	
	private double borderMultiplicator = 1.5;
	
	public void setBorderMultiplicator(double borderMultiplicator){
		this.borderMultiplicator = borderMultiplicator;
	}
	
	
	public RunningMedian(){
		
	}
	
	 
	/**
	 * Find the Median in a given ArrayList
	 * @param list is a ArrayList of ValueSets
	 * @return the median of the given list
	 */
	 public double median(ArrayList<ValueSet> list){
		 double median;
		 int size = list.size();
		 if (list.size() % 2 == 0)							// even numbers
				median = ( list.get(size / 2 - 1).getValueAsDouble() + list.get(size / 2).getValueAsDouble() ) / 2;
			else median = list.get(size / 2).getValueAsDouble();	// odd numbers
		 
		 return median;
	 }
	 
	/**
	 * Method checks if a given value is an outlier in comparision to a list of values.
	 * It calculates the median and checks if it lies in a interval (media +- borderMultiplicator * inter quartile range)
	 * @param val the value to be checked as ValueSet
	 * @param list
	 * @return true if the value is an possible outlier, false if it's not
	 */
	public boolean isOutlier(ValueSet val, ArrayList<ValueSet> list){
		boolean outlierTag = false;
		//calculate median
		double median = this.median(list);
		// calculate 25 % quartile
		double quartile25 = list.get((int)Math.ceil(list.size()*0.25)-1).getValueAsDouble();
		// calculate 75 % quartile
		double quartile75 = list.get((int)Math.ceil(list.size()*0.75)-1).getValueAsDouble();
		// calculate IQR
		double IQR = quartile75 - quartile25;
		// define maxBorder
		double maxBorder = median + borderMultiplicator * IQR;
		// define minBorder
		double minBorder = median - borderMultiplicator * IQR;
		//check if the tested value is outside of the borders
		if (val.getValueAsDouble()<minBorder || val.getValueAsDouble()>maxBorder) outlierTag = true;
		
		return outlierTag;
	}
	
	
	
}