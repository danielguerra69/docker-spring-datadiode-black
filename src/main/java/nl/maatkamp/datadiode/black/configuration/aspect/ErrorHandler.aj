package nl.maatkamp.datadiode.black.configuration.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by marcelmaatkamp on 29/09/15.
 */
@Aspect
public aspect ErrorHandler {
    private final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    Object around() : execution(* com.rabbitmq.client.impl..*+.*(..)) {
        log.info("sig: " + thisJoinPoint.getSignature().getName() );

        try {
            return proceed();
        } catch (Exception e) {
            log.info(thisJoinPoint + " -> " + e);
            return null;
        }
    }

}
