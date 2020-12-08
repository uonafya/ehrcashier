<%
	ui.decorateWith("kenyaemr", "standardPage")
    ui.includeCss("ehrcashier", "paging.css")

    ui.includeJavascript("ehrconfigs", "moment.js")
    ui.includeJavascript("ehrcashier", "paging.js")
    ui.includeJavascript("ehrcashier", "common.js")
    ui.includeJavascript("ehrcashier", "jq.print.js")
	ui.includeJavascript("ehrconfigs", "knockout-3.4.0.js")
	ui.includeJavascript("ehrconfigs", "jquery-ui-1.9.2.custom.min.js")
	ui.includeJavascript("ehrconfigs", "emr.js")
	ui.includeJavascript("ehrconfigs", "jquery.simplemodal.1.4.4.min.js")

	ui.includeCss("ehrconfigs", "jquery-ui-1.9.2.custom.min.css")
	ui.includeCss("ehrconfigs", "referenceapplication.css")

    def props = ["sno", "service", "select", "quantity", "pay", "unitprice", "itemtotal"]
%>

<script type="text/javascript">
    jq(document).ready(function () {
        function strReplace(word) {
            var res = word.replace("[", "");
            res = res.replace("]", "");
            return res;
        }

        jq('#surname').html(strReplace('${patient.names.familyName}') + ',<em>surname</em>');
        jq('#othname').html(strReplace('${patient.names.givenName}') + ' &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <em>other names</em>');
        jq('#agename').html('${patient.age} years (' + moment('${patient.birthdate}').format('DD,MMM YYYY') + ')');

        jq("#commentField").hide();
        jq("#okButton").hide();

        jq('#initialtotal').text(formatAccounting(jq('#initialtotal').text().trim()));
        jq("#totalValue2").html(toWords(jq("#total").val()));

        jq('.cancel').on('click', function () {
            jq("#commentField").toggle();
            jq("#okButton").toggle();
        });

        jq('.confirm').on('click', function () {
            jq("#printSection").print({
                globalStyles: false,
                mediaPrint: false,
                stylesheet: '${ui.resourceLink("pharmacyapp", "styles/print-out.css")}',
                iframe: false,
                width: 600,
                height: 700
            });
            jq("#billForm").submit();
        });


    });

    function validate() {
        if (StringUtils.isBlank(jQuery("#comment").val())) {
            alert("Please enter comment");
            return false;
        }
        else {

            var patientId = ${patient.patientId};
            var billType = "free";
            var comment = jQuery("#comment").val();
            window.location.href = emr.pageLink("ehrcashier", "addPatientServiceBillForBD", {
                "patientId": patientId,
                "billType": billType,
                "comment": comment
            });
        }
    }
	function formatAccounting(nStr) {
		nStr = parseFloat(nStr).toFixed(2);
		nStr += '';
		x = nStr.split('.');
		x1 = x[0];
		x2 = x.length > 1 ? '.' + x[1] : '';
		var rgx = /(\\d+)(\\d{3})/;
		while (rgx.test(x1)) {
			x1 = x1.replace(rgx, '\$1' + ',' + '\$2');
		}
		return x1 + x2;
	}

	function stringReplace(word) {
		var res = word.replace("[", "");
		res=res.replace("]","");
		return res;
	}
</script>

<style>
	.name {
		color: #f26522;
	}

	.form-textbox {
		height: 12px !important;
		font-size: 12px !important;
	}

	.hidden {
		display: none;
	}

	.retired {
		text-decoration: line-through;
		color: darkgrey;
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
		margin-left: 4px;
		padding: 2px 10px;
	}

	.status-container {
		padding: 5px 10px 5px 5px;
	}

	.catg {
		color: #363463;
		margin: 35px 10px 0 0;
	}

	@media print {
		.donotprint {
			display: none;
		}

		.spacer {
			margin-top: 70px;
			font-family: "Dot Matrix Normal", Arial, Helvetica, sans-serif;
			font-style: normal;
			font-size: 14px;
		}

		.printfont {
			font-family: "Dot Matrix Normal", Arial, Helvetica, sans-serif;
			font-style: normal;
			font-size: 14px;
		}
	}

	.formfactor {
		background: #f3f3f3 none repeat scroll 0 0;
		border: 1px solid #ddd;
		margin-bottom: 5px;
		margin-top: 5px;
		min-height: 38px;
		padding: 5px 10px;
		text-align: left;
		width: auto;
	}

	.dashboard .info-section {
		margin: 0;
		padding-bottom: 10px;
		padding-top: 10px;
		width: 100%;
	}

	.print-only {
		display: none;
	}
</style>

