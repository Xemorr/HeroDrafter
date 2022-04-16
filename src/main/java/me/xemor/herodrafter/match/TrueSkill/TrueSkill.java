package me.xemor.herodrafter.match.TrueSkill;

import com.google.gson.annotations.SerializedName;

import java.io.Serial;

/*
    Equivalent to GameInfo in original documentation: https://github.com/moserware/Skills/blob/ee312fd39db913de2fc297951dc663e12dc7e504/Skills/GameInfo.cs
 */
public class TrueSkill {

    private double beta;
    @SerializedName(value = "draw_probability")
    private double drawProbability;
    @SerializedName(value = "dynamics_factor")
    private double dynamicsFactor;
    private double mean;
    @SerializedName(value = "standard_deviation")
    private double standardDeviation;


    public double getBeta() {
        return beta;
    }

    public double getDrawProbability() {
        return drawProbability;
    }

    public double getDynamicsFactor() {
        return dynamicsFactor;
    }

    public double getMean() {
        return mean;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }
}
