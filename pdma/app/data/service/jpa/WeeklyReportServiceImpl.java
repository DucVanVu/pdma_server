package org.pepfar.pdma.app.data.service.jpa;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jsoup.Jsoup;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.aop.Loggable;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.LocationDto;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.RoleDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.dto.WRCaseDto;
import org.pepfar.pdma.app.data.dto.WRChartDataDto;
import org.pepfar.pdma.app.data.dto.WRChartFilterDto;
import org.pepfar.pdma.app.data.dto.WRExportExcelFilterDto;
import org.pepfar.pdma.app.data.dto.WRProgressSummaryDetailsDto;
import org.pepfar.pdma.app.data.dto.WRProgressSummaryDto;
import org.pepfar.pdma.app.data.dto.WRProgressSummaryFilterDto;
import org.pepfar.pdma.app.data.dto.WeeklyReportDto;
import org.pepfar.pdma.app.data.dto.WeeklyReportFilterDto;
import org.pepfar.pdma.app.data.repository.*;
import org.pepfar.pdma.app.data.service.WeeklyReportService;
import org.pepfar.pdma.app.data.types.NotificationSourceType;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
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

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class WeeklyReportServiceImpl implements WeeklyReportService {

    @Autowired
    private WeeklyReportRepository repos;

    @Autowired
    private OrganizationRepository orgRepos;

    @Autowired
    private WRCaseRepository wrcaseRepos;

    @Autowired
    private HIVConfirmLabRepository labRepos;

    @Autowired
    private LocationRepository locRepos;

    @Autowired
    private AdminUnitRepository auRepos;

    @Autowired
    private UserOrganizationRepository uoRepos;

    @Autowired
    private WeeklyReportPlainRepository wrpRepos;

    @Autowired
    private SLTargetRepository sltRepos;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private NotificationRepository notiRepos;

    @Autowired
    private CaseOrgRepository coRepos;

    @Override
    @Transactional(readOnly = true)
    public WeeklyReportDto findById(Long id) {

        if (!CommonUtils.isPositive(id, true)) {
            return null;
        }

        WeeklyReport entity = repos.findOne(id);

        if (entity != null) {
            WeeklyReportDto dto = new WeeklyReportDto(entity);

            if (entity.getCases() != null) {
                List<WRCaseDto> dtos = new ArrayList<>();

                entity.getCases().parallelStream().forEachOrdered(e -> {
                    WRCaseDto wrcDto = new WRCaseDto(e);
                    wrcDto.setTransed2OpcAssist(false);

//                    if (CommonUtils.isTrue(e.getLinked2OpcAssist())) {
//                        wrcDto.setTransed2OpcAssist(true);
//                    } else {
                    String patientChartId = e.getPatientChartId();
                    Long orgId = null;

                    if (e.getOpc() != null) {
                        orgId = e.getOpc().getId();
                    }

                    if (!CommonUtils.isEmpty(patientChartId, true) && CommonUtils.isPositive(orgId, true)) {
                        List<CaseOrg> cos = coRepos.checkWeeklyPatientEnrollment(e.getPatientChartId(), e.getOpc().getId());

                        if (cos.size() > 0) {
                            wrcDto.setTransed2OpcAssist(true);
                            wrcDto.setPatientChartIdInOpcAssist(cos.get(0).getPatientChartId());
                        }
                    }
//                    }

                    dtos.add(wrcDto);
                });

                dto.getCases().clear();
                dto.getCases().addAll(dtos);
            }

            return dto;
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyReportDto findByWeekAndOrg(WeeklyReportDto dto) {
        if (dto == null || dto.getFromDate() == null || dto.getOrganization() == null
                || !CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
            return null;
        }

        // @formatter:off
        QWeeklyReport q = QWeeklyReport.weeklyReport;
        BooleanExpression be = q.fromDate.between(dto.getFromDate().minusDays(1), dto.getToDate())
                .and(q.organization.id.longValue().eq(dto.getOrganization().getId()));

        // @formatter:on

        WeeklyReportDto ret = null;
        Iterator<WeeklyReport> itr = repos.findAll(be).iterator();

        if (itr.hasNext()) {
            ret = new WeeklyReportDto(itr.next());
        }

        return ret;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyReportDto> findAll(OrganizationDto[] orgs) {

        QWeeklyReport q = QWeeklyReport.weeklyReport;
        BooleanExpression be = q.id.isNotNull();

        if (!CommonUtils.isEmpty(orgs)) {
            List<Long> ids = new ArrayList<>();
            // @formatter:off
            Stream.of(orgs).parallel()
                    .filter(o -> (o != null) && CommonUtils.isPositive(o.getId(), true))
                    .forEach(o -> {
                        ids.add(o.getId());
                    });
            // @formatter:on

            be = q.organization.id.longValue().in(ids);
        }

        List<WeeklyReportDto> dtos = new ArrayList<>();
        Iterable<WeeklyReport> entities = repos.findAll(be, q.fromDate.desc());
        entities.forEach(wr -> {
            dtos.add(new WeeklyReportDto(wr));
        });

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WeeklyReportDto> findAllPageable(WeeklyReportFilterDto filter) {

        if (filter == null) {
            filter = new WeeklyReportFilterDto();
        }

        if (filter.getPageIndex() < 0) {
            filter.setPageIndex(0);
        }

        if (filter.getPageSize() <= 0) {
            filter.setPageSize(25);
        }

        if (CommonUtils.isEmpty(filter.getOrgs())) {
            OrganizationDto emptyOrg = new OrganizationDto();
            emptyOrg.setId(0l);

            filter.setOrgs(new OrganizationDto[]{emptyOrg});
        }

        QWeeklyReport q = QWeeklyReport.weeklyReport;
        BooleanExpression be = q.id.isNotNull();

        Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
                new Sort(new Order(Direction.DESC, "fromDate")));

        List<Long> ids = new ArrayList<>();
        for (OrganizationDto org : filter.getOrgs()) {
            if (org != null && CommonUtils.isPositive(org.getId(), true)) {
                ids.add(org.getId());
            }
        }
        be = be.and(q.organization.id.longValue().in(ids));

        if (filter.getStatus() >= 0) {
            be = be.and(q.status.intValue().eq(filter.getStatus()));
        }

        switch (filter.getRole()) {
            case Constants.ROLE_USER:
                // fall through
            case Constants.ROLE_ADMIN:
                // fall through
            case Constants.ROLE_DONOR:
                be = be.and(q.status.intValue().eq(3)); // 3 = published
                break;
            case Constants.ROLE_NATIONAL_MANAGER:
                be = be.and(q.status.intValue().goe(2)); // 2 = approved, 3 = published
                break;
            case Constants.ROLE_PROVINCIAL_MANAGER:
                // fall through
            case Constants.ROLE_DISTRICT_MANAGER:
                be = be.and(q.status.intValue().goe(0)); // 0 = drafting (as per Hai Phong request), 1 = pending
                // approval, 2 = approved, 3 = published
                break;
            case Constants.ROLE_SITE_MANAGER:
                break;
            default:
                break;
        }

        if (filter.getDate() != null) {
            be = be.and(q.fromDate.loe(filter.getDate()).and(q.toDate.goe(filter.getDate())));
        }

        List<WeeklyReportDto> dtos = new ArrayList<>();
        Page<WeeklyReport> page = repos.findAll(be, pageable);
        page.getContent().parallelStream().forEachOrdered(wr -> {
            dtos.add(new WeeklyReportDto(wr));
        });

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReportDto saveOne(WeeklyReportDto dto) {

        if (dto == null || dto.getFromDate() == null || dto.getOrganization() == null
                || !CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
            return null;
        }

        // @formatter:off
        QWeeklyReport q = QWeeklyReport.weeklyReport;
        BooleanExpression be = q.fromDate.between(dto.getFromDate().minusDays(1), dto.getToDate())
                .and(q.organization.id.longValue().eq(dto.getOrganization().getId()));
        // @formatter:on
        Iterator<WeeklyReport> itr = repos.findAll(be).iterator();

        WeeklyReport entity = null;

        if (CommonUtils.isPositive(dto.getId(), true)) {
            entity = repos.findOne(dto.getId());
        }

        if (entity == null && itr.hasNext()) {
            entity = itr.next();
        }

        if (entity == null) {
            entity = dto.toEntity();
            entity.setFreezed(false);
            entity.setStatus(0); // default when created to DRAFTING
        } else {
            entity.setFromDate(dto.getFromDate());
            entity.setToDate(dto.getToDate());
            entity.setName(dto.getName());
            entity.setHtsTst(dto.getHtsTst());
            entity.setHtsPos(dto.getHtsPos());
            entity.setTxNew(dto.getTxNew());
            entity.setTxNewBreakdown1(dto.getTxNewBreakdown1());
            entity.setTxNewBreakdown2(dto.getTxNewBreakdown2());
            entity.setNote(dto.getNote());
            entity.setDapproved(dto.isDapproved());
        }

        // Readjust from date/to date
        LocalDateTime tempDate = dto.getFromDate().plusDays(3); // to be safe
        entity.setFromDate(tempDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1L).withHour(0).withMinute(0)
                .withSecond(0).withNano(0));
        entity.setToDate(tempDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7L).withHour(23).withMinute(59)
                .withSecond(59).withNano(0));

        Organization org = null;

        if (dto.getOrganization() != null && CommonUtils.isPositive(dto.getOrganization().getId(), true)) {
            org = orgRepos.findOne(dto.getOrganization().getId());
        }

        entity.setOrganization(org);

        // Only editable when not freezed yet
        if (!entity.isFreezed()) {
            entity = repos.save(entity);
        } else {
            throw new RuntimeException("The report was freezed and could not be edited.");
        }

        if (entity != null) {
            return new WeeklyReportDto(entity);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReportDto saveDApproval(WeeklyReportDto dto) {
        if (dto == null) {
            return null;
        }

        WeeklyReport entity = null;

        if (CommonUtils.isPositive(dto.getId(), true)) {
            entity = repos.findOne(dto.getId());
        }

        if (entity == null) {
            return null;
        }

        entity.setDapproved(dto.isDapproved());

        // Only editable when not freezed yet
        if (!entity.isFreezed()) {
            entity = repos.save(entity);
        } else {
            throw new RuntimeException("The report was freezed and could not be edited.");
        }

        if (entity != null) {
            return new WeeklyReportDto(entity);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReportDto saveCases(WeeklyReportDto dto) {

        if (dto == null || !CommonUtils.isPositive(dto.getId(), true) || CommonUtils.isEmpty(dto.getCases())) {
            throw new IllegalArgumentException();
        }

        WeeklyReport entity = repos.findOne(dto.getId());

        if (entity == null) {
            return null;
        }

        List<WRCase> cases = new ArrayList<>();

        for (WRCaseDto theCase : dto.getCases()) {

            WRCase e = null;
            if (CommonUtils.isPositive(theCase.getId(), true)) {
                e = wrcaseRepos.findOne(theCase.getId());
            }

            if (e == null) {
                e = theCase.toEntity();
                e.setHtsCaseStatus(0);
                e.setTxCaseStatus(0);
            } else {
                e.setFullname(theCase.getFullname());
                e.setGender(theCase.getGender());
                e.setDob(theCase.getDob());
                e.setNationalId(theCase.getNationalId());
                e.setHivConfirmId(theCase.getHivConfirmId());
                e.setScreeningDate(theCase.getScreeningDate());
                e.setScreeningSite(theCase.getScreeningSite());
                e.setConfirmDate(theCase.getConfirmDate());
                e.setEnrollmentDate(theCase.getEnrollmentDate());
                e.setArvInitiationDate(theCase.getArvInitiationDate());
                e.setPatientChartId(theCase.getPatientChartId());
                e.setHtsCaseStatus(theCase.getHtsCaseStatus());
                e.setTxCaseStatus(theCase.getTxCaseStatus());
                e.setNote(theCase.getNote());

                e.setRtriPos(theCase.getRtriPos());
                e.setOfferedPns(theCase.getOfferedPns());
            }

            Organization vct = null;
            Organization opc = null;
            HIVConfirmLab lab = null;

            if (theCase.getVct() != null && CommonUtils.isPositive(theCase.getVct().getId(), true)) {
                vct = orgRepos.findOne(theCase.getVct().getId());
            }

            if (theCase.getOpc() != null && CommonUtils.isPositive(theCase.getOpc().getId(), true)) {
                opc = orgRepos.findOne(theCase.getOpc().getId());
            }

            if (theCase.getConfirmLab() != null && CommonUtils.isPositive(theCase.getConfirmLab().getId(), true)) {
                lab = labRepos.findOne(theCase.getConfirmLab().getId());
            }

            // Locations
            List<Location> locations = new ArrayList<>();
            for (LocationDto loc : theCase.getLocations()) {
                Location address = null;

                if (CommonUtils.isPositive(loc.getId(), true)) {
                    address = locRepos.findOne(loc.getId());
                }

                if (address == null && CommonUtils.isPositive(theCase.getId(), true) && loc.getAddressType() != null) {
                    List<Location> locs = locRepos.findForWRCase(theCase.getId(), loc.getAddressType());

                    if (locs != null && locs.size() > 0) {
                        address = locs.get(0);
                    }
                }

                if (address == null) {
                    address = new Location();
                }

                address.setAddressType(loc.getAddressType());
                address.setStreetAddress(loc.getStreetAddress());
                address.setAccuracy(loc.getAccuracy());
                address.setLatitude(loc.getLatitude());
                address.setLongitude(loc.getLongitude());

                AdminUnit commune = null;
                AdminUnit district = null;
                AdminUnit province = null;
                AdminUnit country = null;

                if (loc.getCommune() != null && CommonUtils.isPositive(loc.getCommune().getId(), true)) {
                    commune = auRepos.findOne(loc.getCommune().getId());
                }

                if (loc.getDistrict() != null && CommonUtils.isPositive(loc.getDistrict().getId(), true)) {
                    district = auRepos.findOne(loc.getDistrict().getId());
                }

                if (loc.getProvince() != null && CommonUtils.isPositive(loc.getProvince().getId(), true)) {
                    province = auRepos.findOne(loc.getProvince().getId());
                }

                if (loc.getCountry() != null) {
                    if (CommonUtils.isPositive(loc.getCountry().getId(), true)) {
                        country = auRepos.findOne(loc.getCountry().getId());
                    }
                    if (country == null && !CommonUtils.isEmpty(loc.getCountry().getCode())) {
                        country = auRepos
                                .findOne(QAdminUnit.adminUnit.code.equalsIgnoreCase(loc.getCountry().getCode()));
                    }
                }

                address.setCommune(commune);
                address.setDistrict(district);
                address.setProvince(province);
                address.setCountry(country);
                address.setWrCase(e);

                locations.add(address);
            }

            e.getLocations().clear();
            e.getLocations().addAll(locations);

            e.setVct(vct);
            e.setOpc(opc);
            e.setConfirmLab(lab);

            cases.add(e);
        }

        entity.getCases().clear();
        entity.getCases().addAll(cases);

        entity = repos.save(entity);

        if (entity != null) {
            return new WeeklyReportDto(entity);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReportDto deleteCases(WeeklyReportDto dto, boolean deletePositiveCase) {

        if (dto == null || !CommonUtils.isPositive(dto.getId(), true) || CommonUtils.isEmpty(dto.getCases())) {
            throw new IllegalArgumentException();
        }

        WeeklyReport entity = repos.findOne(dto.getId());

        if (entity == null) {
            return null;
        }

        boolean updateReportNeeded = false;

        for (WRCaseDto p : dto.getCases()) {

            WRCase e = null;
            if (CommonUtils.isPositive(p.getId(), true)) {
                e = wrcaseRepos.findOne(p.getId());
            }

            if (e != null) {

                if (entity.getCases().contains(e)) {
                    entity.getCases().remove(e);
                }

                boolean sameReport = false;

                if (deletePositiveCase && e.getVct() != null) {
                    if (e.getOpc() != null && CommonUtils.isPositive(e.getOpc().getId(), true)
                            && e.getVct().getId().longValue() == e.getOpc().getId()) {

                        sameReport = true;
                        if (!updateReportNeeded) {
                            updateReportNeeded = true;
                        }

                        e.setVct(null);
                        e.setScreeningDate(null);

                        if (e.getReports().size() > 1) {
                            sameReport = false;
                        }
                    } else {
                        wrcaseRepos.delete(e);
                    }

                    // Delete treatment case
                } else if (e.getOpc() != null) {
                    if (e.getVct() != null && CommonUtils.isPositive(e.getVct().getId(), true)) {
                        sameReport = true;
                        if (!updateReportNeeded) {
                            updateReportNeeded = true;
                        }

                        e.setOpc(null);
                        e.setEnrollmentDate(null);
                        e.setArvInitiationDate(null);
                        e.setPatientChartId(null);

                        if (e.getReports().size() > 1) {
                            sameReport = false;
                        }
                    } else {
                        wrcaseRepos.delete(e);
                    }
                }

                if (sameReport) {
                    entity.getCases().add(e);
                }
            }
        }

        if (updateReportNeeded) {
            entity = repos.save(entity);
        } else {
            entity = repos.findOne(entity.getId());
        }

        return new WeeklyReportDto(entity);
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReportDto saveStatus(WeeklyReportDto dto) {

        if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
            throw new IllegalArgumentException("Cannot save a null WeeklyReportDto instance!");
        }

        WeeklyReport entity = repos.findOne(dto.getId());
        WeeklyReportDto origin = new WeeklyReportDto(entity);

        if (entity != null) {

            if ((entity.getStatus() > 0) && (dto.getStatus() == 0) && CommonUtils.isTrue(entity.getDapproved())) {
                entity.setDapproved(false);
            }

            // Comment when the report is returned from higher level.
            // When re-submit the report, comment is set to NULL
            if (origin.getStatus() > dto.getStatus() && !CommonUtils.isEmpty(dto.getComment())) {
                entity.setComment(dto.getComment());
            } else {
                entity.setComment(null);
            }

            switch (dto.getStatus()) {
                case 0:
                    // returned to site
                    entity.setSubmissionDate(null);
                    entity.setApprovalDate(null);
                    entity.setPublishDate(null);

                    break;

                case 1:
                    // submitted

                    // check logical between total and list of cases
//                    List<WRCase> cases = Lists.newArrayList(entity.getCases());
//                    List<WRCase> htcCases =
//                            cases.parallelStream().filter(c -> c.getVct() != null).collect(Collectors.toList());
//                    List<WRCase> opcCases =
//                            cases.parallelStream().filter(c -> c.getOpc() != null).collect(Collectors.toList());

//                    if (entity.getHtsPos() != htcCases.size()) {
//                        throw new RuntimeException("Error comparing the total # of cases delcared and the actual " +
//                                "records.");
//                    }

                    entity.setSubmissionDate(CommonUtils.hanoiNow());
                    entity.setApprovalDate(null);
                    entity.setPublishDate(null);
                    break;

                case 2:
                    entity.setApprovalDate(CommonUtils.hanoiNow());
                    entity.setPublishDate(null);
                    break;

                case 3:
                    // published
                    entity.setPublishDate(CommonUtils.hanoiNow());
                    break;

                default:
                    break;
            }

            entity.setStatus(dto.getStatus());
            entity = repos.save(entity);
        }

        if (entity != null) {

            // Save notification
            System.out.println("-- BEGIN: adding notification for report status changed.");
            if (entity.getId() > 0) {

                String modifier = null;
                User curUser = SecurityUtils.getCurrentUser();

                for (Role r : curUser.getRoles()) {

                    if (r.getName().equalsIgnoreCase(Constants.ROLE_DISTRICT_MANAGER)) {
                        modifier = "tuyến quận/huyện.";
                        break;
                    }

                    if (r.getName().equalsIgnoreCase(Constants.ROLE_PROVINCIAL_MANAGER)) {
                        modifier = "tuyến tỉnh/thành phố.";
                        break;
                    }
                }

                User creator = userRepos.findOne(QUser.user.username.equalsIgnoreCase(entity.getCreatedBy()));

                if (!CommonUtils.isEmpty(modifier)) {
                    if (origin.getStatus() > 0 && entity.getStatus() == 0) {
                        Notification notification = new Notification();
                        notification.setType(NotificationSourceType.WEEKLY_REPORT_RETURNED);
                        notification.setTitle(entity.getName() + " vừa được trả lại bởi " + modifier);
                        notification.setContent(entity.getName() + " vừa được trả lại bởi " + modifier);
                        notification.setUser(creator);
                        notification.setSeen(Boolean.FALSE);

                        notiRepos.save(notification);
                    } else if (origin.getStatus() < 2 && entity.getStatus() == 2) {
                        Notification notification = new Notification();
                        notification.setType(NotificationSourceType.WEEKLY_REPORT_APPROVED);
                        notification.setTitle(entity.getName() + " vừa được duyệt bởi " + modifier);
                        notification.setContent(entity.getName() + " vừa được duyệt bởi " + modifier);
                        notification.setUser(creator);
                        notification.setSeen(Boolean.FALSE);

                        notiRepos.save(notification);
                    }
                }
            }

            return new WeeklyReportDto(entity);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public void synthesize(boolean syncAll) {
        QWeeklyReport q = QWeeklyReport.weeklyReport;
        BooleanExpression be = q.status.eq(3); // only published ones

        if (!syncAll) {
            LocalDateTime right = CommonUtils.hanoiTodayStart().minusMonths(2);
            be = be.and(q.fromDate.goe(right));
        }

        Iterator<WeeklyReport> entities = repos.findAll(be).iterator();

        QWeeklyReportPlain q2 = QWeeklyReportPlain.weeklyReportPlain;

        while (entities.hasNext()) {
            final WeeklyReport r = entities.next();
            Set<WRCase> cases = r.getCases();

            int htsPos = (int) cases.parallelStream().filter(c -> (c.getVct() != null))
                    .filter(c -> c.getConfirmDate() != null
                            && (c.getConfirmDate().isAfter(r.getFromDate())
                            || c.getConfirmDate().isEqual(r.getFromDate()))
                            && (c.getConfirmDate().isBefore(r.getToDate())
                            || c.getConfirmDate().isEqual(r.getToDate())))
                    .count();

            int htsPosBreakdown1 = (int) cases.parallelStream().filter(c -> (c.getVct() != null))
                    .filter(c -> c.getConfirmDate() != null
                            && (c.getConfirmDate().isAfter(r.getFromDate())
                            || c.getConfirmDate().isEqual(r.getFromDate()))
                            && (c.getConfirmDate().isBefore(r.getToDate()) || c.getConfirmDate().isEqual(r.getToDate()))
                            && c.getHtsCaseStatus() == 1)
                    .count();

            int htsPosBreakdown2 = (int) cases.parallelStream().filter(c -> (c.getVct() != null))
                    .filter(c -> c.getConfirmDate() != null
                            && (c.getConfirmDate().isAfter(r.getFromDate())
                            || c.getConfirmDate().isEqual(r.getFromDate()))
                            && (c.getConfirmDate().isBefore(r.getToDate()) || c.getConfirmDate().isEqual(r.getToDate()))
                            && c.getHtsCaseStatus() == 2)
                    .count();

            int htsPosBreakdown3 = (int) cases.parallelStream().filter(c -> (c.getVct() != null))
                    .filter(c -> c.getConfirmDate() != null
                            && (c.getConfirmDate().isAfter(r.getFromDate())
                            || c.getConfirmDate().isEqual(r.getFromDate()))
                            && (c.getConfirmDate().isBefore(r.getToDate()) || c.getConfirmDate().isEqual(r.getToDate()))
                            && c.getHtsCaseStatus() == 4)
                    .count();

            int htsPosBreakdown4 = (int) cases.parallelStream().filter(c -> (c.getVct() != null))
                    .filter(c -> c.getConfirmDate() != null
                            && (c.getConfirmDate().isAfter(r.getFromDate())
                            || c.getConfirmDate().isEqual(r.getFromDate()))
                            && (c.getConfirmDate().isBefore(r.getToDate()) || c.getConfirmDate().isEqual(r.getToDate()))
                            && c.getHtsCaseStatus() == 0)
                    .count();

            int htsPosBreakdown5 = (int) cases.parallelStream().filter(c -> (c.getVct() != null))
                    .filter(c -> c.getConfirmDate() != null
                            && (c.getConfirmDate().isAfter(r.getFromDate())
                            || c.getConfirmDate().isEqual(r.getFromDate()))
                            && (c.getConfirmDate().isBefore(r.getToDate()) || c.getConfirmDate().isEqual(r.getToDate()))
                            && c.getRtriPos() == 1)
                    .count();

            int htsPosBreakdown6 = (int) cases.parallelStream().filter(c -> (c.getVct() != null))
                    .filter(c -> c.getConfirmDate() != null
                            && (c.getConfirmDate().isAfter(r.getFromDate())
                            || c.getConfirmDate().isEqual(r.getFromDate()))
                            && (c.getConfirmDate().isBefore(r.getToDate()) || c.getConfirmDate().isEqual(r.getToDate()))
                            && c.getOfferedPns() == 1)
                    .count();

            int txNew = (int) cases.parallelStream().filter(c -> c.getOpc() != null
                    && c.getOpc().getId() == r.getOrganization().getId() && c.getEnrollmentDate() != null
                    && (c.getEnrollmentDate().isAfter(r.getFromDate())
                    || c.getEnrollmentDate().isEqual(r.getFromDate()))
                    && (c.getEnrollmentDate().isBefore(r.getToDate()) || c.getEnrollmentDate().isEqual(r.getToDate())))
                    .count();

            int txNewBreakdown1 = (int) cases.parallelStream().filter(c -> c.getOpc() != null
                    && c.getOpc().getId() == r.getOrganization().getId() && c.getEnrollmentDate() != null
                    && (c.getEnrollmentDate().isAfter(r.getFromDate())
                    || c.getEnrollmentDate().isEqual(r.getFromDate()))
                    && (c.getEnrollmentDate().isBefore(r.getToDate()) || c.getEnrollmentDate().isEqual(r.getToDate()))
                    && c.getTxCaseStatus() == 1).count();

            int txNewBreakdown2 = (int) cases.parallelStream().filter(c -> c.getOpc() != null
                    && c.getOpc().getId() == r.getOrganization().getId() && c.getEnrollmentDate() != null
                    && (c.getEnrollmentDate().isAfter(r.getFromDate())
                    || c.getEnrollmentDate().isEqual(r.getFromDate()))
                    && (c.getEnrollmentDate().isBefore(r.getToDate()) || c.getEnrollmentDate().isEqual(r.getToDate()))
                    && c.getTxCaseStatus() == 2).count();

            int txNewBreakdown3 = (int) cases.parallelStream()
                    .filter(c -> c.getOpc() != null && c.getOpc().getId().longValue() == r.getOrganization().getId())
                    .filter(c -> c.getArvInitiationDate() != null && c.getConfirmDate() != null)
                    .filter(c -> (c.getConfirmDate().isAfter(r.getFromDate())
                            || c.getEnrollmentDate().isEqual(r.getFromDate()))
                            && (c.getConfirmDate().isBefore(r.getToDate())
                            || c.getEnrollmentDate().isEqual(r.getToDate())))
                    .filter(c -> c.getArvInitiationDate().toLocalDate().isEqual(c.getConfirmDate().toLocalDate()))
                    .count();

            int txNewBreakdown4 = (int) cases.parallelStream().filter(c -> c.getOpc() != null
                    && c.getOpc().getId() == r.getOrganization().getId() && c.getEnrollmentDate() != null
                    && (c.getEnrollmentDate().isAfter(r.getFromDate())
                    || c.getEnrollmentDate().isEqual(r.getFromDate()))
                    && (c.getEnrollmentDate().isBefore(r.getToDate()) || c.getEnrollmentDate().isEqual(r.getToDate()))
                    && c.getTxCaseStatus() == 3).count();

            int txNewBreakdown5 = (int) cases.parallelStream().filter(c -> c.getOpc() != null
                    && c.getOpc().getId() == r.getOrganization().getId() && c.getEnrollmentDate() != null
                    && (c.getEnrollmentDate().isAfter(r.getFromDate())
                    || c.getEnrollmentDate().isEqual(r.getFromDate()))
                    && (c.getEnrollmentDate().isBefore(r.getToDate()) || c.getEnrollmentDate().isEqual(r.getToDate()))
                    && c.getTxCaseStatus() == 4).count();

            int txNewBreakdown6 = (int) cases.parallelStream().filter(c -> c.getOpc() != null
                    && c.getOpc().getId() == r.getOrganization().getId() && c.getEnrollmentDate() != null
                    && (c.getEnrollmentDate().isAfter(r.getFromDate())
                    || c.getEnrollmentDate().isEqual(r.getFromDate()))
                    && (c.getEnrollmentDate().isBefore(r.getToDate()) || c.getEnrollmentDate().isEqual(r.getToDate()))
                    && c.getTxCaseStatus() == 0).count();

//			System.out.println("--> htsPos: " + htsPos);
//			System.out.println("--> txNew: " + txNew);
//			System.out.println("--> txNewBreakdown1: " + txNewBreakdown1);
//			System.out.println("--> txNewBreakdown2: " + txNewBreakdown2);
//			System.out.println("--> txNewBreakdown3: " + txNewBreakdown3);

            r.setHtsPos(htsPos);
            r.setTxNew(txNew);
            r.setTxNewBreakdown1(txNewBreakdown1);
            r.setTxNewBreakdown2(txNewBreakdown2);
            r.setTxNewBreakdown3(txNewBreakdown3);

            WeeklyReport r2 = repos.save(r);

            if (r2 != null) {
                // Update the plain table for charts/graphs
                Organization organization = orgRepos.findOne(r2.getOrganization().getId());
                WeeklyReportPlain wrp = wrpRepos.findOne(q2.report.id.longValue().eq(r2.getId()));

                if (wrp == null) {
                    wrp = new WeeklyReportPlain();
                }

                wrp.setReport(r2);
                wrp.setOrganization(organization);
                wrp.setFromDate(r2.getFromDate());
                wrp.setToDate(r2.getToDate());
                wrp.setHtsTst(r2.getHtsTst());

                wrp.setHtsPos(htsPos);
                wrp.setHtsPosBreakdown1(htsPosBreakdown1);
                wrp.setHtsPosBreakdown2(htsPosBreakdown2);
                wrp.setHtsPosBreakdown3(htsPosBreakdown3);
                wrp.setHtsPosBreakdown4(htsPosBreakdown4);
                wrp.setHtsPosBreakdown5(htsPosBreakdown5);
                wrp.setHtsPosBreakdown6(htsPosBreakdown6);

                wrp.setTxNew(txNew);
                wrp.setTxNewBreakdown1(txNewBreakdown1);
                wrp.setTxNewBreakdown2(txNewBreakdown2);
                wrp.setTxNewBreakdown3(txNewBreakdown3);
                wrp.setTxNewBreakdown4(txNewBreakdown4);
                wrp.setTxNewBreakdown5(txNewBreakdown5);
                wrp.setTxNewBreakdown6(txNewBreakdown6);

                wrpRepos.save(wrp);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WRProgressSummaryDto findProgressSummary(final WRProgressSummaryFilterDto filter) {

        if (filter.getPageIndex() < 0) {
            filter.setPageIndex(0);
        }

        if (filter.getPageSize() <= 0) {
            filter.setPageSize(25);
        }

        if (filter.getDateOfWeek() == null) {
            filter.setDateOfWeek(CommonUtils.hanoiTodayStart());
        }

        QUser qUser = QUser.user;
        QOrganization qOrg = QOrganization.organization;

        // Get all applicable organizations
        List<Long> ids = new ArrayList<>();
        Iterable<UserOrganization> ous = uoRepos.findAll(qUser.id.longValue().eq(filter.getUser().getId()));
        ous.forEach(uo -> {
            ids.add(uo.getOrganization().getId());
        });

        if (filter.getProvince() != null && CommonUtils.isPositive(filter.getProvince().getId(), true)) {

            ids.clear();
            ous = uoRepos.findAll(qUser.id.longValue().eq(filter.getUser().getId())
                    .and(qOrg.address.isNotNull().and(qOrg.address.province.isNotNull()
                            .and(qOrg.address.province.id.longValue().eq(filter.getProvince().getId())))));
            ous.forEach(uo -> {
                ids.add(uo.getOrganization().getId());
            });
        }

        QOrganization q1 = QOrganization.organization;
        BooleanExpression be = q1.id.longValue().in(ids).and(q1.active.isTrue());

        PageRequest pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
                new Sort(Direction.ASC, "name"));
        Page<Organization> page = orgRepos.findAll(be, pageable);
        List<Organization> orgs = Lists.newArrayList(orgRepos.findAll(be)); // for counting summary

        // Get all reports available
        QWeeklyReport q2 = QWeeklyReport.weeklyReport;
        be = q2.organization.id.longValue().in(ids).and(q2.organization.active.isTrue())
                .and(q2.fromDate.loe(filter.getDateOfWeek())).and(q2.toDate.goe(filter.getDateOfWeek()));

        Iterable<WeeklyReport> wrs = repos.findAll(be);

        // Create DTOs
        LocalDateTime tempDate = filter.getDateOfWeek();
        List<WRProgressSummaryDetailsDto> dtos = new ArrayList<>();

        // No status selected...
        if (filter.getStatus() < -1) {
            for (Organization org : page.getContent()) {
                int reportStatus = -1;
                LocalDateTime submissionDate = null;
                LocalDateTime approvalDate = null;
                LocalDateTime publishDate = null;

                for (WeeklyReport r : wrs) {
                    if (org.getId().longValue() == r.getOrganization().getId()) {
                        reportStatus = r.getStatus();
                        submissionDate = r.getSubmissionDate();
                        approvalDate = r.getApprovalDate();
                        publishDate = r.getPublishDate();

                        break;
                    }
                }

                WRProgressSummaryDetailsDto dto = new WRProgressSummaryDetailsDto();
                dto.setName(org.getName());
                dto.setProvince(org.getAddress().getProvince().getName());
                dto.setDistrict(org.getAddress().getDistrict().getName());
                dto.setStatus(reportStatus);
                dto.setFromDate(tempDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1L).withHour(0).withMinute(0)
                        .withSecond(0).withNano(0));
                dto.setToDate(tempDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7L).withHour(23).withMinute(59)
                        .withSecond(59));
                dto.setSubmissionDate(submissionDate);
                dto.setApprovalDate(approvalDate);
                dto.setPublishDate(publishDate);

                dtos.add(dto);
            }
        }

        // Count summary/results data
        long totalElements = 0;
        long runningIndex = 0;
        long startIndex = filter.getPageIndex() * filter.getPageSize();
        final WRProgressSummaryDto progress = new WRProgressSummaryDto();

        for (Organization org : orgs) {
            int reportStatus = -1;
            LocalDateTime submissionDate = null;
            LocalDateTime approvalDate = null;
            LocalDateTime publishDate = null;

            for (WeeklyReport r : wrs) {
                if (org.getId().longValue() == r.getOrganization().getId()) {
                    reportStatus = r.getStatus();
                    submissionDate = r.getSubmissionDate();
                    approvalDate = r.getApprovalDate();
                    publishDate = r.getPublishDate();

                    // count results
                    if (filter.getStatus() >= -1) {
                        if (filter.getStatus() == reportStatus) {
                            progress.getResults()[0] += r.getHtsTst();
                            progress.getResults()[1] += r.getHtsPos();
                            progress.getResults()[2] += r.getTxNew();
                        }
                    } else {
                        progress.getResults()[0] += r.getHtsTst();
                        progress.getResults()[1] += r.getHtsPos();
                        progress.getResults()[2] += r.getTxNew();
                    }
                }
            }

            // One specific report status selected
            if (filter.getStatus() >= -1) {
                if (filter.getStatus() == reportStatus) {
                    if (runningIndex >= startIndex) {
                        if (dtos.size() < filter.getPageSize()) {
                            WRProgressSummaryDetailsDto dto = new WRProgressSummaryDetailsDto();
                            dto.setName(org.getName());
                            dto.setProvince(org.getAddress().getProvince().getName());
                            dto.setDistrict(org.getAddress().getDistrict().getName());
                            dto.setStatus(reportStatus);
                            dto.setFromDate(tempDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1L).withHour(0)
                                    .withMinute(0).withSecond(0).withNano(0));
                            dto.setToDate(tempDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7L).withHour(23)
                                    .withMinute(59).withSecond(59));

                            dto.setSubmissionDate(submissionDate);
                            dto.setApprovalDate(approvalDate);
                            dto.setPublishDate(publishDate);

                            dtos.add(dto);
                        }
                    }

                    totalElements++;
                    runningIndex++;

                    progress.getSummary()[reportStatus + 1]++;
                }
            } else {
                progress.getSummary()[reportStatus + 1]++;
            }
        }

        if (filter.getStatus() < -1) {
            totalElements = page.getTotalElements();
        }

        progress.setDetails(new PageImpl<>(dtos, pageable, totalElements));

        return progress;
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public void adjustReportingPeriod() {

        repos.findAll().forEach(r -> {
            // Readjust from date/to date
            LocalDateTime tempDate = r.getFromDate().plusDays(3); // to be safe

            r.setFromDate(tempDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1L).withHour(0).withMinute(0)
                    .withSecond(0).withNano(0));
            r.setToDate(tempDate.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7L).withHour(23).withMinute(59)
                    .withSecond(59));

            repos.save(r);
        });

    }

    @Override
    @Transactional(readOnly = true)
    public WRChartDataDto getChartData(WRChartFilterDto filter) {

        WRChartDataDto data = new WRChartDataDto();

        if (filter == null || filter.getUser() == null) {
            return data;
        }

        if (filter.getToDate() == null) {
            LocalDateTime endOfLastWeek = CommonUtils
                    .dateEnd(CommonUtils.hanoiNow().minusWeeks(1).with(DayOfWeek.SUNDAY));
            filter.setToDate(endOfLastWeek);
        }

        // Get from-date based on to-date
        LocalDateTime fromDate = CommonUtils.dateStart(filter.getToDate().minusWeeks(8).with(DayOfWeek.MONDAY));

//		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
//		System.out.println("-----");
//		System.out.println(sdf.format(CommonUtils.fromLocalDateTime(fromDate)));
//		System.out.println(sdf.format(CommonUtils.fromLocalDateTime(filter.getToDate())));
//		System.out.println("-----");

        data.setFromDate(fromDate);
        data.setToDate(filter.getToDate());

        // Filter by status and date range
        QWeeklyReportPlain q = QWeeklyReportPlain.weeklyReportPlain;
        BooleanExpression be = q.fromDate.goe(fromDate).and(q.toDate.loe(filter.getToDate()));

        QSLTarget q2 = QSLTarget.sLTarget;
        BooleanExpression be2 = q2.id.isNotNull();

        QUser qUser = QUser.user;
        QOrganization qOrg = QOrganization.organization;

        BooleanExpression be3 = q.id.isNotNull();

        // Filter by organizations/provinces
        List<Long> ids = new ArrayList<>();
        Iterable<UserOrganization> ous = uoRepos.findAll(qUser.id.longValue().eq(filter.getUser().getId()));
        ous.forEach(uo -> {
            ids.add(uo.getOrganization().getId());
        });

        be = be.and(q.organization.id.longValue().in(ids));
        be3 = be3.and(q.organization.id.longValue().in(ids));
        be2 = be2.and(q2.site.id.longValue().in(ids));

        if (filter.getOrg() != null && CommonUtils.isPositive(filter.getOrg().getId(), true)) {

            ids.clear();
            ids.add(filter.getOrg().getId());

            be = be.and(q.organization.id.longValue().eq(filter.getOrg().getId()));
            be3 = be3.and(q.organization.id.longValue().eq(filter.getOrg().getId()));
            be2 = be2.and(q2.site.id.longValue().eq(filter.getOrg().getId()));
        } else if (filter.getProvince() != null && CommonUtils.isPositive(filter.getProvince().getId(), true)) {

            ids.clear();
            ous = uoRepos.findAll(qUser.id.longValue().eq(filter.getUser().getId())
                    .and(qOrg.address.isNotNull().and(qOrg.address.province.isNotNull()
                            .and(qOrg.address.province.id.longValue().eq(filter.getProvince().getId())))));
            ous.forEach(uo -> {
                ids.add(uo.getOrganization().getId());
            });

            be = be.and(q.organization.id.longValue().in(ids));
            be3 = be3.and(q.organization.id.longValue().in(ids));
            be2 = be2.and(q2.site.id.longValue().in(ids));
        }

        // Get 8-week period data lists
        List<WeeklyReportPlain> reports = Lists
                .newArrayList(wrpRepos.findAll(be, new Sort(new Order(Direction.ASC, "fromDate"))));

        // Get supported data
        LocalDateTime[][] _8weeks = get8Weeks(filter.getToDate());

        for (int i = 0; i < 8; i++) {
            LocalDateTime[] week = _8weeks[i];
            final int j = i;

            reports.parallelStream().forEachOrdered(r -> {
                if (isReportInDateRange(r, week)) {
                    // Get data for that specific week
                    data.getHtsTstData()[j] += (long) r.getHtsTst();
                    data.getHtsPosData()[j] += (long) r.getHtsPos();
                    data.getTxNewData()[j] += (long) r.getTxNew();
                    data.getTxNewNDiagData()[j] += (long) r.getTxNewBreakdown1();
                    data.getTxNewODiagData()[j] += (long) r.getTxNewBreakdown2();
                    data.getTxNewOLTFData()[j] += (long) r.getTxNewBreakdown4();
                    data.getTxNewOProvData()[j] += (long) r.getTxNewBreakdown5();
                    data.getTxNewIndeterminate()[j] += (long) r.getTxNewBreakdown6();
                    data.getTxNewSameDayData()[j] += (long) r.getTxNewBreakdown3();

                    data.getPosNewData()[j] += (long) r.getHtsPosBreakdown1();
                    data.getPosOldData()[j] += (long) r.getHtsPosBreakdown2();
                    data.getPosOProvData()[j] += (long) r.getHtsPosBreakdown3();
                    data.getPosIndeterminate()[j] += (long) r.getHtsPosBreakdown4();
                    data.getPosRtriPos()[j] += (long) r.getHtsPosBreakdown5();
                    data.getPosOfferedPns()[j] += (long) r.getHtsPosBreakdown6();
                }
            });
        }

        // Positive rate calculation
        for (int i = 0; i < 8; i++) {
            if (data.getHtsTstData()[i] > 0) {
                data.getPosRate()[i] = (double) data.getHtsPosData()[i] / data.getHtsTstData()[i];
            }
        }

        // Get targets data
        List<SLTarget> targets = Lists.newArrayList(sltRepos.findAll(be2));
        targets.parallelStream().forEachOrdered(slt -> {
            switch (slt.getIndicator()) {
                case HTS_TST:
                    data.setHtsTstAnnTarget(data.getHtsTstAnnTarget() + slt.getTarget());
                    break;
                case HTS_POS:
                    data.setHtsPosAnnTarget(data.getHtsPosAnnTarget() + slt.getTarget());
                    break;
                case TX_NEW:
                    data.setTxNewAnnTarget(data.getTxNewAnnTarget() + slt.getTarget());
                    break;
            }
        });

        // Get 1 year period data lists
        LocalDateTime toDate3 = null;
        LocalDateTime fromDate3 = null;

        int selMonth = filter.getToDate().getMonthValue();
        if (selMonth >= 1 && selMonth <= 9) {
            toDate3 = filter.getToDate().withMonth(9).withDayOfMonth(30).withHour(23).withMinute(59).withSecond(59)
                    .withNano(0);
            fromDate3 = filter.getToDate().minusYears(1).withMonth(10).withDayOfMonth(1).withHour(0).withMinute(0)
                    .withSecond(0).withNano(0);
        } else {
            toDate3 = filter.getToDate().plusYears(1).withMonth(9).withDayOfMonth(30).withHour(23).withMinute(59)
                    .withSecond(59).withNano(0);
            fromDate3 = filter.getToDate().withMonth(10).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
                    .withNano(0);
        }

        be3 = be3.and(q.fromDate.goe(fromDate3)).and(q.toDate.loe(toDate3));
        reports = Lists.newArrayList(wrpRepos.findAll(be3, new Sort(new Order(Direction.ASC, "fromDate"))));

        // Get supported data
        LocalDateTime[][] _12months = get12Months(filter.getToDate().getMonthValue(), filter.getToDate().getYear());

        for (int i = 0; i < 12; i++) {
            final int j = i;
            LocalDateTime[] month = _12months[i];

            reports.parallelStream().forEachOrdered(r -> {
                if (isReportInDateRange(r, month)) {
                    data.getHtsTstCumData()[j] += (long) r.getHtsTst();
                    data.getHtsPosCumData()[j] += (long) r.getHtsPos();
                    data.getTxNewCumData()[j] += (long) r.getTxNew();
                }
            });

        }

        // Cumulative calculation
        for (int i = 1; i < 12; i++) {
            data.getHtsTstCumData()[i] += data.getHtsTstCumData()[i - 1];
            data.getHtsPosCumData()[i] += data.getHtsPosCumData()[i - 1];
            data.getTxNewCumData()[i] += data.getTxNewCumData()[i - 1];
        }

        // Summary
        QWeeklyReport q3 = QWeeklyReport.weeklyReport;
        be = q3.organization.id.longValue().in(ids);

        data.getSummary()[0] = (int) repos.count(be.and(q3.status.eq(0)));
        data.getSummary()[1] = (int) repos.count(be.and(q3.status.eq(1)));
        data.getSummary()[2] = (int) repos.count(be.and(q3.status.eq(2)));
        data.getSummary()[3] = (int) repos.count(be.and(q3.status.eq(3)));

        // Last update
        LocalDateTime yesterday = CommonUtils.hanoiTodayStart().minusMinutes(1);
        data.setLastUpdate(yesterday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public Workbook createExcelFile(final WRExportExcelFilterDto filter) {
        List<WeeklyReportDto> dtos = new ArrayList<>();

        UserDto user = filter.getUser();
        boolean confidentialRequired = false;
        for (RoleDto r : user.getRoles()) {
            if (r.getName().equalsIgnoreCase(Constants.ROLE_ADMIN) || r.getName().equalsIgnoreCase(Constants.ROLE_DONOR)
                    || r.getName().equalsIgnoreCase(Constants.ROLE_NATIONAL_MANAGER)) {
                confidentialRequired = true;
            }
        }

        Iterable<UserOrganization> ous = uoRepos.findAll(QUser.user.id.longValue().eq(user.getId()));

        if (filter.getReport() != null && repos.exists(filter.getReport().getId())) {
            // Single report
            OrganizationDto org = filter.getReport().getOrganization();

            ous.forEach(uo -> {
                if (org != null && CommonUtils.isPositive(org.getId(), true)) {
                    if (org.getId().longValue() == uo.getOrganization().getId()) {
                        dtos.add(filter.getReport());
                    }
                }
            });
        } else {
            // Multiple reports
            QWeeklyReport q = QWeeklyReport.weeklyReport;
            BooleanExpression be = q.id.isNotNull();

            List<Long> ids = new ArrayList<>();
            ous.forEach(uo -> {
                ids.add(uo.getOrganization().getId());
            });

            be = be.and(q.organization.id.longValue().in(ids));

            if (SecurityUtils.isUserInRole(user, Constants.ROLE_DONOR)) {
                be = be.and(q.status.eq(3));
            } else if (SecurityUtils.isUserInRole(user, Constants.ROLE_NATIONAL_MANAGER)) {
                be = be.and(q.status.goe(2));
            } else if (SecurityUtils.isUserInRole(user, Constants.ROLE_PROVINCIAL_MANAGER)) {
                be = be.and(q.status.goe(1));
            } else if (SecurityUtils.isUserInRole(user, Constants.ROLE_SITE_MANAGER)) {
                be = be.and(q.status.goe(0));
            }

            if (filter.getOrg() != null && CommonUtils.isPositive(filter.getOrg().getId(), true)) {
                be = be.and(q.organization.id.longValue().eq(filter.getOrg().getId()));
            } else if (filter.getProvince() != null && CommonUtils.isPositive(filter.getProvince().getId(), true)) {
                be = be.and(q.organization.address.province.id.longValue().eq(filter.getProvince().getId()));
            }

            LocalDateTime fromDate = filter.getFromDate().with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1L).withHour(0)
                    .withMinute(0).withSecond(0).withNano(0);
            LocalDateTime toDate = filter.getToDate().with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7L).withHour(23)
                    .withMinute(59).withSecond(59);

            be = be.and(q.fromDate.goe(fromDate)).and(q.toDate.loe(toDate));

            repos.findAll(be, q.fromDate.desc()).forEach(r -> {
                dtos.add(new WeeklyReportDto(r));
            });
        }

        return createExcelWorkbook(dtos, confidentialRequired);
    }

    @Override
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public void deleteMultiple(WeeklyReportDto[] dtos) {
        if (CommonUtils.isEmpty(dtos)) {
            return;
        }

        QWeeklyReportPlain q = QWeeklyReportPlain.weeklyReportPlain;

        for (WeeklyReportDto dto : dtos) {
            if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
                continue;
            }

            WeeklyReport entity = repos.findOne(dto.getId());

            if (entity != null) {

                // first, remove relationships to cases that relates to another report
                boolean updateReportNeeded = false;
                List<WRCase> cases = Lists.newArrayList(entity.getCases());
                for (WRCase c : cases) {

                    c = wrcaseRepos.findOne(c.getId());
                    if (c.getReports().size() > 1) {
                        entity.getCases().remove(c);
                        updateReportNeeded = true;

                        // update the case: remove OPC/VCT data accordingly
                        if (c.getConfirmDate().isBefore(entity.getFromDate())) {
                            // the entity has the Case as a treatment case
                            c.setOpc(null);
                            c.setEnrollmentDate(null);
                            c.setArvInitiationDate(null);
                            c.setPatientChartId(null);
                        } else {
                            // the entity has the Case as a positive case
                            c.setVct(null);
                            c.setScreeningDate(null);
                        }

                        wrcaseRepos.save(c);
                    }
                }

                if (updateReportNeeded) {
                    entity = repos.save(entity);
                }

                // remove the weekly report plain record
                WeeklyReportPlain wrp = wrpRepos.findOne(q.report.id.longValue().eq(entity.getId()));
                if (wrp != null) {
                    wrpRepos.delete(wrp); // auto delete the report
                } else {
                    repos.delete(entity);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanDemoData() {
        for (int i = 0; i < 40; i++) {
            User user = userRepos.findOne(QUser.user.username.equalsIgnoreCase("demo_user_" + (i + 1)));

            if (user == null) {
                continue;
            }

            Iterable<WeeklyReport> reports = repos
                    .findAll(QWeeklyReport.weeklyReport.createdBy.equalsIgnoreCase(user.getUsername()));

            QWeeklyReportPlain q = QWeeklyReportPlain.weeklyReportPlain;

            for (WeeklyReport r : reports) {
                // first, remove relationships to cases that relates to another report
                boolean updateReportNeeded = false;
                List<WRCase> cases = Lists.newArrayList(r.getCases());
                for (WRCase c : cases) {

                    c = wrcaseRepos.findOne(c.getId());
                    if (c.getReports().size() > 1) {
                        r.getCases().remove(c);
                        updateReportNeeded = true;

                        // update the case: remove OPC/VCT data accordingly
                        if (c.getConfirmDate().isBefore(r.getFromDate())) {
                            // the entity has the Case as a treatment case
                            c.setOpc(null);
                            c.setEnrollmentDate(null);
                            c.setArvInitiationDate(null);
                            c.setPatientChartId(null);
                        } else {
                            // the entity has the Case as a positive case
                            c.setVct(null);
                            c.setScreeningDate(null);
                        }

                        wrcaseRepos.save(c);
                    }
                }

                if (updateReportNeeded) {
                    r = repos.save(r);
                }

                // remove the weekly report plain record
                WeeklyReportPlain wrp = wrpRepos.findOne(q.report.id.longValue().eq(r.getId()));
                if (wrp != null) {
                    wrpRepos.delete(wrp); // auto delete the report
                } else {
                    repos.delete(r);
                }
            }
        }
    }

    // Private methods

    /**
     * Create an output of type Excel for a single weekly report
     *
     * @param filter
     * @return
     */
    private Workbook createExcelWorkbook(List<WeeklyReportDto> dtos, boolean confidentialRequired) {

        Workbook wbook = new SXSSFWorkbook(100);
        Sheet summarySheet = wbook.createSheet("Chỉ số báo cáo tuần");
        Sheet posSheet = wbook.createSheet("DS KH dương tính");
        Sheet txnewSheet = wbook.createSheet("DS điều trị mới");

        List<WeeklyReport> reports = new ArrayList<>();
        dtos.parallelStream().forEachOrdered(r -> {
            WeeklyReport entity = repos.findOne(r.getId());
            reports.add(entity);
        });

        if (reports.size() <= 0) {
            return wbook;
        }

        createSummarySheet(reports, summarySheet, wbook);
        createHtsPosSheet(reports, posSheet, wbook, confidentialRequired);
        createTxNewSheet(reports, txnewSheet, wbook, confidentialRequired);

        return wbook;
    }

    private void createSummarySheet(List<WeeklyReport> reports, Sheet summarySheet, Workbook wbook) {
        // Summary sheet - Title
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();
        CellStyle dateCellStyle = wbook.createCellStyle();

        int rowIndex = 0;
        Row row = summarySheet.createRow(rowIndex);
        row.setHeightInPoints(27);

        font.setFontName("Times New Roman");
        font.setBold(true);
        font.setFontHeightInPoints((short) 15);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Cell cell = row.createCell(1, CellType.STRING);
        cell.setCellValue("BÁO CÁO TUẦN CHƯƠNG TRÌNH PEPFAR");
        cell.setCellStyle(cellStyle);

        // Summary Sheet - Sub title
        font = wbook.createFont();
        cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        if (reports.size() == 1) {
            rowIndex++;
            row = summarySheet.createRow(rowIndex);
            row.setHeightInPoints(27);

            cell = row.createCell(1, CellType.STRING);
            cell.setCellValue(getReportSubtitle(reports.get(0)));
            cell.setCellStyle(cellStyle);
        }

        // Column widths
        int[] colWidths = {21, 146, 146, 236, 117, 280, 131, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
                200, 200, 400};
        for (int i = 0; i < colWidths.length; i++) {
            summarySheet.setColumnWidth(i, ExcelUtils.pixel2WidthUnits(colWidths[i]));
        }

        // Summary sheet - Table header
        font = wbook.createFont();
        cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        rowIndex++;
        row = summarySheet.createRow(rowIndex);
        row.setHeightInPoints(22);

        // 1. Province
        cell = row.createCell(1, CellType.STRING);
        cell.setCellValue("Tỉnh");
        summarySheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, 1, 1));
        cell.setCellStyle(cellStyle);

        // 2. District
        cell = row.createCell(2, CellType.STRING);
        cell.setCellValue("Huyện");
        summarySheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, 2, 2));
        cell.setCellStyle(cellStyle);

        // 3. Site
        cell = row.createCell(3, CellType.STRING);
        cell.setCellValue("Cơ sở thực hiện");
        summarySheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, 3, 3));
        cell.setCellStyle(cellStyle);

        // 4. Date of report
        cell = row.createCell(4, CellType.STRING);
        cell.setCellValue("Ngày báo cáo");
        summarySheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, 4, 4));
        cell.setCellStyle(cellStyle);

        // 5. Report for week
        cell = row.createCell(5, CellType.STRING);
        cell.setCellValue("Báo cáo của tuần");
        summarySheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, 5, 5));
        cell.setCellStyle(cellStyle);

        // 6. # of tests in week
        cell = row.createCell(6, CellType.STRING);
        cell.setCellValue("Số XN HIV trong tuần/HTS_TST");
        summarySheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, 6, 6));
        cell.setCellStyle(cellStyle);

        cell = row.createCell(20, CellType.STRING);
        cell.setCellValue("Ghi chú");
        summarySheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, 20, 20));
        cell.setCellStyle(cellStyle);

        // count[0] = HTS_POS, count[1] = HTS_POS: new, count[2] = HTS_POS: old,
        // count[3] = HTS_POS: ngoai tinh, count[4]= HTS_POS chua xac dinh
        // count[5] = TX_NEW, count[6] = TX_NEW new, count[7] = TX_NEW old chua DT,
        // count[8] = TX_NEW old bo tri
        // count[9] = TX_NEW ngoai tinh, count[10] = TX_NEW chua xac dinh

        // 7. # of confirmed positives in week
        cell = row.createCell(7, CellType.STRING);
        cell.setCellValue("Số BN XN khẳng định dương tính trong tuần/HTS_POS");
        summarySheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 7, 13));
        cell.setCellStyle(cellStyle);

        // 8. # of TX_NEW
        cell = row.createCell(14, CellType.STRING);
        cell.setCellValue("Số người bắt đầu điều trị ARV trong tuần báo cáo (TX_NEW)");
        summarySheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 14, 19));
        cell.setCellStyle(cellStyle);

        rowIndex++;
        row = summarySheet.createRow(rowIndex);
        row.setHeightInPoints(22);

        int colIndex = 7;

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("Tổng số HTS_POS");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("HTS_POS: Chẩn đoán mới");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("HTS_POS: Chẩn đoán cũ");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("HTS_POS: Ngoại tỉnh");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("HTS_POS: Chưa xác định");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("HTS_POS: S.lọc nhiễm mới (+)");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("HTS_POS: Được tư vấn XNBTBC");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("Tổng số TX_NEW");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("TX_NEW: Điều trị mới");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("TX_NEW: Cũ chưa điều trị");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("TX_NEW: Cũ bỏ trị");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("TX_NEW: Ngoại tỉnh");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(colIndex++, CellType.STRING);
        cell.setCellValue("TX_NEW: Chưa xác định");
        cell.setCellStyle(cellStyle);

        String[] arr = "B,C,D,E,F,G".split(",");
        for (int i = 0; i < arr.length; i++) {
            ExcelUtils.setBorders4Region(arr[i] + rowIndex + ":" + arr[i] + (rowIndex + 1), summarySheet);
        }
        ExcelUtils.setBorders4Region("H" + rowIndex + ":N" + rowIndex, summarySheet);
        arr = "H,I,J,K,L,M,N".split(",");
        for (int i = 0; i < arr.length; i++) {
            ExcelUtils.setBorders4Region(arr[i] + (rowIndex + 1) + ":" + arr[i] + (rowIndex + 1), summarySheet);
        }
        ExcelUtils.setBorders4Region("O" + rowIndex + ":T" + rowIndex, summarySheet);
        arr = "O,P,Q,R,S,T".split(",");
        for (int i = 0; i < arr.length; i++) {
            ExcelUtils.setBorders4Region(arr[i] + (rowIndex + 1) + ":" + arr[i] + (rowIndex + 1), summarySheet);
        }

        // ghi chu column header
        ExcelUtils.setBorders4Region("U" + rowIndex + ":U" + (rowIndex + 1), summarySheet);

        // Index row for filtering
        rowIndex++;
        row = summarySheet.createRow(rowIndex);
        row.setHeightInPoints(22);

        ExcelUtils.setBorders4Style(cellStyle);

        for (int i = 1; i <= 20; i++) {
            cell = row.createCell(i, CellType.STRING);
            cell.setCellValue("( " + i + " )");
            cell.setCellStyle(cellStyle);
        }

        // Summary sheet - Table content
        font = wbook.createFont();
        cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle cellStyleAlt = wbook.createCellStyle();
        cellStyleAlt.cloneStyleFrom(cellStyle);
        cellStyleAlt.setAlignment(HorizontalAlignment.LEFT);
        cellStyleAlt.setIndention((short) 1);

        for (WeeklyReport report : reports) {
            rowIndex++;
            row = summarySheet.createRow(rowIndex);
            row.setHeightInPoints(22);

            Location siteAddress = report.getOrganization().getAddress();
            colIndex = 1;

            // Province
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyleAlt);
            if (siteAddress != null && siteAddress.getProvince() != null) {
                cell.setCellValue(report.getOrganization().getAddress().getProvince().getName());
            }

            // District
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellStyle(cellStyleAlt);
            if (siteAddress != null && siteAddress.getDistrict() != null) {
                cell.setCellValue(report.getOrganization().getAddress().getDistrict().getName());
            }

            // Site name
            cell = row.createCell(colIndex++, CellType.STRING);
            cell.setCellValue(report.getOrganization().getName());
            cell.setCellStyle(cellStyleAlt);

            // Report date
            dateCellStyle.cloneStyleFrom(cellStyle);
            DataFormat format = wbook.createDataFormat();
            dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(CommonUtils.fromLocalDateTime(report.getCreateDate()));
            cell.setCellStyle(dateCellStyle);

            // Report for week
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(getWeekofReport(report));
            cell.setCellStyle(cellStyle);

            // HTS_TST
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(report.getHtsTst());
            cell.setCellStyle(cellStyle);

            long[] caseCounts = countReportedCases(report);

            // HTS_POS
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[0]);
            cell.setCellStyle(cellStyle);

            // HTS_POS: chan doan moi
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[1]);
            cell.setCellStyle(cellStyle);

            // HTS_POS: chan doan cu
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[2]);
            cell.setCellStyle(cellStyle);

            // HTS_POS: ngoai tinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[3]);
            cell.setCellStyle(cellStyle);

            // HTS_POS: chua xac dinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[4]);
            cell.setCellStyle(cellStyle);

            // HTS_POS: RTRI+
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[5]);
            cell.setCellStyle(cellStyle);

            // HTS_POS: OFFERED PNS
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[6]);
            cell.setCellStyle(cellStyle);

            // TX_NEW
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[7]);
            cell.setCellStyle(cellStyle);

            // TX_NEW: Tx new
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[8]);
            cell.setCellStyle(cellStyle);

            // TX_NEW: old chua dieu tri
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[9]);
            cell.setCellStyle(cellStyle);

            // TX_NEW: old bo tri
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[10]);
            cell.setCellStyle(cellStyle);

            // TX_NEW: ngoai tinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[11]);
            cell.setCellStyle(cellStyle);

            // TX_NEW: chua xac dinh
            cell = row.createCell(colIndex++, CellType.NUMERIC);
            cell.setCellValue(caseCounts[12]);
            cell.setCellStyle(cellStyle);

            // Note
            cell = row.createCell(colIndex++, CellType.STRING);
            if (!CommonUtils.isEmpty(report.getNote())) {
                cell.setCellValue(Jsoup.parse(report.getNote()).text());
            } else {
                cell.setCellValue("-");
            }
            cell.setCellStyle(cellStyle);

        }

        int startIndex = 4;

        if (reports.size() == 1) {
            startIndex++;
        }

        summarySheet.setAutoFilter(CellRangeAddress.valueOf("B" + startIndex + ":R" + rowIndex));
    }

    private void createHtsPosSheet(List<WeeklyReport> reports, Sheet htsPosSheet, Workbook wbook,
                                   boolean confidentialRequired) {
        // HtsPos sheet - Title
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();
        CellStyle dateCellStyle = wbook.createCellStyle();
        CellStyle dateCellStyleAlt = wbook.createCellStyle();

        int rowIndex = 0;
        Row row = htsPosSheet.createRow(rowIndex);
        row.setHeightInPoints(33);

        font.setFontName("Times New Roman");
        font.setBold(true);
        font.setFontHeightInPoints((short) 15);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Cell cell = row.createCell(1, CellType.STRING);
        cell.setCellValue("BÁO CÁO DANH SÁCH KHÁCH HÀNG XÉT NGHIỆM HIV DƯƠNG TÍNH");
        cell.setCellStyle(cellStyle);

        // Column widths
        int[] colWidths = {21, 80, 50, 240, 178, 178, 125, 280, 125, 163, 125, 80, 158, 176, 176, 176, 176, 176, 176,
                176, 176, 160, 160, 285, 150, 200, 200, 285};
        for (int i = 0; i < colWidths.length; i++) {
            htsPosSheet.setColumnWidth(i, ExcelUtils.pixel2WidthUnits(colWidths[i]));
        }

        // HtsPos sheet - Table header
        font = wbook.createFont();
        cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        rowIndex++;
        row = htsPosSheet.createRow(rowIndex);
        row.setHeightInPoints(28);

        String[] arr = ("STT,PK,Tên cơ sở xét nghiệm,Huyện/Quận,Tỉnh/Thành phố,Ngày báo cáo,Báo cáo của tuần,Mã XNKĐ " +
                "HIV+,Họ và tên khách hàng,Ngày tháng năm sinh,Giới tính,Số CMND (nếu có)")
                .split(",");

        for (int i = 0; i < arr.length; i++) {
            cell = row.createCell(i + 1, CellType.STRING);
            cell.setCellValue(arr[i]);
            htsPosSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, i + 1, i + 1));
            cell.setCellStyle(cellStyle);
        }

        int colIndex = arr.length + 1;

        cell = row.createCell(colIndex, CellType.STRING);
        cell.setCellValue("Địa chỉ hộ khẩu");
        htsPosSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, colIndex, colIndex + 3));
        cell.setCellStyle(cellStyle);

        colIndex += 4;

        cell = row.createCell(colIndex, CellType.STRING);
        cell.setCellValue("Địa chỉ nơi ở hiện tại");
        htsPosSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, colIndex, colIndex + 3));
        cell.setCellStyle(cellStyle);

        colIndex += 4;
        String[] arr2 = ("Ngày làm XN sàng lọc,Ngày khẳng định HIV+,Nơi khẳng định HIV+,Dương tính cũ - mới,Kết quả " +
                "sàng lọc nhiễm mới,Tư vấn XNBTBC,Ghi chú")
                .split(",");

        for (int i = 0; i < arr2.length; i++) {
            cell = row.createCell(colIndex + i, CellType.STRING);
            cell.setCellValue(arr2[i]);
            htsPosSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, colIndex + i, colIndex + i));
            cell.setCellStyle(cellStyle);
        }

        rowIndex++;
        row = htsPosSheet.createRow(rowIndex);
        row.setHeightInPoints(28);

        arr2 = "Thôn bản/số nhà,Xã/phường,Huyện/quận,Tỉnh/thành phố,Thôn bản/số nhà,Xã/phường,Huyện/quận,Tỉnh/thành phố"
                .split(",");

        for (int i = 0; i < arr2.length; i++) {
            cell = row.createCell(arr.length + 1 + i, CellType.STRING);
            cell.setCellValue(arr2[i]);
            cell.setCellStyle(cellStyle);
        }

        // Bordering for merged cells
        arr = "B,C,D,E,F,G,H,I,J,K,L,U,V,W,X,Y,Z,AA,AB".split(",");
        for (int i = 0; i < arr.length; i++) {
            ExcelUtils.setBorders4Region(arr[i] + rowIndex + ":" + arr[i] + (rowIndex + 1), htsPosSheet);
        }

        ExcelUtils.setBorders4Region("N" + rowIndex + ":Q" + rowIndex, htsPosSheet);
        ExcelUtils.setBorders4Region("R" + rowIndex + ":U" + rowIndex, htsPosSheet);

        arr = "M,N,O,P,Q,R,S,T".split(",");
        for (int i = 0; i < arr.length; i++) {
            ExcelUtils.setBorders4Region(arr[i] + (rowIndex + 1) + ":" + arr[i] + (rowIndex + 1), htsPosSheet);
        }

        // Index row for filtering
        rowIndex++;
        row = htsPosSheet.createRow(rowIndex);
        row.setHeightInPoints(22);

        ExcelUtils.setBorders4Style(cellStyle);

        for (int i = 1; i <= 27; i++) {
            cell = row.createCell(i, CellType.STRING);
            cell.setCellValue("( " + i + " )");
            cell.setCellStyle(cellStyle);
        }

        // HtsPos sheet - Table content
        font = wbook.createFont();
        cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle cellStyleAlt = wbook.createCellStyle();
        cellStyleAlt.cloneStyleFrom(cellStyle);
        cellStyleAlt.setAlignment(HorizontalAlignment.LEFT);
        cellStyleAlt.setIndention((short) 1);

        dateCellStyle.cloneStyleFrom(cellStyle);
        dateCellStyleAlt.cloneStyleFrom(cellStyle);

        DataFormat format = wbook.createDataFormat();
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
        dateCellStyleAlt.setDataFormat(format.getFormat("dd/MM/yyyy hh:mm AM/PM"));

        for (WeeklyReport report : reports) {
            Iterator<WRCase> cases = report.getCases().iterator();

            while (cases.hasNext()) {
                WRCase c = cases.next();
                if (c.getVct() != null && c.getConfirmDate() != null && c.getConfirmDate().isAfter(report.getFromDate())
                        && c.getConfirmDate().isBefore(report.getToDate())) {

                    rowIndex++;
                    row = htsPosSheet.createRow(rowIndex);
                    row.setHeightInPoints(22);

                    Location siteAddress = report.getOrganization().getAddress();

                    colIndex = 1;

                    // STT
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(rowIndex - 3);
                    cell.setCellStyle(cellStyle);

                    // Case primary key
                    cell = row.createCell(colIndex++, CellType.NUMERIC);
                    cell.setCellValue(c.getId());
                    cell.setCellStyle(cellStyle);

                    // Site name
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(report.getOrganization().getName());
                    cell.setCellStyle(cellStyleAlt);

                    // District
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyleAlt);
                    if (siteAddress != null && siteAddress.getDistrict() != null) {
                        cell.setCellValue(report.getOrganization().getAddress().getDistrict().getName());
                    }

                    // Province
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyleAlt);
                    if (siteAddress != null && siteAddress.getProvince() != null) {
                        cell.setCellValue(report.getOrganization().getAddress().getProvince().getName());
                    }

                    // Report date
                    cell = row.createCell(colIndex++, CellType.NUMERIC);
                    cell.setCellValue(CommonUtils.fromLocalDateTime(report.getCreateDate()));
                    cell.setCellStyle(dateCellStyle);

                    // Report of week
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(getWeekofReport(report));
                    cell.setCellStyle(cellStyleAlt);

                    // Client HIV confirm code
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(c.getHivConfirmId());
                    cell.setCellStyle(cellStyleAlt);

                    // Client name
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(confidentialRequired ? "-" : c.getFullname());
                    cell.setCellStyle(cellStyleAlt);

                    // DOB
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(CommonUtils.fromLocalDateTime(c.getDob()));
                    cell.setCellStyle(dateCellStyle);

                    // Gender
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(c.getGender().toString());
                    cell.setCellStyle(cellStyleAlt);

                    // National ID
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(confidentialRequired ? "-" : c.getNationalId());
                    cell.setCellStyle(cellStyleAlt);

                    // Addresses
                    Location rAddress = null;
                    Location cAddress = null;

                    Set<Location> locs = c.getLocations();
                    for (Location loc : locs) {
                        if (loc == null) {
                            continue;
                        }

                        switch (loc.getAddressType()) {
                            case RESIDENT_ADDRESS:
                                rAddress = loc;
                                break;
                            case CURRENT_ADDRESS:
                                cAddress = loc;
                                break;
                            default:
                                break;
                        }
                    }

//					Location rAddress = c.getResidentialAddress();
//					Location cAddress = c.getCurrentAddress();

                    if (rAddress != null) {
                        // R address - details
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellValue(confidentialRequired ? "-" : rAddress.getStreetAddress());
                        cell.setCellStyle(cellStyleAlt);

                        // R address - commune
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (rAddress.getCommune() != null) {
                            cell.setCellValue(confidentialRequired ? "-" : rAddress.getCommune().getName());
                        }

                        // R address - district
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (rAddress.getDistrict() != null) {
                            cell.setCellValue(confidentialRequired ? "-" : rAddress.getDistrict().getName());
                        }

                        // R address - province
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (rAddress.getProvince() != null) {
                            cell.setCellValue(rAddress.getProvince().getName());
                        }
                    } else {
                        // create empty residential address cells
                        for (int i = 0; i < 4; i++) {
                            cell = row.createCell(colIndex++, CellType.STRING);
                            cell.setCellStyle(cellStyle);
                            cell.setCellValue("-");
                        }
                    }

                    if (cAddress != null) {
                        // C address - details
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellValue(confidentialRequired ? "-" : cAddress.getStreetAddress());
                        cell.setCellStyle(cellStyleAlt);

                        // C address - commune
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (cAddress.getCommune() != null) {
                            cell.setCellValue(confidentialRequired ? "-" : cAddress.getCommune().getName());
                        }

                        // C address - district
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (cAddress.getDistrict() != null) {
                            cell.setCellValue(confidentialRequired ? "-" : cAddress.getDistrict().getName());
                        }

                        // C address - province
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (cAddress.getProvince() != null) {
                            cell.setCellValue(cAddress.getProvince().getName());
                        }
                    } else {
                        // create empty current address cells
                        for (int i = 0; i < 4; i++) {
                            cell = row.createCell(colIndex++, CellType.STRING);
                            cell.setCellStyle(cellStyle);
                            cell.setCellValue("-");
                        }
                    }

                    // Screening date
                    cell = row.createCell(colIndex++, CellType.NUMERIC);
                    cell.setCellValue(CommonUtils.fromLocalDateTime(c.getScreeningDate()));
                    cell.setCellStyle(dateCellStyleAlt);

                    // Confirm date
                    cell = row.createCell(colIndex++, CellType.NUMERIC);
                    cell.setCellValue(CommonUtils.fromLocalDateTime(c.getConfirmDate()));
                    cell.setCellStyle(dateCellStyleAlt);

                    // Confirm lab
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyleAlt);
                    if (c.getConfirmLab() != null) {
                        cell.setCellValue(c.getConfirmLab().getName());
                    }

                    // New/Old status
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyleAlt);
                    cell.setCellValue(getPosStatus(c.getHtsCaseStatus()));

                    // RTRI+
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyleAlt);
                    cell.setCellValue(c.getRtriPosLabel());

                    // Offerred PNS
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyleAlt);
                    cell.setCellValue(c.getOfferedPnsLabel());

                    // Note
                    cell = row.createCell(colIndex++, CellType.STRING);
                    if (!CommonUtils.isEmpty(c.getNote())) {
                        cell.setCellValue(Jsoup.parse(c.getNote()).text());
                    } else {
                        cell.setCellValue("-");
                    }
                    cell.setCellStyle(cellStyleAlt);
                }
            }
        }

        // Hide the primary key column
        htsPosSheet.setColumnHidden(2, true);

        if (rowIndex >= 4) {
            htsPosSheet.setAutoFilter(CellRangeAddress.valueOf("B4:Y" + rowIndex));
        }

    }

    private void createTxNewSheet(List<WeeklyReport> reports, Sheet txNewSheet, Workbook wbook,
                                  boolean confidentialRequired) {
        // TxNew sheet - Title
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();
        CellStyle dateCellStyle = wbook.createCellStyle();
        CellStyle dateCellStyleAlt = wbook.createCellStyle();

        int rowIndex = 0;
        Row row = txNewSheet.createRow(rowIndex);
        row.setHeightInPoints(33);

        font.setFontName("Times New Roman");
        font.setBold(true);
        font.setFontHeightInPoints((short) 15);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Cell cell = row.createCell(1, CellType.STRING);
        cell.setCellValue("BÁO CÁO DANH SÁCH BỆNH NHÂN BẮT ĐẦU ĐIỀU TRỊ ARV");
        cell.setCellStyle(cellStyle);

        // Column widths
        int[] colWidths = {21, 80, 50, 240, 178, 178, 125, 280, 125, 163, 125, 80, 158, 176, 176, 176, 176, 176, 176,
                176, 176, 160, 285, 160, 160, 150, 285};
        for (int i = 0; i < colWidths.length; i++) {
            txNewSheet.setColumnWidth(i, ExcelUtils.pixel2WidthUnits(colWidths[i]));
        }

        // TxNew sheet - Table header
        font = wbook.createFont();
        cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        rowIndex++;
        row = txNewSheet.createRow(rowIndex);
        row.setHeightInPoints(28);

        String[] arr = ("STT,PK,Tên phòng khám ngoại trú,Huyện/Quận,Tỉnh/Thành phố,Ngày báo cáo,Báo cáo của tuần,Mã " +
                "BN" +
                " tại PKNT,Họ và tên khách hàng,Ngày tháng năm sinh,Giới tính,Số CMND (nếu có)")
                .split(",");

        for (int i = 0; i < arr.length; i++) {
            cell = row.createCell(i + 1, CellType.STRING);
            cell.setCellValue(arr[i]);
            txNewSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, i + 1, i + 1));
            cell.setCellStyle(cellStyle);
        }

        int colIndex = arr.length + 1;

        cell = row.createCell(colIndex, CellType.STRING);
        cell.setCellValue("Địa chỉ hộ khẩu");
        txNewSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, colIndex, colIndex + 3));
        cell.setCellStyle(cellStyle);

        colIndex += 4;

        cell = row.createCell(colIndex, CellType.STRING);
        cell.setCellValue("Địa chỉ nơi ở hiện tại");
        txNewSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, colIndex, colIndex + 3));
        cell.setCellStyle(cellStyle);

        colIndex += 4;
        String[] arr2 = ("Ngày khẳng định HIV+,Nơi khẳng định HIV+,Ngày đăng ký điều trị,Ngày bắt đầu điều trị ARV,Ca" +
                " " +
                "điều trị cũ/mới,Ghi chú")
                .split(",");

        for (int i = 0; i < arr2.length; i++) {
            cell = row.createCell(colIndex + i, CellType.STRING);
            cell.setCellValue(arr2[i]);
            txNewSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, colIndex + i, colIndex + i));
            cell.setCellStyle(cellStyle);
        }

        rowIndex++;
        row = txNewSheet.createRow(rowIndex);
        row.setHeightInPoints(28);

        arr2 = "Thôn bản/số nhà,Xã/phường,Huyện/quận,Tỉnh/thành phố,Thôn bản/số nhà,Xã/phường,Huyện/quận,Tỉnh/thành phố"
                .split(",");

        for (int i = 0; i < arr2.length; i++) {
            cell = row.createCell(arr.length + 1 + i, CellType.STRING);
            cell.setCellValue(arr2[i]);
            cell.setCellStyle(cellStyle);
        }

        // Bordering for merged cells
        arr = "B,C,D,E,F,G,H,I,J,K,L,U,V,W,X,Y,Z,AA".split(",");
        for (int i = 0; i < arr.length; i++) {
            ExcelUtils.setBorders4Region(arr[i] + rowIndex + ":" + arr[i] + (rowIndex + 1), txNewSheet);
        }

        ExcelUtils.setBorders4Region("N" + rowIndex + ":Q" + rowIndex, txNewSheet);
        ExcelUtils.setBorders4Region("R" + rowIndex + ":U" + rowIndex, txNewSheet);

        arr = "M,N,O,P,Q,R,S,T".split(",");
        for (int i = 0; i < arr.length; i++) {
            ExcelUtils.setBorders4Region(arr[i] + (rowIndex + 1) + ":" + arr[i] + (rowIndex + 1), txNewSheet);
        }

        // Index row for filtering
        rowIndex++;
        row = txNewSheet.createRow(rowIndex);
        row.setHeightInPoints(22);

        ExcelUtils.setBorders4Style(cellStyle);

        for (int i = 1; i <= 26; i++) {
            cell = row.createCell(i, CellType.STRING);
            cell.setCellValue("( " + i + " )");
            cell.setCellStyle(cellStyle);
        }

        // TxNew sheet - Table content
        font = wbook.createFont();
        cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        ExcelUtils.setBorders4Style(cellStyle);

        CellStyle cellStyleAlt = wbook.createCellStyle();
        cellStyleAlt.cloneStyleFrom(cellStyle);
        cellStyleAlt.setAlignment(HorizontalAlignment.LEFT);
        cellStyleAlt.setIndention((short) 1);

        dateCellStyle.cloneStyleFrom(cellStyle);
        dateCellStyleAlt.cloneStyleFrom(cellStyle);

        DataFormat format = wbook.createDataFormat();
        dateCellStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));
        dateCellStyleAlt.setDataFormat(format.getFormat("dd/MM/yyyy hh:mm AM/PM"));

        for (WeeklyReport report : reports) {
            Iterator<WRCase> cases = report.getCases().iterator();

            while (cases.hasNext()) {
                WRCase c = cases.next();
                if (c.getOpc() != null && c.getOpc().getId() == report.getOrganization().getId().longValue()
                        && c.getEnrollmentDate() != null && c.getEnrollmentDate().isAfter(report.getFromDate())
                        && c.getEnrollmentDate().isBefore(report.getToDate())) {

                    rowIndex++;
                    row = txNewSheet.createRow(rowIndex);
                    row.setHeightInPoints(22);

                    Location siteAddress = report.getOrganization().getAddress();

                    colIndex = 1;

                    // STT
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(rowIndex - 3);
                    cell.setCellStyle(cellStyle);

                    // case primary key
                    cell = row.createCell(colIndex++, CellType.NUMERIC);
                    cell.setCellValue(c.getId());
                    cell.setCellStyle(cellStyle);

                    // Site name
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(report.getOrganization().getName());
                    cell.setCellStyle(cellStyleAlt);

                    // District
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyleAlt);
                    if (siteAddress != null && siteAddress.getDistrict() != null) {
                        cell.setCellValue(report.getOrganization().getAddress().getDistrict().getName());
                    }

                    // Province
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyleAlt);
                    if (siteAddress != null && siteAddress.getProvince() != null) {
                        cell.setCellValue(report.getOrganization().getAddress().getProvince().getName());
                    }

                    // Report date
                    cell = row.createCell(colIndex++, CellType.NUMERIC);
                    cell.setCellValue(CommonUtils.fromLocalDateTime(report.getCreateDate()));
                    cell.setCellStyle(dateCellStyle);

                    // Report of week
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(getWeekofReport(report));
                    cell.setCellStyle(dateCellStyle);

                    // Patient chart code
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(c.getPatientChartId());
                    cell.setCellStyle(cellStyleAlt);

                    // Client name
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(confidentialRequired ? "-" : c.getFullname());
                    cell.setCellStyle(cellStyleAlt);

                    // DOB
                    cell = row.createCell(colIndex++, CellType.STRING);
                    if (c.getDob() != null) {
                        cell.setCellValue(CommonUtils.fromLocalDateTime(c.getDob()));
                    }
                    cell.setCellStyle(dateCellStyle);

                    // Gender
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(c.getGender().toString());
                    cell.setCellStyle(cellStyleAlt);

                    // National ID
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(confidentialRequired ? "-" : c.getNationalId());
                    cell.setCellStyle(cellStyleAlt);

                    // Addresses
                    Location rAddress = null;
                    Location cAddress = null;

                    Set<Location> locs = c.getLocations();
                    for (Location loc : locs) {
                        if (loc == null) {
                            continue;
                        }

                        switch (loc.getAddressType()) {
                            case RESIDENT_ADDRESS:
                                rAddress = loc;
                                break;
                            case CURRENT_ADDRESS:
                                cAddress = loc;
                                break;
                            default:
                                break;
                        }
                    }

//					Location rAddress = c.getResidentialAddress();
//					Location cAddress = c.getCurrentAddress();

                    if (rAddress != null) {
                        // R address - details
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellValue(confidentialRequired ? "-" : rAddress.getStreetAddress());
                        cell.setCellStyle(cellStyleAlt);

                        // R address - commune
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (rAddress.getCommune() != null) {
                            cell.setCellValue(confidentialRequired ? "-" : rAddress.getCommune().getName());
                        }

                        // R address - district
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (rAddress.getDistrict() != null) {
                            cell.setCellValue(confidentialRequired ? "-" : rAddress.getDistrict().getName());
                        }

                        // R address - province
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (rAddress.getProvince() != null) {
                            cell.setCellValue(rAddress.getProvince().getName());
                        }
                    } else {
                        // create empty residential address cells
                        for (int i = 0; i < 4; i++) {
                            cell = row.createCell(colIndex++, CellType.STRING);
                            cell.setCellStyle(cellStyle);
                            cell.setCellValue("-");
                        }
                    }

                    if (cAddress != null) {
                        // C address - details
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellValue(confidentialRequired ? "-" : cAddress.getStreetAddress());
                        cell.setCellStyle(cellStyleAlt);

                        // C address - commune
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (cAddress.getCommune() != null) {
                            cell.setCellValue(confidentialRequired ? "-" : cAddress.getCommune().getName());
                        }

                        // C address - district
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (cAddress.getDistrict() != null) {
                            cell.setCellValue(confidentialRequired ? "-" : cAddress.getDistrict().getName());
                        }

                        // C address - province
                        cell = row.createCell(colIndex++, CellType.STRING);
                        cell.setCellStyle(cellStyleAlt);
                        if (cAddress.getProvince() != null) {
                            cell.setCellValue(cAddress.getProvince().getName());
                        }
                    } else {
                        // create empty current address cells
                        for (int i = 0; i < 4; i++) {
                            cell = row.createCell(colIndex++, CellType.STRING);
                            cell.setCellStyle(cellStyle);
                            cell.setCellValue("-");
                        }
                    }

                    // Confirm date
                    cell = row.createCell(colIndex++, CellType.NUMERIC);
                    if (c.getConfirmDate() != null) {
                        cell.setCellValue(CommonUtils.fromLocalDateTime(c.getConfirmDate()));
                    }
                    cell.setCellStyle(dateCellStyleAlt);

                    // Confirm lab
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellStyle(cellStyleAlt);
                    if (c.getConfirmLab() != null) {
                        cell.setCellValue(c.getConfirmLab().getName());
                    }

                    // Enrollment date
                    cell = row.createCell(colIndex++, CellType.NUMERIC);
                    if (c.getEnrollmentDate() != null) {
                        cell.setCellValue(CommonUtils.fromLocalDateTime(c.getEnrollmentDate()));
                    }
                    cell.setCellStyle(dateCellStyleAlt);

                    // ART start date
                    cell = row.createCell(colIndex++, CellType.NUMERIC);
                    if (c.getArvInitiationDate() != null) {
                        cell.setCellValue(CommonUtils.fromLocalDateTime(c.getArvInitiationDate()));
                    }
                    cell.setCellStyle(dateCellStyleAlt);

                    // Case status
                    cell = row.createCell(colIndex++, CellType.STRING);
                    cell.setCellValue(getTreatmentStatus(c.getTxCaseStatus()));
                    cell.setCellStyle(cellStyleAlt);

                    // Note
                    cell = row.createCell(colIndex++, CellType.STRING);
                    if (!CommonUtils.isEmpty(c.getNote())) {
                        cell.setCellValue(Jsoup.parse(c.getNote()).text());
                    } else {
                        cell.setCellValue("-");
                    }
                    cell.setCellStyle(cellStyleAlt);
                }
            }
        }

        // Hide the primary key column
        txNewSheet.setColumnHidden(2, true);

        if (rowIndex >= 4) {
            txNewSheet.setAutoFilter(CellRangeAddress.valueOf("B4:Z" + rowIndex));
        }

    }

    private String getPosStatus(int status) {
        // 0 = undetermined, 1 = new, 2 = old, 4 = Ngoai tinh
        String ret = "";
        switch (status) {
            case 0:
                ret = "Chưa xác định";
                break;
            case 1:
                ret = "Chẩn đoán mới";
                break;
            case 2:
                ret = "Chẩn đoán cũ";
                break;
            case 4:
                ret = "Ngoại tỉnh";
                break;
        }

        return ret;
    }

    /**
     * Convert treatment status to string
     */
    private String getTreatmentStatus(int status) {
        // 0 = undetermined, 1 = new, 2 = old chua DT, 3 = Ca cu bo tri, 4 = Ngoai tinh
        String ret = "";
        switch (status) {
            case 0:
                ret = "Chưa xác định";
                break;
            case 1:
                ret = "Điều trị mới";
                break;
            case 2:
                ret = "Cũ chưa điều trị";
                break;
            case 3:
                ret = "Cũ bỏ trị";
                break;
            case 4:
                ret = "Ngoại tỉnh";
                break;
        }

        return ret;
    }

    /**
     * Count the # of HIVPOS and TXNEW case
     *
     * @param report
     * @return
     */
    private long[] countReportedCases(WeeklyReport report) {
        long[] counts = new long[13]; // count[0] = HTS_POS, count[1] = HTS_POS: new, count[2] = HTS_POS: old,
        // count[3] = HTS_POS: ngoai tinh, count[4]= HTS_POS chua xac dinh
        // count[5] = HTS_POS/RTRI+, count[6] = HTS_POS/OFFERED_PNS
        // count[7] = TX_NEW, count[8] = TX_NEW new, count[9] = TX_NEW old chua DT,
        // count[10] = TX_NEW old bo tri
        // count[11] = TX_NEW ngoai tinh, count[12] = TX_NEW chua xac dinh

        Set<WRCase> set = report.getCases();
        set.parallelStream().forEach(c -> {
            if (c.getVct() != null && c.getConfirmDate() != null && c.getConfirmDate().isAfter(report.getFromDate())
                    && c.getConfirmDate().isBefore(report.getToDate())) {
                counts[0]++; // hts_pos

                switch (c.getHtsCaseStatus()) {
                    case 0:
                        counts[4]++;
                        break;
                    case 1:
                        counts[1]++;
                        break;
                    case 2:
                        counts[2]++;
                        break;
                    case 4:
                        counts[3]++;
                        break;
                }

                if (c.getRtriPos() == 1) {
                    counts[5]++;
                }

                if (c.getOfferedPns() == 1) {
                    counts[6]++;
                }
            }

            if (c.getOpc() != null && report.getOrganization().getId().longValue() == c.getOpc().getId()
                    && c.getEnrollmentDate() != null && c.getEnrollmentDate().isAfter(report.getFromDate())
                    && c.getEnrollmentDate().isBefore(report.getToDate())) {

                counts[7]++; // tx_new

                switch (c.getTxCaseStatus()) {
                    case 0:
                        counts[12]++;
                        break;
                    case 1:
                        counts[8]++;
                        break;
                    case 2:
                        counts[9]++;
                        break;
                    case 3:
                        counts[10]++;
                        break;
                    case 4:
                        counts[11]++;
                        break;
                    default:
                        break;
                }
            }
        });

        return counts;
    }

    private String getReportSubtitle(WeeklyReport report) {
        String rname = report.getName();
        rname = rname.substring(rname.lastIndexOf(" ") + 1);
        rname = "Tuần " + rname;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        rname += " (";
        rname += sdf.format(CommonUtils.fromLocalDateTime(report.getFromDate()));
        rname += " - ";
        rname += sdf.format(CommonUtils.fromLocalDateTime(report.getToDate()));
        rname += ")";

        return rname;
    }

    private String getWeekofReport(WeeklyReport report) {
//		WeekFields weekFields = WeekFields.of(Locale.forLanguageTag("vi-VN"));
//		int weekNumber = report.getFromDate().plusDays(1).get(weekFields.weekOfWeekBasedYear());

        LocalDateTime fromDatePlus1 = report.getFromDate().plusDays(1);
        LocalDate date = LocalDate.of(fromDatePlus1.getYear(), fromDatePlus1.getMonthValue(),
                fromDatePlus1.getDayOfMonth());
        int weekNumber = date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);

        String name = "Tuần " + weekNumber + "/" + report.getFromDate().getYear();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        name += " (";
        name += sdf.format(CommonUtils.fromLocalDateTime(report.getFromDate()));
        name += " - ";
        name += sdf.format(CommonUtils.fromLocalDateTime(report.getToDate()));
        name += ")";

        return name;
    }

    /**
     * Get a list of 8 week with the given end date of the last week
     *
     * @param toDate
     * @return
     */
    private static LocalDateTime[][] get8Weeks(final LocalDateTime toDateInput) {
        if (toDateInput == null) {
            return null;
        }

        LocalDateTime toDate = null;
        LocalDateTime fromDate = null;
        LocalDateTime[][] weeks = new LocalDateTime[8][2];
        for (int i = 0; i < 8; i++) {
            toDate = toDateInput.minusWeeks(i);
            fromDate = toDate.plusMinutes(1).minusDays(7).withHour(0).withMinute(0).withSecond(0).withNano(0);
            weeks[7 - i][0] = fromDate;
            weeks[7 - i][1] = toDate;
        }

        return weeks;
    }

    /**
     * Get a list of 12 month with the given year
     *
     * @param year
     * @return
     */
    private static LocalDateTime[][] get12Months(final int month, final int year) {
        LocalDateTime[][] months = new LocalDateTime[12][2];

        LocalDateTime beginOfYear = null;
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        if (month >= 1 && month <= 9) {
            beginOfYear = LocalDateTime.of(year - 1, 10, 1, 0, 0, 0, 0);
        } else {
            beginOfYear = LocalDateTime.of(year, 10, 1, 0, 0, 0, 0);
        }

        for (int i = 0; i < 12; i++) {
            fromDate = beginOfYear.plusMonths(i);
            toDate = fromDate.plusMonths(1).minusMinutes(1).withHour(23).withMinute(59).withSecond(59).withNano(0);
            months[i][0] = fromDate;
            months[i][1] = toDate;
        }

        return months;
    }

    /**
     * Check if a report is for a specified date range
     *
     * @param r
     * @param range
     * @return
     */
    private boolean isReportInDateRange(WeeklyReportPlain r, LocalDateTime[] range) {
        if (r == null || r.getFromDate() == null || range.length < 2) {
            return false;
        }

        LocalDateTime weekDay = r.getFromDate().plusDays(1);
        return weekDay.isAfter(range[0]) && weekDay.isBefore(range[1]);
    }
}
