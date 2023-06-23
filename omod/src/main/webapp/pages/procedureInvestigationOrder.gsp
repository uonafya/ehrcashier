<%
	ui.decorateWith("kenyaemr", "standardPage")

	ui.includeJavascript("ehrconfigs", "moment.js")
	ui.includeJavascript("ehrconfigs", "jquery.dataTables.min.js")
	ui.includeJavascript("ehrconfigs", "jq.browser.select.js")
	ui.includeJavascript("ehrconfigs", "knockout-3.4.0.js")
	ui.includeJavascript("ehrconfigs", "jquery-ui-1.9.2.custom.min.js")
	ui.includeJavascript("ehrconfigs", "emr.js")
	ui.includeJavascript("ehrconfigs", "jquery.simplemodal.1.4.4.min.js")

	ui.includeCss("ehrconfigs", "referenceapplication.css")
	ui.includeCss("ehrconfigs", "jquery.dataTables.min.css")
	ui.includeCss("ehrconfigs", "onepcssgrid.css")
	ui.includeCss("ehrconfigs", "jquery-ui-1.9.2.custom.min.css")

    def props = ["sno", "service", "select", "quantity", "pay", "unitprice", "itemtotal"]
%>

<script type="text/javascript">
    jQuery(document).ready(function () {
        var sos =${serviceOrderSize};
		
		jq('#surname').html(strReplace('${patient.names.familyName}')+',<em>surname</em>');
		jq('#othname').html(strReplace('${patient.names.givenName}')+' &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <em>other names</em>');
		jq('#agename').html('${patient.age} years ('+ moment('${patient.birthdate}').format('DD,MMM YYYY') +')');
		
		jq('.tad').text('Last Visit: '+ moment('${previousVisit}').format('DD.MM.YYYY hh:mm')+' HRS');

        jQuery("#waiverCommentDiv").hide();
        jQuery('.serquncalc').keyup(function () {
            var result = 0;
            jQuery('.serpricalc').each(function () {
                var valueAdd= jQuery(this).val();
                if (valueAdd !== '') {
                    result += parseInt(jQuery(this).val());
                }
            });

            jQuery('#total').attr('value', result);

        });

        if (sos == 0) {
            jq("#savebill").hide();
        }

        var result = 0;
        jq('#total').attr('value', function () {
            jQuery('.serpricalc').each(function () {
                if (jq(this).val() !== '') {
                    result += parseInt(jQuery(this).val());
                }
            });
            return result;
        });
		
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
			if (!isNaN(parseFloat(numb)) && isFinite(numb) && numb>0){
				jq('#waiverAmount').val(parseFloat(jq('#waiverAmount').val()))
			}
			else {
				jq("#waiverAmount").val('');
			}
        });
		
		jq('#waiverAmount').on('blur', function () {
			var numb = jq('#waiverAmount').val();
			var totl = jq('#total').val();
			
			if (!isNaN(parseFloat(numb)) && isFinite(numb) && numb>0){
				jq('#waiverAmount').val(formatAccounting(numb));
				
				if (parseFloat(numb) > parseFloat(totl)){
					jq().toastmessage('showErrorToast', 'Waiver amount cannot be greater than the total amount');
					jq('#waiverAmount').addClass('red-border');
				} else{
					jq('#waiverAmount').removeClass('red-border');				
				}
			}
			else {
				jq("#waiverAmount").val('0.00');
			}
        });
		
		jq('#savebill').click(function(){		
			if (validate()){
				jq('#orderBillingForm').submit();
			}
		
		});
		
		jq('#waiverAmount').val('0.00');
        recalculate(sos);

    });
	
	function strReplace(word) {
		var res = word.replace("[", "");
		res=res.replace("]","");
		return res;
	}

    function recalculate(sos) {
        var mytot = 0;
        for (i = 1; i <= sos; i++) {
            if (jQuery("#" + i + "paybill").attr('unchecked') || jQuery("#" + i + "selectservice").attr('unchecked')) {
                //do nothing
            } else {
                var servQuantity = parseInt(jQuery("#" + i + "servicequantity").val());
                mytot += (parseInt(jQuery("#" + i + "serviceprice").val()) * servQuantity);
            }
        }
        jQuery("#total").val(formatAccounting(mytot));

    }

    ///end of ready


    function loadWaiverCommentDiv() {
        ///jQuery("#waiverCommentDiv").show();
    }
    function updatePrice(incon) {
        var con = incon.toString();
        var serqunid = con.concat("servicequantity");
        var serpriid = con.concat("serviceprice");
        var unipriid = con.concat("unitprice");
		
        serqun = jQuery("#" + serqunid).val();
        unpri = jQuery("#" + unipriid).val();
        jQuery("#" + serpriid).val(serqun * unpri);

    }
    function disable(incon) {
        var icon = incon.toString();

        if (jQuery("#" + icon + "selectservice").attr('checked')) {
            jQuery("#" + icon + "servicequantity").removeAttr("disabled");
            jQuery("#" + icon + "serviceprice").removeAttr("disabled");
            jQuery("#" + icon + "paybill").removeAttr("disabled");

            var totalValue = jQuery("#total").val();
            var toBeAdded = jQuery("#" + icon + "serviceprice").val();
            var added = parseInt(totalValue, 10) + parseInt(toBeAdded, 10);
            jQuery('#total').val(formatAccounting(added));
        }
        else {
            jQuery("#" + icon + "servicequantity").attr("disabled", "disabled");
            jQuery("#" + icon + "paybill").attr("disabled", "disabled");
            jQuery("#" + icon + "serviceprice").attr("disabled", "disabled");
            var totalValue = jQuery("#total").val();
            var toBeMinus = jQuery("#" + icon + "serviceprice").val();
            var left = totalValue - toBeMinus;
            jQuery('#total').val(formatAccounting(left));
        }

    }

    function payCheckBox(incon) {

        var icon = incon.toString();
        if (jQuery("#" + icon + "paybill").attr('checked')) {
            jQuery("#" + icon + "serviceprice").removeAttr("disabled");
            var totalValue = jQuery("#total").val();
            var toBeAdded = jQuery("#" + icon + "serviceprice").val();
            var added = parseInt(totalValue, 10) + parseInt(toBeAdded, 10);
            jQuery('#total').val(formatAccounting(added));

        }
        else {
            var totalValue = jQuery("#total").val();
            var toBeMinus = jQuery("#" + icon + "serviceprice").val();
            var left = totalValue - toBeMinus;
            jQuery('#total').val(formatAccounting(left));
            jQuery("#" + icon + "serviceprice").attr("disabled", "disabled");
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
    function validate() {
		var errorCount = 0;
		var waiverAmts = jq("#waiverAmount").val();
		var serviceQty = ${serviceOrderSize};
		
        for (var i = 1; i <= serviceQty; i++) {
            var con = i.toString();
            if (jq("#" + con + "selectservice").attr('checked')) {
                var serqunid = con.concat("servicequantity");
                serqun = jq("#" + serqunid).val();
				
				console.log(serqunid);
				console.log(serqun);
				
                if (isNaN(parseFloat(serqun)) || Number(serqun) < 0) {
					jq("#" + serqunid).addClass('red-border');
                    errorCount++;
                }
				else{
					jq("#" + serqunid).removeClass('red-border');
				}                
            }
			else{
				jq("#" + serqunid).removeClass('red-border');
			}
        }
		
		if (Number(jq("#total").val()) < Number(waiverAmts)) {
			jq().toastmessage('showErrorToast', "Waiver amount cannot be Larger than the total Amount");
			return false;
		}
		
		if ((isNaN(waiverAmts) || Number(waiverAmts) < 0) && ${canWave}) {
			jq().toastmessage('showErrorToast', "Please enter correct Waiver Amount");
			return false;
		}
		
		if (Number(waiverAmts) > 0 && jQuery("#waiverComment").val() == "") {
			jq().toastmessage('showErrorToast', "Please enter Waiver Number or Waiver Comment");
			return false;
		}
		
		if (errorCount > 0){
			jq().toastmessage('showErrorToast', "Ensure Quantity fields highlighted in red are filled properly");
			return false;
		}
		else{
			return true;
		}
    }
</script>

<style>
	.toast-item {
        background-color: #222;
    }
	.form-textbox {
		height: 23px !important;
		font-size: 12px !important;
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
	.catg{
		color: #363463;
		margin: 35px 10px 0 0;
	}
	form input, form select, form textarea, form ul.select, .form input, .form select, .form textarea, .form ul.select {
		background: transparent none repeat scroll 0 0;
		border: 1px none #ddd;
	}
	.arcpricalc,
	.serpricalc,
	.rights	{
		text-align: right;
	}
	.hasborder {
		border: 1px solid #ddd;
	}
	form input:focus, form select:focus, form textarea:focus {
		outline: 1px none #ddd!important;
	}
	.red-border {
        border: 1px solid #f00 !important;
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
				<a href="${ui.pageLink('kenyaemr','userHome')}">
				<i class="icon-home small"></i></a>
			</li>
			<li>
				<i class="icon-chevron-right link"></i>
				<a href="${ui.pageLink('ehrcashier','billingQueue')}">Billing</a>
			</li>
			
			<li>
				<i class="icon-chevron-right link"></i>
				<a href="${ui.pageLink('ehrcashier','listOfOrder')}?patientId=${patientId}&date=${date}">Orders</a>
			</li>
			
			<li>
				<i class="icon-chevron-right link"></i>
				Procedure & Investigation
			</li>
		</ul>
	</div>
	
    <div class="patient-header new-patient-header">
        <div class="demographics">
			<h1 class="name">
				<span id="surname">${patient.names.familyName},<em>surname</em></span>
				<span id="othname">${patient.names.givenName} &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<em>other names</em></span>
				
				<span class="gender-age">
					<span>
						<% if (patient.gender == "F") { %>
							Female
						<% } else { %>
							Male
						<% } %>
						</span>
					<span id="agename">${patient.age} years (15.Oct.1996) </span>
					
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

    <form id="orderBillingForm" action="procedureInvestigationOrder.page?patientId=${patientId}&encounterId=${encounterId}&indCount=${serviceOrderSize}&billType=mixed&date=${date}" method="POST" style="padding-top: 5px">
        <div class="dashboard clear">
            <table cellpadding="5" cellspacing="0" width="100%" id="myTable" class="tablesorter thickbox">
                <thead>
                <tr align="center">
                    <th>S.No</th>
                    <th>Service</th>
                    <th>Select</th>
                    <th>Quantity</th>
                    <th>Pay</th>
                    <th style="text-align: right;">Unit Price</th>
                    <th style="text-align: right;">Item Total</th>
                </tr>
                </thead>
                <tbody>

                <% if (serviceOrderList != null || serviceOrderList != "") { %>
                <% serviceOrderList.eachWithIndex { queue, index -> %>
                <tr align="center">
                    <td>${index + 1}</td>
                    <td><input type="text" class="form-textbox" id="${index + 1}service" name="${index + 1}service"
                               value="${queue.name}" readOnly="true">
                    </td>
                    <td>
                        <input type="checkbox" class="form-textbox" id="${index + 1}selectservice"
                               name="${index + 1}selectservice"
                               checked="checked" value="billed" onclick="disable(${index+ 1});">
                    </td>
                    <td>
                        <input type="text" class="form-textbox serquncalc" id="${index + 1}servicequantity"
                               name="${index + 1}servicequantity" size="7" value=1
                               onkeyup="updatePrice(${index+ 1});" style="border: 1px solid #ddd" />
                    </td>
                    <td>
                        <input type="checkbox" class="form-textbox" id="${index + 1}paybill" name="${index + 1}paybill"
                               checked="checked" value="pay" onclick="payCheckBox(${index+ 1});">
                    </td>
                    <td>
                        <input type="text" class="form-textbox arcpricalc" id="${index + 1}unitprice" name="${index + 1}unitprice"
                               size="7"
                               value="${queue.price}" readOnly="true">
                    </td>
                    <td>
                        <input type="text" class="form-textbox serpricalc" id="${index + 1}serviceprice"
                               name="${index + 1}serviceprice"
                               size="7" value="${queue.price}" readOnly="true" />
                    </td>
                </tr>
                <% } %>

                <% } else { %>
                <tr align="center">
                    <td colspan="7">No Orders Found</td>
                </tr>
                <% } %>

                </tbody>
                <tr>
                    <td colspan="6" align="right" style="padding-right: 23px">Total</td>
                    <td align="right"><input type="text" class="form-textbox rights" id="total" name="total" size="7" value="0"
                                             readOnly="true"/>
                    </td>
                </tr>
				<% if (canWave) { %>
				<tr>
                    <td colspan="6" align="right" style="padding-right: 23px">Waiver Amount</td>
                    <td align="right"><input type="text" id="waiverAmount" name="waiverAmount" size="7"
                                             class="form-textbox rights hasborder" /></td>
                </tr>
				<%}%>
            </table>
			<table align="center" width="50%">
				<td>
					<div id="waiverCommentDiv" class="form-group">
						<label for="waiverComment" style="color: #363463;">Waiver Number/Comment</label>
						<textarea  id="waiverComment" name="waiverComment" cols="50" rows="5" class="hasborder" style="width: 97.7%; height: 60px;"></textarea>
													
					</div>
				</td>
			</table>
          <table cellpadding="0" cellspacing="0" width="50%" align="right">
		  		<tr>
					<td align="right" style="padding-right: 23px">Payment Mode</td>
					<td>
						<div id="paymentModesId" class="form-group" align="right">
							<select name="paymentMode" id="paymentMode">
								<option value="Cash">Cash</option>
								<option value="Mpesa">Mpesa</option>
								<option value="Insurance">Insurance</option>
								<option value="NHIF">NHIF</option>
								<option value="Visa">Visa Card</option>
								<option value="AirTel Money">AirTel Money</option>
							</select>
						</div>
					</td>	
				</tr>
				<tr>
					<td align="right" style="padding-right: 23px">Transaction Code</td>
					<td align="right"><input type="text" id="transactionCode" name="transactionCode" size="20" /></td>
				</tr>
				<tr>
                    <td align="right" style="padding-right: 23px">Description</td>
                    <td>
                        <textarea  id="transactionDescription" name="transactionDescription" cols="30" rows="3">
                        </textarea>
                    </td>
                </tr>
			</table>
			<tr>
				<td><input type="button" class="button cancel"
						onclick="javascript:window.location.href = 'billingQueue.page?'"
						value="Cancel">
				</td>
				<td>

					<span id="savebill" name="savebill" class="button confirm right" style="margin: 10px 0"> 
						<i class="icon-save small"></i>
						Save Bill
					</span>
				</td>
			</tr>
        </table>
			
        </div>
    </form>
</div>
