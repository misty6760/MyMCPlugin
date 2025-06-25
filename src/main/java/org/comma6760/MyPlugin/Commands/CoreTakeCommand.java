package org.comma6760.MyPlugin.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoreTakeCommand implements CommandExecutor, TabCompleter {

    private static final List<String> OPTIONS = Arrays.asList("red", "blue", "green", "all");

    private ItemStack createBanner(Material material, NamedTextColor color, String name) {
        ItemStack banner = new ItemStack(material);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name).color(color).decoration(TextDecoration.ITALIC, false));
            banner.setItemMeta(meta);
        }
        return banner;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("플레이어만 사용할 수 있습니다.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(Component.text("사용법: /core_take <red|blue|green|all>").color(NamedTextColor.RED));
            return true;
        }

        String choice = args[0].toLowerCase();
        List<ItemStack> itemsToGive = new ArrayList<>();

        switch (choice) {
            case "red" -> itemsToGive.add(createBanner(Material.RED_BANNER, NamedTextColor.RED, "빨간 팀 코어 설정"));
            case "blue" -> itemsToGive.add(createBanner(Material.BLUE_BANNER, NamedTextColor.BLUE, "파란 팀 코어 설정"));
            case "green" -> itemsToGive.add(createBanner(Material.GREEN_BANNER, NamedTextColor.GREEN, "초록 팀 코어 설정"));
            case "all" -> {
                itemsToGive.add(createBanner(Material.RED_BANNER, NamedTextColor.RED, "빨간 팀 코어 설정"));
                itemsToGive.add(createBanner(Material.BLUE_BANNER, NamedTextColor.BLUE, "파란 팀 코어 설정"));
                itemsToGive.add(createBanner(Material.GREEN_BANNER, NamedTextColor.GREEN, "초록 팀 코어 설정"));
            }
            default -> {
                player.sendMessage(Component.text("올바른 인자를 입력하세요: red, blue, green, all").color(NamedTextColor.RED));
                return true;
            }
        }

        player.getInventory().addItem(itemsToGive.toArray(new ItemStack[0]));
        player.sendMessage(Component.text("팀 코어 설정용 현수막을 지급했습니다.").color(NamedTextColor.GREEN));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String currentArg = args[0].toLowerCase();
            for (String option : OPTIONS) {
                if (option.startsWith(currentArg)) {
                    completions.add(option);
                }
            }
        }
        return completions;
    }
}
