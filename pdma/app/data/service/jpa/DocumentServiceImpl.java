package org.pepfar.pdma.app.data.service.jpa;

import java.util.ArrayList;
import java.util.List;

import org.pepfar.pdma.app.data.domain.Document;
import org.pepfar.pdma.app.data.domain.DocumentType;
import org.pepfar.pdma.app.data.domain.QDocument;
import org.pepfar.pdma.app.data.dto.DocumentDto;
import org.pepfar.pdma.app.data.dto.DocumentFilterDto;
import org.pepfar.pdma.app.data.repository.DocumentRepository;
import org.pepfar.pdma.app.data.repository.DocumentTypeRepository;
import org.pepfar.pdma.app.data.service.DocumentService;
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

import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class DocumentServiceImpl implements DocumentService {

	@Autowired
	private DocumentRepository repos;

	@Autowired
	private DocumentTypeRepository docTypeRepos;

	private Sort defaultSort = new Sort(new Order(Direction.ASC, "createDate"));

	@Override
	@Transactional(readOnly = true)
	public DocumentDto findById(Long id, boolean includeContent) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		Document entity = repos.findOne(id);

		if (entity != null) {
			return new DocumentDto(entity, includeContent);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DocumentDto> findAll(DocumentFilterDto filter) {

		if (filter == null) {
			filter = new DocumentFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		QDocument q = QDocument.document;
		BooleanExpression be = q.id.isNotNull();

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.title.containsIgnoreCase(filter.getKeyword()));
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(), defaultSort);
		Page<Document> _page = repos.findAll(be, pageable);
		List<DocumentDto> content = new ArrayList<>();
		_page.getContent().parallelStream().forEachOrdered(e -> {
			content.add(new DocumentDto(e, false));
		});

		return new PageImpl<>(content, pageable, _page.getTotalElements());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public DocumentDto saveOne(DocumentDto dto) {

		if (CommonUtils.isNull(dto)) {
			throw new RuntimeException();
		}

		Document entity = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			entity = repos.findOne(dto.getId());
		}

		if (entity != null) {
			entity.setTitle(dto.getTitle());
		} else {
			entity = dto.toEntity();
		}

		// Document type
		DocumentType docType = null;
		if (dto.getDocType() != null && CommonUtils.isPositive(dto.getDocType().getId(), true)) {
			docType = docTypeRepos.findOne(dto.getDocType().getId());
		}

		if (docType == null) {
			throw new RuntimeException();
		}

		entity.setDocType(docType);

		entity = repos.save(entity);

		if (entity != null) {
			return new DocumentDto(entity, false);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(DocumentDto[] dtos) {

		if (CommonUtils.isEmpty(dtos)) {
			return;
		}

		for (DocumentDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			Document entity = repos.findOne(dto.getId());

			if (CommonUtils.isNotNull(entity)) {
				repos.delete(entity);
			}
		}
	}

}
