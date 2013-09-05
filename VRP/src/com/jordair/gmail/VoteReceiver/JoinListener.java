/**
 * 
 */
package com.jordair.gmail.VoteReceiver;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Jordan
 * 
 */
public class JoinListener implements Listener {

	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		e.getPlayer().setScoreboard(VoteReceiverPlugin.instance.votedata);
	}
}
