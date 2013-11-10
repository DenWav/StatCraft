package wav.demon;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.*;

public final class DeathsListener implements Listener{

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        final String deathMessage = event.getDeathMessage();
        final String name = event.getEntity().getName();
        addDeathType(name, deathMessage);
    }

    private static void addDeathType(String name, String deathMessage) {
        try {
            File deathLog = new File("/opt/msm/servers/ocminecraft/deaths/" + name);
            boolean newFile = false;
            if (!deathLog.exists()) {
                deathLog.createNewFile();
                newFile = true;
            }

            FileInputStream deathLogStream = new FileInputStream(deathLog);
            BufferedReader br = new BufferedReader(new InputStreamReader(deathLogStream));
            String strLine;
            StringBuilder fileContent = new StringBuilder();
            if (newFile) {
                while ((strLine = br.readLine()) != null) {
                    String tokens[] = strLine.split("~");
                    if (tokens.length == 1) {
                        fileContent.append(Integer.getInteger(tokens[0]) + 1);
                        fileContent.append("\n");
                    } else {
                        if (tokens[0].equals(deathMessage)) {
                            tokens[1] = "" + (Integer.getInteger(tokens[1]) + 1);
                            String newLine = tokens[0] + "~" + tokens[1];
                            fileContent.append(newLine);
                            fileContent.append("\n");
                        } else {
                            String newLine = tokens[0] + "~" + tokens[1];
                            fileContent.append(newLine);
                            fileContent.append("\n");
                        }
                    }
                }
            } else {
                fileContent.append(1);
                fileContent.append("\n");
                fileContent.append(deathMessage + "~" + 1);
                fileContent.append("\n");
            }
            deathLogStream.close();
            FileWriter fstreamWrite = new FileWriter("/opt/msm/servers/ocminecraft/deaths/" + name);
            BufferedWriter out = new BufferedWriter(fstreamWrite);
            out.write(fileContent.toString());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
