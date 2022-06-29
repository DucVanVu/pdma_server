package org.pepfar.pdma.app.utils;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import net.sf.cglib.core.Local;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.pepfar.pdma.app.data.domain.Organization;
import org.pepfar.pdma.app.data.domain.Person;
import org.pepfar.pdma.app.data.dto.PersonDto;
import org.pepfar.pdma.app.data.types.Gender;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ExcelUtils {

    public static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;
    public static final int UNIT_OFFSET_LENGTH = 7;
    public static final int[] UNIT_OFFSET_MAP = new int[] {0, 36, 73, 109, 146, 182, 219};

    /**
     * pixel units to excel width units(units of 1/256th of a character width)
     *
     * @param pxs
     * @return
     */
    public static short pixel2WidthUnits(int pxs) {
        short widthUnits = (short) (EXCEL_COLUMN_WIDTH_FACTOR * (pxs / UNIT_OFFSET_LENGTH));

        widthUnits += UNIT_OFFSET_MAP[(pxs % UNIT_OFFSET_LENGTH)];

        return widthUnits;
    }

    /**
     * excel width units(units of 1/256th of a character width) to pixel units
     *
     * @param widthUnits
     * @return
     */
    public static int widthUnits2Pixel(short widthUnits) {
        int pixels = (widthUnits / EXCEL_COLUMN_WIDTH_FACTOR) * UNIT_OFFSET_LENGTH;

        int offsetWidthUnits = widthUnits % EXCEL_COLUMN_WIDTH_FACTOR;
        pixels +=
                Math.round(
                        (float) offsetWidthUnits
                                / ((float) EXCEL_COLUMN_WIDTH_FACTOR / UNIT_OFFSET_LENGTH));

        return pixels;
    }

    public static void createSimpleHeading(
            Workbook wbook, Sheet sheet, int[] colWidths, String[] texts) {

        for (int i = 0; i < colWidths.length; i++) {
            sheet.setColumnWidth(i, ExcelUtils.pixel2WidthUnits(colWidths[i]));
        }

        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Row row = sheet.createRow(0);
        row.setHeightInPoints(22);

        Cell cell = null;

        for (int i = 0; i < texts.length; i++) {
            cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(texts[i]);
            cell.setCellStyle(cellStyle);
        }

        String[] alphabet =
                "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,AA,AB,AC,AD,AE,AF,AG,AH,AI,AJ,AK,AL,AM,AN,AO,AP,AQ,AP,AS,AT,AU,AV,AW,AX,AY,AZ,BA,BB,BC,BD,BE,BF,BG,BH,BI,BJ,BK"
                        .split(",");

        for (int i = 0; i < texts.length; i++) {
            ExcelUtils.setBorders4Region(alphabet[i] + "1:" + alphabet[i] + "1", sheet);
        }
    }

    /**
     * Set general border for a cell
     *
     * @param cell
     */
    public static void setBorders4Style(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
    }

    /**
     * Set border for merged cells/regions
     *
     * @param regionAddress
     * @param sheet
     */
    public static void setBorders4Region(String regionAddress, Sheet sheet) {
        CellRangeAddress region = CellRangeAddress.valueOf(regionAddress);
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
        RegionUtil.setBottomBorderColor(IndexedColors.BLACK.getIndex(), region, sheet);
        RegionUtil.setTopBorderColor(IndexedColors.BLACK.getIndex(), region, sheet);
        RegionUtil.setLeftBorderColor(IndexedColors.BLACK.getIndex(), region, sheet);
        RegionUtil.setRightBorderColor(IndexedColors.BLACK.getIndex(), region, sheet);
    }

    /**
     * Write text in an existing cell on a worksheet
     *
     * @param sheet
     * @param rowIndex
     * @param colIndex
     * @param text
     */
    public static void writeInCell(Sheet sheet, int rowIndex, int colIndex, String text) {
        if (sheet == null || rowIndex < 0 || colIndex < 0 || CommonUtils.isEmpty(text)) {
            return;
        }

        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return;
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return;
        }

        cell.setCellValue(text);
    }

    /**
     * Write a number in an existing cell on a worksheet
     *
     * @param sheet
     * @param rowIndex
     * @param colIndex
     * @param value
     */
    public static void writeInCell(Sheet sheet, int rowIndex, int colIndex, int value) {
        if (sheet == null || rowIndex < 0 || colIndex < 0) {
            return;
        }

        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return;
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return;
        }

        cell.setCellValue(value);
    }

    /**
     * Write a boolean value in an existing cell on a worksheet
     *
     * @param sheet
     * @param rowIndex
     * @param colIndex
     * @param value
     */
    public static void writeInCell(Sheet sheet, int rowIndex, int colIndex, boolean value) {
        writeInCell(sheet, rowIndex, colIndex, String.valueOf(value));
    }

    public static byte[] generateBarcode(
            String text, String[] additionTexts, BarcodeFormat format, int width, int height) {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = barcodeWriter.encode(text, format, width, height);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        if (bitMatrix != null) {
            BufferedImage bi = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] imgData = null;
            try {
                ImageIO.write(bi, "png", baos);
                baos.flush();
                imgData = baos.toByteArray();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Write text below the barcode
            if (imgData != null && !CommonUtils.isEmpty(additionTexts)) {
                int totalTextLineToadd = additionTexts.length;
                InputStream in = new ByteArrayInputStream(imgData);
                BufferedImage image = null;

                try {
                    image = ImageIO.read(in);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BufferedImage outputImage =
                        new BufferedImage(
                                image.getWidth(),
                                image.getHeight() + 25 * totalTextLineToadd,
                                BufferedImage.TYPE_INT_ARGB);

                Graphics g = outputImage.getGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, outputImage.getWidth(), outputImage.getHeight());
                g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
                g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));

                Color textColor = Color.BLACK;
                g.setColor(textColor);

                FontMetrics fm = g.getFontMetrics();
                int startingYposition = height + 5;

                for (String displayText : additionTexts) {
                    g.drawString(
                            displayText,
                            (outputImage.getWidth() / 2) - (fm.stringWidth(displayText) / 2),
                            startingYposition);
                    startingYposition += 20;
                }

                baos = new ByteArrayOutputStream();

                try {
                    ImageIO.write(outputImage, "PNG", baos);
                    baos.flush();
                    imgData = baos.toByteArray();
                    baos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return imgData;
        }

        return null;
    }

    /**
     * Insert an image into a worksheet
     *
     * @param wbook
     * @param sheet
     * @param image
     * @param x1 - top left column index
     * @param y1 - top left row index
     * @param x2 - bottom right column index
     * @param y2 - bottom right row index
     */
    public static void insertImage(
            Workbook wbook, Sheet sheet, byte[] image, int x1, int y1, int x2, int y2) {
        if (sheet == null || image == null || x1 < 0 || y1 < 0 || x2 < 0 || y2 < 0) {
            return;
        }

        int pictureIdx = wbook.addPicture(image, Workbook.PICTURE_TYPE_PNG);
        // Returns an object that handles instantiating concrete classes
        CreationHelper helper = wbook.getCreationHelper();

        // Creates the top-level drawing patriarch.
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        // Create an anchor that is attached to the worksheet
        ClientAnchor anchor = helper.createClientAnchor();

        // create an anchor with upper left cell _and_ bottom right cell
        anchor.setCol1(x1); // Column B
        anchor.setRow1(y1); // Row 3
        anchor.setCol2(x2); // Column C
        anchor.setRow2(y2); // Row 4

        // Creates a picture
        drawing.createPicture(anchor, pictureIdx);
        sheet.getRow(y1).createCell(x1);
    }

    /**
     * Create and write text in cell
     *
     * @param sheet
     * @param rowIndex
     * @param colIndex
     * @param text
     */
    public static void createAndWriteInCell(Sheet sheet, int rowIndex, int colIndex, String text) {
        if (sheet == null || rowIndex < 0 || colIndex < 0 || CommonUtils.isEmpty(text)) {
            return;
        }

        Row row = sheet.createRow(rowIndex);
        Cell cell = row.createCell(colIndex, CellType.STRING);
        cell.setCellValue(text);
    }

    public static void createAndWriteInCell(
            Sheet sheet,
            int rowIndex,
            int colIndex,
            String text,
            int rowHeight,
            int fontSize,
            boolean bold) {
        if (sheet == null || rowIndex < 0 || colIndex < 0 || CommonUtils.isEmpty(text)) {
            return;
        }

        Workbook wbook = sheet.getWorkbook();
        Font font = wbook.createFont();
        CellStyle cellStyle = wbook.createCellStyle();

        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) fontSize);
        font.setBold(bold);
        font.setColor(IndexedColors.BLACK.getIndex());

        cellStyle.setFont(font);
        cellStyle.setWrapText(false);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setIndention((short) 1);

        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(rowHeight);

        Cell cell = row.createCell(colIndex, CellType.STRING);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(text);
    }

    /**
     * Check if organizations in the list are from different provinces
     *
     * @param orgs
     * @return
     */
    public static boolean isMultiProvince(List<Organization> orgs) {
        if (orgs == null || orgs.size() <= 1) {
            return false;
        }

        long provId = 0l;
        for (Organization org : orgs) {
            if (org != null
                    && org.getAddress() != null
                    && org.getAddress().getProvince() != null
                    && CommonUtils.isPositive(org.getAddress().getProvince().getId(), true)) {
                if (provId == 0l) {
                    provId = org.getAddress().getProvince().getId();
                    continue;
                }

                if (provId != org.getAddress().getProvince().getId()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Increase the arr item value by 1
     *
     * @param person
     * @param arr
     */
    public static void increase(LocalDateTime cutpoint, Person person, AtomicInteger[] arr) {
        LocalDateTime dob = person.getDob();
        Gender gender = person.getGender();

        if (dob == null || gender == null) {
            return;
        }

        long age = CommonUtils.dateDiff(ChronoUnit.YEARS, dob, cutpoint);
        int j = -1;

        if (age < 15) {
            switch (gender) {
                case MALE:
                    j = 0;
                    break;
                case FEMALE:
                    j = 1;
                    break;
                default:
                    j = 1; // to avoid missing patients
                    break;
            }
        } else {
            switch (gender) {
                case MALE:
                    j = 2;
                    break;
                case FEMALE:
                    j = 3;
                    break;
                default:
                    j = 3; // to avoid missing patients
                    break;
            }
        }

        if (j >= 0) {
            arr[j].incrementAndGet();
        }
    }

    /**
     * Increase the array item value by 1
     *
     * @param arr
     * @param i
     * @param j
     */
    public static void increase(LocalDateTime cutpoint, PersonDto person, AtomicInteger[][] arr, int i) {

        LocalDateTime dob = person.getDob();
        Gender gender = person.getGender();

        if (dob == null || gender == null) {
            return;
        }

        long age = CommonUtils.dateDiff(ChronoUnit.YEARS, dob, cutpoint);
        int j = -1;

        if (age < 15) {
            switch (gender) {
                case MALE:
                    j = 0;
                    break;
                case FEMALE:
                    j = 1;
                    break;
                default:
                    j = 1; // to avoid missing patients
                    break;
            }
        } else {
            switch (gender) {
                case MALE:
                    j = 2;
                    break;
                case FEMALE:
                    j = 3;
                    break;
                default:
                    j = 3; // to avoid missing patients
                    break;
            }
        }

        if (i >= 0 && j >= 0) {
            arr[i][j].incrementAndGet();
        }
    }

    /**
     * Increase the array item value by 1
     *
     * @param arr
     * @param i
     * @param j
     */
    public static void increase(LocalDateTime cutpoint, Person person, AtomicInteger[][] arr, int i) {

        LocalDateTime dob = person.getDob();
        Gender gender = person.getGender();

        if (dob == null || gender == null) {
            return;
        }

        long age = CommonUtils.dateDiff(ChronoUnit.YEARS, dob, cutpoint);
        int j = -1;

        if (age < 15) {
            switch (gender) {
                case MALE:
                    j = 0;
                    break;
                case FEMALE:
                    j = 1;
                    break;
                default:
                    j = 1; // to avoid missing patients
                    break;
            }
        } else {
            switch (gender) {
                case MALE:
                    j = 2;
                    break;
                case FEMALE:
                    j = 3;
                    break;
                default:
                    j = 3; // to avoid missing patients
                    break;
            }
        }

        if (i >= 0 && j >= 0) {
            arr[i][j].incrementAndGet();
        }
    }
}
