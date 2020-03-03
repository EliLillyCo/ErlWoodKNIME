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
package org.erlwood.knime;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/** Preferences for the Web Viewer.
 * @author Tom Wilkin
 */
public class WebViewerPreferencesUI extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {
    
    
   public WebViewerPreferencesUI() {
        super(GRID);
        
    }
    
    /** {@inheritDoc} */
    @Override
    protected void createFieldEditors() {
    	addField(new StringFieldEditor(CoreActivator.WEBVIEWER_URL, 
    			"Web Viewer Home Page", getFieldEditorParent( )));
    	addField(new BooleanFieldEditor(CoreActivator.WEBVIEWER_REFRESH, 
    			"Should the viewer periodically refresh?", 
    			getFieldEditorParent( )));
    	addField(new IntegerFieldEditor(CoreActivator.WEBVIEWER_REFRESH_INTERVAL,
    			"Refresh Interval (seconds)", getFieldEditorParent( )));
   }

    /**
     * {@inheritDoc}
     */
    public void init(final IWorkbench workbench) {
    	setPreferenceStore(CoreActivator.getDefault().getPreferenceStore());
        setDescription("Erlwood Web Viewer Preferences");
    }
	
}