<div class="clear"></div>

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
                Service Bills
            </li>
        </ul>
    </div>

    <div class="patient-header new-patient-header">
        <div class="demographics">
            <h1 class="name">
                <span id="surname">${patient.names.familyName},<em>surname</em></span>
                <span id="othname">${patient.names.givenName} &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<em>other names</em>
                </span>

                <span class="gender-age">
                    <span>
                        ${gender}
                    </span>
                    <span id="agename">${patient.age} years (15.Oct.1996)</span>

                </span>
            </h1>

            <br/>

            <div id="stacont" class="status-container">
                <span class="status active"></span>
                Visit Status
            </div>

            <div class="tag">Outpatient ${fileNumber}</div>

            <div class="tad">Billing ID: #00${bill?.patientServiceBillId}</div>
        </div>

        <div class="identifiers">
            <em>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Patient ID</em>
            <span>${patient.getPatientIdentifier()}</span>
            <br>

            <div class="catg">
                <i class="icon-tags small" style="font-size: 16px"></i><small>Category:</small> ${category}
            </div>
        </div>

        <div class="close"></div>
    </div>


    <% if (bill != null) { %>	
	<div id="printSection">
		<center class="print-only">		
			<h2>
				<img width="100" height="100" align="center" title="OpenMRS" alt="OpenMRS" src="${ui.resourceLink('ehrcashier', 'images/kenya_logo.bmp')}"><br/>
				<b>
					<u>${userLocation}</u>
				</b>
			</h2>
			
			<h2>
				RECEIPT #00${bill?.patientServiceBillId}
			</h2>
		</center>
		
		<div class="print-only">
			<label>
				<span class='status active'></span>
				Identifier:
			</label>
			<span>${patient.getPatientIdentifier()}</span>
			<br/>
			
			<label>
				<span class='status active'></span>
				Full Names:
			</label>
			<span>${patient.givenName} ${patient.familyName} ${patient.middleName?patient.middleName:''}</span>
			<br/>
			
			<label>
				<span class='status active'></span>
				Age:
			</label>
			<span>${patient.age} (${ui.formatDatePretty(patient.birthdate)})</span>
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
			<span>${dateTime}</span>
			<br/>
			
			<label>
				<span class='status active'></span>
				Payment Catg:
			</label>
			<span>${paymentCategoryName} / ${paymentSubCategory}</span>
			<br/>
			
			<% if (bill?.voided) { %>
				<label>
					<span class='status active'></span>
					Bill Description:
				</label>
				<span>${bill?.description}</span>    
				<br/>
			<% } %>
		</div>
	
		<table width="100%" border="1" cellpadding="5" cellspacing="0" id="myTable" class="tablesorter thickbox"
			   style="margin-top: 5px">
			<thead>
			<tr>
				<th>#</th>
				<th align="center">Service Name</th>
				<th style="text-align: right;">Price (KSh)</th>
				<th style="text-align: right;">Quantity</th>
				<th style="text-align: right;">Amount</th>
			</tr>
			</thead>
			<tbody>

			<% bill?.billItems.eachWithIndex { it, index -> %>
			<% if (bill?.voidedDate == null) { %>
			<% if (it.name != "INPATIENT DEPOSIT") { %>
			<tr>
				<td>${index+1}</td>
				<td>${it.name}</td>
				<td align="right">${it.unitPrice}</td>
				<td align="right">${it.quantity}</td>
				<td class="printfont" height="20" align="right" style="">
					<% if (it?.actualAmount != null) { %>
					<% if (it?.actualAmount == it.amount) { %>
					<% if (it?.voidedDate != null) { %>
					<span style="text-decoration: line-through;">${it.amount}</span>
					<% } else { %>
					${it.amount}
					<% } %>
					<% } else { %>

					<span style="text-decoration: line-through;">${it.amount}</span>
					<b>${it?.actualAmount}</b>
					<% } %>
					<% } else { %>
					${it?.amount}
					<% } %>
				</td>
			</tr>
			<% } %>
			<% } %>
			<% } %>
			<% def initialtotal = 0 %>
			<% bill?.billItems.each { %>
			<% if (it.name.equalsIgnoreCase("INPATIENT DEPOSIT")) {
				initialtotal += it.amount
			}
			%>
			<% } %>

			<tr>
				<td></td>
				<td colspan="3"><b>SUB TOTAL</b></td>
				<td align="right">
					<% if (bill?.actualAmount != null) { %>
					<% if (bill?.actualAmount == bill?.amount) { %>
					<% if (bill?.voided) { %>
					<span style="text-decoration: line-through;"><b>${bill?.amount - initialtotal}</b>
					</span>
					<% } else { %>
					<b>${bill?.amount - initialtotal}</b>
					<% } %>
					<% } else { %>
					<span style="text-decoration: line-through;">${bill?.amount - initialtotal}</span>
					<% if (bill?.voided) { %>
					<span style="text-decoration: line-through;"><b>${bill?.actualAmount - initialtotal}</b>
					</span>
					<% } else { %>
					<b>${bill?.actualAmount - initialtotal}</b>
					<% } %>
					<% } %>
					<% } else { %>
					${bill?.amount - initialtotal}
					<% } %>
				</td>
			</tr>
			<% bill?.billItems.each { %>
			<% if (it.voidedDate == null) { %>
			<% if (it.name.equalsIgnoreCase("INPATIENT DEPOSIT")) { %>
			<tr>
				<td></td>
				<td colspan="3">ADVANCE PAYMENT</td>
				<td class="printfont" height="20" align="right" style="">
					<% if (StringUtils.isNotBlank(it.actualAmount)) { %>
					<% if (it.actualAmount == it.amount) { %>
					${it.amount}
					<% } else { %>
					<span style="text-decoration: line-through;">${it.amount}</span>
					<b>${it.actualAmount}</b>
					<% } %>

					<% } else { %>
					${it.amount}
					<% } %>
				</td>
			</tr>
			<% } %>
			<% } %>
			<% } %>
			
			<tr>
				<td></td>
				<td colspan="3">
					<b>ADVANCE PAYMENT</b>
				</td>
				<td align="right" id="initialtotal">${initialtotal}</td>
			</tr>
			
			<tr>
				<td></td>
				<td colspan="3">
					<b>WAIVER AMOUNT</b>
					<% if (bill?.waiverAmount > 0) { %>
						(${bill?.comment})
					<% } %>				
				</td>
				<td align="right">${bill?.waiverAmount}</td>
			</tr>			
			
			<% if (bill?.rebateAmount > 0) { %>
				<tr>
					<td></td>
					<td colspan="3"><b>REBATE AMOUNT</b></td>
					<td align="right">${bill?.rebateAmount}</td>
				</tr>
			<% } %>

			<tr>
				<td></td>
				<td colspan="3"><b>NET AMOUNT</b></td>
				<td align="right"><b>${bill?.actualAmount - bill?.waiverAmount - initialtotal}</b></td>
			</tr>
			</tbody>
		</table>

		<div class="print-only" style="margin: 20px 10px 0 10px">				
			<span>Attending Cashier: <b>${cashier}</b></span>
		</div>
		
		<center class="print-only" style="margin-top: 30px">
			<span>Signature of Billing Clerk/ Stamp</span>		
		</center>
	</div>

    <input type="hidden" id="total" value="${bill?.actualAmount - bill?.waiverAmount}">

    <% } %>


    <form method="POST" id="billForm">
        <div class="formfactor">

            <% if (!patient.dead) { %>
            <button type="button" class="task"
                    onclick="window.location.href = 'addPatientServiceBillForBD.page?patientId=${patient.patientId}&billType=paid&lastBillId=${bill?.patientServiceBillId}'"
                    style="margin-left: -3px">Add Paid Bill</button>
            <button type="button" class="cancel">Add Free Bill</button>
            <% } %>
            <% if (bill != null) { %>

            <span type="button" class="button confirm" style="float: right; margin-right: -5px;">
                <i class="icon-print"></i>
                Print Bill
            </span>

            <% } %>

            <span id="commentField">
                <label for="comment">Comment</label>
                <input id="comment" name="comment"/>
            </span>

            <span id="okButton">
                <input type="button" value="Ok" onclick="return validate();"/>
            </span>
        </div>

    </form>
