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

import java.io.IOException;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.erlwood.knime.icons.IconLoader;
import org.erlwood.knime.utils.debugging.ErlwoodMenuContributions;


public class DebuggingPreferencesUI extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public DebuggingPreferencesUI() {
		super(GRID);
	}

	/** {@inheritDoc} */
	@Override
	protected void createFieldEditors() {
		addField(new Spacer(getFieldEditorParent()));
		
		final BooleanFieldEditor bfe = new BooleanFieldEditor(CoreActivator.SHOW_DEBUGGING_MENU,
				"Show debugging menu ?", getFieldEditorParent());

		final Button checkbox = (Button) bfe.getDescriptionControl(getFieldEditorParent());

		checkbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ErlwoodMenuContributions.addRemoveMenus(bfe.getBooleanValue());
			}
		});
		
		addField(bfe);
		
		addField(new Spacer(getFieldEditorParent()));
		
		try {
		
			addField(new ImageLabel(getFieldEditorParent(),
					new Image(Display.getCurrent(), IconLoader.loadIconStream("DebugMenu.png"))));
		} catch (IOException ex) {
			// Do nothing
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void init(final IWorkbench workbench) {
		setPreferenceStore(CoreActivator.getDefault().getPreferenceStore());
		setDescription("Erlwood Debugging Preferences");
	}

	private class Spacer extends FieldEditor {
		private Label label;
		
		Spacer(Composite parent) {
			super("TXT_FIELD", "", parent);
			label.setText("");
		}

		/**
	     * {@inheritDoc}
	     */
	    @Override
	    protected void createControl(final Composite parent) {
	        label = new Label(parent, SWT.NONE);
	        super.createControl(parent); // calls doFillIntoGrid!
	    }
	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected void adjustForNumColumns(final int numColumns) {
	        Object o = label.getLayoutData();
	        if (o instanceof GridData) {
	            ((GridData)o).horizontalSpan = numColumns;
	        }
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected void doFillIntoGrid(final Composite parent, final int numColumns) {
	    	label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, numColumns, 1));
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected void doLoad() {
	        // nothing to load
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected void doLoadDefault() {
	        // nothing to do
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected void doStore() {
	        // nothing to store
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public int getNumberOfControls() {
	        return 1;
	    }
	}
}
