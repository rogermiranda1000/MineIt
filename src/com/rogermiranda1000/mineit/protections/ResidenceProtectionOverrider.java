package com.rogermiranda1000.mineit.protections;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.rogermiranda1000.mineit.MineIt;
import org.bukkit.Bukkit;

public class ResidenceProtectionOverrider implements ProtectionOverrider {
    @Override
    public void overrideProtection(org.bukkit.event.block.BlockBreakEvent event) {
        ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if (res != null) {
            if (res.getPermissions().playerHas(event.getPlayer(), Flags.destroy, FlagPermissions.FlagCombo.FalseOrNone)) {
                // the block breaked is a residence, and the player don't have the permissions
                res.getPermissions().setPlayerFlag(event.getPlayer().getName(), "destroy", FlagPermissions.FlagState.TRUE);
                Bukkit.getScheduler().runTaskLater(MineIt.instance,()->res.getPermissions().setPlayerFlag(event.getPlayer().getName(), "destroy", FlagPermissions.FlagState.FALSE), 1); // undo
            }
        }
    }
}
