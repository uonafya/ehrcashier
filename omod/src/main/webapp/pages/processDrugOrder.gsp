<%
    ui.decorateWith("kenyaemr", "standardPage")
    ui.includeJavascript("ehrcashier", "jq.print.js")
    ui.includeCss("ehrconfigs", "jquery.dataTables.min.css")
    ui.includeCss("ehrconfigs", "onepcssgrid.css")
    ui.includeJavascript("ehrconfigs", "moment.js")
    ui.includeJavascript("ehrconfigs", "jquery.dataTables.min.js")
    ui.includeJavascript("ehrconfigs", "jq.browser.select.js")
    ui.includeJavascript("ehrconfigs", "knockout-3.4.0.js")
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
%>
<style>
.retired {
    text-decoration: line-through;
    color: darkgrey;
}
</style>
<script>
    jq(function () {
        var listOfDrugToIssue =
        ${listDrugIssue}.
        listDrugIssue;
        var listNonDispensed =
        ${listOfNotDispensedOrder}.
        listOfNotDispensedOrder;

        var wAmount = "${waiverAmount}" == "null" ? "0" : "${waiverAmount}";
        var wComment = "${waiverComment}";

        jq('#waiverAmount').on('keyup', function () {
            if (jq(this).val() > 0) {
                if (jq(this).val() > parseInt(orders.totalSurcharge())) {
                    jq().toastmessage('showErrorToast', "Waiver can not be greater than total payable!");
                    jq(this).val('');
                    jq('#waivComment').hide();
                } else {
                    jq('#waivComment').show();
                }
            } else if (isNaN(jq(this).val().trim())) {
                jq('#waivComment').hide();
                jq(this).val('');
                jq().toastmessage('showErrorToast', "Enter Valid Waiver Amount!");
            }
            else {
                jq('#waivComment').hide();
            }
        });

        function DrugOrderViewModel() {
            var self = this;
            self.availableOrders = ko.observableArray([]);
            self.nonDispensed = ko.observableArray([]);
            var mappedOrders = jQuery.map(listOfDrugToIssue, function (item) {
                return new DrugOrder(item);
            });
            var mappedNonDispensed = jQuery.map(listNonDispensed, function (item) {
                return new NonDrugOrder(item);
            });

            self.availableOrders(mappedOrders);
            self.nonDispensed(mappedNonDispensed);

            //observable waiver
            self.waiverAmount = ko.observable(wAmount);

            //observable comment
            self.comment = ko.observable("");

            //observable drug
            self.flag = ko.observable(${flag});
            self.prescriber = ko.observable("${prescriber?:''}");

            // Computed data
            self.totalSurcharge = ko.computed(function () {
                var total = 0;
                for (var i = 0; i < self.availableOrders().length; i++) {
                    total += self.availableOrders()[i].orderTotal();
                }
                return total.toFixed(2);
            });

            self.runningTotal = ko.computed(function () {
                var rTotal = self.totalSurcharge() - self.waiverAmount();
                return rTotal.toFixed(2);
            });


            //submit bill
            self.submitBill = function () {
                var flag = ${flag};
				var emrLink = emr.pageLink("ehrcashier", "billingQueue");
				    emrLink = emrLink.substring(0, emrLink.length-1)+'#pharmacyTab';				
				
				if (flag === 0) {
                    emrLink = null;
                }
				
                jq("#printSection").print({
                    globalStyles: false,
                    mediaPrint: false,
                    stylesheet: '${ui.resourceLink("pharmacyapp", "styles/print-out.css")}',
                    iframe: false,
                    width: 980,
                    height: 700,
					redirectTo: emrLink
                });				

                if (flag === 0) {
                    jq("#drugBillsForm").submit();
                }
            }

            self.isNonPaying = ko.computed(function () {
                var cat = "${paymentCategory}";
                if (cat == 2) {
                    return true;
                } else {
                    return false;
                }
            });
        }

        function DrugOrder(item) {
            var self = this;
            self.initialBill = ko.observable(item);
            self.orderTotal = ko.computed(function () {
                var quantity = self.initialBill().quantity;
                var price = self.initialBill().transactionDetail.costToPatient;
                return quantity * price;
            });
        }

        function NonDrugOrder(item) {
            var self = this;
            self.initialNonBill = ko.observable(item);
        }

        var orders = new DrugOrderViewModel();
        ko.applyBindings(orders, jq("#dispensedDrugs")[0]);

    });//end of document ready
