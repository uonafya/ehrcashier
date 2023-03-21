package org.openmrs.module.ehrcashier.page.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ehrcashier.EhrCashierConstants;
import org.openmrs.module.ehrconfigs.metadata.EhrCommonMetadata;
import org.openmrs.module.ehrinventory.InventoryService;
import org.openmrs.module.ehrinventory.model.InventoryStoreItemPatient;
import org.openmrs.module.ehrinventory.model.InventoryStoreItemPatientDetail;
import org.openmrs.module.ehrinventory.model.InventoryStoreItemTransaction;
import org.openmrs.module.ehrinventory.model.InventoryStoreItemTransactionDetail;
import org.openmrs.module.ehrinventory.util.DateUtils;
import org.openmrs.module.hospitalcore.HospitalCoreService;
import org.openmrs.module.hospitalcore.model.*;
import org.openmrs.module.hospitalcore.util.ActionValue;
import org.openmrs.module.hospitalcore.util.FlagStates;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AppPage(EhrCashierConstants.APP_EHRCASHIER)
public class ProcessItemOrderPageController {
	
	public String get(PageModel model, UiSessionContext sessionContext, PageRequest pageRequest,
	        @RequestParam("orderId") Integer orderId, PageModel pageModel, UiUtils ui) {
		
		pageModel.addAttribute("userLocation", Context.getService(KenyaEmrService.class).getDefaultLocation().getName());
		InventoryService inventoryService = Context.getService(InventoryService.class);
		
		InventoryStore store = inventoryService.getStoreById(4);
		
		List<InventoryStoreItemPatientDetail> listItemIssue = inventoryService.listStoreItemPatientDetail(orderId);
		if (listItemIssue != null && listItemIssue.size() > 0) {
			InventoryStoreItemTransaction transaction = new InventoryStoreItemTransaction();
			transaction.setDescription("ISSUE ITEM TO PATIENT " + DateUtils.getDDMMYYYY());
			transaction.setStore(store);
			transaction.setTypeTransaction(ActionValue.TRANSACTION[1]);
			transaction.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
			
			transaction = inventoryService.saveStoreItemTransaction(transaction);
			for (InventoryStoreItemPatientDetail pDetail : listItemIssue) {
				Date date1 = new Date();
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Integer totalQuantity = inventoryService
				        .sumStoreItemCurrentQuantity(store.getId(), pDetail.getTransactionDetail().getItem().getId(),
				            pDetail.getTransactionDetail().getSpecification().getId());
				
				int t = totalQuantity;
				
				Integer receipt = pDetail.getStoreItemPatient().getId();
				model.addAttribute("receiptid", receipt);
				
				InventoryStoreItemTransactionDetail inventoryStoreItemTransactionDetail = inventoryService
				        .getStoreItemTransactionDetailById(pDetail.getTransactionDetail().getParent().getId());
				
				InventoryStoreItemTransactionDetail itemTransactionDetail = inventoryService
				        .getStoreItemTransactionDetailById(inventoryStoreItemTransactionDetail.getId());
				
				inventoryStoreItemTransactionDetail.setCurrentQuantity(itemTransactionDetail.getCurrentQuantity());
				Integer flags = pDetail.getTransactionDetail().getFlag();
				
				if (flags == null) {
					model.addAttribute("flag", FlagStates.NOT_PROCESSED);
				} else if (flags >= 1) {
					model.addAttribute("flag", FlagStates.PARTIALLY_PROCESSED);
				} else {
					model.addAttribute("flag", flags);
				}
				
				inventoryService.saveStoreItemTransactionDetail(inventoryStoreItemTransactionDetail);
				// save transactiondetail first
				InventoryStoreItemTransactionDetail transDetail = new InventoryStoreItemTransactionDetail();
				transDetail.setTransaction(transaction);
				transDetail.setCurrentQuantity(0);
				transDetail.setIssueQuantity(pDetail.getQuantity());
				transDetail.setOpeningBalance(totalQuantity);
				transDetail.setClosingBalance(t);
				transDetail.setQuantity(0);
				transDetail.setVAT(pDetail.getTransactionDetail().getVAT());
				transDetail.setCostToPatient(pDetail.getTransactionDetail().getCostToPatient());
				transDetail.setUnitPrice(pDetail.getTransactionDetail().getUnitPrice());
				transDetail.setItem(pDetail.getTransactionDetail().getItem());
				transDetail.setCompanyName(pDetail.getTransactionDetail().getCompanyName());
				transDetail.setDateManufacture(pDetail.getTransactionDetail().getDateManufacture());
				transDetail.setReceiptDate(pDetail.getTransactionDetail().getReceiptDate());
				transDetail.setCreatedOn(date1);
				transDetail.setAttribute(pDetail.getTransactionDetail().getItem().getAttributeName());
				transDetail.setSpecification(pDetail.getTransactionDetail().getSpecification());
				transDetail.setFlag(FlagStates.PARTIALLY_PROCESSED);
				BigDecimal moneyUnitPrice = pDetail.getTransactionDetail().getCostToPatient()
				        .multiply(new BigDecimal(pDetail.getQuantity()));
				transDetail.setTotalPrice(moneyUnitPrice);
				transDetail.setParent(pDetail.getTransactionDetail());
				transDetail = inventoryService.saveStoreItemTransactionDetail(transDetail);
				
			}
			
		}
		if (listItemIssue != null && listItemIssue.size() > 0) {
			model.addAttribute("waiverAmount", 0);
			model.addAttribute("waiverComment", "");
		}
		
		List<SimpleObject> dispensedItems = SimpleObject.fromCollection(listItemIssue, ui, "quantity",
		    "transactionDetail.costToPatient", "transactionDetail.item.name", "transactionDetail.specification.name",
		    "transactionDetail.item.category.name", "transactionDetail.item.subCategory.name",
		    "transactionDetail.item.specifications.name");
		model.addAttribute("listItemIssue", SimpleObject.create("listItemIssue", dispensedItems).toJson());
		if (CollectionUtils.isNotEmpty(listItemIssue)) {
			model.addAttribute("issueItemPatient", listItemIssue.get(0).getStoreItemPatient());
			model.addAttribute("date", listItemIssue.get(0).getStoreItemPatient().getCreatedOn());
			
			int age = listItemIssue.get(0).getStoreItemPatient().getPatient().getAge();
			if (age < 1) {
				model.addAttribute("age", "<1");
			} else {
				model.addAttribute("age", age);
			}
			//TODO starts here
			
			PatientIdentifier pi = listItemIssue.get(0).getStoreItemPatient().getPatient().getPatientIdentifier();
			int patientId = pi.getPatient().getPatientId();
			Date issueDate = listItemIssue.get(0).getStoreItemPatient().getCreatedOn();
			
			//TODO ends here
			pageModel.addAttribute("birthdate", listItemIssue.get(0).getStoreItemPatient().getPatient().getBirthdate());
			
			model.addAttribute("identifier", listItemIssue.get(0).getStoreItemPatient().getPatient().getPatientIdentifier());
			model.addAttribute("givenName", listItemIssue.get(0).getStoreItemPatient().getPatient().getGivenName());
			model.addAttribute("familyName", listItemIssue.get(0).getStoreItemPatient().getPatient().getFamilyName());
			if (listItemIssue.get(0).getStoreItemPatient().getPatient().getMiddleName() != null) {
				model.addAttribute("middleName", listItemIssue.get(0).getStoreItemPatient().getPatient().getMiddleName());
			} else {
				model.addAttribute("middleName", " ");
			}
			if (listItemIssue.get(0).getStoreItemPatient().getPatient().getGender().equals("M")) {
				model.addAttribute("gender", "Male");
			}
			if (listItemIssue.get(0).getStoreItemPatient().getPatient().getGender().equals("F")) {
				model.addAttribute("gender", "Female");
			}
			
			HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
			List<PersonAttribute> pas = hcs.getPersonAttributes(listItemIssue.get(0).getStoreItemPatient().getPatient()
			        .getId());
			
			String prescriber = null;
			
			if (listItemIssue.get(0).getStoreItemPatient().getCreatedBy() != null) {
				prescriber = listItemIssue.get(0).getStoreItemPatient().getCreatedBy();
			}
			
			model.addAttribute("pharmacist", listItemIssue.get(0).getStoreItemPatient().getCreatedBy());
			model.addAttribute("cashier", Context.getAuthenticatedUser().getPersonName());
			model.addAttribute("prescriber", prescriber);
			model.addAttribute("lastVisit", hcs.getLastVisitTime(listItemIssue.get(0).getStoreItemPatient().getPatient()));
			PersonAttributeType patientCategoryAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(
			    "09cd268a-f0f5-11ea-99a8-b3467ddbf779");
			PersonAttributeType payingCategoryAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(
			    "972a32aa-6159-11eb-bc2d-9785fed39154");
			
			model.addAttribute("paymentSubCategory", pi.getPatient().getAttribute(payingCategoryAttributeType).getValue());
			model.addAttribute("paymentCategory", pi.getPatient().getAttribute(patientCategoryAttributeType).getValue());
			model.addAttribute("paymentCategoryName", pi.getPatient().getAttribute(patientCategoryAttributeType).getValue());
		}
		return null;
	}
	
