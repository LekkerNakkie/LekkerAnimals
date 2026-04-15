package me.lekkernakkie.lekkeranimal.data;

import java.util.concurrent.ThreadLocalRandom;

public class HeadPriceRule {

    public enum Type {
        FIXED,
        RANGE
    }

    private final Type type;
    private final double fixed;
    private final double min;
    private final double max;

    public HeadPriceRule(Type type, double fixed, double min, double max) {
        this.type = type == null ? Type.FIXED : type;
        this.fixed = Math.max(0.0D, fixed);

        double normalizedMin = Math.max(0.0D, min);
        double normalizedMax = Math.max(0.0D, max);

        if (normalizedMax < normalizedMin) {
            double temp = normalizedMin;
            normalizedMin = normalizedMax;
            normalizedMax = temp;
        }

        this.min = normalizedMin;
        this.max = normalizedMax;
    }

    public Type getType() {
        return type;
    }

    public double getFixed() {
        return fixed;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double rollPrice() {
        return switch (type) {
            case FIXED -> fixed;
            case RANGE -> {
                if (max <= min) {
                    yield min;
                }
                yield ThreadLocalRandom.current().nextDouble(min, max + 0.0000001D);
            }
        };
    }

    public double getMinPossible() {
        return type == Type.FIXED ? fixed : min;
    }

    public double getMaxPossible() {
        return type == Type.FIXED ? fixed : max;
    }

    public boolean isConfigured() {
        return getMaxPossible() > 0.0D;
    }
}