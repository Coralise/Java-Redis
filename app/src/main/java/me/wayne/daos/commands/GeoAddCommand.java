package me.wayne.daos.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import me.wayne.AssertUtil;
import me.wayne.daos.GeoMember;
import me.wayne.daos.GeoSpace;
import me.wayne.daos.io.StorePrintWriter;

public class GeoAddCommand extends AbstractCommand<Integer> {

    public GeoAddCommand() {
        super("GEOADD", 4);
    }

    @Override
    protected Integer processCommand(StorePrintWriter out, List<String> args) {

        GeoAddOptions options = parseArgs(args);
        String key = options.getKey();
        List<GeoMember> members = options.getMembers();
        boolean nx = options.isNx();
        boolean xx = options.isXx();
        boolean ch = options.isCh();
        
        AssertUtil.assertTrue(!(nx && xx), "ERROR: NX and XX options are mutually exclusive");

        GeoSpace geoSet = store.getStoreValue(key, GeoSpace.class, new GeoSpace());
        logger.log(Level.INFO, "GeoSet Size: {0}", geoSet.size());

        int updated = 0;
        for (GeoMember member : members) {
            logger.log(Level.INFO, "Adding member: {0}", member.getMember());
            boolean contains = geoSet.contains(member);
            logger.log(Level.INFO, "Contains member: {0}", contains);
            if ((contains && nx) || (!contains && xx)) {
                logger.log(Level.INFO, "Skipped member: {0}", member.getMember());
                continue;
            }
            if (contains) {
                logger.log(Level.INFO, "Removed member: {0}", member.getMember());
                geoSet.remove(member);
            }
            geoSet.add(member);
            logger.log(Level.INFO, "Added member: {0}", member.getMember());
            if (ch || !contains) {
                updated++;
                logger.log(Level.INFO, "Updated count: {0}", updated);
            }
        }

        store.setStoreValue(key, geoSet);
        
        return updated;
    }

    private GeoAddOptions parseArgs(List<String> args) {
        String key = args.get(0);
        boolean nx = false;
        boolean xx = false;
        boolean ch = false;
        int index = 1;

        while (index < args.size() && (args.get(index).equals("NX") || args.get(index).equals("XX") || args.get(index).equals("CH"))) {
            switch (args.get(index)) {
                case "NX":
                    nx = true;
                    break;
                case "XX":
                    xx = true;
                    break;
                case "CH":
                    ch = true;
                    break;
                default:
                    break;
            }
            index++;
        }

        List<GeoMember> members = new ArrayList<>();
        while (index < args.size()) {
            double longitude = Double.parseDouble(args.get(index++));
            double latitude = Double.parseDouble(args.get(index++));
            String member = args.get(index++);
            members.add(new GeoMember(longitude, latitude, member));
        }

        return new GeoAddOptions(key, nx, xx, ch, members);
    }

    private static class GeoAddOptions {
        private final String key;
        private final boolean nx;
        private final boolean xx;
        private final boolean ch;
        private final List<GeoMember> members;

        public GeoAddOptions(String key, boolean nx, boolean xx, boolean ch, List<GeoMember> members) {
            this.key = key;
            this.nx = nx;
            this.xx = xx;
            this.ch = ch;
            this.members = members;
        }

        public String getKey() {
            return key;
        }

        public boolean isNx() {
            return nx;
        }

        public boolean isXx() {
            return xx;
        }

        public boolean isCh() {
            return ch;
        }

        public List<GeoMember> getMembers() {
            return members;
        }
    }
    
}
