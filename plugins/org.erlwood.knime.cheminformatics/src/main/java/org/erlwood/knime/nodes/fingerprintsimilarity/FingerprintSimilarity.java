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
package org.erlwood.knime.nodes.fingerprintsimilarity;

import org.knime.core.data.vector.bitvector.BitVectorValue;

public final class FingerprintSimilarity {
	private FingerprintSimilarity() {
	}

	public static double tanimoto(BitVectorValue a, BitVectorValue b) {
		double intersection = 0.0;
		double as = 0.0;
		double bs = 0.0;
		for (int i = 0; i < a.length(); i++) {
			if (a.get(i) && b.get(i)) {
				intersection++;
			}
			if (a.get(i)) {
				as++;
			}
			if (b.get(i)) {
				bs++;
			}
		}
		return intersection / (double) (as + bs - intersection);
	}

	public static double intersection(BitVectorValue a, BitVectorValue b) {
		double intersection = 0.0;
		double as = 0.0;

		for (int i = 0; i < a.length(); i++) {
			if (a.get(i) && b.get(i)) {
				intersection++;
			}
			if (a.get(i)) {
				as++;
			}
		}
		return intersection / (double) (as);
	}

	public static double getSimilarity(BitVectorValue a, BitVectorValue b,
			String measure) {
		if (measure.equalsIgnoreCase("Tanimoto")) {
			return tanimoto(a, b);
		}
		if (measure.equalsIgnoreCase("Intersection")) {
			return intersection(a, b);
		}

		return Double.NaN;
	}
}
