package org.openmrs.module.ehrcashier.medatada;

public class MTransaction {
	
	private String transactionCode;
	
	private String timeOfTransacton;
	
	private String transactionAmount;
	
	public String getTransactionCode() {
		return transactionCode;
	}
	
	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}
	
	public String getTimeOfTransacton() {
		return timeOfTransacton;
	}
	
	public void setTimeOfTransacton(String timeOfTransacton) {
		this.timeOfTransacton = timeOfTransacton;
	}
	
	public String getTransactionAmount() {
		return transactionAmount;
	}
	
	public void setTransactionAmount(String transactionAmount) {
		this.transactionAmount = transactionAmount;
	}
	
	@Override
	public String toString() {
		return "MTransaction{" + "transactionCode='" + transactionCode + '\'' + ", timeOfTransacton='" + timeOfTransacton
		        + '\'' + ", transactionAmount='" + transactionAmount + '\'' + '}';
	}
}
