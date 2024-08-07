package no.sandramoen.transagentx.screens.shell

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.github.tommyettinger.textra.TypingLabel
import no.sandramoen.transagentx.actors.Vignette
import no.sandramoen.transagentx.screens.gameplay.Level2
import no.sandramoen.transagentx.screens.gameplay.Level3
import no.sandramoen.transagentx.screens.gameplay.Level4
import no.sandramoen.transagentx.screens.gameplay.Level5
import no.sandramoen.transagentx.screens.shell.intro.SaturnScreen
import no.sandramoen.transagentx.ui.ControllerMessage
import no.sandramoen.transagentx.ui.MadeByLabel
import no.sandramoen.transagentx.utils.*

class MenuScreen(private val playMusic: Boolean = false) : BaseScreen() {
    private lateinit var continueButton: TextButton
    private lateinit var startButton: TextButton
    private lateinit var optionsButton: TextButton
    private lateinit var titleLabel: TypingLabel
    private lateinit var controllerMessage: ControllerMessage

    private var highlightedActor: Actor? = null
    private var madeByLabel = MadeByLabel()
    private var usingMouse = true
    private var isAxisFreeToMove = true
    private var axisCounter = 0f
    private var time = 0f

    override fun initialize() {
        titleLabel = TypingLabel(
            "{HANG=.3;.2}{GRADIENT=#73bed3;#ebede9;0.1;0}TRAN{ENDGRADIENT}S A{GRADIENT=#ebede9;#df84a5;0.1;0}GENT X{ENDGRADIENT}{ENDHANG}",
            BaseGame.mediumLabelStyle
        )
        titleLabel.setScale(1f)
        titleLabel.alignment = Align.center

        Vignette(uiStage)
        controllerMessage = ControllerMessage()

        val table = Table()
        table.add(titleLabel).padTop(Gdx.graphics.height * .1f)
        table.row()
        table.add(menuButtonsTable()).fillY().expandY()
        table.row()
        table.add(controllerMessage)
        table.row()
        table.add(madeByLabel).padBottom(Gdx.graphics.height * .02f)
        uiTable.add(table).fill().expand()

        if (playMusic)
            GameUtils.playAndLoopMusic(BaseGame.level5Music)

        if (Controllers.getControllers().size > 0) {
            BaseActor(0f, 0f, uiStage).addAction(Actions.sequence(
                Actions.delay(.05f),
                Actions.run {
                    highlightedActor = startButton
                    usingMouse = false
                }
            ))
        }

        checkControllerConnected()
        background()
    }

    override fun update(dt: Float) {
        if (time < 2)
            time += dt

        if (axisCounter > .25f)
            isAxisFreeToMove = true
        else
            axisCounter += dt
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        if (!usingMouse) {
            continueButton.label.color = BaseGame.lightBlue
            startButton.label.color = Color.WHITE
            optionsButton.label.color = Color.WHITE
            madeByLabel.color = madeByLabel.grayColor
        }
        usingMouse = true
        return super.mouseMoved(screenX, screenY)
    }

    override fun keyDown(keycode: Int): Boolean {
        if (time > 1 && (keycode == Keys.BACK || keycode == Keys.ESCAPE || keycode == Keys.BACKSPACE || keycode == Keys.Q))
            exitGame()
        else if (keycode == Keys.ENTER || keycode == Keys.NUMPAD_ENTER || keycode == Keys.SPACE)
            BaseGame.lastPlayedLevel?.let { setLevelScreenWithDelay(it) }
        return false
    }

