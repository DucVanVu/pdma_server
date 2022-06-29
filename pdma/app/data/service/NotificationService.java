package org.pepfar.pdma.app.data.service;

import java.util.List;

import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.NotificationDto;
import org.pepfar.pdma.app.data.dto.OPCAssistNotificationData;
import org.pepfar.pdma.app.data.dto.WRCommentFilterDto;
import org.springframework.data.domain.Page;

public interface NotificationService
{

	public NotificationDto findById(Long id);

	public List<NotificationDto> findList(WRCommentFilterDto filter);

	public void markAsRead(User currentUser);

	public long countUnread(User currentUser);

	// find top 5 comments of the current user
	public Page<NotificationDto> findNotificationsPageable(User currentUser);

	public NotificationDto saveOne(NotificationDto dto);

	// broadcast for everyone
	public boolean broadcastAll(NotificationDto dto, User currentUser);

	// role-based broadcast
	public boolean broadcast(NotificationDto dto, User currentUser);

	public void deleteMultiple(NotificationDto[] dtos);
	
	public OPCAssistNotificationData getOPCNotificationData(Long orgId);

}
