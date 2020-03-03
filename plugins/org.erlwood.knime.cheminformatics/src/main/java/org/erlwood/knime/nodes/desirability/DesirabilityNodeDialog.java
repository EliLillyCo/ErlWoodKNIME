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
package org.erlwood.knime.nodes.desirability;

import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;
import org.erlwood.knime.utils.gui.DesirabilityObjectivesBean;
import org.erlwood.knime.utils.gui.RankingNodeDialog;

/**
 * <code>NodeDialog</code> for the "Desirability" Node. Modification of the
 * Pareto Node to perform as a Desirability Node, following standard
 * Desirability criterion.
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author David Thorner & Dimitar Hristozov
 */
public class DesirabilityNodeDialog extends RankingNodeDialog {

	private DesirabilityObjectivesBean mPnlConfig;

	/**
	 * New pane for configuring Desirability node dialog. This is just a
	 * suggestion to demonstrate possible default dialog components.
	 */
	protected DesirabilityNodeDialog() {
		super(DesirabilityNodeModel.CFG_SETTINGS);
		mPnlConfig = new DesirabilityObjectivesBean(this);
		((JPanel) this.getTab("Options")).add(mPnlConfig);
	}

	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {

		super.loadAdditionalSettingsFrom(settings, specs);
		mPnlConfig.fromString(getConfigString());
	}

	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		super.saveAdditionalSettingsTo(settings, mPnlConfig.toString( ));
	}

}
