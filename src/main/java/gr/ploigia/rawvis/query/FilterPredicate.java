package gr.ploigia.rawvis.query;

import java.util.function.Predicate;

public class FilterPredicate implements Predicate<Float> {

    private final FilterOperator operator;

    private final float constant;

    public FilterPredicate(FilterOperator operator, float constant) {
        this.operator = operator;
        this.constant = constant;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public float getConstant() {
        return constant;
    }

    @Override
    public boolean test(Float value) {
        if (value == null) {
            return false;
        }
        switch (operator) {
            case EQUAL:
                return value.equals(constant);
            case LESS_THAN:
                return value < constant;
            case NOT_EQUAL:
                return value != constant;
            case GREATER_THAN:
                return value > constant;
            case LESS_THAN_OR_EQUAL:
                return value <= constant;
            case GREATER_THAN_OR_EQUAL:
                return value >= constant;
            default:
                return false;
        }
    }
}
