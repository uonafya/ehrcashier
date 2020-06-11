/**
 *  Copyright 2020  HealthIT
 *
 *  This file is part of Billing module.
 *
 *  Cashier module is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 1 of the License, or
 *  (at your option) any later version.


 *
 **/

package org.openmrs.module.ehrcashier.billcalculator.common;

import org.openmrs.module.billingui.includable.billcalculator.BillCalculator;

import java.math.BigDecimal;
import java.util.Map;

public class BillCalculatorImpl implements BillCalculator {

	/**
	 * Return 100%
	 */
	public BigDecimal getRate(Map<String, Object> parameters) {
		return new BigDecimal(1);
	}

	public boolean isFreeBill(Map<String, Object> parameters) {

		return false;
	}
}
