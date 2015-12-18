package com.Nateb1121.gravatar;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.security.MessageDigest;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;


public class Gravatar extends JavaPlugin{
	private String email;
	private CachedServerIcon icon;
	private boolean saveAsFile;
	
	@Override
	public void onEnable(){
		getServer().getLogger().info("Nateb1121's GravatarIcon has been enabled!");
		
		if (!new File(getDataFolder(), "config.yml").exists()) {
			saveDefaultConfig();
		}
		
		
		loadEmail();
		getServer().getPluginManager().registerEvents(new Listeners(),this );	
		
		icon = getIcon();
		saveAsFile = getConfig().getBoolean("SAVEFILE");
		getLogger().info("Saving files: " + saveAsFile);
	}
	
	private void loadEmail(){
		email = getConfig().getString("EMAIL");
		email = email.toLowerCase();
		email = generateHash(email);
	}
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("updateImage")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(p.hasPermission("gravatar.use") || p.isOp() || p.hasPermission("GravatarIcon.use") || p.hasPermission("GravatarServerIcon.use")){
					try {
						loadEmail();
						icon = getIcon();
						sender.sendMessage("Success!");
					} catch (Exception e) {
						sender.sendMessage("There has been an error... I'm sorry!");
						e.printStackTrace();
					}
					return true;
				} else {
					p.sendMessage("Sorry, but you don't have permission to use this.");
				}
			} else {
				try {
					loadEmail();
					icon = getIcon();
					sender.sendMessage("Success!");
				} catch (Exception e) {
					sender.sendMessage("There has been an error... I'm sorry!");
					e.printStackTrace();
				}
				return true;
			}
			
		}
			
	    return false;
	}
	
	private String generateHash(String s){
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
	        md.update(s.getBytes());
	 
	        byte byteData[] = md.digest();
	 
	        //convert the byte to hex format method 1
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) {
	        	sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        
	        return sb.toString();
	        
		} catch(Exception e) {
			Bukkit.getServer().getLogger().severe("COULD NOT CALCULATE HASH");
			return "";
		}	
	}
	
	private BufferedImage getImage(){
		try{
			URL url = new URL("http://www.gravatar.com/avatar/"+email+"?s=64");
			getLogger().info("URL: " + "http://www.gravatar.com/avatar/"+email+"?s=64");
			Image image = ImageIO.read(url);
			
			//Save the file if need be. 
			if(saveAsFile){
				File picture = new File(getDataFolder() + File.separator + "serverIcon.png");
				ImageIO.write((BufferedImage) image, "png", picture);
				getLogger().info("Image has been saved incase Gravatar is unreachable.");
			}
			 
			return (BufferedImage) image;
		} catch(Exception e) {
			getLogger().severe("Error loading image from Gravatar using file on Disk!");
			
			try{
				File file = new File(getDataFolder() + File.separator + "serverIcon.png");
				Image image = ImageIO.read(file);
				return (BufferedImage) image;
			} catch(Exception fileRead){
				getLogger().severe("Could not load image from Disc!");
				return null;
			}
		}
	}
	
	private CachedServerIcon getIcon(){
		try{
			return Bukkit.loadServerIcon(getImage());
		} catch (Exception e){
			getLogger().severe("Loading failed.");
			return null;
		}
	}
	
	
	public class Listeners implements Listener {
		@EventHandler
		public void serverPing(ServerListPingEvent evt){
			evt.setServerIcon(icon);
		}

	}
	
}