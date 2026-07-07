package org.knovash.squeezealice.utils;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
@Log4j2
public class LoggingAspect {

    // Перехватываем все методы в пакете provider (включая статические)
    @Around("@annotation(LogExecution)")

    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println(">>> ASPECT CALLED: " + joinPoint.getSignature());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        long start = System.currentTimeMillis();
        log.info("→ {}.{}() – args: {}", className, methodName, joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();
            log.info("← {}.{}() – возврат: {}, время: {} ms",
                    className, methodName, result, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.error("✗ {}.{}() – исключение: {}, время: {} ms",
                    className, methodName, t.getMessage(), System.currentTimeMillis() - start);
            throw t;
        }
    }
}