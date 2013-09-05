package com.jordair.gmail.VoteReceiver;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (VoteReceiverPlugin.instance.getManager() != null && VoteReceiverPlugin.instance.getManager().isConnected()) {
			if (VoteReceiverPlugin.instance.getManager().emptyTable("votedata") != null)
				sender.sendMessage(ChatColor.YELLOW + "Vote log has been cleared.");
			else
				sender.sendMessage(ChatColor.RED + "Failed to clear vote log.");
			return true;
		}

		return false;
	}
}
