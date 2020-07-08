package org.openmrs.module.ehrcashier.page.controller;

public class BillAccess {
	
	public boolean authenticate(PageRequest pageRequest, UiSessionContext sessionContext) {
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
