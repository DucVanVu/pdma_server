package org.pepfar.pdma.app.data.aop;

import java.io.Serializable;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SystemLoggingAspectPointCut
{

//	@AfterReturning(
//			pointcut = "@annotation(org.pepfar.pdma.app.data.aop.Loggable) && (execution(* saveOne(..)) || execution(* saveCases(..)))",
//			returning = "returnedObj")
	public void logSaveEntities(JoinPoint joinPoint, Serializable returnedObj) throws Throwable {
		System.out.println(joinPoint.getSignature().getName() + " get called!");
	}

//	@AfterReturning(pointcut = "@annotation(org.pepfar.pdma.app.data.aop.Loggable) && execution(* deleteMultiple(..))")
	public void logDeleteEntities(JoinPoint joinPoint) throws Throwable {
		System.out.println(joinPoint.getSignature().getName() + " get called!");
	}

}