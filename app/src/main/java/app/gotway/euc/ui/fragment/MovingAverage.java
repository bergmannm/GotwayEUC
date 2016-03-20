package app.gotway.euc.ui.fragment;

/**
 * Created by jack on 3/19/2016.
 */
public class MovingAverage {

    private float value;

    private float coef;
    private float oneMinusCoef;

    public void reset(float coef, float initVal) {
        if (coef<=0 || coef>=1) {
            throw new IllegalArgumentException("coef");
        }
        this.coef = coef;
        this.oneMinusCoef = 1 - coef;
        this.value = initVal;
    }


    public void add(float newValue) {
        value = coef *newValue + oneMinusCoef * value;
    }

    public float get() {
        return value;
    }

    public float getCoef() {
        return coef;
    }
}
