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

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Preference page field showing text.
 *
 * @author Luke Bullard
 */
public class ImageLabel extends FieldEditor {

    private Label label;

    /**
     * @param parent
     * @param text to show
     */
    ImageLabel(final Composite parent, final Image img) {
        super("TXT_FIELD", "", parent);
        label.setImage(img);       
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