</script>

<style>
.name {
    color: #f26522;
}

#breadcrumbs a, #breadcrumbs a:link, #breadcrumbs a:visited {
    text-decoration: none;
}

.new-patient-header .demographics .gender-age {
    font-size: 14px;
    margin-left: -55px;
    margin-top: 12px;
}

.new-patient-header .demographics .gender-age span {
    border-bottom: 1px none #ddd;
}

.new-patient-header .identifiers {
    margin-top: 5px;
}

.tag {
    padding: 2px 10px;
}

.tad {
    background: #666 none repeat scroll 0 0;
    border-radius: 1px;
    color: white;
    display: inline;
    font-size: 0.8em;
    padding: 2px 10px;
}

.status-container {
    padding: 5px 10px 5px 5px;
}

.catg {
    color: #363463;
    margin: 35px 10px 0 0;
}

.title {
    border: 1px solid #eee;
    margin: 3px 0;
    padding: 5px;
}

.title i {
    font-size: 1.5em;
    padding: 0;
}

.title span {
    font-size: 20px;
}

.title em {
    border-bottom: 1px solid #ddd;
    color: #888;
    display: inline-block;
    font-size: 0.5em;
    margin-right: 10px;
    text-transform: lowercase;
    width: 200px;
}

table {
    font-size: 14px;
}

th:first-child {
    width: 5px;
}

#orderBillingTable th:nth-child(3) {
    min-width: 105px;
}

#orderBillingTable th:nth-child(4) {
    min-width: 85px;
}

#orderBillingTable th:nth-child(5) {
    width: 50px;
}

#waivers label {
    display: inline-block;
    padding-left: 10px;
    width: 140px;
}

.print-only {
    display: none;
}
</style>

