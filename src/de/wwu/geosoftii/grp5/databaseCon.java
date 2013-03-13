package de.wwu.geosoftii.grp5;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * Class that handles data base connection stuff and provides methods for
 * running an moving window outlier detection 
 * 
 * @author sven
 * 
 */
public class databaseCon {

	private Properties properties = new Properties();
	private Logger logger;

	Connection con;
	private String db_username;
	private String db_password;
	private String db_url;


	/**
	 * Constructor for connection object that establishes connection to local
	 * psql databse using user, pw and url from config.properties.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public databaseCon() throws FileNotFoundException, IOException {
		// load data from properties file into attributes
		properties.load(new FileInputStream("outlier_config.properties"));
		this.db_username = (String) properties.getProperty("db_username");
		this.db_password = (String) properties.getProperty("db_password");
		this.db_url = (String) properties.getProperty("db_url");

		// logging stuff
		logger = Logger.getLogger(this.getClass());
		PropertyConfigurator.configure("log4j.properties");

		// Get the psql driver
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException cnfe) {
			logger.warn("Driver not found!");
			cnfe.printStackTrace();
			System.exit(1);
		}

		// establish connection
		try {
			con = DriverManager.getConnection("jdbc:postgresql:" + this.db_url,
					this.db_username, this.db_password);
		} catch (SQLException e) {
			logger.warn("Connection failed!");
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * Method that closes the database connection
	 */
	public void disconnect() {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
				con = null;
			}
		} catch (SQLException e) {
			logger.warn("Cannot disconnect!");
		}
	}
	
	/**
	 * Function returns all features of interest ids that have corresponding values in the quality table
	 * @return ArrayList of Strings with foi_ids of features that have corresponding entries in the quality table
	 */
	public ArrayList<String> getAllFeatures(){
		String query = "SELECT DISTINCT feature_of_interest_id FROM observation NATURAL INNER JOIN quality;";
		ArrayList<String> list = new ArrayList<String>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()){
				list.add(rs.getString(1));
			}
		} catch (SQLException e) {
			logger.warn("Invalid query: "+query);
			e.printStackTrace();
		}
		//logger.info("Features in quality table: "+list);
		return list;
	}
	
	
	/**
	 * This method gets the all the n values with a timestamp smaller/equal to the checkPoint timestamp.
	 * -> All the values in window.
	 * It's capable of returning the windows content ordered by timestamp or value
	 * @param checkPoint the data point the window is calculated for
	 * @param n is the window width
	 * @param phenomenon_id is the id of the observed phenomenon
	 * @param feature_of_interest_id is the id of the feature of interest
	 * @param order_by_value set true if the output should be ordered ascending by the datapoints value, false if it should be ordered ascending by the time_stamp
	 * @return ArrayList<ValuSet> that contains all the values in the window
	 */
	public ArrayList<ValueSet> getValuesInWindow(ValueSet checkPoint, int n, String phenomenon_id, String feature_of_interest_id, boolean order_by_value){
		
		ArrayList<ValueSet> list = new ArrayList<ValueSet>();
		String query = "";
		if (order_by_value) {
			query = "SELECT * FROM (SELECT * FROM observation NATURAL INNER JOIN quality WHERE feature_of_interest_id='"+feature_of_interest_id+"' and phenomenon_id='"+phenomenon_id+"' and time_stamp<='"+checkPoint.getDate()+"' ORDER BY time_stamp DESC LIMIT "+n+") t  ORDER BY (numeric_value);";
		} else {
			//get all it's predecessors ordered 
			query = "SELECT * FROM (SELECT * FROM observation NATURAL INNER JOIN quality WHERE feature_of_interest_id='"+feature_of_interest_id+"' and phenomenon_id='"+phenomenon_id+"' and time_stamp<='"+checkPoint.getDate()+"' ORDER BY time_stamp DESC LIMIT "+n+") t  ORDER BY (time_stamp);";
		}
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()){
				list.add(new ValueSet(rs.getTimestamp("time_stamp"), rs.getString("quality_id"), rs.getString("numeric_value"), rs.getString("quality_value")));
			}
		} catch (SQLException e) {
			logger.warn("Invalid query: "+query);
			e.printStackTrace();
		}
		
		return list;
		
	}
	
	/**
	 * This method reads the oldest unchecked value from the database corresponding to a certain phenomenon and feature combination.
	 * It always leaves out the first n-1 unchecked values stored in the database because it's not possible to validate those with
	 * a left sided window of size n.
	 * @param n is window size
	 * @param phenomenon_id is the current tested phenomenon
	 * @param feature_of_interest_id is the current tested feature
	 * @return ValueSet that is the oldest unchecked value
	 */
	public ValueSet getOldestUncheckedValue(int n, String phenomenon_id, String feature_of_interest_id) {
		/*
		 * inner query:
		 * Search for the oldest n untested values of a phenomenon of a feature and return sorted ascending by date.
		 * outer query:
		 * Sorts the result of the inner query descending and chooses the first value
		 * => The values that are not choosen are the offset of n-1
		 */
		ValueSet val = new ValueSet();

		String query = "SELECT * FROM "
				+ "( SELECT time_stamp, quality_id, numeric_value, quality_value FROM (observation NATURAL INNER JOIN quality) WHERE phenomenon_id='"
				+ phenomenon_id
				+ "' AND quality_value='not_tested' AND feature_of_interest_id='"
				+ feature_of_interest_id
				+ "' order by (time_stamp) ASC LIMIT "
				+ n + ") t order by (time_stamp) DESC LIMIT 1";
		//logger.info("Query: "+query);
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next()){
				val.setDate((Date)rs.getTimestamp("time_stamp"));
				val.setQuality_id(rs.getString("quality_id"));
				val.setValue(rs.getString("numeric_value"));
				val.setQuality_value(rs.getString("quality_value"));
			}
		} catch (SQLException e) {
			logger.warn("Invalid query: "+query);
			e.printStackTrace();
		}
		return val;
	}
	
	/**
	 * Method provides possibility to change the quality_value column in the quality table
	 * @param tag yes (outlier), no (no outlier), not_tested (not yet tested)
	 * @param quality_id is primary key in outlier table
	 */
	public void setOutlierInformation(String tag, String quality_id){
		if (tag=="yes" || tag=="no" || tag=="not_tested"){
			String query = "UPDATE quality SET quality_value='"+tag+"' WHERE quality_id='"+quality_id+"';";
			try {
				Statement stmt = con.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException e) {
				logger.warn("Invalid query: "+query);
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * Method gets the newest timestamp in the quality table as reference for the outlier detection
	 * @return Newest time_stamp in quality table
	 */
	public Date getNewestQualityTimestamp(){
		String query = "SELECT MAX(time_stamp) as date FROM observation NATURAL INNER JOIN quality WHERE quality_name='outlier';";
		Date date = null;
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			date = rs.getTimestamp("date");
		} catch (SQLException e) {
			logger.warn("Invalid query: "+query);
			e.printStackTrace();
		}
		return date;
	}
	
	/**
	 * Method reads out the phenomenon assigned to a feature of interest
	 * @param id is the feature of interest's id
	 * @return ArrayList<String> containing the phenomenas ids as strings
	 */
	public ArrayList<String> getPhenomenaOfFeature(String id){
		ArrayList<String> list = new ArrayList<String>();
		String query = "SELECT DISTINCT phenomenon_id FROM feature_of_interest NATURAL INNER JOIN observation WHERE feature_of_interest_id='"+id+"';";
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				list.add(rs.getString("phenomenon_id"));
			}
		} catch (SQLException e) {
			logger.warn("Invalid query: "+query);
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Method to reset the outlier marking process
	 */
	public void resetOutlierMarking(){
			// set the quality_value of the "outlier"-quality criteria back to "not tested" 
			String query = "UPDATE quality SET quality_value='not_tested' WHERE quality_name='outlier';";
			try {
				Statement stmt = con.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException e) {
				logger.warn("Invalid query: "+query);
				e.printStackTrace();
			}
	}
	
	

	

}
