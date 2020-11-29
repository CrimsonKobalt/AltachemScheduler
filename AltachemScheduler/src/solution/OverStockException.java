package solution;

public class OverStockException extends Exception{
	public OverStockException() {
		super("item-stock overschreden.");
	}
	public OverStockException(int i) {
		super("item-stock overschreden voor item " + i + ".");
	}
}
