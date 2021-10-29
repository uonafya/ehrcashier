
<script>
    jq("#verifymcode").onclick({
        //make a call to the m-pesa verification controller funtion
    });
</script>

<table cellpadding="5" cellspacing="0" width="100%" id="verificationtable" class="thickbox">
    <tr align="center">
        <td style="text-align:right;">
            <label type="text">Transaction Code</label>
        </td>

        <td>
            <input type="text" class="form-textbox" id="transactioncode" name="transactioncode" placeholder="PJT7VMBUJG" autocomplete="off" style="width: 96.40%; text-align:center;"/>
        </td>

        <td>
            <span id="verifymcode" name="verifymcode" class="button confirm center" style="margin: 10px 0">
                <i class="icon-save small"></i>
                Send
            </span>
        </td>
    </tr>
</table>
