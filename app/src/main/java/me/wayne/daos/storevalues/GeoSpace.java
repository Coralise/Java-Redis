package me.wayne.daos.storevalues;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeoSpace extends TreeSet<GeoMember> {

    private static final Logger LOGGER = Logger.getLogger(GeoSpace.class.getName());

    public GeoMember floor(String member) {
        GeoMember geoMember = new GeoMember(member);
        return floor(geoMember);
    }

    public PrintableList<List<Object>> geoSearch(String fromMember, String longitude, String latitude, String radius, String unit,
            String width, String height, String order, String count, boolean any, boolean withCoord, boolean withDist,
            boolean withHash) throws AssertionError {
        double fromLongitude;
        double fromLatitude;
        if (fromMember != null) { // FROMMEMBER
            LOGGER.log(Level.INFO, "Using FROMMEMBER: {0}", fromMember);
            GeoMember fromGeoMember = floor(fromMember);
            if (fromGeoMember == null || !fromGeoMember.getMember().equals(fromMember)) throw new AssertionError("ERR: Specified member not found: " + fromMember);
            fromLongitude = fromGeoMember.getLongitude();
            fromLatitude = fromGeoMember.getLatitude();
        } else { // FROMLONLAT
            LOGGER.log(Level.INFO, "Using FROMLONLAT: longitude={0}, latitude={1}", new Object[]{longitude, latitude});
            fromLongitude = Double.parseDouble(longitude);
            fromLatitude = Double.parseDouble(latitude);
        }

        TreeSet<List<Object>> resultSet;
        if (radius != null) { // BYRADIUS
            resultSet = geoSearchByRadius(fromLongitude, fromLatitude, radius, unit, withCoord, withDist, withHash);
        } else { // BYBOX
            resultSet = geoSearchByBox(fromLongitude, fromLatitude, width, height, unit, withCoord, withDist, withHash);
        }

        List<List<Object>> list = resultSet.stream().map(e -> {
            e.removeFirst();
            return e;
        }).toList();

        // Apply count limit if specified
        if (count != null) {
            LOGGER.log(Level.INFO, "Applying count limit: count={0}, any={1}", new Object[]{count, any});
            int countLimit = Integer.parseInt(count);
            if (any) {
                list = list.stream().limit(countLimit).toList();
            } else {
                list = list.subList(0, Math.min(countLimit, list.size()));
            }
        }

        LOGGER.log(Level.INFO, "GEOSEARCH command processed successfully");
        return order != null && "DESC".equals(order) ? new PrintableList<>(list.reversed()) : new PrintableList<>(list);
    }

    private TreeSet<List<Object>> geoSearchByRadius(double fromLongitude, double fromLatitude, String radius, String unit,
            boolean withCoord, boolean withDist, boolean withHash) {
        TreeSet<List<Object>> resultSet = new TreeSet<>((o1, o2) -> Double.compare((double) o1.get(0), (double) o2.get(0)));
        LOGGER.log(Level.INFO, "Using BYRADIUS: radius={0}, unit={1}", new Object[]{radius, unit});
        double radiusInMeters = convertToMeters(Double.parseDouble(radius), unit);

        for (GeoMember geoMember : this) {
            double distance = haversine(fromLatitude, fromLongitude, geoMember.getLatitude(), geoMember.getLongitude());
            LOGGER.log(Level.INFO, "Distance and radius: {0} - {1}", new Object[]{distance, radiusInMeters});
            if (distance <= radiusInMeters) {
                PrintableList<Object> result = new PrintableList<>();
                result.add(distance);
                result.add(geoMember.getMember());
                if (withDist) {
                    result.add(convertFromMeters(distance, unit));
                }
                if (withHash) {
                    result.add(getGeoHash(geoMember.getLongitude(), geoMember.getLatitude()));
                }
                if (withCoord) {
                    result.add(new PrintableList<>(List.of(geoMember.getLongitude(), geoMember.getLatitude())));
                }
                resultSet.add(result);
            }
        }
        return resultSet;
    }

    private TreeSet<List<Object>> geoSearchByBox(double fromLongitude, double fromLatitude, String width, String height, String unit,
            boolean withCoord, boolean withDist, boolean withHash) {
        TreeSet<List<Object>> resultSet = new TreeSet<>((o1, o2) -> Double.compare((double) o1.get(0), (double) o2.get(0)));
        LOGGER.log(Level.INFO, "Using BYBOX: width={0}, height={1}, unit={2}", new Object[]{width, height, unit});
        double widthInMeters = convertToMeters(Double.parseDouble(width), unit);
        double heightInMeters = convertToMeters(Double.parseDouble(height), unit);

        // Calculate the bounding box
        double latDiff = heightInMeters / 111320.0; // 1 degree latitude ~ 111.32 km
        double lonDiff = widthInMeters / (111320.0 * Math.cos(Math.toRadians(fromLatitude))); // Adjust for longitude

        double minLat = fromLatitude - latDiff / 2;
        double maxLat = fromLatitude + latDiff / 2;
        double minLon = fromLongitude - lonDiff / 2;
        double maxLon = fromLongitude + lonDiff / 2;

        for (GeoMember geoMember : this) {
            double memberLat = geoMember.getLatitude();
            double memberLon = geoMember.getLongitude();
            if (memberLat >= minLat && memberLat <= maxLat && memberLon >= minLon && memberLon <= maxLon) {
                double distance = haversine(fromLatitude, fromLongitude, memberLat, memberLon);
                ArrayList<Object> result = new ArrayList<>();
                result.add(distance);
                result.add(geoMember.getMember());
                if (withDist) {
                    result.add(convertFromMeters(distance, unit));
                }
                if (withHash) {
                    result.add(getGeoHash(geoMember.getLongitude(), geoMember.getLatitude()));
                }
                if (withCoord) {
                    result.add(List.of(geoMember.getLongitude(), geoMember.getLatitude()));
                }
                resultSet.add(result);
            }
        }
        return resultSet;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radius of the Earth in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private double convertToMeters(double value, String unit) {
        switch (unit.toUpperCase()) {
            case "KM":
                return value * 1000;
            case "MI":
                return value * 1609.34;
            case "FT":
                return value * 0.3048;
            case "M":
            default:
                return value;
        }
    }

    private double convertFromMeters(double value, String unit) {
        switch (unit.toUpperCase()) {
            case "KM":
                return value / 1000;
            case "MI":
                return value / 1609.34;
            case "FT":
                return value / 0.3048;
            case "M":
            default:
                return value;
        }
    }
    
    private static final int GEOHASH_PRECISION = 26;
    public long getGeoHash(double longitude, double latitude) {
        long latBits = encodeBits(latitude, -90, 90);
        long lonBits = encodeBits(longitude, -180, 180);
        return interleaveBits(lonBits, latBits);
    }

    private long encodeBits(double value, double min, double max) {
        long bits = 0L;
        for (int i = 0; i < GEOHASH_PRECISION; i++) {
            double mid = (min + max) / 2;
            if (value >= mid) {
                bits |= (1L << (GEOHASH_PRECISION - 1 - i));
                min = mid;
            } else {
                max = mid;
            }
        }
        return bits;
    }

    private long interleaveBits(long lonBits, long latBits) {
        long geoHash = 0L;
        for (int i = 0; i < GEOHASH_PRECISION; i++) {
            geoHash |= ((lonBits >> (GEOHASH_PRECISION - 1 - i)) & 1L) << (2 * (GEOHASH_PRECISION - 1 - i) + 1);
            geoHash |= ((latBits >> (GEOHASH_PRECISION - 1 - i)) & 1L) << (2 * (GEOHASH_PRECISION - 1 - i));
        }
        return geoHash;
    }

}