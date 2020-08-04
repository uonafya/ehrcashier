package org.openmrs.module.ehrcashier.page.controller;

import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BillingQueuePageController {
	
	public String get(PageModel pageModel, UiSessionContext sessionContext, PageRequest pageRequest, UiUtils ui) {
		BillAccess ba = new BillAccess();
		boolean auth = ba.authenticate(pageRequest, sessionContext, ui);
		if (!auth) {
			return "redirect: index.htm";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String dateStr = sdf.format(new Date());
		pageModel.addAttribute("currentDate", dateStr);
		pageModel.addAttribute("currentTime", new Date());
		return null;
	}
	
}
