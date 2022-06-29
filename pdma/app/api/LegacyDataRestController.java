package org.pepfar.pdma.app.api;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pepfar.pdma.app.data.opcassistimport.Importer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/legacy_data")
public class LegacyDataRestController {

	@Autowired
	private Importer importer;

	@RequestMapping(path = "/upload/{opcId}", method = RequestMethod.POST)
	public void importDBFile(@PathVariable("opcId") Long opcId, @RequestParam("file") MultipartFile file)
			throws Exception {

		try {
			if (!file.isEmpty()) {
				Workbook wbook = new XSSFWorkbook(file.getInputStream());
				importer.importData(wbook, opcId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@RequestMapping(path = "/upload-2/{opcId}/{importTB}", method = RequestMethod.POST)
	public void importDBFile2(@PathVariable("opcId") Long opcId, @PathVariable("importTB") Boolean importTB,
			@RequestParam("file") MultipartFile file) throws Exception {

		try {
			if (!file.isEmpty()) {
				Workbook wbook = new XSSFWorkbook(file.getInputStream());
				importer.importDataRevised(wbook, opcId, importTB);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@RequestMapping(path = "/upload/other-regimen", method = RequestMethod.POST)
	public void importDBFile3(@RequestParam("file") MultipartFile file) throws Exception {

		try {
			if (!file.isEmpty()) {
				Workbook wbook = new XSSFWorkbook(file.getInputStream());
				importer.importDataRevised2(wbook);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@RequestMapping(path = "/hivinfo-id", method = RequestMethod.POST)
	public void importHIVInfoID(@RequestParam("file") MultipartFile file) throws Exception {

		try {
			if (!file.isEmpty()) {
				Workbook wbook = new XSSFWorkbook(file.getInputStream());
				importer.importHIVInfoID(wbook);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@RequestMapping(path = "/mmt", method = RequestMethod.POST)
	public void importMMTData(@RequestParam("file") MultipartFile file) throws Exception {

		try {
			if (!file.isEmpty()) {
				Workbook wbook = new XSSFWorkbook(file.getInputStream());
				importer.importMMTData(wbook);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
