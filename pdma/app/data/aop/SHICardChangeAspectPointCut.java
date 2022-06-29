package org.pepfar.pdma.app.data.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class SHICardChangeAspectPointCut
{

//	@Autowired
//	private CaseService caseService;

//	@AfterReturning("execution(*)")
	public void shiCardChanged(JoinPoint joinPoint) {
		// TODO
	}
}