    override fun buttonDown(controller: Controller?, buttonCode: Int): Boolean {
        usingMouse = false
        if (isDirectionalPad(controller)) {
            if (controller!!.getButton(XBoxGamepad.DPAD_UP))
                swapButtons(up = true)
            else if (controller.getButton(XBoxGamepad.DPAD_DOWN))
                swapButtons(up = false)
        } else if (controller!!.getButton(XBoxGamepad.BUTTON_B)) {
            exitGame()
        } else if (controller!!.getButton(XBoxGamepad.BUTTON_A) && highlightedActor == continueButton) {
            BaseGame.lastPlayedLevel?.let { setLevelScreenWithDelay(it) }
        } else if (controller!!.getButton(XBoxGamepad.BUTTON_A) && highlightedActor == startButton) {
            setLevelScreenWithDelay("Level1")
        } else if (controller.getButton(XBoxGamepad.BUTTON_A) && highlightedActor == optionsButton) {
            setOptionsScreenWithDelay()
        } else if (controller.getButton(XBoxGamepad.BUTTON_A) && highlightedActor == madeByLabel) {
            madeByLabel.openURIWithDelay()
        }
        return super.buttonDown(controller, buttonCode)
    }

    override fun axisMoved(controller: Controller?, axisCode: Int, value: Float): Boolean {
        if (isAxisFreeToMove && value > .1f) {
            val direction = Vector2(
                controller!!.getAxis(XBoxGamepad.AXIS_LEFT_Y),
                -controller.getAxis(XBoxGamepad.AXIS_LEFT_X)
            )

            if (direction.angleDeg() in 30.0..150.0) {
                swapButtons(up = true)
            } else if (direction.angleDeg() in 210.0..330.0)
                swapButtons(up = false)

            isAxisFreeToMove = false
            axisCounter = 0f
        }
        return super.axisMoved(controller, axisCode, value)
    }

    override fun connected(controller: Controller?) {
        highlightedActor = startButton
        usingMouse = false
        GameUtils.vibrateController()
        controllerMessage.showConnected()
        BaseGame.controllerConnectedSound!!.play(BaseGame.soundVolume)
        pause()
    }

    override fun disconnected(controller: Controller?) {
        controllerMessage.showDisConnected()
        BaseGame.controllerDisconnectedSound!!.play(BaseGame.soundVolume)
        pause()
    }

    private fun checkControllerConnected() {
        if (!BaseGame.isControllerChecked) {
            BaseGame.isControllerChecked = true
            if (Controllers.getControllers().size > 0) {
                controllerMessage.showConnected()
                val controller = Controllers.getControllers()[0]
                if (controller.canVibrate() && BaseGame.isVibrationEnabled)
                    GameUtils.vibrateController(1000, .2f)
            } else {
                controllerMessage.showNoControllerFound()
            }
        }
    }

    private fun swapButtons(up: Boolean) {
        if (up) {
            if (highlightedActor == continueButton) {
                highlightedActor = madeByLabel
                continueButton.label.color = BaseGame.lightBlue
                startButton.label.color = Color.WHITE
                optionsButton.label.color = Color.WHITE
                madeByLabel.color = BaseGame.lightPink
            } else if (highlightedActor == startButton) {
                highlightedActor = continueButton
                continueButton.label.color = BaseGame.lightPink
                startButton.label.color = Color.WHITE
                optionsButton.label.color = Color.WHITE
                madeByLabel.color = Color.WHITE
            } else if (highlightedActor == optionsButton) {
                highlightedActor = startButton
                continueButton.label.color = BaseGame.lightBlue
                startButton.label.color = BaseGame.lightPink
                optionsButton.label.color = Color.WHITE
                madeByLabel.color = madeByLabel.grayColor
            } else if (highlightedActor == madeByLabel) {
                highlightedActor = optionsButton
                continueButton.label.color = BaseGame.lightBlue
                startButton.label.color = Color.WHITE
                optionsButton.label.color = BaseGame.lightPink
                madeByLabel.color = madeByLabel.grayColor
            }
        } else {
            if (highlightedActor == continueButton) {
                highlightedActor = startButton
                continueButton.label.color = BaseGame.lightBlue
                startButton.label.color = BaseGame.lightPink
                optionsButton.label.color = Color.WHITE
                madeByLabel.color = madeByLabel.grayColor
            } else if (highlightedActor == startButton) {
                continueButton.label.color = BaseGame.lightBlue
                highlightedActor = optionsButton
                startButton.label.color = Color.WHITE
                optionsButton.label.color = BaseGame.lightPink
                madeByLabel.color = madeByLabel.grayColor
            } else if (highlightedActor == optionsButton) {
                continueButton.label.color = BaseGame.lightBlue
                highlightedActor = madeByLabel
                startButton.label.color = Color.WHITE
                optionsButton.label.color = Color.WHITE
                madeByLabel.color = BaseGame.lightPink
            } else if (highlightedActor == madeByLabel) {
                continueButton.label.color = BaseGame.lightPink
                highlightedActor = continueButton
                startButton.label.color = Color.WHITE
                optionsButton.label.color = Color.WHITE
                madeByLabel.color = madeByLabel.grayColor
            }
        }
    }

