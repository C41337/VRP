package com.jordair.gmail.VoteReceiver;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteListener implements Listener {

	private VoteReceiverPlugin plugin;

	public VoteListener(VoteReceiverPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Listen for Votifier events and react accordingly.
	 * 
	 * @param e
	 *            The Votifier event
	 */
	@EventHandler
	private void onVoteMade(VotifierEvent e) {
		if (!plugin.isOpen()) { return; }
		/*
		 * Get the Vote made.
		 */
		Vote v = e.getVote();
		if (v.getUsername().equalsIgnoreCase("anonymous")) { return; }
		if (plugin.getManager() != null && plugin.getManager().isConnected()) {
			/*
			 * Add the voter to the database (if not currently in).
			 */
			plugin.getManager().add(v.getUsername());
			/*
			 * Get the voter's current votes.
			 */
			Object o = plugin.getManager().get("votedata", v.getUsername(), "votes");
			int votes = 0;
			if (o instanceof Integer) {
				votes = (Integer) o;
			}
			/*
			 * Increment their votes, store the last vote time and broadcast current votes.
			 */
			plugin.getManager().set("votedata", v.getUsername(), "votes", votes + 1);
			plugin.getManager().set("votedata", v.getUsername(), "last_vote", "'" + v.getTimeStamp() + "'");
			plugin.alert(v.getUsername() + " now has " + ChatColor.DARK_RED + (votes + 1) + ChatColor.RESET + " votes.");
		}
	}
}
