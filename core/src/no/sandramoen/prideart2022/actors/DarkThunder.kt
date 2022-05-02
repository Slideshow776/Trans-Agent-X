package no.sandramoen.prideart2022.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import no.sandramoen.prideart2022.utils.BaseActor
import no.sandramoen.prideart2022.utils.BaseGame

class DarkThunder(stage: Stage) : BaseActor(0f, 0f, stage) {
    init {
        loadImage("whitePixel")
        color = Color.BLACK
        setSize(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        animate()
    }

    private fun animate() {
        addAction(Actions.forever(Actions.sequence(
            Actions.alpha(.25f, .1f),
            Actions.alpha(0f, .1f),
            Actions.delay(MathUtils.random(6f, 18f)),
            Actions.run { BaseGame.thunderSound!!.play(BaseGame.soundVolume, MathUtils.random(.5f, 1.5f), 0f) }
        )))
    }
}