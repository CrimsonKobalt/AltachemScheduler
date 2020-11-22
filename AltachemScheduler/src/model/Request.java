package model;

import java.util.Arrays;

public class Request {
	private int requestId;
	
	private int[] amountsRequested;
	private boolean[] shippingDays;
	
	public void setAmountsRequested(int[] amounts) {
		this.amountsRequested = amounts;
	}
	
	public void setShippingDays(boolean[] canBeShipped) {
		this.shippingDays = canBeShipped;
	}
	
	public Request(int i) {
		this.requestId = i;
	}
	
	public String requestedItemsToString() {
		return Arrays.toString(this.amountsRequested);
	}
	
	public String shippingDaysToString() {
		return Arrays.toString(shippingDays);
	}
	
	public int getId() {
		return this.requestId;
	}
}
