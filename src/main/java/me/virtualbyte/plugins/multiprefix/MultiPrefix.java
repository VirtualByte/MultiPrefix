package me.virtualbyte.plugins.multiprefix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiPrefix extends JavaPlugin implements Listener {
	
	public static Permission	permission	= null;
	public static Chat			chat		= null;
	
	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			Bukkit.getLogger().severe("[MultiPrefix] Vault was not detected - disabling plugin.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		if (!setupPermissions() || !setupChat()) {
			Bukkit.getLogger().severe("[MultiPrefix] Unable to hook into Vault's permission API.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		saveDefaultConfig();
		
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
		}
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(
				net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}
	
	private boolean setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(
				net.milkbowl.vault.chat.Chat.class);
		if (chatProvider != null) {
			chat = chatProvider.getProvider();
		}
		
		return (chat != null);
	}
	
	public String getPrefix(Player player) {
		String prefix = "";
		List<String> groups = new ArrayList<String>(Arrays.asList(permission.getPlayerGroups(null, player)));
		
		if (getConfig().getBoolean("reverse", true)) {
			ListIterator<String> iterator = groups.listIterator(groups.size());
			
			while (iterator.hasPrevious()) {
				String group = (String) iterator.previous();
				prefix += chat.getGroupPrefix(player.getWorld(), group);
			}
		} else {
			ListIterator<String> iterator = groups.listIterator();
			
			while (iterator.hasNext()) {
				String group = (String) iterator.next();
				prefix += chat.getGroupPrefix(player.getWorld(), group);
			}
		}
		
		return ChatColor.translateAlternateColorCodes('&', prefix);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		event.setFormat(event.getFormat().replace("{group_prefix}", getPrefix(event.getPlayer())));
	}
	
}