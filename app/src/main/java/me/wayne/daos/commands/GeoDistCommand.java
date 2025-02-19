package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.List;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.GeoMember;
import me.wayne.daos.storevalues.GeoSpace;

public class GeoDistCommand extends AbstractCommand<String> {

    public GeoDistCommand() {
        super("GEODIST", 3, 4);
    }

    @Override
    protected String processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        String key = args.get(0);
        String member1 = args.get(1);
        String member2 = args.get(2);
        String unit = args.size() > 3 ? args.get(3) : "m";

        GeoSpace geoSet = store.getStoreValue(key, true).getValue(GeoSpace.class);

        GeoMember geoMember1 = geoSet.floor(member1);
        GeoMember geoMember2 = geoSet.floor(member2);

        if (geoMember1 == null || !geoMember1.getMember().equals(member1)) throw new AssertionError("ERROR: Member " + member1 + " not found");
        if (geoMember2 == null || !geoMember2.getMember().equals(member2)) throw new AssertionError("ERROR: Member " + member2 + " not found");

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
