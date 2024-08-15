package com.stambul.library.tools;

import java.math.BigInteger;

public class ConvertingTools {

    public static Integer toInt(Object object) {
        if (object == null)
            return null;

        if (object instanceof Integer)
            return (int) object;
        if (object instanceof Long)
            return ((Long) object).intValue();
        if (object instanceof BigInteger)
            return ((BigInteger) object).intValue();
        throw new IllegalArgumentException("Unexpected data type: " + object);
    }

    public static Double toDouble(Object object) {
        if (object == null)
            return null;

        if (object instanceof Double)
            return (double) object;
        if (object instanceof Integer)
            return ((Integer) object).doubleValue();
        if (object instanceof Long)
            return ((Long) object).doubleValue();
        if (object instanceof BigInteger)
            return ((BigInteger) object).doubleValue();
        throw new IllegalArgumentException("Unexpected data type: " + object);
    }
}
