package Game

import controller._
import model.OpenMainWindowEvent
import view.Tui
//// todo undo pattern spieleranzahl namen wie viele spieler und bots im beforegamestartsstate
////todo option and try wo exceptions kommen koennen oder none
//
//object Monopoly {
//  val gameController = new GameController
//  val tui = new Tui(gameController)
//
//  def main(args: Array[String]) = gameController.run()
//}
//
//// todo if player.gameover -> remove player
// todo currentPlayer = players(isturn)
import scalafx.application.JFXApp

import scala.language.implicitConversions

// todo menubar http://sjgpsoft.blogspot.com/2016/02/scalafx-menu-basics.html
// todo sbt -> run ....https://github.com/scalafx/scalafx-hello-world/tree/SFX-8
//todo dialogs: https://github.com/scalafx/scalafx/blob/master/scalafx-demos/src/main/scala/scalafx/controls/DialogsDemo.scala
object Monopoly extends JFXApp {
    val gameController = new GameController
    val tui = new Tui(gameController)
    gameController.notifyObservers(OpenMainWindowEvent())
}