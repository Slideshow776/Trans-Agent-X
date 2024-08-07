package no.sandramoen.transagentx.screens.gameplay

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import no.sandramoen.transagentx.actors.*
import no.sandramoen.transagentx.actors.characters.lost.Lost1
import no.sandramoen.transagentx.screens.shell.EpilogueScreen
import no.sandramoen.transagentx.utils.BaseActor
import no.sandramoen.transagentx.utils.BaseGame
import no.sandramoen.transagentx.utils.BaseGame.Companion.myBundle
import no.sandramoen.transagentx.utils.GameUtils

class Level5 : BaseLevel() {
    private val lostSoulsSpawner = BaseActor(0f, 0f, mainStage)
    private val rainSplatter = BaseActor(0f, 0f, mainStage)

    override fun initialize() {
        tilemap = TilemapActor(BaseGame.level4, mainStage)
        super.initialize()

        triggerLevelProgression()
        playMusicWithDelay()
    }

    override fun pause() {
        super.pause()
        rainSplatter.clearActions()
    }

    override fun resume() {
        super.resume()
        spawnRainSplatter()
    }

    private fun playMusicWithDelay() {
        BaseActor(0f, 0f, mainStage).addAction(Actions.sequence(
            Actions.delay(5f),
            Actions.run { GameUtils.playAndLoopMusic(BaseGame.level5Music) }
        ))
    }

    private fun triggerLevelProgression() {
        val rain = Rain(0f, 0f, uiStage)
        rain.color = Color(0.647f, 0.188f, 0.188f, 1f) // red
        objectivesLabel.setMyText(myBundle!!.get("objective7"))
        player.shakyCamIntensity = .0125f
        player.isShakyCam = true
        spawnSpace()
        spawnRainSplatter()
        GameUtils.playAndLoopMusic(BaseGame.rainMusic)
        spawnLostSouls()
        DarkThunder(uiStage)
        BaseActor(0f, 0f, mainStage).addAction(
            Actions.sequence(
                Actions.run { fadeFleetAdmiralInAndOut(myBundle!!.get("fleetAdmiral28")) },
                Actions.delay(3.5f),
                Actions.run {
                    fadeFleetAdmiralInAndOut(myBundle!!.get("fleetAdmiral29"))
                    player.shakyCamIntensity = .0125f
                },
                Actions.delay(2f),
                Actions.run { objectivesLabel.fadeIn() },
                Actions.delay(38f),
                Actions.run {
                    fadeFleetAdmiralInAndOut(myBundle!!.get("fleetAdmiral30"))
                    lostSoulsSpawner.clearActions()
                    player.shakyCamIntensity = .05f
                },
                Actions.delay(20f),
                Actions.run {
                    fadeFleetAdmiralInAndOut(
                        myBundle!!.get("fleetAdmiral31"),
                        6f
                    )
                    player.shakyCamIntensity = .15f
                    objectivesLabel.fadeOut()
                    BaseGame.setSteamAchievement("ACHIEVEMENT_LEVEL_5")
                },
                Actions.delay(6f),
                Actions.run {
                    playerExitLevel()
                    player.isShakyCam = false
                },
                Actions.delay(3f),
                Actions.run {
                    BaseGame.lastPlayedLevel = "Level1"
                    GameUtils.saveGameState()
                    BaseGame.rainMusic!!.stop()
                    BaseGame.thunderSound!!.play(
                        BaseGame.soundVolume,
                        MathUtils.random(.5f, 1.5f),
                        0f
                    )
                    BaseGame.level5Music!!.isLooping = false
                    BaseGame.setActiveScreen(EpilogueScreen())
                }
            ))
    }

    private fun spawnRainSplatter() {
        rainSplatter.addAction(Actions.forever(Actions.sequence(
            Actions.delay(0f),
            Actions.run {
                for (i in 0..3) {
                    val position = randomWorldPosition(0f)
                    val rainSplatter = RainSplatter(position.x, position.y, mainStage)
                    rainSplatter.color = Color(0.647f, 0.188f, 0.188f, 1f) // red
                }
            }
        )))
    }

    private fun spawnSpace() {
        BaseActor(0f, 0f, mainStage).addAction(Actions.forever(Actions.sequence(
            Actions.delay(.075f),
            Actions.run {
                val position = randomWorldPosition(0f)
                SpaceIsThePlace(position.x, position.y, mainStage)
            }
        )))
    }

    private fun spawnLostSouls() {
        lostSoulsSpawner.addAction(Actions.forever(Actions.sequence(
            Actions.delay(2f),
            Actions.run {
                val position = randomWorldPosition()
                Lost1(position.x, position.y, mainStage, player)
            }
        )))
    }
}
