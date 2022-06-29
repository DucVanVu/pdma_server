package org.pepfar.pdma.app.data.service.jpa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.Case;
import org.pepfar.pdma.app.data.domain.Notification;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QAppointment;
import org.pepfar.pdma.app.data.domain.QCase;
import org.pepfar.pdma.app.data.domain.QCaseOrg;
import org.pepfar.pdma.app.data.domain.QNotification;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.NotificationDto;
import org.pepfar.pdma.app.data.dto.OPCAssistNotificationData;
import org.pepfar.pdma.app.data.dto.WRCommentFilterDto;
import org.pepfar.pdma.app.data.repository.AppointmentRepository;
import org.pepfar.pdma.app.data.repository.NotificationRepository;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.UserRepository;
import org.pepfar.pdma.app.data.service.NotificationService;
import org.pepfar.pdma.app.data.types.PatientStatus;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private NotificationRepository repos;

	@Autowired
	private UserRepository uRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Autowired
	private AppointmentRepository appRepos;

	@Autowired
	private EntityManager em;

	@Override
	@Transactional(readOnly = true)
	public NotificationDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Notification entity = repos.findOne(id);

		if (entity != null) {
			return new NotificationDto(entity, null);
		}

		return null;
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void markAsRead(User currentUser) {

		if (currentUser == null || CommonUtils.isEmpty(currentUser.getUsername())) {
			return;
		}

		QNotification q = QNotification.notification;
		Iterator<Notification> entities = repos
				.findAll(q.user.username.equalsIgnoreCase(currentUser.getUsername()).and(q.seen.isFalse())).iterator();

		entities.forEachRemaining(e -> {
			e.setSeen(Boolean.TRUE);
			repos.save(e);
		});
	}

	@Override
	@Transactional(readOnly = true)
	public long countUnread(User currentUser) {

		if (currentUser == null || !CommonUtils.isPositive(currentUser.getId(), true)) {
			return 0l;
		}

		QNotification q = QNotification.notification;
		BooleanExpression be = q.user.id.longValue().eq(currentUser.getId()).and(q.seen.isFalse());

		return repos.count(be);
	}

	/**
	 * If there are less than (5) unread then also get those read ones
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<NotificationDto> findNotificationsPageable(User currentUser) {

		if (currentUser == null || !CommonUtils.isPositive(currentUser.getId(), true)) {
			return new PageImpl<>(new ArrayList<NotificationDto>());
		}

		Pageable pageable = new PageRequest(0, 5, new Sort(Direction.DESC, "createDate"));
		QNotification q = QNotification.notification;
		BooleanExpression be = q.user.id.longValue().eq(currentUser.getId()); // .and(q.seen.isFalse()) <-- comment.

		List<NotificationDto> dtos = new ArrayList<>();
		Page<Notification> page = repos.findAll(be, pageable);

		page.getContent().forEach(e -> {
			dtos.add(new NotificationDto(e, null));
		});

		Page<NotificationDto> pageDtos = new PageImpl<>(dtos, pageable, page.getTotalElements());

		return pageDtos;
	}

	@Override
	@Transactional(readOnly = true)
	public List<NotificationDto> findList(WRCommentFilterDto filter) {

		if (filter == null) {
			filter = new WRCommentFilterDto();
		}

		QNotification q = QNotification.notification;
		BooleanExpression be = q.id.isNotNull();

		if (filter.getReport() != null && CommonUtils.isPositive(filter.getReport().getId(), true)) {
			be = be.and(q.sourceId.longValue().eq(filter.getReport().getId()));
		}

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.content.containsIgnoreCase(filter.getKeyword()));
		}

		List<NotificationDto> dtos = new ArrayList<>();
		repos.findAll(be, q.createDate.desc()).forEach(e -> {
			dtos.add(new NotificationDto(e, null));
		});

		return dtos;
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public NotificationDto saveOne(NotificationDto dto) {
		if (dto == null) {
			throw new RuntimeException("Cannot save a null entity.");
		}

		Notification entity = null;
		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
		} else {
			entity.setTitle(dto.getTitle());
			entity.setContent(dto.getContent());
			entity.setType(dto.getType());
			entity.setSourceId(dto.getSourceId());
			entity.setSeen(dto.getSeen());
		}

		User user = null;
		if (dto.getUser() != null && CommonUtils.isPositive(dto.getUser().getId(), true)) {
			user = uRepos.findOne(dto.getUser().getId());
		}

		entity.setUser(user);

		entity = repos.save(entity);

		if (entity != null) {
			return new NotificationDto(entity, null);
		} else {
			throw new RuntimeException();
		}
	}

	// broadcast for everyone
	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public boolean broadcastAll(NotificationDto dto, User currentUser) {

		return false;
	}

	// role-based broadcast
	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public boolean broadcast(NotificationDto dto, User currentUser) {

		return false;
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(NotificationDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (NotificationDto dto : dtos) {
			Notification entity = null;
			if (dto != null && CommonUtils.isPositive(dto.getId(), true)) {
				entity = repos.findOne(dto.getId());
			}

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public OPCAssistNotificationData getOPCNotificationData(Long orgId) {

		OPCAssistNotificationData data = new OPCAssistNotificationData();
		Organization org = orgRepos.findOne(orgId);

		if (org == null) {
			return data;
		}

		// # of appointments today
		QAppointment qApp = QAppointment.appointment;
		LocalDateTime todayStart = CommonUtils.hanoiTodayStart();
		LocalDateTime todayEnd = CommonUtils.hanoiTodayEnd();

		BooleanExpression be = qApp.organization.id.longValue().eq(orgId);
		be = be.and(qApp.appointmentDate.between(todayStart, todayEnd));

		data.setTodayAppointmentCount(appRepos.count(be));

		// # of pending arrival patients
		QCase qc = QCase.case$;
		QCaseOrg qco = QCaseOrg.caseOrg;

		List<Predicate> predicates = new ArrayList<>();

		predicates.add(qc.deleted.isFalse());
		predicates.add(qco.latestRelationship.isTrue());
		predicates.add(qco.current.isTrue());
		predicates.add(qco.refTrackingOnly.isFalse());
		predicates.add(qco.organization.id.longValue().eq(orgId));
		predicates.add(qco.status.eq(PatientStatus.PENDING_ENROLLMENT));

		JPAQuery<Case> query = new JPAQuery<Case>(em).from(qc).innerJoin(qc.caseOrgs, qco);
		query = query.where(predicates.toArray(new Predicate[0]));

		data.setPendingEnrollmentCount(query.fetchCount());

		return data;
	}
}
