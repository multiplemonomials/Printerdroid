package com.piedpiper.barchart;

import android.graphics.Bitmap;

public class LineChartAttributes {

    boolean gridVisible = true;

    float X_unit_value = 1;
    float Y_unit_value = 1;

//  String X_unit_name = "Horizontal axixs";
//  String Y_unit_name = "Vertical axixs";

    String X_unit_name = "X";
    String Y_unit_name = "Y";

    String backgroundColor = "#FFFFFFFF";
    String axisColor = "#FF000000";
    String gridColor = "#88555555";

    String axisNameColor = "#FF000000";
    String axisUnitColor = "#88555555";

    Bitmap background;

    public float axisStrokeWidth = 1.25f;
    public float gridStrokeWidth = 0.25f;

    public boolean isGridVisible() {
        return gridVisible;
    }
    public void setGridVisible(boolean gridVisible) {
        this.gridVisible = gridVisible;
    }
    public float getX_unit_value() {
        return X_unit_value;
    }
    public void setX_unit_value(float x_unit_value) {
        X_unit_value = x_unit_value;
    }
    public float getY_unit_value() {
        return Y_unit_value;
    }
    public void setY_unit_value(float y_unit_value) {
        Y_unit_value = y_unit_value;
    }
    public String getX_unit_name() {
        return X_unit_name;
    }
    public void setX_unit_name(String x_unit_name) {
        X_unit_name = x_unit_name;
    }
    public String getY_unit_name() {
        return Y_unit_name;
    }
    public void setY_unit_name(String y_unit_name) {
        Y_unit_name = y_unit_name;
    }
    public String getBackgroundColor() {
        return backgroundColor;
    }
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    public String getAxisColor() {
        return axisColor;
    }
    public void setAxisColor(String axisColor) {
        this.axisColor = axisColor;
    }
    public String getGridColor() {
        return gridColor;
    }
    public void setGridColor(String gridColor) {
        this.gridColor = gridColor;
    }
    public Bitmap getBackground() {
        return background;
    }
    public void setBackground(Bitmap background) {
        this.background = background;
    }
}