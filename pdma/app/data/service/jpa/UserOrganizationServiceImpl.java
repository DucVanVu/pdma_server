package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.QUserOrganization;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.domain.UserOrganization;
import org.pepfar.pdma.app.data.domain.UserOrganizationPK;
import org.pepfar.pdma.app.data.dto.UserOrganizationDto;
import org.pepfar.pdma.app.data.repository.OrganizationRepository;
import org.pepfar.pdma.app.data.repository.UserOrganizationRepository;
import org.pepfar.pdma.app.data.repository.UserRepository;
import org.pepfar.pdma.app.data.service.UserOrganizationService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class UserOrganizationServiceImpl implements UserOrganizationService
{

	@Autowired
	private UserOrganizationRepository repos;

	@Autowired
	private UserRepository userRepos;

	@Autowired
	private OrganizationRepository orgRepos;

	@Override
	@Transactional(readOnly = true)
	public UserOrganizationDto findById(UserOrganizationPK id) {

		if (id == null) {
			return null;
		}

		UserOrganization entity = repos.findOne(id);

		if (entity != null) {
			return new UserOrganizationDto(entity);
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserOrganizationDto> findAll(Long userId) {

		if (!CommonUtils.isPositive(userId, true)) {
			return new ArrayList<>();
		}

		QUserOrganization q = QUserOrganization.userOrganization;
		BooleanExpression be = q.user.id.longValue().eq(userId);

		List<UserOrganizationDto> dtos = new ArrayList<>();
		repos.findAll(be).forEach(e -> {
			dtos.add(new UserOrganizationDto(e));
		});

		return dtos;
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public UserOrganizationDto saveOne(UserOrganizationDto dto) {

		if (dto == null || dto.getUser() == null || dto.getOrganization() == null
				|| !CommonUtils.isPositive(dto.getUser().getId(), true)
						&& !CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
			throw new IllegalArgumentException("Entity to save cannot be null.");
		}

		UserOrganization entity = null;
		if (userRepos.exists(dto.getUser().getId()) && orgRepos.exists(dto.getOrganization().getId())) {

			UserOrganizationPK id = new UserOrganizationPK(dto.getUser().getId(), dto.getOrganization().getId());
			entity = repos.findOne(id);
		}

		if (entity == null) {
			entity = dto.toEntity();
		} else {
			entity.setReadAccess(dto.getReadAccess());
			entity.setWriteAccess(dto.getWriteAccess());
			entity.setDeleteAccess(dto.getDeleteAccess());
			entity.setHtsRole(dto.getHtsRole());
			entity.setPeRole(dto.getPeRole());
			entity.setPnsRole(dto.getPnsRole());
			entity.setSnsRole(dto.getSnsRole());
			entity.setSelfTestRole(dto.getSelfTestRole());
		}

		User user = userRepos.findOne(dto.getUser().getId());
		Organization org = orgRepos.findOne(dto.getOrganization().getId());

		entity.setUser(user);
		entity.setOrganization(org);

		entity = repos.save(entity);

		if (entity != null) {
			return new UserOrganizationDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void saveMultiple(UserOrganizationDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (UserOrganizationDto dto : dtos) {

			if ((dto == null) || (dto.getUser() == null) || (dto.getOrganization() == null)
					|| !CommonUtils.isPositive(dto.getUser().getId(), true)
					|| !CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
				continue;
			}

			UserOrganization entity = null;
			if (userRepos.exists(dto.getUser().getId()) && orgRepos.exists(dto.getOrganization().getId())) {

				UserOrganizationPK id = new UserOrganizationPK(dto.getUser().getId(), dto.getOrganization().getId());
				entity = repos.findOne(id);
			}

			if (entity == null) {
				entity = dto.toEntity();
			} else {
				entity.setReadAccess(true); // by default
				entity.setWriteAccess(dto.getWriteAccess());
				entity.setDeleteAccess(dto.getDeleteAccess());
				entity.setHtsRole(dto.getHtsRole());
				entity.setPeRole(dto.getPeRole());
				entity.setPnsRole(dto.getPnsRole());
				entity.setSnsRole(dto.getSnsRole());
				entity.setSelfTestRole(dto.getSelfTestRole());
			}

			User user = userRepos.findOne(dto.getUser().getId());
			Organization org = orgRepos.findOne(dto.getOrganization().getId());

			entity.setUser(user);
			entity.setOrganization(org);

			entity = repos.save(entity);
		}
	}

	@Override
	@Loggable
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(UserOrganizationDto[] dtos) {
		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (UserOrganizationDto dto : dtos) {
			if (dto == null || dto.getUser() == null || dto.getOrganization() == null
					|| !CommonUtils.isPositive(dto.getUser().getId(), true)
					|| !CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
				continue;
			}

			UserOrganization entity = repos
					.findOne(new UserOrganizationPK(dto.getUser().getId(), dto.getOrganization().getId()));

			if (entity != null) {
				repos.delete(entity);
			}
		}
	}

}
