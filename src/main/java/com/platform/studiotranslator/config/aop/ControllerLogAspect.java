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

import java.util.Arrays;

@Aspect
@Component
public class ControllerLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ControllerLogAspect.class);

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerMethods() {}

    @Around("restControllerMethods()")
    public Object logControllerAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        // 1. Log who is trying to access
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null) ? auth.getName() : "Anonymous";
        String roles = (auth != null) ? auth.getAuthorities().toString() : "[NONE]";

        logger.info("==> REQUEST: {}.{}() | User: {} | Roles: {}", className, methodName, user, roles);
        logger.info("--> ARGS: {}", Arrays.toString(args));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            logger.info("<== RESPONSE: {}.{}() | Time: {}ms", className, methodName, executionTime);
            return result;
        } catch (Throwable e) {
            logger.error("X== EXCEPTION in {}.{}(): {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}
