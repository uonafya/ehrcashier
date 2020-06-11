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
