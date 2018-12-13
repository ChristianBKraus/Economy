package jupiterpa.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class EID {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	public Marker SERVICE = MarkerFactory.getMarker("EID");
	
	static Long s_number = 0L;
	public static EID get(char appl) {
		return new EID(appl);
	}
		
	Long number;
	private EID(char appl) {
		s_number++;
		logger.trace(SERVICE,"EID - {}: {}",appl,s_number);
		number = s_number;
	}
	private EID(EID number) {
		this.number = number.number;
	}
	@Override
	public String toString() {
		return number.toString();
	}
}
