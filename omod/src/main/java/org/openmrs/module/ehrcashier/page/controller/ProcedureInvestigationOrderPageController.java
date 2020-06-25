package org.openmrs.module.ehrcashier.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ehrcashier.billcalculator.BillCalculatorForBDService;
import org.openmrs.module.hospitalcore.BillingService;
import org.openmrs.module.hospitalcore.HospitalCoreService;
import org.openmrs.module.hospitalcore.PatientDashboardService;
import org.openmrs.module.hospitalcore.model.*;
import org.openmrs.module.hospitalcore.util.HospitalCoreUtils;
import org.openmrs.module.hospitalcore.util.Money;
import org.openmrs.module.hospitalcore.util.PatientUtils;
import org.openmrs.module.referenceapplication.ReferenceApplicationWebConstants;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProcedureInvestigationOrderPageController {

    public String get(PageModel model,
                      UiSessionContext sessionContext,
                      PageRequest pageRequest,
                      UiUtils ui,
                      @RequestParam("patientId") Integer patientId,
                      @RequestParam("encounterId") Integer encounterId,
                      @RequestParam(value = "date", required = false) String dateStr) {
        BillAccess ba=new BillAccess();
        boolean auth=ba.authenticate(pageRequest,sessionContext);
        if(!auth){
            return "redirect: index.htm";
        }
        BillingService billingService = Context.getService(BillingService.class);
        List<BillableService> serviceOrderList = billingService.listOfServiceOrder(patientId, encounterId);
        model.addAttribute("serviceOrderList", serviceOrderList);
        model.addAttribute("serviceOrderSize", serviceOrderList.size());
        model.addAttribute("patientId", patientId);
        model.addAttribute("encounterId", encounterId);
        HospitalCoreService hospitalCoreService = Context.getService(HospitalCoreService.class);
        PatientSearch patientSearch = hospitalCoreService.getPatientByPatientId(patientId);
        Patient patient = Context.getPatientService().getPatient(patientId);
        model.addAttribute("age", patient.getAge());
        model.addAttribute("category", patient.getAttribute(14));
        model.addAttribute("previousVisit",hospitalCoreService.getLastVisitTime(patient));

        if (patient.getAttribute(43) == null){
            model.addAttribute("fileNumber", "");
        }
        else if (StringUtils.isNotBlank(patient.getAttribute(43).getValue())){
            model.addAttribute("fileNumber", "(File: "+patient.getAttribute(43)+")");
        }
        else {
            model.addAttribute("fileNumber", "");
        }

        if (patient.getGender().equals("M")) {
            model.addAttribute("gender", "Male");
        }
        if (patient.getGender().equals("F")) {
            model.addAttribute("gender", "Female");
        }

        model.addAttribute("patientSearch", patientSearch);
        model.addAttribute("date", dateStr);
        return null;
    }

    public String post(PageModel model, Object command, HttpServletRequest request, @RequestParam("patientId") Integer patientId, @RequestParam("encounterId") Integer encounterId,
                       @RequestParam("indCount") Integer indCount, @RequestParam(value = "waiverAmount", required = false) BigDecimal waiverAmount,
                       @RequestParam(value = "waiverComment", required = false) String waiverComment, @RequestParam(value = "paymentMode", required = false) String paymentMode,
                       @RequestParam(value = "billType", required = false) String billType, UiUtils uiUtils, @RequestParam(value = "date", required = true) String date) {
        Map<String, Object> redirectParams = new HashMap<String, Object>();

        BillingService billingService = Context.getService(BillingService.class);

        PatientDashboardService patientDashboardService = Context.getService(PatientDashboardService.class);

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
        String servicename;
        int quantity = 0;
        String selectservice;
        BigDecimal unitPrice;
        String reschedule;
        String paybill;
        BillableService service;
        Money mUnitPrice;
        Money itemAmount;
        Money totalAmount = new Money(BigDecimal.ZERO);
        BigDecimal rate;
        String billTyp;
        BigDecimal totalActualAmount = new BigDecimal(0);
        OpdTestOrder opdTestOrder = new OpdTestOrder();
        HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
        List<PersonAttribute> pas = hcs.getPersonAttributes(patientId);
        String patientCategory = null;
        for (PersonAttribute pa : pas) {
            PersonAttributeType attributeType = pa.getAttributeType();
            PersonAttributeType personAttributePCT = hcs.getPersonAttributeTypeByName("Paying Category Type");
            PersonAttributeType personAttributeNPCT = hcs.getPersonAttributeTypeByName("Non-Paying Category Type");
            PersonAttributeType personAttributeSSCT = hcs.getPersonAttributeTypeByName("Special Scheme Category Type");
            if (attributeType.getPersonAttributeTypeId() == personAttributePCT.getPersonAttributeTypeId()) {
                patientCategory = pa.getValue();
            } else if (attributeType.getPersonAttributeTypeId() == personAttributeNPCT.getPersonAttributeTypeId()) {
                patientCategory = pa.getValue();
            } else if (attributeType.getPersonAttributeTypeId() == personAttributeSSCT.getPersonAttributeTypeId()) {
                patientCategory = pa.getValue();
            }else{
                //TO-DO temporarily set to general paying

            }
        }

        for (Integer i = 1; i <= indCount; i++) {
            selectservice = request.getParameter(i.toString() + "selectservice");
            if ("billed".equals(selectservice)) {
                servicename = request.getParameter(i.toString() + "service");
                quantity = NumberUtils.createInteger(request.getParameter(i.toString() + "servicequantity"));
                reschedule = request.getParameter(i.toString() + "reschedule");
                paybill = request.getParameter(i.toString() + "paybill");
                String parameter = request.getParameter(i.toString() + "unitprice");
                unitPrice = NumberUtils.createBigDecimal(parameter);
                //ConceptService conceptService = Context.getConceptService();
                //Concept con = conceptService.getConcept("servicename");
                service = billingService.getServiceByConceptName(servicename);

                mUnitPrice = new Money(unitPrice);
                itemAmount = mUnitPrice.times(quantity);
                totalAmount = totalAmount.plus(itemAmount);

                item = new PatientServiceBillItem();
                item.setCreatedDate(new Date());
                item.setName(servicename);
                item.setPatientServiceBill(bill);
                item.setQuantity(quantity);
                item.setService(service);
                item.setUnitPrice(unitPrice);

                item.setAmount(itemAmount.getAmount());


                // Get the ratio for each bill item
                Map<String, Object> parameters = HospitalCoreUtils.buildParameters(
                        "patient", patient, "attributes", attributes, "billItem",
                        item, "request", request);

                if ("pay".equals(paybill)) {
                    billTyp = "paid";
                } else {
                    billTyp = "free";

                }

                rate = calculator.getRate(parameters, billTyp);
                item.setActualAmount(item.getAmount().multiply(rate));
                totalActualAmount = totalActualAmount.add(item.getActualAmount());
                bill.addBillItem(item);

                opdTestOrder = billingService.getOpdTestOrder(encounterId, service.getConceptId());
                opdTestOrder.setBillingStatus(1);
                patientDashboardService.saveOrUpdateOpdOrder(opdTestOrder);

            } else {
                servicename = request.getParameter(i.toString() + "service");
                service = billingService.getServiceByConceptName(servicename);
                opdTestOrder = billingService.getOpdTestOrder(encounterId, service.getConceptId());
                opdTestOrder.setCancelStatus(1);
                patientDashboardService.saveOrUpdateOpdOrder(opdTestOrder);
            }
        }

        bill.setAmount(totalAmount.getAmount());
        bill.setActualAmount(totalActualAmount);
        /*added waiver amount */
        if (waiverAmount != null) {
            bill.setWaiverAmount(waiverAmount);
        } else {
            BigDecimal wavAmt = new BigDecimal(0);
            bill.setWaiverAmount(wavAmt);
        }
        bill.setComment(waiverComment);
        bill.setPaymentMode(paymentMode);
        if (patientCategory.equals("PRISONER") || patientCategory.equals("STUDENT SCHEME")) {
            bill.setPatientCategory("EXEMPTED PATIENT");
            bill.setComment("");
        }

        bill.setPatientSubCategory(patientCategory);

        PersonAttribute pCat = patient.getAttribute(45);

        if (pCat != null && pCat.getValue().equals("NHIF CIVIL SERVANT")) {
            bill.setPatientCategory("NHIF Patient");
        }

        bill.setFreeBill(2);
        bill.setReceipt(billingService.createReceipt());
        bill = billingService.savePatientServiceBill(bill);
        redirectParams.put("patientId", patientId);
        redirectParams.put("billId", bill.getPatientServiceBillId());
        redirectParams.put("billType", billType);
        return "redirect:" + uiUtils.pageLink("ehrcashier", "patientServiceBillForBD", redirectParams);
    }
}
