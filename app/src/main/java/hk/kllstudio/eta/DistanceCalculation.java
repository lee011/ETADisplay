package hk.kllstudio.eta;

import java.util.Locale;

public class DistanceCalculation {

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.abs(R * c * 1000);
    }

    public static String getHumanReadableDistance(double distance) {
        if (distance > 5000) { // [5, +inf) km, 0.5 km interval
            double round = Math.round(distance / 500);
            return String.format(Locale.TRADITIONAL_CHINESE, "%.1f 公里", round * 0.5);
        } else if (distance > 1000) { // (1, 5] km, 0.1 km interval
            double round = Math.round(distance / 100);
            return String.format(Locale.TRADITIONAL_CHINESE, "%.1f 公里", round * 0.1);
        } else if (distance > 500) { // (500, 1000] m, 100 m interval
            double round = Math.round(distance / 100);
            return String.format(Locale.TRADITIONAL_CHINESE, "%.0f 米", round * 100);
        } else if (distance > 300) { // (300, 500] m, 50 m interval
            double round = Math.round(distance / 50);
            return String.format(Locale.TRADITIONAL_CHINESE, "%.0f 米", round * 50);
        } else { // [0, 300] m, 10 m interval
            double round = Math.round(distance / 10);
            return String.format(Locale.TRADITIONAL_CHINESE, "%.0f 米", round * 10);
        }
    }

    public static String getHumanReadableDistance(double lat1, double lon1, double lat2, double lon2) {
        return getHumanReadableDistance(calculateDistance(lat1, lon1, lat2, lon2));
    }
}
