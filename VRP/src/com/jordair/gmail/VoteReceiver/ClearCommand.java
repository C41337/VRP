package com.jordair.gmail.VoteReceiver;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearCommand implements CommandExecutor {

	private VoteReceiverPlugin plugin;

	public ClearCommand(VoteReceiverPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("votes.clear")) {
			if (plugin.getManager() != null && plugin.getManager().isConnected()) {
				if (plugin.getManager().emptyTable("votedata")) {
					sender.sendMessage(ChatColor.YELLOW + "Vote log has been cleared.");
				} else {
					sender.sendMessage(ChatColor.RED + "Failed to clear vote log.");
				}
				return true;
			}
		}

		return false;
	}
}
