package com.martinambrus.wgchrf;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerListener implements Listener {

    private final WGChestRefillFlagPlugin plugin;
    private final Map<Location, ItemStack[]> originalContents = new HashMap<>();

    public PlayerListener(WGChestRefillFlagPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDispenserDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (block.getType().equals(Material.DISPENSER)) {
            Boolean refills = query.queryValue(BukkitAdapter.adapt(block.getLocation()), null, this.plugin.CHESTSAUTOFILL_FLAG);
            if (refills != null && refills) {
                // re-add the same item that was just dispensed by a auto-refillable dispenser
                ((Dispenser) event.getBlock().getState()).getInventory().addItem(event.getItem());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        // check if we're on a protected ground
        HumanEntity player = event.getPlayer();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        Boolean refills = query.queryValue(BukkitAdapter.adapt(player.getLocation()), null, this.plugin.CHESTSAUTOFILL_FLAG);
        if (refills == null || !refills) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        Location location;

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        // check if we're on a protected ground
        HumanEntity player = event.getPlayer();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        Boolean refills = query.queryValue(BukkitAdapter.adapt(player.getLocation()), null, this.plugin.CHESTSAUTOFILL_FLAG);
        if (refills == null || !refills) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        Location location;

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
        this.originalContents.remove(event.getBlock().getLocation());
    }
}
