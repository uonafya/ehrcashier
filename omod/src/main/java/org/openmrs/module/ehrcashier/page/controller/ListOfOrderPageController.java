package org.openmrs.module.ehrcashier.page.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ehrcashier.EhrCashierConstants;
import org.openmrs.module.hospitalcore.BillingService;
import org.openmrs.module.hospitalcore.HospitalCoreService;
import org.openmrs.module.hospitalcore.model.OpdTestOrder;
import org.openmrs.module.hospitalcore.model.PatientSearch;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@AppPage(EhrCashierConstants.APP_EHRCASHIER)
public class ListOfOrderPageController {
	
	public String get(PageModel model, UiSessionContext sessionContext, PageRequest pageRequest, UiUtils ui,
	        @RequestParam("patientId") Integer patientId, @RequestParam(value = "date", required = false) String dateStr) {
		
		BillingService billingService = Context.getService(BillingService.class);
		PatientService patientService = Context.getPatientService();
		Patient patient = patientService.getPatient(patientId);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		List<OpdTestOrder> listOfOrders = billingService.listOfOrder(patientId, date);
		// Add Patient Details on the page where Order ID is clicked
		HospitalCoreService hospitalCoreService = Context.getService(HospitalCoreService.class);
		PatientSearch patientSearch = hospitalCoreService.getPatientByPatientId(patientId);
		
		model.addAttribute("age", patient.getAge());
		
		if (patient.getGender().equals("M")) {
			model.addAttribute("gender", "Male");
		}
		if (patient.getGender().equals("F")) {
			model.addAttribute("gender", "Female");
		}
		model.addAttribute(
		    "category",
		    patient.getAttribute(Context.getPersonService().getPersonAttributeTypeByUuid(
		        "09cd268a-f0f5-11ea-99a8-b3467ddbf779")));
		model.addAttribute("previousVisit", hospitalCoreService.getLastVisitTime(patient));
		
		if (patient.getAttribute(Context.getPersonService().getPersonAttributeTypeByUuid(
		    "09cd268a-f0f5-11ea-99a8-b3467ddbf779")) == null) {
			model.addAttribute("fileNumber", "");
		} else if (StringUtils.isNotBlank(patient.getAttribute(
		    Context.getPersonService().getPersonAttributeTypeByUuid("09cd268a-f0f5-11ea-99a8-b3467ddbf779")).getValue())) {
			model.addAttribute(
			    "fileNumber",
			    "(File: "
			            + patient.getAttribute(Context.getPersonService().getPersonAttributeTypeByUuid(
			                "09cd268a-f0f5-11ea-99a8-b3467ddbf779")) + ")");
		} else {
			model.addAttribute("fileNumber", "");
		}
		/*
		if(patient.getAttribute(14).getValue() == "Waiver"){
			model.addAttribute("exemption", patient.getAttribute(32));
		}
		else if(patient.getAttribute(14).getValue()!="General" && patient.getAttribute(14).getValue()!="Waiver"){
			model.addAttribute("exemption", patient.getAttribute(36));
		}
		else {
			model.addAttribute("exemption", " ");
		}
		*/
		
		model.addAttribute("patientSearch", patientSearch);
		model.addAttribute("listOfOrders", listOfOrders);
		model.addAttribute("patientId", patientId);
		model.addAttribute("date", dateStr);
		return null;
	}
}
