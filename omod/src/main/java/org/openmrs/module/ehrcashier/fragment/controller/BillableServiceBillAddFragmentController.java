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
