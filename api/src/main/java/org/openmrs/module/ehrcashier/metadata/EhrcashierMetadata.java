package org.openmrs.module.ehrcashier.metadata;

import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.openmrs.module.metadatadeploy.bundle.Requires;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.idSet;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.privilege;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.role;

/**
 * Implementation of access control to the app.
 */
@Component
@Requires(org.openmrs.module.kenyaemr.metadata.SecurityMetadata.class)
public class EhrcashierMetadata extends AbstractMetadataBundle {
	
	public static class _Privilege {
		
		public static final String APP_EC_MODULE_APP = "App: ehrcashier.cashier";
	}
	
	public static final class _Role {
		
		public static final String APPLICATION_EC_MODULE = "Ehrcashier Module";
	}
	
	/**
	 * @see AbstractMetadataBundle#install()
	 */
	@Override
	public void install() {
		
		install(privilege(_Privilege.APP_EC_MODULE_APP, "Able to access ehrcashier module features"));
		install(role(_Role.APPLICATION_EC_MODULE, "Can access ehrcashier module App",
		    idSet(org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT),
		    idSet(_Privilege.APP_EC_MODULE_APP)));
	}
}
