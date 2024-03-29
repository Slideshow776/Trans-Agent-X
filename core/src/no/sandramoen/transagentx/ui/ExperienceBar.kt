package no.sandramoen.transagentx.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Label
import no.sandramoen.transagentx.utils.BaseActor
import no.sandramoen.transagentx.utils.BaseGame

class ExperienceBar(x: Float, y: Float, stage: Stage) : BaseActor(0f, 0f, stage) {
    private var progress: BaseActor
    private var label: Label

    private var nextLevel = 10f
    private var currentXP = 0f
    private var constant = 2f
    private val ratio = 1.14f

    var level = 1

    init {
        loadImage("whitePixel")
        color = Color(0.035f, 0.039f, 0.078f, 1f)
        setSize(Gdx.graphics.width.toFloat(), Gdx.graphics.height * .05f)
        setPosition(x, y - height)

        progress = BaseActor(0f, 0f, stage)
        progress.loadImage("whitePixel")
        progress.color = Color(0.875f, 0.518f, 0.647f, 1f) // light pink
        progress.setSize(0f, height)
        addActor(progress)

        label = Label("${BaseGame.myBundle!!.get("level")} $level", BaseGame.smallLabelStyle)
        label.color = Color(0.922f, 0.929f, 0.914f, 0f) // white
        label.setFontScale(.5f)
        label.setPosition(width - label.prefWidth * 1.2f, 0f)
        label.addAction(fadeIn())
        addActor(label)
    }

    fun increment(number: Int): Boolean {
        currentXP += number
        val level = this.level
        nearArithmeticProgression()

        if (level < this.level)
            return true
        return false
    }

    private fun nearArithmeticProgression() {
        var percent = currentXP / nextLevel
        if (percent >= 1) {
            val restXP = (percent - 1) * nextLevel
            constant += ratio
            nextLevel += constant
            currentXP = restXP
            percent = currentXP / nextLevel
            label.setText("${BaseGame.myBundle!!.get("level")} ${++level}")
            BaseGame.playerLevelUpSound!!.play(BaseGame.soundVolume)
        }

        // println("current XP: $currentXP, next level: $nextLevel, constant: $constant")
        progress.addAction(Actions.sizeTo(width * percent, height, .25f))
    }

    private fun fadeIn(): SequenceAction? {
        return Actions.sequence(
            Actions.delay(6f),
            Actions.fadeIn(1f)
        )
    }
}