	public String post(HttpServletRequest request, PageModel pageModel, UiUtils ui) {
		pageModel.addAttribute("userLocation", Context.getService(KenyaEmrService.class).getDefaultLocation().getName());
		
		String itemOrder = request.getParameter("itemOrder");
		JSONObject jsonObject = new JSONObject(itemOrder);
		
		String comment = jsonObject.getString("comment");
		String waiverAmountString = jsonObject.getString("waiverAmount");
		
		BigDecimal waiverAmount = null;
		if (StringUtils.isNotEmpty(waiverAmountString)) {
			waiverAmount = new BigDecimal(waiverAmountString);
		}
		
		int receiptid = Integer.parseInt(request.getParameter("receiptid"));
		pageModel.addAttribute("receiptid", receiptid);
		InventoryService inventoryService = (InventoryService) Context.getService(InventoryService.class);
		List<Role> role = new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles());
		//InventoryStoreRoleRelation srl = null;
		InventoryStore store = inventoryService.getStoreById(4);
		
		List<InventoryStoreItemPatientDetail> listItemIssue = inventoryService.listStoreItemPatientDetail(receiptid);
		InventoryStoreItemPatient inventoryStoreItemPatient = null;
		
		if (listItemIssue != null && listItemIssue.size() > 0) {
			InventoryStoreItemTransaction transaction = new InventoryStoreItemTransaction();
			transaction.setDescription("ISSUE ITEM TO PATIENT " + DateUtils.getDDMMYYYY());
			transaction.setStore(store);
			transaction.setTypeTransaction(ActionValue.TRANSACTION[1]);
			transaction.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
			transaction = inventoryService.saveStoreItemTransaction(transaction);
			for (InventoryStoreItemPatientDetail pDetail : listItemIssue) {
				Date date1 = new Date();
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Integer totalQuantity = inventoryService
				        .sumStoreItemCurrentQuantity(store.getId(), pDetail.getTransactionDetail().getItem().getId(),
				            pDetail.getTransactionDetail().getSpecification().getId());
				int t = totalQuantity - pDetail.getQuantity();
				
				InventoryStoreItemTransactionDetail inventoryStoreItemTransactionDetail = inventoryService
				        .getStoreItemTransactionDetailById(pDetail.getTransactionDetail().getParent().getId());
				InventoryStoreItemTransactionDetail itemTransactionDetail = inventoryService
				        .getStoreItemTransactionDetailById(inventoryStoreItemTransactionDetail.getId());
				inventoryStoreItemTransactionDetail.setCurrentQuantity(itemTransactionDetail.getCurrentQuantity()
				        - pDetail.getQuantity());
				inventoryService.saveStoreItemTransactionDetail(inventoryStoreItemTransactionDetail);
				
				// save transactiondetail first
				InventoryStoreItemTransactionDetail transDetail = new InventoryStoreItemTransactionDetail();
				transDetail.setTransaction(transaction);
				transDetail.setCurrentQuantity(0);
				
				transDetail.setIssueQuantity(pDetail.getQuantity());
				transDetail.setOpeningBalance(totalQuantity);
				transDetail.setClosingBalance(t);
				transDetail.setQuantity(0);
				transDetail.setVAT(pDetail.getTransactionDetail().getVAT());
				transDetail.setCostToPatient(pDetail.getTransactionDetail().getCostToPatient());
				transDetail.setUnitPrice(pDetail.getTransactionDetail().getUnitPrice());
				transDetail.setItem(pDetail.getTransactionDetail().getItem());
				transDetail.setCompanyName(pDetail.getTransactionDetail().getCompanyName());
				transDetail.setDateManufacture(pDetail.getTransactionDetail().getDateManufacture());
				transDetail.setReceiptDate(pDetail.getTransactionDetail().getReceiptDate());
				transDetail.setCreatedOn(date1);
				transDetail.setAttribute(pDetail.getTransactionDetail().getItem().getAttributeName());
				transDetail.setFlag(1);
				
				BigDecimal moneyUnitPrice = pDetail.getTransactionDetail().getCostToPatient()
				        .multiply(new BigDecimal(pDetail.getQuantity()));
				
				transDetail.setTotalPrice(moneyUnitPrice);
				transDetail.setParent(pDetail.getTransactionDetail());
				transDetail.setSpecification(pDetail.getTransactionDetail().getSpecification());
				transDetail = inventoryService.saveStoreItemTransactionDetail(transDetail);
				pDetail.setQuantity(pDetail.getQuantity());
				
				pDetail.setTransactionDetail(transDetail);
				
				// save issue to patient detail
				inventoryService.saveStoreItemPatientDetail(pDetail);
				inventoryStoreItemPatient = inventoryService.getStoreItemPatientById(pDetail.getStoreItemPatient().getId());
				if (transDetail.getFlag() == FlagStates.PARTIALLY_PROCESSED) {
					
					inventoryStoreItemPatient.setStatuss(1);
					
				}
				Integer flags = pDetail.getTransactionDetail().getFlag();
				pageModel.addAttribute("flag", flags);
			}
			// update patient detail
			//inventoryStoreItemPatient.setWaiverAmount(waiverAmount);
			//inventoryStoreDrugPatient.setComment(comment);
			inventoryService.saveStoreItemPatient(inventoryStoreItemPatient);
			
			List<SimpleObject> dispensedItems = SimpleObject.fromCollection(listItemIssue, ui, "quantity",
			    "transactionDetail.costToPatient", "transactionDetail.item.name", "transactionDetail.specification.name",
			    "transactionDetail.item.category.name", "transactionDetail.item.subCategory.name",
			    "transactionDetail.item.specifications.name");
			pageModel.addAttribute("listDrugIssue", SimpleObject.create("listDrugIssue", dispensedItems).toJson());
			if (CollectionUtils.isNotEmpty(listItemIssue)) {
				pageModel.addAttribute("issueItemPatient", listItemIssue.get(0).getStoreItemPatient());
				
				pageModel.addAttribute("date", listItemIssue.get(0).getStoreItemPatient().getCreatedOn());
				pageModel.addAttribute("age", listItemIssue.get(0).getStoreItemPatient().getPatient().getAge());
				
				//TODO starts here
				
				PatientIdentifier pi = listItemIssue.get(0).getStoreItemPatient().getPatient().getPatientIdentifier();
				
				int patientId = pi.getPatient().getPatientId();
				Date issueDate = listItemIssue.get(0).getStoreItemPatient().getCreatedOn();
				
				//TODO ends here
				
				pageModel.addAttribute("identifier", listItemIssue.get(0).getStoreItemPatient().getPatient()
				        .getPatientIdentifier());
				pageModel.addAttribute("givenName", listItemIssue.get(0).getStoreItemPatient().getPatient().getGivenName());
				pageModel
				        .addAttribute("familyName", listItemIssue.get(0).getStoreItemPatient().getPatient().getFamilyName());
				
				if (listItemIssue.get(0).getStoreItemPatient().getPatient().getMiddleName() != null) {
					pageModel.addAttribute("middleName", listItemIssue.get(0).getStoreItemPatient().getPatient()
					        .getMiddleName());
				} else {
					pageModel.addAttribute("middleName", "");
				}
				
				if (listItemIssue.get(0).getStoreItemPatient().getPatient().getGender().equals("M")) {
					pageModel.addAttribute("gender", "Male");
				} else {
					pageModel.addAttribute("gender", "Female");
				}
				pageModel.addAttribute("pharmacist", listItemIssue.get(0).getStoreItemPatient().getCreatedBy());
				pageModel.addAttribute("cashier", Context.getAuthenticatedUser().getPersonName());
				
				HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
				List<PersonAttribute> pas = hcs.getPersonAttributes(listItemIssue.get(0).getStoreItemPatient().getPatient()
				        .getId());
				PersonAttributeType personAttributePCT = Context.getPersonService().getPersonAttributeTypeByUuid(
				    EhrCommonMetadata._EhrPersonAttributeType.PAYMENT_CATEGORY);
				
				for (PersonAttribute pa : pas) {
					PersonAttributeType attributeType = pa.getAttributeType();
					
					if (attributeType.getPersonAttributeTypeId().equals(personAttributePCT.getPersonAttributeTypeId())) {
						pageModel.addAttribute("paymentSubCategory", pa.getValue());
						break;
					}
				}
			}
		}
		return "redirect:" + ui.pageLink("ehrcashier", "billingQueue");
	}
}
