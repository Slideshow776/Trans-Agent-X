package no.sandramoen.prideart2022.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import no.sandramoen.prideart2022.utils.BaseActor
import no.sandramoen.prideart2022.utils.BaseGame
import no.sandramoen.prideart2022.utils.XBoxGamepad

class Player(x: Float, y: Float, stage: Stage) : BaseActor(0f, 0f, stage) {
    private var movementSpeed = 25f
    private var movementAcceleration = movementSpeed * 8f
    private lateinit var runAnimationWES: Animation<TextureAtlas.AtlasRegion>
    private lateinit var runAnimationWEN: Animation<TextureAtlas.AtlasRegion>
    private lateinit var runAnimationN: Animation<TextureAtlas.AtlasRegion>
    private lateinit var runAnimationS: Animation<TextureAtlas.AtlasRegion>
    private lateinit var idleAnimation: Animation<TextureAtlas.AtlasRegion>
    private var state = State.Idle

    var health = 2

    init {
        loadAnimation()
        centerAtPosition(x, y)

        setAcceleration(movementAcceleration)
        setMaxSpeed(movementSpeed)
        setDeceleration(movementAcceleration)

        alignCamera()
        zoomCamera(.5f)

        setBoundaryPolygon(8)
        setOrigin(Align.center)
    }

    override fun act(dt: Float) {
        super.act(dt)
        if (pause) return

        movementPolling(dt)
        applyPhysics(dt)
        setMovementAnimation()

        boundToWorld()
        alignCamera(lerp = .1f)
    }

    fun hit() {
        isCollisionEnabled = false
        health--
        val colourDuration = .75f
        addAction(
            Actions.sequence(
                Actions.color(Color.BLACK, colourDuration / 2),
                Actions.run { isCollisionEnabled = true },
                Actions.color(Color.WHITE, colourDuration / 2)
            )
        )

        reduceMovementSpeedBy(20)
    }

    fun flashColor(color: Color) {
        val duration = .25f
        addAction(
            Actions.sequence(
                Actions.color(color, duration / 2),
                Actions.color(Color.WHITE, duration / 2)
            )
        )
    }

    fun death() {
        color = Color.BLACK
    }

    private fun reduceMovementSpeedBy(percent: Int) {
        movementSpeed *= (100 - percent) / 100f
        setMaxSpeed(movementSpeed)
    }

    private fun movementPolling(dt: Float) {
        if (Controllers.getControllers().size > 0)
            controllerPolling()
        keyboardPolling()
    }

    private fun controllerPolling() {
        val controller = Controllers.getControllers()[0] // TODO: Warning => this is dangerous
        controllerAxisPolling(controller)
        controllerDirectionalPadPolling(controller)
    }

    private fun controllerAxisPolling(controller: Controller) {
        val direction = Vector2(
            controller.getAxis(XBoxGamepad.AXIS_LEFT_Y),
            -controller.getAxis(XBoxGamepad.AXIS_LEFT_X)
        )

        val length = direction.len()
        val deadZone = .1f
        if (length > deadZone) {
            setSpeed(length * 100f)
            setMotionAngle(direction.angleDeg())
        }
    }

    private fun controllerDirectionalPadPolling(controller: Controller) {
        if (controller.getButton(XBoxGamepad.DPAD_UP))
            accelerateAtAngle(90f)
        if (controller.getButton(XBoxGamepad.DPAD_LEFT))
            accelerateAtAngle(180f)
        if (controller.getButton(XBoxGamepad.DPAD_DOWN))
            accelerateAtAngle(270f)
        if (controller.getButton(XBoxGamepad.DPAD_RIGHT))
            accelerateAtAngle(0f)
    }

    private fun keyboardPolling() {
        if (Gdx.input.isKeyPressed(Keys.W))
            accelerateAtAngle(90f)
        if (Gdx.input.isKeyPressed(Keys.A))
            accelerateAtAngle(180f)
        if (Gdx.input.isKeyPressed(Keys.S))
            accelerateAtAngle(270f)
        if (Gdx.input.isKeyPressed(Keys.D))
            accelerateAtAngle(0f)
    }

    private fun loadAnimation() {
        var animationImages: Array<TextureAtlas.AtlasRegion> = Array()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/idle"))
        idleAnimation = Animation(1f, animationImages)
        animationImages.clear()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/runWES1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/runWES2"))
        runAnimationWES = Animation(.1f, animationImages, Animation.PlayMode.LOOP_PINGPONG)
        animationImages.clear()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/runWEN1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/runWEN2"))
        runAnimationWEN = Animation(.1f, animationImages, Animation.PlayMode.LOOP_PINGPONG)
        animationImages.clear()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/runN1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/runN2"))
        runAnimationN = Animation(.1f, animationImages, Animation.PlayMode.LOOP_PINGPONG)
        animationImages.clear()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/runS1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/runS2"))
        runAnimationS = Animation(.1f, animationImages, Animation.PlayMode.LOOP_PINGPONG)
        animationImages.clear()

        setAnimation(idleAnimation)
    }

    private fun setMovementAnimation() {
        setAnimation()
        flipAnimation()
    }

    private fun setAnimation() {
        if (!isMoving() && !isState(State.Idle)) {
            setAnimationAndState(idleAnimation, State.Idle)
        } else if (isMoving() && !isState(State.RunningN) && (getMotionAngle() in 70f..110f)) {
            setAnimationAndState(runAnimationN, State.RunningN)
        } else if (isMoving() && !isState(State.RunningWEN) && ((getMotionAngle() > 45 && getMotionAngle() < 70f) || (getMotionAngle() > 110f && getMotionAngle() < 135f))) {
            setAnimationAndState(runAnimationWEN, State.RunningWEN)
        } else if (isMoving() && !isState(State.RunningWES) && ((getMotionAngle() <= 45 || getMotionAngle() > 290) || (getMotionAngle() < 250f && getMotionAngle() >= 135))) {
            setAnimationAndState(runAnimationWES, State.RunningWES)
        } else if (isMoving() && !isState(State.RunningS) && (getMotionAngle() in 250f..290f)) {
            setAnimationAndState(runAnimationS, State.RunningS)
        }
    }

    private fun isState(state: State): Boolean {
        return this.state == state
    }

    private fun setAnimationAndState(animation: Animation<TextureAtlas.AtlasRegion>, state: State) {
        setAnimation(animation)
        this.state = state
    }

    private fun flipAnimation() {
        if ((getMotionAngle() <= 90 || getMotionAngle() >= 270) && !isFacingRight)
            flip()
        else if ((getMotionAngle() > 90 && getMotionAngle() < 270) && isFacingRight)
            flip()
    }

    private enum class State {
        Idle, RunningWES, RunningWEN, RunningN, RunningS
    }
}
