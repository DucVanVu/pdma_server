package org.pepfar.pdma.app.data.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.Role;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.NotificationDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.dto.WeeklyReportDto;
import org.pepfar.pdma.app.data.service.NotificationService;
import org.pepfar.pdma.app.data.service.UserService;
import org.pepfar.pdma.app.data.service.WeeklyReportService;
import org.pepfar.pdma.app.data.types.NotificationSourceType;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class WRChangedPointCut
{

	@Autowired
	private WeeklyReportService wrService;

	@Autowired
	private NotificationService notiService;

	@Autowired
	private UserService uService;

	/**
	 * When a weekly report is approved/returned --> notify provinces/sites
	 * 
	 * @param jointPoint
	 * @throws Throwable
	 */
//	@Around(
//			value = "within(org.pepfar.pdma.app.data.service.jpa.WeeklyReportServiceImpl) && execution(* saveStatus(..))")
	public Object controlStatusChanged_WR(ProceedingJoinPoint joinPoint) throws Throwable {

		WeeklyReportDto dto = null;
		WeeklyReportDto retVal = null;
		WeeklyReportDto originDto = null;

		try {
			// report before updated
			Object[] args = joinPoint.getArgs();
			dto = (WeeklyReportDto) args[0];

			// get the original object
			originDto = wrService.findById(dto.getId());

			// Check the status
			// if national/province returns the report to site -> change the district
			// approval to pending-approval
			if ((originDto.getStatus() > 0) && (dto.getStatus() == 0) && CommonUtils.isTrue(originDto.getDapproved())) {
				dto.setDapproved(false);
				args[0] = dto;
			}

			// proceeding to update
			retVal = (WeeklyReportDto) joinPoint.proceed(args);
		} catch (Exception ex) {
			throw ex;
		}

		if (retVal != null && originDto != null) {
			System.out.println("-- BEGIN: adding notification for report status changed.");

			WeeklyReportDto dtoAfterUpdate = (WeeklyReportDto) retVal;
			if (dtoAfterUpdate.getId() > 0) {

				String modifier = null;
				User curUser = SecurityUtils.getCurrentUser();

				for (Role r : curUser.getRoles()) {

					if (r.getName().equalsIgnoreCase(Constants.ROLE_DISTRICT_MANAGER)) {
						modifier = "tuyến quận/huyện.";
						break;
					}

					if (r.getName().equalsIgnoreCase(Constants.ROLE_PROVINCIAL_MANAGER)) {
						modifier = "tuyến tỉnh/thành phố.";
						break;
					}
				}

				dtoAfterUpdate = wrService.findById(dtoAfterUpdate.getId());
				UserDto creator = uService.findByUsername(dtoAfterUpdate.getCreatedBy());

				if (originDto.getStatus() > 0 && dtoAfterUpdate.getStatus() == 0) {
					NotificationDto notification = new NotificationDto();
					notification.setType(NotificationSourceType.WEEKLY_REPORT_RETURNED);
					notification.setTitle(dtoAfterUpdate.getName() + " vừa được trả lại bởi " + modifier);
					notification.setContent(dtoAfterUpdate.getName() + " vừa được trả lại bởi " + modifier);
					notification.setUser(creator);
					notification.setSeen(Boolean.FALSE);

					notiService.saveOne(notification);
				} else if (originDto.getStatus() < 2 && dtoAfterUpdate.getStatus() == 2) {
					NotificationDto notification = new NotificationDto();
					notification.setType(NotificationSourceType.WEEKLY_REPORT_APPROVED);
					notification.setTitle(dtoAfterUpdate.getName() + " vừa được duyệt bởi " + modifier);
					notification.setContent(dtoAfterUpdate.getName() + " vừa được duyệt bởi " + modifier);
					notification.setUser(creator);
					notification.setSeen(Boolean.FALSE);

					notiService.saveOne(notification);
				}
			}

			System.out.println("-- END: adding notification for report status changed.");
		}

		return retVal;
	}

	/**
	 * When a weekly report is updated by district or provincial users --> notify
	 * sites
	 * 
	 * @param jointPoint
	 * @throws Throwable
	 */
//	@AfterReturning(
//			pointcut = "within(org.pepfar.pdma.app.data.service.jpa.WeeklyReportServiceImpl) && execution(* saveOne(..)) || execution(* saveCases(..)) || execution(* deleteCases(..))",
//			returning = "returnedObj")
	public void afterSavingWeeklyReport(JoinPoint jointPoint, Object returnedObj) throws Throwable {

		// Only consider when the editor has ROLE_PROVINCIAL_MANAGER or
		// ROLE_DISTRICT_MANAGER
		User curUser = SecurityUtils.getCurrentUser();
		if (curUser == null) {
			return;
		}

		boolean foundRole = false;
		for (Role r : curUser.getRoles()) {
			if (r.getName().equalsIgnoreCase(Constants.ROLE_DISTRICT_MANAGER)
					|| r.getName().equalsIgnoreCase(Constants.ROLE_PROVINCIAL_MANAGER)) {
				foundRole = true;
			}
		}

		if (!foundRole) {
			return;
		}

		if (returnedObj == null || !(returnedObj instanceof WeeklyReportDto)) {
			return;
		}

		WeeklyReportDto dto = (WeeklyReportDto) returnedObj;
		if (!CommonUtils.isPositive(dto.getId(), true)) {
			return;
		}

		dto = wrService.findById(dto.getId());
		if (dto == null) {
			return;
		}

		UserDto creator = uService.findByUsername(dto.getCreatedBy());
		if (creator == null) {
			return;
		}

		String modifier = null;
		for (Role r : curUser.getRoles()) {

			if (r.getName().equalsIgnoreCase(Constants.ROLE_DISTRICT_MANAGER)) {
				modifier = "tuyến quận/huyện.";
				break;
			}

			if (r.getName().equalsIgnoreCase(Constants.ROLE_PROVINCIAL_MANAGER)) {
				modifier = "tuyến tỉnh/thành phố.";
				break;
			}
		}

		// updated by another user other than the creator?
		if (!dto.getCreatedBy().equalsIgnoreCase(dto.getModifiedBy())) {
			System.out.println("-- BEGIN: adding notification for modifying a weekly report.");

			NotificationDto notification = new NotificationDto();
			notification.setType(NotificationSourceType.WEEKLY_REPORT_UPDATED);
			notification.setTitle(dto.getName());
			notification.setContent(dto.getName() + " vừa được cập nhật bởi " + modifier);
			notification.setUser(creator);
			notification.setSeen(Boolean.FALSE);

			notiService.saveOne(notification);

			System.out.println("-- END: adding notification for modifying a weekly report.");
		}
	}
}
