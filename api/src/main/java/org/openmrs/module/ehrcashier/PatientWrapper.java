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
import org.openmrs.Person;

import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PatientWrapper extends Patient implements Serializable {
    private Date lastVisitTime;
    private String wrapperIdentifier,formartedVisitDate;

    public PatientWrapper(Date lastVisitTime) {
        this.lastVisitTime = lastVisitTime;
    }

    public PatientWrapper(Person person, Date lastVisitTime) {
        super(person);
        this.lastVisitTime = lastVisitTime;
        this.wrapperIdentifier = ((Patient)person).getPatientIdentifier().getIdentifier();
    }

    public PatientWrapper(Integer patientId, Date lastVisitTime) {
        super(patientId);
        this.lastVisitTime = lastVisitTime;
    }

    public Date getLastVisitTime() {
        return lastVisitTime;
    }
    public String getFormartedVisitDate(){

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

}
