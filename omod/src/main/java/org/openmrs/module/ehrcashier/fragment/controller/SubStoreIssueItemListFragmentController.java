package org.openmrs.module.ehrcashier.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.ehrinventory.InventoryService;
import org.openmrs.module.ehrinventory.model.InventoryStoreItemPatient;
import org.openmrs.module.ehrinventory.model.InventoryStoreItemPatientDetail;
import org.openmrs.module.ehrinventory.util.PagingUtil;
import org.openmrs.module.ehrinventory.util.RequestUtil;
import org.openmrs.module.hospitalcore.util.FlagStates;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubStoreIssueItemListFragmentController {
	
	public void controller() {
	}
	
	/**
	 * @param pageSize
	 * @param currentPage
	 * @param issueName
	 * @param fromDate
	 * @param toDate
	 * @param receiptId
	 * @param request
	 * @param uiUtils
	 * @return
	 */
	public List<SimpleObject> getItemOrderList(@RequestParam(value = "pageSize", required = false) Integer pageSize,
	        @RequestParam(value = "currentPage", required = false) Integer currentPage,
	        @RequestParam(value = "issueName", required = false) String issueName,
	        @RequestParam(value = "fromDate", required = false) String fromDate,
	        @RequestParam(value = "toDate", required = false) String toDate,
	        @RequestParam(value = "receiptId", required = false) Integer receiptId,
	        @RequestParam(value = "processed", required = false) Integer processed, HttpServletRequest request,
	        UiUtils uiUtils) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		InventoryService inventoryService = (InventoryService) Context.getService(InventoryService.class);
		
		int total = inventoryService.countStoreItemPatient(4, issueName, fromDate, toDate);
		String temp = "";
		
		if (issueName != null) {
			if (StringUtils.isBlank(temp)) {
				temp = "?issueName=" + issueName;
			} else {
				temp += "&issueName=" + issueName;
			}
		}
		if (!StringUtils.isBlank(fromDate)) {
			if (StringUtils.isBlank(temp)) {
				temp = "?fromDate=" + fromDate;
			} else {
				temp += "&fromDate=" + fromDate;
			}
		}
		if (!StringUtils.isBlank(toDate)) {
			if (StringUtils.isBlank(temp)) {
				temp = "?toDate=" + toDate;
			} else {
				temp += "&toDate=" + toDate;
			}
		}
		if (receiptId != null) {
			if (StringUtils.isBlank(temp)) {
				temp = "?receiptId=" + receiptId;
			} else {
				temp += "&receiptId=" + receiptId;
			}
		}
		PagingUtil pagingUtil = new PagingUtil(RequestUtil.getCurrentLink(request) + temp, pageSize, currentPage, total);
		if (StringUtils.isBlank(fromDate)) {
			fromDate = sdf.format(new Date());
		}
		List<SimpleObject> orderList = new ArrayList<SimpleObject>();
		List<InventoryStoreItemPatient> listIssue = inventoryService.listStoreItemPatient(4, receiptId, issueName, fromDate,
		    toDate, pagingUtil.getStartPos(), pagingUtil.getPageSize());
		for (InventoryStoreItemPatient inventoryStoreItemPatient : listIssue) {
			//???
			inventoryStoreItemPatient = inventoryService.saveStoreItemPatient(inventoryStoreItemPatient);
			List<InventoryStoreItemPatientDetail> inventoryStoreItemPatientDetails = inventoryService
			        .listStoreItemPatientDetail(inventoryStoreItemPatient.getId());
			
			Integer flags = FlagStates.NOT_PROCESSED;
			if (inventoryStoreItemPatientDetails.size() > 0) {
				flags = inventoryStoreItemPatientDetails.get(inventoryStoreItemPatientDetails.size() - 1)
				        .getTransactionDetail().getFlag();
			}
			if (flags == null) {
				flags = FlagStates.NOT_PROCESSED;
			}
			if (flags >= FlagStates.PARTIALLY_PROCESSED && processed == FlagStates.NOT_PROCESSED) {
				continue;
			}
			
			String created = sdf.format(inventoryStoreItemPatient.getCreatedOn());
			String changed = sdf.format(new Date());
			int value = changed.compareTo(created);
			inventoryStoreItemPatient.setValues(value);
			//create simple object
			SimpleObject orderItem = SimpleObject.create("id", inventoryStoreItemPatient.getId());
			orderItem.put("identifier", inventoryStoreItemPatient.getIdentifier());
			orderItem.put("patientId", inventoryStoreItemPatient.getPatient().getId());
			orderItem.put("givenName", inventoryStoreItemPatient.getPatient().getGivenName());
			orderItem.put("familyName", inventoryStoreItemPatient.getPatient().getFamilyName());
			orderItem.put("middleName", inventoryStoreItemPatient.getPatient().getMiddleName());
			orderItem.put("patient.age", inventoryStoreItemPatient.getPatient().getAge());
			orderItem.put("gender", inventoryStoreItemPatient.getPatient().getGender());
			orderItem.put("createdOn", inventoryStoreItemPatient.getCreatedOn());
			orderItem.put("flag", flags);
			orderList.add(orderItem);
		}
		return orderList;
	}
}
