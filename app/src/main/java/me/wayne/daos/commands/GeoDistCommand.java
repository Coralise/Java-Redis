package me.wayne.daos.commands;

import java.util.List;
import java.util.TreeSet;

import me.wayne.InMemoryStore;
import me.wayne.daos.GeoMember;

public class GeoDistCommand extends AbstractCommand<String> {

    public GeoDistCommand() {
        super("GEODIST", 3, 4);
    }

    @Override
    protected String processCommand(Thread thread, InMemoryStore store, List<String> args) {
        String key = args.get(0);
        String member1 = args.get(1);
        String member2 = args.get(2);
        String unit = args.get(3) != null ? args.get(3) : "m";

        TreeSet<GeoMember> geoSet = getGeoSet(store, key);

        GeoMember geoMember1 = null;
        GeoMember geoMember2 = null;
        for (GeoMember geoMember : geoSet) {
            if (geoMember.getMember().equals(member1)) geoMember1 = geoMember;
            if (geoMember.getMember().equals(member2)) geoMember2 = geoMember;
            if (geoMember1 != null && geoMember2 != null) break;
        }

        if (geoMember1 == null) throw new AssertionError("ERROR: Member " + member1 + " not found");
        if (geoMember2 == null) throw new AssertionError("ERROR: Member " + member2 + " not found");

        return String.valueOf(convertFromMeters(haversine(geoMember1.getLatitude(), geoMember1.getLongitude(),
        geoMember2.getLatitude(), geoMember2.getLongitude()), unit));

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
    
}
