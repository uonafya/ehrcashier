<%
    def props = ["wrapperIdentifier", "names", "age", "gender", "formartedVisitDate", "action"]
%>

<script>

    jq(document).ready(function () {
        jq('.col4 select').bind('change keyup', function() {
            ADVSEARCH.delay();
        });

        jq('#tabs-7 input').keydown(function (e) {
            var key = e.keyCode || e.which;
            if ((key == 9 || key == 13) && jq(this).attr('id') != 'searchPhrase') {
                ADVSEARCH.delay();
            }
        });

        jq('#lastDayOfVisit-display').on("change", function (dateText) {
            ADVSEARCH.delay();
        });
    });

    jQuery.fn.clearForm = function() {
        return this.each(function() {
            var type = this.type, tag = this.tagName.toLowerCase();
            if (tag == 'form')
                return jQuery(':input',this).clearForm();
            if ((type == 'text' || type == 'hidden') && jQuery(this).attr('id') != 'searchPhrase')
                this.value = '';
            else if (type == 'checkbox' || type == 'radio')
                this.checked = false;
            else if (tag == 'select')
                this.selectedIndex = -1;
        });
    };

    function strReplace(word) {
        var res = word.replace("[", "");
        res=res.replace("]","");
        return res;
    }

    ADVSEARCH = {
        timeoutId: 0,
        showing: false,
        params: "",
        delayDuration: 1,
        pageSize: 10,
        beforeSearch: function () {
        },
        // search patient
        searchPatient: function (currentPage, pageSize) {
            this.beforeSearch();
            var phrase = jQuery("#searchPhrase").val();

            if (phrase.length >= 1) {
                jQuery("#ajaxLoader").show();
                getPatientQueue(1);
            }
            else{
                jq().toastmessage('showNoticeToast', "Specify atleast one character to Search");
            }
        },
        // start searching patient
        startSearch: function (e) {
            e = e || window.event;
            ch = e.which || e.keyCode;
            if (ch != null) {
                if ((ch >= 48 && ch <= 57) || (ch >= 96 && ch <= 105)
                        || (ch >= 65 && ch <= 90)
                        || (ch == 109 || ch == 189 || ch == 45) || (ch == 8)
                        || (ch == 46)) {
                } else if (ch == 13) {
                    clearTimeout(this.timeoutId);
                    this.timeoutId = setTimeout("ADVSEARCH.delay()",
                            this.delayDuration);
                }
            }
        },
        // delay before search
        delay: function () {
            this.searchPatient(0, this.pageSize);
        },
        visitAddPatientBill: function(patientId){
            window.location.href = emr.pageLink("ehrcashier", "billableServiceBillListForBD", {
                "patientId": patientId
            });
        }
    };

    // get queue
    function getPatientQueue(currentPage) {
        this.currentPage = currentPage;
        var phrase = jQuery("#searchPhrase").val();
        var pgSize = 1000;
        var gender = jQuery("#gender").val();
        var age = jQuery("#age").val();
        var ageRange = jQuery("#ageRange").val();
        var patientMaritalStatus = jQuery("#patientMaritalStatus").val();
        var lastDayOfVisit = jq('#lastDayOfVisit-field').val() && moment(jq('#lastDayOfVisit-field').val()).format('DD/MM/YYYY');
        var lastVisit = jQuery('#lastVisit').val();
        var phoneNumber = jQuery("#phoneNumber").val();
        var relativeName = jQuery("#relativeName").val();
        var nationalId = jQuery("#nationalId").val();
        var fileNumber = jQuery("#fileNumber").val();
        jQuery.ajax({
            type: "POST",
            url: "${ui.actionLink('ehrcashier','searchPatient','searchSystemPatient')}",
            dataType: "json",
            data: ({
                gender: gender,
                phrase: phrase,
                currentPage: currentPage,
                pageSize: pgSize,
                age: age,
                ageRange: ageRange,
                patientMaritalStatus: patientMaritalStatus,
                lastVisit: lastVisit,
                phoneNumber: phoneNumber,
                relativeName: relativeName,
                nationalId: nationalId,
                fileNumber: fileNumber,
                lastDayOfVisit: lastDayOfVisit
            }),
            success: function (data) {
                jQuery("#ajaxLoader").hide();
                pData = data;
                updateSystemQueueTable(data);
            },
            error: function (xhr, ajaxOptions, thrownError) {
                jq().toastmessage('showNoticeToast', "Bill ID Does Not Exist");
                jQuery("#ajaxLoader").hide();
            }
        });
    }
    function HideDashboard() {
        jQuery('#dashboard').hide();
        jQuery('#patientSystemSearchForm').clearForm();
    }
    function ShowDashboard() {
        jQuery('#dashboard').toggle(500);
        jQuery('#patientSystemSearchForm').clearForm();
    }

    //update the queue table
    function updateSystemQueueTable(data) {
        var jq = jQuery;
        jq('#patient-search-results-table > tbody > tr').remove();
        var tbody = jq('#patient-search-results-table > tbody');
        for (index in data) {
            var item = data[index];
            var row = '<tr>';
            <% props.each {
               if(it == props.last()){
                  def pageLinkRevisit = ui.pageLink("ehrcashier", "billingQueue");
                   %>
            row += '<td> ' +
                    '<a title="View/Add Bill" onclick="ADVSEARCH.visitAddPatientBill('+item.patientId +');"><i class="icon-arrow-right small" ></i></a>'+
                    '</td>';
            <% } else {%>
            row += '<td>' + item.${ it} + '</td>';
            row=strReplace(row);
            <% }
               } %>
            row += '</tr>';
            tbody.append(row);
        }
        if (jq('#patient-search-results-table tr').length <= 1){
            tbody.append('<tr align="center"><td colspan="6">No patients found</td></tr>');
        }
    }
