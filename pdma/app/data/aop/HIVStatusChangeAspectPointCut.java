package org.pepfar.pdma.app.data.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class HIVStatusChangeAspectPointCut
{

//	@AfterReturning(pointcut = "execution()")
	public void updateHIVStatus(JoinPoint joinPoint) {
		// TODO
	}
}
