package com.piedpiper.barchart;

public class PathAttributes {

    String pathColor = "#0000FF"; 
    String pointColor = "#FF0000";

    float strokeWidthOfPath = 2;
    float radiusOfPoints = 3;

    String pathName;

    public String getPathColor() {
        return pathColor;
    }

    public void setPathColor(String pathColor) {
        this.pathColor = pathColor;
    }

    public String getPointColor() {
        return pointColor;
    }

    public void setPointColor(String pointColor) {
        this.pointColor = pointColor;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public float getStrokeWidthOfPath() {
        return strokeWidthOfPath;
    }

    public void setStrokeWidthOfPath(float strokeWidthOfPath) {
        this.strokeWidthOfPath = strokeWidthOfPath;
    }

    public float getRadiusOfPoints() {
        return radiusOfPoints;
    }

    public void setRadiusOfPoints(float radiusOfPoints) {
        this.radiusOfPoints = radiusOfPoints;
    }
}