package org.pepfar.pdma.app.data.opcassistimport;

import java.time.LocalDateTime;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.pepfar.pdma.app.utils.CommonUtils;

public class ImportUtils {

	private static final DataFormatter formatter = new DataFormatter();

	public static LocalDateTime fromCellValue(Cell cell) {

		if (cell == null) {
			return null;
		}

		String val = formatter.formatCellValue(cell);
		LocalDateTime dateVal = null;

		if (!CommonUtils.isEmpty(val)) {
			String[] arr = val.split("/");

			if (arr == null || arr.length != 3 || CommonUtils.isEmpty(arr[0], true) || CommonUtils.isEmpty(arr[1], true)
					|| CommonUtils.isEmpty(arr[2], true)) {
				return null;
			}

			int day = Integer.parseInt(arr[0]);
			int month = Integer.parseInt(arr[1]);
			int year = Integer.parseInt(arr[2]);

			switch (month) {
			case 2:
				if (day > 28) {
					day = 28;
				}

				break;

			case 4:
			case 6:
			case 9:
			case 11:
				if (day > 30) {
					day = 30;
				}

				break;

			default:
				break;
			}

			dateVal = LocalDateTime.of(year, month, day, 0, 0, 0);
		}

		return dateVal;
	}

	public static String[] getAddressCodes(Cell cell) {

		if (cell == null) {
			return null;
		}

		String val = formatter.formatCellValue(cell);
		if (CommonUtils.isEmpty(val)) {
			return null;
		}

		String[] codes = val.split("\\|");
		if (codes.length <= 0) {
			return null;
		}

		int iCommuneCode = 0;
		int iDistrictCode = 0;
		int iProvinceCode = 0;

		if (codes.length == 1) {
			iProvinceCode = Integer.parseInt(codes[0]);

			return new String[] { "province_" + iProvinceCode };
		} else if (codes.length == 2) {
			iDistrictCode = Integer.parseInt(codes[0]);
			iProvinceCode = Integer.parseInt(codes[1]);

			return new String[] { "district_" + iDistrictCode, "province_" + iProvinceCode };
		} else if (codes.length == 3) {
			iCommuneCode = Integer.parseInt(codes[0]);
			iDistrictCode = Integer.parseInt(codes[1]);
			iProvinceCode = Integer.parseInt(codes[2]);

			return new String[] { "commune_" + iCommuneCode, "district_" + iDistrictCode, "province_" + iProvinceCode };
		} else {
			return null;
		}
	}
}
