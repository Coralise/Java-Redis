package me.wayne.daos.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wayne.daos.storevalues.bitfields.BitField;
import me.wayne.daos.storevalues.bitfields.OverflowMode;
import me.wayne.daos.io.StorePrintWriter;
import me.wayne.daos.storevalues.StoreValue;

public class BitFieldCommand extends AbstractCommand<List<Integer>> {

    public BitFieldCommand() {
            super("BITFIELD", 4);
        }
    
        @Override
    protected List<Integer> processCommand(StorePrintWriter out, @Nullable UUID requestUuid, String inputLine, List<String> args) {
        
        String key = args.get(0);
        List<String> subcommands = extractSubcommands(args.subList(1, args.size()));

        StoreValue storeValue = store.getStoreValue(key);
        BitField bitField;
        if (storeValue != null) {
            bitField = storeValue.getValue(BitField.class);
        } else {
            bitField = new BitField();
            store.setStoreValue(key, bitField, inputLine);
        }

        List<Integer> results = new ArrayList<>();

        OverflowMode overflow = OverflowMode.WRAP;
        for (String subcommand : subcommands) {
            String[] subcommandArgs = subcommand.split(" ");
            switch (subcommandArgs[0].toUpperCase()) {
                case "GET":
                    results.add(bitField.getInt(subcommandArgs[2], subcommandArgs[1]));
                    break;
                case "OVERFLOW":
                    overflow = OverflowMode.valueOf(subcommandArgs[1].toUpperCase());
                    break;
                case "SET":
                    results.add(bitField.set(
                        Long.parseLong(subcommandArgs[3]),
                        subcommandArgs[2],
                        subcommandArgs[1],
                        overflow != null ? overflow : OverflowMode.WRAP));
                    overflow = OverflowMode.WRAP;
                    break;
                case "INCRBY":
                    results.add(bitField.incrBy(
                        Long.parseLong(subcommandArgs[3]),
                        subcommandArgs[2],
                        subcommandArgs[1],
                        overflow != null ? overflow : OverflowMode.WRAP));
                    overflow = OverflowMode.WRAP;
                    break;
                default:
                    break;
            }
        }

        return results;

    }

    private List<String> extractSubcommands(List<String> args) {
        List<String> subcommands = new ArrayList<>();
        StringBuilder currentSubcommand = new StringBuilder();
        Pattern pattern = Pattern.compile("GET|OVERFLOW|SET|INCRBY");

        for (String arg : args) {
            Matcher matcher = pattern.matcher(arg.toUpperCase());
            if (matcher.matches() && currentSubcommand.length() > 0) {
                subcommands.add(currentSubcommand.toString().trim());
                currentSubcommand.setLength(0);
            }
            currentSubcommand.append(arg).append(" ");
        }

        if (currentSubcommand.length() > 0) {
            subcommands.add(currentSubcommand.toString().trim());
        }

        return subcommands;
    }
    
}
