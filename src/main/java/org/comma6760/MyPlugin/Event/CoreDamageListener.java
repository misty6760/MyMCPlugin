package org.comma6760.MyPlugin.Event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.comma6760.MyPlugin.Utils.CoreHealthManager;

public class CoreDamageListener implements Listener {

    @EventHandler
    public void onCoreHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Interaction interaction)) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!CoreHealthManager.isCore(interaction)) return;

        event.setCancelled(true);

        int newHealth = CoreHealthManager.damageCore(interaction);
        player.sendActionBar(Component.text("코어 체력: " + newHealth, NamedTextColor.RED));

        if (newHealth <= 0) {
            CoreHealthManager.destroyCore(interaction);
        }
    }
}
