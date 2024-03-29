package no.sandramoen.transagentx.actors.characters.player

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import no.sandramoen.transagentx.actors.Explosion
import no.sandramoen.transagentx.actors.GroundCrack
import no.sandramoen.transagentx.actors.characters.enemies.SpiderWeb
import no.sandramoen.transagentx.actors.particles.RainbowExplosion
import no.sandramoen.transagentx.actors.particles.RunningSmokeEffect
import no.sandramoen.transagentx.utils.BaseActor
import no.sandramoen.transagentx.utils.BaseGame
import no.sandramoen.transagentx.utils.XBoxGamepad

class Player(x: Float, y: Float, stage: Stage) : BaseActor(0f, 0f, stage) {
    private lateinit var bodyRunWESAnimation: Animation<TextureAtlas.AtlasRegion>
    private lateinit var bodyRunWENAnimation: Animation<TextureAtlas.AtlasRegion>
    private lateinit var bodyRunNAnimation: Animation<TextureAtlas.AtlasRegion>
    private lateinit var bodyRunSAnimation: Animation<TextureAtlas.AtlasRegion>
    private lateinit var bodyIdleAnimation: Animation<TextureAtlas.AtlasRegion>
    private lateinit var bodyDeathAnimation: Animation<TextureAtlas.AtlasRegion>
    private lateinit var bodySmileAnimation: Animation<TextureAtlas.AtlasRegion>

    private var shield: Shield
    private var wobbleAction: RepeatAction? = null
    private var runningSmokeAction: RepeatAction? = null
    private var state = State.Idle
    private var movementSpeed = 26f
    private var movementAcceleration = movementSpeed * 8f
    private var beard: Beard = Beard(this)
    private var hair: Hair = Hair(this)
    private var skin: Skin = Skin(this)

    var isInvulnerable = false
    val originalMovementSpeed = movementSpeed
    var isPlaying = true
    var health: Int = 4

    init {
        addActor(skin)
        addActor(hair)
        addActor(beard)

        loadBodyAnimation()
        centerAtPosition(x, y)

        setAcceleration(movementAcceleration)
        setMaxSpeed(movementSpeed)
        setDeceleration(movementAcceleration)

        alignCamera()
        zoomCamera(.5f)

        setBoundaryPolygon(8)
        setOrigin(Align.center)

        pantingAnimation()
        shield = Shield(0f, 0f, stage)
        playEnterAnimation()
    }

    override fun act(dt: Float) {
        super.act(dt)
        if (!isPlaying) return

        movementPolling()
        applyPhysics(dt)
        setMovementAnimation()

        boundToWorld()
        alignCamera(lerp = .1f)
        shield.centerAtActor(this)
        checkSpiderWeb()
    }

    override fun death() {
        isCollisionEnabled = false
        resetColors()
        addAction(
            Actions.parallel(
                Actions.scaleTo(1f, 1f, 0f),
                Actions.moveBy(0f, 100f, BeamOut.animationDuration, Interpolation.circleIn),
                Actions.fadeOut(BeamOut.animationDuration, Interpolation.circleIn),
                Actions.rotateBy(40f, BeamOut.animationDuration),
                Actions.run { isShakyCam = false }
            )
        )
        BeamOut(x, y - 2, stage, this)
        setAnimation(bodyDeathAnimation)
        hair.setAnimation(hair.deathAnimation)
        beard.setAnimation(beard.deathAnimation)
        skin.setAnimation(skin.deathAnimation)
        isPlaying = false
        GroundCrack(x - width / 2, y - height / 1, stage)
    }

    override fun flip() {
        super.flip()
        hair.flip()
        beard.flip()
        skin.flip()
    }

    fun smile() {
        setAnimation(bodySmileAnimation)
        hair.setAnimation(hair.smileAnimation)
        skin.setAnimation(skin.smileAnimation)
        beard.setAnimation(beard.smileAnimation)
    }

    fun fadeOut() {
        val duration = 2f
        addAction(Actions.fadeOut(duration))
        skin.addAction(Actions.fadeOut(duration))
        beard.addAction(Actions.fadeOut(duration))
        hair.addAction(Actions.fadeOut(duration))
        removeRunningSmokeAction()
    }

    fun isHurt(amount: Int): Boolean {
        if (!isInvulnerable) {
            hurtAnimation()
            if (shield.isActive) {
                shield.fadeOut()
            } else {
                health -= amount
                Explosion(this, stage)
                setHealthSpeed()
                jumpToRandomLocation()
                return true
            }
        }
        return false
    }

    fun healthBack() {
        health++
        setHealthSpeed()
    }

