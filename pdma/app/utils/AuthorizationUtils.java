package org.pepfar.pdma.app.utils;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.pepfar.pdma.app.data.domain.QOrganization;
import org.pepfar.pdma.app.data.domain.QUserOrganization;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.domain.UserOrganization;
import org.pepfar.pdma.app.data.types.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;

@Service
public class AuthorizationUtils {

	@Autowired
	private EntityManager em;

	/**
	 * Get a list of organization IDs that the current user is granted with given
	 * permissions
	 * 
	 * @return
	 */
	public List<Long> getGrantedOrgIds(Permission permission) {
		QOrganization qo = QOrganization.organization;
		QUserOrganization quo = QUserOrganization.userOrganization;

		User user = SecurityUtils.getCurrentUser();

		if (user == null || !CommonUtils.isPositive(user.getId(), true)) {
			return new ArrayList<>();
		}

		// Get all applicable organizations
		List<Long> ids = new ArrayList<>();
		List<Predicate> preds = new ArrayList<>();

		preds.add(quo.user.id.longValue().eq(user.getId()));

		switch (permission) {
			case READ_ACCESS:
				preds.add(quo.readAccess.isTrue());
				break;

			case WRITE_ACCESS:
				preds.add(quo.writeAccess.isTrue());
				break;

			case DELETE_ACCESS:
				preds.add(quo.deleteAccess.isTrue());
				break;

			case PE_ACCESS:
				preds.add(quo.peRole.isTrue());
				break;

			case HTS_ACCESS:
				preds.add(quo.htsRole.isTrue());
				break;

			case PNS_ACCESS:
				preds.add(quo.pnsRole.isTrue());
				break;

			case SNS_ACCESS:
				preds.add(quo.snsRole.isTrue());
				break;

			case SELFTEST_ACCESS:
				preds.add(quo.selfTestRole.isTrue());
				break;

			default:
				break;
		}

		JPAQuery<UserOrganization> query = new JPAQuery<UserOrganization>(em).from(quo).innerJoin(quo.organization, qo);
		query.where(preds.toArray(new Predicate[0]));
		query.orderBy(qo.name.asc());

		List<UserOrganization> uos = query.fetch();

		uos.forEach(uo -> {
			ids.add(uo.getOrganization().getId());
		});

		return ids;
	}

	/**
	 * Get a list of [OPC] organization IDs that the current user is granted with
	 * given permissions
	 * 
	 * @return
	 */
	public List<Long> getGrantedOrgIds(Permission permission, Long provinceId, boolean considerOPCOnly) {
		QOrganization qo = QOrganization.organization;
		QUserOrganization quo = QUserOrganization.userOrganization;

		User user = SecurityUtils.getCurrentUser();

		if (user == null || !CommonUtils.isPositive(user.getId(), true)) {
			return null;
		}

		// Get all applicable organizations
		List<Long> ids = new ArrayList<>();
		List<Predicate> preds = new ArrayList<>();

		preds.add(quo.user.id.longValue().eq(user.getId()));

		if (CommonUtils.isPositive(provinceId, true)) {
			preds.add(qo.address.province.id.eq(provinceId.longValue()));
		}

		if (considerOPCOnly) {
			preds.add(qo.opcSite.isTrue());
		}

		switch (permission) {
			case READ_ACCESS:
				preds.add(quo.readAccess.isTrue());
				break;

			case WRITE_ACCESS:
				preds.add(quo.writeAccess.isTrue());
				break;

			case DELETE_ACCESS:
				preds.add(quo.deleteAccess.isTrue());
				break;

			case PE_ACCESS:
				preds.add(quo.peRole.isTrue());
				break;

			default:
				break;
		}

		JPAQuery<UserOrganization> query = new JPAQuery<UserOrganization>(em).from(quo).innerJoin(quo.organization, qo);
		query.where(preds.toArray(new Predicate[0]));
		query.orderBy(qo.name.asc());

		List<UserOrganization> uos = query.fetch();

		uos.forEach(uo -> {
			ids.add(uo.getOrganization().getId());
		});

		return ids;
	}
}
