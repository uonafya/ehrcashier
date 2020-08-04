package org.openmrs.module.ehrcashier.page.controller;

import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.page.PageRequest;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.UiUtils;

public class BillAccess {
	
	public boolean authenticate(PageRequest pageRequest, UiSessionContext sessionContext, UiUtils ui) {
		pageRequest.getSession().setAttribute(ReferenceApplicationWebConstants.SESSION_ATTRIBUTE_REDIRECT_URL, ui.thisUrl());
		sessionContext.requireAuthentication();
		Boolean isPriviledged = Context.hasPrivilege("Access Billing");
		if (!isPriviledged) {
			return false;
		} else {
			return true;
		}
	}
}
