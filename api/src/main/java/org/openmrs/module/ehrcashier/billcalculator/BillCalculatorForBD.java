/**
 *
 *  This file is part of ehrcashier module.
 *
 *  ehrcashier module is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  ehrcashier module is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ehrcashier module.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/

package org.openmrs.module.ehrcashier.billcalculator;

import java.math.BigDecimal;
import java.util.Map;

public interface BillCalculatorForBD {
	
	/**
	 * Return the rate to calculate for a particular bill item
	 * 
	 * @param parameters TODO
	 * @return
	 */
	public BigDecimal getRate(Map<String, Object> parameters, String billType);
	
	/**
	 * Determine whether a bill should be free or not
	 * 
	 * @param parameters TODO
	 * @return
	 */
	//ghanshyam 3-june-2013 New Requirement #1632 Orders from dashboard must be appear in billing queue.User must be able to generate bills from this queue
	public int isFreeBill(String billType);
}
