package de.wwu.geosoftii.grp5;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Main class that controls the outlier detection workflow.
 * @author sven
 *
 */
public class Main {
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//logging stuff
		Logger logger = Logger.getRootLogger();
		PropertyConfigurator.configure("log4j.properties");
		
		//create properties
		Properties properties = new Properties();
		
		//create running median object
		RunningMedian rm = new RunningMedian();
		
		try {
			//create new database connection
			databaseCon dbCon = new databaseCon();
			
			// invalid query -> for testing purposes
			//ValueSet testPoint = dbCon.getOldestUncheckedValue(30, "urn:ogc:object:feature:Sensor:AQE:temperature-sensor", "75842");
			
			//load the properties file
			properties.load(new FileInputStream("outlier_config.properties"));

			boolean reset = Boolean.valueOf(properties.getProperty("reset_outlier_information"));
			
			//resets the outlier marking before starting the program -> for testing purposes
			if (reset) dbCon.resetOutlierMarking();
			
			//Get newest timestamp from quality table as reference
			Date refDate = dbCon.getNewestQualityTimestamp();
			
			// set window width from properties file
			int winWidth = Integer.valueOf(properties.getProperty("window_width"));
			
			// set border multiplicator
			double borderMultiplicator = Double.valueOf(properties.getProperty("border_multiplicator"));
			rm.setBorderMultiplicator(borderMultiplicator);
			
			
			// get all features and their phenomenons
			// first get the feaures ids
			ArrayList<String> featureIds = dbCon.getAllFeatures();
			// empty collection to be filled with features
			ArrayList<Feature> features = new ArrayList<Feature>();
			Iterator<String> idIter = featureIds.iterator();
			// create all the features
			// iterate over the feature ids
			while(idIter.hasNext()){
				String id = idIter.next();
				//create new feature
				Feature tempFeature = new Feature(id);
				//save the phenomenon of the feature
				tempFeature.setPhenomena(dbCon.getPhenomenaOfFeature(id));
				//add feature to feature collection
				features.add(tempFeature);
			}
			
			//iterate over the features
			Iterator<Feature> featIter = features.iterator();
			while(featIter.hasNext()){
				// temporary feature
				Feature tempFeature = featIter.next();
				// temporary featureId
				String featureId = tempFeature.getId();
				// iterate over the features phenomena
				ArrayList<String> phenomena = tempFeature.getPhenomena();
				Iterator<String> phenIter = phenomena.iterator();
				while(phenIter.hasNext()){
					//current phenomenon
					String tempPhenomenon = phenIter.next();
					// search for outliers
					ValueSet checkPoint = dbCon.getOldestUncheckedValue(winWidth, tempPhenomenon, featureId);
					//check every value that is younger than the reference date
					while( checkPoint!=null && refDate.after(checkPoint.getDate()) ){
					//check for outliers
						//get the ValueSets used to test the current value, sorted by value
						ArrayList<ValueSet> valuesInWindowList = dbCon.getValuesInWindow(checkPoint, winWidth, tempPhenomenon, featureId, true);
						//check if there are enough values for outlier detection in database
						//in the beginning there may be not enough values in the database
						if (valuesInWindowList.size()==winWidth){
							//check if the current value is an outlier
							boolean isOutlier = rm.isOutlier(checkPoint, valuesInWindowList);
							String outlierTag = "not_tested";
							if (isOutlier) outlierTag = "yes";
							else if (!isOutlier) outlierTag = "no";
							//update the information in the table
							dbCon.setOutlierInformation(outlierTag, checkPoint.getQuality_id());
							logger.info(checkPoint.getDate()+"  "+checkPoint.getQuality_id()+": "+checkPoint.getValue()+" "+checkPoint.getQuality_value()+" "+outlierTag);
						}
						//save the next value
						checkPoint = dbCon.getOldestUncheckedValue(winWidth, tempPhenomenon, featureId);
					}
				}
			}
			
			// close the database connection
			dbCon.disconnect();
			
		} catch (FileNotFoundException e) {
			logger.warn("outlier_config.properties could not be found");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
		//Testing different functions
		/*
		System.out.println(dbCon.getAllFeatures());
		
		
		ArrayList<ValueSet> val = dbCon.getValuesInWindow(30, aqeSensors.getTemperaturePhenomenon(), "75842", true);
		int count = 0;
		Iterator valIter = val.iterator();
		while(valIter.hasNext()){
			count++;
			ValueSet temp = (ValueSet) valIter.next();
			System.out.println(count+" "+temp.getDate()+" "+temp.getQuality_id()+": "+temp.getValue()+" "+temp.getQuality_value());
		}
		
		System.out.println(rm.isOutlier(dbCon.getOldestUncheckedValue(30, aqeSensors.getTemperaturePhenomenon(), "75842"), dbCon.getValuesInWindow(30, aqeSensors.getTemperaturePhenomenon(), "75842", true)));
		
		System.out.println(dbCon.getNewestQualityTimestamp());
		
		System.out.println(dbCon.getPhenomenaOfFeature("75842"));
		
		*/
		
		/*
		ValueSet temp = dbCon.getOldestUncheckedValue(30, aqeSensors.getTemperaturePhenomenon(), "75842");
		System.out.println(temp.getDate()+"  "+temp.getQuality_id()+": "+temp.getValue()+" "+temp.getQuality_value());
		*/	
		
	}
	
	/*
	 * Changes
	 * ValueSet-, SensorSet-, FeatureSet- and databaseConnection-classes added.
	 * Program is now possible to validate the data stored in the database with a left sided moving window filter (running median)
	 */
	
	

}
