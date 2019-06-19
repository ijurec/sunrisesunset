package com.task.sunrisesunset.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtil {

    public static String roundingRank(double rank) {
        double rankDouble = rank;
        BigDecimal bigDecimal = new BigDecimal(rankDouble);
        BigDecimal newDecimal = bigDecimal.setScale(7, RoundingMode.DOWN);
        rankDouble = newDecimal.doubleValue();
        return String.valueOf(rankDouble);
    }
}
