package org.pepfar.pdma.app.api;

import static org.assertj.core.api.Assertions.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.Media;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.CaseDeleteFilterDto;
import org.pepfar.pdma.app.data.dto.CaseDto;
import org.pepfar.pdma.app.data.dto.CaseDto4Search;
import org.pepfar.pdma.app.data.dto.CaseFilterDto;
import org.pepfar.pdma.app.data.dto.CaseOrgDto;
import org.pepfar.pdma.app.data.dto.CaseOrgUpdateDto;
import org.pepfar.pdma.app.data.dto.CaseReferralResultDto;
import org.pepfar.pdma.app.data.dto.CheckNationalIdDto;
import org.pepfar.pdma.app.data.dto.PersonDto;
import org.pepfar.pdma.app.data.dto.PhotoCropperDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.opcassistimport.Importer;
import org.pepfar.pdma.app.data.service.CaseService;
import org.pepfar.pdma.app.data.service.PersonService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.ImageUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.BarcodeFormat;

@RestController
@RequestMapping(path = "/api/v1/case")
public class CaseRestController {

    @Autowired
    private CaseService service;

    @Autowired
    private PersonService personService;

    @Autowired
    private Importer importer;

    @Value("${endpoints.cors.allowed-origins}")
    private String endpoint;

    @GetMapping(path = "/{coId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseOrgDto> getCase(@PathVariable("coId") Long coId) {
        CaseOrgDto dto = service.findByCaseOrgId(coId);

        if (dto == null) {
            return new ResponseEntity<CaseOrgDto>(new CaseOrgDto(), HttpStatus.OK);
        }

        return new ResponseEntity<CaseOrgDto>(dto, HttpStatus.OK);
    }

    @PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<CaseOrgDto>> getAllCases(@RequestBody CaseFilterDto filter) {

        // To make sure the cases are filtered appropriately by viewer
        User user = SecurityUtils.getCurrentUser();
        filter.setUser(new UserDto(user, false));

        Page<CaseOrgDto> services = service.findAllPageable(filter);

        return new ResponseEntity<Page<CaseOrgDto>>(services, HttpStatus.OK);
    }

