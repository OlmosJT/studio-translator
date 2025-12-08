package com.platform.studiotranslator.config.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
public class ControllerLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLogAspect.class);

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllers() {}

    @Around("restControllers()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {

        // Generate request ID (helps tracking across logs)
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null) ? auth.getName() : "Anonymous";
        String roles = (auth != null)
                ? auth.getAuthorities().stream().map(Object::toString).collect(Collectors.joining(", "))
                : "NONE";

        String args = Stream.of(joinPoint.getArgs())
                .map(arg -> (arg == null) ? "null" : shorten(arg.toString()))
                .collect(Collectors.joining(", "));

        // Log request
        log.info("\n" +
                        "┌─────────────────────────────────────────┐\n" +
                        "│  CONTROLLER REQUEST                     │\n" +
                        "├─────────────────────────────────────────┤\n" +
                        "│ Request ID : {} \n" +
                        "│ Handler    : {}.{}() \n" +
                        "│ User       : {} \n" +
                        "│ Roles      : {} \n" +
                        "│ Args       : [{}] \n" +
                        "└─────────────────────────────────────────┘",
                requestId, className, methodName, user, roles, args
        );

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long time = System.currentTimeMillis() - start;

            log.info("\n" +
                            "┌─────────────────────────────────────────┐\n" +
                            "│  CONTROLLER RESPONSE                    │\n" +
                            "├─────────────────────────────────────────┤\n" +
                            "│ Request ID : {} \n" +
                            "│ Handler    : {}.{}() \n" +
                            "│ Time       : {} ms \n" +
                            "│ Result     : {} \n" +
                            "└─────────────────────────────────────────┘",
                    requestId, className, methodName, time, shorten(result)
            );

            return result;

        } catch (Throwable ex) {
            long time = System.currentTimeMillis() - start;

            log.error("\n" +
                            "┌─────────────────────────────────────────┐\n" +
                            "│  CONTROLLER EXCEPTION                   │\n" +
                            "├─────────────────────────────────────────┤\n" +
                            "│ Request ID : {} \n" +
                            "│ Handler    : {}.{}() \n" +
                            "│ Time       : {} ms \n" +
                            "│ Error      : {} \n" +
                            "└─────────────────────────────────────────┘",
                    requestId, className, methodName, time, ex.toString()
            );

            throw ex;
        }
    }

    /** Prevent logs from exploding. Limit long strings. */
    private String shorten(Object obj) {
        if (obj == null) return "null";
        String s = obj.toString();
        if (s.length() > 300) { // safe limit
            return s.substring(0, 300) + "...(truncated)";
        }
        return s;
    }
}
