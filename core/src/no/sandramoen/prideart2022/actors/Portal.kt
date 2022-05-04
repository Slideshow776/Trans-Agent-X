package no.sandramoen.prideart2022.actors

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import no.sandramoen.prideart2022.actors.particles.BluePortalEffect
import no.sandramoen.prideart2022.actors.particles.OrangePortalEffect
import no.sandramoen.prideart2022.utils.BaseActor

class Portal(x: Float, y: Float, stage: Stage, val orange: Boolean) : BaseActor(x, y, stage) {
    private var orangePortalEffect: OrangePortalEffect
    private var bluePortalEffect: BluePortalEffect

    init {
        orangePortalEffect = OrangePortalEffect()
        bluePortalEffect = BluePortalEffect()

        if (orange) {
            loadImage("orangePortal")
            startOrangeEffect()
        } else {
            loadImage("bluePortal")
            startBlueEffect()
        }

        startValueAnimation()
    }

    override fun setPosition(x: Float, y: Float) {
        super.setPosition(x, y)
        orangePortalEffect.centerAtActor(this)
        bluePortalEffect.centerAtActor(this)
    }

    override fun remove(): Boolean {
        isCollisionEnabled = false
        orangePortalEffect.remove()
        bluePortalEffect.remove()
        return super.remove()
    }

    fun setNewPosition(position: Vector2) {
        fadeOut()
        addAction(Actions.sequence(
            Actions.delay(10f),
            Actions.run {
                setPosition(position.x, position.y)
                fadeIn()
            }
        ))
    }

    private fun fadeOut() {
        isCollisionEnabled = false
        addAction(Actions.fadeOut(1f))
        if (orange) orangePortalEffect.stop()
        else bluePortalEffect.stop()
    }

    private fun fadeIn() {
        isCollisionEnabled = true
        addAction(Actions.fadeIn(1f))
        if (orange) orangePortalEffect.start()
        else bluePortalEffect.start()
    }

    private fun startValueAnimation() {
        addAction(
            Actions.forever(
                Actions.parallel(
                    pulseAnimation(),
                    rotateAnimation()
                )
            )
        )
    }

    private fun pulseAnimation(): SequenceAction? {
        return Actions.sequence(
            Actions.scaleTo(1.05f, 1.05f, .5f),
            Actions.scaleTo(.95f, .95f, .5f)
        )
    }

    private fun rotateAnimation(): SequenceAction? {
        return Actions.sequence(
            Actions.rotateBy(2.5f, .5f),
            Actions.rotateBy(-5f, 1f),
            Actions.rotateBy(2.5f, .5f)
        )
    }

    private fun startOrangeEffect() {
        orangePortalEffect.setScale(.01f)
        orangePortalEffect.centerAtActor(this)
        stage.addActor(orangePortalEffect)
        orangePortalEffect.start()
    }

    private fun startBlueEffect() {
        bluePortalEffect.setScale(.01f)
        bluePortalEffect.centerAtActor(this)
        stage.addActor(bluePortalEffect)
        bluePortalEffect.start()
    }
}
