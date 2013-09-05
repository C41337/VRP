package com.jordair.gmail.VoteReceiver;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("vro")) {
			if (VoteReceiverPlugin.instance.getManager() != null && VoteReceiverPlugin.instance.getManager().isConnected()) {
				if (!VoteReceiverPlugin.instance.isOpen())
					VoteReceiverPlugin.instance.open(VoteReceiverPlugin.Cause.COMMAND);
				else
					sender.sendMessage("Voting is already open.");
				return true;
			}
		} else if (command.getName().equalsIgnoreCase("vrx"))
			if (VoteReceiverPlugin.instance.getManager() != null && VoteReceiverPlugin.instance.getManager().isConnected()) {
				if (VoteReceiverPlugin.instance.isOpen())
					VoteReceiverPlugin.instance.close(VoteReceiverPlugin.Cause.COMMAND);
				else
					sender.sendMessage("Voting is already closed.");
				return true;
			}

		return false;
	}
}
