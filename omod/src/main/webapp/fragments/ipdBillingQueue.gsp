<h2 style="display: inline-block;">Indoor Patient Queue</h2>

<a class="button confirm" id="getIpdPatients" style="float: right; margin: 8px 5px 0 0;">
    Get Patients
</a>

<div class="formfactor onerow">
    <div class="first-col">
        ${ui.includeFragment("uicommons", "field/datetimepicker", [formFieldName: 'datetime', id: 'ipddatetime', label: 'Date', useTime: false, defaultToday: true])}
    </div>

    <div class="second-col">
        <label for="ipdSearchKey">Search patient in Queue:</label><br/>
        <input id="ipdSearchKey" type="text" name="ipdSearchKey" placeholder="Enter Patient Name/ID:">
    </div>
</div>

<div>
    <section>
        <div>
            <table cellpadding="5" cellspacing="0" width="100%" id="ipdQueueList">
                <thead>
                <tr align="center">
                    <th style="width:200px">Patient ID</th>
                    <th>Given Name</th>
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

    <div id="ipdselection" style="display: none; padding-top: 5px;">
        <select name="sizeSelector" id="ipdSizeSelector" onchange="getIpdBillingQueue(1);" style="width: 60px">
            <option value="10" id="1">10</option>
            <option value="20" id="2" selected>20</option>
            <option value="50" id="3">50</option>
            <option value="100" id="4">100</option>
            <option value="150" id="5">150</option>
        </select>

        Entries showing
    </div>
</div>