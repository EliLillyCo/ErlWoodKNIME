/*
 * Copyright (c) 2000, 2002, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
*/
package org.erlwood.knime.utils.gui.layout;

/**
 * Constants used by TableLayout.
 * 
 * @author Daniel E. Barbalace
 * 
 * Code retrieved from http://www.oracle.com/technetwork/java/tablelayout-141489.html
 */

public interface TableLayoutConstants {

	/** Indicates that the component is left justified in its cell */
	public static final int LEFT = 0;

	/** Indicates that the component is top justified in its cell */
	public static final int TOP = 0;

	/** Indicates that the component is centred in its cell */
	public static final int CENTER = 1;

	/** Indicates that the component is full justified in its cell */
	public static final int FULL = 2;

	/** Indicates that the component is bottom justified in its cell */
	public static final int BOTTOM = 3;

	/** Indicates that the component is right justified in its cell */
	public static final int RIGHT = 3;

	/** Indicates that the row/column should fill the available space */
	public static final double FILL = -1.0;

	/**
	 * Indicates that the row/column should be allocated just enough space to
	 * accommodate the preferred size of all components contained completely
	 * within this row/column.
	 */
	public static final double PREFERRED = -2.0;

	/**
	 * Indicates that the row/column should be allocated just enough space to
	 * accommodate the minimum size of all components contained completely within
	 * this row/column.
	 */
	public static final double MINIMUM = -3.0;

	/** Minimum value for an alignment */
	public static final int MIN_ALIGN = 0;

	/** Maximum value for an alignment */
	public static final int MAX_ALIGN = 3;

}
