<%
    ui.decorateWith("kenyaemr", "standardPage")


    ui.includeJavascript("ehrcashier", "paging.js")
    ui.includeJavascript("ehrcashier", "moment.js")
    ui.includeJavascript("ehrcashier", "common.js")
    ui.includeJavascript("ehrcashier", "jquery.PrintArea.js")
    ui.includeJavascript("ehrcashier", "knockout-3.4.0.js")

    ui.includeCss("ehrcashier", "paging.css")
    ui.includeCss("ehrconfigs", "referenceapplication.css")

%>
<script>
    var pData;

    jq(function () {
        var bill = new BillItemsViewModel();
        pData = getBillableServices();

		jq('#surname').html(stringReplace('${patient.names.familyName}')+',<em>surname</em>');
		jq('#othname').html(stringReplace('${patient.names.givenName}')+' &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <em>other names</em>');
		jq('#agename').html('${patient.age} years ('+ moment('${patient.birthdate}').format('DD,MMM YYYY') +')');
		jq('.tad').text('Last Visit: '+ moment('${previousVisit}').format('DD.MM.YYYY hh:mm')+' HRS');
		
		jq("#waiverCommentDiv").hide();
		
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


        // Class to represent a row in the bill addition grid
        function BillItem(quantity, initialBill) {
            var self = this;
            self.quantity = ko.observable(quantity);
            self.initialBill = ko.observable(initialBill);

            self.formattedPrice = ko.computed(function () {
                var price = self.initialBill().price;
                return price ? price.toFixed(2) : "0.00";
            });

            self.itemTotal = ko.computed(function () {
                var price = self.initialBill().price;
                var quantity = self.quantity();
                var runningTotal = price * quantity;
                return runningTotal ? runningTotal : "0.00";
            });
        }

        // Overall viewmodel for this screen, along with initial state
        function BillItemsViewModel() {
            var self = this;

            // Non-editable catalog data - would come from the server
            self.availableServices = pData;


            // Editable data
            self.billItems = ko.observableArray([]);

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

            // Operations
            self.addBillItem = function (availableServices) {
                self.billItems.push(new BillItem("1", availableServices));
            }
            self.removeBillItem = function (item) {
                self.billItems.remove(item);
                numberDataTables();
            }
            self.cancelBillAddition = function () {
                window.location.replace("billableServiceBillListForBD.page?patientId=${patientId}&billId=${lastBillId}")
            }
            self.submitBill = function () {
                if (self.totalSurcharge() < self.waiverAmount()) {
                    alert("Please enter correct Waiver Amount");
                } else if (isNaN(self.waiverAmount()) || self.waiverAmount() < 0) {
                    alert("Please enter correct Waiver Amount");
                } else {
                    //submit the details to the server
                    jq("#billsForm").submit();

                }
            }
        }

        ko.applyBindings(bill, jq("#example")[0]);

        jq("#service").autocomplete({
            minLength: 3,
            source: function (request, response) {
                jq.getJSON('${ ui.actionLink("ehrcashier", "billableServiceBillAdd", "loadBillableServices") }',
                        {
                            name: request.term
                        }
                ).success(function (data) {

                            var results = [];
                            for (var i in data) {
                                var result = {label: data[i].name, value: data[i]};
                                results.push(result);
                            }
                            response(results);
                        });
            },
            focus: function (event, ui) {
                jq("#service").val(ui.item.value.name);
                return false;
            },
            select: function (event, ui) {
                event.preventDefault();
                jQuery("#service").val(ui.item.value.name);
                bill.addBillItem(ui.item.value);
                jq('#service').val('');
                jq('#datafield').show();
                numberDataTables();

            }
        });
    });//end of document ready function

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

    function getBillableServices() {
        var toReturn;
        jQuery.ajax({
            type: "GET",
            url: "${ui.actionLink('ehrcashier','billableServiceBillAdd','loadBillableServices')}",
            dataType: "json",
            data: ({
                name: "ray"
            }),
            global: false,
            async: false,
            success: function (data) {
                toReturn = data;
            }
        });
        return toReturn;
    }
</script>

