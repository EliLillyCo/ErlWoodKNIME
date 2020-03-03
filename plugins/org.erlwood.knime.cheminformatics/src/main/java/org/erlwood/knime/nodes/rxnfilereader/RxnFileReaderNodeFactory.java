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
package org.erlwood.knime.nodes.rxnfilereader;

import org.erlwood.knime.nodes.molfilereader.MolFileReaderNodeDialog;
import org.erlwood.knime.utils.node.AbstractErlWoodNodeFactory;
import org.erlwood.knime.utils.nodes.ChemContentsNodeModel;
import org.erlwood.knime.utils.nodes.ChemContentsNodeView;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;


/**
 * <code>NodeFactory</code> for the "RxnFileReader" Node.
 * 
 *
 * @author Dimitar Hristozov
 */
public class RxnFileReaderNodeFactory extends AbstractErlWoodNodeFactory<ChemContentsNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ChemContentsNodeModel createNodeModel() {
        return new RxnFileReaderNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<ChemContentsNodeModel> createNodeView(final int viewIndex,
            final ChemContentsNodeModel nodeModel) {
    	return new ChemContentsNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane()
    {
    	return new MolFileReaderNodeDialog(
        	RxnFileReaderNodeModel.CFG_FILENAME,
        	RxnFileReaderNodeModel.CFG_FILEPROPS, 
        	RxnFileReaderNodeModel.CFG_OUT_OPTIONS,
        	RxnFileReaderNodeModel.CFG_OUT_COLS,
        	RxnFileReaderNodeModel.CFG_IS_SHALLOW_SCAN,
        	RxnFileReaderNodeModel.CFG_LAST_DIRECTORY,
        	RxnFileReaderNodeModel.CFG_FOUND_PROPS,
        	RxnFileReaderNodeModel.CFG_REQUIRES_SCAN,
        	true,
        	RxnFileReaderNodeModel.ARR_DEFAULT_OPTIONS,
        	RxnFileReaderNodeModel.ARR_DEFAULT_COLS
    	);
    }

}

