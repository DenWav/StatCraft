package wav.demon;

import com.google.gson.Gson;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public final class DeathsListener implements Listener{

    private IRCBot plugin;

    public DeathsListener(IRCBot plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        final String deathMessage = event.getDeathMessage();
        final String name = event.getEntity().getName();
        addDeathType(name, deathMessage);
    }

    private void addDeathType(String name, String deathMessage) {

        Map<String, Map<String, Map<String, Integer>>> statsForPlayers = plugin.getMap();
        if (statsForPlayers.get(name) == null) {
            statsForPlayers.put(name, new HashMap<String, Map<String, Integer>>());
        }
        if (statsForPlayers.get(name).get("death") == null) {
            statsForPlayers.get(name).put("death", new HashMap<String, Integer>());
        }
        if (statsForPlayers.get(name).get("death").get(deathMessage) == null) {
            statsForPlayers.get(name).get("death").put(deathMessage, 1);
        } else {
            statsForPlayers.get(name).get("death").put(deathMessage, statsForPlayers.get(name).get("death").get(deathMessage) + 1);
        }
        try {
            Gson gson = new Gson();
            String json = gson.toJson(statsForPlayers);
            PrintWriter out = new PrintWriter("/opt/msm/servers/ocminecraft/stats.txt");
            out.println(json);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        try {
//            File deathLog = new File("/opt/msm/servers/ocminecraft/deaths/" + name);
//            boolean newFile = false;
//            if (!deathLog.exists()) {
//                deathLog.createNewFile();
//                newFile = true;
//            }
//
//            FileInputStream deathLogStream = new FileInputStream(deathLog);
//            BufferedReader br = new BufferedReader(new InputStreamReader(deathLogStream));
//            String strLine;
//            StringBuilder fileContent = new StringBuilder();
//            if (newFile) {
//                while ((strLine = br.readLine()) != null) {
//                    String tokens[] = strLine.split("~");
//                    if (tokens.length == 1) {
//                        fileContent.append(Integer.getInteger(tokens[0]) + 1);
//                        fileContent.append("\n");
//                    } else {
//                        if (tokens[0].equals(deathMessage)) {
//                            tokens[1] = "" + (Integer.getInteger(tokens[1]) + 1);
//                            String newLine = tokens[0] + "~" + tokens[1];
//                            fileContent.append(newLine);
//                            fileContent.append("\n");
//                        } else {
//                            String newLine = tokens[0] + "~" + tokens[1];
//                            fileContent.append(newLine);
//                            fileContent.append("\n");
//                        }
//                    }
//                }
//            } else {
//                fileContent.append(1);
//                fileContent.append("\n");
//                fileContent.append(deathMessage + "~" + 1);
//                fileContent.append("\n");
//            }
//            deathLogStream.close();
//            FileWriter fstreamWrite = new FileWriter("/opt/msm/servers/ocminecraft/deaths/" + name);
//            BufferedWriter out = new BufferedWriter(fstreamWrite);
//            out.write(fileContent.toString());
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
