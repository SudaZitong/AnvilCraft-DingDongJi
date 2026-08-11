package com.dingdongji.mod.client;

import com.dingdongji.mod.init.ModParticles;
import com.dingdongji.mod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 超限合金靴子蹈虚飞行粒子处理器（复刻飘升机机制）。
 * 服务端通过 IonocraftBootsFlyingPacket 同步"正在蹈虚飞行的玩家"到所有客户端，
 * 客户端在此遍历视野内所有玩家，为每个蹈虚飞行玩家在脚底本地渲染 mod 自己的粒子。
 * 因此所有玩家都能看到，且实现方式与飘升机一致。
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class IonocraftBootsClientHandler {

    /** 服务端同步的正在蹈虚飞行的玩家 entityId 集合 */
    private static final Set<Integer> SYNCED_FLYING_PLAYERS = ConcurrentHashMap.newKeySet();

    /**
     * 由 IonocraftBootsFlyingPacket 在客户端调用，记录服务端同步的蹈虚飞行状态。
     */
    public static void onFlyingSync(int playerId, boolean flying) {
        if (flying) {
            SYNCED_FLYING_PLAYERS.add(playerId);
        } else {
            SYNCED_FLYING_PLAYERS.remove(playerId);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;
        ClientLevel level = mc.level;
        LocalPlayer local = mc.player;
        if (local == null) return;

        // 每2tick喷一次粒子，降低粒子量（更稳定、更少）
        if ((level.getGameTime() & 1) == 1) return;

        // 自己的粒子仅第三人称显示：第一人称不渲染（避免脚下粒子第一人称看不到）
        boolean firstPerson = mc.options.getCameraType() == CameraType.FIRST_PERSON;

        for (Player player : level.players()) {
            if (player.isCreative() || player.isSpectator()) continue;
            if (player == local && firstPerson) continue;

            // 本地玩家：读本地 abilities + 服务端同步的蹈虚状态（双保险），且穿超限靴子；
            // 远程玩家：用服务端同步的精确蹈虚飞行状态
            boolean flying = player == local
                    ? (isWearingTranscendiumBoots(player)
                       && (player.getAbilities().flying || SYNCED_FLYING_PLAYERS.contains(player.getId())))
                    : SYNCED_FLYING_PLAYERS.contains(player.getId());
            if (!flying) continue;

            spawnBootsParticles(level, player, level.random);
        }
    }

    private static boolean isWearingTranscendiumBoots(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        return boots.is(ModItems.TRANSCENDIUM_BOOTS.get());
    }

    private static void spawnBootsParticles(ClientLevel level, Player player, RandomSource random) {
        float yawRad = (float) Math.toRadians(player.yBodyRot);
        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);

        // 粒子前后位移取反（左脚为 -、右脚为 +），使粒子与脚的实际前后摆动方向一致；
        // 脚抬起用 Math.abs(Mth.sin(...)) 让两只脚同步抬起（方块人行走动画）。
        float limbPos = 0f, limbSpeed = 0f;
        try { limbPos = player.walkAnimation.position(); limbSpeed = player.walkAnimation.speed(); } catch(Exception e) {}
        double legSwing = Mth.cos(limbPos * 0.6662F) * 1.4F * limbSpeed;   // 前后交替项
        double footLift = Math.abs(Mth.sin(limbPos * 0.6662F)) * 1.4F * limbSpeed * 0.15; // 两只脚同步抬起
        double side = 0.12;     // 内缩到靴底正下方中心（原0.2，内缩约1.3像素）
        double fwdK = 0.45;     // 脚前后摆动位移系数（格）
        double footY = player.getY() + 0.05;  // 脚底表面基准高度

        // 左右脚各一处：前后取反（左脚 -、右脚 +），抬起两只脚同步（Math.abs）
        double[][] feet = {
            { -side, -legSwing * fwdK, footLift },   // 左脚
            {  side,  legSwing * fwdK, footLift }    // 右脚（前后反相，抬起同步）
        };

        for (double[] f : feet) {
            // 正确朝向：forward = (-sinYaw, cosYaw)，right = (cosYaw, sinYaw)
            double worldX = player.getX() + f[0] * cosYaw - f[1] * sinYaw;
            double worldZ = player.getZ() + f[0] * sinYaw + f[1] * cosYaw;
            double worldY = footY + f[2];

            // 垂直向下喷（第一人称不显示自己，第三人称看垂直下喷更自然）
            level.addParticle(
                ModParticles.IONOCRAFT_BOOTS_EXHAUST.get(),
                true,
                worldX + random.nextGaussian() * 0.02,
                worldY + random.nextGaussian() * 0.02,
                worldZ + random.nextGaussian() * 0.02,
                0.0,                                // x：无水平偏移（稳定向下）
                -0.22 - random.nextFloat() * 0.1,   // 向下速度适中，靠寿命实现长消失距离
                0.0                                 // z：无水平偏移
            );
        }
    }
}
