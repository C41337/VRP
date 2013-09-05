package com.jordair.gmail.VoteReceiver;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DisplayCommand implements CommandExecutor {

	private VoteReceiverPlugin plugin;
	String zone = Calendar.getInstance().getTimeZone().getDisplayName(false, TimeZone.SHORT);

	public DisplayCommand(VoteReceiverPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("votes.view")) {
			if (plugin.getManager() != null && plugin.getManager().isConnected()) {
				if (command.getName().equalsIgnoreCase("vr")) {
					displayVotes(sender, args);
					return true;
				} else if (command.getName().equalsIgnoreCase("vc")) {
					if (plugin.isOpen()) {
						displayTotal(sender);
					} else {
						sender.sendMessage("Voting is not open at the moment.");
					}
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Display the total number of votes in the database.
	 * 
	 * @param sender
	 *            The sender to display to
	 */
	private void displayTotal(CommandSender sender) {
		/*
		 * Iterate through the users in the database.
		 */
		int votes = 0;
		for (String user : plugin.getManager().getKeys("votedata")) {
			/*
			 * Get their number of votes and add it to the total.
			 */
			votes += plugin.getManager().getInt("votedata", user, "votes");
		}
		/*
		 * Write the header.
		 */
		sender.sendMessage("-=-=-=-=- " + ChatColor.YELLOW + "Vote Log Total Votes" + ChatColor.RESET + " -=-=-=-=-");
		sender.sendMessage("");
		if (plugin.opening != null) {
			sender.sendMessage(ChatColor.RED + "" + votes + ChatColor.RESET + " votes since " + ChatColor.RED + plugin.opening.toString()
					+ ChatColor.RESET + " " + zone + ".");
		} else {
			sender.sendMessage(ChatColor.RED + "" + votes + ChatColor.RESET + " votes.");
		}
	}

	/**
	 * Sort the votes in descending order and output the top n to the
	 * CommandSender.
	 * 
	 * @param sender
	 *            The CommandSender
	 * @param args
	 *            The command arguments
	 */
	@SuppressWarnings("deprecation")
	private void displayVotes(CommandSender sender, String[] args) {
		Map<String, Integer> output = new HashMap<String, Integer>();
		Map<String, String> crossinput = new HashMap<String, String>();

		/*
		 * Iterate through the users in the database.
		 */
		for (String user : plugin.getManager().getKeys("votedata")) {
			/*
			 * Get their number of votes.
			 */
			int votes = plugin.getManager().getInt("votedata", user, "votes");
			/*
			 * Get their last vote timestamp and store everything.
			 */
			String last = plugin.getManager().getString("votedata", user, "last_vote");
			output.put(user, votes);
			crossinput.put(user, last);
		}
		/*
		 * Sort the users by descending order of votes.
		 */
		ValueComparator bvc = new ValueComparator(output);
		TreeMap<String, Integer> sorted = new TreeMap<String, Integer>(bvc);
		sorted.putAll(output);
		/*
		 * Determine how many scores to output.
		 */
		int amt = 0;
		int max = 0;
		if (args.length == 1) {
			try {
				max = Integer.parseInt(args[0]);
			} catch (NumberFormatException exc) {
				max = 0;
			}
		}
		/*
		 * Write the header.
		 */
		sender.sendMessage("-=-=-=-=- " + ChatColor.YELLOW + "Vote Log Top " + ChatColor.RED + (max != 0 ? "" + max : "10")
				+ ChatColor.RESET + " -=-=-=-=-");
		sender.sendMessage("");
		/*
		 * Iterate through the sorted list and output the data.
		 */
		for (String s : sorted.keySet()) {
			/*
			 * Reach limit, finish outputting.
			 */
			if ((max > 0 && amt >= max) || (max == 0 && amt >= 10)) {
				continue;
			}
			String last = crossinput.get(s);
			Date d = new Date();
			try {
				d.setTime(Long.parseLong(last) * 1000);
				last = (d.getYear() + 1900) + "-" + (d.getMonth() + 1) + "-" + d.getDate() + " " + d.getHours() + ":"
						+ (d.getMinutes() > 9 ? d.getMinutes() : "0" + d.getMinutes()) + ":"
						+ (d.getSeconds() > 9 ? d.getSeconds() : "0" + d.getSeconds());
			} catch (Exception e) {
				last = last.replaceAll(" -0700", "");
			}
			sender.sendMessage(ChatColor.BOLD + "" + (amt + 1) + ". " + ChatColor.RESET + ChatColor.GOLD + s + ChatColor.RESET + ": "
					+ ChatColor.RED + output.get(s) + ChatColor.RESET + " votes. Last vote: " + ChatColor.RED + last + ChatColor.RESET
					+ " " + zone + ".");
			sender.sendMessage("");
			amt++;
		}
	}

	/**
	 * Sorts a map by integer value in descending order.
	 */
	public class ValueComparator implements Comparator<String> {

		Map<String, Integer> base;

		public ValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			}
		}
	}
}
