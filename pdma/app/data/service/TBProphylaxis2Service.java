package org.pepfar.pdma.app.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.pepfar.pdma.app.data.dto.ObjectDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxis2Dto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisDto;
import org.pepfar.pdma.app.data.dto.TBProphylaxisFilterDto;
import org.springframework.data.domain.Page;

public interface TBProphylaxis2Service {
	
	public TBProphylaxis2Dto findById(Long id);
	
	public TBProphylaxis2Dto findLatest(Long caseId);

	public List<TBProphylaxis2Dto> findAll(TBProphylaxisFilterDto filter);
	
	public Page<TBProphylaxis2Dto> findAllPageable(TBProphylaxisFilterDto filter);

	public TBProphylaxis2Dto saveOne(TBProphylaxis2Dto dto);

	public void deleteMultiple(TBProphylaxis2Dto[] dtos);

	public void deletaOneById(Long id);
	//hàm check hoàn thành điều trị + cảnh báo liều lượng để hoàn thành đợt điều trị
	public ObjectDto checkComplete(Long tbProphylaxis2Id,Long tbProphylaxis2DispenseId, Integer dose, LocalDateTime date,boolean isSave);
	//hàm check không hoàn thành điều trị + cảnh báo liều lượng để hoàn thành đợt điều trị
	public ObjectDto checkNotComplete(Long tbProphylaxis2Id,Long tbProphylaxis2DispenseId, Integer dose, LocalDateTime date,boolean isSave);
	// hàm set trạng thái
	public TBProphylaxis2Dto setStatus(Long id);
	//hàm check phác đồ phù hợp với loại  tuổi theo đợt điều trị
	public ObjectDto checkAgeByRegimen(Long caseId,String regimen);
}
