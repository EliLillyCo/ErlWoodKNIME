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
package org.erlwood.knime.nodes.openphacts.exec.chemicalstructuresearch;

import java.util.ArrayList;
import java.util.List;

import org.erlwood.knime.nodes.openphacts.exec.AbstractExec;
import org.erlwood.knime.nodes.openphacts.settings.OpenPhactsSettings;
import org.erlwood.knime.nodes.openphacts.settings.chemicalstructuresearch.GetCompoundsBySimilaritySettings;
import org.erlwood.knime.utils.clients.RESTWebServiceClient.RESTParameter;
import org.erlwood.knime.utils.settings.AbstractColumnAndValueSettings;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;

public class GetCompoundsBySimilarityExec extends AbstractExec {
	public static enum eSimilarityType {
		Tanimoto,
		Tversky,
		Euclidian;
	}
	
	private AbstractColumnAndValueSettings s1 = null;
		
	private int inSmilesIndex;
	private eSimilarityType similarityType;
	private double threshold;
	private double tverskyA;
	private double tverskyB;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getMethod() {
		return "structure/similarity";
	}
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialiseSettings(BufferedDataTable input, OpenPhactsSettings openPhactsSettings) {
						
		GetCompoundsBySimilaritySettings s = openPhactsSettings.getCompoundsBySimilaritySettings();
		
		s1 = s.getSmilesSettings();
		
		similarityType = eSimilarityType.valueOf(s.getSimilarityTypeSettings().getValue());
		threshold = s.getThresholdSetting().getDoubleValue();
		tverskyA = s.getTverskyASetting().getDoubleValue();
		tverskyB = s.getTverskyBSetting().getDoubleValue();
			
		
		inSmilesIndex = s1.isUseColumn() ? input.getSpec().findColumnIndex(s1.getColumnName()) : -1;
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<RESTParameter<?>> getParams(DataRow row) {
		List<RESTParameter<?>> retValue = new ArrayList<RESTParameter<?>>();
		
		String smiles = inSmilesIndex != -1 ? row.getCell(inSmilesIndex).toString() : s1.getValue();
		
		retValue.add(new RESTParameter<String>("searchOptions.Molecule", smiles));
		retValue.add(new RESTParameter<Integer>("searchOptions.SimilarityType", similarityType.ordinal()));
		retValue.add(new RESTParameter<Double>("searchOptions.Threshold", threshold));
		
		retValue.add(new RESTParameter<Double>("searchOptions.Alpha", tverskyA));
		retValue.add(new RESTParameter<Double>("searchOptions.Beta", tverskyB));
		
		return retValue;
	}

	
	



	
	
}
