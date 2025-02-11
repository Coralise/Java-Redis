package me.wayne.daos;

import java.util.TreeSet;

public class GeoSpace extends TreeSet<GeoMember> {

    public GeoMember floor(String member) {
        GeoMember geoMember = new GeoMember(member);
        return floor(geoMember);
    }

}