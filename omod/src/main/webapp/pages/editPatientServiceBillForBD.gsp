<%
    ui.decorateWith("kenyaemr", "standardPage")
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
<script>
    var pData;
    jq(function () {
        jq("#waiverCommentDiv").hide();
        jq('#waiverAmount').on('change keyup paste', function () {
            var numb = jq('#waiverAmount').val();

            if (!isNaN(parseFloat(numb)) && isFinite(numb) && numb > 0) {
                jq("#waiverCommentDiv").show();
            }
            else {
                jq("#waiverCommentDiv").hide();
            }
        });
        pData = ${billingItems};
        var billItems = pData.billingItems;
        var bill = new BillItemsViewModel();
        jq('#surname').html(stringReplace('${patient.names.familyName}') + ',<em>surname</em>');
        jq('#othname').html(stringReplace('${patient.names.givenName}') + ' &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <em>other names</em>');
        jq('#agename').html('${patient.age} years (' + moment('${patient.birthdate}').format('DD,MMM YYYY') + ')');

        jq('.tad').text('Last Visit: ' + moment('${previousVisit}').format('DD.MM.YYYY hh:mm') + ' HRS');


        // Class to represent a row in the bill addition grid
        function BillItem(initialBill) {
            var self = this;
            self.initialBill = ko.observable(initialBill);
            self.quantity = ko.observable(initialBill.quantity);
            self.price = ko.observable(initialBill.unitPrice);
            self.formattedPrice = ko.computed(function () {
                var price = self.price();
                return price ? price.toFixed(2) : "0.00";
            });

            self.itemTotal = ko.computed(function () {
                var price = self.price();
                var quantity = self.quantity();
                var runningTotal = price * quantity;
                return runningTotal ? runningTotal : "0.00";
            });
        }

        function BillItemsViewModel() {
            var self = this;

            // Editable data
            self.billItems = ko.observableArray([]);
            var mappedBillItems = jQuery.map(billItems, function (item) {
                return new BillItem(item)
            });
            self.billItems(mappedBillItems);

            // Computed data
            self.totalSurcharge = ko.computed(function () {
                var total = 0;
                for (var i = 0; i < self.billItems().length; i++)
                    total += self.billItems()[i].itemTotal();
                return total;
            });

            //observable waiver
            self.waiverAmount = ko.observable(0.00);

            //observable comment
            self.comment = ko.observable("");

            //observable waiver Number
            self.waiverNumber = ko.observable("");

            // Operations

            self.removeBillItem = function (item) {
                if (self.billItems().length > 1) {
                    self.billItems.remove(item);
					numberDataTables();
                }else{
                    jq().toastmessage('showErrorToast', "A Bill Must have at least one item");
               }

            }
            self.cancelBillAddition = function () {
                window.location.replace("billableServiceBillListForBD.page?patientId=${patientId}&billId=${billId}")
            }
            self.submitBill = function () {
                jQuery("#action").val("submit");
                var waiverComment = jQuery("#waiverComment").val();
                if (self.totalSurcharge() < self.waiverAmount()) {
                    jq().toastmessage('showErrorToast', "Please enter correct Waiver Amount");
                } else if (isNaN(self.waiverAmount()) || self.waiverAmount() < 0) {
                    jq().toastmessage('showErrorToast', "Please enter correct Waiver Amount");
                } else if (waiverComment == '' || waiverComment == null) {
                    jq().toastmessage('showErrorToast', "Please enter Comments/Waiver Number");

                } else {
                    //submit the details to the server
                    jq("#billsForm").submit();

                }
            }

            self.voidBill = function () {
                var waiverComment = jQuery("#waiverComment").val();
                if (waiverComment == '' || waiverComment == null) {
                    jq().toastmessage('showNoticeToast', "Please enter Comment");
                    return false;
                }
                if (confirm("Are you sure about this?")) {
                    jQuery("#action").val("void");
                    jQuery('#billsForm').submit();
                } else {
                    return false;
                }

            }
        }
		
		jq('#waiverAmount').on('change keyup paste', function () {
			var numb = jq('#waiverAmount').val();
			
			if (!isNaN(parseFloat(numb)) && isFinite(numb) && numb>0){
				jq("#waiverCommentDiv").show();
			}
			else {
				jq("#waiverCommentDiv").hide();
			}
        });

        jq('#waiverAmount').on('focus', function () {
            var numb = jq('#waiverAmount').val();
            if (!isNaN(parseFloat(numb)) && isFinite(numb) && numb > 0) {
                jq('#waiverAmount').val(parseFloat(jq('#waiverAmount').val()))
            }
            else {
                jq("#waiverAmount").val('');
            }
        });

        jq('#waiverAmount').on('blur', function () {
            var numb = jq('#waiverAmount').val();
            if (!isNaN(parseFloat(numb)) && isFinite(numb) && numb > 0) {
                jq('#waiverAmount').val(formatAccounting(numb));
            }
            else {
                jq("#waiverAmount").val('0.00');
            }
        });
		
		jq("#waiverCommentDiv").hide();
		jq("#waiverAmount").val(formatAccounting(jq("#waiverAmount").val()));

        ko.applyBindings(bill, jq("#example")[0]);
		numberDataTables();

    });//end of document ready

	function formatDataTables() {
        if (('#datafield tr').length == 0) {
            jq('#datafield').hide();
        }
        else {
            jq('#datafield').show();
        }
        numberDataTables();
    }

    function numberDataTables() {
        var i = 0;

        jq('#datafield  > tr').each(function () {
            jq('#datafield tr').find("span.nombre").eq(i).text(i + 1);
            i++;
        });
    }

</script>

