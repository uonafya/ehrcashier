package org.openmrs.module.ehrcashier.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ehrcashier.EhrCashierConstants;
import org.openmrs.module.ehrcashier.billcalculator.BillCalculatorForBDService;
import org.openmrs.module.hospitalcore.BillingService;
import org.openmrs.module.hospitalcore.HospitalCoreService;
import org.openmrs.module.hospitalcore.model.BillableService;
import org.openmrs.module.hospitalcore.model.PatientServiceBill;
import org.openmrs.module.hospitalcore.model.PatientServiceBillItem;
import org.openmrs.module.hospitalcore.util.HospitalCoreUtils;
import org.openmrs.module.hospitalcore.util.Money;
import org.openmrs.module.hospitalcore.util.PatientUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@AppPage(EhrCashierConstants.APP_EHRCASHIER)
public class EditPatientServiceBillForBDPageController {
	
	private Log logger = LogFactory.getLog(getClass());
	
	public String get(PageModel model, UiSessionContext sessionContext, PageRequest pageRequest, UiUtils ui,
	        @RequestParam("billId") Integer billId, @RequestParam("patientId") Integer patientId) {
		BillAccess ba = new BillAccess();
		boolean auth = ba.authenticate(pageRequest, sessionContext, ui);
		if (!auth) {
			return "redirect: index.htm";
		}
		Patient patient = Context.getPatientService().getPatient(patientId);
		Map<String, String> attributes = PatientUtils.getAttributes(patient);
		
		BillingService billingService = Context.getService(BillingService.class);
		HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
		
		List<BillableService> services = billingService.getAllServices();
		Map<Integer, BillableService> mapServices = new HashMap<Integer, BillableService>();
		for (BillableService ser : services) {
			mapServices.put(ser.getConceptId(), ser);
		}
		Integer conceptId = Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
		    "billing.rootServiceConceptId"));
		Concept concept = Context.getConceptService().getConcept(conceptId);
		model.addAttribute("serviceMap", mapServices);
		model.addAttribute("tabs", billingService.traversTab(concept, mapServices, 1));
		model.addAttribute("patientId", patientId);
		model.addAttribute("previousVisit", hcs.getLastVisitTime(patient));
		
		PatientServiceBill bill = billingService.getPatientServiceBillById(billId);
		Set<PatientServiceBillItem> billItemsRaw = bill.getBillItems();
		Set<PatientServiceBillItem> billItems = new HashSet<PatientServiceBillItem>();
		for (PatientServiceBillItem patientServiceBill : billItemsRaw) {
			if (!patientServiceBill.getVoided()) {
				billItems.add(patientServiceBill);
			}
		}
		
		List<SimpleObject> simpleObjects = SimpleObject.fromCollection(billItems, ui, "patientServiceBillItemId",
		    "service.conceptId", "service.name", "quantity", "amount", "unitPrice");
		
		String billingItems = SimpleObject.create("billingItems", simpleObjects).toJson();
		model.addAttribute("billingItems", billingItems);
		
		model.addAttribute("freeBill", bill.getFreeBill());
		model.addAttribute("waiverAm", bill.getWaiverAmount());
		model.addAttribute("bill", bill);
		model.addAttribute("billId", billId);
		model.addAttribute("patient", patient);
		model.addAttribute(
		    "category",
		    patient.getAttribute(Context.getPersonService().getPersonAttributeTypeByUuid(
		        "09cd268a-f0f5-11ea-99a8-b3467ddbf779")));
		model.addAttribute("age", patient.getAge());
		
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
		
		if (patient.getGender().equals("M")) {
			model.addAttribute("gender", "Male");
		}
		if (patient.getGender().equals("F")) {
			model.addAttribute("gender", "Female");
		}
		return null;
	}
	
	public String post(PageModel pageModel, Object command, BindingResult bindingResult, HttpServletRequest request,
	        @RequestParam("patientId") Integer patientId, @RequestParam("billId") Integer billId,
	        @RequestParam("action") String action, UiUtils ui) {
		String bills = request.getParameter("bill");
		JSONObject obj = new JSONObject(bills);
		JSONArray billItems = obj.getJSONArray("billItems");
		pageModel.addAttribute("patientId", patientId);
		
		BillingService billingService = Context.getService(BillingService.class);
		PatientServiceBill bill = billingService.getPatientServiceBillById(billId);
		// Get the BillCalculator to calculate the rate of bill item the patient
		// has to pay
		Patient patient = Context.getPatientService().getPatient(patientId);
		Map<String, String> attributes = PatientUtils.getAttributes(patient);
		
		BillCalculatorForBDService calculator = new BillCalculatorForBDService();
		
		/*if (StringUtils.isNotBlank(obj.getString("comment"))) {
			bill.setDescription(obj.getString("comment"));
		}*/
		
		if (StringUtils.isNotBlank(action)) {
			if (action.equalsIgnoreCase("void")) {
				bill.setVoided(true);
				bill.setVoidedDate(new Date());
				for (PatientServiceBillItem item : bill.getBillItems()) {
					item.setVoided(true);
					item.setVoidedDate(new Date());
					/*these 5 lines of code written only due to voided item is being updated in "billing_patient_service_bill_item" table
					  but not being updated in "orders" table */
					Order ord = item.getOrder();
					if (ord != null) {
						ord.setVoided(true);
						ord.setDateVoided(new Date());
					}
					item.setOrder(ord);
				}
				billingService.savePatientServiceBill(bill);
				// Support #343 [Billing][3.2.7-SNAPSHOT]No Queue to be generated from Old bill
				Map<String, Object> redirectParams = new HashMap<String, Object>();
				redirectParams.put("patientId", patientId);
				redirectParams.put("billId", billId);
				return "redirect:" + ui.pageLink("ehrcashier", "billableServiceBillListForBD", redirectParams);
			}
		}
		
		// void old items and reset amount
		Map<Integer, PatientServiceBillItem> mapOldItems = new HashMap<Integer, PatientServiceBillItem>();
		for (PatientServiceBillItem item : bill.getBillItems()) {
			item.setVoided(true);
			item.setVoidedDate(new Date());
			//Bug #323 [BILLING] When a bill with a lab\radiology order is edited the order is re-sent
			Order ord = item.getOrder();
			/* [Billing - Bug #337] [3.2.7 snap shot][billing(DDU,DDU SDMX,Tanda,mohali)]error in edit bill.
			  the problem was while we are editing the bill of other than lab and radiology.
			*/
			if (ord != null) {
				ord.setVoided(true);
				ord.setDateVoided(new Date());
			}
			item.setOrder(ord);
			mapOldItems.put(item.getPatientServiceBillItemId(), item);
		}
		bill.setAmount(BigDecimal.ZERO);
		bill.setPrinted(false);
		
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
			System.out.println(incomingItem);
			unitPrice = NumberUtils.createBigDecimal(Integer.toString(incomingItem.getInt("price")));
			quantity = NumberUtils.createInteger(Integer.toString(incomingItem.getInt("quantity")));
			name = incomingItem.getJSONObject("initialBill").getJSONObject("service").getString("name");
			service = billingService.getServiceByConceptId(incomingItem.getJSONObject("initialBill")
			        .getJSONObject("service").getInt("conceptId"));
			mUnitPrice = new Money(unitPrice);
			itemAmount = mUnitPrice.times(quantity);
			totalAmount = totalAmount.plus(itemAmount);
			Integer sItemId = incomingItem.getJSONObject("initialBill").getInt("patientServiceBillItemId");
			if (sItemId == null) {
				item = new PatientServiceBillItem();
				
				// Get the ratio for each bill item
				Map<String, Object> parameters = HospitalCoreUtils.buildParameters("patient", patient, "attributes",
				    attributes, "billItem", item);
				BigDecimal rate;
				// New Requirement #966[Billing]Add Paid Bill/Add Free Bill for Bangladesh module
				// New Requirement #1632 Orders from dashboard must be appear in billing queue.User must be able to generate bills from this queue
				if (bill.getFreeBill().equals(1)) {
					String billType = "free";
					rate = calculator.getRate(parameters, billType);
				} else if (bill.getFreeBill().equals(2)) {
					String billType = "mixed";
					PatientServiceBillItem patientServiceBillItem = billingService.getPatientServiceBillItem(billId, name);
					String psbi = patientServiceBillItem.getActualAmount().toString();
					if (psbi.equals("0.00")) {
						rate = new BigDecimal(0);
					} else {
						rate = new BigDecimal(1);
					}
					item.setActualAmount(item.getAmount().multiply(rate));
				} else {
					String billType = "paid";
					rate = calculator.getRate(parameters, billType);
				}
				
				item.setAmount(itemAmount.getAmount());
				item.setActualAmount(item.getAmount().multiply(rate));
				totalActualAmount = totalActualAmount.add(item.getActualAmount());
				item.setCreatedDate(new Date());
				item.setName(name);
				item.setPatientServiceBill(bill);
				item.setQuantity(quantity);
				item.setService(service);
				item.setUnitPrice(unitPrice);
				bill.addBillItem(item);
			} else {
				item = mapOldItems.get(sItemId);
				// Get the ratio for each bill item
				Map<String, Object> parameters = HospitalCoreUtils.buildParameters("patient", patient, "attributes",
				    attributes, "billItem", item);
				BigDecimal rate;
				// New Requirement #966[Billing]Add Paid Bill/Add Free Bill for Bangladesh module
				// New Requirement #1632 Orders from dashboard must be appear in billing queue.User must be able to generate bills from this queue
				if (bill.getFreeBill().equals(1)) {
					String billType = "free";
					rate = calculator.getRate(parameters, billType);
				} else if (bill.getFreeBill().equals(2)) {
					String billType = "mixed";
					PatientServiceBillItem patientServiceBillItem = billingService.getPatientServiceBillItem(billId, name);
					String psbi = patientServiceBillItem.getActualAmount().toString();
					if (psbi.equals("0.00")) {
						rate = new BigDecimal(0);
					} else {
						rate = new BigDecimal(1);
					}
					item.setActualAmount(item.getAmount().multiply(rate));
				} else {
					String billType = "paid";
					rate = calculator.getRate(parameters, billType);
				}
				
				// [Billing - Support #344] [Billing] Edited Quantity and Amount information is lost in database
				if (quantity != item.getQuantity()) {
					item.setVoided(true);
					item.setVoidedDate(new Date());
				} else {
					item.setVoided(false);
					item.setVoidedDate(null);
				}
				// Bug #323 [BILLING] When a bill with a lab\radiology order is edited the order is re-sent
				Order ord = item.getOrder();
				if (ord != null) {
					ord.setVoided(false);
					ord.setDateVoided(null);
				}
				item.setOrder(ord);
				// [Billing - Support #344] [Billing] Edited Quantity and Amount information is lost in database
				if (quantity != item.getQuantity()) {
					item = new PatientServiceBillItem();
					item.setService(service);
					item.setUnitPrice(unitPrice);
					item.setQuantity(quantity);
					item.setName(name);
					item.setCreatedDate(new Date());
					item.setOrder(ord);
					bill.addBillItem(item);
				}
				item.setAmount(itemAmount.getAmount());
				item.setActualAmount(item.getAmount().multiply(rate));
				
				totalActualAmount = totalActualAmount.add(item.getActualAmount());
			}
			
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
		String waiverNumber = obj.getString("waiverNumber");
		
		if (waiverNumber != null && waiverNumber != "") {
			bill.setPatientCategory("Waiver Number - " + waiverNumber);
		}
		
		// Determine whether the bill is free or not
		
		// New Requirement #966[Billing]Add Paid Bill/Add Free Bill for Bangladesh module
		// New Requirement #1632 Orders from dashboard must be appear in billing queue.User must be able to generate bills from this queue
		if (bill.getFreeBill().equals(1)) {
			String billType = "free";
			bill.setFreeBill(calculator.isFreeBill(billType));
		} else if (bill.getFreeBill().equals(2)) {
			String billType = "mixed";
			bill.setFreeBill(2);
		} else {
			String billType = "paid";
			bill.setFreeBill(calculator.isFreeBill(billType));
		}
		
		logger.info("Is free bill: " + bill.getFreeBill());
		
		bill = billingService.savePatientServiceBill(bill);
		// #343 [Billing][3.2.7-SNAPSHOT]No Queue to be generated from Old bill
		Map<String, Object> redirectParams = new HashMap<String, Object>();
		redirectParams.put("patientId", patientId);
		redirectParams.put("billId", billId);
		
		return "redirect:" + ui.pageLink("ehrcashier", "billableServiceBillListForBD", redirectParams);
	}
	
}
