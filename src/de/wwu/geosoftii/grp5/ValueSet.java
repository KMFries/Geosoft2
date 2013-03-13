package de.wwu.geosoftii.grp5;

import java.util.Date;

/**
 * Class that encapsulates information for a databoint used for the validation.
 * It stores the date, value, quality_id and quality_value.
 * @author sven
 *
 */
public class ValueSet {
	
	private Date date;
	private String quality_id;
	private String value;
	private String quality_value;
	
	/**
	 * 
	 * @param date as Date object
	 * @param quality_id as String
	 * @param value as String
	 * 
	 */
	public ValueSet(Date date, String quality_id, String value, String quality_value){
		this.date = date;
		this.quality_id = quality_id;
		this.value = value;
		this.quality_value = quality_value;
	}
	
	public ValueSet(){
		
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		//System.out.println("ValueSet: "+date);
		this.date = date;
		//System.out.println("ValueSet date after setDate(): "+this.getDate());
	}

	public String getQuality_id() {
		return quality_id;
	}

	public void setQuality_id(String quality_id) {
		this.quality_id = quality_id;
	}

	public String getValue() {
		return value;
	}
	
	public double getValueAsDouble(){
		return Double.valueOf(this.getValue());
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getQuality_value() {
		return quality_value;
	}

	public void setQuality_value(String quality_value) {
		this.quality_value = quality_value;
	}
	
	

}
