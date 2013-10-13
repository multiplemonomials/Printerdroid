package com.piedpiper.barchart;

import java.util.ArrayList;

public class PathOnChart {

    PathAttributes attributes;

    ArrayList<PointOnChart> points;

    public PathOnChart(ArrayList<PointOnChart> points, PathAttributes pathAttributes) {

        this.attributes = pathAttributes;
        this.points = points;
    }
}