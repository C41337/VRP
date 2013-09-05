package com.jordair.gmail.VoteReceiver;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteListener implements Listener {

	/**
	 * Listen for Votifier events and react accordingly.
	 * 
	 * @param e
	 *            The Votifier event
	 */
	@EventHandler
	private void onVoteMade(VotifierEvent e) {
		if (!VoteReceiverPlugin.instance.isOpen()) { return; }
		/*
		 * Get the Vote made.
		 */
		Vote v = e.getVote();
		if (v.getUsername().equalsIgnoreCase("anonymous")) { return; }
		if (VoteReceiverPlugin.instance.getManager() != null && VoteReceiverPlugin.instance.getManager().isConnected()) {
			/*
			 * Add the voter to the database (if not currently in).
			 */
			VoteReceiverPlugin.instance.getManager().add(v.getUsername());
			/*
			 * Get the voter's current votes.
			 */
			int votes = VoteReceiverPlugin.instance.getManager().getInt("votedata", v.getUsername(), "votes");
			/*
			 * Increment their votes, store the last vote time and broadcast current votes.
			 */
			VoteReceiverPlugin.instance.getManager().set("votedata", v.getUsername(), "votes", votes + 1);
			VoteReceiverPlugin.instance.getManager().set("votedata", v.getUsername(), "last_vote", "'" + v.getTimeStamp() + "'");
			VoteReceiverPlugin.instance.alert(v.getUsername() + " now has " + ChatColor.DARK_RED + (votes + 1) + ChatColor.RESET
					+ " votes.");
		}
	}
}
