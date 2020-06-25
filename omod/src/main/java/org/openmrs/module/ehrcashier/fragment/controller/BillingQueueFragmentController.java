/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.ehrcashier.fragment.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.BillingService;
import org.openmrs.module.hospitalcore.model.PatientSearch;
import org.openmrs.module.hospitalcore.util.PagingUtil;
import org.openmrs.ui.framework.SimpleObject;

import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class BillingQueueFragmentController {

    public SimpleObject getBillingQueue(
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "searchKey", required = false) String searchKey,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            //to work with current selector
            @RequestParam(value = "pgSize", required = false) Integer pgSize,
            PageModel model, UiUtils ui) {
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
        model.addAttribute("pagingUtil", pagingUtil);
        model.addAttribute("patientList", patientSearchResult);
        model.addAttribute("date", dateStr);


        return SimpleObject.create(getpatient);
    }
}
