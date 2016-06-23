package model;

import utils.OsUtils;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Ciprian on 4/26/2016.
 */
public class MathUtils {

    private static void callActionOnSplit(String innerText, String splitText, Consumer<String> action) {

        String[] stringOfMarks = innerText.split(splitText);
        for (String str : stringOfMarks) {
            if (OsUtils.isNullOrEmpty(str.trim())) {
                continue;
            }
            action.accept(str);
        }
    }

    private static List<Float> parseFloatArrayFromString(String innerText) {
        List<Float> result = new ArrayList<>();
        callActionOnSplit(innerText, " ", s -> {
            Float flt = Float.parseFloat(s);
            result.add(flt);
        });
        return result;
    }

    public static String parseFloatToCombinedString(String innerText) {
        List<Float> data = parseFloatArrayFromString(innerText);
        String arrayData
                = StringUtils.join(",", data.stream().map(aFloat -> Float.toString(aFloat) + "f"));
        return arrayData;
    }

    private static List<Integer> parseIntegerArrayFromString(String innerText) {
        List<Integer> result = new ArrayList<>();
        callActionOnSplit(innerText, " ", s -> {
            int flt = Integer.parseInt(s);
            result.add(flt);
        });
        return result;
    }

    public static String parseIntToCombinedString(String innerText) {
        List<Integer> data = parseIntegerArrayFromString(innerText);
        String arrayData
                = StringUtils.join(",", data.stream().map(aFloat -> Integer.toString(aFloat)));
        return arrayData;
    }

    private static List<Double> parseDoubleArrayFromString(String innerText) {
        List<Double> result = new ArrayList<>();
        callActionOnSplit(innerText, " ", s -> {
            double flt = Double.parseDouble(s);
            result.add(flt);
        });
        return result;
    }

    public static String parseDoubleToCombinedString(String innerText) {
        List<Integer> data = parseIntegerArrayFromString(innerText);
        String arrayData
                = StringUtils.join(",", data.stream().map(aFloat -> Integer.toString(aFloat)));
        return arrayData;
    }
}
