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

/**
 * /core_take 명령어 핸들러 클래스
 * - red, blue, green, all 중 하나를 인자로 받아 해당 팀의 코어 배너 아이템을 지급합니다.
 * - TabCompleter 인터페이스도 구현하여 자동완성 기능도 제공합니다.
 */
public class CoreTakeCommand implements CommandExecutor, TabCompleter {

    // 자동완성 및 유효성 검사용 인자 목록
    private static final List<String> OPTIONS = Arrays.asList("red", "blue", "green", "all");

    /**
     * 주어진 색상의 팀 배너 아이템을 생성합니다.
     *
     * @param material 배너의 색(Material)
     * @param color    배너 이름의 글자 색상
     * @param name     배너 이름
     * @return 설정된 ItemStack (배너)
     */
    private ItemStack createBanner(Material material, NamedTextColor color, String name) {
        ItemStack banner = new ItemStack(material);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(name)
                    .color(color)
                    .decoration(TextDecoration.ITALIC, false));
            banner.setItemMeta(meta);
        }

        return banner;
    }

    /**
     * /core_take 명령어가 실행되었을 때 호출됨
     *
     * @param sender  명령어를 실행한 주체
     * @param command 명령어 객체
     * @param label   명령어 라벨
     * @param args    명령어 인자 배열
     * @return true: 성공적으로 처리됨, false: 실패
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String @NotNull [] args) {
        // 플레이어만 실행 가능
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("플레이어만 사용할 수 있습니다.")
                    .color(NamedTextColor.RED));
            return true;
        }

        // 인자 개수가 정확히 1개인지 확인
        if (args.length != 1) {
            player.sendMessage(Component.text("사용법: /core_take <red|blue|green|all>")
                    .color(NamedTextColor.RED));
            return true;
        }

        String choice = args[0].toLowerCase(); // 소문자로 변환하여 비교
        List<ItemStack> itemsToGive = new ArrayList<>();

        // 인자에 따라 적절한 배너 아이템 생성
        switch (choice) {
            case "red" -> itemsToGive.add(createBanner(
                    Material.RED_BANNER, NamedTextColor.RED, "빨간 팀 코어 설정"));
            case "blue" -> itemsToGive.add(createBanner(
                    Material.BLUE_BANNER, NamedTextColor.BLUE, "파란 팀 코어 설정"));
            case "green" -> itemsToGive.add(createBanner(
                    Material.GREEN_BANNER, NamedTextColor.GREEN, "초록 팀 코어 설정"));
            case "all" -> {
                itemsToGive.add(createBanner(Material.RED_BANNER, NamedTextColor.RED, "빨간 팀 코어 설정"));
                itemsToGive.add(createBanner(Material.BLUE_BANNER, NamedTextColor.BLUE, "파란 팀 코어 설정"));
                itemsToGive.add(createBanner(Material.GREEN_BANNER, NamedTextColor.GREEN, "초록 팀 코어 설정"));
            }
            // 유효하지 않은 인자 처리
            default -> {
                player.sendMessage(Component.text("올바른 인자를 입력하세요: red, blue, green, all")
                        .color(NamedTextColor.RED));
                return true;
            }
        }

        // 생성된 배너 아이템을 플레이어 인벤토리에 추가
        player.getInventory().addItem(itemsToGive.toArray(new ItemStack[0]));

        // 지급 완료 메시지 출력
        player.sendMessage(Component.text("팀 코어 설정용 현수막을 지급했습니다.")
                .color(NamedTextColor.GREEN));

        return true;
    }

    /**
     * /core_take 명령어에 대한 자동완성(Tab Complete) 지원
     *
     * @param sender 명령어 실행 주체
     * @param command 명령어 객체
     * @param alias 입력된 명령어 별칭
     * @param args 현재 입력 중인 인자 배열
     * @return 자동완성 목록 (null 허용)
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        // 첫 번째 인자에 대해서만 자동완성 제공
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
