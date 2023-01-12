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
		List<Role> role = new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles());
		
		InventoryStoreRoleRelation srl = null;
		Role rl = null;
		for (Role r : role) {
			if (inventoryService.getStoreRoleByName(r.toString()) != null) {
				srl = inventoryService.getStoreRoleByName(r.toString());
				rl = r;
			}
		}
		InventoryStore store = null;
		if (srl != null) {
			store = inventoryService.getStoreById(srl.getStoreid());
			
		}
		List<InventoryStoreDrugPatientDetail> listDrugIssue = inventoryService.listStoreDrugPatientDetail(orderId);
		if (listDrugIssue != null && listDrugIssue.size() > 0) {
			InventoryStoreDrugTransaction transaction = new InventoryStoreDrugTransaction();
			transaction.setDescription("ISSUE DRUG TO PATIENT " + DateUtils.getDDMMYYYY());
			transaction.setStore(store);
			transaction.setTypeTransaction(ActionValue.TRANSACTION[1]);
			transaction.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
			
			transaction = inventoryService.saveStoreDrugTransaction(transaction);
			for (InventoryStoreDrugPatientDetail pDetail : listDrugIssue) {
				Date date1 = new Date();
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Integer totalQuantity = inventoryService.sumCurrentQuantityDrugOfStore(store.getId(), pDetail
				        .getTransactionDetail().getDrug().getId(), pDetail.getTransactionDetail().getFormulation().getId());
				int t = totalQuantity;
				
				Integer receipt = pDetail.getStoreDrugPatient().getId();
				model.addAttribute("receiptid", receipt);
				
				InventoryStoreDrugTransactionDetail inventoryStoreDrugTransactionDetail = inventoryService
				        .getStoreDrugTransactionDetailById(pDetail.getTransactionDetail().getParent().getId());
				
				InventoryStoreDrugTransactionDetail drugTransactionDetail = inventoryService
				        .getStoreDrugTransactionDetailById(inventoryStoreDrugTransactionDetail.getId());
				
				inventoryStoreDrugTransactionDetail.setCurrentQuantity(drugTransactionDetail.getCurrentQuantity());
				Integer flags = pDetail.getTransactionDetail().getFlag();
				
				if (flags == null) {
					model.addAttribute("flag", FlagStates.NOT_PROCESSED);
				} else if (flags >= 1) {
					model.addAttribute("flag", FlagStates.PARTIALLY_PROCESSED);
				} else {
					model.addAttribute("flag", flags);
				}
				
				inventoryService.saveStoreDrugTransactionDetail(inventoryStoreDrugTransactionDetail);
				// save transactiondetail first
				InventoryStoreDrugTransactionDetail transDetail = new InventoryStoreDrugTransactionDetail();
				transDetail.setTransaction(transaction);
				transDetail.setCurrentQuantity(0);
				transDetail.setIssueQuantity(pDetail.getQuantity());
				transDetail.setOpeningBalance(totalQuantity);
				transDetail.setClosingBalance(t);
				transDetail.setQuantity(0);
				transDetail.setVAT(pDetail.getTransactionDetail().getVAT());
				transDetail.setCostToPatient(pDetail.getTransactionDetail().getCostToPatient());
				transDetail.setUnitPrice(pDetail.getTransactionDetail().getUnitPrice());
				transDetail.setDrug(pDetail.getTransactionDetail().getDrug());
				transDetail.setFormulation(pDetail.getTransactionDetail().getFormulation());
				transDetail.setBatchNo(pDetail.getTransactionDetail().getBatchNo());
				transDetail.setCompanyName(pDetail.getTransactionDetail().getCompanyName());
				transDetail.setDateManufacture(pDetail.getTransactionDetail().getDateManufacture());
				transDetail.setDateExpiry(pDetail.getTransactionDetail().getDateExpiry());
				transDetail.setReceiptDate(pDetail.getTransactionDetail().getReceiptDate());
				transDetail.setCreatedOn(date1);
				transDetail.setReorderPoint(pDetail.getTransactionDetail().getDrug().getReorderQty());
				transDetail.setAttribute(pDetail.getTransactionDetail().getDrug().getAttributeName());
				transDetail.setFrequency(pDetail.getTransactionDetail().getFrequency());
				transDetail.setNoOfDays(pDetail.getTransactionDetail().getNoOfDays());
				transDetail.setComments(pDetail.getTransactionDetail().getComments());
				transDetail.setFlag(FlagStates.PARTIALLY_PROCESSED);
				BigDecimal moneyUnitPrice = pDetail.getTransactionDetail().getCostToPatient()
				        .multiply(new BigDecimal(pDetail.getQuantity()));
				transDetail.setTotalPrice(moneyUnitPrice);
				transDetail.setParent(pDetail.getTransactionDetail());
				transDetail = inventoryService.saveStoreDrugTransactionDetail(transDetail);
				
			}
			
		}
		if (listDrugIssue.size() > 0) {
			model.addAttribute("waiverAmount", listDrugIssue.get(0).getStoreDrugPatient().getWaiverAmount());
			model.addAttribute("waiverComment", listDrugIssue.get(0).getStoreDrugPatient().getComment());
		}
		
		List<SimpleObject> dispensedDrugs = SimpleObject.fromCollection(listDrugIssue, ui, "quantity",
		    "transactionDetail.costToPatient", "transactionDetail.drug.name", "transactionDetail.formulation.name",
		    "transactionDetail.formulation.dozage", "transactionDetail.frequency.name", "transactionDetail.noOfDays",
		    "transactionDetail.comments", "transactionDetail.dateExpiry");
		model.addAttribute("listDrugIssue", SimpleObject.create("listDrugIssue", dispensedDrugs).toJson());
		if (CollectionUtils.isNotEmpty(listDrugIssue)) {
			model.addAttribute("issueDrugPatient", listDrugIssue.get(0).getStoreDrugPatient());
			model.addAttribute("date", listDrugIssue.get(0).getStoreDrugPatient().getCreatedOn());
			
			int age = listDrugIssue.get(0).getStoreDrugPatient().getPatient().getAge();
			if (age < 1) {
				model.addAttribute("age", "<1");
			} else {
				model.addAttribute("age", age);
			}
			//TODO starts here
			
			PatientIdentifier pi = listDrugIssue.get(0).getStoreDrugPatient().getPatient().getPatientIdentifier();
			int patientId = pi.getPatient().getPatientId();
			Date issueDate = listDrugIssue.get(0).getStoreDrugPatient().getCreatedOn();
			Encounter encounterId = listDrugIssue.get(0).getTransactionDetail().getEncounter();
			List<OpdDrugOrder> listOfNotDispensedOrder = new ArrayList<OpdDrugOrder>();
			if (encounterId != null) {
				listOfNotDispensedOrder = inventoryService.listOfNotDispensedOrder(patientId, issueDate, encounterId);
			}
			
			List<SimpleObject> notDispensed = SimpleObject
			        .fromCollection(listOfNotDispensedOrder, ui, "inventoryDrug.name", "inventoryDrugFormulation.name",
			            "inventoryDrugFormulation.dozage", "frequency.name", "noOfDays", "comments");
			model.addAttribute("listOfNotDispensedOrder", SimpleObject.create("listOfNotDispensedOrder", notDispensed)
			        .toJson());
			//TODO ends here
			pageModel.addAttribute("birthdate", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getBirthdate());
			
			model.addAttribute("identifier", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getPatientIdentifier());
			model.addAttribute("givenName", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getGivenName());
			model.addAttribute("familyName", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getFamilyName());
			if (listDrugIssue.get(0).getStoreDrugPatient().getPatient().getMiddleName() != null) {
				model.addAttribute("middleName", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getMiddleName());
			} else {
				model.addAttribute("middleName", " ");
			}
			if (listDrugIssue.get(0).getStoreDrugPatient().getPatient().getGender().equals("M")) {
				model.addAttribute("gender", "Male");
			}
			if (listDrugIssue.get(0).getStoreDrugPatient().getPatient().getGender().equals("F")) {
				model.addAttribute("gender", "Female");
			}
			
			HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
			List<PersonAttribute> pas = hcs.getPersonAttributes(listDrugIssue.get(0).getStoreDrugPatient().getPatient()
			        .getId());
			
			PersonName prescriber = null;
			
			if (listDrugIssue.get(0).getStoreDrugPatient().getPrescriber() != null) {
				prescriber = listDrugIssue.get(0).getStoreDrugPatient().getPrescriber().getPersonName();
			}
			
			model.addAttribute("pharmacist", listDrugIssue.get(0).getStoreDrugPatient().getCreatedBy());
			model.addAttribute("cashier", Context.getAuthenticatedUser().getPersonName());
			model.addAttribute("prescriber", prescriber);
			model.addAttribute("lastVisit", hcs.getLastVisitTime(listDrugIssue.get(0).getStoreDrugPatient().getPatient()));
			PersonAttributeType patientCategoryAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(
			    "09cd268a-f0f5-11ea-99a8-b3467ddbf779");
			PersonAttributeType payingCategoryAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(
			    "972a32aa-6159-11eb-bc2d-9785fed39154");
			
			int paymentCategory = 0;
			if (pi.getPatient().getAttribute(patientCategoryAttributeType).getValue().equals("Paying")) {
				paymentCategory = 1;
			} else if (pi.getPatient().getAttribute(patientCategoryAttributeType).getValue().equals("Non paying")) {
				paymentCategory = 2;
			} else if (pi.getPatient().getAttribute(patientCategoryAttributeType).getValue().equals("Special scheme")) {
				paymentCategory = 3;
			}
			
			model.addAttribute("paymentSubCategory", pi.getPatient().getAttribute(payingCategoryAttributeType).getValue());
			model.addAttribute("paymentCategory", paymentCategory);
			model.addAttribute("paymentCategoryName", pi.getPatient().getAttribute(patientCategoryAttributeType).getValue());
		}
		return null;
	}
	
	public String post(HttpServletRequest request, PageModel pageModel, UiUtils ui) {
		pageModel.addAttribute("userLocation", Context.getAdministrationService()
		        .getGlobalProperty("hospital.location_user"));
		String drugOrder = request.getParameter("drugOrder");
		JSONObject jsonObject = new JSONObject(drugOrder);
		
		String comment = jsonObject.getString("comment");
		String waiverAmountString = jsonObject.getString("waiverAmount");
		
		BigDecimal waiverAmount = null;
		if (StringUtils.isNotEmpty(waiverAmountString)) {
			waiverAmount = new BigDecimal(waiverAmountString);
		}
		
		int receiptid = Integer.parseInt(request.getParameter("receiptid"));
		pageModel.addAttribute("receiptid", receiptid);
		InventoryService inventoryService = (InventoryService) Context.getService(InventoryService.class);
		//InventoryStore store =  inventoryService.getStoreByCollectionRole(new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles()));
		List<Role> role = new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles());
		InventoryStoreRoleRelation srl = null;
		Role rl = null;
		for (Role r : role) {
			if (inventoryService.getStoreRoleByName(r.toString()) != null) {
				srl = inventoryService.getStoreRoleByName(r.toString());
				rl = r;
			}
		}
		InventoryStore store = null;
		if (srl != null) {
			store = inventoryService.getStoreById(srl.getStoreid());
			
		}
		List<InventoryStoreDrugPatientDetail> listDrugIssue = inventoryService.listStoreDrugPatientDetail(receiptid);
		InventoryStoreDrugPatient inventoryStoreDrugPatient = null;
		
		if (listDrugIssue != null && listDrugIssue.size() > 0) {
			InventoryStoreDrugTransaction transaction = new InventoryStoreDrugTransaction();
			transaction.setDescription("ISSUE DRUG TO PATIENT " + DateUtils.getDDMMYYYY());
			transaction.setStore(store);
			transaction.setTypeTransaction(ActionValue.TRANSACTION[1]);
			transaction.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
			transaction = inventoryService.saveStoreDrugTransaction(transaction);
			for (InventoryStoreDrugPatientDetail pDetail : listDrugIssue) {
				Date date1 = new Date();
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Integer totalQuantity = inventoryService.sumCurrentQuantityDrugOfStore(store.getId(), pDetail
				        .getTransactionDetail().getDrug().getId(), pDetail.getTransactionDetail().getFormulation().getId());
				int t = totalQuantity - pDetail.getQuantity();
				
				InventoryStoreDrugTransactionDetail inventoryStoreDrugTransactionDetail = inventoryService
				        .getStoreDrugTransactionDetailById(pDetail.getTransactionDetail().getParent().getId());
				InventoryStoreDrugTransactionDetail drugTransactionDetail = inventoryService
				        .getStoreDrugTransactionDetailById(inventoryStoreDrugTransactionDetail.getId());
				inventoryStoreDrugTransactionDetail.setCurrentQuantity(drugTransactionDetail.getCurrentQuantity()
				        - pDetail.getQuantity());
				inventoryService.saveStoreDrugTransactionDetail(inventoryStoreDrugTransactionDetail);
				
				// save transactiondetail first
				InventoryStoreDrugTransactionDetail transDetail = new InventoryStoreDrugTransactionDetail();
				transDetail.setTransaction(transaction);
				transDetail.setCurrentQuantity(0);
				
				transDetail.setIssueQuantity(pDetail.getQuantity());
				transDetail.setOpeningBalance(totalQuantity);
				transDetail.setClosingBalance(t);
				transDetail.setQuantity(0);
				transDetail.setVAT(pDetail.getTransactionDetail().getVAT());
				transDetail.setCostToPatient(pDetail.getTransactionDetail().getCostToPatient());
				transDetail.setUnitPrice(pDetail.getTransactionDetail().getUnitPrice());
				transDetail.setDrug(pDetail.getTransactionDetail().getDrug());
				transDetail.setFormulation(pDetail.getTransactionDetail().getFormulation());
				transDetail.setBatchNo(pDetail.getTransactionDetail().getBatchNo());
				transDetail.setCompanyName(pDetail.getTransactionDetail().getCompanyName());
				transDetail.setDateManufacture(pDetail.getTransactionDetail().getDateManufacture());
				transDetail.setDateExpiry(pDetail.getTransactionDetail().getDateExpiry());
				transDetail.setReceiptDate(pDetail.getTransactionDetail().getReceiptDate());
				transDetail.setCreatedOn(date1);
				transDetail.setReorderPoint(pDetail.getTransactionDetail().getDrug().getReorderQty());
				transDetail.setAttribute(pDetail.getTransactionDetail().getDrug().getAttributeName());
				transDetail.setFrequency(pDetail.getTransactionDetail().getFrequency());
				transDetail.setNoOfDays(pDetail.getTransactionDetail().getNoOfDays());
				transDetail.setComments(pDetail.getTransactionDetail().getComments());
				transDetail.setFlag(1);
				
				BigDecimal moneyUnitPrice = pDetail.getTransactionDetail().getCostToPatient()
				        .multiply(new BigDecimal(pDetail.getQuantity()));
				
				transDetail.setTotalPrice(moneyUnitPrice);
				transDetail.setParent(pDetail.getTransactionDetail());
				transDetail = inventoryService.saveStoreDrugTransactionDetail(transDetail);
				pDetail.setQuantity(pDetail.getQuantity());
				
				pDetail.setTransactionDetail(transDetail);
				
				// save issue to patient detail
				inventoryService.saveStoreDrugPatientDetail(pDetail);
				inventoryStoreDrugPatient = inventoryService.getStoreDrugPatientById(pDetail.getStoreDrugPatient().getId());
				if (transDetail.getFlag() == FlagStates.PARTIALLY_PROCESSED) {
					
					inventoryStoreDrugPatient.setStatuss(1);
					
				}
				Integer flags = pDetail.getTransactionDetail().getFlag();
				pageModel.addAttribute("flag", flags);
			}
			// update patient detail
			inventoryStoreDrugPatient.setWaiverAmount(waiverAmount);
			inventoryStoreDrugPatient.setComment(comment);
			inventoryService.saveStoreDrugPatient(inventoryStoreDrugPatient);
			
			List<SimpleObject> dispensedDrugs = SimpleObject.fromCollection(listDrugIssue, ui, "quantity",
			    "transactionDetail.costToPatient", "transactionDetail.drug.name", "transactionDetail.formulation.name",
			    "transactionDetail.formulation.dozage", "transactionDetail.frequency.name", "transactionDetail.noOfDays",
			    "transactionDetail.comments", "transactionDetail.dateExpiry");
			pageModel.addAttribute("listDrugIssue", SimpleObject.create("listDrugIssue", dispensedDrugs).toJson());
			if (CollectionUtils.isNotEmpty(listDrugIssue)) {
				pageModel.addAttribute("issueDrugPatient", listDrugIssue.get(0).getStoreDrugPatient());
				
				pageModel.addAttribute("date", listDrugIssue.get(0).getStoreDrugPatient().getCreatedOn());
				pageModel.addAttribute("age", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getAge());
				
				//TODO starts here
				
				PatientIdentifier pi = listDrugIssue.get(0).getStoreDrugPatient().getPatient().getPatientIdentifier();
				
				int patientId = pi.getPatient().getPatientId();
				Date issueDate = listDrugIssue.get(0).getStoreDrugPatient().getCreatedOn();
				Encounter encounterId = listDrugIssue.get(0).getTransactionDetail().getEncounter();
				
				List<OpdDrugOrder> listOfNotDispensedOrder = new ArrayList<OpdDrugOrder>();
				if (encounterId != null) {
					listOfNotDispensedOrder = inventoryService.listOfNotDispensedOrder(patientId, issueDate, encounterId);
				}
				
				List<SimpleObject> notDispensed = SimpleObject.fromCollection(listOfNotDispensedOrder, ui,
				    "inventoryDrug.name", "inventoryDrugFormulation.name", "inventoryDrugFormulation.dozage",
				    "frequency.name", "noOfDays", "comments");
				pageModel.addAttribute("listOfNotDispensedOrder",
				    SimpleObject.create("listOfNotDispensedOrder", notDispensed).toJson());
				
				//TODO ends here
				
				pageModel.addAttribute("identifier", listDrugIssue.get(0).getStoreDrugPatient().getPatient()
				        .getPatientIdentifier());
				pageModel.addAttribute("givenName", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getGivenName());
				pageModel
				        .addAttribute("familyName", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getFamilyName());
				
				if (listDrugIssue.get(0).getStoreDrugPatient().getPatient().getMiddleName() != null) {
					pageModel.addAttribute("middleName", listDrugIssue.get(0).getStoreDrugPatient().getPatient()
					        .getMiddleName());
				} else {
					pageModel.addAttribute("middleName", "");
				}
				
				if (listDrugIssue.get(0).getStoreDrugPatient().getPatient().getGender().equals("M")) {
					pageModel.addAttribute("gender", "Male");
				} else {
					pageModel.addAttribute("gender", "Female");
				}
				pageModel.addAttribute("pharmacist", listDrugIssue.get(0).getStoreDrugPatient().getCreatedBy());
				pageModel.addAttribute("cashier", Context.getAuthenticatedUser().getPersonName());
				
				HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
				List<PersonAttribute> pas = hcs.getPersonAttributes(listDrugIssue.get(0).getStoreDrugPatient().getPatient()
				        .getId());
				System.out.println("The attributes found are :::" + pas);
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
