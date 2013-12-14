package wav.demon;

import com.avaje.ebeaninternal.server.core.ServletContextListener;
import com.google.gson.Gson;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TimedActivities extends Timer {

    private StatCraft plugin;
    private TimerTask totalsUpdate;
    private TimerTask toDisk;
    private TimerTask backups;
    private DecimalFormat df = new DecimalFormat("000");

    public TimedActivities(StatCraft plugin) {
        this.plugin = plugin;
    }

    public boolean startTotalsUpdating(int milliSecs) {
        totalsUpdate = new TimerTask() {
            @Override
            public void run() {
                updateTotals();
            }
        };
        schedule(totalsUpdate, 1, milliSecs);
        return true;
    }

    public boolean stopTotalsUpdating() {
        return totalsUpdate.cancel();
    }

    public boolean forceTotalUpdate() {
        return updateTotals();
    }

    public boolean totalUpdateNull() {
        return totalsUpdate == null;
    }

    public boolean startStatsToDisk(int milliSec) {
        toDisk = new TimerTask() {
            @Override
            public void run() {
                plugin.saveStatFiles();
            }
        };
        schedule(toDisk, 1, milliSec);
        return true;
    }

    public boolean stopStatsToDisk() {
        return toDisk.cancel();
    }

    public boolean forceStatsToDisk() {
        return plugin.saveStatFiles();
    }

    public boolean statsToDiskNull() {
        return toDisk == null;
    }

    public boolean startBackup(int milliSec) {
        backups = new TimerTask() {
            @Override
            public void run() {
                zipBackup(getBackupName());
            }
        };
        schedule(backups, 1, milliSec);
        return true;
    }

    public boolean stopBackup() {
        return backups.cancel();
    }

    public boolean forceBackup() {
        return zipBackup(getBackupName());
    }

    public boolean backupNull() {
        return backups == null;
    }

    @SuppressWarnings("unchecked")
    private boolean updateTotals() {
        // set the tempMap
        Map<Integer, Map<String, Long>> tempMap =
                new HashMap<Integer, Map<String, Long>>();

        // set the first iterator
        Iterator baseIt = plugin.statsForPlayers.entrySet().iterator();
        while (baseIt.hasNext()) {
            // grab the first pair, then the name and the second map
            Map.Entry pairs = (Map.Entry) baseIt.next();
            String name = (String) pairs.getKey();
            if (!name.equalsIgnoreCase("total")) {

                Map<Integer, Map<String, Long>> secondaryMap =
                        (Map<Integer, Map<String, Long>>) pairs.getValue();
                // set the second iterator off of the second map
                Iterator secondaryIt = secondaryMap.entrySet().iterator();
                while (secondaryIt.hasNext()) {
                    // grab the second pair and the type
                    Map.Entry secondPairs = (Map.Entry) secondaryIt.next();
                    int type = (Integer) secondPairs.getKey();
                    if (!tempMap.containsKey(type))
                        tempMap.put(type, new HashMap<String, Long>());

                    // figure out what the total value is, but don't try to grab a null value!
                    // if a value doesn't exist, set it as 0
                    final long totalValue;
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

    public boolean zipBackup(String name) {
        File sourceFolder = new File(plugin.getDataFolder(), "stats");
        String path = plugin.getBackupStatsLocation().replace("~",
                plugin.getDataFolder().toString());

        File dir = new File(path);
        if (!dir.exists())
            if (!dir.mkdirs())
                plugin.getLogger().warning(path + " could not be created successfully, possibly a permissions issue?");

        try {
            File outputFile = new File(dir, name);
            FileOutputStream dest = new FileOutputStream(outputFile.getAbsoluteFile());
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            ArrayList<File> files = new ArrayList<>();
            files = getFiles(sourceFolder, files);
            for (File file : files) {
                FileInputStream fi = new FileInputStream(file.getAbsoluteFile());

                BufferedInputStream origin = new BufferedInputStream(fi, 2048);

                ZipEntry entry = new ZipEntry(file.getParentFile().getName() + "/" + file.getName());
                out.putNextEntry(entry);

                byte data[] = new byte[2048];

                int count;
                while ((count = origin.read(data, 0, 2048)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        int backupNumber = plugin.getBackupStatsNumber();
        ArrayList<String> backups = plugin.getBackups();
        if (plugin.getBackupStatsNumber() != 0)
            while (backups.size() >= backupNumber) {
                File file = new File(backups.get(0));
                if (!file.delete())
                    plugin.getLogger().info("Could not delete " + file.getPath() + ". Perhaps there is a permissions issue?");

                backups.remove(0);
            }

        backups.add(path + "/" + name);
        plugin.incrementBackupNumber();

        ObjectOutputStream os = null;
        try {
            FileOutputStream fileStream = new FileOutputStream(new File(plugin.getDataFolder(), "backup-array"));
            os = new ObjectOutputStream(fileStream);
            os.writeObject(backups);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (IOException e) {
                // you have got to be kidding me..
                e.printStackTrace();
            }
        }

        return true;
    }

    public String getBackupName() {
        String name = plugin.getBackupName();

        String[] backupNameSplit = name.split("\\(~\\)");
        ArrayList<String> splits = new ArrayList<>();
        Date date = new Date();

        for (String s : backupNameSplit) {
            SimpleDateFormat format = new SimpleDateFormat(s);
            format.setTimeZone(TimeZone.getTimeZone(plugin.getTimeZone()));
            splits.add(format.format(date));
        }

        String fileName = "";
        String backupNumber = df.format(plugin.getBackupNumber());

        for (String s : splits) {
            fileName = fileName + s;
            fileName = fileName + backupNumber;
        }

        if (name.startsWith("(~)")) {
            if (!fileName.startsWith(backupNumber))
                fileName = backupNumber + fileName;
        } else {
            if (fileName.startsWith(backupNumber))
                fileName = fileName.substring(backupNumber.length());
        }

        if (name.endsWith("(~)")) {
            if (!fileName.endsWith(backupNumber))
                fileName = fileName + backupNumber;
        } else {
            if (fileName.endsWith(backupNumber))
                fileName = fileName.substring(0, fileName.length() - backupNumber.length());
        }

        fileName = fileName + ".zip";

        return fileName;
    }

    private ArrayList<File> getFiles(File dir, ArrayList<File> files) {
        if (dir.isDirectory())
            for (File file : dir.listFiles())
                getFiles(file, files);
        else
            files.add(dir);

        return files;
    }

}
