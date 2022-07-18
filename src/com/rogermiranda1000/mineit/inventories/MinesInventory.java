package com.rogermiranda1000.mineit.inventories;

import com.rogermiranda1000.helper.BasicInventory;
import com.rogermiranda1000.mineit.Mine;
import com.rogermiranda1000.mineit.MineIt;
import com.rogermiranda1000.mineit.MinesChangedEvent;
import com.rogermiranda1000.mineit.Stage;
import com.rogermiranda1000.mineit.blocks.Mines;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

abstract public class MinesInventory extends BasicInventory implements MinesChangedEvent, CreateMinesInventory {
    private static final String INVENTORY_NAME = "§cEdit mine";
    private static final int MAX_MINES_PER_INV = 45;
    private final ItemStack back;
    @Nullable private ItemStack next_item, pre_item;

    /**
     * editMineInventory's offset
     */
    private final int offset;
    protected final HashMap<Mine,BasicInventory> editMineInventory;
    private MinesInventory next, pre;

    @SuppressWarnings("ConstantConditions")
    public MinesInventory(HashMap<Mine,BasicInventory> minesInventories, int offset, MinesInventory pre) {
        super(MineIt.instance, true);

        this.offset = offset;
        this.editMineInventory = minesInventories;
        this.setPre(pre);
        this.setNext(null);

        this.back = new ItemStack(Material.ANVIL);
        ItemMeta m = this.back.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "Go back");
        this.back.setItemMeta(m);

        this.onMinesChanged(); // create the inventory for the first time
    }

    public MinesInventory() {
        this(new HashMap<>(), 0, null);

        Mines.getInstance().addMinesListener(this); // only the first menu will listen, the other ones will be called from the first
    }

    public Collection<BasicInventory> getMinesInventories() {
        return this.editMineInventory.values();
    }

    public void inventoryClickedEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        // permisions
        if(!player.hasPermission("mineit.open")) {
            player.closeInventory();
            player.sendMessage(MineIt.instance.errorPrefix + "You can't use this menu.");
            return;
        }

        ItemStack clicked = e.getCurrentItem();
        if(clicked == null || clicked.getItemMeta() == null) return;

        if (clicked.equals(this.back)) MineIt.instance.mainInventory.openInventory(player);
        else if (clicked.equals(this.next_item)) this.next.openInventory(player);
        else if (clicked.equals(this.pre_item)) this.pre.openInventory(player);
        else {
            final String clickedMineName = clicked.getItemMeta().getDisplayName();
            Mine m = this.editMineInventory.keySet().stream().filter(mine -> mine.getName().equals(clickedMineName)).findFirst().orElse(null);
            if (m != null) this.mineClicked(e, m);
        }
    }

    abstract void mineClicked(InventoryClickEvent e, Mine m);

    @Nullable
    public BasicInventory searchMine(String title) {
        for (Map.Entry<Mine,BasicInventory> m : this.editMineInventory.entrySet()) {
            if (m.getKey().getName().equalsIgnoreCase(title)) return m.getValue();
        }
        return null; // not found
    }

    private void setNext(MinesInventory next) {
        if (next == null) {
            if (this.next != null) this.next.closeInventories();

            this.next_item = null;
        }
        else {
            next.registerEvent();

            this.next_item = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta m = this.next_item.getItemMeta();
            m.setDisplayName(ChatColor.GREEN + "->");
            this.next_item.setItemMeta(m);
        }

        this.next = next;
    }

    private void setPre(MinesInventory pre) {
        this.pre = pre;
        if (pre == null) this.pre_item = null;
        else {
            this.pre_item = new ItemStack(Material.IRON_BLOCK);
            ItemMeta m = this.pre_item.getItemMeta();
            m.setDisplayName(ChatColor.GREEN + "<-");
            this.pre_item.setItemMeta(m);
        }
    }

    @Override
    public void registerEvent() {
        super.registerEvent();
        if (this.next != null) this.next.registerEvent();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onMinesChanged() {
        Inventory newInventory;

        if (this.next == null) {
            if (this.editMineInventory.size() - this.offset > MinesInventory.MAX_MINES_PER_INV) this.setNext(this.create(this.editMineInventory, this.offset + MinesInventory.MAX_MINES_PER_INV, this)); // we need more
        }
        else {
            if (this.editMineInventory.size() - this.offset <= MinesInventory.MAX_MINES_PER_INV) this.setNext(null); // we don't need it anymore
            else this.next.onMinesChanged();
        }

        int l = (int)Math.ceil((Mines.getInstance().getDifferentValuesNum()-this.offset)/9.0);
        if(l==0) {
            newInventory = Bukkit.createInventory(null, 18, MinesInventory.INVENTORY_NAME);

            // fill the first row with "null" mines
            ItemStack none = new ItemStack(Material.COBBLESTONE);
            ItemMeta m = none.getItemMeta();
            m.setDisplayName("-");
            none.setItemMeta(m);
            for(int x=0; x<9; x++) newInventory.setItem(x, none);

            newInventory.setItem(9, this.back);
        }
        else {
            l++; // the last row is for the back button
            if (l > 6) l = 6; // just show the first mines

            newInventory = Bukkit.createInventory(null, l * 9, MinesInventory.INVENTORY_NAME);
            int pos = 0, backPos = (l - 1) * 9;
            for (Mine mine : MinesInventory.getListWithOffset(new ArrayList<>(Mines.getInstance().getAllValues()), this.offset, MinesInventory.MAX_MINES_PER_INV)) {
                ItemStack mina = new ItemStack(Mine.SELECT_BLOCK); // TODO mine block
                ItemMeta meta = mina.getItemMeta();
                meta.setDisplayName(mine.getName());
                ArrayList<String> lore = new ArrayList<>();
                for (Stage s : mine.getStages()) lore.add(s.toString());
                meta.setLore(lore);
                mina.setItemMeta(meta);

                newInventory.setItem(pos++, mina);
                //if (pos == backPos) break;
            }

            newInventory.setItem(backPos, this.back);

            if (this.pre_item != null) newInventory.setItem(backPos+7, this.pre_item);
            if (this.next_item != null) newInventory.setItem(backPos+8, this.next_item);
        }

        if (this.getInventory() != null) this.newInventory(newInventory); // only if it's not the first time
        this.setInventory(newInventory);
    }

    private static <T> List<T> getListWithOffset(List<T> list, int offset, int maxLenght) {
        return list.subList(offset, Math.min(list.size(), offset+maxLenght));
    }

    @Override
    public void onMineRemoved(Mine m) {
        this.editMineInventory.remove(m);
        this.onMinesChanged();
    }

    @Override
    public void onMineAdded(Mine m) {
        this.editMineInventory.put(m, new EditMineInventory(m));
        this.onMinesChanged();
    }
}
