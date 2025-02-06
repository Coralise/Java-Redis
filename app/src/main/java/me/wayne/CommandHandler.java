package me.wayne;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class CommandHandler implements Runnable {
    private static final String INVALID_ARGS_RESPONSE = "ERROR: Invalid number of arguments";
    private static final String OK_RESPONSE = "OK";
    private Socket clientSocket;
    private InMemoryStore dataStore;

    public InMemoryStore getDataStore() {
        return dataStore;
    }

    public CommandHandler(Socket clientSocket, InMemoryStore dataStore) {
        this.clientSocket = clientSocket;
        this.dataStore = dataStore;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                List<String> tokens = getArgs(inputLine);
                String command = tokens.get(0).toUpperCase();

                try {
                    switch (command) {
                        case "SET":
                            if (tokens.size() == 3) {
                                dataStore.set(tokens.get(1), tokens.get(2));
                                out.println(OK_RESPONSE);
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "GET":
                            if (tokens.size() == 2) {
                                Object value = dataStore.get(tokens.get(1));
                                out.println(value != null ? value : "NULL");
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "DELETE":
                            if (tokens.size() == 2) {
                                out.println(dataStore.delete(tokens.get(1)));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "APPEND":
                            if (tokens.size() == 3) {
                                dataStore.append(tokens.get(1), tokens.get(2));
                                out.println(OK_RESPONSE);
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "STRLEN":
                            if (tokens.size() == 2) {
                                int length = dataStore.strLen(tokens.get(1));
                                out.println(length);
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "GETRANGE":
                            if (tokens.size() == 4) {
                                String value = dataStore.getRange(tokens.get(1), Integer.parseInt(tokens.get(2)), Integer.parseInt(tokens.get(3)));
                                out.println(value);
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "SETRANGE":
                            if (tokens.size() == 4) {
                                String value = dataStore.setRange(tokens.get(1), Integer.parseInt(tokens.get(2)), tokens.get(3));
                                out.println(value);
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "INCR":
                            if (tokens.size() == 2) {
                                dataStore.incr(tokens.get(1));
                                out.println(OK_RESPONSE);
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "DECR":
                            if (tokens.size() == 2) {
                                dataStore.decr(tokens.get(1));
                                out.println(OK_RESPONSE);
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "INCRBY":
                            if (tokens.size() == 3) {
                                dataStore.incrBy(tokens.get(1), Integer.parseInt(tokens.get(2)));
                                out.println(OK_RESPONSE);
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "DECRBY":
                            if (tokens.size() == 3) {
                                dataStore.decrBy(tokens.get(1), Integer.parseInt(tokens.get(2)));
                                out.println(OK_RESPONSE);
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "LPUSH":
                            if (tokens.size() > 1) {
                                out.println(dataStore.lPush(tokens.get(1), tokens.subList(2, tokens.size())));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "RPUSH":
                            if (tokens.size() > 1) {
                                out.println(dataStore.rPush(tokens.get(1), tokens.subList(2, tokens.size())));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "LPOP":
                            if (tokens.size() > 1) {
                                out.println(dataStore.lPop(tokens.get(1), tokens.size() == 3 ? Integer.parseInt(tokens.get(2)) : 1));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "RPOP":
                            if (tokens.size() > 1) {
                                out.println(dataStore.rPop(tokens.get(1), tokens.size() == 3 ? Integer.parseInt(tokens.get(2)) : 1));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "LRANGE":
                            if (tokens.size() == 4) {
                                out.println(dataStore.lRange(tokens.get(1), Integer.parseInt(tokens.get(2)), Integer.parseInt(tokens.get(3))));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "LINDEX":
                            if (tokens.size() == 3) {
                                out.println(dataStore.lIndex(tokens.get(1), Integer.parseInt(tokens.get(2))));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "LSET":
                            if (tokens.size() == 4) {
                                if (dataStore.lSet(tokens.get(1), Integer.parseInt(tokens.get(2)), tokens.get(3))) out.println(OK_RESPONSE);
                                else out.println(INVALID_ARGS_RESPONSE);
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "JSON.SET":
                            String jsonValue = getJsonValue(inputLine);
                            JSONObject jsonObject = new JSONObject("{ \"" + tokens.get(2) + "\": " + jsonValue + " }");
                            dataStore.setJson(tokens.get(1), jsonObject);
                            out.println(OK_RESPONSE);
                            break;
                        case "JSON.GET":
                            if (tokens.size() == 3) {
                                JSONObject json = dataStore.getJson(tokens.get(1));
                                Object value = json.get(tokens.get(2));
                                out.println(value != null ? value : "NULL");
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "SADD":
                            if (tokens.size() > 2) {
                                out.println(dataStore.sAdd(tokens.get(1), tokens.subList(2, tokens.size())));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "SREM":
                            if (tokens.size() > 2) {
                                out.println(dataStore.sRem(tokens.get(1), tokens.subList(2, tokens.size())));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "SISMEMBER":
                            if (tokens.size() == 3) {
                                out.println(dataStore.sIsMember(tokens.get(1), tokens.get(2)));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "SMEMBERS":
                            if (tokens.size() == 2) {
                                out.println(dataStore.sMembers(tokens.get(1)));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "SINTER":
                            if (tokens.size() > 1) {
                                out.println(dataStore.sInter(tokens.subList(1, tokens.size())));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "SUNION":
                            if (tokens.size() > 1) {
                                out.println(dataStore.sUnion(tokens.subList(1, tokens.size())));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        case "SDIFF":
                            if (tokens.size() > 1) {
                                out.println(dataStore.sDiff(tokens.subList(1, tokens.size())));
                            } else {
                                out.println(INVALID_ARGS_RESPONSE);
                            }
                            break;
                        default:
                            out.println("ERROR: Unkown Command");
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getArgs(String input) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                result.add(matcher.group(1));
            } else {
                result.add(matcher.group(2));
            }
        }

        return result;
    }

    private String getJsonValue(String inputLine) {
        Pattern pattern = Pattern.compile("'(.*?)'");
        Matcher m = pattern.matcher(inputLine);
        if (m.find()) {
            return m.group(1);
        }
        return m.find() ? m.group(1) : "\"\"";
    }
}