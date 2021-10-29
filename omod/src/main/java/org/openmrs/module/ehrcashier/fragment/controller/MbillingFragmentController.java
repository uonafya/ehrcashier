package org.openmrs.module.ehrcashier.fragment.controller;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.ehrcashier.medatada.MTransaction;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MbillingFragmentController {
	
	public void controller(FragmentConfiguration config) {
		config.require("billno");
		String transactionCode = config.get("billno").toString();
		//
	}
	
	public SimpleObject verifyMpesaTransaction(@RequestParam("mpesaTransactionCode") String mpesaTracnsactionCode) {
		
		MTransaction mTransaction = new MTransaction();
		/*if the transaction code exists on the local {in used codes}
		* {then check the
		*  balance to be redeemed from the code. -- person who used the code should be same as one trying to redeem from it that is if there is a balance
		* } */
		//send a request to the server for verification of a transaction and return a value that is pushed to the cashier
		return null;
	}
	//verify m-pesa-transaction-code
	
}
