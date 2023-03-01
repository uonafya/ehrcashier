<%
    def props = ["wrapperIdentifier", "names", "age", "gender", "formartedVisitDate", "action"]
%>

<script>

    jq(document).ready(function () {
        var today = new Date();
        getWalkinPatientQueue(1,today);
    });

    // get queue
    function getWalkinPatientQueue(currentPage,todate) {
        this.currentPage = currentPage;
        var phrase = jQuery("#searchPhrase").val();
        var pgSize = 1000;
        var gender = jQuery("#gender").val();
        var age = jQuery("#age").val();
        var ageRange = jQuery("#ageRange").val();
        var patientMaritalStatus = jQuery("#patientMaritalStatus").val();
        var lastDayOfVisit = moment(new Date()).format('DD/MM/YYYY');
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
                console.log(pData);
                getPatientWalkinQueue(data);
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
    function getPatientWalkinQueue(data) {
        var jq = jQuery;
        jq('#walkin-search-results-table > tbody > tr').remove();
        var tbody = jq('#walkin-search-results-table > tbody');
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
        if (jq('#walkin-search-results-table tr').length <= 1){
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


<div id="walkin-search-results" style="display: block; margin-top:3px;">
    <div role="grid" class="dataTables_wrapper" id="walkin-search-results-table_wrapper">
        <table id="walkin-search-results-table" class="dataTable"
               aria-describedby="walkin-search-results-table_info">
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