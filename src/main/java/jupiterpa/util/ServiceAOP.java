package jupiterpa.util;

import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jupiterpa.IService;


@Aspect
@Component
public class ServiceAOP {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	public Marker SERVICE = MarkerFactory.getMarker("SERVICE");
	
	@Autowired SystemService system;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Around("target(jupiterpa.IService)")
	public Object log(ProceedingJoinPoint jointPoint) throws Throwable {
		IService service = (IService) jointPoint.getTarget();
		Integer tenant = system.getCredentials().getTenant();
        MDC.put("service", service.getName() + "-" + tenant );
        logger.info(SERVICE, " START {} ({})", jointPoint.getSignature().getName(),  Arrays.toString(jointPoint.getArgs()));
        
		try {
			Object result = jointPoint.proceed();
			tenant = system.getCredentials().getTenant();
	        MDC.put("service", service.getName() + "-" + tenant );
			if (result != null) {
				logger.info(SERVICE, " DONE {}", jointPoint.getSignature().getName());
				try {
					List list = (List) result;
					logger.info(SERVICE, " RESULT: ");
					list.forEach(e -> logger.info(SERVICE, " - {}", e.toString()) );
				} catch (ClassCastException ex) {
					logger.info(SERVICE, " RESULT {}", result.toString());
				}
			} else {
				logger.info(SERVICE, " DONE  {}", jointPoint.getSignature().getName());
			}
			return result;
		} catch (EconomyException e) {
			tenant = system.getCredentials().getTenant();
	        MDC.put("service", service.getName() + "-" + tenant );
			logger.error(SERVICE, "  => " + e ); 
			throw e;
		} catch (Throwable e) {
			tenant = system.getCredentials().getTenant();
	        MDC.put("service", service.getName() + "-" + tenant );
			e.printStackTrace();
			throw e;
		}

	}
}
