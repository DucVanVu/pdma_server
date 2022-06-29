package org.pepfar.pdma.app.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.pepfar.pdma.app.data.dto.SampleDto;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

@RestController
public class IndexRestController {

    @Autowired
    private ApplicationContext context;

    @RequestMapping(path = "/")
    public ResponseEntity<String> index() {
        int currentYear = LocalDateTime.now().getYear();
        return new ResponseEntity<>("PDMA Online Application. (c) 2019 - " + currentYear + " the EPIC Project.",
                HttpStatus.OK);
    }

    @GetMapping(path = "/sample")
    public ResponseEntity<List<SampleDto>> getSample() {
        List<SampleDto> sampleDtos = new ArrayList<>();

        try (InputStream is = context.getResource("classpath:/sample.json").getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            sampleDtos = mapper.readValue(is, new TypeReference<List<SampleDto>>() {
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(sampleDtos, HttpStatus.OK);
    }

    @RequestMapping(value = "/manual/{manualId}", method = RequestMethod.GET)
    public void downloadManuals(@PathVariable("manualId") Long manualId, HttpServletResponse response) {

        if (!CommonUtils.isPositive(manualId, true)) {
            throw new RuntimeException();
        }

        String filename = "manuals_" + manualId + ".pdf";

        try (InputStream manual = context.getResource("classpath:manuals/" + filename).getInputStream()) {

            CacheControl cc = CacheControl.maxAge(360, TimeUnit.DAYS).cachePublic();

            response.addHeader("Access-Control-Expose-Headers", "x-filename");
            response.addHeader("Content-disposition", "inline; filename=" + filename);
            response.addHeader("x-filename", filename);
            response.setHeader("Cache-Control", cc.getHeaderValue());
            response.setContentType("application/pdf");

            try {
                byte[] content = IOUtils.toByteArray(manual);
                response.getOutputStream().write(content);
                response.flushBuffer();
                response.getOutputStream().close();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
