package com.martinambrus.wgchrf;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
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

    		if (refills != null && refills == true) {
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

		Bukkit.getLogger().info(holder.toString());

		if (holder instanceof Chest) {
			location = ((Chest) holder).getBlock().getLocation();
		} else if (holder instanceof DoubleChest) {
			location = ((DoubleChest) holder).getLocation();
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
		} else if (holder instanceof DoubleChest) {
			location = ((DoubleChest) holder).getLocation();
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
}
