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
package org.erlwood.knime.nodes.openphacts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.erlwood.knime.CheminformaticsActivator;
import org.erlwood.knime.WebServiceCoreActivator;
import org.erlwood.knime.nodes.openphacts.exec.assay.GetAssayExec;
import org.erlwood.knime.nodes.openphacts.exec.chemicalstructuresearch.GetCompoundURIExec;
import org.erlwood.knime.nodes.openphacts.exec.chemicalstructuresearch.GetCompoundsBySimilarityExec;
import org.erlwood.knime.nodes.openphacts.exec.chemicalstructuresearch.GetCompoundsBySubstructureExec;
import org.erlwood.knime.nodes.openphacts.exec.classes.GetChildClassesExec;
import org.erlwood.knime.nodes.openphacts.exec.classes.GetParentClassesExec;
import org.erlwood.knime.nodes.openphacts.exec.classes.GetRootClassesExec;
import org.erlwood.knime.nodes.openphacts.exec.compound.GetCompoundClassesExec;
import org.erlwood.knime.nodes.openphacts.exec.compound.GetCompoundExec;
import org.erlwood.knime.nodes.openphacts.exec.compound.GetCompoundPharmacologyExec;
import org.erlwood.knime.nodes.openphacts.exec.compound.GetCompoundsByClassExec;
import org.erlwood.knime.nodes.openphacts.exec.disease.GetDiseaseAssociationsExec;
import org.erlwood.knime.nodes.openphacts.exec.disease.GetDiseaseExec;
import org.erlwood.knime.nodes.openphacts.exec.disease.GetDiseasesByTargetExec;
import org.erlwood.knime.nodes.openphacts.exec.disease.GetTargetAssociationsExec;
import org.erlwood.knime.nodes.openphacts.exec.disease.GetTargetsByDiseaseExec;
import org.erlwood.knime.nodes.openphacts.exec.general.FreeTextSearchExec;
import org.erlwood.knime.nodes.openphacts.exec.target.GetTargetClassesExec;
import org.erlwood.knime.nodes.openphacts.exec.target.GetTargetCompoundClassesExec;
import org.erlwood.knime.nodes.openphacts.exec.target.GetTargetExec;
import org.erlwood.knime.nodes.openphacts.exec.target.GetTargetPharmacologyByClassExec;
import org.erlwood.knime.nodes.openphacts.exec.target.GetTargetPharmacologyExec;
import org.erlwood.knime.nodes.openphacts.exec.target.GetTargetsByClassExec;
import org.erlwood.knime.nodes.openphacts.settings.OpenPhactsSettings;
import org.erlwood.knime.utils.node.AbstractWebServiceNodeModel;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.DataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;

/**
 * This is the model implementation of OpenPhacts node. 
 * 
 * @author Luke Bullard
 */
public class OpenPhactsNodeModel extends AbstractWebServiceNodeModel {
	private static final NodeLogger LOG = NodeLogger.getLogger(OpenPhactsNodeModel.class);

	private OpenPhactsSettings	openPhactsSettings = new OpenPhactsSettings();
	
	private DataTable rawDataTable;
	
	public enum eMethod {	
		//	
		GetAssay("Get Assay", "Assays", GetAssayExec.class),
		
		//	Chemical Structure Search
		GetCompoundURI("Get Compound URI", "Chemical Structure Search", GetCompoundURIExec.class),
		GetCompoundsBySimilarity("Get Compounds By Similarity", "Chemical Structure Search", GetCompoundsBySimilarityExec.class),
		GetCompoundsBySubstructure("Get Compounds By Substructure", "Chemical Structure Search", GetCompoundsBySubstructureExec.class),
		
		//	Compound
		GetCompound("Get Compound", "Compounds", GetCompoundExec.class),
		GetCompoundClasses("Get Compound Classes", "Compounds", GetCompoundClassesExec.class),
		GetCompoundsByClass("Get Compounds By Class", "Compounds", GetCompoundsByClassExec.class),
		GetCompoundPharmacology("Get Compound Pharmacology", "Compounds", GetCompoundPharmacologyExec.class),		
		
