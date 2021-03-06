package com.jordair.gmail.VoteReceiver;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class VoteReceiverPlugin extends JavaPlugin {

	private SQLManager manager;
	private boolean openForVoting;
	public java.util.Date opening, closing;
	public static VoteReceiverPlugin instance;
	public Scoreboard votedata;
	private Objective o;

	@Override
	@SuppressWarnings("deprecation")
	public void onEnable() {
		instance = this;
		saveDefaultConfig();

		votedata = Bukkit.getScoreboardManager().getNewScoreboard();
		o = votedata.registerNewObjective("votes", "dummy");
		o.setDisplayName(ChatColor.YELLOW + "Top Voters");
		o.setDisplaySlot(DisplaySlot.SIDEBAR);

		/*
		 * Connect to SQL.
		 */
		manager = new SQLManager(getLogger(), "[VRP]", getConfig().getString("SQL.HOST"), getConfig().getInt("SQL.PORT"), getConfig()
				.getString("SQL.DATABASE"), getConfig().getString("SQL.USER"), getConfig().getString("SQL.PASS"));

		/*
		 * Load the schedule data.
		 */
		if (getConfig().getBoolean("Schedule.ENABLED")) {
			/*
			 * Determine opening and closing time.
			 */
			Date open = Date.valueOf(getConfig().getString("Schedule.OPEN.DATE"));
			Date close = Date.valueOf(getConfig().getString("Schedule.CLOSE.DATE"));
			if (open.compareTo(close) <= 0) {
				Time openTime = Time.valueOf(getConfig().getString("Schedule.OPEN.TIME"));
				Time closeTime = Time.valueOf(getConfig().getString("Schedule.CLOSE.TIME"));
				Calendar openC = Calendar.getInstance();
				openC.setTime(open);
				Calendar closeC = Calendar.getInstance();
				closeC.setTime(close);
				openC.set(Calendar.HOUR_OF_DAY, openTime.getHours());
				openC.set(Calendar.MINUTE, openTime.getMinutes());
				openC.set(Calendar.SECOND, openTime.getSeconds());
				closeC.set(Calendar.HOUR_OF_DAY, closeTime.getHours());
				closeC.set(Calendar.MINUTE, closeTime.getMinutes());
				closeC.set(Calendar.SECOND, closeTime.getSeconds());
				java.util.Date closingDate = closeC.getTime();
				java.util.Date openingDate = openC.getTime();
				/*
				 * Store the dates and announce.
				 */
				opening = openingDate;
				closing = closingDate;

				getLogger().info("Current local time is " + new java.util.Date().toString());
				getLogger().info("Voting starts on " + opening.toString());
				getLogger().info("Voting ends on " + closing.toString());
			} else {
				/*
				 * Schedule was set to close before it was open.
				 */
				getLogger().warning("Schedule closing date is set before opening date!");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		} else
			open(Cause.SERVER_START);

		/*
		 * Check if already closed.
		 */
		if (closing != null)
			if (closing.compareTo(new java.util.Date()) < 0) {
				getLogger().info("Set to close prior to today.");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}

		/*
		 * Disable if not connected.
		 */
		if (!manager.isConnected()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		/*
		 * Register commands and listeners.
		 */
		ToggleCommand tc = new ToggleCommand();
		DisplayCommand dc = new DisplayCommand();
		getServer().getPluginManager().registerEvents(new VoteListener(), this);
		getServer().getPluginManager().registerEvents(new JoinListener(), this);
		getCommand("vr").setExecutor(dc);
		getCommand("vc").setExecutor(dc);
		getCommand("vs").setExecutor(new SpoofCommand());
		getCommand("vrc").setExecutor(new ClearCommand());
		getCommand("vrx").setExecutor(tc);
		getCommand("vro").setExecutor(tc);

		/*
		 * Start processes to open and close voting.
		 */
		if (opening != null) {
			long now = System.currentTimeMillis();
			/*
			 * Already supposed to be open so open voting.
			 */
			if (opening.before(new java.util.Date()))
				open(Cause.SERVER_START);
			else
				/*
				 * Open voting later.
				 */
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					@Override
					public void run() {
						open(Cause.SCHEDULED);
					}
				}, (int) Math.ceil((opening.getTime() - now) / 1000d) * 20L);
			if (closing != null)
				/*
				 * Close voting later.
				 */
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					@Override
					public void run() {
						close(Cause.SCHEDULED);
					}
				}, (int) Math.ceil((closing.getTime() - now) / 1000d) * 20L);
		} else
			opening = new java.util.Date();
	}

	@Override
	public void onDisable() {
		reloadConfig();
		getLogger().info("Disabling VoteReceiver. No longer listening on votes.");
		getServer().getScheduler().cancelTasks(this);
		/*
		 * Disconnect from SQL.
		 */
		if (manager != null && manager.isConnected())
			manager.disconnect();
	}

	public boolean isOpen() {
		return openForVoting;
	}

	public void open(Cause cause) {
		openForVoting = true;
		if (getManager() != null && getManager().isConnected() && cause == Cause.SCHEDULED) {
			getManager().emptyTable("votedata");
			getLogger().info("Vote logs have been cleared due to a scheduled voting commencement!");
		}
		getLogger().info("Voting was enabled.");
		alert(ChatColor.YELLOW + "Voting has been enabled!");

		if (getManager() != null && getManager().isConnected()) {
			refreshVoteData();
		}
	}

	public void refreshVoteData() {
		getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
			public void run() {
				votedata = Bukkit.getScoreboardManager().getNewScoreboard();
				o = votedata.registerNewObjective("votes", "dummy");
				o.setDisplayName(ChatColor.YELLOW + "Top Voters");
				o.setDisplaySlot(DisplaySlot.SIDEBAR);

				int i = 0;

				for (String s : getTop10()) {
					if (i < 10)
						o.getScore(Bukkit.getOfflinePlayer(s)).setScore(getManager().getInt("votedata", s, "votes"));
					i++;
				}
				for (Player p : Bukkit.getOnlinePlayers())
					p.setScoreboard(votedata);
			}
		}, 0L);
	}

	private List<String> getTop10() {
		if (getManager() == null || !getManager().isConnected()) { return new ArrayList<String>(); }
		Map<String, Integer> output = new HashMap<String, Integer>();

		/*
		 * Iterate through the users in the database.
		 */
		for (String user : getManager().getKeys("votedata")) {
			/*
			 * Get their number of votes.
			 */
			int votes = getManager().getInt("votedata", user, "votes");
			output.put(user, votes);
		}
		/*
		 * Sort the users by descending order of votes.
		 */
		ValueComparator bvc = new ValueComparator(output);
		TreeMap<String, Integer> sorted = new TreeMap<String, Integer>(bvc);
		sorted.putAll(output);
		List<String> returned = new ArrayList<String>();
		for (String s : sorted.keySet()) {
			returned.add(s);
		}
		return returned;
	}

	/**
	 * Sorts a map by integer value in descending order.
	 */
	public class ValueComparator implements Comparator<String> {

		Map<String, Integer> base;

		public ValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		@Override
		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b))
				return -1;
			else
				return 1;
		}
	}

	public void close(Cause cause) {
		openForVoting = false;
		getLogger().info("Voting was disabled.");
		alert(ChatColor.YELLOW + "Voting has been disabled!");
	}

	public enum Cause {
		SCHEDULED, SERVER_START, COMMAND;
	}

	/**
	 * Get the SQLManager
	 * 
	 * @return The SQLManager
	 */
	public SQLManager getManager() {
		return manager;
	}

	/**
	 * Send a message to every player on the server if enabled.
	 * 
	 * @param string
	 *            The message.
	 */
	public void alert(String string) {
		if (getConfig().getBoolean("Alert_on_vote"))
			for (Player p : getServer().getOnlinePlayers())
				if (p.hasPermission("votes.alert"))
					p.sendMessage(string);
	}
}
