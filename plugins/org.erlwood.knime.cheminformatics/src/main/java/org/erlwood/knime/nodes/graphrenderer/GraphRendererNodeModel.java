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
package org.erlwood.knime.nodes.graphrenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.knime.base.node.util.DataArray;
import org.knime.base.node.util.DefaultDataArray;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ContainerTable;
import org.knime.core.data.container.DataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * This is the model implementation of GraphRenderer. A graph renderer using
 * prefuse java2d library
 * 
 * @author Swanand Gore
 */
@SuppressWarnings("all")
public class GraphRendererNodeModel extends NodeModel {

	private DataArray viewdata = null, matdata = null;
	private int smilescolIndex = -1, namescolIndex = -1, propscolIndex = -1;
	private double cliffcutoff = -999., edgecutoff = -999.;
	private static final NodeLogger LOG = NodeLogger
			.getLogger(GraphRendererNodeModel.class);

	static final String CFG_CLIFFCUT = "GRAPHRENDERERNODEMODEL_CLIFFCUT";
	static final double DEFAULT_CLIFFCUT = 100;
	private SettingsModelDouble cliffcut = new SettingsModelDouble(
			GraphRendererNodeModel.CFG_CLIFFCUT,
			GraphRendererNodeModel.DEFAULT_CLIFFCUT);

	static final String CFG_EDGECUT = "GRAPHRENDERERNODEMODEL_EDGECUT";
	static final double DEFAULT_EDGECUT = 0.5;
	private SettingsModelDouble edgecut = new SettingsModelDouble(
			GraphRendererNodeModel.CFG_EDGECUT,
			GraphRendererNodeModel.DEFAULT_EDGECUT);

	static final String CFG_SMILESCOL = "GRAPHRENDERERNODEMODEL_SMILES_COLUMN";
	static final String DEFAULT_SMILESCOL = "";
	private SettingsModelString smilescol = new SettingsModelString(
			GraphRendererNodeModel.CFG_SMILESCOL,
			GraphRendererNodeModel.DEFAULT_SMILESCOL);

	static final String CFG_PROPCOL = "GRAPHRENDERERNODEMODEL_PROPERTY_COLUMN";
	static final String DEFAULT_PROPCOL = "";
	private SettingsModelString propscol = new SettingsModelString(
			GraphRendererNodeModel.CFG_PROPCOL,
			GraphRendererNodeModel.DEFAULT_PROPCOL);

	static final String CFG_NAMECOL = "GRAPHRENDERERNODEMODEL_NAME_COLUMN";
	static final String DEFAULT_NAMECOL = "";
	private SettingsModelString namescol = new SettingsModelString(
			GraphRendererNodeModel.CFG_NAMECOL,
			GraphRendererNodeModel.DEFAULT_NAMECOL);

