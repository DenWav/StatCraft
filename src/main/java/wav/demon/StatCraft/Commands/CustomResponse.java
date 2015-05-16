package wav.demon.StatCraft.Commands;

import org.bukkit.command.CommandSender;

public interface CustomResponse {

    void respondToCommand(final CommandSender sender,  String[] args);
}