    @GetMapping(path = "/redirect4referral/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void redirect2PatientViewPage(@PathVariable("caseId") Long caseId, HttpServletResponse response) {

        String redirectPath = service.createRedirectPath(caseId);

        if (redirectPath == null) {
            return;
        }

        try {
			response.sendRedirect(endpoint + redirectPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @PostMapping(path = "/list-4-appointment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<CaseDto4Search>> getAllCases4Appointment(@RequestBody CaseFilterDto filter) {

        // To make sure the cases are filtered appropriately by viewer
        User user = SecurityUtils.getCurrentUser();
        filter.setUser(new UserDto(user, false));

        Page<CaseDto4Search> services = service.findAll4Appointment(filter);

        return new ResponseEntity<>(services, HttpStatus.OK);
    }

    @PostMapping(path = "/hivinfoid-exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> hivInfoIdExists(@RequestBody CaseOrgDto dto) {
        return new ResponseEntity<>(service.hivInfoIdExists(dto), HttpStatus.OK);
    }

    @PostMapping(path = "/patientchartid-exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseOrgDto> patientChartIdExists(@RequestBody CaseOrgDto dto) {
        return new ResponseEntity<>(service.patientChartIdExists(dto), HttpStatus.OK);
    }

    @PostMapping(path = "/nationalid-exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CheckNationalIdDto> nationalIdExists(@RequestBody CaseOrgDto dto) {
        return new ResponseEntity<>(service.nationalIdExists(dto), HttpStatus.OK);
    }

    @PostMapping(path = "/patientrecord-exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseOrgDto> patientRecordExists(@RequestBody CaseOrgDto dto) {
        return new ResponseEntity<>(service.patientRecordExists(dto), HttpStatus.OK);
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseOrgDto> saveCase(@RequestBody CaseOrgDto dto) {

        if (dto == null) {
            return new ResponseEntity<>(new CaseOrgDto(), HttpStatus.BAD_REQUEST);
        }

        dto = service.saveOne(dto);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping(path = "/hivinfoid", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseDto> saveHIVInfoID(@RequestBody CaseDto dto) {

        if (dto == null) {
            return new ResponseEntity<CaseDto>(new CaseDto(), HttpStatus.BAD_REQUEST);
        }

        dto = service.updateHivInfoID(dto);

        return new ResponseEntity<CaseDto>(dto, HttpStatus.OK);
    }

    @PostMapping(path = "/rem_hivinfoid", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseDto> removeHIVInfoID(@RequestBody CaseDto dto) {

        if (dto == null) {
            return new ResponseEntity<CaseDto>(new CaseDto(), HttpStatus.BAD_REQUEST);
        }

        dto = service.removeHivInfoID(dto);

        return new ResponseEntity<CaseDto>(dto, HttpStatus.OK);
    }

    @DeleteMapping(path = "/soft", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseOrgDto> softDelete(@RequestBody CaseOrgDto dto) {

        if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
            return new ResponseEntity<>(new CaseOrgDto(), HttpStatus.BAD_REQUEST);
        }

        dto = service.softDelete(dto);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping(path = "/restore", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseOrgDto> restoreRecord(@RequestBody CaseOrgDto dto) {

        if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
            return new ResponseEntity<>(new CaseOrgDto(), HttpStatus.BAD_REQUEST);
        }

        dto = service.restoreCase(dto);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(path = "/status/{caseOrgId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CaseOrgDto>> getFullCaseStatusHistory(@PathVariable("caseOrgId") Long caseOrgId) {
        return new ResponseEntity<>(service.getCaseStatusHistory(caseOrgId), HttpStatus.OK);
    }

    @GetMapping(path = "/case-org/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseOrgDto> getCaseOrg(@PathVariable("id") Long id) {
        CaseOrgDto dto = service.findCaseOrgById(id);

        if (dto == null) {
            return new ResponseEntity<CaseOrgDto>(new CaseOrgDto(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<CaseOrgDto>(dto, HttpStatus.OK);
    }

    @PostMapping(path = "/case-org", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseOrgDto> updateCaseOrg(@RequestBody CaseOrgDto dto) {
        if (dto == null) {
            throw new RuntimeException();
        }

        return new ResponseEntity<>(service.updateCaseOrg(dto), HttpStatus.OK);
    }

    @DeleteMapping(path = "/case-org", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteCaseOrg(@RequestBody CaseOrgDto dto) {
        if (dto == null) {
            throw new RuntimeException();
        }

        service.deleteCaseOrg(dto);
    }

    @PostMapping(path = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> updateTreatmentStatus(@RequestBody CaseOrgUpdateDto dto) {
        if (dto == null) {
            throw new RuntimeException();
        }

        return new ResponseEntity<>(service.updateTreatmentStatus(dto), HttpStatus.OK);
    }

    @PostMapping(path = "/cancel_referral", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> cancelReferral(@RequestBody CaseReferralResultDto dto) {
        if (dto == null) {
            throw new RuntimeException();
        }

        return new ResponseEntity<>(service.cancelReferral(dto), HttpStatus.OK);
    }

    @PostMapping(path = "/referral_result", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> updateReferralResult(@RequestBody CaseReferralResultDto dto) {
        if (dto == null) {
            throw new RuntimeException();
        }

        return new ResponseEntity<>(service.updateReferralResult(dto), HttpStatus.OK);
    }

    @PostMapping(path = "/re-enroll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> reEnrollPatient(@RequestBody CaseOrgUpdateDto dto) {
        if (dto == null) {
            throw new RuntimeException();
        }

        return new ResponseEntity<>(service.reEnrollPatient(dto), HttpStatus.OK);
    }

    @PostMapping(path = "/import/{orgId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void importFromOPCAssist(@RequestParam("file") MultipartFile file, @PathVariable("orgId") Long orgId) {
        try {
            if (!file.isEmpty()) {
                byte[] data = file.getBytes();
                Workbook wbook = new XSSFWorkbook(new ByteArrayInputStream(data));

                importer.importData(wbook, orgId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel file.");
        }
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteCases(@RequestBody CaseDto[] dtos) {
        service.deleteMultiple(dtos);
    }

    @DeleteMapping(path = "/_danger_/del", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteCasesByOrg(@RequestBody CaseDeleteFilterDto filter) {
        service.deleteByOrganization(filter);
    }

    @RequestMapping(path = "/photo/upload/{personId}", method = RequestMethod.POST)
    public ResponseEntity<PersonDto> uploadPatientPhoto(@RequestParam("file") MultipartFile file,
                                                        @PathVariable("personId") Long personId) {

        User user = SecurityUtils.getCurrentUser();
        if (user == null || !CommonUtils.isPositive(user.getId(), true)) {
            return new ResponseEntity<>(new PersonDto(), HttpStatus.FORBIDDEN);
        }

        PersonDto person = new PersonDto();
        person.setId(personId);

        try {
            if (!file.isEmpty()) {
                byte[] data = file.getBytes();

                if (data != null && data.length > 0) {
                    person.setImage(data);

                    person = personService.savePhoto(person);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @RequestMapping(path = "/photo/crop/{personId}", method = RequestMethod.POST)
    public ResponseEntity<PersonDto> cropPatientPhoto(@RequestBody PhotoCropperDto dto,
                                                      @PathVariable("personId") Long personId) {

        User user = SecurityUtils.getCurrentUser();

        if (dto.getUser() == null || !user.getUsername().equals(dto.getUser().getUsername())) {
            return new ResponseEntity<>(new PersonDto(), HttpStatus.FORBIDDEN);
        }

        byte[] photo = personService.getPhoto(personId);

        if (photo == null || photo.length <= 0 || dto.getX() < 0 || dto.getY() < 0 || dto.getW() <= 0
                || dto.getH() <= 0) {
            return new ResponseEntity<>(new PersonDto(), HttpStatus.BAD_REQUEST);
        }

        photo = ImageUtils.crop(photo, dto.getX(), dto.getY(), dto.getW(), dto.getH());

        if (photo == null) {
            return new ResponseEntity<>(new PersonDto(), HttpStatus.BAD_REQUEST);
        }

        PersonDto person = new PersonDto();
        person.setId(personId);

        try {
            person.setImage(photo);

            person = personService.savePhoto(person);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @PostMapping(value = "/photo/{personId}")
    public void getProfilePhoto(HttpServletResponse response, @PathVariable("personId") Long personId)
            throws ServletException, IOException {

        byte[] data = personService.getPhoto(personId);

        if (data == null || data.length <= 0) {
            return;
        }

        // CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

        // response.setHeader("Cache-Control", cc.getHeaderValue());
        response.setContentType("image/png");

        try {
            response.getOutputStream().write(data);
            response.flushBuffer();
            response.getOutputStream().close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @PostMapping(value = "/barcode/{coId}")
    public void getProfileBarcode(HttpServletResponse response, @PathVariable("coId") Long coId)
            throws ServletException, IOException {

        if (!CommonUtils.isPositive(coId, true)) {
            return;
        }

        String urlPrefix = endpoint;
        urlPrefix += "/#/opc/view-patient/" + coId;

        byte[] barcodeImage = ExcelUtils.generateBarcode(urlPrefix, null, BarcodeFormat.QR_CODE, 150, 150);

        if (barcodeImage == null || barcodeImage.length <= 0) {
            return;
        }

        CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

        response.setHeader("Cache-Control", cc.getHeaderValue());
        response.setContentType("image/png");

        try {
            response.getOutputStream().write(barcodeImage);
            response.flushBuffer();
            response.getOutputStream().close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @PostMapping(path = "/refsheet/{coId}")
    public void exportExcel4ReferralSheet(@PathVariable("coId") Long caseOrgId, HttpServletResponse response,
                                          HttpServletRequest request) {

        Workbook wbook = service.exportReferralSheet(caseOrgId, endpoint);

        if (wbook == null) {
            throw new RuntimeException();
        }

        String filename = "phieuchuyentiep-" + System.currentTimeMillis() + ".xlsx";
        CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

        response.addHeader("Access-Control-Expose-Headers", "x-filename");
        response.addHeader("Content-disposition", "inline; filename=" + filename);
        response.addHeader("x-filename", filename);
        response.setHeader("Cache-Control", cc.getHeaderValue());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        try {
            wbook.write(response.getOutputStream());
            response.flushBuffer();
            response.getOutputStream().close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @PostMapping(path = "/excel-4-search")
    public void exportExcel4SearchResults(@RequestBody CaseFilterDto filter, HttpServletResponse response) {

        User user = SecurityUtils.getCurrentUser();

        if (filter == null) {
            filter = new CaseFilterDto();
        }

        filter.setUser(new UserDto(user, false));

        Workbook wbook = service.exportSearchResults(filter);

        if (wbook == null) {
            throw new RuntimeException();
        }

        String filename = "ketquatimkiem-" + System.currentTimeMillis() + ".xlsx";
        CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

        response.addHeader("Access-Control-Expose-Headers", "x-filename");
        response.addHeader("Content-disposition", "inline; filename=" + filename);
        response.addHeader("x-filename", filename);
        response.setHeader("Cache-Control", cc.getHeaderValue());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        try {
            wbook.write(response.getOutputStream());
            response.flushBuffer();
            response.getOutputStream().close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @PostMapping(path = "/excel")
    public void exportExcel(@RequestBody CaseFilterDto filter, HttpServletResponse response) {

        User user = SecurityUtils.getCurrentUser();

        if (filter == null) {
            filter = new CaseFilterDto();
        }

        filter.setUser(new UserDto(user, false));

        Workbook wbook = service.exportData(filter);

        if (wbook == null) {
            throw new RuntimeException();
        }

        String filename = "dulieuphongkham-" + System.currentTimeMillis() + ".xlsx";
        CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

        response.addHeader("Access-Control-Expose-Headers", "x-filename");
        response.addHeader("Content-disposition", "inline; filename=" + filename);
        response.addHeader("x-filename", filename);
        response.setHeader("Cache-Control", cc.getHeaderValue());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        try {
            wbook.write(response.getOutputStream());
            response.flushBuffer();
            response.getOutputStream().close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @PostMapping(path = "/adhoc_data")
    public void exportExcel4AdhocDataRequest(HttpServletResponse response) {

        Workbook wbook = service.exportListOfPatients();

        if (wbook == null) {
            throw new RuntimeException();
        }

        String filename = "dulieu-ly-" + System.currentTimeMillis() + ".xlsx";
        CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

        response.addHeader("Access-Control-Expose-Headers", "x-filename");
        response.addHeader("Content-disposition", "inline; filename=" + filename);
        response.addHeader("x-filename", filename);
        response.setHeader("Cache-Control", cc.getHeaderValue());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        try {
            wbook.write(response.getOutputStream());
            response.flushBuffer();
            response.getOutputStream().close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
