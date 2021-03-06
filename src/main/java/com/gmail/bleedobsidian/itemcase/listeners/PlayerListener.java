/*
 * Copyright (C) 2013 Jesse Prescott <BleedObsidian@gmail.com>
 *
 * ItemCase is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl.html>.
 */
package com.gmail.bleedobsidian.itemcase.listeners;

import com.gmail.bleedobsidian.itemcase.ItemCase;
import com.gmail.bleedobsidian.itemcase.Language;
import com.gmail.bleedobsidian.itemcase.WorldGuard;
import com.gmail.bleedobsidian.itemcase.loggers.PlayerLogger;
import com.gmail.bleedobsidian.itemcase.managers.itemcase.Itemcase;
import com.gmail.bleedobsidian.itemcase.managers.itemcase.ItemcaseType;
import com.gmail.bleedobsidian.itemcase.util.tellraw.JSONChatClickEventType;
import com.gmail.bleedobsidian.itemcase.util.tellraw.JSONChatColor;
import com.gmail.bleedobsidian.itemcase.util.tellraw.JSONChatExtra;
import com.gmail.bleedobsidian.itemcase.util.tellraw.JSONChatFormat;
import com.gmail.bleedobsidian.itemcase.util.tellraw.JSONChatHoverEventType;
import com.gmail.bleedobsidian.itemcase.util.tellraw.JSONChatMessage;
import java.util.Arrays;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Player related event listener. (Only used internally)
 *
 * @author BleedObsidian (Jesse Prescott)
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!ItemCase.getInstance().getConfigFile().getFileConfiguration()
                .getBoolean("Options.Disable-Sneak-Create")) {
            Player player = event.getPlayer();

            if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event
                    .getAction() == Action.RIGHT_CLICK_AIR)
                    && player.isSneaking()) {
                Block block = event.getClickedBlock();

                if (block == null) {
                    if (player.getTargetBlock(null, 100) != null
                            && player.getTargetBlock(null, 100).getType() != Material.AIR) {
                        block = player.getTargetBlock(null, 100);
                    } else {
                        return;
                    }
                }

                for (int id : ItemCase.getInstance().getConfigFile()
                        .getFileConfiguration().getIntegerList("Blocks")) {
                    if (block.getType().getId() == id) {
                        if (!ItemCase.getInstance().getItemcaseManager().
                                isItemcaseAt(
                                        block.getLocation())) {
                            ItemStack itemStack = player.getItemInHand();

                            if (itemStack.getType() != Material.AIR) {
                                if (player
                                        .hasPermission(
                                                "itemcase.create.showcase")) {
                                    if (WorldGuard.isEnabled()
                                            && !WorldGuard
                                            .getWorldGuardPlugin()
                                            .canBuild(player, block)) {
                                        PlayerLogger
                                                .message(
                                                        player,
                                                        Language.
                                                        getLanguageFile()
                                                        .getMessage(
                                                                "Player.ItemCase.Created-Region"));
                                        event.setCancelled(true);
                                        return;
                                    }

                                    Location location = block.getLocation();

                                    ItemStack itemStackCopy = itemStack.clone();
                                    itemStackCopy.setAmount(1);

                                    ItemCase.getInstance().getItemcaseManager()
                                            .createItemcase(itemStackCopy,
                                                    location, player);

                                    PlayerLogger
                                            .message(
                                                    player,
                                                    Language.getLanguageFile()
                                                    .getMessage(
                                                            "Player.ItemCase.Created"));
                                    event.setCancelled(true);
                                } else {
                                    return;
                                }
                            }
                        }
                    }
                }
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK
                    || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (ItemCase.getInstance().getItemcaseManager().isItemcaseAt(
                        event.getClickedBlock().getLocation())) {
                    if (ItemCase.getInstance().getSelectionManager().
                            isPendingSelection(
                                    player)) {
                        ItemCase.getInstance().getSelectionManager().call(
                                player,
                                ItemCase.getInstance().getItemcaseManager().
                                getItemcaseAt(
                                        event.getClickedBlock().getLocation()));
                        event.setCancelled(true);
                    } else {
                        if (!((ItemCase.getInstance()
                                .getItemcaseManager()
                                .getItemcaseAt(
                                        event.getClickedBlock().getLocation())
                                .getOwnerName()
                                .equals(event.getPlayer().getName()) || player
                                .hasPermission("itemcase.destroy.other")) && event.
                                getAction() == Action.LEFT_CLICK_BLOCK)) {
                            if (ItemCase.getInstance()
                                    .getItemcaseManager()
                                    .getItemcaseAt(
                                            event.getClickedBlock()
                                            .getLocation()).getType() == ItemcaseType.SHOP) {
                                if (!ItemCase.getInstance().getShopManager()
                                        .isPendingOrder(player)) {
                                    ItemCase.getInstance()
                                            .getShopManager()
                                            .addPendingOrder(
                                                    ItemCase.getInstance()
                                                    .getItemcaseManager()
                                                    .getItemcaseAt(
                                                            event.
                                                            getClickedBlock()
                                                            .getLocation()),
                                                    player);
                                } else {
                                    PlayerLogger
                                            .message(
                                                    player,
                                                    Language.getLanguageFile()
                                                    .getMessage(
                                                            "Player.ItemCase.Shop-Order-Processing1"));

                                    JSONChatMessage messageCancel = new JSONChatMessage();
                                    messageCancel.addText("[ItemCase]: ",
                                            JSONChatColor.BLUE, null);

                                    JSONChatExtra extraCancel = new JSONChatExtra(
                                            Language.getLanguageFile()
                                            .getMessage(
                                                    "Player.ItemCase.Cancel-Order-Button"),
                                            JSONChatColor.GOLD,
                                            Arrays.asList(JSONChatFormat.BOLD));
                                    extraCancel
                                            .setHoverEvent(
                                                    JSONChatHoverEventType.SHOW_TEXT,
                                                    Language.getLanguageFile()
                                                    .getMessage(
                                                            "Player.ItemCase.Cancel-Order-Button-Hover"));
                                    extraCancel.setClickEvent(
                                            JSONChatClickEventType.RUN_COMMAND,
                                            "/ic order cancel");

                                    messageCancel.addExtra(extraCancel);
                                    messageCancel.sendToPlayer(player);

                                    PlayerLogger
                                            .message(
                                                    player,
                                                    Language.getLanguageFile()
                                                    .getMessage(
                                                            "Player.Order.Amount-End"));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.getItem().hasMetadata("ItemCase")) {
            event.setCancelled(true);
        } else { // Fail safe
            for (Itemcase itemcase : ItemCase.getInstance().getItemcaseManager()
                    .getItemcases()) {
                if (event.getItem().equals(itemcase.getItem())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer() != null) {
            if (ItemCase.getInstance().getAmountManager().isPendingAmount(
                    event.getPlayer())) {
                event.setCancelled(true);
                try {
                    int amount = Integer.parseInt(event.getMessage());
                    ItemCase.getInstance().getAmountManager().setPendingAmount(
                            event.getPlayer(), amount);
                } catch (NumberFormatException e) {
                    ItemCase.getInstance().getAmountManager().
                            removePendingAmount(
                                    event.getPlayer());
                    PlayerLogger.message(
                            event.getPlayer(),
                            Language.getLanguageFile().getMessage(
                                    "Player.Order.Amount-Error1"));
                    PlayerLogger.message(
                            event.getPlayer(),
                            Language.getLanguageFile().getMessage(
                                    "Player.Order.Amount-Error2"));
                    PlayerLogger.message(
                            event.getPlayer(),
                            Language.getLanguageFile().getMessage(
                                    "Player.Order.Amount-End"));
                }
            } else if (ItemCase.getInstance().getShopManager().isPendingOrder(
                    event.getPlayer())) {
                event.setCancelled(true);
            }

            Set<Player> playersOrdering = ItemCase.getInstance().
                    getShopManager().getOrders()
                    .keySet();

            for (Player player : playersOrdering) {
                event.getRecipients().remove(player);
            }
        }
    }
}