</div>


<% if (listBill != null) { %>

<div class="dashboard clear">
    <div class="info-section">
        <div class="info-header">
            <i class="icon-calendar"></i>

            <h3>LIST OF PREVIOUS BILLS</h3>
        </div>

        <div class="info-body">
            <table class="box">
                <thead>
                <th style="width: 40px; text-align: center;">#</th>
                <th style="width: 70px">Bill ID</th>
                <th style="width: 200px">Date</th>
                <th>Description</th>
                <th style="width: 100px; text-align: center;">Action</th>
                </thead>

                <% listBill.eachWithIndex { bill, index -> %>
                <tr class='${index % 2 == 0 ? "oddRow" : "evenRow"} '>
                    <td align="center" class='<% if (bill?.voided) { %>retired <% } %>'>
                        ${index + 1}
                    </td>
                    <td class='<% if (bill?.voided) { %>retired <% } %>'>
                        <% if (bill?.voided == false || (bill?.printed == true && canEdit == true)) { %>
                        <a href="editPatientServiceBillForBD.page?billId=${bill?.patientServiceBillId}&patientId=${
                                patient.patientId}">
                            ${bill?.patientServiceBillId}</a>

                        <% } else { %>
                        ${bill?.patientServiceBillId}
                        <% } %>
                    </td>
                    <td class='<% if (bill?.voided) { %>retired <% } %>'>
                        ${bill?.createdDate}
                    </td>
                    <td class='<% if (bill?.voided) { %>retired <% } %>'>
                        <% if (bill?.description != null) { %>
                        ${bill?.description}
                        <% } else { %>
                        N/A
                        <% } %>
                    </td>
                    <td class='<% if (bill?.voided) { %>retired <% } %>' style="text-align: center; ">
                        <% if (bill?.voided) { %>
                        <input type="button" value="View" class="task retired"
                               onclick="javascript:window.location.href = 'patientServiceVoidedBillViewForBD.page?patientId=${patient.patientId}&billId=${bill?.patientServiceBillId}'"/>
                        <% } else { %>
                        <input type="button" value="View" class="task"
                               onclick="javascript:window.location.href = 'patientServiceBillForBD.page?patientId=${patient.patientId}&billId=${bill?.patientServiceBillId}'"/>
                        <% } %>
                    </td>
                </tr>
                <% } %>
            </table>
        </div>
    </div>
</div>
<% } %>
