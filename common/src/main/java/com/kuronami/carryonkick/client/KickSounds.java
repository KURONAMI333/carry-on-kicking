package com.kuronami.carryonkick.client;

import com.kuronami.carryonkick.Constants;
import com.kuronami.carryonkick.network.KickVisualPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

/** 溜めの開始・解除に追随し、次元移動後まで音を残さない。 */
public final class KickSounds {
    private static final SoundEvent WINDUP = sound("windup");
    private static final SoundEvent KICK = sound("kick");
    private static final SoundEvent DROP = sound("drop");
    private static final Map<Integer, WindupSound> LOOPS = new HashMap<>();

    private KickSounds() {
    }

    /** クライアントで確定した姿勢変化に対応する音を再生する。 */
    public static void onPhase(int entityId, KickVisualPhase phase) {
        Minecraft minecraft = Minecraft.getInstance();
        WindupSound previous = LOOPS.remove(entityId);
        if (previous != null) {
            previous.release();
        }
        if (minecraft.level == null) {
            return;
        }
        var player = minecraft.level.getEntity(entityId);
        if (player == null) {
            return;
        }
        if (phase == KickVisualPhase.START) {
            WindupSound loop = new WindupSound(entityId, minecraft.level);
            LOOPS.put(entityId, loop);
            // >0.2秒を20Hzへ対応させる。短押し・取消後の遅延再生はcanPlaySoundが拒否する。
            minecraft.getSoundManager().playDelayed(loop, 5);
        } else if (phase == KickVisualPhase.KICK) {
            var state = ClientKickState.get(entityId);
            float charge = state == null ? 1.0F : state.normalizedCharge();
            minecraft.getSoundManager().play(new SimpleSoundInstance(KICK, SoundSource.PLAYERS,
                    0.8F * charge, 1.0F, SoundInstance.createUnseededRandom(),
                    player.getX(), player.getY() + 0.8, player.getZ()));
        } else if (phase == KickVisualPhase.DROP) {
            var random = SoundInstance.createUnseededRandom();
            minecraft.getSoundManager().play(new SimpleSoundInstance(DROP, SoundSource.PLAYERS,
                    0.15F + random.nextFloat() * 0.10F, 1.0F, random,
                    player.getX(), player.getY() + 0.8, player.getZ()));
        }
    }

    /** ワールド切替・切断時に、このMODが開始した音だけを停止する。 */
    public static void clear() {
        var manager = Minecraft.getInstance().getSoundManager();
        LOOPS.values().forEach(manager::stop);
        LOOPS.clear();
    }

    private static SoundEvent sound(String path) {
        // この音はS2Cの姿勢通知を受けてローカル再生するため、音イベント自体は送信しない。
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path));
    }

    private static final class WindupSound extends AbstractTickableSoundInstance {
        private final int entityId;
        private final Object level;
        private int audibleTicks;
        private int fadeOutTicks;

        private WindupSound(int entityId, Object level) {
            super(WINDUP, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.entityId = entityId;
            this.level = level;
            this.looping = true;
            this.volume = 0;
            updatePosition();
        }

        @Override
        public boolean canPlaySound() {
            var state = ClientKickState.get(entityId);
            return !isStopped() && Minecraft.getInstance().level == level
                    && (fadeOutTicks > 0 || (LOOPS.get(entityId) == this
                    && state != null && state.phase() == KickVisualPhase.START));
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        private void release() {
            if (audibleTicks == 0) {
                // まだ開始していないplayDelayedを、解放後に鳴らさない。
                stop();
            } else {
                fadeOutTicks = 2;
            }
        }

        @Override
        public void tick() {
            if (!canPlaySound() || !updatePosition()) {
                stop();
                LOOPS.remove(entityId, this);
                return;
            }
            if (fadeOutTicks > 0) {
                this.volume *= 0.5F;
                if (--fadeOutTicks == 0) {
                    stop();
                }
                return;
            }
            this.volume = 0.6F * Math.min(++audibleTicks / 2.0F, 1.0F);
            float charge = ClientKickState.pose(entityId, 0).charge();
            this.pitch = 1.0F + net.minecraft.util.Mth.clamp((charge - 0.2F) / 0.8F, 0, 1);
        }

        private boolean updatePosition() {
            var currentLevel = Minecraft.getInstance().level;
            var entity = currentLevel == null ? null : currentLevel.getEntity(entityId);
            if (entity == null || !entity.isAlive()) {
                return false;
            }
            this.x = entity.getX();
            this.y = entity.getY() + 0.8;
            this.z = entity.getZ();
            return true;
        }
    }
}
