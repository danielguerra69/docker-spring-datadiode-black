package nl.maatkamp.datadiode.black.configuration.aspect;

import com.google.gson.Gson;
import com.rabbitmq.client.impl.Frame;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.LoadTimeWeavingConfigurer;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;

/**
 * Created by marcelmaatkamp on 05/03/15.
 */

@Configuration
@Aspect
@EnableAspectJAutoProxy
@EnableLoadTimeWeaving(aspectjWeaving= EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
public class AspectConfiguration implements LoadTimeWeavingConfigurer {

    private final Logger log = LoggerFactory.getLogger(AspectConfiguration.class);

    Gson gson = new Gson();

    @Before("execution(* com.rabbitmq.client.impl..*+.*(..))")
    public void readFrame() {
        log.info("readFrame!");
    }

    @Pointcut("com.rabbitmq.client.impl.FrameHandler.writeFrame() && args(frame,..)")
    public void writeFrame(Frame frame) {
        log.info("writeFrame!");
    }

    @Override
    public LoadTimeWeaver getLoadTimeWeaver() {
        InstrumentationLoadTimeWeaver ltw =  new InstrumentationLoadTimeWeaver ();
        return ltw;
    }


/**
    // IMPL: com.rabbitmq.client.impl.SocketFrameHandler
    // @Pointcut("com.rabbitmq.client.impl.FrameHandler.readFrame()")
    @Before("execution(* *.*(..))")
    public void afterReadFrame(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        log.info("afterReadFrame("+joinPoint.toLongString()+"), signature("+gson.toJson(signature)+")");
    }


    @Before("execution(* com.rabbitmq.client.impl.FrameHandler.readFrame(..))")
    public void afterReadFrame(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        log.info("afterReadFrame("+joinPoint+"), signature("+gson.toJson(signature)+")");
    }

    /*
    @Pointcut("com.rabbitmq.client.impl.FrameHandler.writeFrame() && args(frame,..)")
    public void afterWriteFrame(Frame frame) {
        log.info("afterWriteFrame!");
    }
    */

}
