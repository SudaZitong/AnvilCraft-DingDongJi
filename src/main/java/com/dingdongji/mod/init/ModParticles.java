package com.dingdongji.mod.init;

import com.dingdongji.mod.KryptonMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, KryptonMod.MODID);

    public static final Supplier<SimpleParticleType> IONOCRAFT_BOOTS_EXHAUST =
            PARTICLES.register("ionocraft_boots_exhaust", () -> new SimpleParticleType(false));

    public static final Supplier<SimpleParticleType> NEUTRON_BARRIER_REPEL =
            PARTICLES.register("neutron_barrier_repel", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> NEUTRON_BARRIER_ABSORB =
            PARTICLES.register("neutron_barrier_absorb", () -> new SimpleParticleType(false));
}
