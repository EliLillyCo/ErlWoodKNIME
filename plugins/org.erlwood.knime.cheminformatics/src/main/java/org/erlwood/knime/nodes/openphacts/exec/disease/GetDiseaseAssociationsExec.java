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
package org.erlwood.knime.nodes.openphacts.exec.disease;

import java.util.ArrayList;
import java.util.List;

import org.erlwood.knime.nodes.openphacts.exec.AbstractExec;
import org.erlwood.knime.nodes.openphacts.settings.OpenPhactsSettings;
import org.erlwood.knime.nodes.openphacts.settings.disease.GetDiseaseAssociationsSettings;
import org.erlwood.knime.utils.clients.RESTWebServiceClient.RESTParameter;
import org.erlwood.knime.utils.settings.AbstractColumnAndValueSettings;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;

public class GetDiseaseAssociationsExec extends AbstractExec {
	private AbstractColumnAndValueSettings s1 = null;
	
	private int inUriIndex;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getMethod() {
		return "disease/assoc/byDisease";
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialiseSettings(BufferedDataTable input, OpenPhactsSettings openPhactsSettings) {
				
		GetDiseaseAssociationsSettings s = openPhactsSettings.getDiseaseAssociationsSettings();
				
		s1 = s.getURISettings();
		
		inUriIndex = s1.isUseColumn() ? input.getSpec().findColumnIndex(s1.getColumnName()) : -1;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<RESTParameter<?>> getParams(DataRow row) {
		List<RESTParameter<?>> retValue = new ArrayList<RESTParameter<?>>();
		
		String uri = inUriIndex != -1 ? row.getCell(inUriIndex).toString() : s1.getValue();
		
		retValue.add(new RESTParameter<String>("uri", uri));
		retValue.add(new RESTParameter<String>("_pageSize", "all"));
		
		return retValue;
	}

}
