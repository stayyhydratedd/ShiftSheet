package ru.stayyhydratedd.wbot.ShiftSheet.util;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Scanner;

@RequiredArgsConstructor
public class InputOutputUtil {

    private final JColorUtil colorUtil;

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

    public String parseInput(Scanner scanner, String... regexps) {
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
}
