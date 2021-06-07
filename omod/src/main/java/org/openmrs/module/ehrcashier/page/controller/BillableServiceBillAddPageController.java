package org.openmrs.module.ehrcashier.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ehrcashier.EhrCashierConstants;
import org.openmrs.module.ehrcashier.billcalculator.BillCalculatorForBDService;
import org.openmrs.module.hospitalcore.BillingService;
import org.openmrs.module.hospitalcore.HospitalCoreService;
import org.openmrs.module.hospitalcore.IpdService;
import org.openmrs.module.hospitalcore.model.*;
import org.openmrs.module.hospitalcore.util.HospitalCoreUtils;
import org.openmrs.module.hospitalcore.util.Money;
import org.openmrs.module.hospitalcore.util.PatientUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AppPage(EhrCashierConstants.APP_EHRCASHIER)
public class BillableServiceBillAddPageController {
	
	private Log logger = LogFactory.getLog(getClass());
	
	public String get(PageModel pageModel, UiSessionContext sessionContext, PageRequest pageRequest, UiUtils ui,
	        @RequestParam("patientId") Integer patientId, @RequestParam(value = "comment", required = false) String comment,
	        @RequestParam(value = "billType", required = false) String billType,
	        @RequestParam(value = "encounterId", required = false) Integer encounterId,
	        @RequestParam(value = "typeOfPatient", required = false) String typeOfPatient,
	        @RequestParam(value = "lastBillId", required = false) String lastBillId) {
		
		Patient patient = Context.getPatientService().getPatient(patientId);
		Map<String, String> attributes = PatientUtils.getAttributes(patient);
		BillingService billingService = Context.getService(BillingService.class);
		HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
		
		List<BillableService> services = billingService.getAllServices();
		pageModel.addAttribute("services", services);
		Map<Integer, BillableService> mapServices = new HashMap<Integer, BillableService>();
		for (BillableService ser : services) {
			mapServices.put(ser.getConceptId(), ser);
		}
		Integer conceptId = Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
		    "billing.rootServiceConceptId"));
		Concept concept = Context.getConceptService().getConcept(conceptId);
		pageModel.addAttribute("tabs", billingService.traversTab(concept, mapServices, 1));
		
		pageModel.addAttribute("patientId", patientId);
		pageModel.addAttribute("patient", patient);
		pageModel.addAttribute("attributes", attributes);
		if (patient.getGender().equals("M")) {
			pageModel.addAttribute("gender", "Male");
		}
		if (patient.getGender().equals("F")) {
			pageModel.addAttribute("gender", "Female");
		}
		pageModel.addAttribute("age", patient.getAge());
		pageModel.addAttribute("currentDate", new Date());
		pageModel.addAttribute("category", patient.getAttribute(14));
		pageModel.addAttribute("lastBillId", lastBillId);
		pageModel.addAttribute("previousVisit", hcs.getLastVisitTime(patient));
		
		if (patient.getAttribute(Context.getPersonService().getPersonAttributeTypeByUuid(
		    "09cd268a-f0f5-11ea-99a8-b3467ddbf779")) == null) {
			pageModel.addAttribute("fileNumber", "");
		} else if (StringUtils.isNotBlank(patient.getAttribute(
		    Context.getPersonService().getPersonAttributeTypeByUuid("09cd268a-f0f5-11ea-99a8-b3467ddbf779")).getValue())) {
			pageModel.addAttribute(
			    "fileNumber",
			    "(File: "
			            + patient.getAttribute(Context.getPersonService().getPersonAttributeTypeByUuid(
			                "09cd268a-f0f5-11ea-99a8-b3467ddbf779")) + ")");
		} else {
			pageModel.addAttribute("fileNumber", "");
		}
		return null;
	}
	
	public String post(HttpServletRequest request, PageModel model, Object command, BindingResult bindingResult,
	        @RequestParam("patientId") Integer patientId,
	        @RequestParam(value = "paymentMode", required = false) String paymentMode,
	        @RequestParam(value = "billType", required = false) String billType, UiUtils uiUtils,
	        @RequestParam(value = "encounterId", required = false) Integer encounterId) {
		String bills = request.getParameter("bill");
		JSONObject obj = new JSONObject(bills);
		JSONArray billItems = obj.getJSONArray("billItems");
		model.addAttribute("patientId", patientId);
		
		if (encounterId != null) {
			BillingService billingService = Context.getService(BillingService.class);
			IpdService ipdService = Context.getService(IpdService.class);
			PatientService patientService = Context.getPatientService();
			Patient patient = patientService.getPatient(patientId);
			IndoorPatientServiceBill bill = new IndoorPatientServiceBill();
			bill.setCreatedDate(new Date());
			bill.setPatient(patient);
			bill.setCreator(Context.getAuthenticatedUser());
			
			IndoorPatientServiceBillItem item;
			int quantity = 0;
			Money itemAmount;
			Money mUnitPrice;
			Money totalAmount = new Money(BigDecimal.ZERO);
			BigDecimal totalActualAmount = new BigDecimal(0);
			BigDecimal unitPrice;
			String name;
			BillableService service;
			for (int i = 0; i < billItems.length(); i++) {
				JSONObject incomingItem = billItems.getJSONObject(i);
				unitPrice = NumberUtils.createBigDecimal(Integer.toString(incomingItem.getJSONObject("initialBill").getInt(
				    "price")));
				quantity = NumberUtils.createInteger(Integer.toString(incomingItem.getInt("quantity")));
				name = incomingItem.getJSONObject("initialBill").getString("name");
				service = billingService
				        .getServiceByConceptId(incomingItem.getJSONObject("initialBill").getInt("conceptId"));
				mUnitPrice = new Money(unitPrice);
				itemAmount = mUnitPrice.times(quantity);
				totalAmount = totalAmount.plus(itemAmount);
				
				item = new IndoorPatientServiceBillItem();
				item.setCreatedDate(new Date());
				item.setName(name);
				item.setIndoorPatientServiceBill(bill);
				item.setQuantity(quantity);
				item.setService(service);
				item.setUnitPrice(unitPrice);
				item.setAmount(itemAmount.getAmount());
				item.setActualAmount(itemAmount.getAmount());
				item.setOrderType("SERVICE");
				totalActualAmount = totalActualAmount.add(item.getActualAmount());
				
				bill.addBillItem(item);
				
			}
			bill.setAmount(totalAmount.getAmount());
			bill.setActualAmount(totalActualAmount);
			bill.setEncounter(Context.getEncounterService().getEncounter(encounterId));
			bill = billingService.saveIndoorPatientServiceBill(bill);
			List<IndoorPatientServiceBill> indoorPatientServiceBillList = billingService
			        .getIndoorPatientServiceBillByEncounter(Context.getEncounterService().getEncounter(encounterId));
			IndoorPatientServiceBillItem indoorPatientServiceBillItem = billingService.getIndoorPatientServiceBillItem(
			    "ADMISSION FILE CHARGES", indoorPatientServiceBillList);
			IpdPatientAdmission ipdPatientAdmission = ipdService.getIpdPatientAdmissionByEncounter(Context
			        .getEncounterService().getEncounter(encounterId));
			if (indoorPatientServiceBillItem != null && ipdPatientAdmission != null) {
				ipdPatientAdmission.setInitialDepositStatus(1);
				ipdService.saveIpdPatientAdmission(ipdPatientAdmission);
			}
			if (bill != null) {
				billingService.saveBillEncounterAndOrderForIndoorPatient(bill);
			}
			return "redirect:" + uiUtils.pageLink("ehrcashier", "ipdbillingqueue");
			
		} else {
			BillingService billingService = Context.getService(BillingService.class);
			PatientService patientService = Context.getPatientService();
			// Get the BillCalculator to calculate the rate of bill item the patient has to pay
			Patient patient = patientService.getPatient(patientId);
			Map<String, String> attributes = PatientUtils.getAttributes(patient);
			BillCalculatorForBDService calculator = new BillCalculatorForBDService();
			
			PatientServiceBill bill = new PatientServiceBill();
			bill.setCreatedDate(new Date());
			bill.setPatient(patient);
			bill.setCreator(Context.getAuthenticatedUser());
			PatientServiceBillItem item;
			int quantity = 0;
			Money itemAmount;
			Money mUnitPrice;
			Money totalAmount = new Money(BigDecimal.ZERO);
			BigDecimal totalActualAmount = new BigDecimal(0);
			BigDecimal unitPrice;
			String name;
			BillableService service;
			
			//loop over the incoming items
			for (int i = 0; i < billItems.length(); i++) {
				JSONObject incomingItem = billItems.getJSONObject(i);
				unitPrice = NumberUtils.createBigDecimal(Integer.toString(incomingItem.getJSONObject("initialBill").getInt(
				    "price")));
				quantity = NumberUtils.createInteger(Integer.toString(incomingItem.getInt("quantity")));
				name = incomingItem.getJSONObject("initialBill").getString("name");
				service = billingService
				        .getServiceByConceptId(incomingItem.getJSONObject("initialBill").getInt("conceptId"));
				mUnitPrice = new Money(unitPrice);
				itemAmount = mUnitPrice.times(quantity);
				totalAmount = totalAmount.plus(itemAmount);
				
				item = new PatientServiceBillItem();
				item.setCreatedDate(new Date());
				item.setName(name);
				item.setPatientServiceBill(bill);
				item.setQuantity(quantity);
				item.setService(service);
				item.setUnitPrice(unitPrice);
				item.setAmount(itemAmount.getAmount());
				
				// Get the ratio for each bill item
				Map<String, Object> parameters = HospitalCoreUtils.buildParameters("patient", patient, "attributes",
				    attributes, "billItem", item, "request", request);
				BigDecimal rate = calculator.getRate(parameters, billType);
				item.setActualAmount(item.getAmount().multiply(rate));
				totalActualAmount = totalActualAmount.add(item.getActualAmount());
				bill.addBillItem(item);
			}
			bill.setAmount(totalAmount.getAmount());
			bill.setActualAmount(totalActualAmount);
			
			/*added waiver amount */
			if (obj.getInt("waiverAmount") != 0) {
				bill.setWaiverAmount(NumberUtils.createBigDecimal(obj.getString("waiverAmount")));
			} else {
				BigDecimal wavAmt = new BigDecimal(0);
				bill.setWaiverAmount(wavAmt);
			}
			bill.setComment(obj.getString("comment"));
			HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
			List<PersonAttribute> pas = hcs.getPersonAttributes(patientId);
			String patientSubCategory = null;
			PersonService personService = Context.getPersonService();
			
			PersonAttributeType paymentSubCategory = personService
			        .getPersonAttributeTypeByUuid("972a32aa-6159-11eb-bc2d-9785fed39154");
			
			for (PersonAttribute pa : pas) {
				PersonAttributeType attributeType = pa.getAttributeType();
				PersonAttributeType personAttributePCT = hcs.getPersonAttributeTypeByName("Paying Category Type");
				PersonAttributeType personAttributeNPCT = hcs.getPersonAttributeTypeByName("Non-Paying Category Type");
				PersonAttributeType personAttributeSSCT = hcs.getPersonAttributeTypeByName("Special Scheme Category Type");
				if (attributeType.getPersonAttributeTypeId().equals(personAttributePCT.getPersonAttributeTypeId())) {
					patientSubCategory = pa.getValue();
				} else if (attributeType.getPersonAttributeTypeId().equals(personAttributeNPCT.getPersonAttributeTypeId())) {
					patientSubCategory = pa.getValue();
				} else if (attributeType.getPersonAttributeTypeId().equals(personAttributeSSCT.getPersonAttributeTypeId())) {
					patientSubCategory = pa.getValue();
				}
			}
			bill.setPatientSubCategory(patient.getAttribute(paymentSubCategory).getValue());
			
			bill.setPaymentMode(paymentMode);
			
			bill.setFreeBill(calculator.isFreeBill(billType));
			logger.info("Is free bill: " + bill.getFreeBill());
			
			bill.setReceipt(billingService.createReceipt());
			bill = billingService.savePatientServiceBill(bill);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("patientId", patientId);
			params.put("billId", bill.getPatientServiceBillId());
			params.put("billType", billType);
			
			return "redirect:" + uiUtils.pageLink("ehrcashier", "patientServiceBillForBD", params);
			
		}
		
	}
}
