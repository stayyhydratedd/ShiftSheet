package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.sheets.v4.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.models.MonthSheet;
import ru.stayyhydratedd.wbot.ShiftSheet.models.Pwz;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.MonthSheetRepository;
import ru.stayyhydratedd.wbot.ShiftSheet.util.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MonthSheetService {

    private final MonthSheetRepository monthSheetRepository;
    private final SessionContext sessionContext;
    private final PwzService pwzService;
    private final GoogleFileWorkerUtil googleFileWorkerUtil;
    private final JColorUtil jColorUtil;
    private final DateUtil dateUtil;
    private final CellsUtil cellsUtil;
    private final PrinterUtil printer;
    private final HelperUtil helper;

    public void save(MonthSheet monthSheet) {
        monthSheetRepository.save(monthSheet);
    }

    public List<MonthSheet> findAll(){
        return monthSheetRepository.findAll();
    }

    public void createMonthSheet() {
        if (sessionContext.getCurrentPwz().isEmpty()){
            System.out.printf("%sУ вас не указан ПВЗ", jColorUtil.WARN);
            return;
        }
        Optional<Pwz> foundPwz = pwzService.findById(sessionContext.getCurrentPwz().get().getId());
        if(foundPwz.isEmpty()){
            System.out.printf("%sВозникла непредвиденная ошибка", jColorUtil.ERROR);
            return;
        }
        Pwz pwz = foundPwz.get();

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        double payRate = pwz.getPayRate();

        MonthSheet monthSheet = MonthSheet.builder()
                .googleId(pwz.getGoogleId())
                .year(year)
                .month(month)
                .payRate(payRate)
                .pwz(pwz)
                .build();

        save(monthSheet);
        executeCreateMonthSheetInPwzSpreadsheet(monthSheet);
        executeFormatMonthSheetInPwzSpreadsheet(monthSheet);
        executeDeleteZeroIdSheet(monthSheet);
    }

    public void executeCreateMonthSheetInPwzSpreadsheet(MonthSheet monthSheet) {

        String year = monthSheet.getYear().toString();
        String month = monthSheet.getMonth().toString();

        if(month.length() == 1)
            month = "0" + month;

        Integer sheetId = Integer.parseInt(year.substring(year.length() - 2) + month);

        int daysInMonth = dateUtil.getDaysInMonth(monthSheet);

        List<Request> requests = Arrays.asList(
                new Request().setAddSheet(  //смена id листа, имени и количества строк с колоннами
                        new AddSheetRequest().setProperties(
                                new SheetProperties().setGridProperties(
                                                new GridProperties()
                                                        .setColumnCount(daysInMonth + 2)
                                                        .setRowCount(15)
                                                        .setFrozenColumnCount(1)    //заморозка одной колонны слева
                                                        .setFrozenRowCount(3)   //заморозка трех строк сверху
                                        )
                                        .setSheetId(sheetId)
                                        .setTitle(dateUtil.getFullMonthSheetDate(monthSheet))
                        )
                ),
                new Request().setRepeatCell(    //12 шрифт для первой строки
                        new RepeatCellRequest().setCell(
                                new CellData().setUserEnteredFormat(
                                        new CellFormat().setTextFormat(
                                                new TextFormat().setFontSize(12)
                                        ).setHorizontalAlignment("center")
                                )
                        ).setRange(
                                new GridRange()
                                        .setSheetId(sheetId)
                                        .setStartRowIndex(0)
                                        .setEndRowIndex(1)
                                        .setStartColumnIndex(0)
                                        .setEndColumnIndex(daysInMonth + 3)
                        ).setFields("*")
                ),
                new Request().setRepeatCell(    //11 шрифт для второй и третей строк
                        new RepeatCellRequest().setCell(
                                new CellData().setUserEnteredFormat(
                                        new CellFormat().setTextFormat(
                                                new TextFormat().setFontSize(11)
                                        ).setHorizontalAlignment("center")
                                )
                        ).setRange(
                                new GridRange()
                                        .setSheetId(sheetId)
                                        .setStartRowIndex(1)
                                        .setEndRowIndex(3)
                                        .setStartColumnIndex(0)
                                        .setEndColumnIndex(daysInMonth + 3)
                        ).setFields("*")
                ),
                new Request().setRepeatCell(    //выравнивание по горизонтали на центр для всех ячеек
                        new RepeatCellRequest().setCell(
                                new CellData().setUserEnteredFormat(
                                        new CellFormat().setHorizontalAlignment("center")
                                )
                        ).setRange(
                                new GridRange()
                                        .setSheetId(sheetId)
                                        .setStartRowIndex(3)
                                        .setEndRowIndex(15)
                                        .setStartColumnIndex(0)
                                        .setEndColumnIndex(daysInMonth + 2)
                        ).setFields("*")
                ),
                new Request().setRepeatCell(    //красим последние ячейки во второй и третей строке в рандомный цвет
                        new RepeatCellRequest().setCell(
                                new CellData().setUserEnteredFormat(
                                        new CellFormat().setBackgroundColor(
                                                        new Color()
                                                                .setGreen(new Random().nextFloat(0.8f, 0.99f))
                                                                .setBlue(new Random().nextFloat(0.8f, 0.99f))
                                                                .setRed(new Random().nextFloat(0.8f, 0.99f))
                                                ).setTextFormat(
                                                        new TextFormat()
                                                                .setFontSize(11)
                                                ).setHorizontalAlignment("center")
                                                .setVerticalAlignment("bottom")
                                )
                        ).setRange(
                                new GridRange()
                                        .setSheetId(sheetId)
                                        .setStartRowIndex(1)
                                        .setEndRowIndex(3)
                                        .setStartColumnIndex(daysInMonth + 1)
                                        .setEndColumnIndex(daysInMonth + 2)
                        ).setFields("*")
                ),
                new Request().setRepeatCell(    //красим первые ячейки во второй и третей строке в рандомный цвет
                        new RepeatCellRequest().setCell(
                                new CellData().setUserEnteredFormat(
                                        new CellFormat().setBackgroundColor(
                                                        new Color()
                                                                .setGreen(new Random().nextFloat(0.8f, 0.99f))
                                                                .setBlue(new Random().nextFloat(0.8f, 0.99f))
                                                                .setRed(new Random().nextFloat(0.8f, 0.99f))
                                                ).setTextFormat(
                                                        new TextFormat()
                                                                .setFontSize(11)
                                                ).setHorizontalAlignment("center")
                                                .setVerticalAlignment("bottom")
                                )
                        ).setRange(
                                new GridRange()
                                        .setSheetId(sheetId)
                                        .setStartRowIndex(1)
                                        .setEndRowIndex(3)
                                        .setStartColumnIndex(0)
                                        .setEndColumnIndex(1)
                        ).setFields("*")
                ),
                new Request().setMergeCells(    //объединение ячеек с 1 по 16 в первой строке
                        new MergeCellsRequest().setRange(
                                new GridRange()
                                        .setStartRowIndex(0)
                                        .setEndRowIndex(1)
                                        .setStartColumnIndex(1)
                                        .setEndColumnIndex(16)
                                        .setSheetId(sheetId)
                        )
                ),
                new Request().setMergeCells(    //объединение ячеек с 16 по последнюю во второй строке
                        new MergeCellsRequest().setRange(
                                new GridRange()
                                        .setStartRowIndex(0)
                                        .setEndRowIndex(1)
                                        .setStartColumnIndex(16)
                                        .setEndColumnIndex(daysInMonth + 2)
                                        .setSheetId(sheetId)
                        )
                ),
                new Request().setMergeCells(    //объединение последних ячеек во второй и третей строках
                        new MergeCellsRequest().setRange(
                                new GridRange()
                                        .setStartRowIndex(1)
                                        .setEndRowIndex(3)
                                        .setStartColumnIndex(daysInMonth + 1)
                                        .setEndColumnIndex(daysInMonth + 2)
                                        .setSheetId(sheetId)
                        )
                ),
                new Request().setMergeCells(    //объединение первых ячеек во второй и третей строках
                        new MergeCellsRequest().setRange(
                                new GridRange()
                                        .setStartRowIndex(1)
                                        .setEndRowIndex(3)
                                        .setStartColumnIndex(0)
                                        .setEndColumnIndex(1)
                                        .setSheetId(sheetId)
                        )
                ),
                new Request().setUpdateDimensionProperties(     //88 пикселей для всех колонн
                        new UpdateDimensionPropertiesRequest().setRange(
                                new DimensionRange()
                                        .setSheetId(sheetId)
                                        .setDimension("columns")
                                        .setStartIndex(0)
                                        .setEndIndex(daysInMonth + 3)
                        ).setProperties(
                                new DimensionProperties()
                                        .setPixelSize(88)
                        ).setFields("*")
                ),
                new Request().setUpdateDimensionProperties(     //25 пикселей для всех строк
                        new UpdateDimensionPropertiesRequest().setRange(
                                new DimensionRange()
                                        .setSheetId(sheetId)
                                        .setDimension("rows")
                                        .setStartIndex(0)
                                        .setEndIndex(16)
                        ).setProperties(
                                new DimensionProperties()
                                        .setPixelSize(25)
                        ).setFields("*")
                )
        );
        try{
            googleFileWorkerUtil.executeRequestsBody(requests, monthSheet.getGoogleId());
            System.out.printf("Лист в таблице успешно создан\n"); //todo
        } catch (Exception e){
            System.out.printf("Не удалось создать лист в таблице\n"); //todo

        }
    }

    public void executeDeleteZeroIdSheet(MonthSheet monthSheet) {

        List<Sheet> sheets = pwzService.getMonthSheets(monthSheet.getGoogleId());
        for(Sheet sheet : sheets){
            if(sheet.getProperties().getSheetId() == 0){
                List<Request> requests = List.of(
                        new Request().setDeleteSheet(
                                new DeleteSheetRequest().setSheetId(0)
                        )
                );
                try {
                    googleFileWorkerUtil.executeRequestsBody(requests, monthSheet.getGoogleId());
                } catch (IOException e) {
                    System.out.printf("%sНе удалось удалить нулевой лист\n", jColorUtil.WARN);
                }
            }
        }
    }

    public void executeFormatMonthSheetInPwzSpreadsheet(MonthSheet monthSheet) {

        List<String> daysOfWeek = dateUtil.getDaysOfWeek(monthSheet);
        String fullMonthSheetDate = dateUtil.getFullMonthSheetDate(monthSheet); //example июнь 25, май 24
        String sheetTitleForExecution = fullMonthSheetDate + "!";
        int daysInMonth = dateUtil.getDaysInMonth(monthSheet);

        List<Object> datesRow = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            datesRow.add(String.valueOf(i));
        }

        List<Object> weekdaysRow = new ArrayList<>(daysOfWeek);

        String rangeDatesAndWeekdays = sheetTitleForExecution + cellsUtil
                .getCellsRange(2, 2, daysInMonth + 2, 3);
        String rangeNameHeader = sheetTitleForExecution + cellsUtil
                .getCellsRange(1, 2, 1, 3);
        String rangeHoursHeader = sheetTitleForExecution + cellsUtil
                .getCellsRange(daysInMonth + 2, 2, daysInMonth + 2, 3);
        String rangeMonthTitle = sheetTitleForExecution + cellsUtil
                .getCellsRange(2, 1, 16, 1);
        String rangeMonthColumn = sheetTitleForExecution + cellsUtil
                .getCellsRange(17, 1, daysInMonth + 1, 1);

        List<ValueRange> data = List.of(
                createValueRange(rangeDatesAndWeekdays, List.of(datesRow, weekdaysRow)),
                createValueRange(rangeNameHeader, List.of(List.of("Имя"))),
                createValueRange(rangeHoursHeader, List.of(List.of("Кол-во\nчасов"))),
                createValueRange(rangeMonthTitle, List.of(List.of(fullMonthSheetDate))),
                createValueRange(rangeMonthColumn, List.of(List.of(fullMonthSheetDate)))
        );

        try {
            googleFileWorkerUtil.executeDataBody(data, monthSheet.getGoogleId());
        } catch (IOException e) {
//            System.out.printf(TextColour.ERROR + "Таблица с id '%s' не была найдена\n", spreadsheetId); todo
        }
    }
    private ValueRange createValueRange(String range, List<List<Object>> values) {
        return new ValueRange().setRange(range).setValues(values);
    }

}