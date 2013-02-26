package de.wwu.geosoftii.grp5;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double[] Array = {2,5,3,9,7,1,6,0};					// the array example
		
		RunningMedian rm = new RunningMedian();
		rm.RunningWindow(Array);
	}

}
