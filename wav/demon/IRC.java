package wav.demon;

import jerklib.ConnectionManager;
import jerklib.Profile;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.MessageEvent;
import jerklib.events.modes.ModeAdjustment;
import jerklib.listeners.IRCEventListener;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Scanner;

public class IRC implements IRCEventListener {

    final private String nick = "OCMCB";
    final private String pass = "";
    private IRCBot plugin;
    private ConnectionManager manager;
    private Session session;

    @Override
    public void receiveEvent(IRCEvent ircEvent) {
        if (ircEvent.getType() == IRCEvent.Type.CONNECT_COMPLETE) {
            ircEvent.getSession().sayPrivate("nickserv", "identify " + pass);
            ircEvent.getSession().join("#ocminecraft");
        } else if (ircEvent.getType() == IRCEvent.Type.CHANNEL_MESSAGE) {
            MessageEvent me = (MessageEvent) ircEvent;
            // save to final variable to ensure there is only one method call
            final String message = me.getMessage();

              ////////////////////////
             //    List Players    //
            ////////////////////////
            if (message.equals(".list")) {
                // list players on the server (this is more for IRC)
                String playerList = "";
                for (Player player : plugin.getServer().getOnlinePlayers())
                    playerList = playerList + player.getName() + " ";

                if (playerList.equalsIgnoreCase("")) {
                   me.getChannel().say("There aren't currently any players online!");
                } else {
                    me.getChannel().say(playerList);
                }
              ////////////////////////
             //     List Deaths    //
            ////////////////////////
            } else if (message.startsWith("!deaths")) {
                String name;
                String names[];

                // figure out what name we need to list the deaths for
                if (message.replace("\\s+$", "").equals("!deaths"))
                    name = me.getNick();
                else
                    name = message.substring("!deaths ".length());

                names = name.split(" ");

                for (String n : names) {
                    try {
                        File deathList = new File("/opt/msm/servers/ocminecraft/deaths/" + n);
                        Scanner scanner = new Scanner(deathList);
                        String deaths = scanner.nextLine();
                        me.getChannel().say(name + " has died " + deaths + " times!");
                        scanner.close();
                    } catch (FileNotFoundException e) {
                        me.getChannel().say(name + " has no deaths on record!");
                    }
                }
            } else if (message.startsWith("!kill")) {
                String name = me.getNick();
                String args[];
                boolean op = false;
                for (String ops : me.getChannel().getNicksForMode(ModeAdjustment.Action.PLUS, 'O')) {
                    if (name.equalsIgnoreCase("ops"))
                        op = true;
                }
                if (op) {
                    args = message.substring("!kill ".length()).split(" ");
                    if (args.length == 1) {
                        kill(args[0], "", me);
                    } else if (args.length == 2) {
                        kill(args[0], args[1], me);
                    } else {
                        me.getChannel().say("You must specify only a target, or a target and a kill type. Types are \"explode\" or \"slap\".");
                    }
                } else {
                    me.getChannel().say("You don't have permission to do that!");
                }

            // Count Deaths
              /////////////////////////
             // START: Count Deaths //
            /////////////////////////
//            } else if (message.matches(".* was squashed by a falling anvil")) {
//                final String name = message.substring(0, message.indexOf(" was squashed by a falling anvil"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was pricked to death")) {
//                final String name = message.substring(0, message.indexOf(" was pricked to death"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* walked into a cactus whilst trying to escape .*")) {
//                final String name = message.substring(0, message.indexOf(" walked into a cactus whilst trying to escape "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was shot by arrow")) {
//                final String name = message.substring(0, message.indexOf(" was shot by arrow"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* drowned")) {
//                final String name = message.substring(0, message.indexOf(" drowned"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* drowned whilst trying to escape .*")) {
//                final String name = message.substring(0, message.indexOf(" drowned whilst trying to escape "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* blew up")) {
//                final String name = message.substring(0, message.indexOf(" blew up"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was blown up by .*")) {
//                final String name = message.substring(0, message.indexOf(" was blown up by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* hit the ground too hard")) {
//                final String name = message.substring(0, message.indexOf(" hit the ground too hard"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* fell from a high place")) {
//                final String name = message.substring(0, message.indexOf(" fell from a high place"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* fell off a ladder")) {
//                final String name = message.substring(0, message.indexOf(" fell off a ladder"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* fell off some vines")) {
//                final String name = message.substring(0, message.indexOf(" fell off some vines"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* fell out of the water")) {
//                final String name = message.substring(0, message.indexOf(" fell out of the water"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* fell into a patch of fire")) {
//                final String name = message.substring(0, message.indexOf(" fell into a patch of fire"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* fell into a patch of cacti")) {
//                final String name = message.substring(0, message.indexOf(" fell into a patch of cacti"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was doomed to fall .*")) {
//                final String name = message.substring(0, message.indexOf(" was doomed to fall "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was shot off some vines by .*")) {
//                final String name = message.substring(0, message.indexOf(" was shot off some vines by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was shot off a ladder by .*")) {
//                final String name = message.substring(0, message.indexOf(" was shot off a ladder by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was blown from a high place by .*")) {
//                final String name = message.substring(0, message.indexOf(" was blown from a high place by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* went up in flames")) {
//                final String name = message.substring(0, message.indexOf(" went up in flames"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* burned to death")) {
//                final String name = message.substring(0, message.indexOf(" burned to death"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was burnt to a crisp whilst fighting .*")) {
//                final String name = message.substring(0, message.indexOf(" was burnt to a crisp whilst fighting "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* walked into a fire whilst fighting .*")) {
//                final String name = message.substring(0, message.indexOf(" walked into a fire whilst fighting "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was slain by .*")) {
//                final String name = message.substring(0, message.indexOf(" was slain by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was shot by .*")) {
//                final String name = message.substring(0, message.indexOf(" was shot by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was fireballed by .*")) {
//                final String name = message.substring(0, message.indexOf(" was fireballed by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was killed by .* using magic")) {
//                final String name = message.substring(0, message.indexOf(" was killed by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* got finished off by .* using .*")) {
//                final String name = message.substring(0, message.indexOf(" got finished off by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was slain by .* using .*")) {
//                final String name = message.substring(0, message.indexOf(" was slain by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* tried to swim in lava")) {
//                final String name = message.substring(0, message.indexOf(" tried to swim in lava"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* tried to swim in lava while trying to escape .*")) {
//                final String name = message.substring(0, message.indexOf(" tried to swim in lava while trying to escape "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* died")) {
//                final String name = message.substring(0, message.indexOf(" died"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* got finished off by .* using .*")) {
//                final String name = message.substring(0, message.indexOf(" got finished off by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was slain by .* using .*")) {
//                final String name = message.substring(0, message.indexOf(" was slain by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was shot by .*")) {
//                final String name = message.substring(0, message.indexOf(" was shot by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was killed by .* using magic")) {
//                final String name = message.substring(0, message.indexOf(" was killed by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was killed by magic")) {
//                final String name = message.substring(0, message.indexOf(" was killed by magic"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* starved to death")) {
//                final String name = message.substring(0, message.indexOf(" starved to death"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* suffocated in a wall")) {
//                final String name = message.substring(0, message.indexOf(" suffocated in a wall"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was killed while trying to hurt .*")) {
//                final String name = message.substring(0, message.indexOf(" was killed while trying to hurt "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was pummeled by .*")) {
//                final String name = message.substring(0, message.indexOf(" was pummeled by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* fell out of the world")) {
//                final String name = message.substring(0, message.indexOf(" fell out of the world"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* fell from a high place and fell out of the world")) {
//                final String name = message.substring(0, message.indexOf(" fell from a high place and fell out of the world"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* was knocked into the void by .*")) {
//                final String name = message.substring(0, message.indexOf(" was knocked into the void by "));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            } else if (message.matches(".* withered away")) {
//                final String name = message.substring(0, message.indexOf(" withered away"));
//                final String deathMessage = message.substring(message.indexOf(name) + name.length(), message.length());
//                addDeathType(name, deathMessage);
//            }
              /////////////////////////
             //  END: Count Deaths  //
            /////////////////////////
            } else {
                System.out.println(ircEvent.getType() + " " + ircEvent.getRawEventData());
            }
        }
    }

    public IRC(IRCBot plugin) {
        this.plugin = plugin;
    }

    public void enableIRC() {
        manager = new ConnectionManager(new Profile(nick));
        session = manager.requestConnection("irc.freenode.net");
        session.addIRCEventListener(this);
    }

    public void disableIRC() {
        manager.quit("Bye!");
    }

    public Session getSession() {
        return session;
    }

    private void kill(String targetName, String type, MessageEvent me) {
        Player target = (plugin.getServer().getPlayer(targetName));
        if (target == null) {
            me.getChannel().say(targetName + " is not online!");
        } else {
            if (type.equalsIgnoreCase("explode")) {
                target.getWorld().createExplosion(target.getLocation(), 0);
                target.setHealth(0);
            } else if (type.equalsIgnoreCase("slap")) {
                target.setHealth(target.getHealth() - 1);
            } else if (type.equalsIgnoreCase("")) {
                target.setHealth(0);
            } else {
                me.getChannel().say("I don't recognize that kill type. Must be either \"explode\" or \"slap\".");
            }
        }
    }
}
