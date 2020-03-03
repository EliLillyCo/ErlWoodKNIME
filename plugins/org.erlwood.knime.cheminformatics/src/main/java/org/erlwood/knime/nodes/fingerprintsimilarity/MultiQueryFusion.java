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

import org.knime.core.node.NodeLogger;

public final class MultiQueryFusion {
	private static final NodeLogger LOG = NodeLogger.getLogger(MultiQueryFusion.class);
	
	private MultiQueryFusion() {
	}
	public static double maxsim(double[] sims) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < sims.length; i++) {
			if (sims[i] > max) {
				max = sims[i];
			}
		}
		return max;
	}

	public static double avg(double[] sims) {
		double max = 0.0;
		for (int i = 0; i < sims.length; i++) {
			max += sims[i];
		}
		return max / (double) sims.length;
	}

	public static double getFusedSimilarity(double[] sims, String method) {
		if (method.equalsIgnoreCase("MAXSIM")) {
			return maxsim(sims);
		}
		if (method.equalsIgnoreCase("AVERAGE")) {
			return avg(sims);
		}
		LOG.debug("Method " + method + " not defined!");
		return Double.NaN;
	}
}
