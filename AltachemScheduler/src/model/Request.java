package model;

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
}
