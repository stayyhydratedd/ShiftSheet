package ru.stayyhydratedd.wbot.ShiftSheet.util;

import com.diogonunes.jcolor.Attribute;
import com.sun.jna.Function;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.TEXT_COLOR;

public class JColorUtil {

    private static final int ERROR_COLOR = 9;
    private static final int WARN_COLOR = 221;
    private static final int INFO_COLOR = 75;
    private static final int SUCCESS_COLOR = 41;
    private static final int IN_PROCESS_COLOR = 213;

    public String ERROR = "[" + colorize("ERROR", TEXT_COLOR(ERROR_COLOR)) + "] ";
    public String WARN = "[" + colorize("WARN", TEXT_COLOR(WARN_COLOR)) + "] ";
    public String INFO = "[" + colorize("INFO", TEXT_COLOR(INFO_COLOR)) + "] ";
    public String SUCCESS = "[" + colorize("SUCCESS", TEXT_COLOR(SUCCESS_COLOR)) + "] ";
    public String IN_PROCESS = "[" + colorize("IN_PROCESS", TEXT_COLOR(IN_PROCESS_COLOR)) + "] ";

    public enum COLOR {
        ERROR, WARN, INFO, SUCCESS, IN_PROCESS
    }

    private static final Map<COLOR, Integer> colorMap = new HashMap<>() {{
        put(COLOR.ERROR, ERROR_COLOR);
        put(COLOR.WARN, WARN_COLOR);
        put(COLOR.INFO, INFO_COLOR);
        put(COLOR.SUCCESS, SUCCESS_COLOR);
        put(COLOR.IN_PROCESS, IN_PROCESS_COLOR);
    }};

    public String turnTextIntoColor(String text, COLOR color) {
        return colorize(text, TEXT_COLOR(colorMap.get(color)));
    }

    @PostConstruct
    public void enableWindows10AnsiSupport() {
        Function GetStdHandleFunc = Function.getFunction("kernel32", "GetStdHandle");
        WinDef.DWORD STD_OUTPUT_HANDLE = new WinDef.DWORD(-11);
        WinNT.HANDLE hOut = (WinNT.HANDLE) GetStdHandleFunc.invoke(WinNT.HANDLE.class, new Object[]{STD_OUTPUT_HANDLE});

        WinDef.DWORDByReference p_dwMode = new WinDef.DWORDByReference(new WinDef.DWORD(0));
        Function GetConsoleModeFunc = Function.getFunction("kernel32", "GetConsoleMode");
        GetConsoleModeFunc.invoke(WinDef.BOOL.class, new Object[]{hOut, p_dwMode});

        int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
        WinDef.DWORD dwMode = p_dwMode.getValue();
        dwMode.setValue(dwMode.intValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
        Function SetConsoleModeFunc = Function.getFunction("kernel32", "SetConsoleMode");
        SetConsoleModeFunc.invoke(WinDef.BOOL.class, new Object[]{hOut, dwMode});
    }

    public void printLogo() {
        int greenColor = 41;
        int whiteColor = 15;

        Attribute green = TEXT_COLOR(greenColor);
        Attribute white = TEXT_COLOR(whiteColor);

        String logo = """
                /**
                *  ________________________\s
                * ||        Shift         ||
                * ||______________________||
                * |/______________________\\|
                *  ____ ____ ____ ____ ____\s
                * ||S |||h |||e |||e |||t ||
                * ||__|||__|||__|||__|||__||
                * |/__\\|/__\\|/__\\|/__\\|/__\\|
                * .........by stayyhydratedd\s
                */
                
                """;

        logo.lines().map(line -> {
            StringBuilder sb = new StringBuilder();
            for (char c : line.toCharArray()) {
                String s = Character.toString(c);
                if (s.matches("[A-Za-z.]"))
                    sb.append(colorize(s, white));
                else
                    sb.append(colorize(s, green));
            }
            return sb.toString();
        }).forEach(line -> {
            System.out.println(line);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
