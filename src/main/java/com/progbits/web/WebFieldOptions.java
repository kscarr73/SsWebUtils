
package com.progbits.web;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author scarr
 */
public class WebFieldOptions {
	private String dateTimeFormat = "yyyy-MM-dd";
	private DateTimeFormatter dtFormat = DateTimeFormat.forPattern(dateTimeFormat);

	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
		dtFormat = DateTimeFormat.forPattern(dateTimeFormat);
	}
	
	public DateTimeFormatter getDateTimeFormatter() {
		return dtFormat;
	}
}
