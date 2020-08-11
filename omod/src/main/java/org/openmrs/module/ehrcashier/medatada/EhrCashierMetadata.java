package org.openmrs.module.ehrcashier.medatada;

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
public class EhrCashierMetadata extends AbstractMetadataBundle {
	
	public static class _Privilege {
		
		public static final String APP_EHRCASHIER_MODULE_APP = "App: ehrcashier.billing";
	}
	
	public static final class _Role {
		
		public static final String APPLICATION_EHRCASHIER_MODULE = "Revenue Collection";
	}
	
	/**
	 * @see AbstractMetadataBundle#install()
	 */
	@Override
	public void install() {
		install(privilege(_Privilege.APP_EHRCASHIER_MODULE_APP, "Able to access Key revenue collection module"));
		install(role(_Role.APPLICATION_EHRCASHIER_MODULE, "Can access ehrcashier module App",
		    idSet(org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT),
		    idSet(_Privilege.APP_EHRCASHIER_MODULE_APP)));
	}
}
