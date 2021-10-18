package org.openmrs.module.ehrcashier.metadata;

import org.openmrs.module.hospitalcore.BillingConstants;
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
		
		public static final String BILL_WAIVER = "Can waiver bills for patients";
		
		public static final String PRIV_EDIT_BILL_ONCE_PRINTED = "Can edit bill once paid";
		
	}
	
	public static final class _Role {
		
		public static final String CASHIER = "EHR Cashier";
		
		public static final String CAN_WAVE = "EHR Wave bills";
		
		public static final String CAN_EDIT_BILL = "EHR Edit Bill";
		
	}
	
	/**
	 * @see AbstractMetadataBundle#install()
	 */
	@Override
	public void install() {
		install(privilege(_Privilege.CASHIER_MODULE_APP, "Able to access EHR Cashier module features"));
		install(privilege(_Privilege.BILL_WAIVER, "Able to waive patient bills"));
		install(privilege(_Privilege.PRIV_EDIT_BILL_ONCE_PRINTED, "Able to edit Bill once printed"));
		
		install(role(_Role.CASHIER, "Able access Key EHR Cashier module App",
		    idSet(org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT),
		    idSet(_Privilege.CASHIER_MODULE_APP)));
		
		install(role(_Role.CAN_WAVE, "Able to wave patient bills",
		    idSet(org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT),
		    idSet(_Privilege.BILL_WAIVER)));
		
		install(role(_Role.CAN_EDIT_BILL, "Able to edit patient bills",
		    idSet(org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT),
		    idSet(BillingConstants.PRIV_EDIT_BILL_ONCE_PRINTED)));
		
	}
}
