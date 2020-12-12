package com.example.ibird.bean;

public class RecoResult {
    private double probability;
    private String label;
    private int id;
    private String info;

    public RecoResult(double possibility, String name) {
        this.probability = possibility;
        this.label = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
