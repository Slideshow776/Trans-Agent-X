package no.sandramoen.transagentx.actors.characters.enemies

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import no.sandramoen.transagentx.actors.characters.player.Player
import no.sandramoen.transagentx.utils.BaseActor

class SpiderWebShot(x: Float, y: Float, stage: Stage, player: Player) : BaseActor(x, y, stage) {
    private val shotSpeed = .5f
    init {
        loadImage("enemies/spiderShot")
        addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveTo(player.x, player.y, shotSpeed),
                Actions.rotateBy(1440f, shotSpeed)
            ),
            Actions.run {
                SpiderWeb(this, player)
                remove()
            }
        ))
    }
}
