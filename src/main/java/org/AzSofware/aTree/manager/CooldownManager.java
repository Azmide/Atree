package org.AzSofware.aTree.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Per-player cooldowns for the TreeCapitator. */
public class CooldownManager {

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public boolean isOnCooldown(UUID uuid, double seconds) {
        return getRemainingSeconds(uuid, seconds) > 0;
    }

    /** Remaining cooldown in seconds, 0 when none. */
    public double getRemainingSeconds(UUID uuid, double seconds) {
        Long last = cooldowns.get(uuid);
        if (last == null) return 0;
        double remaining = (seconds * 1000.0 - (System.currentTimeMillis() - last)) / 1000.0;
        return Math.max(0, remaining);
    }

    public void applyCooldown(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    public void remove(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public void clear() {
        cooldowns.clear();
    }
}
