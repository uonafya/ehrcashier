package org.openmrs.module.ehrcashier.metadata;

import org.openmrs.module.metadatadeploy.bundle.Requires;
import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.idSet;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.privilege;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.role;

@Component
@Requires(org.openmrs.module.kenyaemr.metadata.SecurityMetadata.class)
public class EhrCashierSecurityMetadata extends AbstractMetadataBundle {
	
	/**
	 * Application IDs
	 */
	
	public static final class _Privilege {
		
		public static final String CASHIER_MODULE_APP = "App: ehrcashier.billing";
	}
	
	public static final class _Role {
		
		public static final String CASHIER = "EHR Cashier";
		
	}
	
	/**
	 * @see AbstractMetadataBundle#install()
	 */
	@Override
	public void install() {
		install(privilege(_Privilege.CASHIER_MODULE_APP, "Able to access EHR Cashier module features"));
		
		install(role(_Role.CASHIER, "Can access Key EHR Cashier module App",
		    idSet(org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT),
		    idSet(_Privilege.CASHIER_MODULE_APP)));
	}
}
