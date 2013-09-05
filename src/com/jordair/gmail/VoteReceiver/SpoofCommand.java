package com.jordair.gmail.VoteReceiver;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class SpoofCommand implements CommandExecutor {

	private VoteReceiverPlugin plugin;

	public SpoofCommand(VoteReceiverPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("votes.spoof")) {
			Vote vote = new Vote();
			vote.setAddress("127.0.0.1");
			vote.setServiceName("testVote");
			vote.setTimeStamp(System.currentTimeMillis() / 1000 + "");
			vote.setUsername(sender.getName());
			VotifierEvent ve = new VotifierEvent(vote);
			plugin.getServer().getPluginManager().callEvent(ve);
		}
		return false;
	}

}
