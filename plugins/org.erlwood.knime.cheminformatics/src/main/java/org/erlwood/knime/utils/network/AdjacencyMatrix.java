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
package org.erlwood.knime.utils.network;

import java.util.Arrays;
import java.util.Iterator;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.distmatrix.type.DistanceVectorDataValue;

@SuppressWarnings("serial")
public class AdjacencyMatrix extends DataCell implements
		DistanceVectorDataValue, CollectionDataValue {
	public static final DataType TYPE = DataType.getType(AdjacencyMatrix.class,
			DoubleCell.TYPE);
	private final int id;
	private final double[] edges;
	private final boolean symmetric;

	private final double maxdist = Double.POSITIVE_INFINITY;

	public AdjacencyMatrix(int id, boolean directed, final double[] distances) {
		this.id = id;
		this.edges = distances;
		this.symmetric = !directed;
		for (int i = 0; i < distances.length; i++) {
			if (distances[i] > maxdist) {
				distances[i] = maxdist;
			}
		}
	}

	// CollectionDataValue
	@Override
	public DataType getElementType() {
		return DoubleCell.TYPE;
	}

	@Override
	public int size() {
		return edges.length;
	}

	@Override
	public boolean containsBlobWrapperCells() {
		return false;
	}

	@Override
	public Iterator<DataCell> iterator() {
		return new Iterator<DataCell>() {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < edges.length;
			}

			@Override
			public DataCell next() {
				index++;
				if (Double.isNaN(edges[index - 1])) {
					return DataType.getMissingCell();
				}
				return new DoubleCell(edges[index - 1]);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("No remove allowed");
			}

		};
	}

	// DataCell methods
	@Override
	public String toString() {
		return Arrays.toString(edges);
	}

	@Override
	protected boolean equalsDataCell(DataCell dc) {
		if (dc instanceof AdjacencyMatrix) {
			AdjacencyMatrix newName = (AdjacencyMatrix) dc;
			return (this.id == newName.id && Arrays.equals(this.edges,
					newName.edges));

		}
		return false;
	}

	@Override
	public int hashCode() {
		return edges.hashCode();
	}

	// DistanceVectorValue methods

	@Override
	public int getIdentifier() {
		return id;
	}

	@Override
	public double getDistance(DistanceVectorDataValue other) {
		if (!(other instanceof AdjacencyMatrix)) {
			return Double.POSITIVE_INFINITY;
		}
		if (getIdentifier() == other.getIdentifier()) {
			return 0.0;
		}
		if (other.getIdentifier() >= edges.length || other.getIdentifier() < 0) {
			if (symmetric && getIdentifier() >= 0.0
					&& getIdentifier() < ((AdjacencyMatrix) other).edges.length) {
				return other.getDistance(this);
			}
			return maxdist;
		}
		return edges[other.getIdentifier()];
	}

	@Override
	public boolean isSymmetric() {
		return symmetric;
	}

	@Override
	public int getSignature() {
		return +1;
	}

	/**
	 * Gets the length of the edges.
	 * 
	 * @return The edge length.
	 */
	public int getEdgeLength() {
		return edges.length;
	}

	/**
	 * Gets the edge value given an index.
	 * 
	 * @param index
	 *            The index of the edge to get
	 * @return The edge value.
	 */
	public double getEdge(int index) {
		return edges[index];
	}
}
