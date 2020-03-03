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
package org.erlwood.knime.utils.gui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.erlwood.knime.WebServiceCoreActivator;

/** Preference dialog containing the user settings allowing them to override the default values
 * populated in the web service configuration in the dialog.
 * @author Tom Wilkin */
public class WebServiceSettingsPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage
{
	
	public WebServiceSettingsPreferencePage( ) {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(WebServiceCoreActivator.getDefault( ).getPreferenceStore( ));
		setDescription("Erlwood Web Service Settings Preferences");
	}

	@Override
	protected void createFieldEditors( ) {
		addField(WebServiceCoreActivator.WEB_SERVICE_TIMEOUT, 
        		"Web Service Timeout (seconds)"
        );
        addField(WebServiceCoreActivator.WEB_SERVICE_MAX_CHILD_ELEMENTS,
        		"Web Service Max Child Elements"
        );
	}
	
	/** Add the a new field to the dialog.
	 * @param id The id to use to store the preference value.
	 * @param title The label to title the preference value. */
	private void addField(final String id, final String title) {
		addField(new IntegerFieldEditor(id, title + ":", getFieldEditorParent( )));
	}

}