	/**
	 * Constructor for the node model.
	 */
	protected GraphRendererNodeModel() {
		super(2, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		setViewData(new DefaultDataArray(inData[0], 1, inData[0].getRowCount(),
				exec));
		setMatData(new DefaultDataArray(inData[1], 1, inData[1].getRowCount(),
				exec));

		setEdgeCutoff(edgecut.getDoubleValue());
		setCliffCutoff(edgecut.getDoubleValue());
		setSmilesColIndex(inData[0].getSpec().findColumnIndex(
				smilescol.getStringValue()));
		setNamesColIndex(inData[0].getSpec().findColumnIndex(
				namescol.getStringValue()));
		setPropsColIndex(inData[0].getSpec().findColumnIndex(
				propscol.getStringValue()));
		LOG.debug("Smiles Col Index " + getSmilesColIndex() + " "
				+ getNamesColIndex() + " " + getPropsColIndex());

		BufferedDataTable[] retTables = { inData[0] };
		return retTables;
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
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		return new DataTableSpec[] { inSpecs[0] };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		edgecut.saveSettingsTo(settings);
		cliffcut.saveSettingsTo(settings);
		smilescol.saveSettingsTo(settings);
		namescol.saveSettingsTo(settings);
		propscol.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		cliffcut.loadSettingsFrom(settings);
		edgecut.loadSettingsFrom(settings);
		smilescol.loadSettingsFrom(settings);
		namescol.loadSettingsFrom(settings);
		propscol.loadSettingsFrom(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		edgecut.validateSettings(settings);
		cliffcut.validateSettings(settings);
		smilescol.validateSettings(settings);
		namescol.validateSettings(settings);
		propscol.validateSettings(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		try {
			File f = new File(internDir, "activityCliffs_viz");
			FileInputStream fis = new FileInputStream(f);
			NodeSettingsRO settings = NodeSettings.loadFromXML(fis);
			fis.close();
			setSmilesColIndex(settings.getInt("smilescolIndex"));
			setNamesColIndex(settings.getInt("namescolIndex"));
			setPropsColIndex(settings.getInt("propscolIndex"));
			setEdgeCutoff(settings.getDouble("edgecutoff"));
			setCliffCutoff(settings.getDouble("cliffcutoff"));
			File input0 = new File(internDir, "activityCliffData0");
			ContainerTable table0 = DataContainer.readFromZip(input0);
			setViewData(new DefaultDataArray(table0, 1, table0.getRowCount(),
					exec));
			File input1 = new File(internDir, "activityCliffData1");
			ContainerTable table1 = DataContainer.readFromZip(input1);
			setMatData(new DefaultDataArray(table1, 1, table1.getRowCount(),
					exec));

		} catch (Exception e) {
			throw new IOException(
					"Unable to load internals: " + e.getMessage(), e);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		try {
			NodeSettings settings = new NodeSettings("activityCliffsInternals");

			settings.addInt("smilescolIndex", getSmilesColIndex());
			settings.addInt("namescolIndex", getNamesColIndex());
			settings.addInt("propscolIndex", getPropsColIndex());
			settings.addDouble("edgecutoff", getEdgeCutoff());
			settings.addDouble("cliffcutoff", getCliffCutoff());
			File f = new File(internDir, "activityCliffs_viz");
			FileOutputStream fos = new FileOutputStream(f);
			settings.saveToXML(fos);
			fos.close();

			File input0 = new File(internDir, "activityCliffData0");
			DataContainer.writeToZip(getViewData(), input0, exec);
			File input1 = new File(internDir, "activityCliffData1");
			DataContainer.writeToZip(getMatData(), input1, exec);
		} catch (Exception e) {
			throw new IOException(
					"Unable to save internals: " + e.getMessage(), e);
		}

	}

	public double getCliffCutoff() {
		return cliffcutoff;
	}

	public void setCliffCutoff(double cliffcutoff) {
		this.cliffcutoff = cliffcutoff;
	}

	public double getEdgeCutoff() {
		return edgecutoff;
	}

	public void setEdgeCutoff(double edgecutoff) {
		this.edgecutoff = edgecutoff;
	}

	public DataArray getViewData() {
		return viewdata;
	}

	public void setViewData(DataArray viewdata) {
		this.viewdata = viewdata;
	}

	public DataArray getMatData() {
		return matdata;
	}

	public void setMatData(DataArray matdata) {
		this.matdata = matdata;
	}

	public int getSmilesColIndex() {
		return smilescolIndex;
	}

	public void setSmilesColIndex(int smilescolIndex) {
		this.smilescolIndex = smilescolIndex;
	}

	public int getNamesColIndex() {
		return namescolIndex;
	}

	public void setNamesColIndex(int namescolIndex) {
		this.namescolIndex = namescolIndex;
	}

	public int getPropsColIndex() {
		return propscolIndex;
	}

	public void setPropsColIndex(int propscolIndex) {
		this.propscolIndex = propscolIndex;
	}

}
