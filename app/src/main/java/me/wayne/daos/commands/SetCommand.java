package me.wayne.daos.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.wayne.AssertUtil;
import me.wayne.InMemoryStore;
import me.wayne.daos.io.StorePrintWriter;

public class SetCommand extends AbstractCommand<String> {

    public SetCommand() {
        super("SET", 2, 6);
    }

    @Override
    protected String processCommand(StorePrintWriter out, InMemoryStore store, List<String> args) {

        Map<String, Object> parsedArgs = parseArgs(args);
        String key = (String) parsedArgs.get("key");
        String value = (String) parsedArgs.get("value");
        boolean nx = parsedArgs.get("NX") != null;
        boolean xx = parsedArgs.get("XX") != null;
        boolean get = parsedArgs.get("GET") != null;
        boolean keepttl = parsedArgs.get("KEEPTTL") != null;
        Long ex = (Long) parsedArgs.get("EX");
        Long px = (Long) parsedArgs.get("PX");
        Long exat = (Long) parsedArgs.get("EXAT");
        Long pxat = (Long) parsedArgs.get("PXAT");

        AssertUtil.assertTrue(!(nx && xx), "ERROR: NX and XX options are mutually exclusive");
        int ttlOptionsCount = 0;
        if (keepttl) ttlOptionsCount++;
        if (ex != null) ttlOptionsCount++;
        if (px != null) ttlOptionsCount++;
        if (exat != null) ttlOptionsCount++;
        if (pxat != null) ttlOptionsCount++;
        AssertUtil.assertTrue(ttlOptionsCount <= 1, "ERROR: Only one of KEEPTTL, EX, PX, EXAT, or PXAT can be specified");

        Object oldValue = store.getStoreValue(key, Object.class);
        if (oldValue != null && nx) return null;
        if (oldValue == null && xx) return null;

        store.setStoreValue(key, value);
        return get ? (String) oldValue : OK_RESPONSE;
    }

    private Map<String, Object> parseArgs(List<String> args) {
        Map<String, Object> parsedArgs = new HashMap<>();
        parsedArgs.put("key", args.get(0));
        parsedArgs.put("value", args.get(1));

        for (int i = 2; i < args.size(); i++) {
            String option = args.get(i).toUpperCase();
            switch (option) {
                case "NX", "XX", "GET", "KEEPTTL":
                    parsedArgs.put(option, true);
                    break;
                case "EX":
                    if (i + 1 < args.size()) {
                        parsedArgs.put("EX", Long.parseLong(args.get(++i)));
                    }
                    break;
                case "PX":
                    if (i + 1 < args.size()) {
                        parsedArgs.put("PX", Long.parseLong(args.get(++i)));
                    }
                    break;
                case "EXAT":
                    if (i + 1 < args.size()) {
                        parsedArgs.put("EXAT", Long.parseLong(args.get(++i)));
                    }
                    break;
                case "PXAT":
                    if (i + 1 < args.size()) {
                        parsedArgs.put("PXAT", Long.parseLong(args.get(++i)));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option: " + option);
            }
        }

        return parsedArgs;
    }
    
}
