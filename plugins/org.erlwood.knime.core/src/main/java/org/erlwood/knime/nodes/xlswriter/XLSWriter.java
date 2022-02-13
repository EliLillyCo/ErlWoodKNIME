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
package org.erlwood.knime.nodes.xlswriter;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;

/**
 * 
 * @author ohl, University of Konstanz
 * @author Luke Bullard
 */
public class XLSWriter {

	private static final NodeLogger LOGGER = NodeLogger
			.getLogger(XLSWriter.class);

	/**
	 * Excel (Ver. 2003) can handle datasheets up to 64k x 256 cells!
	 */
	private static final int MAX_NUM_OF_ROWS = 65536;

	private static final int MAX_NUM_OF_COLS = 256;

	private final XLSWriterSettings mSettings;
	private final Map<String, DoubleColumnSettings> h = new Hashtable<String, DoubleColumnSettings>();
	private final Map<String, StringColumnSettings> s = new Hashtable<String, StringColumnSettings>();

    private CellStyle red;

    private CellStyle yellow;

    private CellStyle green;

    private CellStyle blue;

    private CellStyle white;

	/**
	 * Creates a new writer with the specified settings.
	 * 
	 * @param outStream
	 *            the created workbook will be written to.
	 * @param settings
	 *            the settings.
	 */
	public XLSWriter(
			final XLSWriterSettings settings,
			final DoubleColumnSettings[] cSettings,
			final StringColumnSettings[] sSettings) {
		if (settings == null) {
			throw new IllegalArgumentException ("Can't operate with null settings!");
		}
	
		mSettings = settings;
		h.clear();

		for (DoubleColumnSettings c : cSettings) {
			if (c.isColorable()) {
				h.put(c.getName(), c);
			}
		}

		s.clear();

		for (StringColumnSettings c : sSettings) {
			if (c.isColourable()) {
				s.put(c.getName(), c);
			}
		}
	}

	private void setCellStyles(HSSFWorkbook wb) {
	    red = wb.createCellStyle();
        yellow = wb.createCellStyle();
        green = wb.createCellStyle();
        blue = wb.createCellStyle();
        white = wb.createCellStyle();
       
        red.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        yellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        green.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        blue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        white.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        
            setHSSFColors((HSSFWorkbook) wb);
       
	}
	
	private void setCellStyles(XSSFWorkbook wb) {
        red = wb.createCellStyle();
        yellow = wb.createCellStyle();
        green = wb.createCellStyle();
        blue = wb.createCellStyle();
        white = wb.createCellStyle();
       
        red.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        yellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        green.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        blue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        white.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
       
        setXSSFColors((XSSFWorkbook) wb);

    }
	private void setHSSFColors(HSSFWorkbook wb) {
	    wb.getCustomPalette().setColorAtIndex((short) 41,
	                                          Integer.valueOf(255).byteValue(), Integer.valueOf(170).byteValue(),
	                                          Integer.valueOf(170).byteValue());

      red.setFillForegroundColor((short) 41);
      
          
      yellow.setFillForegroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
      
  
      green.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex());
      
      
      
      wb.getCustomPalette().setColorAtIndex((short) 39,
              Integer.valueOf(135).byteValue(), Integer.valueOf(206).byteValue(),
              Integer.valueOf(250).byteValue());

      blue.setFillForegroundColor((short) 39);
      
      
      