<style>
	.name {
		color: #f26522;
	}
	.toast-item {
        background-color: #222;
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

	form input[type="text"] {
		background: transparent none repeat scroll 0 0;
	}

	form input[type="text"]:focus {
		outline: 1px none #ddd;
	}

	td a,
	td a:hover {
		cursor: pointer;
		text-decoration: none;
	}

	td input {
		background: transparent none repeat scroll 0 0;
		border: 1px solid #aaa;
		padding-right: 10px;
		text-align: right;
		width: 80px;
	}

	table th, table td {
		border: 1px solid #ddd;
		padding: 5px 20px;
	}

	.align-left {
		width: 200px;
		display: inline-block;
	}

	.align-right {
		float: right;
		width: 720px;
		display: inline-block;
	}
	
	#waiverCommentDiv label{
		display: inline-block;
		margin-left: 10px;
		width: 150px;
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
	.formfactor h1 {
		color: #f26522;
		display: inline-block;
		font-size: 1.3em;
		margin: 5px;
	}
	.formfactor h2 {
		display: inline-block;
		float: right;
		margin-top: 5px;
		padding-right: 10px;
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
                <a href="${ui.pageLink('ehrcashier', 'billingQueue')}">Service Bills</a>
            </li>

            <li>
                <i class="icon-chevron-right link"></i>
                Edit Bill(Bill ID: ${billId})
            </li>
        </ul>
    </div>

    <div class="patient-header new-patient-header">
        <div class="demographics">
            <h1 class="name">
                <span id="surname"></span>
                <span id="othname"></span>

                <span class="gender-age">
                    <span>
                        <% if (patient.gender == "F") { %>
                        Female
                        <% } else { %>
                        Male
                        <% } %>
                    </span>
                    <span id="agename"></span>

                </span>
            </h1>

            <br/>

            <div id="stacont" class="status-container">
                <span class="status active"></span>
                Visit Status
            </div>

            <div class="tag">Outpatient ${fileNumber}</div>

            <div class="tad">Last Visit</div>
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

    <div id="example">
        <div class="formfactor">
			<h1>
				<i class="icon-pencil small"></i>Edit Bill
			</h1>
            <h2>Items (<span data-bind="text: billItems().length"></span>)</h2>
        </div>
        <table style="margin-bottom: 5px;">
            <thead>
				<tr>
					<th style="width:40px; text-align: center;">#</th>
					<th>Service Name</th>
					<th style="width:90px">Quantity</th>
					<th style="width:90px; text-align:right;">Unit Price</th>
					<th style="width:90px; text-align:right;">Item Total</th>
					<th style="width:20px; text-align:center;">&nbsp;</th>
				</tr>
            </thead>

            <tbody id="datafield" data-bind="foreach: billItems, visible: billItems().length > 0">
				<tr>
					<td style="text-align: center;"><span class="nombre"></span></td>
					<td data-bind="text: initialBill().service.name"></td>

					<td>
						<input data-bind="value: quantity">
					</td>

					<td style="text-align: right;">
						<span data-bind="text: formattedPrice"></span>
					</td>

					<td style="text-align: right;">
						<span data-bind="text: itemTotal().toFixed(2)"></span>
					</td>

					<td style="text-align: center;">
						<a class="remover" href="#" data-bind="click: \$root.removeBillItem">
							<i class="icon-remove small" style="color:red"></i>
						</a>
					</td>
				</tr>
            </tbody>

            <tbody>
				<tr style="border: 1px solid #ddd;">
					<td style="text-align: center;"></td>
					<td colspan="3"><b>Total Charge: Kshs</b></td>

					<td style="text-align: right;">
						<span data-bind="text: totalSurcharge().toFixed(2)"></span>
					</td>
					<td style="text-align: right;"></td>
				</tr>

				<tr style="border: 1px solid #ddd;">
					<td style="text-align: center;"></td>
					<td colspan="3"><b>Waiver Amount: Kshs</b></td>

					<td style="text-align: right;">
						<input id="waiverAmount" data-bind="value: waiverAmount" style="margin-right: -10px"/>
					</td>
					<td style="text-align: right;"></td>
				</tr>
            </tbody>
        </table>

        <div id="waiverCommentDiv" style="padding-top: 2px;">
            <label for="waiverNumber" style="color: rgb(54, 52, 99); width: 150px; padding-left: 0px; display: inline-block;">Waiver Number</label>
            <input type="text" size="20" data-bind="value: waiverNumber" name="waiverNumber" id="waiverNumber" style="width: 808px;"/>
			<br/>
        </div>
		
		<div id="othersCommentDiv" style="padding-top: 2px;">
			<label for="waiverComment" style="color: rgb(54, 52, 99); width: 150px; padding-left: 10px; display: inline-block;">Comment</label>
			<textarea type="text" id="waiverComment" name="waiverComment" size="7" class="hasborder"
					  style="height: 60px; width: 808px; margin-top: 2px;"
					  data-bind="value: comment"></textarea>
		</div>
       

        <form method="post" id="billsForm" style="padding-top: 10px">
            <input type="hidden" id="patientId" value="${patientId}">
            <input type="hidden" id="action" name="action">
            <textarea name="bill" data-bind="value: ko.toJSON(\$root)" style="display:none;"></textarea>
			
            <button data-bind="click: submitBill, enable: billItems().length > 0 " class="confirm" style="float: right; margin-right: 2px;">Save Bill</button>
            <button id="billVoid" data-bind="click: voidBill" class="cancel" style="margin-left: 2px">Void Bill</button>
            <button data-bind="click: cancelBillAddition" class="cancel">Cancel</button>

        </form>

    </div>
</div>



