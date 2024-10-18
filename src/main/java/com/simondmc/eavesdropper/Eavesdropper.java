package com.simondmc.eavesdropper;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Eavesdropper extends JavaPlugin implements Listener {
    HashMap<CommandSender, Integer> subscribers = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("eavesdrop")) {
            try {
                if (args.length == 0) {
                    throw new Exception();
                }
                int level = Integer.parseInt(args[0]);
                if (level < 0 || level > 2) {
                    throw new Exception();
                }
                if (level == 0) {
                    subscribers.remove(sender);
                    sender.sendMessage("§eDisabled eavesdropping.");
                } else {
                    subscribers.put(sender, level);
                    sender.sendMessage("§eEnabled level §a" + level + "§e eavesdropping.");
                }
            } catch (Exception e) {
                sender.sendMessage("§cUsage: /eavesdrop <0/1/2>");
            }
        } else if (label.equalsIgnoreCase("msg")) {
            try {
                if (args.length < 2) {
                    throw new Exception();
                }
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    throw new Exception();
                }
                // there's probably a better way of doing this
                String message = String.join(" ", args).substring(String.join(" ", args).indexOf(" ") + 1);
                sender.sendMessage(String.format("§7§oYou whisper to %s: %s", player.getName(), message));
                player.sendMessage(String.format("§7§o%s whispers to you: %s", sender.getName(), message));
            } catch (Exception e) {
                sender.sendMessage("§cUsage: /msg <player> <message>");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("msg")) {
            if (args.length == 1) {
                return null;
            }
            // disable player name completion after first argument
            return new ArrayList<>();
        }
        return null;
    }

    @EventHandler
    public void onSend(PlayerCommandPreprocessEvent e) {
        subscribers.forEach((subscriber, level) -> {
            subscriber.sendMessage(
                    String.format("§7[Eavesdropper] %s ran the command %s", e.getPlayer().getName(), e.getMessage())
            );
        });
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent e) {
        subscribers.forEach((subscriber, level) -> {
            if (level == 2) {
                subscriber.sendMessage(
                        String.format("§7[Eavesdropper] %s is typing: %s", e.getSender().getName(), e.getBuffer())
                );
            }
        });
    }
}
