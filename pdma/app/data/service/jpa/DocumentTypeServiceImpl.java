package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pepfar.pdma.app.data.domain.DocumentType;
import org.pepfar.pdma.app.data.domain.QDocumentType;
import org.pepfar.pdma.app.data.dto.DocumentTypeDto;
import org.pepfar.pdma.app.data.repository.DocumentTypeRepository;
import org.pepfar.pdma.app.data.service.DocumentTypeService;
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
public class DocumentTypeServiceImpl implements DocumentTypeService {

	@Autowired
	private DocumentTypeRepository repos;

	private Sort defaultSort = new Sort(new Order(Direction.ASC, "active"), new Order(Direction.ASC, "name"));

	@Override
	@Transactional(readOnly = true)
	public DocumentTypeDto findById(Long id) {
		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		DocumentType entity = repos.findOne(id);

		if (entity != null) {
			return new DocumentTypeDto(entity);
		} else {
			return null;
		}
	}

	@Override
	@Transactional()
	public DocumentTypeDto findByCode(String code) {

		if (CommonUtils.isEmpty(code)) {
			return null;
		}

		QDocumentType q = QDocumentType.documentType;
		DocumentType entity = repos.findOne(q.code.equalsIgnoreCase(code));

		if (entity != null) {
			return new DocumentTypeDto(entity);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<DocumentTypeDto> findAll() {
		List<DocumentTypeDto> list = new ArrayList<DocumentTypeDto>();

		QDocumentType q = QDocumentType.documentType;

		repos.findAll(q.active.isTrue(), defaultSort).iterator().forEachRemaining(dt -> {
			list.add(new DocumentTypeDto(dt));
		});

		return list;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DocumentTypeDto> findAll(int pageIndex, int pageSize) {

		if (pageIndex < 0) {
			pageIndex = 0;
		}

		if (pageSize <= 0) {
			pageSize = 25;
		}

		Pageable pageable = new PageRequest(pageIndex, pageSize, defaultSort);

		Page<DocumentType> _page = repos.findAll(pageable);
		List<DocumentTypeDto> content = _page.getContent().stream().map(dt -> new DocumentTypeDto(dt))
				.collect(Collectors.toList());

		return new PageImpl<>(content, pageable, _page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public DocumentTypeDto saveOne(DocumentTypeDto dto) {
		if (CommonUtils.isNull(dto)) {
			throw new RuntimeException();
		}

		DocumentType entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity == null) {
			entity = dto.toEntity();
		} else {
			entity.setCode(dto.getCode());
			entity.setName(dto.getName());
			entity.setActive(dto.getActive());
		}

		entity = repos.save(entity);

		if (entity != null) {
			return new DocumentTypeDto(entity);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(DocumentTypeDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (DocumentTypeDto dto : dtos) {

			DocumentType entity = repos.findOne(dto.getId());

			if (CommonUtils.isNull(entity)) {
				throw new RuntimeException();
			}

			repos.delete(entity);
		}
	}
}
