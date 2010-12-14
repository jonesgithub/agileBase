/*
 *  Copyright 2010 GT webMarque Ltd
 * 
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.manageData.fields;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.grlea.log.SimpleLogger;

import com.gtwm.pb.model.interfaces.fields.DecimalValue;
import com.gtwm.pb.model.manageData.TableData;

public class DecimalValueDefn implements DecimalValue {

	private DecimalValueDefn() {
		this.decimalValue = null;
	}

	public DecimalValueDefn(Double decimalValue) {
		this.decimalValue = decimalValue;
	}

	public double getValueFloat() {
		if (this.decimalValue == null) {
			return 0;
		} else {
			return this.decimalValue;
		}
	}

	public String toString() {
		if (this.decimalValue == null) {
			return "";
		} else {
			logger.debug("Formatted version of " + decimalValue + " is " + formatter.format(decimalValue));
			return formatter.format(decimalValue);
		}
	}

	public boolean isNull() {
		return (this.decimalValue == null);
	}

	private static final NumberFormat formatter = new DecimalFormat("#0.####################");

	private static final SimpleLogger logger = new SimpleLogger(DecimalValueDefn.class);

	private final Double decimalValue;

}