<style>
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

	.formfactor label {
		color: #f26522;
		padding-left: 5px;
	}

	.formfactor input {
		border: 1px solid #aaa;
		color: #222;
		display: block;
		height: 29px;
		margin: 5px 0;
		min-width: 98%;
		padding: 5px 10px;
	}

	.formfactor h2 {
		display: inline-block;
		float: right;
		margin-top: -40px;
		padding-right: 10px;
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

	#datafield {
		display: none;
	}

	#waiverAmount {
		margin-left: -12px;
		margin-right: -12px;
		width: 137px;
	}
	.name {
		color: #f26522;
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
                Add Paid Bill
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
                <i class="icon-tags small" style="font-size: 16px"></i><small>Category:</small> ${category}/${subCategory}
            </div>
        </div>

        <div class="close"></div>
    </div>


    <div id="example">
        <div class="formfactor">
            <label for="service">Add Bill Item:</label><br/>
            <input type="text" id="service" name="service" placeholder="Enter Bill Name"/>

            <h2>Bill Items (<span data-bind="text: billItems().length"></span>)</h2>
        </div>




        <table>
            <thead>
            <tr>
                <th style="width: 40px; text-align: center;">#</th>
                <th>Service Name</th>
                <th style="width: 90px">Quantity</th>
                <th style="width:120px; text-align:right;">Unit Price</th>
                <th style="width:120px; text-align:right;">Item Total</th>
                <th style="width:20px; text-align:center;">&nbsp;</th>
            </tr>
            </thead>

            <tbody id="datafield" data-bind="foreach: billItems, visible: billItems().length > 0">
				<tr>
					<td style="text-align: center;"><span class="nombre"></span></td>
					<td data-bind="text: initialBill().name"></td>

					<td>
						<input data-bind="value: quantity">
					</td>

					<td style="text-align: right;">
						<span data-bind="text: formattedPrice"></span>
					</td>

					<td style="text-align: right;">
						<span data-bind="text: (itemTotal()).toFixed(2)"></span>
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
					<td colspan="3"><b>Total surcharge: Kshs</b></td>

					<td style="text-align: right;">
						<span data-bind="text: (totalSurcharge()).toFixed(2)"></span>
					</td>
					<td style="text-align: right;"></td>
				</tr>

               <% if(canWaiveBills) {%> 
                    <tr style="border: 1px solid #ddd;">
                        <td style="text-align: center;"></td>
                        <td colspan="3"><b>Waiver Amount: Kshs</b></td>

                        <td style="text-align: right;">
                            <input id="waiverAmount" data-bind="value: waiverAmount"/>
                        </td>
                        <td style="text-align: right;"></td>
                    </tr>
                <%}%>
            </tbody>
            <tbody>
                <tr style="border: 1px solid #ddd;">
                    <td style="text-align: center;"></td>
                    <td colspan="2"></td>
                    <td><b>Payment Mode</b></td>

                    <td style="text-align: right;" colspan="2">
                    <select name="paymentMode" id="paymentMode">
                        <option value="Cash">Cash</option>
                        <option value="Mpesa">Mpesa</option>
                        <option value="Insurance">Insurance</option>
                        <option value="NHIF">NHIF</option>
                        <option value="Visa">Visa Card</option>
                        <option value="AirTel Money">AirTel Money</option>
                    </select>
                    </td>
                    <td style="text-align: right;"></td>
                </tr>

                <tr style="border: 1px solid #ddd;">
                    <td style="text-align: center;"></td>
                    <td colspan="2"></td>
                    <td><b>Transaction Code</b></td>

                    <td style="text-align: right;" colspan="2">
                        <input id="transactionCode" name="transactionCode" size="20" />
                    </td>
                    <td style="text-align: right;"></td>
                </tr>
                <tr style="border: 1px solid #ddd;">
                    <td style="text-align: center;"></td>
                    <td colspan="2"></td>
                    <td><b>Description</b></td>

                    <td style="text-align: right;" colspan="2">
                        <textarea  id="transactionDescription" name="transactionDescription" cols="50" rows="2">
                        </textarea>
                    </td>
                    <td style="text-align: right;"></td>
                </tr>
            </tbody>

        </table>
		
		<div id="waiverCommentDiv" style="padding-top: 10px;">
			<label for="waiverComment" style="color: rgb(54, 52, 99);">Waiver Number/Comment</label>
			<textarea id="waiverComment" name="waiverComment" size="7" class="hasborder" style="width: 99.4%; height: 60px;" data-bind="value: comment"></textarea>
		</div>



        <form method="post" id="billsForm" style="padding-top: 10px">
            <input id="patientId" type="hidden" value="${patientId}">
            <textarea name="bill" data-bind="value: ko.toJSON(\$root)" style="display:none;"></textarea>
            <button data-bind="click: submitBill, enable: billItems().length > 0 " class="confirm">Save</button>
            <button data-bind="click: cancelBillAddition" class="cancel">Cancel</button>

        </form>

    </div>

</div>


