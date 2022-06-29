package org.pepfar.pdma.app.api;

import org.pepfar.pdma.app.data.dto.AdminUnitEditTableDto;
import org.pepfar.pdma.app.data.service.AdminUnitEditTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/admin_unit_edit_table")
public class AdminUnitEditTableRestController {

    @Autowired
    private AdminUnitEditTableService service;

//    @Secured({"ROLE_ADMIN","ROLE_DLP","ROLE_SDAH","ROLE_DISTRICT","ROLE_WARD","ROLE_FAMER","ROLE_USER", WLConstant.ROLE_SDAH_VIEW})
//    @RequestMapping(value="/searchByPage", method = RequestMethod.POST)
//    public Page<AdministrativeUnitEditableDto> getSearchByPage(@RequestBody AnimalReportDataSearchDto searchDto) {
//        return service.findPage(searchDto);
//    }

    @RequestMapping(value="/save", method = RequestMethod.POST)
    public AdminUnitEditTableDto saveOrUpdate(@RequestBody AdminUnitEditTableDto dto) {
        return service.saveOrUpdate(dto);
    }

//    @Secured({"ROLE_ADMIN","ROLE_DLP","ROLE_SDAH","ROLE_DISTRICT","ROLE_WARD","ROLE_FAMER","ROLE_USER", WLConstant.ROLE_SDAH_VIEW})
//    @RequestMapping(value = "/getAdministrativeUnitEditableById/{id}", method = RequestMethod.GET)
//    public AdministrativeUnitEditableDto getAdministrativeUnitEditableById(@PathVariable Long id) {
//        return service.getAdministrativeUnitEditableById(id);
//    }
//
//    @Secured({"ROLE_ADMIN","ROLE_DLP","ROLE_SDAH","ROLE_DISTRICT","ROLE_WARD","ROLE_FAMER","ROLE_USER", WLConstant.ROLE_SDAH_VIEW})
//    @RequestMapping(value = "/getAdministrativeUnitEditableByAdminUnit/{id}", method = RequestMethod.GET)
//    public List<AdministrativeUnitEditableDto> getAdministrativeUnitEditableByAdminUnit(@PathVariable Long id) {
//        return service.getAdministrativeUnitEditableByAdminUnit(id);
//    }
//
//    @Secured({"ROLE_ADMIN","ROLE_DLP","ROLE_SDAH","ROLE_DISTRICT","ROLE_WARD","ROLE_FAMER","ROLE_USER", WLConstant.ROLE_SDAH_VIEW})
//    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
//    public boolean deleteById(@PathVariable Long id) {
//        try {
//            this.service.deleteById(id);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }


}
