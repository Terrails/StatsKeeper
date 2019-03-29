package terrails.statskeeper.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import terrails.statskeeper.config.SKConfig;
import terrails.statskeeper.effect.IEffectCure;
import terrails.statskeeper.api.effect.SKPotions;
import terrails.statskeeper.api.event.PlayerUseFinishedCallback;

import java.util.Iterator;
import java.util.Map;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements IEffectCure {

    @ModifyVariable(method = "updatePostDeath()V", ordinal = 0, at = @At(value = "STORE", ordinal = 0), require = 1)
    private int dropExperience(int amount) {
        LivingEntity entity = (LivingEntity) (Object) this;
        //noinspection ConstantConditions
        if (!SKConfig.drop_experience && entity instanceof PlayerEntity) {
            return 0;
        }
        return amount;
    }

    @Shadow protected ItemStack activeItemStack;
    @Shadow private @Final Map<StatusEffect, StatusEffectInstance> activePotionEffects;
    @Shadow protected void method_6129(StatusEffectInstance statusEffectInstance_1) {}

    @Override
    public void clearPlayerStatusEffects(ItemStack stack) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.world.isClient) {
            return;
        }
        Iterator<StatusEffectInstance> iterator_1 = this.activePotionEffects.values().iterator();

        while (iterator_1.hasNext()) {
            StatusEffectInstance effect = iterator_1.next();

            if (effect.getEffectType() == SKPotions.NO_APPETITE) {
                continue;
            }

            this.method_6129(effect);
            iterator_1.remove();
        }
    }

    @Inject(method = "method_6040()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;method_6098(Lnet/minecraft/item/ItemStack;I)V"))
    private void itemUseFinished(CallbackInfo info) {
        LivingEntity entity = (LivingEntity) (Object) this;
        //noinspection ConstantConditions
        if (entity instanceof PlayerEntity) {
            PlayerUseFinishedCallback.EVENT.invoker().onItemUseFinished((PlayerEntity) entity, this.activeItemStack);
        }
    }
}