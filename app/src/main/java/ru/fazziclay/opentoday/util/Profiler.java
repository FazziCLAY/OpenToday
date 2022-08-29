package ru.fazziclay.opentoday.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Profiler {
    private final String key;
    private final long constructorTime;
    private long endTime;
    private final List<Point> points = new ArrayList<>();

    public Profiler(String key) {
        this.key = key;
        constructorTime = System.currentTimeMillis();
    }

    public void point(String subOperation) {
        points.add(new Point(subOperation));
    }

    public void end() {
        endTime = System.currentTimeMillis();

        StringBuilder stringBuilder = new StringBuilder();
        long latestOperationTime = constructorTime;
        int i = 0;
        while (i < points.size()) {
            stringBuilder.append("\n");
            Point point = points.get(i);
            long latestElapsed = point.time - latestOperationTime;
            latestOperationTime = point.time;

            stringBuilder.append("#").append(i).append(" C: ").append(latestElapsed).append(" L: ").append(point.time - constructorTime).append(" - (").append(point.operation).append(") ");
            i++;
        }

        long latestElapsed = endTime - latestOperationTime;
        stringBuilder.append("\n").append("#").append("E").append(" C: ").append(latestElapsed).append(" L: ").append(endTime - constructorTime).append(" - ").append("[END]");

        Log.d("Profiler", "[" + key + "] Ended! Constructor elapsed: " + (endTime - constructorTime) + stringBuilder);
    }

    private static class Point {
        String operation;
        long time;

        public Point(String subOperation) {
            time = System.currentTimeMillis();
            operation = subOperation;
        }
    }
}
