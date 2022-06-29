package org.pepfar.pdma.app.data.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ARVTreatmentChangeAspectPointCut
{

//	@AfterReturning(pointcut = "execution()")
	public void arvStart(JoinPoint joinPoint) {
		// TODO
	}

}
