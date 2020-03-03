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
package org.erlwood.knime.utils;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

/** Class used to update the node execution progress bar and react to 
 * cancellation events during a file transfer.
 * @author Tom Wilkin */
public class FileTransferUpdateThread extends Thread {
	
	/** The ExecutionContext for the node execution. */
	private ExecutionContext context;
	
	/** The percentage of the progress for each file. */
	private double fraction;
	
	/** The current row that is being processed during the execution. */
	private int currentRow;

	/** The total amount of the current file/directory that has been copied. */
	private long totalCopied;

	/**
	 * The total overall size of the current file/directory that is to be
	 * copied.
	 */
	private long totalSize;
	
	/** Whether the file size update thread is running. */
	private boolean isRunning;
	
	public FileTransferUpdateThread(final ExecutionContext exec, 
			final double fraction)
	{
		context = exec;
		this.fraction = fraction;
		isRunning = false;
		currentRow = -1;
		totalCopied = 0;
		totalSize = 0;
	}
	
	/** Update the thread progress when the next file transfer has started.
	 * @param size The size of this file. */
	public void newFile(final long size) { 
		totalSize = size;
		totalCopied = 0;
	}
	
	/** Update the thread now that some data has been transferred.
	 * @param bytes The number of bytes that have been transferred for the
	 * current file. */
	public void increment(final int bytes) { totalCopied += bytes; }
	
	/** Update the thread now the next row in the input data has been started.*/
	public void nextRow( ) { currentRow++; }
	
	/** Stop the thread executing as an exception/cancellation has occurred in
	 * the KNIME execution. */
	public void cancel( ) { isRunning = false; }
	
	@Override
	public void run( ) {
		double progress = 0;
		isRunning = true;

		while (progress < 1 && isRunning) {
			try {
				context.checkCanceled();
			} catch(CanceledExecutionException e) {
				// stop this thread as the user cancelled.
				break;
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// safe to ignore as this is just sleeping
			}

			// calculate progress
			if(totalSize > 0) {
				progress = currentRow
						* fraction
						+ (fraction * ((double) totalCopied / (double) totalSize));
				context.setProgress(progress);
			}
		}
		isRunning = false;
	}

};
