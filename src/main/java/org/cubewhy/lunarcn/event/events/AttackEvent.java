/**
 * 攻击事件
 * */
package org.cubewhy.lunarcn.event.events;

import net.minecraft.entity.Entity;
import org.cubewhy.lunarcn.event.Event;

public class AttackEvent extends Event {
    private final Entity target;

    public AttackEvent(Entity target) {
        this.target = target;
    }

    /**
     * Get attack target
     * @return target
     * */
    public Entity getTarget() {
        return target;
    }
}
