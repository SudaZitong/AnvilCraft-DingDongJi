package com.dingdongji.mod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 超限合金靴子飘升机增强飞行时，从靴底喷出的紫色砧子粒子。
 * 与铁砧工艺的 IonoCraftBackpackExhaustParticle 渲染逻辑一致，但颜色为紫色。
 */
public class IonocraftBootsExhaustParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    /** 起始（最大）尺寸，从靴子喷出后随下坠逐渐变小 */
    private final float baseSize;

    protected IonocraftBootsExhaustParticle(
        ClientLevel level, double x, double y, double z,
        double speedX, double speedY, double speedZ, SpriteSet sprites
    ) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.gravity = 0.0F;         // 无浮力，匀速稳定向下（不飘忽）
        this.friction = 0.98F;       // 几乎无阻力，保持向下速度
        this.xd = speedX;            // 直接用传入速度，无随机水平抖动（稳定向下）
        this.yd = speedY;
        this.zd = speedZ;
        this.rCol = 1.0F;            // 白色（纹理自带紫色 anvilon_space）
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.baseSize = 0.08F; // 固定起始大小，与飘升机粒子最大时一致，不随机
        this.quadSize = this.baseSize;
        this.lifetime = (int) (12.0 / ((double) this.random.nextFloat() * 0.4 + 0.6)); // 寿命明显加长(12~20tick)，消失距离更远
        this.setSpriteFromAge(sprites);
        this.alpha = 0.6F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        float progress = (float) this.age / (float) this.lifetime;
        this.alpha = 0.6F * (1.0F - progress);
        // 从靴子喷出时最大，下坠到消失逐渐变小
        this.quadSize = this.baseSize * (1.0F - progress);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType type, ClientLevel level,
            double x, double y, double z,
            double speedX, double speedY, double speedZ
        ) {
            return new IonocraftBootsExhaustParticle(
                level, x, y, z, speedX, speedY, speedZ, this.sprites
            );
        }
    }
}
