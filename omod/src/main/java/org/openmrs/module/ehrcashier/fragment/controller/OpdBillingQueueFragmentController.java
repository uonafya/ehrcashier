package org.openmrs.module.ehrcashier.fragment.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.BillingService;
import org.openmrs.module.hospitalcore.model.PatientSearch;
import org.openmrs.module.hospitalcore.util.PagingUtil;
import org.openmrs.ui.framework.SimpleObject;

import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OpdBillingQueueFragmentController {

     /* public SimpleObject getBillingQueue(
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "searchKey", required = false) String searchKey,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            // 21/11/2014 to work with size selector
            @RequestParam(value = "pgSize", required = false) Integer pgSize,
            PageModel sharedPageModel, UiUtils ui) {
        BillingService billingService = Context.getService(BillingService.class);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 21/11/2014 to work with size selector for OPDQueue
        List<PatientSearch> patientSearchResult = billingService.searchListOfPatient(date, searchKey, currentPage, pgSize);

        List<SimpleObject> getpatient = null;
        if (patientSearchResult != null) {

            getpatient = SimpleObject.fromCollection(getpatient, ui, "date", "searchKey", "page", "pgSize", "id", "name");
        }

        if (currentPage == null) currentPage = 1;
        int total = billingService.countSearchListOfPatient(date, searchKey, currentPage);
        PagingUtil pagingUtil = new PagingUtil(pgSize, currentPage, total);
        sharedPageModel.addAttribute("pagingUtil", pagingUtil);
        sharedPageModel.addAttribute("patientList", patientSearchResult);
        sharedPageModel.addAttribute("date", dateStr);
        return SimpleObject.create(getpatient);
    }*/

    public List<SimpleObject> getBillingQueue(@RequestParam(value = "date", required = false) String dateStr,
                                              @RequestParam(value = "searchKey", required = false) String searchKey,
                                              @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                              // 21/11/2014 to work with size selector
                                              @RequestParam(value = "pgSize", required = false) Integer pgSize,
                                              PageModel sharedPageModel, UiUtils ui) {
        BillingService billingService = Context.getService(BillingService.class);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 21/11/2014 to work with size selector for OPDQueue
        List<PatientSearch> patientSearchResult = billingService.searchListOfPatient(date, searchKey, currentPage, pgSize);
        if (currentPage == null) currentPage = 1;
        int total = billingService.countSearchListOfPatient(date, searchKey, currentPage);
        PagingUtil pagingUtil = new PagingUtil(pgSize, currentPage, total);
//        sharedPageModel.addAttribute("pagingUtil", pagingUtil);
//        sharedPageModel.addAttribute("patientList", patientSearchResult);
//        sharedPageModel.addAttribute("date", dateStr);

        // 21/11/2014 to get the cashier processing column
//                User authenticatedUser=Context.getAuthenticatedUser();
//                sharedPageModel.addAttribute("user", authenticatedUser);

        return SimpleObject.fromCollection(patientSearchResult, ui, "fullname", "identifier", "age", "gender","patientId");
    }
}
