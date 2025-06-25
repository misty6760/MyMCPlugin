package org.comma6760.MyPlugin.Main;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

// 이벤트 및 명령어 클래스
import org.comma6760.MyPlugin.Event.ChestDetectRightClickListener;
import org.comma6760.MyPlugin.Event.CoreDamageListener;
import org.comma6760.MyPlugin.Event.CorePlaceListener;
import org.comma6760.MyPlugin.Commands.GetTeamSelectChestCommand;
import org.comma6760.MyPlugin.Commands.CoreTakeCommand;

import java.util.Objects;

public final class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // 상호작용 및 설치 이벤트 등록
        getServer().getPluginManager().registerEvents(new ChestDetectRightClickListener(), this);
        getServer().getPluginManager().registerEvents(this, this); // 메인 클래스에서 직접 처리하는 이벤트
        getServer().getPluginManager().registerEvents(new CorePlaceListener(), this);
        getServer().getPluginManager().registerEvents(new CoreDamageListener(), this);

        // 팀 생성 및 접두사 설정
        createTeamIfNotExist("red", "빨강", NamedTextColor.RED);
        createTeamIfNotExist("blue", "파랑", NamedTextColor.BLUE);
        createTeamIfNotExist("green", "초록", NamedTextColor.GREEN);

        // 명령어 등록
        Objects.requireNonNull(getCommand("select_team")).setExecutor(new GetTeamSelectChestCommand());
        Objects.requireNonNull(getCommand("core_take")).setExecutor(new CoreTakeCommand());

        // 콘솔 메시지
        Bukkit.getConsoleSender().sendMessage(
                Component.text("[ 플러그인이 활성화 되었습니다. ]").color(NamedTextColor.GREEN)
        );
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(
                Component.text("[ 플러그인이 비활성화 되었습니다. ]").color(NamedTextColor.RED)
        );
    }

    /**
     * 팀이 없으면 생성하고 색상 및 접두사를 설정함.
     */
    private void createTeamIfNotExist(String teamId, String prefixText, NamedTextColor color) {
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        var team = scoreboard.getTeam(teamId);

        if (team == null) {
            team = scoreboard.registerNewTeam(teamId);
        }

        team.color(color);
        team.prefix(Component.text("[" + prefixText + "] ").color(color));

        Bukkit.getConsoleSender().sendMessage(
                Component.text("팀 '" + teamId + "' prefix 설정 완료").color(color)
        );
    }

    /**
     * 플레이어가 서버에 접속하면 노란색 메시지로 환영
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        event.joinMessage(Component.text(p.getName() + "님이 서버에 접속했어요!").color(NamedTextColor.YELLOW));
    }

    /**
     * 채팅 시 팀 접두사를 붙여서 메시지 출력
     */
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Component prefix = Component.empty();
        for (var team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                prefix = team.prefix();
                break;
            }
        }

        Component formatted = prefix.append(Component.text(player.getName()).color(NamedTextColor.WHITE))
                .append(Component.text(": ").color(NamedTextColor.GRAY))
                .append(event.message().color(NamedTextColor.WHITE));

        event.renderer((source, displayName, message, viewer) -> formatted);
    }
}