</script>

<style>
	.dashboard .info-header #rcptFrom .add-on i {
		font-size: 10px!important;
	}
	#rcptFrom i{
		
	}
</style>

<form onsubmit="return false" id="patientSystemSearchForm" method="get">
    <input autocomplete="off" placeholder="Search by Patient ID,Name or Bill Id" id="searchPhrase"
           style="float:left; width:70%; padding:6px 10px -1px;" onkeyup="ADVSEARCH.startSearch(event);">
    <img id="ajaxLoader" style="display:none; float:left; margin: 3px -4%;"
         src="${ui.resourceLink("ehrcashier", "images/ajax-loader.gif")}"/>

    <div id="advanced" class="advanced" onclick="ShowDashboard();"><i class="icon-filter"></i>ADVANCED SEARCH
    </div>

    <div id="dashboard" class="dashboard" style="display:none;">
        <div class="info-section">
            <div class="info-header">
                <i class="icon-diagnosis"></i>

                <h3>ADVANCED SEARCH</h3>
                <span id="as_close" onclick="HideDashboard();">
                    <div class="identifiers">
                        <span style="background:#00463f; padding: 1px 8px 5px;">x</span>
                    </div>
                </span>
            </div>

            <div class="info-body" style="min-height: 140px;">
                <ul>
                    <li>
                        <div class="onerow">
                            <div class="col4">
                                <label for="gender">Gender</label>
                                <select style="width: 160px" id="gender" name="gender">
                                    <option value="Any">Any</option>
                                    <option value="M">Male</option>
                                    <option value="F">Female</option>
                                </select>
                            </div>

                            <div class="col4">
                                <label>Last Visit</label>
                                ${ui.includeFragment("uicommons", "field/datetimepicker", [formFieldName: 'lastDayOfVisit', id: 'lastDayOfVisit', label: '', useTime: false, defaultToday: false, class: ['newdtp'], endDate: new Date()])}
                            </div>

                            <div class="col4 last">
                                <label for="relativeName">Relative Name</label>
                                <input type="text" id="relativeName" name="relativeName" style="width: 160px"
                                       placeholder="Relative Name">
                            </div>
                        </div>

                        <div class="onerow" style="padding-top: 2px;">
                            <div class="col4">
                                <label for="age">Age</label>
                                <input type="text" id="age" name="age" style="width: 160px" placeholder="Patient Age">
                            </div>

                            <div class="col4">
                                <label for="gender">Previous Visit</label>
                                <select style="width: 160px" id="lastVisit">
                                    <option value="Any">Anytime</option>
                                    <option value="31">Last month</option>
                                    <option value="183">Last 6 months</option>
                                    <option value="366">Last year</option>
                                </select>
                            </div>

                            <div class="col4 last">
                                <label for="nationalId">National ID</label>
                                <input type="text" id="nationalId" name="nationalId" style="width: 160px"
                                       placeholder="National ID">
                            </div>
                        </div>

                        <div class="onerow" style="padding-top:2px;">
                            <div class="col4">
                                <label for="ageRange">Range &plusmn;</label>
                                <select id="ageRange" name="ageRange" style="width: 160px">
                                    <option value="0">Exact</option>
                                    <option value="1">1</option>
                                    <option value="2">2</option>
                                    <option value="3">3</option>
                                    <option value="4">4</option>
                                    <option value="5">5</option>
                                </select>
                            </div>

                            <div class="col4">
                                <label for="phoneNumber">Phone No.</label>
                                <input type="text" id="phoneNumber" name="phoneNumber" style="width: 160px"
                                       placeholder="Phone No.">
                            </div>

                            <div class="col4 last">
                                <label for="fileNumber">File Number</label>
                                <input type="text" id="fileNumber" name="fileNumber" style="width: 160px"
                                       placeholder="File Number">
                            </div>
                        </div>

                        <div class="onerow" style="padding-top: 1px;">
                            <div class="col4">
                                <label for="patientMaritalStatus">Marital Status</label>
                                <select id="patientMaritalStatus" style="width: 160px">
                                    <option value="">Any</option>
                                    <option value="Single">Single</option>
                                    <option value="Married">Married</option>
                                    <option value="Divorced">Divorced</option>
                                    <option value="Widow">Widow</option>
                                    <option value="Widower">Widower</option>
                                    <option value="Separated">Separated</option>
                                </select>
                            </div>

                            <div class="col4">
                                &nbsp;
                            </div>

                            <div class="col4 last">&nbsp;</div>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</form>

