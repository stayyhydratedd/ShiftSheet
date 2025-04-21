package ru.stayyhydratedd.wbot.ShiftSheet.util;

import org.springframework.stereotype.Component;

@Component
public class CellsUtil {

    public String getCellsRange(int colNumFrom, int rowNumFrom, int colNumTill, int rowNumTill) {
        if (colNumFrom > colNumTill || rowNumFrom > rowNumTill) {
            throw new IllegalArgumentException("Start column/row must not be greater than end column/row");
        }

        String cellFrom = getCellAddress(colNumFrom, rowNumFrom);
        String cellTo = getCellAddress(colNumTill, rowNumTill);

        return cellFrom + ":" + cellTo;
    }

    public String getCell(int colNum, int rowNum) {
        return getCellAddress(colNum, rowNum);
    }

    private String getCellAddress(int colNum, int rowNum) {
        return getColumnLetter(colNum) + rowNum;
    }

    // Конвертирует число в Excel-подобную букву столбца
    private String getColumnLetter(int colNum) {
        if (colNum <= 0) throw new IllegalArgumentException("Column number must be >= 1");

        StringBuilder sb = new StringBuilder();
        while (colNum > 0) {
            colNum--; // Excel: 1 -> A, 26 -> Z, 27 -> AA, и т.д.
            sb.insert(0, (char) ('A' + (colNum % 26)));
            colNum /= 26;
        }
        return sb.toString();
    }
}
