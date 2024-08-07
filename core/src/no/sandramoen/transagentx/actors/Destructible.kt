package no.sandramoen.transagentx.actors

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import no.sandramoen.transagentx.actors.characters.enemies.GhostFreed
import no.sandramoen.transagentx.actors.characters.player.Player
import no.sandramoen.transagentx.actors.particles.DestructiblesEffect
import no.sandramoen.transagentx.utils.BaseActor
import no.sandramoen.transagentx.utils.BaseGame

class Destructible(x: Float, y: Float, stage: Stage, val player: Player) : BaseActor(x, y, stage) {
    private var type = Type.Barrel
    private var soundDistance = 40f

    init {
        randomType()
        setBoundaryRectangle()
        zIndex = player.zIndex - 1
        if (MathUtils.randomBoolean())
            flip()
    }

    fun destroy() {
        destroyType()
        rotation = MathUtils.random(0f, 360f)
        setScale(MathUtils.random(.8f, 1.2f), MathUtils.random(.8f, 1.2f))
        isCollisionEnabled = false
        SmallExplosion(this, stage)
        GhostFreed(x, y, stage)
        particleEffects()
    }

    private fun particleEffects() {
        val effect = DestructiblesEffect()
        effect.setScale(.03f)
        effect.setPosition(x + width / 2, y + height / 5)
        stage.addActor(effect)
        effect.start()
    }

    private fun destroyType() {
        if (type == Type.Barrel) {
            loadImage("destructibles/barrelDestroyed")
            if (isWithinDistance2(soundDistance, player))
                BaseGame.barrelDestroyedSound!!.play(BaseGame.soundVolume)
        } else if (type == Type.Vase) {
            loadImage("destructibles/vaseDestroyed")
            if (isWithinDistance2(soundDistance, player))
                BaseGame.vaseDestroyedSound!!.play(BaseGame.soundVolume)
        } else if (type == Type.Bottle) {
            loadImage("destructibles/bottleDestroyed")
            if (isWithinDistance2(soundDistance, player))
                BaseGame.bottleDestroyedSound!!.play(BaseGame.soundVolume)
        } else if (type == Type.Chair) {
            loadImage("destructibles/chairDestroyed")
            if (isWithinDistance2(soundDistance, player))
                BaseGame.chairDestroyedSound!!.play(BaseGame.soundVolume)
        } else if (type == Type.Skeleton) {
            loadImage("destructibles/skeletonDestroyed")
            if (isWithinDistance2(soundDistance, player))
                BaseGame.skeletonDestroyedSound!!.play(BaseGame.soundVolume)
        } else if (type == Type.Skulls) {
            loadImage("destructibles/skullsDestroyed")
            if (isWithinDistance2(soundDistance, player))
                BaseGame.skullsDestroyedSound!!.play(BaseGame.soundVolume)
        } else if (type == Type.Skull) {
            loadImage("destructibles/skullDestroyed")
            if (isWithinDistance2(soundDistance, player))
                BaseGame.skullDestroyedSound!!.play(BaseGame.soundVolume)
        } else if (type == Type.SmallBush) {
            loadImage("destructibles/smallBushDestroyed")
            if (isWithinDistance2(soundDistance, player))
                BaseGame.smallBushDestroyedSound!!.play(BaseGame.soundVolume)
        }
    }

    private fun randomType() {
        when (MathUtils.random(0, 7)) {
            0 -> {
                type = Type.Barrel
                loadImage("destructibles/barrel")
            }
            1 -> {
                type = Type.Vase
                loadImage("destructibles/vase")
            }
            2 -> {
                type = Type.Bottle
                loadImage("destructibles/bottle")
            }
            3 -> {
                type = Type.Chair
                loadImage("destructibles/chair")
            }
            4 -> {
                type = Type.Skeleton
                rotation = MathUtils.random(0f, 360f)
                loadImage("destructibles/skeleton")

            }
            5 -> {
                type = Type.Skulls
                loadImage("destructibles/skulls")
            }
            6 -> {
                type = Type.Skull
                rotation = MathUtils.random(-10f, 10f)
                loadImage("destructibles/skull")
            }
            7 -> {
                type = Type.Skull
                rotation = MathUtils.random(0f, 360f)
                loadImage("destructibles/smallBush")
            }
        }
    }

    private enum class Type {
        Barrel, Vase, Bottle, Chair, Skeleton, Skulls, Skull, SmallBush
    }
}
