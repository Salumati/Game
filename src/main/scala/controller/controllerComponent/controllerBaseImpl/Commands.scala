package controller.controllerComponent.controllerBaseImpl

import controller.controllerComponent.ControllerInterface
import model.newGameEvent
import util.Command

class createGameCommand(controller: ControllerInterface, npcNames: Vector[String], playerNames: Vector[String]) extends Command {
    override def doStep: Unit = controller.createGame(npcNames, playerNames)

    override def redoStep: Unit = controller.createGame(npcNames, playerNames)

    override def undoStep: Unit = controller.notifyObservers(newGameEvent())
}