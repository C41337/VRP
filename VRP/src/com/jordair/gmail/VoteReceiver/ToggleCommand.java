package com.jordair.gmail.VoteReceiver;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleCommand implements CommandExecutor {

	private VoteReceiverPlugin plugin;

	public ToggleCommand(VoteReceiverPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("vro")) {
			if (sender.hasPermission("votes.open")) {
				if (plugin.getManager() != null && plugin.getManager().isConnected()) {
					if (!plugin.isOpen()) {
						plugin.open(VoteReceiverPlugin.Cause.COMMAND);
					} else {
						sender.sendMessage("Voting is already open.");
					}
					return true;
				}
			}
		} else if (command.getName().equalsIgnoreCase("vrx")) {
			if (sender.hasPermission("votes.close")) {
				if (plugin.getManager() != null && plugin.getManager().isConnected()) {
					if (plugin.isOpen()) {
						plugin.close(VoteReceiverPlugin.Cause.COMMAND);
					} else {
						sender.sendMessage("Voting is already closed.");
					}
					return true;
				}
			}
		}

		return false;
	}
}
