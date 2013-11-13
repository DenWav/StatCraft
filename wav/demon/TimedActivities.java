package wav.demon;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class TimedActivities extends Timer {

    private StatCraft plugin;
    private TimerTask totalsUpdate;

    public TimedActivities(StatCraft plugin) {
        this.plugin = plugin;
    }

    public boolean startTotalsUpdateing(int minutes) {
        totalsUpdate = new TimerTask() {
            @Override
            public void run() {
                updateTotals();
            }
        };
        schedule(totalsUpdate, 01, 1000*60*minutes);
        return true;
    }

    public boolean stopTotalsUpdating() {
        return totalsUpdate.cancel();
    }

    public boolean forceTotalUpdate() {
        return updateTotals();
    }

    private boolean updateTotals() {
        // set the tempMap
        Map<Integer, Map<String, Integer>> tempMap =
                new HashMap<Integer, Map<String, Integer>>();

        // set the first iterator
        Iterator baseIt = plugin.statsForPlayers.entrySet().iterator();
        while (baseIt.hasNext()) {
            // grab the first pair, then the name and the second map
            Map.Entry pairs = (Map.Entry) baseIt.next();
            String name = (String) pairs.getKey();
            if (!name.equalsIgnoreCase("total")) {

                Map<Integer, Map<String, Integer>> secondaryMap =
                        (Map<Integer, Map<String, Integer>>) pairs.getValue();
                // set the second iterator off of the second map
                Iterator secondaryIt = secondaryMap.entrySet().iterator();
                while (secondaryIt.hasNext()) {
                    // grab the second pair and the type
                    Map.Entry secondPairs = (Map.Entry) secondaryIt.next();
                    int type = (Integer) secondPairs.getKey();
                    if (!tempMap.containsKey(type))
                        tempMap.put(type, new HashMap<String, Integer>());

                    // figure out what the total value is, but don't try to grab a null value!
                    // if a value doesn't exist, set it as 0
                    final int totalValue;
                    if (plugin.statsForPlayers.get(name).containsKey(type))
                        if (plugin.statsForPlayers.get(name).get(type).containsKey("total"))
                            totalValue = plugin.statsForPlayers.get(name).get(type).get("total");
                        else
                            totalValue = 0;
                    else
                        totalValue = 0;

                    tempMap.get(type).put(name, totalValue);
                }
            }
        }
        // set gson
        Gson gson = new Gson();

        // set iterator
        Iterator it = tempMap.entrySet().iterator();
        while (it.hasNext()) {
            // grab the first pair
            Map.Entry pairs = (Map.Entry) it.next();
            // get the name, this is how we will know what type it is
            int type = (Integer) pairs.getKey();
            // convert the HashMap to json
            String json = gson.toJson(tempMap.get(type));

            // make sure the directory exists for us to write to
            File outputDir = new File(plugin.getDataFolder(), "stats/totals");
            PrintWriter out;

            // make sure the output directory exists
            if (!outputDir.exists())
                outputDir.mkdir();

            // write to the file
            try {
                // set the file
                out = new PrintWriter(outputDir.toString() + "/" + type);
                // write
                out.println(json);
                // close 'er up
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            it.remove();
        }
        return true;
    }

}
