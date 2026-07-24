package com.gsmtracker.position;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class TripSegmenter {

    /** Полный конвейер: точность -> разрыв по времени -> стоянки. */
    public List<List<Position>> segment(List<Position> points, TripSegmentationParams params) {
        List<Position> clean = filterByAccuracy(points, params.maxAccuracyMeters());
        List<List<Position>> byGap = splitByTimeGap(clean, params.maxGap());

        List<List<Position>> result = new ArrayList<>();
        for (List<Position> coarse : byGap) {
            result.addAll(splitByStops(coarse, params.speedThresholdMps(), params.minStopDuration()));
        }
        return result;
    }

    /** Отбрасывает точки-выбросы с плохой точностью (accuracy > порога). */
    public List<Position> filterByAccuracy(List<Position> points, double maxAccuracyMeters) {
        List<Position> result = new ArrayList<>();
        for (Position p : points) {
            if (p.getAccuracy() == null || p.getAccuracy() <= maxAccuracyMeters) {
                result.add(p);
            }
        }
        return result;
    }

    /** Шаг 1: граница поездки — пауза между точками больше maxGap. */
    public List<List<Position>> splitByTimeGap(List<Position> points, Duration maxGap) {
        List<List<Position>> segments = new ArrayList<>();
        if (points.isEmpty()) {
            return segments;
        }
        List<Position> current = new ArrayList<>();
        current.add(points.getFirst());
        for (int i = 1; i < points.size(); i++) {
            Duration gap = Duration.between(points.get(i - 1).getRecordedAt(), points.get(i).getRecordedAt());
            if (gap.compareTo(maxGap) > 0) {
                segments.add(current);
                current = new ArrayList<>();
            }
            current.add(points.get(i));
        }
        segments.add(current);
        return segments;
    }

    /**
     * Шаг 2: внутри одного (непрерывного по времени) сегмента делит по стоянкам.
     * Стоянка = подряд идущие "стоячие" точки, длящиеся не меньше minStopDuration.
     * Реальная стоянка обрывает поездку и сама отбрасывается (это парковка).
     * Короткая пауза (например, светофор) остаётся внутри поездки.
     */
    public List<List<Position>> splitByStops(List<Position> points,
                                             double speedThresholdMps,
                                             Duration minStopDuration) {
        List<List<Position>> result = new ArrayList<>();
        List<Position> current = new ArrayList<>();
        List<Position> stopRun = new ArrayList<>();
        Position prev = null;

        for (Position p : points) {
            boolean stationary = effectiveSpeedMps(prev, p) < speedThresholdMps;
            if (stationary) {
                stopRun.add(p);
            } else {
                if (!stopRun.isEmpty()) {
                    if (isRealStop(stopRun, minStopDuration)) {
                        if (!current.isEmpty()) {
                            result.add(current);
                            current = new ArrayList<>();
                        }
                        // реальная стоянка (парковка) отбрасывается
                    } else {
                        current.addAll(stopRun); // короткая пауза остаётся в поездке
                    }
                    stopRun.clear();
                }
                current.add(p);
            }
            prev = p;
        }

        // хвостовой набор стоячих точек: короткую паузу оставляем, парковку отбрасываем
        if (!stopRun.isEmpty() && !isRealStop(stopRun, minStopDuration)) {
            current.addAll(stopRun);
        }
        if (!current.isEmpty()) {
            result.add(current);
        }
        return result;
    }

    private boolean isRealStop(List<Position> stopRun, Duration minStopDuration) {
        Duration d = Duration.between(
                stopRun.getFirst().getRecordedAt(),
                stopRun.getLast().getRecordedAt());
        return d.compareTo(minStopDuration) >= 0;
    }

    /** Скорость: берём из данных, а если её нет — считаем из расстояния/времени. */
    private double effectiveSpeedMps(Position prev, Position curr) {
        if (curr.getSpeed() != null) {
            return curr.getSpeed();
        }
        if (prev == null) {
            return 0.0;
        }
        double dist = GeoDistance.haversineMeters(prev, curr);
        double dt = Duration.between(prev.getRecordedAt(), curr.getRecordedAt()).getSeconds();
        return dt > 0 ? dist / dt : 0.0;
    }
}