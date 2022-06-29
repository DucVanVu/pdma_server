package org.pepfar.pdma.app.data.service.jpa;

import java.util.List;

import org.pepfar.pdma.app.data.dto.ReleaseHistoryDto;
import org.pepfar.pdma.app.data.service.ReleaseHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReleaseHistoryServiceImpl implements ReleaseHistoryService
{

	@Override
	@Transactional(readOnly = true)
	public ReleaseHistoryDto findById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReleaseHistoryDto> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ReleaseHistoryDto saveOne(ReleaseHistoryDto dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteMultiple(ReleaseHistoryDto[] dtos) {
		// TODO Auto-generated method stub

	}

}
