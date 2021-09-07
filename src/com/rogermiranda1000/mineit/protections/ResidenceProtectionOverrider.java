package com.rogermiranda1000.mineit.protections;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.rogermiranda1000.mineit.MineIt;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class ResidenceProtectionOverrider implements ProtectionOverrider {
    @Override
    public Object getProtection(BlockBreakEvent event) {
        ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if (res == null) return null;

        // TODO canBreakBlock(Player var0, Block var1, boolean var2)
        if (res.getPermissions().playerHas(event.getPlayer(), Flags.destroy, FlagPermissions.FlagCombo.OnlyTrue)) return null;

        // the block breaked is a residence, and the player don't have the permissions
        return res;
    }

    @Override
    public void overrideProtection(@NotNull Object region, Player player) {
        ResidencePermissions perm = ((ClaimedResidence)region).getPermissions();
        perm.setPlayerFlag(player.getName(), "destroy", FlagPermissions.FlagState.TRUE);
        Bukkit.getScheduler().runTaskLater(MineIt.instance,()->perm.setPlayerFlag(player.getName(), "destroy", FlagPermissions.FlagState.FALSE), 1);
    }
}
