package jupiterpa.util;

import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
public class SystemService {
	@Getter Credentials credentials = new Credentials(0);
	
	public void logon(Credentials credentials) {
		this.credentials = credentials;
	}
	public void logoff() {
		this.credentials = new Credentials(0);
	}
}
