<%
    ui.decorateWith("appui", "standardEmrPage", [title: "Cashier Module"])
    ui.includeCss("uicommons", "styleguide/index.css")
    ui.includeCss("ehrconfigs", "jquery.dataTables.min.css")
    ui.includeCss("ehrconfigs", "onepcssgrid.css")
    ui.includeJavascript("ehrconfigs", "moment.js")
    ui.includeJavascript("ehrconfigs", "jquery.dataTables.min.js")
    ui.includeJavascript("ehrconfigs", "jq.browser.select.js")
    ui.includeJavascript("ehrconfigs", "knockout-3.4.0.js")
    ui.includeJavascript("ehrconfigs", "jquery-1.12.4.min.js")
    ui.includeJavascript("ehrconfigs", "jquery-ui-1.9.2.custom.min.js")
    ui.includeJavascript("ehrconfigs", "underscore-min.js")
    ui.includeJavascript("ehrconfigs", "emr.js")
    ui.includeCss("ehrconfigs", "jquery-ui-1.9.2.custom.min.css")
    // toastmessage plugin: https://github.com/akquinet/jquery-toastmessage-plugin/wiki
    ui.includeJavascript("ehrconfigs", "jquery.toastmessage.js")
    ui.includeCss("ehrconfigs", "jquery.toastmessage.css")
    // simplemodal plugin: http://www.ericmmartin.com/projects/simplemodal/
    ui.includeJavascript("ehrconfigs", "jquery.simplemodal.1.4.4.min.js")
    ui.includeCss("ehrconfigs", "referenceapplication.css")
    def props = ["sno", "service", "select", "quantity", "pay", "unitprice", "itemtotal"]
%>
<style>
	.form-textbox {
		height: 12px !important;
		font-size: 12px !important;
	}
	.name {
		color: #f26522;
	}
</style>
<script type="text/javascript">
    jQuery(document).ready(function () {

    });

    ///end of ready


</script>

<script type="text/javascript">
    function updatePrice(incon) {
        var con = incon.toString();
        var serqunid = con.concat("servicequantity");
        var serpriid = con.concat("serviceprice");
        var unipriid = con.concat("unitprice");
//alert(document.getElementById(serqunid).value);
        serqun = jQuery("#" + serqunid).val();
        unpri = jQuery("#" + unipriid).val();
        jQuery("#" + serpriid).val(serqun * unpri);
    }
</script>

<script type="text/javascript">
    function disable(incon) {
        var icon = incon.toString();

        if (jQuery("#" + icon + "selectservice").attr('checked')) {
            jQuery("#" + icon + "servicequantity").removeAttr("disabled");
            jQuery("#" + icon + "serviceprice").removeAttr("disabled");
            jQuery("#" + icon + "paybill").removeAttr("disabled");

            var totalValue = jQuery("#total").val();
            var toBeAdded = jQuery("#" + icon + "serviceprice").val();
            var added = parseInt(totalValue, 10) + parseInt(toBeAdded, 10);
            jQuery('#total').val(added);
        }
        else {
            jQuery("#" + icon + "servicequantity").attr("disabled", "disabled");
            jQuery("#" + icon + "paybill").attr("disabled", "disabled");
            jQuery("#" + icon + "serviceprice").attr("disabled", "disabled");
            var totalValue = jQuery("#total").val();
            var toBeMinus = jQuery("#" + icon + "serviceprice").val();
            var left = totalValue - toBeMinus;
            jQuery('#total').val(left);
        }

    }


    function payCheckBox(incon) {

        var icon = incon.toString();
        if (jQuery("#" + icon + "paybill").attr('checked')) {
            jQuery("#" + icon + "serviceprice").removeAttr("disabled");
            var totalValue = jQuery("#total").val();
            var toBeAdded = jQuery("#" + icon + "serviceprice").val();
            var added = parseInt(totalValue, 10) + parseInt(toBeAdded, 10);
            jQuery('#total').val(added);

        }
        else {
            var totalValue = jQuery("#total").val();
            var toBeMinus = jQuery("#" + icon + "serviceprice").val();
            var left = totalValue - toBeMinus;
            jQuery('#total').val(left);
            jQuery("#" + icon + "serviceprice").attr("disabled", "disabled");
        }


    }


</script>

<script type="text/javascript">
    function validate(serviceOrderSize) {
        for (var i = 1; i <= serviceOrderSize; i++) {
            var con = i.toString();
            if (jQuery("#" + con + "selectservice").attr('checked')) {
                var serqunid = con.concat("servicequantity");
                serqun = jQuery("#" + serqunid).val();
                if (serqun == null || serqun == "") {
                    alert("Please enter quantity");
                    return false;
                }

                if (Number(jQuery("#total").val()) < Number(jQuery("#waiverAmount").val())) {
                    alert("Please enter correct Waiver Amount");
                    return false;
                }
                if (isNaN(jQuery("#waiverAmount").val()) || jQuery("#waiverAmount").val() < 0) {
                    alert("Please enter correct Waiver Amount");
                    return false;
                }
                if (jQuery("#waiverAmount").val() > 0 && jQuery("#waiverComment").val() == "") {
                    alert("Please enter Waiver Number");
                    return false;
                }

                if (serqun != null || quantity != "") {
                    if (isNaN(serqun)) {
                        alert("Please enter quantity in correct format");
                        return false;
                    }
                }
            }
        }
    }

</script>

<div class="clear"></div>

<div class="container">

    <div class="patient-header new-patient-header">
        <div class="demographics">
            <h1 class="name">
                <span><small>${patient.familyName}</small>,<em>surname</em></span>
                <span><small>${patient.givenName} &nbsp;${(patient.middleName)?.replace(',', ' ')}</small><em>name</em>
                </span>
            </h1>

            <div class="gender-age">
                <span>${gender}</span>
                <span>${age} year(s)</span>
            </div>
            <br>

            <div class="status-container">
                <span class="status active"></span>
                Active Visit
            </div>

            <div class="tag">Outpatient (File Number :${fileNumber})</div>
        </div>

        <div class="identifiers">
            <em>Patient ID</em>
            <span>${patient.identifier}</span>
            <em>Payment Category</em>
            <span>${category}</span>
        </div>

        <div class="identifiers">
            <em>Date/ Time:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</em>
            <span>${date}</span>

        </div>
    </div>


</div>
