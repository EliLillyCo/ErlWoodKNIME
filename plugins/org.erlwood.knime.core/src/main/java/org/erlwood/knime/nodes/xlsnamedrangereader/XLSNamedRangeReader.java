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
 * 
 * Extended from KNIME implementation of 'XLS Writer' node to include
 * further functionality.
 * 
 * The KNIME license for this content is as follows:
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * -------------------------------------------------------------------
 *
 * History
 *   Mar 15, 2007 (ohl): created
 */
package org.erlwood.knime.nodes.xlsnamedrangereader;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Holds the settings for the XLSWriter.
 *
 * @author ohl, University of Konstanz
 * @author Luke Bullard
 */
public class XLSNamedRangeReader {

	private static final String CFGKEY_XCELLOFFSET = "xCellOffset";

    private static final String CFGKEY_YCELLOFFSET = "yCellOffset";

    private static final String CFGKEY_WRITECOLHDR = "writeColHdr";

    private static final String CFGKEY_WRITEROWHDR = "writeRowHdr";

    private static final String CFGKEY_FILENAME = "filename";
    

    private static final String CFGKEY_SHEETNAME = "sheetname";
    
    private static final String CFGKEY_NAMEDRANGE = "NamedRange";

    private static final String CFGKEY_MISSINGPATTERN = "missingPattern";
    
    private static final String CFGKEY_MERGEREFS = "mergeRefs";

    private static final String CFG_OVERWRITE_OK = "overwrite_ok";
    private static final String CFG_AUTOSTART = "autostart";
    private static final String CFG_PIVOT = "pivot";
    
    private static final String CFG_CREATE_NEW_FILE = "CFG_CREATE_NEW_FILE";
    private static final String CFG_ABORT_ON_EXISTING_SHEET = "CFG_ABORT_ON_EXISTING_SHEET";
    private static final String CFG_ABORT_ON_MISSING_FILE = "CFG_ABORT_ON_MISSING_FILE";

    private int mXCellOffset;

    private int mYCellOffset;

    private boolean mWriteColHeader;

    private boolean mWriteRowID;

    private String mFilename;

    private String mSheetname;
    
    private String mNamedRange;

    private String mMissingPattern;

    private boolean mOverwriteOK;
    
    private boolean mAutostart;
    
    private boolean mPivot;
    
    private String mMergeRefs;

    private boolean isCreateNewFile;

    private boolean isAbortOnExistingSheet;

    private boolean isAbortOnMissingFile;

    /**
     * Creates a new settings object with default settings but no filename.
     */
    public XLSNamedRangeReader() {
        mXCellOffset = 0;
        mYCellOffset = 0;
        mWriteColHeader = false;
        mWriteRowID = false;
        mFilename = null;
        mSheetname = null;
        mNamedRange = null;
        mMissingPattern = null;
        mOverwriteOK = false;
        mAutostart = false;
        mPivot = false;
        mMergeRefs = null;
    }

    /**
     * Creates a new object with the setting values read from the specified
     * settings object.
     *
     * @param settings containing the values for the writer's settings.
     * @throws InvalidSettingsException if the specified settings object
     *             contains incorrect settings.
     */
    public XLSNamedRangeReader(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        mXCellOffset = settings.getInt(CFGKEY_XCELLOFFSET);
        mYCellOffset = settings.getInt(CFGKEY_YCELLOFFSET);
        mWriteColHeader = settings.getBoolean(CFGKEY_WRITECOLHDR);
        mWriteRowID = settings.getBoolean(CFGKEY_WRITEROWHDR);
        mFilename = settings.getString(CFGKEY_FILENAME);
        mSheetname = settings.getString(CFGKEY_SHEETNAME);
        mNamedRange = settings.getString(CFGKEY_NAMEDRANGE);
        mMissingPattern = settings.getString(CFGKEY_MISSINGPATTERN);
        // option added for KNIME 2.0, we use "true" as default in cases
        // where this option is not present (old KNIME 1.x flows) in order
        // to mimic the old functionality
        mOverwriteOK = settings.getBoolean(CFG_OVERWRITE_OK, true);
        mAutostart = settings.getBoolean(CFG_AUTOSTART, false);
        mPivot = settings.getBoolean(CFG_PIVOT, false);
        try {
        	mMergeRefs = settings.getString(CFGKEY_MERGEREFS);
        	isCreateNewFile = settings.getBoolean(CFG_CREATE_NEW_FILE);
        	isAbortOnExistingSheet = settings.getBoolean(CFG_ABORT_ON_EXISTING_SHEET);
        	isAbortOnMissingFile = settings.getBoolean(CFG_ABORT_ON_MISSING_FILE);
        	
        } catch(Exception ex) {
        	// Do nothing
        }
    }

