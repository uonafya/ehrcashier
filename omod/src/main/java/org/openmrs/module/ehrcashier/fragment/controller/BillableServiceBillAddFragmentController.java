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
import org.openmrs.module.hospitalcore.model.BillableService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class BillableServiceBillAddFragmentController {

    /**
     * Handles the get and post requests by default
     */
    public void controller(){
    }

    public List<SimpleObject> loadBillableServices(@RequestParam(value = "name") String name,
                                                   UiUtils uiUtils){
        BillingService billingService = Context.getService(BillingService.class);
        List<BillableService> services = billingService.searchService(name);
        return SimpleObject.fromCollection(services, uiUtils, "conceptId", "name", "shortName", "price");
    }
}