    fun toggleHairColor() {
        if (isCollisionEnabled) {
            hair.toggleColor()
            beard.toggleColor()
            rainbowExplosionEffect()
            BaseGame.rainbowSound!!.play(
                BaseGame.soundVolume * .1f,
                MathUtils.random(.95f, 1.05f),
                0f
            )
            BaseGame.setSteamAchievement("ACHIEVEMENT_COLOUR")
        }
    }

    fun toggleHairStyle() {
        if (isCollisionEnabled) {
            hair.toggleStyle()
            val hairAnimation = when (state) {
                State.Idle -> hair.idleAnimation
                State.RunningN -> hair.runNAnimation
                State.RunningS -> hair.runSAnimation
                State.RunningWEN -> hair.runWENAnimation
                State.RunningWES -> hair.runWESAnimation
            }
            hair.setAnimation(hairAnimation)
            rainbowExplosionEffect()
            BaseGame.rainbowSound!!.play(
                BaseGame.soundVolume * .1f,
                MathUtils.random(.95f, 1.05f),
                0f
            )
            BaseGame.setSteamAchievement("ACHIEVEMENT_COLOUR")
        }
    }

    fun toggleBeardStyle() {
        if (isCollisionEnabled) {
            beard.toggleStyle()
            val beardAnimation = when (state) {
                State.Idle -> beard.idleAnimation
                State.RunningN -> beard.runNAnimation
                State.RunningS -> beard.runSAnimation
                State.RunningWEN -> beard.runWENAnimation
                State.RunningWES -> beard.runWESAnimation
            }
            beard.setAnimation(beardAnimation)
            rainbowExplosionEffect()
            BaseGame.rainbowSound!!.play(
                BaseGame.soundVolume * .1f,
                MathUtils.random(.95f, 1.05f),
                0f
            )
            BaseGame.setSteamAchievement("ACHIEVEMENT_COLOUR")
        }
    }

    fun toggleSkinColor() {
        if (isCollisionEnabled) {
            skin.toggleColor()
            rainbowExplosionEffect()
            BaseGame.rainbowSound!!.play(
                BaseGame.soundVolume * .1f,
                MathUtils.random(.95f, 1.05f),
                0f
            )
            BaseGame.setSteamAchievement("ACHIEVEMENT_COLOUR")
        }
    }

    fun exitLevel() {
        smile()
        isPlaying = false
        shield.isVisible = false
        resetColors()
        addAction(stretchAndMoveOut())
    }

    fun activateShield() {
        shield.fadeIn()
    }

    fun disableShield() {
        if (shield.isActive)
            shield.fadeOut()
    }

    fun speedBoost() {
        movementSpeed += 2
        setMaxSpeed(movementSpeed)
        addAction(Actions.sequence(
            Actions.delay(3f),
            Actions.run {
                movementSpeed -= 2
                setMaxSpeed(movementSpeed)
            }
        ))
    }

    private fun resetColors() {
        color = Color.WHITE
        hair.clearActions()
        hair.color = hair.activeColor
        beard.clearActions()
        beard.color = beard.activeColor
        skin.clearActions()
        skin.color = skin.activeColor
    }

    private fun checkSpiderWeb() {
        for (enemy: BaseActor in getList(stage, SpiderWeb::class.java.canonicalName)) {
            if (overlaps(enemy as SpiderWeb)) {
                addAction(Actions.sequence(
                    Actions.delay(.1f),
                    Actions.run { setMaxSpeed(originalMovementSpeed * .25f) }
                ))
            } else {
                setHealthSpeed()
            }
        }
    }

    private fun rainbowExplosionEffect() {
        val effect = RainbowExplosion()
        effect.setScale(.02f)
        effect.centerAtActor(this)
        stage.addActor(effect)
        effect.zIndex = zIndex - 1
        effect.start()
    }

    private fun setHealthSpeed() {
        when (health) {
            4 -> movementSpeed = 27f
            3 -> movementSpeed = 28f
            2 -> movementSpeed = 30f
            1 -> movementSpeed = 31f
        }
        setMaxSpeed(movementSpeed)
    }

    private fun jumpToRandomLocation() {
        addAction(Actions.moveBy(MathUtils.random(-5f, 5f), MathUtils.random(-5f, 5f), .1f))
    }

    private fun pantingAnimation() {
        addAction(
            Actions.sequence(
                Actions.delay(8f),
                Actions.forever(
                    Actions.sequence(
                        Actions.scaleTo(1.025f, .975f, .5f),
                        Actions.scaleTo(.975f, 1.025f, .5f)
                    )
                )
            )
        )
    }

