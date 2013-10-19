package com.piedpiper.barchart;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;

public class LineChartView extends ChartView {

    private float no_of_x_positive_unit;
    private float no_of_y_positive_unit;

    private float no_of_x_negetive_unit;
    private float no_of_y_negetive_unit;

    private float x_unit_length;
    private float y_unit_length;

    private float x_translate_factor;
    private float y_translate_factor;

    private ArrayList<PathOnChart> paths;
    private LineChartAttributes lineChartAttributes;

    public LineChartView(Activity activity, ArrayList<PathOnChart> paths, LineChartAttributes lineChartAttributes) {
        super(activity);
        this.paths = configurePaths(paths);
        this.lineChartAttributes = lineChartAttributes;
    }

    private ArrayList<PathOnChart> configurePaths(ArrayList<PathOnChart> paths) {

        double minimum_x = 0;
        double maximum_x = 0;

        double minimum_y = 0;
        double maximum_y = 0;

        ArrayList<PathOnChart> paths_configured = paths;

        for (PathOnChart pathOnChart : paths_configured) {

            ArrayList<PointOnChart> points = pathOnChart.points;

            for (PointOnChart pointOnChart : points) {

                if(pointOnChart.x < minimum_x) minimum_x = pointOnChart.x;
                if(pointOnChart.y < minimum_y) minimum_y = pointOnChart.y;
                if(pointOnChart.x > maximum_x) maximum_x = pointOnChart.x;
                if(pointOnChart.y > maximum_y) maximum_y = pointOnChart.y;

            }
        }

        no_of_x_negetive_unit = (float) Math.abs(minimum_x);
        no_of_x_positive_unit = (float) Math.abs(maximum_x);

        no_of_y_negetive_unit = (float) Math.abs(minimum_y);
        no_of_y_positive_unit = (float) Math.abs(maximum_y);

        x_unit_length = (float) (chart_width / (no_of_x_negetive_unit + no_of_x_positive_unit));
        y_unit_length = (float) (chart_height / (no_of_y_negetive_unit+no_of_y_positive_unit));

        //x_translate_factor = offset_left;
        y_translate_factor = display_height - offset_bottom;

        x_translate_factor = (no_of_x_negetive_unit*x_unit_length)+offset_left;
        y_translate_factor = display_height - (no_of_y_negetive_unit*y_unit_length) - offset_bottom;

        for (PathOnChart pathOnChart : paths_configured) {

            ArrayList<PointOnChart> points = pathOnChart.points;

            for (PointOnChart pointOnChart : points) {

                //System.out.println("(x_translate_factor, x_unit_length)"+"("+x_translate_factor+","+x_unit_length+")");
                //System.out.println("(y_translate_factor, y_unit_length)"+"("+y_translate_factor+","+y_unit_length+")");
                //System.out.println("Before : (x, y)"+"("+pointOnChart.x+","+pointOnChart.y+")");

                pointOnChart.x = x_translate_factor + ((float)(pointOnChart.x) * x_unit_length);
                pointOnChart.y = y_translate_factor - ((float)(pointOnChart.y) * y_unit_length);

                System.out.println("After : (x, y)"+"("+pointOnChart.x+","+pointOnChart.y+")");

            }
        }

        return paths;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor(lineChartAttributes.backgroundColor));
        if(lineChartAttributes.background != null) canvas.drawBitmap(lineChartAttributes.background, 0, 0, paint);
        if(lineChartAttributes.gridVisible) drawGrid(canvas);
        drawAxis(canvas);
        drawPoints(canvas);
        drawPhaths(canvas);
        drawAxisNames(canvas);
        //drawAxisUnits(canvas);
    }


    private void drawPhaths(Canvas canvas) {
        for (PathOnChart pathOnChart : paths) {
            paint.setColor(Color.parseColor(pathOnChart.attributes.pointColor));
            Path path = new Path();
            PointOnChart p1 = pathOnChart.points.get(0);
            path.moveTo(p1.x, p1.y);
            for (PointOnChart point : pathOnChart.points) {

                path.quadTo(p1.x, p1.y, point.x, point.y); 
                p1 = point;
                //System.out.println("Point x = " + p1.x + " y = " + p1.y);

                path.lineTo(p1.x, p1.y);

                super.paint.setColor(Color.parseColor(pathOnChart.attributes.pathColor));
                super.paint.setStrokeWidth(pathOnChart.attributes.strokeWidthOfPath);
                super.paint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(path, paint);
            }
        }
    }


    private void drawAxisNames(Canvas canvas) {
        paint.setColor(Color.parseColor(lineChartAttributes.axisNameColor));
        paint.setStrokeWidth(lineChartAttributes.axisStrokeWidth);

        Path path = new Path();
        //RectF rect = new RectF(offset_left*0.33f, y_translate_factor+(lineChartAttributes.Y_unit_name.length()*5), offset_left*0.33f, y_translate_factor-(lineChartAttributes.Y_unit_name.length()*5));
        RectF rect = new RectF(xbl - offset_left*0.50f, ybl, xtl - offset_left*0.50f, ytl);
        path.addRect(rect , Direction.CCW);
        canvas.drawTextOnPath(lineChartAttributes.Y_unit_name, path, 0, 0, paint);

        path = new Path();
        //rect = new RectF(x_translate_factor-(lineChartAttributes.X_unit_name.length()*5), display_height-offset_bottom*0.33f, x_translate_factor+(lineChartAttributes.X_unit_name.length()*5), display_height-offset_bottom*0.33f);
        rect = new RectF(xbl, ybl + offset_bottom*0.60f, xbr, ybr + offset_bottom*0.60f); 
        path.addRect(rect , Direction.CW);
        canvas.drawTextOnPath(lineChartAttributes.X_unit_name, path, 0, 0, paint);
    }

    private void drawPoints(Canvas canvas) {
        for (PathOnChart path : paths) {
            paint.setColor(Color.parseColor(path.attributes.pointColor));
            for (PointOnChart point : path.points) {
                canvas.drawCircle((float)point.x, (float)point.y, path.attributes.radiusOfPoints, paint);
            }
        }
    }

    private void drawAxis(Canvas canvas) {

        paint.setColor(Color.parseColor(lineChartAttributes.axisColor));
        paint.setStrokeWidth(lineChartAttributes.axisStrokeWidth);
        //y
        canvas.drawLine((float) (xtl+(no_of_x_negetive_unit*x_unit_length)), ytl, (float) (xbl+(no_of_x_negetive_unit*x_unit_length)), ybl, paint);
        //x
        canvas.drawLine(xtl,(float) (ytl+(no_of_y_positive_unit*y_unit_length)), xtr, (float) (ytr+(no_of_y_positive_unit*y_unit_length)), paint);
    }

    private void drawGrid(Canvas canvas) {

        float max_stroke_width = lineChartAttributes.gridStrokeWidth;
        float gridStrokeWidth_x = max_stroke_width ;
        float gridStrokeWidth_y = max_stroke_width;

        gridStrokeWidth_x = (float) (10 / (no_of_x_negetive_unit+no_of_x_positive_unit));
        gridStrokeWidth_y = (float) (10 / (no_of_y_negetive_unit+no_of_y_positive_unit));

        gridStrokeWidth_x = (gridStrokeWidth_x > max_stroke_width) ? max_stroke_width : gridStrokeWidth_x;
        gridStrokeWidth_y = (gridStrokeWidth_y > max_stroke_width) ? max_stroke_width : gridStrokeWidth_y;

        paint.setStrokeWidth(gridStrokeWidth_x);
        for(int i=0; i<(no_of_x_negetive_unit+no_of_x_positive_unit)+1; i++) {
            canvas.drawLine(xtl+(i*x_unit_length), ytl, xbl+(i*x_unit_length), ybl, paint);
        }

        paint.setStrokeWidth(gridStrokeWidth_y);
        for(int i=0; i<(no_of_y_negetive_unit+no_of_y_positive_unit)+1; i++) {
            canvas.drawLine(xtl, ytl+(i*y_unit_length), xtr,ytr+(i*y_unit_length), paint);
        }
    }
}