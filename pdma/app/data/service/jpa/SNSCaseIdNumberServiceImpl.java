package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pepfar.pdma.app.data.domain.DocumentType;
import org.pepfar.pdma.app.data.domain.QDocumentType;
import org.pepfar.pdma.app.data.domain.QSNSCaseIdNumber;
import org.pepfar.pdma.app.data.domain.SNSCase;
import org.pepfar.pdma.app.data.domain.SNSCaseIdNumber;
import org.pepfar.pdma.app.data.dto.DocumentTypeDto;
import org.pepfar.pdma.app.data.dto.SNSCaseIdNumberDto;
import org.pepfar.pdma.app.data.repository.DocumentTypeRepository;
import org.pepfar.pdma.app.data.repository.SNSCaseIdNumberRepository;
import org.pepfar.pdma.app.data.repository.SNSCaseRepository;
import org.pepfar.pdma.app.data.service.DocumentTypeService;
import org.pepfar.pdma.app.data.service.SNSCaseIdNumberService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SNSCaseIdNumberServiceImpl implements SNSCaseIdNumberService {

	@Autowired
	private SNSCaseIdNumberRepository repos;
	@Autowired
	private SNSCaseRepository snsCaseRepos;

	private Sort defaultSort = new Sort(new Order(Direction.ASC, "primary"), new Order(Direction.ASC, "idNumberType"));

	@Override
	@Transactional(readOnly = true)
	public SNSCaseIdNumberDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		SNSCaseIdNumber entity = repos.findOne(id);

		if (entity != null) {
			return new SNSCaseIdNumberDto(entity);
		} else {
			return null;
		}
	}

	

	@Override
	@Transactional(readOnly = true)
	public List<SNSCaseIdNumberDto> findAll() {
		List<SNSCaseIdNumberDto> list = new ArrayList<SNSCaseIdNumberDto>();

//		QSNSCaseIdNumber q = QSNSCaseIdNumber.snsCaseIdNumber;
//
//		repos.findAll(q.primary, defaultSort).iterator().forEachRemaining(dt -> {
//			list.add(new SNSCaseIdNumberDto(dt));
//		});

		return list;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SNSCaseIdNumberDto> findAll(int pageIndex, int pageSize) {

		if (pageIndex < 0) {
			pageIndex = 0;
		}

		if (pageSize <= 0) {
			pageSize = 25;
		}

		Pageable pageable = new PageRequest(pageIndex, pageSize, defaultSort);

		Page<SNSCaseIdNumber> _page = repos.findAll(pageable);
		List<SNSCaseIdNumberDto> content = _page.getContent().stream().map(dt -> new SNSCaseIdNumberDto(dt))
				.collect(Collectors.toList());

		return new PageImpl<>(content, pageable, _page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public SNSCaseIdNumberDto saveOne(SNSCaseIdNumberDto dto) {
		if (CommonUtils.isNull(dto)) {
			throw new RuntimeException();
		}

		SNSCaseIdNumber entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity=new SNSCaseIdNumber();
//			entity = dto.toEntity();
		}
		entity.setIdNumberType(dto.getIdNumberType());
		entity.setValue(dto.getValue());
		entity.setPrimary(dto.getPrimary());
		if(dto.getSnsCase()!=null && dto.getSnsCase().getId()!=null) {
			SNSCase snscase=snsCaseRepos.findOne(dto.getSnsCase().getId());
			if(snscase!=null) {
				entity.setSnsCase(snscase);
			}
		}
		entity = repos.save(entity);

		if (entity != null) {
			return new SNSCaseIdNumberDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(SNSCaseIdNumberDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (SNSCaseIdNumberDto dto : dtos) {

			SNSCaseIdNumber entity = repos.findOne(dto.getId());

			if (CommonUtils.isNull(entity)) {
				throw new RuntimeException();
			}

			repos.delete(entity);
		}
	}
}
