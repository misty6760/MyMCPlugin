package org.comma6760.MyPlugin.Event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public class ChestDetectRightClickListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() == Material.CHEST) {
                ItemMeta meta = item.getItemMeta();

                if (meta != null && meta.hasDisplayName() && meta.hasLore()) {
                    Component displayName = meta.displayName();
                    Component expectedName = Component.text("팀 선택하기")
                            .color(NamedTextColor.GOLD)
                            .decoration(TextDecoration.ITALIC, false)
                            .decoration(TextDecoration.BOLD, true);

                    List<Component> lore = meta.lore();
                    Component expectedLoreLine = Component.text("팀을 선택하려면 우클릭을 하세요")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false);

                    if (displayName != null && displayName.equals(expectedName)
                            && lore != null && !lore.isEmpty()
                            && lore.getFirst().equals(expectedLoreLine)) {

                        event.setCancelled(true);

                        Inventory virtualChest = Bukkit.createInventory(
                                null,
                                27,
                                Component.text("팀 선택하기").color(NamedTextColor.GOLD)
                        );

                        ItemStack redDye = new ItemStack(Material.RED_DYE);
                        ItemMeta redMeta = redDye.getItemMeta();
                        redMeta.displayName(
                                Component.text("빨간 팀")
                                        .color(NamedTextColor.RED)
                                        .decoration(TextDecoration.ITALIC, false)
                        );
                        redDye.setItemMeta(redMeta);

                        ItemStack blueDye = new ItemStack(Material.BLUE_DYE);
                        ItemMeta blueMeta = blueDye.getItemMeta();
                        blueMeta.displayName(
                                Component.text("파란 팀")
                                        .color(NamedTextColor.BLUE)
                                        .decoration(TextDecoration.ITALIC, false)
                        );
                        blueDye.setItemMeta(blueMeta);

                        ItemStack greenDye = new ItemStack(Material.GREEN_DYE);
                        ItemMeta greenMeta = greenDye.getItemMeta();
                        greenMeta.displayName(
                                Component.text("초록 팀")
                                        .color(NamedTextColor.GREEN)
                                        .decoration(TextDecoration.ITALIC, false)
                        );
                        greenDye.setItemMeta(greenMeta);

                        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                        ItemMeta blackMeta = blackPane.getItemMeta();

                        // 이름, 설명 완전히 제거
                        blackMeta.displayName(Component.empty());
                        blackMeta.lore(Collections.emptyList());

                        blackMeta.addItemFlags(
                                ItemFlag.HIDE_ATTRIBUTES,
                                ItemFlag.HIDE_ENCHANTS,
                                ItemFlag.HIDE_UNBREAKABLE
                        );

                        blackPane.setItemMeta(blackMeta);

                        for (int i = 0; i < virtualChest.getSize(); i++) {
                            if (i != 11 && i != 13 && i != 15) {
                                virtualChest.setItem(i, blackPane);
                            }
                        }

                        virtualChest.setItem(11, redDye);
                        virtualChest.setItem(13, blueDye);
                        virtualChest.setItem(15, greenDye);

                        player.openInventory(virtualChest);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text("팀 선택하기").color(NamedTextColor.GOLD))) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        Material type = clicked.getType();

        if (type == Material.BLACK_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            return;
        }

        String targetTeam;
        NamedTextColor msgColor;
        String joinMsg;

        if (type == Material.RED_DYE) {
            targetTeam = "red";
            msgColor = NamedTextColor.RED;
            joinMsg = "빨간 팀에 참가했습니다!";
        } else if (type == Material.BLUE_DYE) {
            targetTeam = "blue";
            msgColor = NamedTextColor.BLUE;
            joinMsg = "파란 팀에 참가했습니다!";
        } else if (type == Material.GREEN_DYE) {
            targetTeam = "green";
            msgColor = NamedTextColor.GREEN;
            joinMsg = "초록 팀에 참가했습니다!";
        } else {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        player.closeInventory();

        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (var team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }

        player.performCommand("team join " + targetTeam);
        player.sendMessage(Component.text(joinMsg).color(msgColor));
    }
}
