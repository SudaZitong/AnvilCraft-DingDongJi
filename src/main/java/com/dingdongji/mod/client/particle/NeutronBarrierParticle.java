package com.dingdongji.mod.client.particle;

import com.dingdongji.mod.init.ModParticles;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.*;

/**
 * 中子屏障粒子（平躺于地面，与地面平行）：
 * - 屏蔽敌对生物（repel）：从玩家胸腔（下移约5像素）同时放大、淡出、下坠到地面，
 *   放大到 2 格触发边缘，跟随玩家，不受玩家移动影响。生命周期 0.8s。
 * - 屏蔽弹射物（absorb）：从弹射物消失点快速放大 + 淡出，同时顺时针快速旋转，
 *   生命周期 0.8s，跟随玩家。
 */
@OnlyIn(Dist.CLIENT)
public class NeutronBarrierParticle extends TextureSheetParticle {
    private final boolean isRepel;
    private final float maxSize;
    private final java.util.UUID playerUUID;
    private final double offsetX;
    private final double offsetZ;
    /** 弹射物消失点的起始高度（absorb 用）；repel 用动态计算 */
    private final double startY;

    protected NeutronBarrierParticle(ClientLevel level, double x, double y, double z, boolean isRepel) {
        super(level, x, y, z);
        this.isRepel = isRepel;
        this.gravity = 0F;
        this.friction = 1.0F;
        this.lifetime = 16; // 0.8s（两个粒子统一）
        this.quadSize = 0F;
        // repel 放大到 2 格触发边缘；absorb 略小
        this.maxSize = isRepel ? 2.0F : 1.2F;
        this.alpha = 0F;
        this.rCol = 1F;
        this.gCol = 1F;
        this.bCol = 1F;
        this.startY = y;

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            this.playerUUID = player.getUUID();
            this.offsetX = x - player.getX();
            this.offsetZ = z - player.getZ();
        } else {
            this.playerUUID = null;
            this.offsetX = 0;
            this.offsetZ = 0;
        }
    }

    @Override
    public void tick() {
        super.tick();
        float t = (float) this.age / this.lifetime;
        Player player = playerUUID != null ? Minecraft.getInstance().level.getPlayerByUUID(playerUUID) : null;

        // 放大 + 淡出 + （repel）下坠 同时进行，中间不缩小
        this.quadSize = this.maxSize * t;
        this.alpha = 1F - t;

        if (isRepel) {
            // 跟随玩家：x,z 在玩家正下方；y 从胸腔(下移5像素)下坠到地面
            if (player != null) {
                double groundY = player.getY();
                double chestY = groundY + 1.2; // 胸腔 +1.5 再下移约5像素(0.3)
                this.setPos(player.getX(), chestY - (1.2 * t), player.getZ());
            } else {
                this.setPos(this.x, this.startY - (1.2 * t), this.z);
            }
        } else {
            // absorb：跟随玩家相对位置，保持起始高度
            if (player != null) {
                this.setPos(player.getX() + offsetX, this.y, player.getZ() + offsetZ);
            }
        }
    }

    /**
     * repel（屏蔽敌对生物）平躺渲染（与地面平行）；absorb（屏蔽弹射物）改为竖直公告板——
     * 立着的方形，水平方向始终朝向玩家，玩家从任何方向看都像正对自己。两者都双面渲染，
     * 任意角度可见、不受面剔除影响。
     */
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        net.minecraft.world.phys.Vec3 camPos = camera.getPosition();
        float cx = (float)(Mth.lerp(partialTick, this.xo, this.x) - camPos.x);
        float cy = (float)(Mth.lerp(partialTick, this.yo, this.y) - camPos.y);
        float cz = (float)(Mth.lerp(partialTick, this.zo, this.z) - camPos.z);

        float size = this.getQuadSize(partialTick);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTick);
        float r = this.rCol;
        float g = this.gCol;
        float b = this.bCol;
        float a = this.alpha;

        if (!isRepel) {
            // ===== absorb：完全公告板（billboard）=====
            // 粒子平面由相机 up/right 向量张成，法线始终指向玩家相机视线方向。
            // 玩家从任何方向（平视 / 俯视 / 仰视 / 环绕）看，都正对粒子的 2D 面，
            // 即「立着的、面自适应面对玩家」。
            // 完全 billboard：用相机视线方向(look)与上方向(up)构造面朝相机的平面。
            // right = look × up（叉积），再取 right × look 重正交出竖直方向，保证精确。
            org.joml.Vector3f look = camera.getLookVector();
            org.joml.Vector3f upV = camera.getUpVector();
            org.joml.Vector3f rightV = new org.joml.Vector3f(look).cross(upV).normalize();
            org.joml.Vector3f upV2 = new org.joml.Vector3f(rightV).cross(look).normalize();
            // 保留原有「绕视线轴快速旋转」动画：在 rightV/upV2 张成的平面内绕 look 轴旋转，
            // 面仍始终朝向玩家（billboard），同时带有旋转放大消失的动画样式。
            float rotAngle = -(this.age + partialTick) * 0.6F;
            float cR = (float) Math.cos(rotAngle);
            float sR = (float) Math.sin(rotAngle);
            float rxx = rightV.x * cR + upV2.x * sR;
            float ryy = rightV.y * cR + upV2.y * sR;
            float rzz = rightV.z * cR + upV2.z * sR;
            float upx = -rightV.x * sR + upV2.x * cR;
            float upy = -rightV.y * sR + upV2.y * cR;
            float upz = -rightV.z * sR + upV2.z * cR;
            float half = size;
            // 4 角点：右上 / 左上 / 左下 / 右下（rightV±upV2 张成的平面）
            float[][] corners = {
                {cx + (rxx + upx) * half, cy + (ryy + upy) * half, cz + (rzz + upz) * half},
                {cx + (-rxx + upx) * half, cy + (-ryy + upy) * half, cz + (-rzz + upz) * half},
                {cx + (-rxx - upx) * half, cy + (-ryy - upy) * half, cz + (-rzz - upz) * half},
                {cx + (rxx - upx) * half, cy + (ryy - upy) * half, cz + (rzz - upz) * half}
            };
            float[][] uvs = {
                {u1, v1}, {u0, v1}, {u0, v0}, {u1, v0}
            };
            // 正面（始终朝向相机）
            for (int i = 0; i < 4; i++) {
                buffer.addVertex(corners[i][0], corners[i][1], corners[i][2])
                        .setUv(uvs[i][0], uvs[i][1])
                        .setColor(r, g, b, a)
                        .setLight(light);
            }
            // 背面（顶点顺序反转，双面可见）
            for (int i = 0; i < 4; i++) {
                int j = 3 - i;
                buffer.addVertex(corners[j][0], corners[j][1], corners[j][2])
                        .setUv(uvs[i][0], uvs[i][1])
                        .setColor(r, g, b, a)
                        .setLight(light);
            }
            return;
        }

        // ===== repel：平躺（与地面平行）双面渲染 =====
        float[][] raw = {
            {-size, -size}, {size, -size}, {size, size}, {-size, size}
        };
        float[][] uv = {
            {u1, v1}, {u1, v0}, {u0, v0}, {u0, v1}
        };
        // 先画朝上的面（从 +y 看为逆时针），再画朝下的面（顺序反转）
        for (int i = 0; i < 4; i++) {
            buffer.addVertex(cx + raw[i][0], cy, cz + raw[i][1])
                    .setUv(uv[i][0], uv[i][1])
                    .setColor(r, g, b, a)
                    .setLight(light);
        }
        for (int i = 0; i < 4; i++) {
            int j = 3 - i; // 反转顺序，构成朝下的面
            buffer.addVertex(cx + raw[j][0], cy, cz + raw[j][1])
                    .setUv(uv[i][0], uv[i][1])
                    .setColor(r, g, b, a)
                    .setLight(light);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            boolean isRepel = type == ModParticles.NEUTRON_BARRIER_REPEL.get();
            NeutronBarrierParticle p = new NeutronBarrierParticle(level, x, y, z, isRepel);
            p.pickSprite(this.sprites);
            return p;
        }
    }
}
