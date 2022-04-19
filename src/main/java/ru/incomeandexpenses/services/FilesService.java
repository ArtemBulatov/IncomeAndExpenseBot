package ru.incomeandexpenses.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.incomeandexpenses.dto.OperationDTO;
import ru.incomeandexpenses.models.Verify;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FilesService {
    private static final String INCOME = "Доход";
    private static final String EXPENSE = "Расход";
    private static final String DATE = "Дата";

    private final DtoService dtoService;
    private final VerifyService verifyService;

    public FilesService(DtoService dtoService, VerifyService verifyService) {
        this.dtoService = dtoService;
        this.verifyService = verifyService;
    }

    public InputFile createReport() {
        Verify lastVerify = verifyService.getLast();
        if (lastVerify == null) {
            lastVerify = new Verify();
            lastVerify.setDate(LocalDateTime.now(ZoneId.of("Europe/Moscow")).minusDays(30));
        }

        int generalIncomeVal = 0;
        int maxIncomeVal = 0;
        int lexaIncomeVal = 0;
        List<OperationDTO> incomeList = dtoService.getAllIncomeDtoList(lastVerify.getDate());
        List<OperationDTO> maxIncomeList = new ArrayList<>();
        List<OperationDTO> lexaIncomeList = new ArrayList<>();

        for (OperationDTO dto : incomeList) {
            if (dto.getAuthor().equals("Max")) {
                generalIncomeVal = generalIncomeVal + dto.getValue();
                maxIncomeVal = maxIncomeVal + dto.getValue();
                maxIncomeList.add(dto);
            } else if (dto.getAuthor().equals("@ETOKUHNI (кухни на заказ)")) {
                generalIncomeVal = generalIncomeVal + dto.getValue();
                lexaIncomeVal = lexaIncomeVal + dto.getValue();
                lexaIncomeList.add(dto);
            }
        }

        int generalExpenseVal = 0;
        int maxExpenseVal = 0;
        int lexaExpenseVal = 0;
        List<OperationDTO> expenseList = dtoService.getAllExpenseDtoList(lastVerify.getDate());
        List<OperationDTO> maxExpenseList = new ArrayList<>();
        List<OperationDTO> lexaExpenseList = new ArrayList<>();

        for (OperationDTO dto : expenseList) {
            if (dto.getAuthor().equals("Max")) {
                generalExpenseVal = generalExpenseVal + dto.getValue();
                maxExpenseVal = maxExpenseVal + dto.getValue();
                maxExpenseList.add(dto);
            } else if (dto.getAuthor().equals("@ETOKUHNI (кухни на заказ)")) {
                generalExpenseVal = generalExpenseVal + dto.getValue();
                lexaExpenseVal = lexaExpenseVal + dto.getValue();
                lexaExpenseList.add(dto);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try(XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            sheet.setDefaultColumnWidth(17);

            XSSFRow firstRow = sheet.createRow(0);
            XSSFCell general = firstRow.createCell(0);
            general.setCellValue("Общее");
            XSSFCell generalIncome = firstRow.createCell(1);
            generalIncome.setCellValue(INCOME);
            XSSFCell generalIncomeValue = firstRow.createCell(2);
            generalIncomeValue.setCellValue(generalIncomeVal);
            XSSFCell generalExpense = firstRow.createCell(3);
            generalExpense.setCellValue(EXPENSE);
            XSSFCell generalExpenseValue = firstRow.createCell(4);
            generalExpenseValue.setCellValue(generalExpenseVal);
            XSSFCell result = firstRow.createCell(5);
            result.setCellValue("Итог");
            XSSFCell resultValue = firstRow.createCell(6);
            resultValue.setCellValue(generalIncomeVal - generalExpenseVal);
            XSSFCell halfOfResult = firstRow.createCell(7);
            halfOfResult.setCellValue("Итог 0.5");
            XSSFCell halfOfResultValue = firstRow.createCell(8);
            halfOfResultValue.setCellValue((generalIncomeVal - generalExpenseVal) / 2);

            XSSFRow secondRow = sheet.createRow(1);
            XSSFCell max = secondRow.createCell(0);
            max.setCellValue("Макс");
            XSSFCell maxIncome = secondRow.createCell(1);
            maxIncome.setCellValue(INCOME);
            XSSFCell maxIncomeValue = secondRow.createCell(2);
            maxIncomeValue.setCellValue(maxIncomeVal);
            XSSFCell maxExpense = secondRow.createCell(3);
            maxExpense.setCellValue(EXPENSE);
            XSSFCell maxExpenseValue = secondRow.createCell(4);
            maxExpenseValue.setCellValue(maxExpenseVal);
            XSSFCell maxBalance = secondRow.createCell(5);
            maxBalance.setCellValue("На балансе");
            XSSFCell maxBalanceValue = secondRow.createCell(6);
            maxBalanceValue.setCellValue(maxIncomeVal - maxExpenseVal);


            XSSFRow thirdRow = sheet.createRow(2);
            XSSFCell lexa = thirdRow.createCell(0);
            lexa.setCellValue("Лёша");
            XSSFCell lexaIncome = thirdRow.createCell(1);
            lexaIncome.setCellValue(INCOME);
            XSSFCell lexaIncomeValue = thirdRow.createCell(2);
            lexaIncomeValue.setCellValue(lexaIncomeVal);
            XSSFCell lexaExpense = thirdRow.createCell(3);
            lexaExpense.setCellValue(EXPENSE);
            XSSFCell lexaExpenseValue = thirdRow.createCell(4);
            lexaExpenseValue.setCellValue(lexaExpenseVal);
            XSSFCell lexaBalance = thirdRow.createCell(5);
            lexaBalance.setCellValue("На балансе");
            XSSFCell lexaBalanceValue = thirdRow.createCell(6);
            lexaBalanceValue.setCellValue(lexaIncomeVal - lexaExpenseVal);

            XSSFRow verifyRow = sheet.createRow(4);
            XSSFCell verifyText = verifyRow.createCell(0);
            verifyText.setCellValue("Дата и время последней сверки: ");
            XSSFCell verifyDate = verifyRow.createCell(2);
            verifyDate.setCellValue(lastVerify.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            sheet.addMergedRegion(new CellRangeAddress(4,  4, 0, 1));

            XSSFRow fifthRow = sheet.createRow(6);
            XSSFCell maxName = fifthRow.createCell(0);
            maxName.setCellValue("Макс");
            maxName.setCellStyle(getNameStyle(workbook));
            XSSFCell lexaName = fifthRow.createCell(11);
            lexaName.setCellValue("Лёша");
            lexaName.setCellStyle(getNameStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(6,  6, 0, 9));
            sheet.addMergedRegion(new CellRangeAddress(6, 6, 11, 20));


            XSSFRow sixthRow = sheet.createRow(7);
            XSSFCell maxIncomeColumn = sixthRow.createCell(0);
            maxIncomeColumn.setCellValue(INCOME);
            maxIncomeColumn.setCellStyle(getStyle(workbook, IndexedColors.GREEN));

            XSSFCell maxIncomeDateColumn = sixthRow.createCell(1);
            maxIncomeDateColumn.setCellValue(DATE);
            maxIncomeDateColumn.setCellStyle(getStyle(workbook, IndexedColors.GREY_25_PERCENT));

            XSSFCell maxIncomeClientAndPurpose = sixthRow.createCell(2);
            maxIncomeClientAndPurpose.setCellValue("Клиент, назначение");
            maxIncomeClientAndPurpose.setCellStyle(getStyle(workbook, IndexedColors.GREY_25_PERCENT));
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 2, 4));

            XSSFCell maxExpenseColumn = sixthRow.createCell(5);
            maxExpenseColumn.setCellValue(EXPENSE);
            maxExpenseColumn.setCellStyle(getStyle(workbook, IndexedColors.RED));

            XSSFCell maxExpenseDateColumn = sixthRow.createCell(6);
            maxExpenseDateColumn.setCellValue(DATE);
            maxExpenseDateColumn.setCellStyle(getStyle(workbook, IndexedColors.GREY_25_PERCENT));

            XSSFCell maxExpenseClientAndPurpose = sixthRow.createCell(7);
            maxExpenseClientAndPurpose.setCellValue("Клиент, назначение");
            maxExpenseClientAndPurpose.setCellStyle(getStyle(workbook, IndexedColors.GREY_25_PERCENT));
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 7, 9));

            XSSFCell lexaIncomeColumn = sixthRow.createCell(11);
            lexaIncomeColumn.setCellValue(INCOME);
            lexaIncomeColumn.setCellStyle(getStyle(workbook, IndexedColors.GREEN));

            XSSFCell lexaIncomeDateColumn = sixthRow.createCell(12);
            lexaIncomeDateColumn.setCellValue(DATE);
            lexaIncomeDateColumn.setCellStyle(getStyle(workbook, IndexedColors.GREY_25_PERCENT));

            XSSFCell lexaIncomeClientAndPurpose = sixthRow.createCell(13);
            lexaIncomeClientAndPurpose.setCellValue("Клиент, назначение");
            lexaIncomeClientAndPurpose.setCellStyle(getStyle(workbook, IndexedColors.GREY_25_PERCENT));
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 13, 15));

            XSSFCell lexaExpenseColumn = sixthRow.createCell(16);
            lexaExpenseColumn.setCellValue(EXPENSE);
            lexaExpenseColumn.setCellStyle(getStyle(workbook, IndexedColors.RED));

            XSSFCell lexaExpenseDateColumn = sixthRow.createCell(17);
            lexaExpenseDateColumn.setCellValue(DATE);
            lexaExpenseDateColumn.setCellStyle(getStyle(workbook, IndexedColors.GREY_25_PERCENT));

            XSSFCell lexaExpenseClientAndPurpose = sixthRow.createCell(18);
            lexaExpenseClientAndPurpose.setCellValue("Клиент, назначение");
            lexaExpenseClientAndPurpose.setCellStyle(getStyle(workbook, IndexedColors.GREY_25_PERCENT));
            sixthRow.createCell(21);
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 18, 20));

            int operationSize = maxIncomeList.size();
            if (maxExpenseList.size() > operationSize) {
                operationSize = maxExpenseList.size();
            }
            if (lexaIncomeList.size() > operationSize) {
                operationSize = lexaIncomeList.size();
            }
            if (lexaExpenseList.size() > operationSize) {
                operationSize = lexaExpenseList.size();
            }

            for (int i = 0; i < operationSize; i++) {
                XSSFRow nextRow = sheet.createRow(i+8);

                if (i < maxIncomeList.size()) {
                    OperationDTO maxIncomeDto = maxIncomeList.get(i);
                    XSSFCell incomeValue = createCell(workbook, nextRow, 0);
                    incomeValue.setCellValue(maxIncomeDto.getValue());
                    XSSFCell date = createCell(workbook, nextRow,1);
                    date.setCellValue(maxIncomeDto.getStringDate());
                    XSSFCell client = createCell(workbook, nextRow,2);
                    client.setCellValue(maxIncomeDto.getClient() + ", " + maxIncomeDto.getPurpose());
                    sheet.addMergedRegion(new CellRangeAddress(i+8, i+8, 2, 4));
                }
                if (i < maxExpenseList.size()) {
                    OperationDTO maxExpenseDto = maxExpenseList.get(i);
                    XSSFCell expenseValue = createCell(workbook, nextRow,5);
                    expenseValue.setCellValue(maxExpenseDto.getValue());
                    XSSFCell date = createCell(workbook, nextRow,6);
                    date.setCellValue(maxExpenseDto.getStringDate());
                    XSSFCell client = createCell(workbook, nextRow,7);
                    client.setCellValue(maxExpenseDto.getClient() + ", " + maxExpenseDto.getPurpose());
                    sheet.addMergedRegion(new CellRangeAddress(i+8, i+8, 7, 9));
                }
                if (i < lexaIncomeList.size()) {
                    OperationDTO lexaIncomeDto = lexaIncomeList.get(i);
                    XSSFCell incomeValue = createCell(workbook, nextRow,11);
                    incomeValue.setCellValue(lexaIncomeDto.getValue());
                    XSSFCell date = createCell(workbook, nextRow,12);
                    date.setCellValue(lexaIncomeDto.getStringDate());
                    XSSFCell client = createCell(workbook, nextRow,13);
                    client.setCellValue(lexaIncomeDto.getClient() + ", " + lexaIncomeDto.getPurpose());
                    sheet.addMergedRegion(new CellRangeAddress(i+8, i+8, 13, 15));
                }
                if (i < lexaExpenseList.size()) {
                    OperationDTO lexaExpenseDto = lexaExpenseList.get(i);
                    XSSFCell expenseValue = createCell(workbook, nextRow,16);
                    expenseValue.setCellValue(lexaExpenseDto.getValue());
                    XSSFCell date = createCell(workbook, nextRow,17);
                    date.setCellValue(lexaExpenseDto.getStringDate());
                    XSSFCell client = createCell(workbook, nextRow,18);
                    client.setCellValue(lexaExpenseDto.getClient() + ", " + lexaExpenseDto.getPurpose());
                    sheet.addMergedRegion(new CellRangeAddress(i+8, i+8, 18, 20));
                }
            }

            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            InputStream inputStream  = new ByteArrayInputStream(outputStream.toByteArray());
            InputFile inputFile = new InputFile();
            inputFile.setMedia(inputStream, "Отчёт "
                    + LocalDateTime.now(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HHч mmм"))
                    + ".xlsx");
            outputStream.close();
            inputStream.close();
            return inputFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private XSSFCellStyle getStyle(XSSFWorkbook workbook, IndexedColors color) {
        byte[] rgb=DefaultIndexedColorMap.getDefaultRGB(color.getIndex());
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(new XSSFColor(rgb));
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle getNameStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private XSSFCell createCell(XSSFWorkbook workbook, XSSFRow row, int columnIndex) {
        XSSFCell cell = row.createCell(columnIndex);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        cell.setCellStyle(style);
        return cell;
    }
}
