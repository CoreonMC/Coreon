package at.blaulix.coreon.command;

import at.blaulix.coreon.handler.CoreonHandler;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

public class CoreonCommand implements CommandExecutor {

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if(!sender.hasPermission("coreon.admin")) {
            sender.sendMessage("You don't have permission to use this command.");
            return false;
        }
        if(sender instanceof Player player){
            CoreonHandler coreonHandler = new CoreonHandler();
            coreonHandler.coreonSettings(player);
            return true;
        }
        return false;
    }
}
