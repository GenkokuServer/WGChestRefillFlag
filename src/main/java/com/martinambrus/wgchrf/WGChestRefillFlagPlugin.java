/*
 * Copyright (C) 2013 mewin<mewin001@hotmail.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.martinambrus.wgchrf;

import java.io.IOException;
import java.util.logging.Level;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.BooleanFlag;


public class WGChestRefillFlagPlugin extends JavaPlugin {
    public BooleanFlag CHESTSAUTOFILL_FLAG;

    @Override
    public void onEnable() {
        if (!this.getWorldGuard()) {
            this.getLogger().log(Level.SEVERE, "Could not find WorldGuard.");
            this.getPluginLoader().disablePlugin(this);
            return;
        } else {
            this.getLogger().log(Level.INFO, "Hooked into WorldGuard.");
        }

        PlayerListener listener = new PlayerListener(this);
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            BooleanFlag flag = new BooleanFlag("chest-auto-refill");
            registry.register(flag);
            CHESTSAUTOFILL_FLAG = flag; // only set our field if there was no error
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("chest-auto-refill");
            if (existing instanceof BooleanFlag) {
                CHESTSAUTOFILL_FLAG = (BooleanFlag) existing;
            } else {
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
            }
        }
    }

    private boolean getWorldGuard() {
        Plugin plug = this.getServer().getPluginManager().getPlugin("WorldGuard");
        return plug instanceof WorldGuardPlugin;
    }
}