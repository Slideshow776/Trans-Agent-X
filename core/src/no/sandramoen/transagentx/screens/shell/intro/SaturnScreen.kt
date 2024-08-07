package no.sandramoen.transagentx.screens.shell.intro

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.github.tommyettinger.textra.TypingLabel
import no.sandramoen.transagentx.screens.gameplay.Level1
import no.sandramoen.transagentx.utils.BaseActor
import no.sandramoen.transagentx.utils.BaseGame
import no.sandramoen.transagentx.utils.BaseGame.Companion.myBundle
import no.sandramoen.transagentx.utils.BaseScreen

class SaturnScreen : BaseScreen() {
    private val camera = mainStage.camera as OrthographicCamera
    private lateinit var saturn: BaseActor
    private lateinit var spaceStation: BaseActor
    private lateinit var beam: BaseActor

    private var act1 = false
    private var act2 = false
    private var act3 = false

    private var isAct3CameraZoom = false

    private var timeElapsed = 0f

    private val label = TypingLabel("${myBundle!!.get("skipIntro")}", BaseGame.smallLabelStyle)

    override fun initialize() {
        saturn = BaseActor(0f, 0f, mainStage)
        saturn.loadImage("saturn")
        saturn.isVisible = false

        spaceStation = BaseActor(0f, 0f, mainStage)
        spaceStation.loadImage("whitePixel")
        spaceStation.color = Color.BLACK
        spaceStation.setPosition(saturn.width * .64f, saturn.height * .6f)

        beam = BaseActor(0f, 0f, mainStage)
        beam.loadImage("whitePixel")
        beam.color.a = .9f
        beam.setOrigin(Align.left)
        beam.isVisible = false

        camera.position.set(Vector3(saturn.width * .5f, saturn.height * .5f, 0f))

        BaseActor(0f, 0f, mainStage).addAction(Actions.sequence(
            Actions.delay(1.5f),
            Actions.run { act1() }
        ))

        label.setAlignment(Align.center)
        label.wrap = true
        uiTable.add(label).expandY().bottom().padBottom(Gdx.graphics.height * .02f).width(Gdx.graphics.width * .98f)
        Gdx.input.setCursorPosition(Gdx.graphics.width / 2, Gdx.graphics.height + 10)
    }

    override fun update(dt: Float) {
        timeElapsed += dt

        if (act2) act2Camera()
        else if (act3) {
            act3Camera()
            beam.setPosition(spaceStation.x, spaceStation.y)
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        // TODO: debug, remove before launch -------------------------
        if (keycode == Keys.Q) Gdx.app.exit()
        else if (keycode == Keys.R) BaseGame.setActiveScreen(SaturnScreen())
        // else if (keycode == Keys.W) println("time elapsed: $timeElapsed")
        // -----------------------------------------------------------
        else skipIntro()
        return super.keyDown(keycode)
    }

    override fun buttonDown(controller: Controller?, buttonCode: Int): Boolean {
        skipIntro()
        return super.buttonDown(controller, buttonCode)
    }

    private fun skipIntro() {
        BaseGame.intro1VoiceSound!!.stop()
        BaseGame.cinematic1Music!!.stop()
        BaseGame.cinematic2Music!!.stop()
        BaseGame.setActiveScreen(Level1())
    }

    private fun act1() { // 6
        BaseGame.intro1VoiceSound!!.play(BaseGame.voiceVolume)
        BaseGame.cinematic2Music!!.play()
        BaseGame.cinematic2Music!!.volume = BaseGame.musicVolume

        act1 = true
        act2 = false
        act3 = false

        label.restart()
        label.setText("${myBundle!!.get("saturn11")} {WAIT=.5}${myBundle!!.get("saturn12")} {WAIT=.4}${myBundle!!.get("saturn13")}")
        saturn.setScale(0f)
        saturn.isVisible = true
        saturn.addAction(
            Actions.sequence(
                Actions.scaleTo(.3f, .3f, 3f, Interpolation.linear),
                Actions.run {
                    label.restart()
                    label.setText("{FAST}${myBundle!!.get("saturn21")} {WAIT=.3}{FAST}${myBundle!!.get("saturn22")}")
                },
                Actions.scaleTo(1f, 1f, 1f, Interpolation.bounceOut),
                Actions.delay(1f),
                Actions.run {
                    label.restart()
                    label.setText("${myBundle!!.get("saturn31")} {SPEED=0.85}{COLOR=#c74343}${myBundle!!.get("saturn32")}{CLEARCOLOR} {WAIT=.8}${myBundle!!.get("saturn33")} {WAIT=1}{COLOR=#e8c170}${myBundle!!.get("saturn34")}{CLEARCOLOR}...") },
                Actions.delay(1f),
                Actions.run { act2() }
            )
        )
    }

    private fun act2() { // 10
        act1 = false
        act2 = true
        act3 = false
        spaceStation.addAction(Actions.parallel(
            Actions.moveTo(saturn.x, saturn.y, 180f),
            Actions.sequence(Actions.sequence(
                Actions.delay(3.5f),
                Actions.run {
                    BaseGame.cinematic1Music!!.play()
                    BaseGame.cinematic1Music!!.volume = BaseGame.musicVolume
                },
                Actions.delay(3f),
                Actions.run {
                    label.restart()
                    label.setText("{FADE}${BaseGame.myBundle!!.get("saturn4")}") },
                Actions.delay(3.5f),
                Actions.run { act3() }
            ))
        ))
    }

    private fun act2Camera() {
        if (camera.zoom >= .5f)
            camera.zoom -= .001f

        if (camera.position.x < spaceStation.x)
            camera.position.set(Vector3(camera.position.x + .1f, camera.position.y, 0f))
        else if (camera.position.x > spaceStation.x)
            camera.position.set(Vector3(spaceStation.x, camera.position.y, 0f))
    }

    private fun act3() { // 5
        act1 = false
        act2 = false
        act3 = true
        label.setText("")
        beam.isVisible = true
        beam.isShakyCam = true
        BaseGame.spaceStationBeamSound!!.play(BaseGame.soundVolume)
        beam.addAction(Actions.sequence(
            Actions.scaleTo(2_000f, 1f, 2f),
            Actions.run { isAct3CameraZoom = true },
            Actions.delay(5f),
            Actions.run { BaseGame.setActiveScreen(LightspeedScreen()) }
        ))
    }

    private fun act3Camera() {
        if (!isAct3CameraZoom && (camera.position.x > spaceStation.x))
            camera.position.set(Vector3(spaceStation.x, camera.position.y, 0f))
        if (isAct3CameraZoom) {
            if (camera.zoom - .002f > 0)
                camera.zoom -= .002f

            camera.position.set(
                Vector3(
                    camera.position.x + .5f,
                    beam.y + beam.height / 2,
                    0f
                )
            )
        }
    }
}