<div class="container">
    <div class="example">
        <ul id="breadcrumbs">
            <li>
                <a href="${ui.pageLink('kenyaemr', 'userHome')}">
                    <i class="icon-home small"></i></a>
            </li>

            <li>
                <i class="icon-chevron-right link"></i>
                <a href="${ui.pageLink('ehrcashier', 'billingQueue')}">Billing</a>
            </li>

            <li>
                <i class="icon-chevron-right link"></i>
                Pharmacy Order
            </li>
        </ul>
    </div>

    <div class="patient-header new-patient-header">
        <div class="demographics">
            <h1 class="name">
                <span id="surname">${familyName},<em>surname</em></span>
                <span id="othname">${givenName} ${middleName} &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<em>other names</em>
                </span>

                <span class="gender-age">
                    <span>
                        ${gender}
                    </span>
                    <span id="agename">${age} years</span>
                </span>
            </h1>

            <br/>

            <div id="stacont" class="status-container">
                <span class="status active"></span>
                Visit Status
            </div>

            <div class="tag">Outpatient</div>

            <div class="tad">Last Visit: ${ui.formatDatePretty(lastVisit)}</div>
        </div>

        <div class="identifiers">
            <em>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Patient ID</em>
            <span>${identifier}</span>
            <br>

            <div class="catg">
                <i class="icon-tags small" style="font-size: 16px"></i><small>Category:</small>${paymentSubCategory}
            </div>
        </div>

        <div class="close"></div>
    </div>


    <div class="title">
        <i class="icon-time"></i>
        <span>
            ${ui.formatDatePretty(date)}
            <em style="width: 70px;">&nbsp; order date</em>
        </span>

        <i class="icon-quote-left"></i>
        <span>
            00${receiptid}
            <em>&nbsp; receipt number</em>
        </span>
    </div>

    <div class="dashboard clear" id="dispensedDrugs">
        <div id="printSection">
            <center class="print-only">
                <h2>
                    <img width="100" height="100" align="center" title="OpenMRS" alt="OpenMRS"
                         src="${ui.resourceLink('ehrcashier', 'images/kenya_logo.bmp')}"><br/>
                    <b>
                        <u>${userLocation}</u>
                    </b>
                </h2>

                <h2>
                    CASH RECEIPT : PHARM-00${receiptid}
                </h2>
            </center>

            <div class="print-only">
                <label>
                    <span class='status active'></span>
                    Identifier:
                </label>
                <span>${identifier}</span>
                <br/>

                <label>
                    <span class='status active'></span>
                    Full Names:
                </label>
                <span>${givenName} ${familyName} ${middleName ? middleName : ''}</span>
                <br/>

                <label>
                    <span class='status active'></span>
                    Age:
                </label>
                <span>${age} (${ui.formatDatePretty(birthdate)})</span>
                <br/>

                <label>
                    <span class='status active'></span>
                    Gender:
                </label>
                <span>${gender}</span>
                <br/>

                <label>
                    <span class='status active'></span>
                    Print Date:
                </label>
                <span>${date}</span>
                <br/>

                <label>
                    <span class='status active'></span>
                    Payment Catg:
                </label>
                <span>${paymentCategoryName} / ${paymentSubCategory}</span>
                <br/>
                <br/>
            </div>

            <table width="100%" id="orderBillingTable" class="tablesorter thickbox">
                <thead>
                <tr align="center">
                    <th>#</th>
                    <th>DRUG</th>
                    <th>FORMULATION</th>
                    <th>FREQUENCY</th>
                    <th>#DAYS</th>
                    <th>COMMENTS</th>
                    <th>EXPIRY</th>
                    <th>QNTY</th>
                    <th>PRICE</th>
                    <th>TOTAL</th>
                </tr>
                </thead>

                <tbody data-bind="foreach: availableOrders, visible: availableOrders().length > 0">
                <tr>
                    <td data-bind="text: \$index()+1"></td>
                    <td data-bind="text: initialBill().transactionDetail.drug.name"></td>
                    <td>
                        <span data-bind="text: initialBill().transactionDetail.formulation.name"></span> -
                        <span data-bind="text: initialBill().transactionDetail.formulation.dozage"></span>
                    </td>
                    <td data-bind="text: initialBill().transactionDetail.frequency.name"></td>
                    <td data-bind="text: initialBill().transactionDetail.noOfDays"></td>
                    <td data-bind="text: initialBill().transactionDetail.comments"></td>
                    <td data-bind="text: initialBill().transactionDetail.dateExpiry.substring(0, 11)"></td>
                    <td data-bind="text: initialBill().quantity"></td>
                    <td data-bind="text: initialBill().transactionDetail.costToPatient.toFixed(2)"></td>
                    <td data-bind="text: orderTotal().toFixed(2)"></td>
                </tr>
                </tbody>

                <tbody>
                <tr>
                    <td></td>

                    <td colspan="8">
                        <b>TOTAL AMOUNT</b>
                    </td>

                    <td>
                        <span data-bind="text: totalSurcharge, css:{'retired': isNonPaying()}"></span>
                        <span data-bind="visible: isNonPaying()">0.00</span>
                    </td>
                </tr>

                <tr>
                    <td></td>

                    <td colspan="8">
                        <span data-bind="visible: flag() == 0"><b>TOTAL PAYABLE</b></span>
                        <span data-bind="visible: flag() == 1"><b>TOTAL TENDERED</b></span>
                    </td>

                    <td>
                        <span data-bind="text: runningTotal,css:{'retired': isNonPaying()}"></span>
                        <span data-bind="visible: isNonPaying()">0.00</span>
                    </td>
                </tr>
                </tbody>
            </table>

            <div data-bind="visible: nonDispensed().length > 0" class="print-only">
                <h2>
                    DRUGS NOT ISSUED
                </h2>

                <table width="100%" id="nonDispensedDrugsTable" class="tablesorter thickbox">
                    <thead>
                    <tr align="center">
                        <th>#</th>
                        <th>DRUG</th>
                        <th>FORMULATION</th>
                        <th>FREQUENCY</th>
                        <th>#DAYS</th>
                        <th>COMMENTS</th>
                    </tr>
                    </thead>

                    <tbody data-bind="foreach: nonDispensed">
                    <tr>
                        <td data-bind="text: \$index()+1"></td>
                        <td data-bind="text: initialNonBill().inventoryDrug.name"></td>
                        <td>
                            <span data-bind="text: initialNonBill().inventoryDrugFormulation.name"></span> -
                            <span data-bind="text: initialNonBill().inventoryDrugFormulation.dozage"></span> -
                        </td>
                        <td data-bind="text: initialNonBill().frequency.name"></td>
                        <td data-bind="text: initialNonBill().noOfDays"></td>
                        <td data-bind="text: initialNonBill().comments"></td>
                    </tr>
                    </tbody>
                </table>
            </div>


            <div class="print-only" style="margin: 20px 10px 0 10px">
                <span style="float:right;">Attending Pharmacist: <b>${pharmacist}</b></span>
                <span>Attending Cashier: <b>${cashier}</b></span><br/>
                <span data-bind="visible: prescriber() != ''">Prescribed By: <b>${prescriber}</b></span>
            </div>
        </div>

        <div id='waivers' data-bind="visible: flag() == 0" style="margin-top: 10px">
            <label for="waiverAmount">Waiver Amount:</label>
            <input id="waiverAmount" data-bind="value: waiverAmount"/><br/>

            <div id="waivComment" style="margin-top: 2px; display: none;">
                <label for="waiverComment">Waiver Comment:</label>
                <textarea type="text" id="waiverComment" name="waiverComment"
                          size="7" class="hasborder" style="height: 60px; width: 83.4%; resize: none;"
                          data-bind="value: comment"></textarea> <br/>
            </div>
        </div>

        <div id="nonDispensedDrugs" data-bind="visible: nonDispensed().length > 0" style="display: none;">
            <div class="title" style="margin-top: 10px;">
                <i class="icon-remove-sign" style="color: #f00"></i>
                <span>
                    DRUGS NOT ISSUED
                    <em>&nbsp; from pharmacy</em>
                </span>
            </div>

            <table width="100%" id="nonDispensedDrugsTable1" class="tablesorter thickbox">
                <thead>
                <tr align="center">
                    <th>#</th>
                    <th>DRUG</th>
                    <th>FORMULATION</th>
                    <th>FREQUENCY</th>
                    <th>#DAYS</th>
                    <th>COMMENTS</th>
                </tr>
                </thead>

                <tbody data-bind="foreach: nonDispensed">
                <tr>
                    <td data-bind="text: \$index()+1"></td>
                    <td data-bind="text: initialNonBill().inventoryDrug.name"></td>
                    <td>
                        <span data-bind="text: initialNonBill().inventoryDrugFormulation.name"></span> -
                        <span data-bind="text: initialNonBill().inventoryDrugFormulation.dozage"></span> -
                    </td>
                    <td data-bind="text: initialNonBill().frequency.name"></td>
                    <td data-bind="text: initialNonBill().noOfDays"></td>
                    <td data-bind="text: initialNonBill().comments"></td>
                </tr>
                </tbody>
            </table>
        </div>

        <div style="margin: 10px">
            <i class="icon-user small"></i>
            <label style="font-size: 90%; color: rgb(85, 85, 85); display: inline-block; width: 115px;">Cashier:</label>${cashier}<br/>

            <i class="icon-user small"></i>
            <label style="font-size: 90%; color: rgb(85, 85, 85); display: inline-block; width: 115px;">Pharmacist:</label>${pharmacist}<br/>
        </div>

        <form method="post" id="drugBillsForm" style="display: none;">
            <input id="patientId" name="patientId" type="text" value="${identifier}">
            <input id="receiptid" name="receiptid" type="text" value="${receiptid}">
            <input id="flag" name="flag" type="text" value="${flag}">

            <textarea name="drugOrder" data-bind="value: ko.toJSON(\$root)"></textarea>
        </form>
		
		<span class="button cancel" onclick="javascript:window.location.href = 'billingQueue.page?'">Cancel</span>
		
		<% if (flag == 1) { %>
		<span id="savebill" class="button task right"
			  data-bind="click: submitBill, enable: availableOrders().length > 0 ">
			<i class="icon-print small"></i>
			Reprint
		</span>
		<% } else { %>
		<span id="savebill" class="button task right"
			  data-bind="click: submitBill, enable: availableOrders().length > 0 ">
			<i class="icon-save small"></i>
			Finish
		</span>
		<% } %>
    </div>
</div>