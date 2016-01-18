package com.martinambrus.wgchrf;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.mewin.util.Util;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;

public class PlayerListener implements Listener {

    private WGChestRefillFlagPlugin plugin;
    private WorldGuardPlugin wgp;
    private Map<Location, ItemStack[]> originalContents = new HashMap<Location, ItemStack[]>();

    public PlayerListener(WGChestRefillFlagPlugin plugin, WorldGuardPlugin wgp) {
    	this.wgp = wgp;
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDispenserDispense(BlockDispenseEvent event) {
    	Block block = event.getBlock();

    	// check if this originated from a dispenser within a region with the right flag
    	if (block.getType().equals(Material.DISPENSER)) {
    		Boolean refills = (Boolean)Util.getFlagValue(this.wgp, event.getBlock().getLocation(), (Flag<?>)this.plugin.CHESTSAUTOFILL_FLAG);

    		if (refills == true) {
    			// re-add the same item that was just dispensed by a auto-refillable dispenser
    			((Dispenser) event.getBlock().getState()).getInventory().addItem(event.getItem());
    		}
    	}
    }

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
		// check if we're on a protected ground
		Boolean refills = (Boolean)Util.getFlagValue(this.wgp, event.getPlayer().getLocation(), (Flag<?>)this.plugin.CHESTSAUTOFILL_FLAG);
		if (refills == null || refills != true) {
			return;
		}

		InventoryHolder holder = event.getInventory().getHolder();
		Location location = null;

		if (holder instanceof Chest) {
			location = ((Chest) holder).getBlock().getLocation();
		} else if (holder instanceof Dropper) {
			location = ((Dropper) holder).getBlock().getLocation();
		} else if (holder instanceof Hopper) {
			location = ((Hopper) holder).getBlock().getLocation();
		} else if (holder instanceof Dispenser) {
			location = ((Dispenser) holder).getBlock().getLocation();
		} else {
			return;
		}

		if (!this.originalContents.containsKey(location)) {
			// this is a little dirty hack, since InventoryCloseEvent seems to hold the instance
			// of the original block's inventory somewhere and modify its values in the this.originalContents
			// variable, even though there really shouldn't be any link there anymore
			// ... we will therefore close the inventory which was opened just now to keep
			//     our initial set of values intact and re-open it once everything is saved
			this.originalContents.put(location, event.getInventory().getContents().clone());
			event.getPlayer().closeInventory();
			event.getPlayer().openInventory(event.getInventory());
		}
    }

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
		// check if we're on a protected ground
		Boolean refills = (Boolean)Util.getFlagValue(this.wgp, event.getPlayer().getLocation(), (Flag<?>)this.plugin.CHESTSAUTOFILL_FLAG);
		if (refills == null || refills != true) {
			return;
		}

		InventoryHolder holder = event.getInventory().getHolder();
		Location location = null;

		if (holder instanceof Chest) {
			location = ((Chest) holder).getBlock().getLocation();
		} else if (holder instanceof Dropper) {
			location = ((Dropper) holder).getBlock().getLocation();
		} else if (holder instanceof Hopper) {
			location = ((Hopper) holder).getBlock().getLocation();
		} else if (holder instanceof Dispenser) {
			location = ((Dispenser) holder).getBlock().getLocation();
		} else {
			return;
		}

