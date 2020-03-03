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
package org.erlwood.knime.nodes.moleculedifferencechecker;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

/** This is the dialog implementation of MoleculeDifferenceChecker.
 * 
 * @author Tom Wilkin */
public class MoleculeDifferenceCheckerNodeDialog extends DefaultNodeSettingsPane
{
	
	// load the settings models
	private final SettingsModelBoolean mFailOnFirstError = 
			createSettingsModelFailOnFirstError( );

	MoleculeDifferenceCheckerNodeDialog( ) {
		addDialogComponent(new DialogComponentBoolean(mFailOnFirstError, 
				"Fail on First Error"));
	}
	
	/** @return The SettingsModel for the 'FailOnFirstError' option. */
	public static SettingsModelBoolean createSettingsModelFailOnFirstError( ) {
		return new SettingsModelBoolean("failOnFirstError", true);
	}
	
};
