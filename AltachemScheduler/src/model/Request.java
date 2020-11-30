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
	public boolean isShippingDay(int day) {
		return shippingDays[day];
	}
	public Request(int i) {
		this.requestId = i;
	}
	
	public int[] getAmountsRequested() {
		return this.amountsRequested;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + requestId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
		if (requestId != other.requestId)
			return false;
		return true;
	}
}
