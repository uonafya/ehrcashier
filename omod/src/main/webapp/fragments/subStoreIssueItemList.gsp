<script>
    var toReturn;
    jq(function () {
        var receiptsData = getItemOrderList();

        jQuery('.date-pick').datepicker({minDate: '-100y', dateFormat: 'dd/mm/yy'});
        jq("#issueName, #receiptId").on("keyup", function () {
            issueList();
        });

        jq("#fromDate-display, #toDate-display, #searchProcessed").change(function () {
            issueList();
        });

		jq('#getPharmPatients').click(function(){
			issueList();
		});

        function issueList() {
            var receiptId 	= jq("#receiptId").val();
            var issueName 	= jq("#issueName").val();
            var fromDate 	= moment(jq("#fromDate-field").val()).format('DD/MM/YYYY');
            var toDate 		= moment(jq("#toDate-field").val()).format('DD/MM/YYYY');
            var processed	= jq('#searchProcessed:checked').length;

            toReturn		= getItemOrderList(issueName, fromDate, toDate, receiptId, processed);

            list.drugList(toReturn);
        }

        function IssueDrugViewModel() {
            var self = this;
            // Editable data
            self.drugList = ko.observableArray([]);
            var mappedDrugItems = jQuery.map(receiptsData, function (item) {
                return item;
            });

            self.viewDetails = function (item) {
                window.location.replace("detailedReceiptOfDrug.page?receiptId=" + item.id);
            };
            self.drugList(mappedDrugItems);
            self.processDrugOrder = function (item) {
                //redirect to processing page
                var url = '${ui.pageLink("ehrcashier","processDrugOrder")}';
                window.location.href = url + '?orderId=' + item.id + '&patientId=' + item.patientId;
            }
        }

        var list = new IssueDrugViewModel();
        ko.applyBindings(list, jq("#orderList")[0]);
    });

    function getItemOrderList(issueName, fromDate, toDate, receiptId, processed) {
		if (typeof processed == 'undefined'){
			processed = jq('#searchProcessed:checked').length;
		}

        jQuery.ajax({
            type: "GET",
            url: '${ui.actionLink("ehrcashier", "subStoreIssueItemList", "getItemOrderList")}',
            dataType: "json",
            global: false,
            async: false,
            data: {
                issueName: issueName,
                fromDate: fromDate,
                toDate: toDate,
                processed: processed,
                receiptId: receiptId
            },
            success: function (data) {
                toReturn = data;
            }
        });
        return toReturn;
    }
</script>

<style>
	.formfactor .zero-col{
		display: inline-block;
		margin-top: 5px;
		overflow: hidden;
		width: 35%;
	}
	.formfactor .other-col{
		display: inline-block;
		margin-top: 5px;
		overflow: hidden;
		width: 21%;
	}
	.formfactor .zero-col input,
	.formfactor .other-col input{
		padding: 0 10px;
		width: 98%;
	}
	#fromDate label,
	#toDate label{
		display: none;
	}
	#fromDate .add-on,
	#toDate .add-on {
		margin-top: 5px;
	}
	.formfactor .zero-col label,
	.formfactor .other-col label {
		color: #363463;
		cursor: pointer;
		padding-left: 5px;
		margin-bottom: 5px;
	}
	#orderList th:first-child{
		width: 5px;
	}
	#orderList th:nth-child(2){
		width: 70px;
	}
	#orderList th:nth-child(3){
		width: 220px;
	}
	#orderList th:nth-child(5){
		width: 100px;
	}
	#orderList th:last-child{
		width: 100px;
	}
	#divSeachProcessed{
		margin-right: 5px;
		margin-top: 18px;
	}
	#divSeachProcessed label{
		cursor: pointer;
	}
	#divSeachProcessed input{
		cursor: pointer;
	}
	.process-lozenge {
		border: 1px solid #f00;
		border-radius: 4px;
		color: #f00;
		display: inline-block;
		font-size: 0.7em;
		padding: 1px 2px;
		vertical-align: text-bottom;
	}
	.process-seen {
		background: #fff799 none repeat scroll 0 0 !important;
		color: #000 !important;
	}
</style>

<h2>Inventory Patient Queue</h2>

<span id="getPharmPatients" class="button confirm right" style="float: right; margin: 8px 5px 0 0;">
	<i class="icon-refresh small"></i>
	Get Patients
</span>

<div id="divSeachProcessed" class="right">
	<label style="padding: 3px 10px; background: rgb(255, 247, 153) none repeat scroll 0px 0px; border: 1px solid rgb(238, 238, 238);">
		<input type="checkbox" id="searchProcessed" name="searchProcessed">
		Include Paid Patients
	</label>
</div>

<div class="formfactor onerow">
	<div class="zero-col">
		<label for="issueName">Patient Name</label><br/>
		<input type="text" name="issueName" id="issueName" class=" searchFieldBlur" placeholder="Patient Name"/>
	</div>

	<div class="other-col">
		<label for="fromDate-display">From Date:</label><br/>
		${ui.includeFragment("uicommons", "field/datetimepicker", [formFieldName: 'fromDate', id: 'fromDate', label: 'From Date', useTime: false, defaultToday: true, class: ['searchFieldChange', 'date-pick', 'searchFieldBlur']])}
	</div>

	<div class="other-col">
		<label for="toDate-display">To Date:</label><br/>
		${ui.includeFragment("uicommons", "field/datetimepicker", [formFieldName: 'toDate', id: 'toDate', label: 'To Date', useTime: false, defaultToday: true, class: ['searchFieldChange', 'date-pick', 'searchFieldBlur']])}
	</div>

	<div class="other-col">
		<label for="receiptId">From Date:</label><br/>
		<input type="text" name="receiptId" class="searchFieldChange" id="receiptId" placeholder="Receipt No."/>
	</div>
</div>

<form method="get" id="form">
    <div id="orderList">
        <table width="100%">
            <thead>
				<tr>
					<th>#</th>
					<th>RECEIPT</th>
					<th>IDENTIFIER</th>
					<th>NAMES</th>
					<th>DATE</th>
					<th>ACTION</th>
				</tr>
            </thead>
            <tbody data-bind="foreach: drugList">
				<tr data-bind="css: {'process-seen': flag > 0}">
					<td data-bind="text: \$index() + 1"></td>
					<td data-bind="text: id"></td>
					<td data-bind="text: identifier"></td>
					<td>
						<span data-bind="text: givenName"></span>&nbsp;
						<span data-bind="text: familyName"></span>
						<span data-bind="visible: flag > 0" class="process-lozenge">Processed</span>
					</td>
					<td data-bind="text: moment(new Date(createdOn)).format('DD/MM/YYYY')"></td>
					<td>
						<a class="remover" href="#" data-bind="click: \$root.processDrugOrder"
						   title="Detail issue item to this patient">
							<span data-bind="visible: flag == 0">
								<i class="icon-cogs small"></i>PROCESS
							</span>

							<span data-bind="visible: flag > 0">
								<i class="icon-folder-open small"></i> VIEW
							</span>

						</a>
					</td>
				</tr>
            </tbody>
        </table>
    </div>
</form>