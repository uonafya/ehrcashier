package org.openmrs.module.ehrcashier.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class MainPageController {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/*@RequestMapping(method= RequestMethod.GET)*/
	public String get(PageModel model, UiSessionContext sessionContext, PageRequest pageRequest, UiUtils ui) {
		BillAccess ba = new BillAccess();
		boolean auth = ba.authenticate(pageRequest, sessionContext, ui);
		if (!auth) {
			return "redirect: index.htm";
		}
		String prefix = Context.getAdministrationService().getGlobalProperty("registration.identifier_prefix");
		model.addAttribute("idPrefix", prefix);
		return null;
	}
	
	/*@RequestMapping(method=RequestMethod.POST)*/
	public void submit(PageModel model, @RequestParam("identifier") String identifier) {
		
		String prefix = Context.getAdministrationService().getGlobalProperty("registration.identifier_prefix");
		if (identifier.contains("-") && !identifier.contains(prefix)) {
			identifier = prefix + identifier;
		}
		List<Patient> patientsList = Context.getPatientService().getPatients(identifier.trim());
		model.addAttribute("patients", patientsList);
		
	}
	
}
