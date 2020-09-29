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

    def props = ["identifier", "fullname", "age", "gendergit", "action"]
%>
<head>
	<script>
        currentPage = 1;
        var pData;
        var jq = jQuery;

        //update the queue table
        function updateQueueTable(data, alerts) {
            var date = moment(jq("#datetime-field").val()).format('DD/MM/YYYY');
            jq('#queueList > tbody > tr').remove();
            var tbody = jq('#queueList > tbody');
			
			if(typeof alerts === 'undefined'){
				alerts = true;
			}
			
			if (data.length == 0){
				tbody.append('<tr align="center"><td colspan="5">No patient found</td></tr>');
				jq("#selection").hide();
				
				if (alerts){
					jq().toastmessage('showErrorToast', "No Records found in the Billing OPD Queue!");
				}
			}
			else {
				for (index in data) {
					var item = data[index];
					var row = '<tr>';
					<% props.each {
				   if(it == props.last()){
						
					  def pageLink = ui.pageLink("ehrcashier", "listOfOrder") %>
					row += '<td> <a href="${pageLink}?patientId=' + item.patientId + '&date=' + date + '"> <i class="icon-signin small"> </i>GO</a> </td>';
					<% } else { if (it == "gender"){%>
						if (item.${ it } == "M"){
							row += '<td>Male</td>';
						}
						else {
							row += '<td>Female</td>';
						}
					<%}else if (it == "age"){ %>
						row += '<td>' + item.${ it } + ' years </td>';
					<%}else{ %>
						row += '<td>' + item.${ it } + '</td>';
					<% }%>
					
					<% }
				   } %>
					row += '</tr>';
					tbody.append(row);
				}
				
				jq("#selection").show();
			}
        }

        // get queue
        function getBillingQueue(currentPage, alerts) {
            this.currentPage = currentPage;
            var date = moment(jq("#datetime-field").val()).format('DD/MM/YYYY');
            var searchKey = jq("#searchKey").val();
            var pgSize = jq("#sizeSelector").val();
            jQuery.ajax({
                type: "GET",
                url: "${ui.actionLink('ehrcashier','opdBillingQueue','getBillingQueue')}",
                dataType: "json",
                data: ({
                    date: date,
                    searchKey: searchKey,
                    currentPage: currentPage,
                    pgSize: pgSize
                }),
                success: function (data) {
                    pData = data;
                    updateQueueTable(data, alerts);

//                    jQuery("#billingqueue").show(0);
//                    jQuery("#billingqueue").html(data);
                },

            });
        }

        jq(function () {
            jq("#tabs").tabs();
			
            jq("#selection").hide(0);
            jq("#getOpdPatients").click(function () {
                getBillingQueue(1);
            });

            var lastValue = '';
            jq("#searchKey").on('change keyup paste mouseup', function () {
                if (jq(this).val() != lastValue) {
                    lastValue = jq(this).val();
                    getBillingQueue(1);
                }

            });
			
			jq('#datetime').on("change", function (dateText) {
				getBillingQueue(1);
			});
			
			getBillingQueue(1, false);
        });
    </script>

    <style>
		body {
			margin-top: 20px;
		}

		.col1, .col2, .col3, .col4, .col5, .col6, .col7, .col8, .col9, .col10, .col11, .col12 {
			color: #555;
			text-align: left;
		}

		form input,
		form select {
			margin: 0px;
			display: inline-block;
			min-width: 50px;
			padding: 2px 10px;
			height: 32px !important;
		}

		.info-header span {
			cursor: pointer;
			display: inline-block;
			float: right;
			margin-top: -2px;
			padding-right: 5px;
		}

		.dashboard .info-section {
			margin: 2px 5px 5px;
		}

		.toast-item {
			background-color: #222;
		}

		@media all and (max-width: 768px) {
			.onerow {
				margin: 0 0 100px;
			}
		}

		form .advanced {
			background: #363463 none repeat scroll 0 0;
			border-color: #dddddd;
			border-style: solid;
			border-width: 1px;
			color: #fff;
			cursor: pointer;
			float: right;
			padding: 6px 0;
			text-align: center;
			width: 27%;
		}
		form .advanced i {
			font-size: 22px;
		}
		.col4 label {
			width: 110px;
			display: inline-block;
		}
		.col4 input[type=text] {
			display: inline-block;
			padding: 4px 10px;
		}
		.col4 select {
			padding: 4px 10px;
		}
		form select {
			min-width: 50px;
			display: inline-block;
		}
		
		.identifiers span {
			border-radius: 50px;
			color: white;
			display: inline;
			font-size: 0.8em;
			letter-spacing: 1px;
			margin: 5px;
		}

		table.dataTable thead th, table.dataTable thead td {
			padding: 5px 10px;
		}

		form input:focus {
			border: 1px solid #00f !important;
		}

		input[type="text"], select {
			border: 1px solid #aaa;
			border-radius: 2px !important;
			box-shadow: none !important;
			box-sizing: border-box !important;
			height: 32px;
		}

		.newdtp {
			width: 166px;
		}

		#lastDayOfVisit label {
			display: none;
		}

		#lastDayOfVisit input {
			width: 160px;
		}
		.add-on {
			color: #f26522;
			float: right;
			left: auto;
			margin-left: -29px;
			margin-top: 10px;
			position: absolute;
		}
		.chrome .add-on {
		  margin-left:-31px;
		  margin-top:-27px !important;
		  position:relative !important;
		}
		#lastDayOfVisit-wrapper .add-on{
			margin-top: 5px;
		}
		.ui-widget-content a {
			color: #007fff;
		}
		#breadcrumbs a, #breadcrumbs a:link, #breadcrumbs a:visited {
			text-decoration: none;
		}
		.new-patient-header .identifiers {
			margin-top: 5px;
		}
		.name {
			color: #f26522;
		}
		#inline-tabs {
			background: #f9f9f9 none repeat scroll 0 0;
		}
		.formfactor{
			background: #f3f3f3 none repeat scroll 0 0;
			border: 1px solid #ddd;
			margin-bottom: 5px;
			padding: 5px 10px;
			text-align: left;
			width: auto;
		}
		.formfactor .lone-col{
			display: inline-block;
			margin-top: 5px;
			overflow: hidden;
			width: 100%;
		}
		.formfactor .first-col{
			display: inline-block;
			margin-top: 5px;
			overflow: hidden;
			width: 300px;
		}
		.formfactor .second-col{
			display: inline-block;
			float: right;
			margin-top: 5px;
			overflow: hidden;
			width: 600px;
		}
		#datetime label{
			display: none;
		}
		.formfactor .lone-col input,
		.formfactor .first-col input,
		.formfactor .second-col input{
			margin-top: 5px;
			padding: 5px 15px;
			width: 100%;
		}
		.formfactor .lone-col label,
		.formfactor .first-col label,
		.formfactor .second-col label{
			padding-left: 5px;
			color: #363463;
			cursor: pointer;
		}
		.ui-tabs-panel h2{
			display: inline-block;
		}
		td a:hover{
			text-decoration: none;
		}
		#modal-overlay {
			background: #000 none repeat scroll 0 0;
			opacity: 0.4 !important;
		}
    </style>
