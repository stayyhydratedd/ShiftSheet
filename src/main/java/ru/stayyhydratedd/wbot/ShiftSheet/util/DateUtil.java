package ru.stayyhydratedd.wbot.ShiftSheet.util;

import org.springframework.stereotype.Component;
import ru.stayyhydratedd.wbot.ShiftSheet.models.MonthSheet;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DateUtil {

    public int getDaysInMonth(MonthSheet monthSheet) {
        YearMonth yearMonth = getYearMonth(monthSheet);
        return yearMonth.lengthOfMonth();
    }

    public String getFullMonthSheetDate(MonthSheet monthSheet){
        String monthName = Month.of(monthSheet.getMonth())
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"));
        String monthYear = monthSheet.getYear().toString();
        return monthName + " " + monthYear.substring(monthYear.length() - 2);
    }

    public List<String> getDaysOfWeek(MonthSheet monthSheet) {
        YearMonth yearMonth = getYearMonth(monthSheet);
        List<String> daysOfWeek = new ArrayList<>();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            daysOfWeek.add(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("ru")));
        }
        return daysOfWeek;
    }

    private YearMonth getYearMonth(MonthSheet monthSheet) {
        return YearMonth.of(monthSheet.getYear(), monthSheet.getMonth());
    }
}
