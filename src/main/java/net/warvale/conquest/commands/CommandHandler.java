package net.warvale.conquest.commands;

import java.util.ArrayList;
import java.util.List;

import net.warvale.conquest.ConquestCore;
import net.warvale.conquest.commands.admin.SetLobbyCommand;
import net.warvale.conquest.commands.admin.SetStateCommand;
import net.warvale.conquest.commands.admin.SetdebugCommand;
import net.warvale.conquest.exceptions.CommandException;
import net.warvale.conquest.message.MessageManager;
import net.warvale.conquest.message.PrefixType;

import org.bukkit.ChatColor;
import org.bukkit.command.*;


public class CommandHandler implements CommandExecutor, TabCompleter {
    private final ConquestCore plugin;

    public CommandHandler(ConquestCore plugin) {
        this.plugin = plugin;
    }

    private List<AbstractCommand> cmds = new ArrayList<AbstractCommand>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        AbstractCommand command = getCommand(cmd.getName());

        if (command == null) { // this shouldn't happen, it only uses registered commands but incase.
            return true;
        }

        if (!sender.hasPermission(command.getPermission())) {
            sender.sendMessage("§cYou do not have permission to execute this command");
            return true;
        }

        try {
            if (!command.execute(sender, args)) {
                sender.sendMessage("Usage: " + command.getUsage());
            }
        } catch (CommandException ex) {
            sender.sendMessage(ChatColor.RED + ex.getMessage()); // send them the exception message
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + ex.getClass().getName() + ": " + ex.getMessage());
            ex.printStackTrace(); // send them the exception and tell the console the error if its not a command exception
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        AbstractCommand command = getCommand(cmd.getName());

        if (command == null) { // this shouldn't happen, it only uses registered commands but incase.
            return null;
        }

        if (!sender.hasPermission(command.getPermission())) {
            return null;
        }

        try {
            List<String> list = command.tabComplete(sender, args);

            // if the list is null, replace it with everyone online.
            if (list == null) {
                return null;
            }

            // I don't want anything done if the list is empty.
            if (list.isEmpty()) {
                return list;
            }

            List<String> toReturn = new ArrayList<String>();

            if (args[args.length - 1].isEmpty()) {
                for (String type : list) {
                    toReturn.add(type);
                }
            } else {
                for (String type : list) {
                    if (type.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                        toReturn.add(type);
                    }
                }
            }

            return toReturn;
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + ex.getClass().getName() + ": " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Get a uhc command.
     *
     * @param name The name of the uhc command
     * @return The Command if found, null otherwise.
     */
    protected AbstractCommand getCommand(String name) {
        for (AbstractCommand cmd : cmds) {
            if (cmd.getName().equalsIgnoreCase(name)) {
                return cmd;
            }
        }
        
        return null;
    }

    /**
     * Register all the commands.
     */
    public void registerCommands() {

        //admin
        cmds.add(new SetStateCommand());
        cmds.add(new SetdebugCommand());
        cmds.add(new SetLobbyCommand());



        for (AbstractCommand cmd : cmds) {
            PluginCommand pCmd = plugin.getCommand(cmd.getName());
            
            cmd.setupInstances(plugin);

            // if its null, broadcast the command name so I know which one it is (so I can fix it).
            if (pCmd == null) {
                MessageManager.broadcast(PrefixType.MAIN, cmd.getName());
                continue;
            }

            pCmd.setExecutor(this);
            pCmd.setTabCompleter(this);
        }
    }
}