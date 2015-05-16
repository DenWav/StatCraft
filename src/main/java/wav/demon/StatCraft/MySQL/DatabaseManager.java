package wav.demon.StatCraft.MySQL;

import com.mysema.query.sql.MySQLTemplates;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.bukkit.OfflinePlayer;
import wav.demon.StatCraft.Querydsl.QPlayers;
import wav.demon.StatCraft.StatCraft;
import wav.demon.StatCraft.Util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {

    private StatCraft plugin;
    private Connection connection;

    public DatabaseManager(StatCraft plugin) {
        this.plugin = plugin;
        String url = "jdbc:mysql://" + plugin.config().mysql.hostname + ":" + plugin.config().mysql.port + "/" + plugin.config().mysql.database;
        try {
            connection = DriverManager.getConnection(url, plugin.config().mysql.username, plugin.config().mysql.password);
        } catch (SQLException ex) {
            plugin.getLogger().severe(red(" *** StatCraft was unable to communicate with the database,"));
            plugin.getLogger().severe(red(" *** please check your settings and reload, StatCraft will"));
            plugin.getLogger().severe(red(" *** now be disabled."));
            plugin.getPluginLoader().disablePlugin(plugin);
        }

    }

    public Connection getConnection() {
        return connection;
    }

    public void setupDatabase()  {
        for (Table table : Table.values()) {
            checkTable(table);
            if (!plugin.isEnabled())
                break;
        }
        if (plugin.isEnabled())
            plugin.getLogger().info("Database verified successfully.");
    }

    private void checkTable(Table table) {
        PreparedStatement intPst = null;
        ResultSet resultSet = null;
        ResultSet intResultSet = null;
        try (PreparedStatement pst =
                     connection.prepareStatement("SELECT ENGINE FROM information_schema.TABLES where TABLE_NAME = ?;")) {
            pst.setString(1, table.getName());
            try {
                // This can fail because there is no table
                resultSet = pst.executeQuery();
            } catch (SQLException e) {
                remakeTable(table, false);
            }

            if (resultSet == null) {
                remakeTable(table, false);
                return;
            }

            DatabaseMetaData dbm = getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, plugin.config().mysql.database + "." + table.getName(), null);
            if (tables.next() && resultSet.next()) {
                // Table exists
                // Make sure the engine is correct
                if (!resultSet.getString("ENGINE").equals("InnoDB")) {
                    plugin.getLogger().warning(table.getName() + " is using an incorrect engine.");
                    remakeTable(table, true);
                } else {
                    // Make sure the columns are correct
                    intPst = connection.prepareStatement(String.format("SHOW COLUMNS FROM %s;", table.getName()));
                    intResultSet = intPst.executeQuery();

                    // Make sure there are columns
                    if (intResultSet.last()) {
                        // Make sure there is the correct number of columns
                        if (intResultSet.getRow() != table.getColumns()) {
                            plugin.getLogger().warning(table.getName() + " has an incorrect number of columns.");
                            remakeTable(table, true);
                        } else {
                            // Make sure the columns are correct
                            intResultSet.beforeFirst();
                            for (String column : table.getColumnNames()) {
                                intResultSet.next();
                                if (!intResultSet.getString("Field").equals(column)) {
                                    plugin.getLogger().warning(table.getName() + " has incorrect columns.");
                                    remakeTable(table, true);
                                    break;
                                }
                            }
                        }
                    } else {
                        plugin.getLogger().warning(table.getName() + " has no columns");
                        remakeTable(table, true);
                    }
                }
            }
            else {
                // Table does not exist
               remakeTable(table, false);
            }
            tables.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resultSet
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Close intPst
            try {
                if (intPst != null)
                    intPst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Close intResultSet
            try {
                if (intResultSet != null)
                    intResultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void remakeTable(Table table, boolean ask) {
        if (!ask) {
            dropTable(table);
            createTable(table);
            plugin.getLogger().info("Created table `" + table.getName() + "`.");
        } else {
            if (plugin.config().mysql.forceSetup) {
                dropTable(table);
                createTable(table);
                plugin.getLogger().info("Created table `" + table.getName() + "`.");
            } else {
                plugin.getLogger().severe(
                    red("*** " + table.getName() + " is not set up correctly and conflicts with StatCraft's setup."));
                plugin.getLogger().severe(
                    red("*** Change mysql.forceSetup, remove or rename this table, or create a new database for"));
                plugin.getLogger().severe(
                    red("*** StatCraft to use. StatCraft will not run unless there are no conflicting tables."));
                plugin.getLogger().severe(
                    red("*** StatCraft will now be disabled."));
                plugin.getPluginLoader().disablePlugin(plugin);
            }
        }
    }

    private String red(String text) {
        return "\u001b[1;31m" + text + "\u001b[m";
    }

    private void dropTable(Table table) {
        try (PreparedStatement pst = getConnection().prepareStatement(
                String.format("DROP TABLE IF EXISTS %s;", table.getName()))) {
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable(Table table) {
        try (PreparedStatement pst = getConnection().prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + table.getName() + table.getCreate() + " ENGINE=InnoDB;")) {
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized int getPlayerId(UUID uuid) {
        byte[] array = Util.UUIDToByte(uuid);

        SQLQuery query = new SQLQuery(getConnection(), SQLTemplates.DEFAULT);
        Integer res = query
                .from(QPlayers.players)
                .where(QPlayers.players.uuid.eq(array))
                .uniqueResult(QPlayers.players.id);
        return res == null ? -1 : res;
    }

    public synchronized int getPlayerId(String name) {
        SQLQuery query = new SQLQuery(getConnection(), SQLTemplates.DEFAULT);
        QPlayers p = QPlayers.players;
        Integer res = query
                .from(p)
                .where(p.name.eq(name))
                .uniqueResult(p.id);

        if (res == null) {
            // it failed to find a player by that name, so attempt to do a UUID lookup
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);

            // Check if it's an offline UUID
            if (player.getUniqueId().version() < 4)
                return -1;

            res = query
                    .from(p)
                    .where(p.uuid.eq(Util.UUIDToByte(player.getUniqueId())))
                    .uniqueResult(p.id);

            if (res == null)
                return -1;

            // fix the UUID / name pairing
            SQLUpdateClause clause = new SQLUpdateClause(getConnection(), SQLTemplates.DEFAULT, p);
            clause
                    .where(p.uuid.eq(Util.UUIDToByte(player.getUniqueId())))
                    .set(p.name, name)
                    .execute();

            return res;
        } else {
            return res;
        }
    }

    public synchronized SQLQuery getNewQuery() {
        return new SQLQuery(getConnection(), MySQLTemplates.DEFAULT);
    }

    public synchronized SQLUpdateClause getUpdateClause(RelationalPath path) {
        return new SQLUpdateClause(getConnection(), MySQLTemplates.DEFAULT, path);
    }

    public synchronized SQLInsertClause getInsertClause(RelationalPath path) {
        return new SQLInsertClause(getConnection(), MySQLTemplates.DEFAULT, path);
    }

    // In case something goes wrong, at least try to close the connection nicely
    @Override
    public void finalize() throws Throwable {
        super.finalize();
        if (connection != null)
            connection.close();
    }
}
