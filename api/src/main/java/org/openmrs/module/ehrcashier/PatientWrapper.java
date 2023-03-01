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

package org.openmrs.module.ehrcashier;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;

import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PatientWrapper extends Patient implements Serializable {
	
	private Date lastVisitTime;
	
	private String wrapperIdentifier, formartedVisitDate;
	
	public PatientWrapper(Date lastVisitTime) {
		this.lastVisitTime = lastVisitTime;
	}
	
	public PatientWrapper(Patient person, Date lastVisitTime) {
		super(person);
		this.lastVisitTime = lastVisitTime;
		this.wrapperIdentifier = patientIdentifierValue(person);
	}
	
	public PatientWrapper(Integer patientId, Date lastVisitTime) {
		super(patientId);
		this.lastVisitTime = lastVisitTime;
	}
	
	public Date getLastVisitTime() {
		return lastVisitTime;
	}
	
	public String getFormartedVisitDate() {
		
		Format formatter = new SimpleDateFormat("dd/MM/yyyy");
		formartedVisitDate = formatter.format(lastVisitTime);
		return formartedVisitDate;
	}
	
	public void setLastVisitTime(Date lastVisitTime) {
		this.lastVisitTime = lastVisitTime;
	}
	
	public String getWrapperIdentifier() {
		return wrapperIdentifier;
	}
	
	public void setWrapperIdentifier(String wrapperIdentifier) {
		this.wrapperIdentifier = wrapperIdentifier;
	}
	
	public void setFormartedVisitDate(String formartedVisitDate) {
		this.formartedVisitDate = formartedVisitDate;
	}
	
	private String patientIdentifierValue(Patient patient) {
		String identifier = "";
		String clinicalNumberUuid = "b4d66522-11fc-45c7-83e3-39a1af21ae0d";
		Set<PatientIdentifier> patientIdentifierList = new HashSet<PatientIdentifier>(patient.getIdentifiers());
		
		for (PatientIdentifier patientIdentifier : patientIdentifierList) {
			if (patientIdentifier.getIdentifierType().equals(
			    Context.getPatientService().getPatientIdentifierTypeByUuid(clinicalNumberUuid))) {
				identifier = patientIdentifier.getIdentifier();
				break;
			}
		}
		return identifier;
	}
}
