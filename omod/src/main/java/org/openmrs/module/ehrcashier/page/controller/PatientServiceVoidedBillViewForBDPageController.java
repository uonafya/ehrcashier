package org.openmrs.module.ehrcashier.page.controller;

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ehrcashier.EhrCashierConstants;
import org.openmrs.module.ehrcashier.billcalculator.BillCalculatorForBDService;
import org.openmrs.module.hospitalcore.BillingConstants;
import org.openmrs.module.hospitalcore.BillingService;
import org.openmrs.module.hospitalcore.model.PatientServiceBill;
import org.openmrs.module.hospitalcore.util.PagingUtil;
import org.openmrs.module.hospitalcore.util.PatientUtils;
import org.openmrs.module.hospitalcore.util.RequestUtil;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@AppPage(EhrCashierConstants.APP_EHRCASHIER)
public class PatientServiceVoidedBillViewForBDPageController {
	
	/**
	 * Default method for handling POST and GET requests
	 */
	public void controller() {
		
	}
	
	public String get(PageModel pageModel, UiSessionContext sessionContext, PageRequest pageRequest, UiUtils ui,
	        @RequestParam("patientId") Integer patientId, @RequestParam(value = "billId", required = false) Integer billId,
	        @RequestParam(value = "pageSize", required = false) Integer pageSize,
	        @RequestParam(value = "currentPage", required = false) Integer currentPage, HttpServletRequest request,
	        UiUtils uiUtils) {
		
		BillingService billingService = Context.getService(BillingService.class);
		Patient patient = Context.getPatientService().getPatient(patientId);
		Map<String, String> attributes = PatientUtils.getAttributes(patient);
		BillCalculatorForBDService calculator = new BillCalculatorForBDService();
		PatientServiceBill bill = billingService.getPatientServiceBillById(billId);
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
		
		if (patient != null) {
			
			int total = billingService.countListPatientServiceBillByPatient(patient);
			PagingUtil pagingUtil = new PagingUtil(RequestUtil.getCurrentLink(request), pageSize, currentPage, total);
			pageModel.addAttribute("pagingUtil", pagingUtil);
			pageModel.addAttribute("patient", patient);
			pageModel.addAttribute("listBill",
			    billingService.listPatientServiceBillByPatient(pagingUtil.getStartPos(), pagingUtil.getPageSize(), patient));
		}
		if (billId != null) {
			
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
			
			pageModel.addAttribute("bill", bill);
		}
		User user = Context.getAuthenticatedUser();
		
		pageModel.addAttribute("canEdit", user.hasPrivilege(BillingConstants.PRIV_EDIT_BILL_ONCE_PRINTED));
		
		Map<String, Object> redirectParams = new HashMap<String, Object>();
		redirectParams.put("patientId", patientId);
		redirectParams.put("billId", billId);
		return "redirect:" + uiUtils.pageLink("ehrcashier", "billableServiceBillListForBD", redirectParams);
	}
	
}
