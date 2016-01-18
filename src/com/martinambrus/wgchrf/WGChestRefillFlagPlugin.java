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

import java.util.logging.Level;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.BooleanFlag;


public class WGChestRefillFlagPlugin extends JavaPlugin
{
    public final BooleanFlag CHESTSAUTOFILL_FLAG = new BooleanFlag("chest-auto-refill");
    private WGCustomFlagsPlugin wgcf;
    private WorldGuardPlugin wgp;
    private PlayerListener listener;

    @Override
    public void onEnable()
    {
        if (!this.getWGCF())
        {
        	this.getLogger().log(Level.SEVERE, "Could not find WGCustomFlags.");
        	this.getPluginLoader().disablePlugin(this);
            return;
        }
        else
        {
        	this.getLogger().log(Level.INFO, "Hooked into WGCustomFlags.");
        }

        if (!this.getWorldGuard())
        {
        	this.getLogger().log(Level.SEVERE, "Could not find WorldGuard.");
        	this.getPluginLoader().disablePlugin(this);
            return;
        }
        else
        {
        	this.getLogger().log(Level.INFO, "Hooked into WorldGuard.");
        }

        this.listener = new PlayerListener(this, this.wgp);
        this.getServer().getPluginManager().registerEvents(this.listener, this);

        this.wgcf.addCustomFlag(this.CHESTSAUTOFILL_FLAG);
    }

    private boolean getWGCF()
    {
        Plugin plug = this.getServer().getPluginManager().getPlugin("WGCustomFlags");
        if (plug == null || !(plug instanceof WGCustomFlagsPlugin))
        {
            return false;
        }
        else
        {
        	this.wgcf = (WGCustomFlagsPlugin) plug;
            return true;
        }
    }

    private boolean getWorldGuard()
    {
        Plugin plug = this.getServer().getPluginManager().getPlugin("WorldGuard");
        if (plug == null || !(plug instanceof WorldGuardPlugin))
        {
            return false;
        }
        else
        {
        	this.wgp = (WorldGuardPlugin) plug;
            return true;
        }
    }
}