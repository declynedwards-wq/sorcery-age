package radon.jujutsu_kaisen.ability.paralyzing_gaze;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.projectile.CursedEnergyImbuedItemProjectile;
import radon.jujutsu_kaisen.entity.projectile.base.JujutsuProjectile;
import radon.jujutsu_kaisen.item.base.CursedToolItem;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.NoSuchElementException;

public class ParalyzingGaze extends Ability implements Ability.IToggled {
    public static final double RANGE = 50.0D;
    public LivingEntity CurrentTarget = null;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean usesHands() {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    public static boolean canParalyze(Entity target) {
        return (target instanceof LivingEntity);
    }

    private @Nullable LivingEntity getTarget(LivingEntity owner) {
        Vec3 start = owner.getEyePosition(1.0F);
        Vec3 direction = owner.getLookAngle();
        Vec3 end = start.add(direction.scale(RANGE));
        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(owner.level(), owner, start, end, new AABB(start, end).inflate(0.5, 0.5, 0.5), e -> !e.isSpectator() && canParalyze(e));
        if (hitResult != null && hitResult.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    @Override
    public void run(LivingEntity owner) {
        if (owner.level().isClientSide) return;
        CurrentTarget = getTarget(owner);
        if (CurrentTarget != null) {
            owner.addEffect(new MobEffectInstance(JJKEffects.STUN.get(), 1*20, 1, false, false, false));
            CurrentTarget.addEffect(new MobEffectInstance(JJKEffects.PARALYZED.get(), 1*20, 1, false, false, false));
        }
    }

    @Override
    public float getCost(LivingEntity owner) {
        if (CurrentTarget != null){
            try {
                ISorcererData ownercap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                ISorcererData targetcap = CurrentTarget.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                return Math.max(3.0F, 3.0F * (targetcap.getRealPower() - ownercap.getRealPower()));
            } catch (NoSuchElementException e) {
                return 3.0F;
            }
        }
        return 3.0F;
    }

    @Override
    public void onEnabled(LivingEntity owner) {
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.MASTER, 2.0F, 1.0F);
    }

    @Override
    public void onDisabled(LivingEntity owner) {
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.MASTER, 2.0F, 1.0F);
    }
}
