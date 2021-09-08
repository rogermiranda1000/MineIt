package com.rogermiranda1000.versioncontroller;

import com.rogermiranda1000.versioncontroller.blocks.BlockManager;
import com.rogermiranda1000.versioncontroller.blocks.BlockPost13;
import com.rogermiranda1000.versioncontroller.blocks.BlockPre13;
import com.rogermiranda1000.versioncontroller.entities.EntityManager;
import com.rogermiranda1000.versioncontroller.entities.EntityPaper;
import com.rogermiranda1000.versioncontroller.entities.EntitySpigot;
import com.rogermiranda1000.versioncontroller.items.ItemManager;
import com.rogermiranda1000.versioncontroller.items.ItemPost9;
import com.rogermiranda1000.versioncontroller.items.ItemPre9;
import com.rogermiranda1000.versioncontroller.particles.ParticleManager;
import com.rogermiranda1000.versioncontroller.particles.ParticlePost9;
import com.rogermiranda1000.versioncontroller.particles.ParticlePre9;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Singleton object for cross-version compatibility
 */
public class VersionController extends ItemManager implements BlockManager, ParticleManager, EntityManager {
    private static VersionController versionController = null;
    public static final Version version = VersionController.getVersion();
    public static final boolean isPaper = VersionController.getMCPaper();

    private static final BlockManager blockManager = (VersionController.version.compareTo(Version.MC_1_13) < 0) ? new BlockPre13() : new BlockPost13();
    private static final ItemManager itemManager = (VersionController.version.compareTo(Version.MC_1_9) < 0) ? new ItemPre9() : new ItemPost9();
    private static final ParticleManager particleManager = (VersionController.version.compareTo(Version.MC_1_9) < 0) ? new ParticlePre9() : new ParticlePost9();
    private static final EntityManager entityManager = (VersionController.isPaper) ? new EntityPaper() : new EntitySpigot();

    /**
     * Get the current minecraft version
     * @return version (1.XX)
     */
    private static Version getVersion() {
        // TODO get the full version
        return new Version(1, Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]), 0);
    }

    /**
     * Get if Paper is running (or, by cons, Spigot)
     * https://www.spigotmc.org/threads/how-do-i-detect-if-a-server-is-running-paper.499064/
     * @author Gadse
     * @return Paper (true), Spigot (false)
     */
    private static boolean getMCPaper() {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder"); // a package from paper
            return true;
        } catch (ClassNotFoundException ignored) { }
        return false;
    }

    public static VersionController get() {
        if (VersionController.versionController == null) VersionController.versionController = new VersionController();
        return VersionController.versionController;
    }

    public @Nullable Object getMaterial(String type) {
        return VersionController.blockManager.getMaterial(type);
    }

    public Object getObject(@NotNull Block block) {
        return VersionController.blockManager.getObject(block);
    }

    @Override
    public Object getObject(@NotNull ItemStack item) {
        return VersionController.blockManager.getObject(item);
    }

    public boolean isPassable(@NotNull Block block) {
        return VersionController.blockManager.isPassable(block);
    }

    @Override
    public String getName(@NotNull Object block) {
        return VersionController.blockManager.getName(block);
    }

    @Override
    public void setType(@NotNull Block block, Object type) {
        VersionController.blockManager.setType(block, type);
    }

    @Override
    public ItemStack getItemStack(Object type) {
        return VersionController.blockManager.getItemStack(type);
    }

    /**
     * It generates a copy of an ItemStack as default (only the type)
     * @param item ItemStack to copy
     * @return ItemStack clone
     */
    public ItemStack cloneItemStack(ItemStack item) {
        return getItemStack(getObject(item));
    }

    @Override
    public ItemStack[] getItemInHand(PlayerInventory playerInventory) {
        return VersionController.itemManager.getItemInHand(playerInventory);
    }

    @Override
    public void playParticle(World world, Object particle, Location loc) {
        VersionController.particleManager.playParticle(world, particle, loc);
    }

    @Override
    public void playParticle(Player ply, Object particle, Location loc) {
        VersionController.particleManager.playParticle(ply, particle, loc);
    }

    @Override
    public Object getParticle(String particle) throws IllegalArgumentException {
        return VersionController.particleManager.getParticle(particle);
    }

    @Override
    public @NotNull Vector getVelocity(Entity e) {
        return VersionController.entityManager.getVelocity(e);
    }

    @Override
    public @NotNull Vector getVelocity(PlayerMoveEvent e) {
        return VersionController.entityManager.getVelocity(e);
    }
}
