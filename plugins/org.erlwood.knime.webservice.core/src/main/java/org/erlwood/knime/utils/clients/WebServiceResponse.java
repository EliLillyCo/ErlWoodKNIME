/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2014 Eli Lilly and Company Limited
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
package org.erlwood.knime.utils.clients;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;

/** Class used to encapsulate the returned data from a web service request.
 * @author Tom Wilkin */
public class WebServiceResponse {

	/** Class containing the data from a returned Column from the web service request. */
	public static class Column {
		
		/** The name of this column to use in KNIME. */
		private String name;
		
		/** The KNIME data type of this column. */
		private DataType type;
		
		public Column(final String name, final DataType type) {
			this.name = name;
			this.type = type;
		}
		
		/** @return The name of this column. */
		public String getName( ) { return name; }
		
		/** The KNIME data type of this column. */
		public DataType getType( ) { return type; }
		
		@Override
		public boolean equals(final Object obj) {
			if(obj instanceof Column) {
				Column column = (Column)obj;
				return column.name.equals(name) && column.type.equals(type);
			}
			return false;
		}
		
		@Override
		public int hashCode( ) {
			int hash = 37;
			hash ^= name.hashCode( );
			hash ^= type.hashCode( );
			return hash;
		}
		
	}
	
	/** The columns that were returned in the request. */
	private List<Column> columns;
	
	/** The rows that were returned in the request. */
	private List<Object[ ]> rows;
	
	public WebServiceResponse( ) { 
		columns = new ArrayList<Column>( );
		rows = new ArrayList<Object[ ]>( );
	}
	
	/** @return Whether the columns have been set. */
	public boolean hasColumns( ) { return columns.size( ) > 0; }
	
	/** @return The columns that were returned from the web service. */
	public List<Column> getColumns( ) { return columns; }
	
	/** @return The rows that were returned from the web service. */
	public List<Object[ ]> getRows( ) { return rows; }
	
	/** Add the specified column to the list.
	 * @param name The name of the column to add.
	 * @param type The type of the column to add. */
	public void addColumn(final String name, final DataType type) {
		Column column = new Column(name, type);
		if(!columns.contains(column)) {
			columns.add(column);
		}
	}
	
	/** @return A new row initialised with the correct size and already added to the row list. */
	public Object[ ] createRow( ) {
		Object[ ] row = new Object[columns.size( )];
		rows.add(row);
		return row;
	}
	
	/** Create the web service response from the supplied JSON input stream.
	 * @param in The JSON input stream to load.
	 * @return The WebServiceResponse instance created from the JSON input. */
	public static WebServiceResponse readJSONStream(final InputStream in) {
		WebServiceResponse response = new WebServiceResponse( );
		
		JsonParser parser = null;
		try {
			parser = Json.createParser(in);
			List<Object> objBuffer = new ArrayList<Object>( );
			String name = null;
			boolean startStoring = false;

			while(parser.hasNext( )) {
				switch(parser.next( )) {
					case KEY_NAME:
						name = parser.getString( );
						if(name.equals("value")) {
							startStoring = true;
						}
						break;
						
					case VALUE_STRING:
						if(startStoring) {
							objBuffer.add(parser.getString( ));
							response.addColumn(name, StringCell.TYPE);
						}
						break;
						
					case VALUE_NUMBER:
						if(startStoring) {
							objBuffer.add(Double.parseDouble(parser.getString( )));
							response.addColumn(name, DoubleCell.TYPE);
						}
						break;
						
					case VALUE_TRUE:
						if(startStoring) {
							objBuffer.add(true);
							response.addColumn(name, BooleanCell.TYPE);
						}
						break;
						
					case VALUE_FALSE:
						if(startStoring) {
							objBuffer.add(false);
							response.addColumn(name, BooleanCell.TYPE);
						}
						break;
						
					case END_OBJECT:
						if(startStoring && objBuffer.size( ) > 0) {
							Object[ ] row = response.createRow( );
							objBuffer.toArray(row);
							objBuffer.clear( );
						}
						break;
						
					default :
						// do nothing
						break;
				}
			}
		} finally {
			if(parser != null) {
				parser.close( );
			}
		}
		
		return response;
	}
	
}
