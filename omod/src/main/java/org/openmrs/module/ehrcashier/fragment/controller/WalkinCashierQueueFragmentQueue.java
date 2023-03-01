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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openmrs.module.ehrcashier.PatientWrapper;

public class WalkinCashierQueueFragmentQueue {
	
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
		Set<Patient> filteredPatients = new HashSet<Patient>();
		
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
		List<PatientWrapper> wrapperList = patientsWithLastVisit((List<Patient>) filteredPatients);
		
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
