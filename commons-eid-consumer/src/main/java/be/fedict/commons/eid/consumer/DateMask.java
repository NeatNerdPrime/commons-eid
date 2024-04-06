/*
 * Commons eID Project.
 * Copyright (C) 2008-2017 FedICT.
 * Copyright (C) 2017 Peter Mylemans.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see
 * http://www.gnu.org/licenses/.
 */

package be.fedict.commons.eid.consumer;

/**
 * DateMask is an enum to support partial date information.
 *
 * @author Peter Mylemans
 */
public enum DateMask {

	/**
	 * Mask that signifies only the year is set.
	 */
	YYYY,

	/**
	 * Mask that signifies a year, month and day of month is set.
	 */
	YYYY_MM_DD
}
