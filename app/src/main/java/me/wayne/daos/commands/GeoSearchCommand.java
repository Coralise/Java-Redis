package me.wayne.daos.commands;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;

import me.wayne.InMemoryStore;
import me.wayne.daos.GeoMember;
import me.wayne.daos.GeoSpace;

public class GeoSearchCommand extends AbstractCommand<List<List<Object>>> {

    public GeoSearchCommand() {
        super("GEOSEARCH", 4);
    }

    @Override
    protected List<List<Object>> processCommand(PrintWriter out, InMemoryStore store, List<String> args) {
        logger.log(Level.INFO, "Processing GEOSEARCH command with arguments: {0}", args);

        Map<String, Object> parsedArgs = parseArgs(args);
        String key = (String) parsedArgs.get("key");
        String fromMember = (String) parsedArgs.get("fromMember");
        String longitude = (String) parsedArgs.get("longitude");
        String latitude = (String) parsedArgs.get("latitude");
        String radius = (String) parsedArgs.get("radius");
        String unit = (String) parsedArgs.get("unit");
        String width = (String) parsedArgs.get("width");
        String height = (String) parsedArgs.get("height");
        String order = (String) parsedArgs.get("order");
        String count = (String) parsedArgs.get("count");
        boolean any = (boolean) parsedArgs.getOrDefault("any", false);
        boolean withCoord = (boolean) parsedArgs.getOrDefault("withCoord", false);
        boolean withDist = (boolean) parsedArgs.getOrDefault("withDist", false);
        boolean withHash = (boolean) parsedArgs.getOrDefault("withHash", false);

        GeoSpace geoSet = store.getStoreValue(key, true).getValue(GeoSpace.class);

        double fromLongitude;
        double fromLatitude;
        if (fromMember != null) { // FROMMEMBER
            logger.log(Level.INFO, "Using FROMMEMBER: {0}", fromMember);
            GeoMember fromGeoMember = geoSet.floor(fromMember);
            if (fromGeoMember == null || !fromGeoMember.getMember().equals(fromMember)) throw new AssertionError("ERR: Specified member not found: " + fromMember);
            fromLongitude = fromGeoMember.getLongitude();
            fromLatitude = fromGeoMember.getLatitude();
        } else { // FROMLONLAT
            logger.log(Level.INFO, "Using FROMLONLAT: longitude={0}, latitude={1}", new Object[]{longitude, latitude});
            fromLongitude = Double.parseDouble(longitude);
            fromLatitude = Double.parseDouble(latitude);
        }
        
        TreeSet<List<Object>> resultSet = new TreeSet<>((o1, o2) -> Double.compare((double) o1.get(0), (double) o2.get(0)));
        if (radius != null) { // BYRADIUS
            logger.log(Level.INFO, "Using BYRADIUS: radius={0}, unit={1}", new Object[]{radius, unit});
            double radiusInMeters = convertToMeters(Double.parseDouble(radius), unit);

            for (GeoMember geoMember : geoSet) {
                double distance = haversine(fromLatitude, fromLongitude, geoMember.getLatitude(), geoMember.getLongitude());
                logger.log(Level.INFO, "Distance and radius: {0} - {1}", new Object[]{distance, radiusInMeters});
                if (distance <= radiusInMeters) {
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

        } else { // BYBOX
            logger.log(Level.INFO, "Using BYBOX: width={0}, height={1}, unit={2}", new Object[]{width, height, unit});
            double widthInMeters = convertToMeters(Double.parseDouble(width), unit);
            double heightInMeters = convertToMeters(Double.parseDouble(height), unit);
        
            // Calculate the bounding box
            double latDiff = heightInMeters / 111320.0; // 1 degree latitude ~ 111.32 km
            double lonDiff = widthInMeters / (111320.0 * Math.cos(Math.toRadians(fromLatitude))); // Adjust for longitude
        
            double minLat = fromLatitude - latDiff / 2;
            double maxLat = fromLatitude + latDiff / 2;
            double minLon = fromLongitude - lonDiff / 2;
            double maxLon = fromLongitude + lonDiff / 2;
        
            for (GeoMember geoMember : geoSet) {
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
        }

        List<List<Object>> list = resultSet.stream().map(e -> {
            e.removeFirst();
            return e;
        }).toList();

        // Apply count limit if specified
        if (count != null) {
            logger.log(Level.INFO, "Applying count limit: count={0}, any={1}", new Object[]{count, any});
            int countLimit = Integer.parseInt(count);
            if (any) {
                list = list.stream().limit(countLimit).toList();
            } else {
                list = list.subList(0, Math.min(countLimit, list.size()));
            }
        }

        logger.log(Level.INFO, "GEOSEARCH command processed successfully");
        return order != null && "DESC".equals(order) ? list.reversed() : list;

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

    // key 
    // <FROMMEMBER member   |   FROMLONLAT longitude latitude>
    // <BYRADIUS radius <M | KM | FT | MI>   |   BYBOX width height <M | KM | FT | MI>>
    // [ASC | DESC] [COUNT count [ANY]] [WITHCOORD] [WITHDIST] [WITHHASH]
    private Map<String, Object> parseArgs(List<String> args) {
        logger.log(Level.INFO, "Parsing arguments: {0}", args);
        Map<String, Object> parsedArgs = new HashMap<>();
        int index = 0;

        // Extract key
        if (index < args.size()) {
            parsedArgs.put("key", args.get(index++));
        }

        // Extract FROMMEMBER or FROMLONLAT
        if (index < args.size()) {
            String fromType = args.get(index++);
            if ("FROMMEMBER".equals(fromType) && index < args.size()) {
                parsedArgs.put("fromMember", args.get(index++));
            } else if ("FROMLONLAT".equals(fromType) && index + 1 < args.size()) {
                parsedArgs.put("longitude", args.get(index++));
                parsedArgs.put("latitude", args.get(index++));
            }
        }

        // Extract BYRADIUS or BYBOX
        if (index < args.size()) {
            String byType = args.get(index++);
            if ("BYRADIUS".equals(byType) && index + 1 < args.size()) {
                parsedArgs.put("radius", args.get(index++));
                parsedArgs.put("unit", args.get(index++).toUpperCase());
            } else if ("BYBOX".equals(byType) && index + 2 < args.size()) {
                parsedArgs.put("width", args.get(index++));
                parsedArgs.put("height", args.get(index++));
                parsedArgs.put("unit", args.get(index++).toUpperCase());
            }
        }

        // Extract optional arguments
        while (index < args.size()) {
            String option = args.get(index++);
            switch (option) {
                case "ASC", "DESC":
                    parsedArgs.put("order", option);
                    break;
                case "COUNT":
                    if (index < args.size()) {
                        parsedArgs.put("count", args.get(index++));
                        if (index < args.size() && "ANY".equals(args.get(index))) {
                            parsedArgs.put("any", true);
                            index++;
                        }
                    }
                    break;
                case "WITHCOORD":
                    parsedArgs.put("withCoord", true);
                    break;
                case "WITHDIST":
                    parsedArgs.put("withDist", true);
                    break;
                case "WITHHASH":
                    parsedArgs.put("withHash", true);
                    break;
                default:
                    // Handle unknown options or arguments
                    break;
            }
        }

        return parsedArgs;
    }
    
}
