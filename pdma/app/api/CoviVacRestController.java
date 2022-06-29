package org.pepfar.pdma.app.api;

import org.pepfar.pdma.app.data.dto.CoviVacDto;
import org.pepfar.pdma.app.data.dto.CoviVacFilterDto;
import org.pepfar.pdma.app.data.service.CoviVacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/covivac")
public class CoviVacRestController {

    @Autowired
    private CoviVacService service;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoviVacDto> getVaccination(@PathVariable("id") Long id) {
        CoviVacDto dto = service.findById(id);

        if (dto == null) {
            return new ResponseEntity<>(new CoviVacDto(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CoviVacDto>> getAllVaccinations(@RequestBody CoviVacFilterDto filter) {

        List<CoviVacDto> services = service.findAll(filter);

        return new ResponseEntity<>(services, HttpStatus.OK);
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoviVacDto> saveVaccination(@RequestBody CoviVacDto dto) {

        if (dto == null) {
            return new ResponseEntity<>(new CoviVacDto(), HttpStatus.BAD_REQUEST);
        }

        dto = service.saveOne(dto);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteVaccinations(@RequestBody CoviVacDto[] dtos) {
        service.deleteMultiple(dtos);
    }

}
