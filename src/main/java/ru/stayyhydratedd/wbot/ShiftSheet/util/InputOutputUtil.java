package ru.stayyhydratedd.wbot.ShiftSheet.util;

import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;
import ru.stayyhydratedd.wbot.ShiftSheet.enums.ConditionContext;

import java.util.*;

@RequiredArgsConstructor
public class InputOutputUtil {

    private final JColorUtil jColorUtil;
    private final HelperUtil helper;

    private final Scanner scanner = new Scanner(System.in);

    private static final Map<String, String> COMMANDS = new HashMap<>(){{
        put("help", "/help");
        put("back", "/back");
    }};

    public void printSequenceFromString(String message) {
        int outputDelay = 15;
        for (char c : message.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(outputDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String parseInputRaw(Scanner scanner, String... regexps) {
        String inputString = scanner.nextLine();
        long regexpsCount = Arrays.stream(regexps).count();
        if (regexpsCount == 0) {
            return inputString;
        }
        int unmatchedRegexps = 0;
        for (String regexp : regexps) {
            if (!inputString.matches(regexp)) {
                unmatchedRegexps++;
            }
        }
        if (unmatchedRegexps == regexpsCount) {
            return null;
        } else {
            return inputString;
        }
    }

    public Optional<String> parseInput(@Language("RegExp") String... regexps) {
        System.out.print(jColorUtil.turnTextIntoColor(">", JColorUtil.COLOR.INFO));

        return Optional.ofNullable(parseInputRaw(scanner, regexps));
    }
    public boolean askYesOrNo(String question, String fieldName, JColorUtil.COLOR color) {
        boolean firstInputAttempt = true;
        while (true){
            if(firstInputAttempt) {
                String ask;
                if (fieldName.matches("'.+'")){
                    fieldName = fieldName.substring(1, fieldName.length() - 1);
                    ask = "%s%s '%s'";
                } else if (fieldName.isEmpty())
                    ask = "%s%s%s";
                else
                    ask = "%s%s %s";
                String alert = "[empty]";

                if (color == JColorUtil.COLOR.INFO) {
                    alert = jColorUtil.INFO;
                } else if (color == JColorUtil.COLOR.WARN) {
                    alert = jColorUtil.WARN;
                }
                System.out.printf(ask + "? (%s/%s)\n", alert,
                        question,
                        jColorUtil.turnTextIntoColor(fieldName, color),
                        jColorUtil.turnTextIntoColor("y", JColorUtil.COLOR.INFO),
                        jColorUtil.turnTextIntoColor("n", JColorUtil.COLOR.INFO));
            }
            firstInputAttempt = false;

            Optional<String> parsed = parseInput("[yYnN]", "/help");
            if (parsed.isEmpty()) {
                System.out.printf("%sНедопустимое значение\n", jColorUtil.ERROR);
                continue;
            }
            if (parsed.get().matches(COMMANDS.get("help"))) {
                helper.getHelp(ConditionContext.ASK_YES_OR_NO);
                continue;
            }
            String answer = parsed.get();
            return answer.equalsIgnoreCase("y");
        }
    }
}
