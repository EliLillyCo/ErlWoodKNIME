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
package org.erlwood.knime.nodes.paretoranking;

import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;
import org.erlwood.knime.utils.gui.ParetoObjectivesBean;
import org.erlwood.knime.utils.gui.RankingNodeDialog;

/**
 * <code>NodeDialog</code> for the "ParetoRanking" Node. This node performs
 * multi-objective Pareto ranking.
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Dimitar Hristozov
 */
public class ParetoRankingNodeDialog extends RankingNodeDialog {

	private ParetoObjectivesBean mPnlConfig;
	
	/**
	 * New pane for configuring the ParetoRanking node.
	 */
	protected ParetoRankingNodeDialog() {
		super(ParetoRankingNodeModel.CFG_SETTINGS);
		mPnlConfig = new ParetoObjectivesBean(this);
		((JPanel) this.getTab("Options")).add(mPnlConfig);
	}

	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
	    
	    for (String s : ((DataTableSpec)specs[0]).getColumnNames()) {
            if (s.contains(";")) {
                throw new NotConfigurableException("This node cannot handle column names with a semi-colon (;)");
            }
        }
	    
		mPnlConfig.setVisible(false);
		super.loadAdditionalSettingsFrom(settings, specs);
		mPnlConfig.fromString(getConfigString());
		mPnlConfig.setVisible(true);
		
	}

	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		super.saveAdditionalSettingsTo(settings, mPnlConfig.toString( ));
	}

}
