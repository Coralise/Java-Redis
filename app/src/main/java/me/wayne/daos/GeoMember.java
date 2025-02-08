package me.wayne.daos;

import me.wayne.AssertUtil;

public class GeoMember implements Comparable<GeoMember> {

    private final double longitude;
    private final double latitude;
    private final String member;

    public GeoMember(Double longitude, Double latitude, String member) {
        AssertUtil.assertTrue(longitude >= -180 && longitude <= 180, "ERROR: Invalid longitude");
        this.longitude = longitude;
        AssertUtil.assertTrue(latitude >= -85.05112878 && latitude <= 85.05112878, "ERROR: Invalid latitude");
        this.latitude = latitude;
        this.member = member;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getMember() {
        return member;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((member == null) ? 0 : member.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GeoMember other = (GeoMember) obj;
        if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
            return false;
        if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
            return false;
        if (member == null) {
            if (other.member != null)
                return false;
        } else if (!member.equals(other.member))
            return false;
        return true;
    }

    @Override
    public int compareTo(GeoMember o) {
        int latCompare = Double.compare(this.latitude, o.latitude);
        if (latCompare != 0) return latCompare;

        int lonCompare = Double.compare(this.longitude, o.longitude);
        if (lonCompare != 0) return lonCompare;

        return this.member.compareTo(o.member);
    }

}