    private fun isDirectionalPad(controller: Controller?): Boolean =
        controller!!.getButton(XBoxGamepad.DPAD_UP) ||
                controller.getButton(XBoxGamepad.DPAD_DOWN) ||
                controller.getButton(XBoxGamepad.DPAD_LEFT) ||
                controller.getButton(XBoxGamepad.DPAD_RIGHT)

    private fun menuButtonsTable(): Table {
        val buttonFontScale = 1f
        continueButton = initializeContinueButton(.75f)
        startButton = initializeStartButton(buttonFontScale)
        optionsButton = initializeOptionsButton(buttonFontScale)

        val table = Table()
        if (BaseGame.lastPlayedLevel != "Level1")
            table.add(continueButton).padBottom(Gdx.graphics.height * .03f).row()
        table.add(startButton).padBottom(Gdx.graphics.height * .03f).row()
        table.add(optionsButton).padBottom(Gdx.graphics.height * .03f)
        return table
    }

    private fun initializeContinueButton(buttonFontScale: Float): TextButton {
        val textButton =
            TextButton(BaseGame.myBundle!!.get("continue"), BaseGame.textButtonStyle)
        textButton.label.setFontScale(buttonFontScale)
        textButton.label.color = BaseGame.lightBlue
        textButton.addListener { e: Event ->
            if (GameUtils.isTouchDownEvent(e)) {
                BaseGame.level5Music!!.stop()
                BaseGame.lastPlayedLevel?.let { setLevelScreenWithDelay(it) }
            }
            false
        }
        textButton.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.alpha(.75f, .25f),
                    Actions.alpha(1f, .25f)
                )
            )
        )
        GameUtils.addTextButtonEnterExitEffect(
            textButton = textButton,
            enterColor = BaseGame.lightPink,
            exitColor = BaseGame.lightBlue
        )
        return textButton
    }

    private fun initializeStartButton(buttonFontScale: Float): TextButton {
        val textButton = TextButton(BaseGame.myBundle!!.get("start"), BaseGame.textButtonStyle)
        textButton.label.setFontScale(buttonFontScale)
        textButton.addListener { e: Event ->
            if (GameUtils.isTouchDownEvent(e)) {
                BaseGame.level5Music!!.stop()
                setLevelScreenWithDelay("Level1")
            }
            false
        }
        if (BaseGame.lastPlayedLevel == "Level1")
            textButton.addAction(
                Actions.forever(
                    Actions.sequence(
                        Actions.alpha(.75f, .25f),
                        Actions.alpha(1f, .25f)
                    )
                )
            )
        GameUtils.addTextButtonEnterExitEffect(textButton)
        return textButton
    }

    private fun initializeOptionsButton(buttonFontScale: Float): TextButton {
        val textButton =
            TextButton(BaseGame.myBundle!!.get("options"), BaseGame.textButtonStyle)
        textButton.label.setFontScale(buttonFontScale)
        textButton.addListener { e: Event ->
            if (GameUtils.isTouchDownEvent(e)) {
                setOptionsScreenWithDelay()
            }
            false
        }
        textButton.label.color.a = .8f
        GameUtils.addTextButtonEnterExitEffect(textButton)
        return textButton
    }

    private fun setLevelScreenWithDelay(level: String) {
        println("setLevelScreenWithDelay: $level")
        prepLeaveMenuScreen()
        BaseGame.spaceStationBeamSound!!.play(BaseGame.soundVolume, .5f, 0f)
        startButton.addAction(Actions.sequence(
            Actions.delay(.5f),
            Actions.run {
                when (level.toLowerCase()) {
                    "level1" -> BaseGame.setActiveScreen(SaturnScreen())
                    "level2" -> BaseGame.setActiveScreen(Level2())
                    "level3" -> BaseGame.setActiveScreen(Level3())
                    "level4" -> BaseGame.setActiveScreen(Level4())
                    "level5" -> BaseGame.setActiveScreen(Level5())
                }
            }
        ))
    }

    private fun setOptionsScreenWithDelay() {
        prepLeaveMenuScreen(stopMusic = false)
        optionsButton.addAction(Actions.sequence(
            Actions.delay(.5f),
            Actions.run { BaseGame.setActiveScreen(OptionsScreen()) }
        ))
    }

    private fun exitGame() {
        prepLeaveMenuScreen()
        BaseActor(0f, 0f, uiStage).addAction(Actions.sequence(
            Actions.delay(.5f),
            Actions.run {
                super.dispose()
                Gdx.app.exit()
            }
        ))
    }

    private fun prepLeaveMenuScreen(stopMusic: Boolean = true) {
        if (stopMusic)
            BaseGame.level5Music!!.stop()
        BaseGame.click1Sound!!.play(BaseGame.soundVolume)
        startButton.touchable = Touchable.disabled
        optionsButton.touchable = Touchable.disabled
    }

    private fun background() {
        val saturn = BaseActor(-5f, -10f, mainStage)
        saturn.loadImage("saturn")
        saturn.color.a = 1f
        /*saturn.addAction(Actions.fadeIn(1f))*/
        /*saturn.color = Color.GRAY*/

        val spaceship = BaseActor(-100f, 0f, mainStage)
        spaceship.loadImage("spaceship")
        spaceship.setScale(.5f)
        spaceship.addAction(Actions.forever(Actions.sequence(
            Actions.moveTo(100f, 0f, 120f),
            Actions.run { spaceship.flip() },
            Actions.moveTo(-100f, 0f, 120f),
            Actions.run { spaceship.flip() }
        )))
        spaceship.color = Color.GRAY

        val playerPortrait = BaseActor(35f, -20f, mainStage)
        var animationImages: Array<TextureAtlas.AtlasRegion> = Array()
        for (i in 1..45)
            animationImages.add(BaseGame.textureAtlas!!.findRegion("player/portrait"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/portraitBlink"))
        playerPortrait.setAnimation(Animation(.1f, animationImages, Animation.PlayMode.LOOP))
        animationImages.clear()
        playerPortrait.setScale(8f)
        playerPortrait.flip()
        playerPortrait.color.a = 0f
        playerPortrait.addAction(Actions.fadeIn(1f))
        /*playerPortrait.color = Color.GRAY*/

        val fleetAdmiral = BaseActor(-35f, 0f, mainStage)
        for (i in 1..35)
            animationImages.add(BaseGame.textureAtlas!!.findRegion("fleet admiral/idle1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("fleet admiral/idle2"))
        fleetAdmiral.setAnimation(Animation(.1f, animationImages, Animation.PlayMode.LOOP))
        animationImages.clear()
        fleetAdmiral.setScale(12f)
        fleetAdmiral.color.a = 0f
        fleetAdmiral.addAction(Actions.fadeIn(1f))
        /*fleetAdmiral.color = Color.GRAY*/

        val earth = BaseActor(-80f, -45f, mainStage)
        earth.loadImage("earth")
        earth.color.a = 0f
        earth.addAction(Actions.fadeIn(1f))
        earth.color = Color.GRAY

        /* ------------------------------------------------------------------------ */
        for (i in 0..11) {
            val enemy = BaseActor(-100f + i * 20, -70f, mainStage)
            enemy.loadImage("enemies/charger/runS1")
            enemy.setScale(5f)
            enemy.color = Color.BLACK
            enemy.addAction(
                Actions.sequence(
                    Actions.delay(MathUtils.random(2f, 4f)),
                    Actions.moveTo(enemy.x, -55f, 1f)
                )
            )
        }

        for (i in 0..11) {
            val enemy = BaseActor(-110f + i * 20, -70f, mainStage)
            enemy.loadImage("enemies/charger/runS1")
            enemy.setScale(5f)
            enemy.color = Color.BLACK
            enemy.addAction(
                Actions.sequence(
                    Actions.delay(MathUtils.random(2f, 4f)),
                    Actions.moveTo(enemy.x, -53f, 1f)
                )
            )
        }

        val bossKim = BaseActor(80f, -100f, mainStage)
        bossKim.loadImage("enemies/bossKim/scream1")
        bossKim.setScale(5f)
        bossKim.color = Color.BLACK
        bossKim.addAction(
            Actions.sequence(
                Actions.delay(4f),
                Actions.moveTo(bossKim.x, -56f, 1f)
            )
        )

        val bossKG = BaseActor(-65f, -100f, mainStage)
        bossKG.loadImage("enemies/bossKG/scream1")
        bossKG.setScale(5f)
        bossKG.color = Color.BLACK
        bossKG.addAction(
            Actions.sequence(
                Actions.delay(4f),
                Actions.moveTo(bossKG.x, -58f, 1f)
            )
        )

        val bossIra = BaseActor(-90f, -100f, mainStage)
        bossIra.loadImage("enemies/bossIra/runS1")
        bossIra.setScale(5f)
        bossIra.color = Color.BLACK
        bossIra.addAction(
            Actions.sequence(
                Actions.delay(4f),
                Actions.moveTo(bossIra.x, -52f, 1f)
            )
        )

        /* ------------------------------------------------------------------------ */
        for (i in 0..20) {
            val enemy = BaseActor(-100f + i * 10, 60f, mainStage)
            enemy.loadImage("enemies/tentacle0")
            enemy.setScale(.75f)
            enemy.rotateBy(180f)
            enemy.color = Color.BLACK
            if (MathUtils.randomBoolean())
                enemy.flip()
            enemy.addAction(
                Actions.sequence(
                    Actions.delay(MathUtils.random(2f, 4f)),
                    Actions.moveTo(enemy.x, 44f, 1f)
                )
            )
        }

        for (i in 0..20) {
            val enemy = BaseActor(-105f + i * 10, 60f, mainStage)
            enemy.loadImage("enemies/tentacle0")
            enemy.setScale(.75f)
            enemy.rotateBy(180f)
            enemy.color = Color.BLACK
            if (MathUtils.randomBoolean())
                enemy.flip()
            enemy.addAction(
                Actions.sequence(
                    Actions.delay(MathUtils.random(2f, 4f)),
                    Actions.moveTo(enemy.x, 46f, 1f)
                )
            )
        }

        /* ------------------------------------------------------------------------ */

        /*for (i in 0..20) {
            val enemy = BaseActor(100f, -45f + i * 5, mainStage)
            enemy.loadImage("enemies/tentacle0")
            enemy.setScale(.75f)
            enemy.rotateBy(90f)
            enemy.color = Color.BLACK
            if (MathUtils.randomBoolean())
                enemy.flip()
            enemy.addAction(Actions.sequence(
                Actions.delay(MathUtils.random(2f, 4f)),
                Actions.moveTo(87f, enemy.y, 1f)
            ))
        }

        for (i in 0..20) {
            val enemy = BaseActor(-110f, -45f + i * 5, mainStage)
            enemy.loadImage("enemies/tentacle0")
            enemy.setScale(.75f)
            enemy.rotateBy(270f)
            enemy.color = Color.BLACK
            if (MathUtils.randomBoolean())
                enemy.flip()
            enemy.addAction(Actions.sequence(
                Actions.delay(MathUtils.random(2f, 4f)),
                Actions.moveTo(-92f, enemy.y, 1f)
            ))
        }*/

    }
}
