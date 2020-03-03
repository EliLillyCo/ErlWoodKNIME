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
package org.erlwood.knime.nodes.vsmetrics;

import java.util.List;
import java.util.Map;

public final class VSMetrics {
    private VSMetrics() {

    }

    public static double computeawAUC(List<Integer> ranks, List<Integer> clusters, double n, double n2, Map<Integer, Integer> h) {
        double ranksum = 0.0;
        int i = 0;
        for (int ri : ranks) {
            double t = (double) ri;
            double decSeen = t - (double) i - 1.0;
            ranksum = ranksum + (decSeen / (double) h.get(clusters.get(i)));
            i++;
        }
        // inactives are regarded as "cluster" --> subtract one from table size
        return 1.0 - (ranksum / ((double) (h.size() - 1.0) * (n2 - n)));
    }

    public static double computeawROCE(List<Integer> ranks,
                                       List<Integer> clusters,
                                       double percent,
                                       double n,
                                       double n2,
                                       Map<Integer, Integer> h) {

        double sum = 0.0;
        double headSize = (n2 - n) * percent;
        int i = 0;
        for (int ri : ranks) {
            double t = (double) ri;
            double decSeen = t - (double) i - 1.0;
            double fpRate = decSeen / (n2 - n);
            if (fpRate > percent) {
                decSeen = headSize;
                break;
            }
            sum = sum + (1.0 / (double) h.get(clusters.get(i)));
            i++;
        }

        return (sum / (double) (h.size() - 1.0)) / ((headSize) / (n2 - n));
    }

    public static double computeROCE(List<Integer> ranks, double percent, double n, double n2) {

        double sum = 0.0;
        double headSize = (n2 - n) * percent;
        int i = 0;
        for (int ri : ranks) {
            double t = (double) ri;
            double decSeen = t - (double) i - 1.0;
            double fpRate = decSeen / (n2 - n);
            if (fpRate > percent) {
                decSeen = headSize;
                break;
            }
            sum++;
            i++;
        }

        return (sum / n) / ((headSize) / (n2 - n));
    }

    public static double computeEF(List<Integer> ranks, double percent, double n, double n2) {
        double expAct = n * percent;
        double sum = 0.0;
        double headSize = n2 * percent;
        for (int ri : ranks) {
            double t = (double) ri;
            if (t <= headSize) {
                sum++;
            } else {
                break;
            }
        }
        return sum / expAct;
    }

    public static double computeAUROC(List<Integer> ranks, double n, double n2) {
        double ranksum = 0.0;
        for (int ri : ranks) {
            double t = (double) ri;
            ranksum += t;
        }

        return 1.0 - ranksum / (n * (n2 - n)) + (n + 1) / (2.0 * (n2 - n));
    }

    public static double computeRIE(List<Integer> ranks, double alpha, double n, double n2) {
        double ranksum = 0.0;
        for (int ri : ranks) {
            double t = Math.exp(-alpha * ((double) ri) / n2);
            ranksum += t;
        }
        ranksum /= n;
        ranksum *= n2;
        double denominator = (1.0 - Math.exp(-alpha)) / (Math.exp(alpha / n2) - 1.0);
        return ranksum / denominator;
    }

    public static double computeBEDROC(double rie, double alpha, double n, double n2) {
        double nom = rie / n2 * Math.sinh(0.5 * alpha);
        double den = Math.cosh(0.5 * alpha) - Math.cosh(0.5 * alpha - alpha * n / n2);
        double t = 1.0 / (1.0 - Math.exp(alpha * (n2 - n) / n2));
        return nom / den - t;
    }
}