<div id="patient-search-results" style="display: block; margin-top:3px;">
    <div role="grid" class="dataTables_wrapper" id="patient-search-results-table_wrapper">
        <table id="patient-search-results-table" class="dataTable"
               aria-describedby="patient-search-results-table_info">
            <thead>
            <tr role="row">
                <th class="ui-state-default" role="columnheader" style="width: 220px;">
                    <div class="DataTables_sort_wrapper">Identifier<span class="DataTables_sort_icon"></span>
                    </div>
                </th>

                <th class="ui-state-default" role="columnheader" width="*">
                    <div class="DataTables_sort_wrapper">Name<span class="DataTables_sort_icon"></span></div>
                </th>

                <th class="ui-state-default" role="columnheader" style="width: 60px;">
                    <div class="DataTables_sort_wrapper">Age<span class="DataTables_sort_icon"></span></div>
                </th>

                <th class="ui-state-default" role="columnheader" style="width: 60px;">
                    <div class="DataTables_sort_wrapper">Gender<span class="DataTables_sort_icon"></span></div>
                </th>

                <th class="ui-state-default" role="columnheader" style="width: 100px;">
                    <div class="DataTables_sort_wrapper">Last Visit<span class="DataTables_sort_icon"></span></div>
                </th>

                <th class="ui-state-default" role="columnheader" style="width: 60px;">
                    <div class="DataTables_sort_wrapper">Action<span class="DataTables_sort_icon"></span></div>
                </th>
            </tr>
            </thead>

            <tbody role="alert" aria-live="polite" aria-relevant="all">
            <tr align="center">
                <td colspan="6">No patients found</td>
            </tr>
            </tbody>
        </table>

    </div>
</div>