      white.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
	}
	
	private void setXSSFColors(XSSFWorkbook wb) {
	  IndexedColorMap colorMap = wb.getStylesSource().getIndexedColors();
	  
      ((XSSFCellStyle)red).setFillForegroundColor(new XSSFColor(new Color(255, 170, 170), colorMap));
               
      ((XSSFCellStyle)yellow).setFillForegroundColor(new XSSFColor(Color.YELLOW, colorMap));
      
      ((XSSFCellStyle)green).setFillForegroundColor(new XSSFColor(new Color(204, 255, 204), colorMap));
      
      ((XSSFCellStyle)blue).setFillForegroundColor(new XSSFColor(new Color(135, 206, 250), colorMap));
  
      ((XSSFCellStyle)white).setFillForegroundColor(new XSSFColor(Color.WHITE, colorMap));
    }
	
	private Workbook creatWorkbook() {
	    Workbook wb = null;
	    if (mSettings.getFilename().toLowerCase().endsWith(".xlsx")) {
            wb = new XSSFWorkbook();
            setCellStyles((XSSFWorkbook)wb);
            
        } else {
            wb = new HSSFWorkbook();
            setCellStyles((HSSFWorkbook)wb);
            if (((HSSFWorkbook)wb).isWriteProtected()) {
                ((HSSFWorkbook)wb).unwriteProtectWorkbook();
            }
        }
	    return wb;
	}
	
	private Workbook getWorkbook() throws Exception {
	    
	    Workbook wb = null;
	    
	    if (mSettings.isCreateNewFile()) {
    	    wb = creatWorkbook();
	    } else {
	    
	        File f = new File(mSettings.getFilename());
	        if (!f.exists()) {
	            wb = creatWorkbook();
	        } else {
    	        InputStream inp = new FileInputStream(mSettings.getFilename());
    	        wb = WorkbookFactory.create(inp);
    	        
    	        if (wb instanceof HSSFWorkbook) {
    	            setCellStyles((HSSFWorkbook)wb);
    	        } else {
    	            setCellStyles((XSSFWorkbook)wb);
    	        }
	        }
	    }
        return wb;
	}
	
	private Sheet getSheet(Workbook wb, final DataTable table) throws Exception {
	    String sheetName = mSettings.getSheetname();
        if ((sheetName == null) || (sheetName.trim().length() == 0)) {
            sheetName = table.getDataTableSpec().getName();
        }
        // max sheetname length is 32 incl. added running index. We cut it to 25
        if (sheetName.length() > 25) {
            sheetName = sheetName.substring(0, 22) + "...";
        }
        // replace characters like \ / * ? [ ] etc.
        sheetName = replaceInvalidChars(sheetName);

        if (mSettings.isCreateNewFile()) {
            return wb.createSheet(sheetName);
        }
        
        Sheet sheet = wb.getSheet(sheetName);
        if (sheet != null) {
            if (mSettings.isAbortOnExistingSheet()) {         
                throw new IllegalStateException("Sheet - '" + sheetName + "' already exists!");
            }
           
            return sheet;
        }
        return wb.createSheet(sheetName);
	}
	
	/**
	 * Writes <code>table</code> with current settings.
	 * 
	 * @param table
	 *            the table to write to the file
	 * @param exec
	 *            an execution monitor where to check for cancelled status and
	 *            report progress to. (In case of cancellation, the file will be
	 *            deleted.)
	 * @throws IOException
	 *             if any related I/O error occurs
	 * @throws CanceledExecutionException
	 *             if execution in <code>exec</code> has been cancelled
	 * @throws NullPointerException
	 *             if table is <code>null</code>
	 */
	public void write(final DataTable table, final ExecutionMonitor exec)
			throws Exception, CanceledExecutionException {

	    Workbook wb = getWorkbook();
	    
		// in case the table doesn't fit in one sheet
		int sheetIdx = 0;
		Sheet sheet = getSheet(wb, table);
		String sheetName = sheet.getSheetName();
		
		DataTableSpec inSpec = table.getDataTableSpec();
		int numOfCols = inSpec.getNumColumns();
		int rowHdrIncr = mSettings.writeRowID() ? 1 : 0;

		if (numOfCols + rowHdrIncr > MAX_NUM_OF_COLS) {
			LOGGER.warn("The table to write has too many columns! Can't put"
					+ " more than " + MAX_NUM_OF_COLS
					+ " columns in one sheet." + " Truncating columns "
					+ (MAX_NUM_OF_COLS + 1) + " to " + numOfCols);
			numOfCols = MAX_NUM_OF_COLS - rowHdrIncr;
		}
		int numOfRows = -1;
		if (table instanceof BufferedDataTable) {
			numOfRows = ((BufferedDataTable) table).getRowCount();
		}

		// the index of the row in the XLsheet
		int rowIdx = 0;
		// the index of the cell in the XLsheet
		int colIdx = 0;

		// write column names
		if (mSettings.writeColHeader()) {

			// Create a new row in the sheet
			Row hdrRow = sheet.createRow(rowIdx++);

			if (mSettings.writeRowID()) {
				hdrRow.createCell(colIdx++).setCellValue("row ID");
			}
			for (int c = 0; c < numOfCols; c++) {
				String cName = inSpec.getColumnSpec(c).getName();
				cName = WordUtils.wrap(cName, 15, "\n", true);

				hdrRow.createCell(colIdx++).setCellValue(cName);

			}

		} // end of if write column names

		// Guess 80% of the job is generating the sheet, 20% is writing it out
		ExecutionMonitor e = exec.createSubProgress(0.8);

		// write each row of the data
		int rowCnt = 0;
		for (DataRow tableRow : table) {

			colIdx = 0;

			// create a new sheet if the old one is full
			if (rowIdx >= MAX_NUM_OF_ROWS) {
				sheetIdx++;
				sheet = wb.createSheet(sheet.getSheetName() + "(" + sheetIdx + ")");
				rowIdx = 0;
				LOGGER.info("Creating additional sheet to store entire table."
						+ "Additional sheet name: " + sheetName + "("
						+ sheetIdx + ")");
			}

			// set the progress
			String rowID = tableRow.getKey().getString();
			String msg;
			if (numOfRows <= 0) {
				msg = "Writing row " + (rowCnt + 1) + " (\"" + rowID + "\")";
			} else {
				msg = "Writing row " + (rowCnt + 1) + " (\"" + rowID
						+ "\") of " + numOfRows;
				e.setProgress(rowCnt / (double) numOfRows, msg);
			}
			// Check if execution was cancelled !
			exec.checkCanceled();

			// Create a new row in the sheet
			Row sheetRow = sheet.createRow(rowIdx++);

			// add the row id
			if (mSettings.writeRowID()) {
				sheetRow.createCell(colIdx++).setCellValue(rowID);
			}
			// now add all data cells
			for (int c = 0; c < numOfCols; c++) {

				DataCell colValue = tableRow.getCell(c);

				if (colValue.isMissing()) {
					String miss = mSettings.getMissingPattern();
					if (miss != null) {
						sheetRow.createCell(colIdx).setCellValue(miss);
					}
				} else {
					Cell sheetCell = sheetRow.createCell(colIdx);

					if (colValue.getType().isCompatible(DoubleValue.class)) {
						double val = ((DoubleValue) colValue).getDoubleValue();
						sheetCell.setCellValue(val);
						if (h.containsKey(inSpec.getColumnSpec(c).getName())
								&& h.get(inSpec.getColumnSpec(c).getName())
										.isActive()) {
							short col = mapColor(
									h.get(inSpec.getColumnSpec(c).getName()),
									val);
							if (col == HSSFColor.HSSFColorPredefined.RED.getIndex()) {
								sheetCell.setCellStyle(red);
							}
							if (col == HSSFColor.HSSFColorPredefined.YELLOW.getIndex()) {
								sheetCell.setCellStyle(yellow);
							}
							if (col == HSSFColor.HSSFColorPredefined.GREEN.getIndex()) {
								sheetCell.setCellStyle(green);
							}

						}
					} else if (colValue.getType().isCompatible(
							StringValue.class)) {
						String val = ((StringValue) colValue).getStringValue();
						sheetCell.setCellValue(val);
						if (s.containsKey(inSpec.getColumnSpec(c).getName())
								&& s.get(inSpec.getColumnSpec(c).getName())
										.isActive()) {
							short col = mapColor(
									s.get(inSpec.getColumnSpec(c).getName()),
									val);
							if (col == HSSFColor.HSSFColorPredefined.RED.getIndex()) {
								sheetCell.setCellStyle(red);
							}
							if (col == HSSFColor.HSSFColorPredefined.YELLOW.getIndex()) {
								sheetCell.setCellStyle(yellow);
							}
							if (col == HSSFColor.HSSFColorPredefined.GREEN.getIndex()) {
								sheetCell.setCellStyle(green);
							}
							if (col == HSSFColor.HSSFColorPredefined.BLUE.getIndex()) {
								sheetCell.setCellStyle(blue);
							}
							if (col == HSSFColor.HSSFColorPredefined.WHITE.getIndex()) {
								sheetCell.setCellStyle(white);
							}

						}

					} else {
						String val = colValue.toString();
						sheetCell.setCellValue(val);
					}

				}

				colIdx++;
			}

			rowCnt++;
		} // end of for all rows in table

		//	Now perform any merge operations that are required
		if (mSettings.getMergeRefs() != null && !mSettings.getMergeRefs().isEmpty()) {
			String[] split = mSettings.getMergeRefs().split(",");
			for (String sp : split) {
				sheet.addMergedRegion(CellRangeAddress.valueOf(sp));
			}
		}
		
		
		// Write the output to a file
		OutputStream mOutStream = new FileOutputStream(mSettings.getFilename());
		wb.write(mOutStream);
		mOutStream.close();


		// Only auto start if we are NOT headless..
		if (mSettings.getAutostart()
				&& !GraphicsEnvironment.getLocalGraphicsEnvironment()
						.isHeadlessInstance()) {
			String fi = (new File(mSettings.getFilename())).getAbsolutePath();
			fi = fi.replaceAll("\\s", "%20");
			String[] commands = { "cmd", "/C", "start", "file:" + fi };
			Runtime.getRuntime().exec(commands);
		}
	}

	public void writePivoted(final DataTable table, final ExecutionMonitor exec)
			throws Exception, CanceledExecutionException {

	    Workbook wb = getWorkbook();
	
		// in case the table doesn't fit in one sheet
		int sheetIdx = 0;
		Sheet sheet = getSheet(wb, table);
		String sheetName = sheet.getSheetName();

		DataTableSpec inSpec = table.getDataTableSpec();
		int numOfCols = inSpec.getNumColumns();
		int rowHdrIncr = mSettings.writeRowID() ? 1 : 0;

		if (numOfCols + rowHdrIncr > MAX_NUM_OF_COLS) {
			LOGGER.warn("The table to write has too many columns! Can't put"
					+ " more than " + MAX_NUM_OF_COLS
					+ " columns in one sheet." + " Truncating columns "
					+ (MAX_NUM_OF_COLS + 1) + " to " + numOfCols);
			numOfCols = MAX_NUM_OF_COLS - rowHdrIncr;
		}
		int numOfRows = -1;
		if (table instanceof BufferedDataTable) {
			numOfRows = ((BufferedDataTable) table).getRowCount();
		}
		int effRow = numOfRows;
		int effCols = numOfCols;
		if (mSettings.writeColHeader()) {
			effRow++;
		}
		if (mSettings.writeRowID()) {
			effCols++;
		}
		Cell[][] cells = new Cell[effRow][effCols];

		// the index of the row in the XLsheet
		int rowIdx = 0;
		// the index of the cell in the XLsheet
		short colIdx = 0;

		// write column names
		if (mSettings.writeColHeader()) {

			// Create a new row in the sheet
			Row hdrRow = sheet.createRow(rowIdx++);

			if (mSettings.writeRowID()) {
				hdrRow.createCell(colIdx++).setCellValue("row ID");
				cells[rowIdx - 1][colIdx - 1] = hdrRow.getCell(colIdx - 1);
			}
			for (int c = 0; c < numOfCols; c++) {
				String cName = inSpec.getColumnSpec(c).getName();

				hdrRow.createCell(colIdx++).setCellValue(cName);
				cells[rowIdx - 1][colIdx - 1] = hdrRow.getCell(colIdx - 1);
			}

		} // end of if write column names

		// Guess 80% of the job is generating the sheet, 20% is writing it out
		ExecutionMonitor e = exec.createSubProgress(0.8);

		// write each row of the data
		int rowCnt = 0;
		for (DataRow tableRow : table) {

			colIdx = 0;

			// create a new sheet if the old one is full
			if (rowIdx >= MAX_NUM_OF_ROWS) {
				sheetIdx++;
				sheet = wb.createSheet(sheetName + "(" + sheetIdx + ")");
				rowIdx = 0;
				LOGGER.info("Creating additional sheet to store entire table."
						+ "Additional sheet name: " + sheetName + "("
						+ sheetIdx + ")");
			}

			// set the progress
			String rowID = tableRow.getKey().getString();
			String msg;
			if (numOfRows <= 0) {
				msg = "Writing row " + (rowCnt + 1) + " (\"" + rowID + "\")";
			} else {
				msg = "Writing row " + (rowCnt + 1) + " (\"" + rowID
						+ "\") of " + numOfRows;
				e.setProgress(rowCnt / (double) numOfRows, msg);
			}
			// Check if execution was cancelled !
			exec.checkCanceled();

			// Create a new row in the sheet
			Row sheetRow = sheet.createRow(rowIdx++);

			// add the row id
			if (mSettings.writeRowID()) {
				sheetRow.createCell(colIdx++).setCellValue(rowID);
				cells[rowIdx - 1][colIdx - 1] = sheetRow.getCell(colIdx - 1);
			}
			// now add all data cells
			for (int c = 0; c < numOfCols; c++) {

				DataCell colValue = tableRow.getCell(c);

				if (colValue.isMissing()) {
					String miss = mSettings.getMissingPattern();
					if (miss != null) {
						sheetRow.createCell(colIdx).setCellValue(miss);

					}
				} else {
					Cell sheetCell = sheetRow.createCell(colIdx);

					if (colValue.getType().isCompatible(DoubleValue.class)) {
						double val = ((DoubleValue) colValue).getDoubleValue();
						sheetCell.setCellValue(val);
						if (h.containsKey(inSpec.getColumnSpec(c).getName())
								&& h.get(inSpec.getColumnSpec(c).getName())
										.isActive()) {
							short col = mapColor(
									h.get(inSpec.getColumnSpec(c).getName()),
									val);
							if (col == HSSFColor.HSSFColorPredefined.RED.getIndex()) {
								sheetCell.setCellStyle(red);
							}
							if (col == HSSFColor.HSSFColorPredefined.YELLOW.getIndex()) {
								sheetCell.setCellStyle(yellow);
							}
							if (col == HSSFColor.HSSFColorPredefined.GREEN.getIndex()) {
								sheetCell.setCellStyle(green);
							}

						}
					} else if (colValue.getType().isCompatible(
							StringValue.class)) {
						String val = ((StringValue) colValue).getStringValue();
						sheetCell.setCellValue(val);
						if (s.containsKey(inSpec.getColumnSpec(c).getName())
								&& s.get(inSpec.getColumnSpec(c).getName())
										.isActive()) {
							short col = mapColor(
									s.get(inSpec.getColumnSpec(c).getName()),
									val);
							if (col == HSSFColor.HSSFColorPredefined.RED.getIndex()) {
								sheetCell.setCellStyle(red);
							}
							if (col == HSSFColor.HSSFColorPredefined.YELLOW.getIndex()) {
								sheetCell.setCellStyle(yellow);
							}
							if (col == HSSFColor.HSSFColorPredefined.GREEN.getIndex()) {
								sheetCell.setCellStyle(green);
							}
							if (col == HSSFColor.HSSFColorPredefined.BLUE.getIndex()) {
								sheetCell.setCellStyle(blue);
							}
							if (col == HSSFColor.HSSFColorPredefined.WHITE.getIndex()) {
								sheetCell.setCellStyle(white);
							}

						}

					} else {
						String val = colValue.toString();
						sheetCell.setCellValue(val);
					}
				}
				cells[rowIdx - 1][colIdx] = sheetRow.getCell(colIdx);
				colIdx++;
			}

			rowCnt++;
		} // end of for all rows in table

		wb.removeSheetAt(sheetIdx);
		sheet = wb.createSheet(sheetName);
		for (int c = 0; c < effCols; c++) {
			Row sheetRow = sheet.createRow(c);
			for (int r = 0; r < effRow; r++) {
				Cell sheetCell = sheetRow.createCell(r);
				sheetCell.setCellStyle(cells[r][c].getCellStyle());
				CellType t = cells[r][c].getCellType();
				sheetCell.setCellType(t);
				switch (t) {
				case STRING:
					sheetCell.setCellValue(cells[r][c].getStringCellValue());
					break;
				case NUMERIC:
					sheetCell.setCellValue(cells[r][c].getNumericCellValue());
					break;
				default:
					sheetCell.setCellValue(mSettings.getMissingPattern());
					break;
				}
			}
		}

		// Write the output to a file
		OutputStream mOutStream = new FileOutputStream(mSettings.getFilename());
		wb.write(mOutStream);
		mOutStream.close();
	
		if (mSettings.getAutostart()) {

			String fi = (new File(mSettings.getFilename())).getAbsolutePath();
			fi = fi.replaceAll("\\s", "%20");
			String[] commands = { "cmd", "/C", "start", "file:" + fi };
			Runtime.getRuntime().exec(commands);
		}
	}

	private short mapColor(DoubleColumnSettings c, double d) {
		if (c.isInverted()) {
			if (d <= c.getLowerBound()) {
				return HSSFColor.HSSFColorPredefined.RED.getIndex();
			}
			if (d <= c.getUpperBound()) {
				return HSSFColor.HSSFColorPredefined.YELLOW.getIndex();
			}
			return HSSFColor.HSSFColorPredefined.GREEN.getIndex();
		} else {
			if (d <= c.getLowerBound()) {
				return HSSFColor.HSSFColorPredefined.GREEN.getIndex();
			}
			if (d <= c.getUpperBound()) {
				return HSSFColor.HSSFColorPredefined.YELLOW.getIndex();
			}
			return HSSFColor.HSSFColorPredefined.RED.getIndex();
		}

	}

	private short mapColor(StringColumnSettings c, String s) {

		if (s.equalsIgnoreCase(c.getQuery())) {
			return (short) c.getColorX(1);
		}
		if (s.equalsIgnoreCase(c.getQuery2())) {
			return (short) c.getColorX(2);
		}
		if (s.equalsIgnoreCase(c.getQuery3())) {
			return (short) c.getColorX(3);
		}

		return (short) c.getColorX(4);
	}

	/**
	 * Replaces characters that are illegal in sheet names. These are
	 * \/:*?"<>|[].
	 * 
	 * @param name
	 *            the name to clean
	 * @return returns the name with all of the above characters replaced by an
	 *         underscore.
	 */
	private String replaceInvalidChars(final String name) {
		StringBuilder result = new StringBuilder();
		int l = name.length();
		for (int i = 0; i < l; i++) {
			char c = name.charAt(i);
			if ((c == '\\') || (c == '/') || (c == ':') || (c == '*')
					|| (c == '?') || (c == '"') || (c == '<') || (c == '>')
					|| (c == '|') || (c == '[') || (c == ']')) {
				result.append('_');
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

}