    /**
     * Saves the current values into the specified object.
     *
     * @param settings to write the values to.
     */
    public void saveSettingsTo(final NodeSettingsWO settings) {
        settings.addInt(CFGKEY_XCELLOFFSET, mXCellOffset);
        settings.addInt(CFGKEY_YCELLOFFSET, mYCellOffset);
        settings.addBoolean(CFGKEY_WRITECOLHDR, mWriteColHeader);
        settings.addBoolean(CFGKEY_WRITEROWHDR, mWriteRowID);
        settings.addString(CFGKEY_FILENAME, mFilename);
        settings.addString(CFGKEY_SHEETNAME, mSheetname);
        settings.addString(CFGKEY_NAMEDRANGE, mNamedRange);
        settings.addString(CFGKEY_MISSINGPATTERN, mMissingPattern);
        settings.addBoolean(CFG_OVERWRITE_OK, mOverwriteOK);
        settings.addBoolean(CFG_AUTOSTART, mAutostart);
        settings.addBoolean(CFG_PIVOT, mPivot);
        settings.addString(CFGKEY_MERGEREFS, mMergeRefs);
        
        settings.addBoolean(CFG_CREATE_NEW_FILE, isCreateNewFile);
        settings.addBoolean(CFG_ABORT_ON_EXISTING_SHEET, isAbortOnExistingSheet);
        settings.addBoolean(CFG_ABORT_ON_MISSING_FILE, isAbortOnMissingFile);
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return mFilename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(final String filename) {
        mFilename = filename;
    }

    /**
     * @return the sheet name
     */
    public String getSheetname() {
        return mSheetname;
    }
    
    public String getNamedRange() {
        return mNamedRange;
    }

    /**
     * @param sheetname the sheet name to set, if null, the table name will be
     *            used as sheet name
     */
    public void setSheetname(final String sheetname) {
        mSheetname = sheetname;
    }
    
    public void setNamedRange(final String NamedRange) {
        mNamedRange = NamedRange;
    }

    /**
     * @return the writeColHeader
     */
    public boolean writeColHeader() {
        return mWriteColHeader;
    }

    /**
     * @param writeColHeader the writeColHeader to set
     */
    public void setWriteColHeader(final boolean writeColHeader) {
        mWriteColHeader = writeColHeader;
    }

    /**
     * @return the writeRowID
     */
    public boolean writeRowID() {
        return mWriteRowID;
    }

    /**
     * @param writeRowID the writeRowID to set
     */
    public void setWriteRowID(final boolean writeRowID) {
        mWriteRowID = writeRowID;
    }

    /**
     * @return the xCellOffset
     */
    public int getXCellOffset() {
        return mXCellOffset;
    }

    /**
     * @param cellOffset the xCellOffset to set
     */
    public void setXCellOffset(final int cellOffset) {
        mXCellOffset = cellOffset;
    }

    /**
     * @return the yCellOffset
     */
    public int getYCellOffset() {
        return mYCellOffset;
    }

    /**
     * @param cellOffset the yCellOffset to set
     */
    public void setYCellOffset(final int cellOffset) {
        mYCellOffset = cellOffset;
    }

    /**
     * @param missingPattern the missingPattern to set. If null, no datasheet
     *            cell will be created.
     */
    public void setMissingPattern(final String missingPattern) {
        mMissingPattern = missingPattern;
    }

    /**
     * @return the missingPattern
     */
    public String getMissingPattern() {
        return mMissingPattern;
    }

    /** Set the overwrite ok property.
     * @param overwriteOK The property. */
    public void setOverwriteOK(final boolean overwriteOK) {
        mOverwriteOK = overwriteOK;
    }

    /** @return the overwrite ok property. */
    public boolean getOverwriteOK() {
        return (mOverwriteOK);
    }

    /** Set the overwrite ok property.
     * @param overwriteOK The property. */
    public void setAutostart(final boolean auto) {
        mAutostart = auto;
    }

    /** @return the overwrite ok property. */
    public boolean getAutostart() {
        return (mAutostart);
    }
    
    /** Set the overwrite ok property.
     * @param overwriteOK The property. */
    public void setPivot(final boolean pivot) {
        mPivot = pivot;
    }

    /** @return the overwrite ok property. */
    public boolean getPivot() {
        return (mPivot);
    }
    
    /**
     * @param mergeRefs the merge references to set. 
     */
    public void setMergeRefs(final String mergeRefs) {
        this.mMergeRefs = mergeRefs;
    }

    /**
     * @return the mergeRefs
     */
    public String getMergeRefs() {
        return mMergeRefs;
    }

    public boolean isCreateNewFile() {
        return isCreateNewFile;
    }
    
    public void setCreateNewFile(boolean bCreateNewFile) {
        isCreateNewFile = bCreateNewFile;
    }

    public boolean isAbortOnExistingSheet() {
        return isAbortOnExistingSheet;
    }
    
    public void setAbortOnExistingSheet(boolean bAbortOnExistingSheet) {
        isAbortOnExistingSheet = bAbortOnExistingSheet;
    }

    public void setAbortOnMissingFile(boolean bAbortOnMissingFile) {
        isAbortOnMissingFile = bAbortOnMissingFile;
    }

    public boolean isAbortOnMissingFile() {
        return isAbortOnMissingFile;
    }
}