    private fun hurtAnimation() {
        isCollisionEnabled = false
        val colourDuration = 1.25f
        setAnimation(bodyDeathAnimation)
        hair.setAnimation(hair.deathAnimation)
        beard.setAnimation(hair.deathAnimation)
        skin.setAnimation(skin.deathAnimation)
        isShakyCam = true
        addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.color(Color.BLACK, colourDuration / 2),
                    Actions.run {
                        hair.addAction(Actions.color(Color.BLACK, colourDuration / 2))
                        beard.addAction(Actions.color(Color.BLACK, colourDuration / 2))
                        skin.addAction(Actions.color(Color.BLACK, colourDuration / 2))
                    }
                ),
                Actions.parallel(
                    Actions.color(Color.WHITE, colourDuration / 2),
                    Actions.run {
                        hair.addAction(Actions.color(hair.activeColor, colourDuration / 2))
                        beard.addAction(Actions.color(beard.activeColor, colourDuration / 2))
                        skin.addAction(Actions.color(skin.activeColor, colourDuration / 2))
                    }
                ),
                Actions.run {
                    isCollisionEnabled = true
                    state = State.Idle
                    setMovementAnimation()
                    isShakyCam = false
                }
            )
        )
    }

    private fun addRunningSmokeAction() {
        if (runningSmokeAction == null) {
            runningSmokeAction = Actions.forever(Actions.sequence(
                Actions.delay(.1f),
                Actions.run {
                    val effect = RunningSmokeEffect()
                    effect.setScale(.015f)
                    effect.setPosition(x + width / 2, y + height / 8)
                    stage.addActor(effect)
                    effect.zIndex = zIndex
                    effect.start()
                }
            ))
            addAction(runningSmokeAction)
        }
    }

    private fun removeRunningSmokeAction() {
        removeAction(runningSmokeAction)
        runningSmokeAction = null
    }

    private fun movementPolling() {
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
            setSpeed(length * movementSpeed)
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
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP))
            accelerateAtAngle(90f)
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT))
            accelerateAtAngle(180f)
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN))
            accelerateAtAngle(270f)
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT))
            accelerateAtAngle(0f)
    }

    private fun loadBodyAnimation() {
        var animationImages: Array<TextureAtlas.AtlasRegion> = Array()

        for (i in 1..20)
            animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/idle1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/idle2"))
        bodyIdleAnimation = Animation(.1f, animationImages, Animation.PlayMode.LOOP_PINGPONG)
        animationImages.clear()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/runWES1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/runWES2"))
        bodyRunWESAnimation = Animation(.1f, animationImages, Animation.PlayMode.LOOP_PINGPONG)
        animationImages.clear()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/runWEN1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/runWEN2"))
        bodyRunWENAnimation = Animation(.1f, animationImages, Animation.PlayMode.LOOP_PINGPONG)
        animationImages.clear()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/runN1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/runN2"))
        bodyRunNAnimation = Animation(.1f, animationImages, Animation.PlayMode.LOOP_PINGPONG)
        animationImages.clear()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/runS1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/runS2"))
        bodyRunSAnimation = Animation(.1f, animationImages, Animation.PlayMode.LOOP_PINGPONG)
        animationImages.clear()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/death1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/death2"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/death3"))
        bodyDeathAnimation = Animation(.2f, animationImages)
        animationImages.clear()

        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/smile1"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/smile2"))
        animationImages.add(BaseGame.textureAtlas!!.findRegion("player/body/smile3"))
        bodySmileAnimation = Animation(.2f, animationImages)
        animationImages.clear()

        setAnimation(bodyIdleAnimation)
        hair.setAnimation(hair.idleAnimation)
        beard.setAnimation(beard.idleAnimation)
        skin.setAnimation(skin.idleAnimation)
    }

    private fun setMovementAnimation() {
        setAnimation()
        flipPlayer()
    }

    private fun addWobbleAction() {
        if (wobbleAction == null) {
            val rotation = 8f
            val duration = .18f
            wobbleAction = Actions.forever(
                Actions.sequence(
                    Actions.rotateTo(rotation, duration),
                    Actions.rotateTo(-rotation, duration * 2),
                    Actions.rotateTo(0f, duration)
                )
            )
            addAction(wobbleAction)
        }
    }

    private fun removeWobbleAction() {
        removeAction(wobbleAction)
        rotation = 0f
        wobbleAction = null
    }

    private fun setAnimation() {
        if (!isMoving() && !isState(State.Idle)) {
            setAnimationAndState(
                bodyIdleAnimation,
                State.Idle,
                hair.idleAnimation,
                skin.idleAnimation,
                beard.idleAnimation
            )
            removeWobbleAction()
            removeRunningSmokeAction()
        } else if (isMoving() && !isState(State.RunningN) && (getMotionAngle() in 70f..110f)) {
            setAnimationAndState(
                bodyRunNAnimation,
                State.RunningN,
                hair.runNAnimation,
                skin.runNAnimation,
                beard.runNAnimation
            )
            addWobbleAction()
            addRunningSmokeAction()
        } else if (isMoving() && !isState(State.RunningWEN) && ((getMotionAngle() > 45 && getMotionAngle() < 70f) || (getMotionAngle() > 110f && getMotionAngle() < 135f))) {
            setAnimationAndState(
                bodyRunWENAnimation,
                State.RunningWEN,
                hair.runWENAnimation,
                skin.runWENAnimation,
                beard.runWENAnimation
            )
            addWobbleAction()
            addRunningSmokeAction()
        } else if (isMoving() && !isState(State.RunningWES) && ((getMotionAngle() <= 45 || getMotionAngle() > 290) || (getMotionAngle() < 250f && getMotionAngle() >= 135))) {
            setAnimationAndState(
                bodyRunWESAnimation,
                State.RunningWES,
                hair.runWESAnimation,
                skin.runWESAnimation,
                beard.runWESAnimation
            )
            addWobbleAction()
            addRunningSmokeAction()
        } else if (isMoving() && !isState(State.RunningS) && (getMotionAngle() in 250f..290f)) {
            setAnimationAndState(
                bodyRunSAnimation,
                State.RunningS,
                hair.runSAnimation,
                skin.runSAnimation,
                beard.runSAnimation
            )
            addWobbleAction()
            addRunningSmokeAction()
        }
    }

    private fun isState(state: State): Boolean {
        return this.state == state
    }

    private fun setAnimationAndState(
        animation: Animation<TextureAtlas.AtlasRegion>,
        state: State,
        hairAnimation: Animation<TextureAtlas.AtlasRegion>,
        skinAnimation: Animation<TextureAtlas.AtlasRegion>,
        beardAnimation: Animation<TextureAtlas.AtlasRegion>
    ) {
        setAnimation(animation)
        hair.setAnimation(hairAnimation)
        skin.setAnimation(skinAnimation)
        beard.setAnimation(beardAnimation)
        this.state = state
    }

    private fun flipPlayer() {
        if (getSpeed() > 0 && (getMotionAngle() <= 90 || getMotionAngle() >= 270) && !isFacingRight)
            cardboardFlipAnimation()
        else if (getSpeed() > 0 && (getMotionAngle() > 90 && getMotionAngle() < 270) && isFacingRight)
            cardboardFlipAnimation()
    }

    private fun cardboardFlipAnimation() {
        flip()
        val duration = .15f
        addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.scaleTo(.025f, 1f, duration),
                    Actions.parallel(
                        Actions.color(Color.BLACK, duration),
                        Actions.run {
                            hair.addAction(Actions.color(Color.BLACK, duration))
                            beard.addAction(Actions.color(Color.BLACK, duration))
                            skin.addAction(Actions.color(Color.BLACK, duration))
                        }
                    )
                ),
                Actions.parallel(
                    Actions.scaleTo(1f, 1f, duration),
                    Actions.parallel(
                        Actions.color(Color.WHITE, duration),
                        Actions.run {
                            hair.addAction(Actions.color(hair.activeColor, duration))
                            beard.addAction(Actions.color(hair.activeColor, duration))
                            skin.addAction(Actions.color(skin.activeColor, duration))
                        }
                    )
                )
            )
        )
    }

    private enum class State {
        Idle, RunningWES, RunningWEN, RunningN, RunningS
    }

    private fun playEnterAnimation() {
        BeamIn(x, y, stage, this)
        revealAnimation(BeamIn.animationDuration)
    }

    private fun revealAnimation(beamDuration: Float) {
        color.a = 0f
        hair.color.a = 0f
        beard.color.a = 0f
        setScale(0f, 0f)
        bouncyFadeIn(beamDuration)
    }

    private fun bouncyFadeIn(beamDuration: Float) {
        addAction(
            Actions.sequence(
                Actions.delay(beamDuration / 3),
                Actions.parallel(
                    Actions.scaleTo(1f, 1f, 1f, Interpolation.bounceOut),
                    Actions.fadeIn(1f, Interpolation.bounceOut),
                    Actions.run {
                        hair.addAction(Actions.fadeIn(1f, Interpolation.bounceOut))
                        beard.addAction(Actions.fadeIn(1f, Interpolation.bounceOut))
                    }
                )
            )
        )
    }

    private fun stretchAndMoveOut(): ParallelAction? {
        return Actions.parallel(
            Actions.scaleTo(.1f, 3f, BeamOut.animationDuration),
            Actions.moveBy(0f, 100f, BeamOut.animationDuration, Interpolation.circleIn),
            Actions.fadeOut(BeamOut.animationDuration, Interpolation.circleIn)
        )
    }
}
