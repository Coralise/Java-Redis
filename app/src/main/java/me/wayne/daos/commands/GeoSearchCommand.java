package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.GeoSpace;

public class GeoSearchCommand extends AbstractCommand<List<List<Object>>> {

    public GeoSearchCommand() {
        super("GEOSEARCH", 4);
    }

    @Override
    protected List<List<Object>> processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
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

        return geoSet.geoSearch(fromMember, longitude, latitude, radius, unit, width, height, order, count, any, withCoord,
                withDist, withHash);

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