</head>

<body>
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
					<a>Billing UI</a>
				</li>

				<li>
					<i class="icon-chevron-right link"></i>
					Cashier Module
				</li>
			</ul>
		</div>
		<div class="patient-header new-patient-header">

			<div class="identifiers">
				<em>&nbsp; &nbsp; Current Time:</em>
				<span>${currentTime}</span>
			</div>

			<div id="tabs" style="margin-top: 40px!important;">
				<ul id="inline-tabs">
					<li><a href="#tabs-1">OPD Queue</a></li>
					<li><a href="#tabs-2">IPD Queue</a></li>
                    <li><a href="#pharmacyTab">Pharmacy</a></li>
					<li><a href="#ambulanceTab">Ambulance</a></li>
					<li><a href="#tabs-4">Tender</a></li>
					<li><a href="#tabs-5">Misc Service</a></li>
					<li><a href="#tabs-7">Search Patient Bill</a></li>
				</ul>

				<div id="tabs-1">
					<h2 style="display: inline-block;">Outdoor Patient Queue</h2>
					
					<a class="button confirm" id="getOpdPatients" style="float: right; margin: 8px 5px 0 0;">
						Get Patients
					</a>
					
					<div class="formfactor onerow">
						<div class="first-col">
							<label> Date </label><br/>
							${ui.includeFragment("uicommons", "field/datetimepicker", [formFieldName: 'datetime', id: 'datetime', label: 'Date', useTime: false, defaultToday: true])}
						</div>
						
						<div class="second-col">
							<label for="searchKey">Search patient in Queue:</label><br/>
							<input id="searchKey" type="text" name="searchKey" placeholder="Enter Patient Name/ID:">
						</div>
					</div>
					
					<div>
						<section>
							<div>
								<table cellpadding="5" cellspacing="0" width="100%" id="queueList">
									<thead>
									<tr align="center">
										<th style="width:200px">Patient ID</th>
										<th>Given Name</th>
										<th>Age</th>
										<th>Gender</th>
										<th style="width: 60px">Action</th>
									</tr>
									</thead>
									<tbody>
										<tr align="center">
											<td colspan="5">No patient found</td>
										</tr>
									</tbody>
								</table>
							</div>
						</section>

						<div id="selection" style="display: none; padding-top: 5px;">
							<select name="sizeSelector" id="sizeSelector" onchange="getBillingQueue(1);" style="width: 60px">
								<option value="10" id="1">10</option>
								<option value="20" id="2" selected>20</option>
								<option value="50" id="3">50</option>
								<option value="100" id="4">100</option>
								<option value="150" id="5">150</option>
							</select>
							
							Entries showing
						</div>
					</div>
				</div>

				<div id="tabs-2">
					<h2>Inpatient Patient Queue</h2>
					
					<a class="button confirm" style="float: right; margin: 8px 5px 0 0;">
						Get Patients
					</a>
					
					<div class="formfactor onerow">
						<div class="lone-col">
							<label for="username">Filter Patient in Queue</label>
							<input id="username" type="text" name="username" placeholder="Enter Patient Name/ID:">
						</div>
					</div>
					
					<section>
						<div>
							<table cellpadding="5" cellspacing="0" width="100%" id="queueList2">
								<thead>
								<tr align="center">
									<th>S.No</th>
									<th>Admission Date</th>
									<th>Patient ID</th>
									<th>Name</th>
									<th>Age</th>
									<th>Admission Ward</th>
									<th>Select Action</th>
								</tr>
								</thead>
								<tbody>
								<tr align="center">
									<td>S.No</td>
									<td>Admission Date</td>
									<td>Patient ID</td>
									<td>Name</td>
									<td>Age</td>
									<td>Admission Ward</td>

									<td><button class="button confirm">Add Bill</button>
										<button class="button confirm">View Bill</button>
									</td>
								</tr>
								<tr align="center">
									<td colspan="7">No patient found</td>
								</tr>
								</tbody>
							</table>
						</div>
					</section>
				</div>
                <div id="pharmacyTab">
                    ${ui.includeFragment("ehrcashier", "subStoreIssueDrugList")}
                </div>

				<div id="ambulanceTab">
					${ui.includeFragment("ehrcashier", "ambulanceBilling")}
				</div>

				<div id="tabs-4">
					<h2>Tender Billing</h2>
					
					<a class="button confirm" style="float: right; margin: 8px 5px 0 0;">
						Get Companies
					</a>
					
					<a class="button task" style="float: right; margin: 8px 5px 0 0;">
						Add Company
					</a>
					
					<div class="formfactor onerow">
						<div class="lone-col">
							<label for="username4">Enter Company's Name:</label>
							<input id="username4" type="text" name="username" placeholder="Name:">
						</div>
					</div>
					
					<section>
						<div>
							<table cellpadding="5" cellspacing="0" width="100%" id="queueList4">
								<thead>
								<tr align="center">
									<th>Company Name</th>
									<th>Description</th>
								</tr>
								</thead>
								<tbody>
								<tr align="center">
									<td>Name</td>
									<td>Description</td>
								</tr>
								<tr align="left">
									<td colspan="7">No Result</td>
								</tr>
								</tbody>
							</table>
						</div>
					</section>
				</div>
				
				<div id="tabs-5">
					<h2>Misc Service Billing</h2>
					
					<a class="button confirm" style="float: right; margin: 8px 5px 0 0;">
						Get Services
					</a>
					
					<a class="button task" style="float: right; margin: 8px 5px 0 0;">
						Add Service
					</a>
					
					<div class="formfactor onerow">
						<div class="lone-col">
							<label for="username5">Enter Service Name:</label>
							<input id ="username5" type="text" name="username" placeholder="Name:">
						</div>
					</div>
					
					<section>
						<div>
							<table cellpadding="5" cellspacing="0" width="100%" id="queueList45">
								<thead>
									<tr align="center">
										<th>Service Name</th>
										<th>Description</th>
									</tr>
								</thead>
								<tbody>
									<tr align="center">
										<td>Name</td>
										<td>Description</td>
									</tr>
									
									<tr align="left">
										<td colspan="7">No Result</td>
									</tr>
								</tbody>
							</table>
						</div>
					</section>
				</div>
				


                <div id="tabs-7">
                    ${ui.includeFragment("ehrcashier", "searchPatient")}
                </div>
			</div>			
		</div>
	</div>