		//	Classes
		GetRootClasses("Get Root Classes", "Classes", GetRootClassesExec.class),
		GetChildClasses("Get Child Classes", "Classes", GetChildClassesExec.class),
		GetParentClasses("Get Parent Classes", "Classes", GetParentClassesExec.class),
		
		//	Diseases
		GetDisease("Get Disease", "Diseases", GetDiseaseExec.class),
		GetDiseaseAssociations("Get Disease Associations", "Diseases", GetDiseaseAssociationsExec.class),
		GetTargetAssociations("Get Target Associations", "Diseases", GetTargetAssociationsExec.class),
		GetDiseasesByTarget("Get Diseases By Target", "Diseases", GetDiseasesByTargetExec.class),
		GetTargetsByDisease("Get Targets By Disease", "Diseases", GetTargetsByDiseaseExec.class),
		
		//	Targets
		GetTarget("Get Target", "Targets", GetTargetExec.class),
		GetTargetClasses("Get Target Classes", "Targets", GetTargetClassesExec.class),
		GetTargetCompoundClasses("Get Target Compound Classes", "Targets", GetTargetCompoundClassesExec.class),
		GetTargetsByClass("Get Targets By Class", "Targets", GetTargetsByClassExec.class),
		GetTargetPharmacology("Get Target Pharmacology", "Targets", GetTargetPharmacologyExec.class),
		GetTargetPharmacologyByClass("Get Target Pharmacology By Class", "Targets", GetTargetPharmacologyByClassExec.class),
		
		//	Free Text Search
		FreeTextSearch("Free Text Search", "General", FreeTextSearchExec.class);
		
		private final String desc;
		private final String category;
		private final Class<? extends IExec> implementation;
		
		private eMethod(String d, String cat, Class<? extends IExec> c) {
			this.desc = d;
			this.category = cat;
			this.implementation = c;
		}
		
		@Override
		public String toString() {
			return desc;
		}
		
		public String getCategory() {
			return category;
		}
		
		@SuppressWarnings("unchecked")
		public IExec<OpenPhactsSettings> getExec() throws Exception {
			return implementation.newInstance();
		}
	}
	
	public enum eFormat {
		NONE,
		JSON,
		XML
	}
	
	/**
	 * Constructor for the node model.
	 * @throws URISyntaxException 
	 */
	protected OpenPhactsNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE_OPTIONAL }, new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE });
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

	    BufferedDataTable input = (BufferedDataTable) inData[0];
	    BufferedDataTable[] output = null;
	    
	    LOG.debug("Executing OpenPhacts method " + openPhactsSettings.getMethod());
	    
	    IExec methodExec = openPhactsSettings.getMethod().getExec();
	    
	    methodExec.setup(getAvailableFlowVariables(org.knime.core.node.workflow.VariableType.StringType.INSTANCE, org.knime.core.node.workflow.VariableType.DoubleType.INSTANCE, org.knime.core.node.workflow.VariableType.IntType.INSTANCE), getCredentialsProvider(), wsSettings, input, openPhactsSettings);
	    output = methodExec.executeCall(input, exec);	  
	    if (methodExec.getWarningMessage() != null) {
	    	setWarningMessage(methodExec.getWarningMessage());
	    }
	   	    	    
	   	rawDataTable = output[1];
	 	    
		return new PortObject[] { output[0], (output[1] == null ? InactiveBranchPortObject.INSTANCE : output[1]) };

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		super.configure(inSpecs);
		return new DataTableSpec[] { null, null };
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		openPhactsSettings.saveSettingsTo(settings);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		super.loadValidatedSettingsFrom(settings);
		openPhactsSettings.loadSettingsFrom(settings);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		super.validateSettings(settings);
		openPhactsSettings.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		File f = new File(internDir, "raw_data.zip");
		if ( f.exists()) {
			rawDataTable = DataContainer.readFromZip(f);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		if (rawDataTable != null) {
			File f = new File(internDir, "raw_data.zip");
			DataContainer.writeToZip(rawDataTable, f, exec);
		}
	}


	public DataTable getRawDataTable() {
		return rawDataTable;
	}

	@Override
	public String getURLPreferenceId() {
		return CheminformaticsActivator.OPEN_PHACTS_URL;
	}

}
