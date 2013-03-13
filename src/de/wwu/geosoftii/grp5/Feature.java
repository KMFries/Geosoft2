package de.wwu.geosoftii.grp5;

import java.util.ArrayList;

/**
 * Encapsulates the different features stored in database with id and collection of assigned phenomena.
 * @author sven
 *
 */
public class Feature {
	
	private String id;
	private ArrayList<String> phenomena = new ArrayList<String>();
	
	public Feature(String id){
		this.id = id;
	}
	
	/**
	 * 
	 * @return the features id
	 */
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 
	 * @return the features phenomena as ArrayList of Strings
	 */
	public ArrayList<String> getPhenomena() {
		return phenomena;
	}
	public void setPhenomena(ArrayList<String> phenomena) {
		this.phenomena = phenomena;
	}
	
	/**
	 * Method that adds a String to the collection of phenomena
	 * @param phenomenon is the phenomenon string to be added
	 */
	public void addPhenomenon(String phenomenon){
		this.phenomena.add(phenomenon);
	}
	
	
	
	

}
