package org.openmrs.module.ehrcashier.page.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.hospitalcore.model.*;
import org.openmrs.module.hospitalcore.util.*;
import org.openmrs.module.referenceapplication.ReferenceApplicationWebConstants;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


public class PatientServiceBillForBDPageController {

    public String get(PageModel model,
                      UiSessionContext sessionContext,
                      PageRequest pageRequest,
                      UiUtils ui,
                      @RequestParam("patientId") Integer patientId,
                      @RequestParam(value = "billId", required = false) Integer billId,
                      @RequestParam(value = "billType", required = false) String billType,
                      @RequestParam(value = "pageSize", required = false) Integer pageSize,
                      @RequestParam(value = "currentPage", required = false) Integer currentPage,
                      @RequestParam(value = "encounterId", required = false) Integer encounterId,
                      @RequestParam(value = "typeOfPatient", required = false) String typeOfPatient,
                      @RequestParam(value = "admissionLogId", required = false) Integer admissionLogId,
                      @RequestParam(value = "requestForDischargeStatus", required = false) Integer requestForDischargeStatus,
                      @RequestParam(value = "itemID", required = false) Integer itemID,
                      @RequestParam(value = "voidStatus", required = false) Boolean voidStatus,
                      @RequestParam(value = "selectedCategory", required = false) Integer selectedCategory,
                      HttpServletRequest request, UiUtils uiUtils) {
        pageRequest.getSession().setAttribute(ReferenceApplicationWebConstants.SESSION_ATTRIBUTE_REDIRECT_URL,ui.thisUrl());
        sessionContext.requireAuthentication();
        Boolean isPriviledged = Context.hasPrivilege("Access Billing");
        if(!isPriviledged){
            return "redirect: index.htm";
        }
        Map<String, Object> redirectParams = new HashMap<String, Object>();
        redirectParams.put("encounterId", encounterId);
        redirectParams.put("patientId", patientId);
        redirectParams.put("billId", billId);
        redirectParams.put("billType", billType);
        if (StringUtils.isNotBlank(typeOfPatient)) {
            model.addAttribute("requestForDischargeStatus", requestForDischargeStatus);
            return "redirect:" + uiUtils.pageLink("ehrcashier", "billListForIndoorPatient", redirectParams);

        } else {
            return "redirect:" + uiUtils.pageLink("ehrcashier", "billableServiceBillListForBD", redirectParams);
        }

    }



}
