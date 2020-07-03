/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.ehrcashier.billcalculator.kenya_hospital;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.ehrcashier.billcalculator.BillCalculatorForBD;
import org.openmrs.module.hospitalcore.concept.TestTree;
import org.openmrs.module.hospitalcore.model.PatientServiceBillItem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BillCalculatorImpl implements BillCalculatorForBD {
	
	private static Map<String, Set<Concept>> testTreeMap;
	
	/**
	 * Get the percentage of price to pay If patient category is RSBY or BPL, the bill should be
	 * 100% free
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public BigDecimal getRate(Map<String, Object> parameters, String billType) {
		BigDecimal rate = new BigDecimal(0);
		PatientServiceBillItem item = (PatientServiceBillItem) parameters.get("billItem");
		
		if (billType.equals("paid")) {
			rate = new BigDecimal(1);
		}
		
		return rate;
	}
	
	/**
	 * Build test tree map for senior citizen billing
	 */
	private static void buildTestTreeMap() {
		testTreeMap = new HashMap<String, Set<Concept>>();
		
		// General lab
		buildTestTree("GENERAL LABORATORY");
		buildTestTree("RADIOLOGY");
		buildTestTree("ULTRASOUND");
		buildTestTree("CARDIOLOGY");
	}
	
	/**
	 * Build test tree for a specific tests
	 * 
	 * @param conceptName
	 */
	private static void buildTestTree(String conceptName) {
		Concept generalLab = Context.getConceptService().getConcept(conceptName);
		TestTree tree = new TestTree(generalLab);
		if (tree.getRootNode() != null) {
			testTreeMap.put(conceptName, tree.getConceptSet());
		}
	}
	
	/**
	 * Determine whether a bill should be free or not. By default, all bills are not free
	 * 
	 * @return
	 */
	//#1632 Orders from dashboard must be appear in billing queue.User must be able to generate bills from this queue
	@SuppressWarnings("unchecked")
	public int isFreeBill(String billType) {
		
		if (billType.equals("paid")) {
			return 0;
		}
		
		return 1;
	}
}
