/**
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

package org.openmrs.module.ehrcashier.fragment.controller;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.BillingService;
import org.openmrs.module.hospitalcore.HospitalCoreService;
import org.openmrs.module.hospitalcore.model.PatientServiceBill;
import org.openmrs.module.hospitalcore.util.HospitalCoreConstants;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.module.ehrcashier.PatientWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchPatientFragmentController {
	
	/**
	 * The controller method handles both the GET and POST requests if none is explicitly defined
	 */
	public void controller() {
	}
	
	/**
	 * Searches for and returns a list of patients given the Bill Id, Patient Identifier or Patient
	 * details(firstname,lastname.gender...e.t.c)
	 * 
	 * @param phrase
	 * @param currentPage
	 * @param pageSize
	 * @param uiUtils
	 * @param request
	 * @return
	 */
	public List<SimpleObject> searchSystemPatient(@RequestParam(value = "phrase", required = false) String phrase,
	        @RequestParam(value = "currentPage", required = false) Integer currentPage,
	        @RequestParam(value = "pageSize", required = false) Integer pageSize, UiUtils uiUtils, HttpServletRequest request) {
		String prefix = Context.getAdministrationService().getGlobalProperty(
		    HospitalCoreConstants.PROPERTY_IDENTIFIER_PREFIX);
		
		String gender = request.getParameter("gender");
		if (gender.equalsIgnoreCase("any")) {
			gender = null;
		}
		Integer age = getInt(request.getParameter("age"));
		Integer ageRange = getInt(request.getParameter("ageRange"));
		String relativeName = request.getParameter("relativeName");
		String lastDayOfVisit = request.getParameter("lastDayOfVisit");
		Integer lastVisitRange = getInt(request.getParameter("lastVisit"));
		String maritalStatus = request.getParameter("patientMaritalStatus");
		String phoneNumber = request.getParameter("phoneNumber");
		String nationalId = request.getParameter("nationalId");
		String fileNumber = request.getParameter("fileNumber");
		HospitalCoreService hcs = (HospitalCoreService) Context.getService(HospitalCoreService.class);
		BillingService billingService = (BillingService) Context.getService(BillingService.class);
		List<Patient> patients = new ArrayList<Patient>();
		Set<Integer> patientIds = new HashSet<Integer>();
		List<Patient> filteredPatients = new ArrayList<Patient>();
		
		int billId;
		
		try {
			billId = Integer.parseInt(phrase);
			PatientServiceBill patientServiceBill = billingService.getPatientServiceBillById(billId);
			Patient patient1 = patientServiceBill.getPatient();
			patients.add(patient1);
		}
		catch (NumberFormatException e) {
			patients = hcs.searchPatient(phrase, gender, age, ageRange, lastDayOfVisit, lastVisitRange, relativeName,
			    maritalStatus, phoneNumber, nationalId, fileNumber);
			e.printStackTrace();
		}
		
		if (!patients.isEmpty()) {
			for (Patient patient : patients) {
				patientIds.add(patient.getPatientId());
			}
		}
		if (!patientIds.isEmpty()) {
			for (Integer patientId : patientIds) {
				filteredPatients.add(Context.getPatientService().getPatient(patientId));
			}
		}
		
		List<PatientWrapper> wrapperList = patientsWithLastVisit(filteredPatients);
		
		return SimpleObject.fromCollection(wrapperList, uiUtils, "patientId", "wrapperIdentifier", "names", "age", "gender",
		    "formartedVisitDate");
	}
	
	/**
	 * Converts a String representation of a number to its interger equivalent, otherwise returns 0
	 * 
	 * @param value - the String to parse
	 * @return the integer equivalent of the string, otherwise returns a 0
	 */
	private Integer getInt(String value) {
		try {
			Integer number = Integer.parseInt(value);
			return number;
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	private List<PatientWrapper> patientsWithLastVisit(List<Patient> patients) {
		HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
		List<PatientWrapper> wrappers = new ArrayList<PatientWrapper>();
		for (Patient patient : patients) {
			wrappers.add(new PatientWrapper(patient, hcs.getLastVisitTime(patient)));
		}
		return wrappers;
	}
	
}
