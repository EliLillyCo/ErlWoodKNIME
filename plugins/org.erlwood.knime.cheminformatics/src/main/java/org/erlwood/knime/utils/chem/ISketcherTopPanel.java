package org.erlwood.knime.utils.chem;

import java.awt.Component;

/**
 * Interface for supplying a top panel for the Sketcher.
 * @author Luke Bullard
 *
 */
public interface ISketcherTopPanel {

	/** 
	 * @return The Component to place at the top of the Sketcher panel or
	 * null if it is not available for some reason.
	 */
	Component getComponent();

	/**
	 * Sets the Callback object to use for this sketcher top panel.
	 * @param cb The callback object
	 */
	void setCallback(ISketcherTopPanelCallback cb);

}