		// it's possible that the flag was turned on while the inventory was open, so we can't know its
		// original content yet, and thus we'll allow anything that was changed and let it be
		if (this.originalContents.containsKey(location)) {
			event.getInventory().setContents(this.originalContents.get(location));
		}
    }

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
		// remove this block's location if it's contained within out cache
		if (this.originalContents.containsKey(event.getBlock().getLocation())) {
			this.originalContents.remove(event.getBlock().getLocation());
		}
    }

    /*@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @SuppressWarnings({ "deprecation" })
    public void onInteract(PlayerInteractEvent event) throws Exception {
    	Player player = event.getPlayer();
        Boolean refills = (Boolean)Util.getFlagValue(this.wgp, player.getLocation(), (Flag<?>)this.plugin.CHESTSAUTOFILL_FLAG);
    	Material clicked = event.getClickedBlock().getType();

        if (
        	refills != null
        	&&
        	refills == true
        	&& event.getAction() == Action.RIGHT_CLICK_BLOCK
        	&&
        		(clicked.equals(Material.CHEST)
        		 || clicked.equals(Material.TRAPPED_CHEST)
        		 || clicked.equals(Material.DISPENSER)
        		 || clicked.equals(Material.HOPPER)
        		 || clicked.equals(Material.HOPPER_MINECART)
        		 || clicked.equals(Material.DROPPER)
        		)
        ) {
        	event.setCancelled(true);
        	switch (clicked) {
        		case CHEST:
        		case TRAPPED_CHEST:
        			Chest chest = (Chest) event.getClickedBlock().getState();
        			Inventory chestInventory = chest.getInventory();
        			int inventorySize = chestInventory.getSize();
        	    	ItemStack[] newInventory = chestInventory.getContents().clone();

        	    	Inventory playerInventory = Bukkit.createInventory(player, inventorySize, capitalize(chestInventory.getType().name()));
        	        playerInventory.setContents(newInventory);

        			player.openInventory(playerInventory);
        			break;

        		case DISPENSER:
        		case DROPPER:
        		case HOPPER:
        		case HOPPER_MINECART:
        			//Dispenser dispenser = (Dispenser) event.getClickedBlock().getState();
        	        //Inventory newDispenserInventory = Bukkit.createInventory(player, InventoryType.DISPENSER);
        	        //newDispenserInventory.setContents(dispenser.getInventory().getContents().clone());
        	        //player.openInventory(newDispenserInventory);
        			// no access to dispensers - shift-clicking on virtual dispenser inventories
        			// generates a huge error stacktrace
        			player.sendMessage(ChatColor.RED + "Access Denied");
        			break;
			default:
				throw new Exception("Unexpected material found on interact.");
        	}
        }
    }*/









    /*
	private String capitalize(final String line) {
    	return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
	}

    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDropperDrop(BlockDispenseEvent event) {
    	Block block = event.getBlock();

    	// check if this originated from a dispenser within a region with the right flag
    	if (block.getType().equals(Material.DISPENSER)) {
    		Boolean refills = (Boolean)Util.getFlagValue(this.wgp, event.getBlock().getLocation(), (Flag<?>)this.plugin.CHESTSAUTOFILL_FLAG);

    		if (refills == true) {
    			event.setCancelled(true);
    			Dispenser dispenser = (Dispenser) event.getBlock().getState();
    			ItemStack[] oldContents = dispenser.getInventory().getContents().clone();
    			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

					@Override
					public void run() {
						plugin.getLogger().info("Setting...");
						for (ItemStack stack : oldContents) {
							if (stack != null) {
								plugin.getLogger().info("stack: " + stack.toString());
							} else {
								plugin.getLogger().info("stack was null");
							}
						}
						dispenser.getInventory().setContents(oldContents);
					}
				}, 20);
    		}
    	}
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @SuppressWarnings({ "deprecation" })
    public void onInteract(PlayerInteractEvent event) throws Exception {
    	Player player = event.getPlayer();
        Boolean refills = (Boolean)Util.getFlagValue(this.wgp, player.getLocation(), (Flag<?>)this.plugin.CHESTSAUTOFILL_FLAG);
    	Material clicked = event.getClickedBlock().getType();

        if (
        	refills != null
        	&&
        	refills == true
        	&& event.getAction() == Action.RIGHT_CLICK_BLOCK
        	&&
        		(clicked.equals(Material.CHEST)
        		 || clicked.equals(Material.TRAPPED_CHEST)
        		 || clicked.equals(Material.DISPENSER)
        		 || clicked.equals(Material.HOPPER)
        		 || clicked.equals(Material.HOPPER_MINECART)
        		 || clicked.equals(Material.DROPPER)
        		)
        ) {
        	event.setCancelled(true);
        	switch (clicked) {
        		case CHEST:
        		case TRAPPED_CHEST:
        			Chest chest = (Chest) event.getClickedBlock().getState();
        			Inventory chestInventory = chest.getInventory();
        			int inventorySize = chestInventory.getSize();
        	    	ItemStack[] newInventory = chestInventory.getContents().clone();

        	    	Inventory playerInventory = Bukkit.createInventory(player, inventorySize, capitalize(chestInventory.getType().name()));
        	        playerInventory.setContents(newInventory);

        			player.openInventory(playerInventory);
        			break;

        		case DISPENSER:
        		case DROPPER:
        		case HOPPER:
        		case HOPPER_MINECART:
        			//Dispenser dispenser = (Dispenser) event.getClickedBlock().getState();
        	        //Inventory newDispenserInventory = Bukkit.createInventory(player, InventoryType.DISPENSER);
        	        //newDispenserInventory.setContents(dispenser.getInventory().getContents().clone());
        	        //player.openInventory(newDispenserInventory);
        			// no access to dispensers - shift-clicking on virtual dispenser inventories
        			// generates a huge error stacktrace
        			player.sendMessage(ChatColor.RED + "Access Denied");
        			break;
			default:
				throw new Exception("Unexpected material found on interact.");
        	}
        }
    }*/
}
