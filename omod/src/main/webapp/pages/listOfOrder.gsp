<%
	ui.decorateWith("kenyaemr", "standardPage")

    ui.includeJavascript("ehrconfigs", "moment.js")
	ui.includeJavascript("ehrconfigs", "jquery.dataTables.min.js")
	ui.includeJavascript("ehrconfigs", "jq.browser.select.js")
	ui.includeJavascript("ehrconfigs", "knockout-3.4.0.js")
	ui.includeJavascript("ehrconfigs", "jquery-ui-1.9.2.custom.min.js")
	ui.includeJavascript("ehrconfigs", "jquery.simplemodal.1.4.4.min.js")
	ui.includeJavascript("ehrconfigs", "emr.js")

	ui.includeCss("ehrconfigs", "jquery-ui-1.9.2.custom.min.css")
	ui.includeCss("ehrconfigs", "referenceapplication.css")
	ui.includeCss("ehrconfigs", "jquery.dataTables.min.css")
	ui.includeCss("ehrconfigs", "onepcssgrid.css")

    def props = ["sno", "orderid", "date", "sentfrom", "notes"]
%>
<script type="text/javascript">
	jq(document).ready(function () {
		function strReplace(word) {
			var res = word.replace("[", "");
			res=res.replace("]","");
			return res;
		}
		
		jq('#surname').html(strReplace('${patient.names.familyName}')+',<em>surname</em>');
		jq('#othname').html(strReplace('${patient.names.givenName}')+' &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <em>other names</em>');
		jq('#agename').html('${patient.age} years ('+ moment('${patient.birthdate}').format('DD,MMM YYYY') +')');
		
		jq('.tad').text('Last Visit: '+ moment('${previousVisit}').format('DD.MM.YYYY hh:mm')+' HRS');
	});
	
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
	.catg{
		color: #363463;
		margin: 35px 10px 0 0;
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
				Cashier Orders
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

    <div style="margin-top: 5px">
        <table cellpadding="0" cellspacing="0" width="100%" id="queueList" class="tablesorter thickbox">
            <thead>
            <tr align="center">
                <th style="width: 60px">S.No</th>
                <th style="width: 100px">Order ID</th>
                <th style="width: 100px">Date</th>
                <th>Sent From</th>
                <th>Notes</th>
            </tr>
            </thead>
            <tbody>

            <% if (listOfOrders != null || listOfOrders != "") { %>
            <% listOfOrders.each { queue -> %>
            <tr align="center">
                <td>${queue.opdOrderId}</td>
                <td><a class="button task"
                       href="${ui.pageLink("ehrcashier", "procedureInvestigationOrder", [patientId: queue.patient.patientId, encounterId: queue.encounter.encounterId, date: date])}">
                    <i class="icon-signout"></i>${queue.encounter.encounterId}</a></td>
                <td style="text-align: left">${date}</td>
                <td style="text-align: left">${queue.fromDept}</td>
                <td style="text-align: left">${summaryDetails}</td>
            </tr>
            <% } %>

            <% } else { %>
            <tr align="center">
                <td colspan="5">No Orders Found</td>
            </tr>
            <% } %>

            </tbody>
        </table>
    </div>
</div>
