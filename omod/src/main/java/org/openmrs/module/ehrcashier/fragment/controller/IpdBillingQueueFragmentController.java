package org.openmrs.module.ehrcashier.fragment.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.BillingService;
import org.openmrs.module.hospitalcore.IpdService;
import org.openmrs.module.hospitalcore.model.IpdPatientAdmitted;
import org.openmrs.module.hospitalcore.util.PagingUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IpdBillingQueueFragmentController {
    /**
     * default handler for POST and GET methods
     */

    public void controller() {

    }

    public List<SimpleObject> getBillingQueue(@RequestParam(value = "date", required = false) String dateStr,
                                              @RequestParam(value = "searchKey", required = false) String searchKey,
                                              @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                              @RequestParam(value = "pgSize", required = false) Integer pgSize,
                                              @RequestParam(value = "admissionLogId", required = false) Integer admissionLogId,
                                              PageModel sharedPageModel, UiUtils ui) {
        BillingService billingService = Context.getService(BillingService.class);
        IpdService ipdService = (IpdService) Context.getService(IpdService.class);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<IpdPatientAdmitted> listIndoorPatient = ipdService.getAllIpdPatientAdmitted();
        if (currentPage == null) currentPage = 1;
        int total = ipdService.countGetAllIndoorPatientFromAdmissionLog(searchKey, currentPage);
        PagingUtil pagingUtil = new PagingUtil(pgSize, currentPage, total);

        List<SimpleObject> ipdPatientList = new ArrayList<SimpleObject>();
        for (IpdPatientAdmitted ipdPatientAdmitted: listIndoorPatient){
            SimpleObject ipdPatient = SimpleObject.create("id", ipdPatientAdmitted.getPatient().getId());
            ipdPatient.put("patientName", ipdPatientAdmitted.getPatientName());
            ipdPatient.put("patientIdentifier",ipdPatientAdmitted.getPatientIdentifier());
            ipdPatient.put("gender", ipdPatientAdmitted.getGender());
            ipdPatientList.add(ipdPatient);
        }

        return ipdPatientList;
    }
}
