package org.openmrs.module.ehrcashier.extension.html;

import org.openmrs.module.web.extension.LinkExt;

public class EhrcashierHeader extends LinkExt {
    @Override
    public String getLabel() {
        return "ehrcashier.title";
    }

    @Override
    public String getUrl() {
        return "ehrcashier/cashier.page";
    }

    @Override
    public String getRequiredPrivilege() {
        return "View Locations";
    }
    public MEDIA_TYPE getMediaType() {
        return MEDIA_TYPE.html;
    }
}
