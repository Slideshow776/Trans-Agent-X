package no.sandramoen.prideart2022.actors.characters.lost

import com.badlogic.gdx.scenes.scene2d.Stage
import no.sandramoen.prideart2022.actors.characters.player.Player

class Lost1(x: Float, y: Float, stage: Stage, player: Player? = null) : BaseLost(x, y, stage, player) {
    init {
        initializeAnimation(1)
    }
}
