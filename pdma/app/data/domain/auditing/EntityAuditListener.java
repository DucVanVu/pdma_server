package org.pepfar.pdma.app.data.domain.auditing;

import java.time.LocalDateTime;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;

public class EntityAuditListener {

	@PrePersist
	public void beforePersit(AuditableEntity auditableEntity) {

		LocalDateTime now = CommonUtils.hanoiNow();

		auditableEntity.setCreateDate(now);
		auditableEntity.setModifyDate(now);

		User user = SecurityUtils.getCurrentUser();

		if (CommonUtils.isNotNull(user)) {

			auditableEntity.setCreatedBy(user.getUsername());
			auditableEntity.setModifiedBy(user.getUsername());

		} else {
			auditableEntity.setCreatedBy(Constants.USER_ADMIN_USERNAME);
		}
	}

	@PreUpdate
	public void beforeMerge(AuditableEntity auditableEntity) {

		auditableEntity.setModifyDate(CommonUtils.hanoiNow());

		User user = SecurityUtils.getCurrentUser();

		if (CommonUtils.isNotNull(user)) {
			auditableEntity.setModifiedBy(user.getUsername());
		}
	}
}
