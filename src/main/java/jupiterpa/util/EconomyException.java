package jupiterpa.util;

public class EconomyException extends Exception {
	private static final long serialVersionUID = 1L;

	char level;
	String msg;
	Object[] objects;
	
	public EconomyException(String msg, Object... objects ) {
		level = 'E';
		this.msg = msg;
		this.objects = objects;
	}
	public EconomyException(char level, String msg, Object... objects) {
		this.level = level;
		this.msg = msg;
		this.objects = objects;
	}
	
	@Override
	public String toString() {
		return String.format(msg, objects);
	}
}
