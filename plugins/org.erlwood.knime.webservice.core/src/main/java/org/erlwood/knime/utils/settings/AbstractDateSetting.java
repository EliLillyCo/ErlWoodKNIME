/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2017 Eli Lilly and Company Limited
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * ------------------------------------------------------------------------
*/
package org.erlwood.knime.utils.settings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.erlwood.knime.utils.settings.AbstractColumnAndValueSettings;
import org.knime.core.node.InvalidSettingsException;


/** Class storing the column/value selection for a Date.
 * @author Tom Wilkin */
public abstract class AbstractDateSetting extends AbstractColumnAndValueSettings {
	
	/** The date formatter to convert the stored date. */
	private static final SimpleDateFormat FORMATTER;
	static {
		FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
		FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	protected AbstractDateSetting( ) {
		setDate(new Date( ));
	}
	
	@Override
	protected String getColumnValidationFailMessage( ) {
		return "You must specify a valid date column.";
	}

	@Override
	protected String getValueValidationFailMessage( ) {
		return "You must specify a valid date value.";
	}
	
	/** @return The date in the range.
	 * @throws InvalidSettingsException If the date is not convertible. */
	public Date getDate( ) throws InvalidSettingsException {
		return convertStringToDate(getValue( ));
	}
	
	/** Set the new date.
	 * @param date The new value for the date. */
	public void setDate(final Date date) {
		setValue(convertDateToString(date));
	}
	
	/** Convert the supplied string date to XML.
	 * @param date The date to convert.
	 * @return The converted XML date.
	 * @throws InvalidSettingsException If the date is not convertible.
	 * @throws DatatypeConfigurationException If the date is not convertible. */
	public static XMLGregorianCalendar convertDateToXML(final String date) 
			throws InvalidSettingsException, DatatypeConfigurationException 
	{
		if(!"".equals(date)) {
			GregorianCalendar calendar = new GregorianCalendar( );
			calendar.setTime(convertStringToDate(date));
			return DatatypeFactory.newInstance( ).newXMLGregorianCalendar(calendar);
		}
		
		return null;		
	}
	
	/** Check the values for the dates are an allowable range.
	 * @param startDate The start of the date range.
	 * @param endDate The end of the date range.
	 * @throws InvalidSettingsException If the dates do not make an allowable range. */
	public static void checkDates(final AbstractDateSetting startDate, 
			final AbstractDateSetting endDate) 
			throws InvalidSettingsException
	{		
		if(endDate != null && !endDate.isUseColumn( )) {
			GregorianCalendar end = new GregorianCalendar( );
			end.setTime(endDate.getDate( ));
			
			// ensure end is not in the future
			if(end.after(new Date( ))) {
				throw new InvalidSettingsException("Date range cannot be set in the future.");
			}
			
			if(startDate != null && !startDate.isUseColumn( )) {
				GregorianCalendar start = new GregorianCalendar( );
				start.setTime(startDate.getDate( ));
				
				// ensure end is after start
				if(!end.after(start)) {
					throw new InvalidSettingsException("End date must be after start date.");
				}
				
				// ensure the dates are not more than 1 year apart
				long diff = end.getTimeInMillis( ) - start.getTimeInMillis( );
				diff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
				if(diff > 366 || (diff > 365 && !end.isLeapYear(end.get(Calendar.YEAR)))) {
					throw new InvalidSettingsException("Date range cannot be greater than 1 year.");
				}
			}
		}
	}
	
	/** Convert the supplied string format date to a date object.
	 * @param date The date to convert.
	 * @return The converted date object.
	 * @throws InvalidSettingsException If the date is not convertible. */
	private static Date convertStringToDate(final String date) throws InvalidSettingsException {
		if(!"".equals(date)) {
			try {
				synchronized(FORMATTER) {
					return FORMATTER.parse(date);
				}
			} catch(ParseException e) {
				throw new InvalidSettingsException(
						"String '" + date + "' does not conform to format yyyy-MM-dd."
				);
			}
		}
		
		return null;
	}
	
	/** Convert the supplied date object a string format.
	 * @param date The date to convert.
	 * @return The converted string date. */
	private static String convertDateToString(final Date date) {
		if(date != null) {
			synchronized(FORMATTER) {
				return FORMATTER.format(date);
			}
		}
		
		return "";
	}
	
}
