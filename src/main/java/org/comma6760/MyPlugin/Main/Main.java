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

import org.comma6760.MyPlugin.Event.ChestDetectRightClickListener;
import org.comma6760.MyPlugin.Event.Cores.CoreDamageListener;
import org.comma6760.MyPlugin.Event.Cores.CorePlaceListener;
import org.comma6760.MyPlugin.Commands.GetTeamSelectChestCommand;
import org.comma6760.MyPlugin.Commands.CoreTakeCommand;

import java.util.Objects;

public final class Main extends JavaPlugin implements Listener {

    // 플러그인의 전역 인스턴스를 저장하는 필드.
    // 다른 클래스에서 플러그인 인스턴스에 쉽게 접근하기 위함.
    private static Main instance;

    /**
     * 플러그인 활성화 시 호출되는 메서드.
     * 서버가 플러그인을 로딩하고 준비하는 단계에서 실행됨.
     */
    @Override
    public void onEnable() {
        // 싱글톤 패턴을 위해 자기 자신을 인스턴스 필드에 저장
        instance = this;

        // 이벤트 리스너 등록
        // ChestDetectRightClickListener: 가상 인벤토리 등 우클릭 관련 이벤트 처리
        getServer().getPluginManager().registerEvents(new ChestDetectRightClickListener(), this);
        // 본인 클래스도 이벤트 리스너로 등록 (플레이어 접속, 채팅 이벤트 처리)
        getServer().getPluginManager().registerEvents(this, this);
        // 코어 설치 이벤트 처리 리스너 등록
        getServer().getPluginManager().registerEvents(new CorePlaceListener(), this);
        // 코어 피해 이벤트 처리 리스너 등록
        getServer().getPluginManager().registerEvents(new CoreDamageListener(), this);

        // 스코어보드 팀이 존재하지 않으면 새로 생성 후, 색상과 접두사(prefix)를 설정
        // 팀 ID는 "red", "blue", "green" 으로 고정
        createTeamIfNotExist("red", "빨강", NamedTextColor.RED);
        createTeamIfNotExist("blue", "파랑", NamedTextColor.BLUE);
        createTeamIfNotExist("green", "초록", NamedTextColor.GREEN);

        // 커맨드 등록
        // "/select_team" 명령어를 처리할 커맨드 실행자 등록
        Objects.requireNonNull(getCommand("select_team")).setExecutor(new GetTeamSelectChestCommand());
        // "/core_take" 명령어를 처리할 커맨드 실행자 등록
        Objects.requireNonNull(getCommand("core_take")).setExecutor(new CoreTakeCommand());

        // 플러그인이 정상적으로 활성화되었음을 콘솔에 출력
        Bukkit.getConsoleSender().sendMessage(
                Component.text("[ 플러그인이 활성화 되었습니다. ]").color(NamedTextColor.GREEN)
        );
    }

    /**
     * 플러그인 비활성화 시 호출되는 메서드.
     * 서버가 플러그인을 언로드하거나 종료할 때 실행됨.
     */
    @Override
    public void onDisable() {
        // 플러그인이 비활성화됨을 콘솔에 알림
        Bukkit.getConsoleSender().sendMessage(
                Component.text("[ 플러그인이 비활성화 되었습니다. ]").color(NamedTextColor.RED)
        );
    }

    /**
     * 플러그인의 전역 인스턴스를 반환하는 메서드.
     * 다른 클래스에서 플러그인 인스턴스에 접근할 때 사용.
     * @return Main 플러그인 인스턴스
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * 스코어보드에서 팀이 존재하는지 확인하고 없으면 새로 생성.
     * 생성 시 팀 접두사(prefix)와 팀 이름 색상을 설정한다.
     *
     * @param teamId 스코어보드 내 팀 식별자 (예: "red", "blue", "green")
     * @param prefixText 플레이어 이름 앞에 붙일 접두사 텍스트 (예: "빨강")
     * @param color 접두사 및 팀 이름에 사용할 색상 (NamedTextColor)
     */
    private void createTeamIfNotExist(String teamId, String prefixText, NamedTextColor color) {
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        var team = scoreboard.getTeam(teamId);

        // 팀이 이미 존재하는지 확인
        if (team == null) {
            // 팀이 없으면 새 팀 생성
            team = scoreboard.registerNewTeam(teamId);
        }

        // 팀 컬러 및 접두사 설정
        team.color(color);
        team.prefix(Component.text("[" + prefixText + "] ").color(color));

        // 콘솔에 팀 접두사 설정 완료 메시지 출력
        Bukkit.getConsoleSender().sendMessage(
                Component.text("팀 '" + teamId + "' prefix 설정 완료").color(color)
        );
    }

    /**
     * 플레이어가 서버에 접속했을 때 호출되는 이벤트 핸들러.
     * 접속 메시지를 서버 채팅에 보여준다.
     *
     * @param event PlayerJoinEvent 이벤트 객체
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        // 접속 메시지를 노란색으로 포맷팅하여 서버에 보여줌
        event.joinMessage(Component.text(p.getName() + "님이 서버에 접속했어요!").color(NamedTextColor.YELLOW));
    }

    /**
     * 플레이어가 채팅할 때 호출되는 비동기 이벤트 핸들러.
     * 플레이어가 속한 팀의 접두사를 채팅 메시지 앞에 붙여서 보여준다.
     *
     * @param event AsyncChatEvent 이벤트 객체
     */
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Component prefix = Component.empty();

        // 플레이어가 속한 팀을 찾아 접두사를 가져옴
        for (var team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                prefix = team.prefix();
                break;  // 첫 번째로 찾은 팀 접두사를 사용
            }
        }

        // 접두사 + 플레이어 이름 + 메시지 형식으로 채팅 메시지 구성
        Component formatted = prefix.append(Component.text(player.getName()).color(NamedTextColor.WHITE))
                .append(Component.text(": ").color(NamedTextColor.GRAY))
                .append(event.message().color(NamedTextColor.WHITE));

        // 각 플레이어에게 이 형식으로 채팅 메시지를 렌더링하도록 설정
        event.renderer((source, displayName, message, viewer) -> formatted);
    }
}
