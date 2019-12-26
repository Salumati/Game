package controller.controllerComponent

import controller.GameControllerInterface
import model.DiceComponent.Dice
import model._
import model.playerComponent.Player
import scalafx.application.JFXApp.PrimaryStage
import util.UndoManager

class GameController extends GameControllerInterface {
    val dice = Dice()
    val undoManager = new UndoManager
    val playerController = new PlayerController(this)
    val boardController = new BoardController(this)
    var humanPlayers = 0
    var npcPlayers = 0
    var board: Vector[Cell] = Vector[Cell]()
    //var chanceCards: Vector[Card] = Vector[Card]()
    //var communityChestCards: Vector[Card] = Vector[Card]()

    var players: Vector[PlayerInterface] = Vector[PlayerInterface]()
    var playerNames: Vector[String] = Vector[String]()
    var remainingFiguresToPick = List[String]("Fingerhut",
        "Hut",
        "Schubkarre",
        "Schuh",
        "Hund",
        "Auto",
        "Bügeleisen",
        "Schiff"
    )
    var playerFigures: Vector[String] = Vector[String]()
    var npcNames: Vector[String] = Vector[String]()
    var round = 1
    var answer = ""
    var currentStage = new PrimaryStage()
    var currentPlayer = 0 // aktueller spieler
    var paschCount = 0
    // feldcoords todo resizeable mainwindow offset xy stackpane
    val goXY = (350, 350)
    val jailXY = (-350, 350)
    val ParkFreeXY = (-350, -350)
    val GoToJailXy = (350, -350)
    val fieldCoordsX = List[Double](
        350, 280, 210, 140, 70, 0, -70, -140, -210, -280, -350,
        -350, -350, -350, -350, -350, -350, -350, -350, -350, -350,
        -280, -210, -140, -70, 0, 70, 140, 210, 280, 350,
        350, 350, 350, 350, 350, 350, 350, 350, 350)

    val fieldCoordsY = List[Double](
        350, 350, 350, 350, 350, 350, 350, 350, 350, 350, 350,
        280, 210, 140, 70, 0, -70, -140, -210, -280, -350,
        -350, -350, -350, -350, -350, -350, -350, -350, -350, -350,
        -280, -210, -140, -70, 0, 70, 140, 210, 280)
    var collectedTax = 0
    // todo save gamestate and load gamestate
    var tmpHumanPlayers = humanPlayers
    var tmpNpcPlayers = npcPlayers
    var tmpBoard = board
    var tmpPlayers = players
    var tmpRound = round
    var tmpCurrentPlayer = currentPlayer
    var tmpCollectedTax = collectedTax


    def createGame(playerNames: Vector[String], npcNames: Vector[String]): Unit = {
        players = playerController.createPlayers(playerNames, npcNames)
        board = boardController.createBoard
        humanPlayers = playerNames.length
        npcPlayers = npcNames.length
    }

    def checkGameOver(): Boolean = {
        var playerwithmoney = 0
        var winner = 0
        for (i <- players.indices) {
            if (players(i).money > 0) {
                playerwithmoney += 1
                winner = i
            }
        }
        //(playerwithmoney == 1, winner)
        playerwithmoney == 1
    }

    def payRent: Unit = {
        val updated = playerController.payRent(board(players(currentPlayer).position).asInstanceOf[Buyable])
        players = updated._2
        board = updated._1
    }

    def buy: Unit = {
        val updated = playerController.buy(board(players(currentPlayer).position).asInstanceOf[Buyable])
        board = updated._1
        players = updated._2
    }

    def buyHome: Unit = {
        val updated = boardController.buyHome(board(players(currentPlayer).position).asInstanceOf[Street])
        board = updated._1
        players = updated._2
    }

    def printFun(e: PrintEvent): Unit = {
        notifyObservers(e)
    }

    def checkDepth(player:PlayerInterface,ownerIdx: Int): Unit = {
            notifyObservers(OpenPlayerDeptDialog(ownerIdx))
            //Disable roll button turn endturn button to declare bankrupt
            val rollDiceBUtton = currentStage.scene().lookup("#rollDice") //.asInstanceOf[javafx.scene.control.Button]
            rollDiceBUtton.setDisable(false)
            val endTurnButton = currentStage.scene().lookup("#endTurn").asInstanceOf[javafx.scene.control.Button]
            endTurnButton.setDisable(false)
            endTurnButton.setText("Declare Bankrupt")
    }



    ///////////todo GUI  ///////////////////////////////////////////

    // functions

    // todo currentstage kann raus tui hat ehe controller

    def onQuit() = {
        notifyObservers(OpenConfirmationDialogEvent())
    }

    def onInformation() = {
        notifyObservers(OpenInformationDialogEvent())
    }

    def onSaveGame() = {
        tmpHumanPlayers = humanPlayers
        tmpNpcPlayers = npcPlayers
        tmpBoard = board
        tmpPlayers = players
        tmpRound = round
        tmpCurrentPlayer = currentPlayer
        tmpCollectedTax = collectedTax

        // lblRollDice.gettext
        // lblResultdice gettext
        // get buttons roll dice and end turn disable/enable
        // maybe also get eventlog
    }

    def onLoadGame() = {
        GameStates.handle(InitGameEvent())
        humanPlayers = tmpHumanPlayers
        npcPlayers = tmpNpcPlayers
        board = tmpBoard
        players = tmpPlayers
        round = tmpRound
        currentPlayer = tmpCurrentPlayer
        collectedTax = tmpCollectedTax
        //todo update gui once
        // clear board (houses,ownersofStreets,playerfigurese)
        // readd (...)
        // updatePlayerList,updateLabels,ClearEventlist

    }

    def onStartGame() = {
        GameStates.handle(InitGameEvent())
        notifyObservers(OpenGetPlayersDialogEvent())
        notifyObservers(ClearGuiElementsEvent())
        notifyObservers(UpdateListViewPlayersEvent())
        GameStates.handle(getPlayersEvent())
        GameStates.handle(createBoardAndPlayersEvent())
        notifyObservers(printEverythingEvent())
        notifyObservers(displayRollForPositionsEvent())
        GameStates.handle(rollForPositionsEvent())
        // Enable roll and disable endturn button
        val rollDiceBUtton = currentStage.scene().lookup("#rollDice") //.asInstanceOf[javafx.scene.control.Button]
        rollDiceBUtton.setDisable(false)
        val endTurnButton = currentStage.scene().lookup("#endTurn") //.asInstanceOf[javafx.scene.control.Button]
        endTurnButton.setDisable(true)

        //        do {

        //            GameStates.handle(checkGameOverEvent())
        //        } while (!gameOver)
        //        GameStates.handle(gameOverEvent())
        //GameStates.handle(InitGameEvent())
    }

    def onRollDice() = {
        players(currentPlayer).strategy.execute("rollDice") match {
            case (roll1: Int, roll2: Int, pasch: Boolean) => println(roll1, roll2, pasch)
                if (pasch) {
                    paschCount += 1
                    if (players(currentPlayer).jailCount > 0) {
                        players = players.updated(currentPlayer, players(currentPlayer).resetJailCount)
                        notifyObservers(OpenPlayerFreeDialog())
                    }
                } else {
                    // Disable rollbutton if no pasch and enable endturn button
                    val rollDiceBUtton = currentStage.scene().lookup("#rollDice") //.asInstanceOf[javafx.scene.control.Button]
                    rollDiceBUtton.setDisable(true)
                    val endTurnButton = currentStage.scene().lookup("#endTurn") //.asInstanceOf[javafx.scene.control.Button]
                    endTurnButton.setDisable(false)
                }
                //move to jail
                if (paschCount == 2) { //todo 3
                    players = players.updated(currentPlayer, players(currentPlayer).moveToJail)
                    players = players.updated(currentPlayer, players(currentPlayer).incJailTime)
                    notifyObservers(MovePlayerFigureEvent(players(currentPlayer).figure, -350, 350)) // jailxy
                    notifyObservers(openGoToJailPaschDialog())
                    val rollDiceBUtton = currentStage.scene().lookup("#rollDice") //.asInstanceOf[javafx.scene.control.Button]
                    rollDiceBUtton.setDisable(true)
                    val endTurnButton = currentStage.scene().lookup("#endTurn") //.asInstanceOf[javafx.scene.control.Button]
                    endTurnButton.setDisable(false)
                } else {
                    // nur wenn player nicht im jail
                    if (players(currentPlayer).jailCount == 0) players = playerController.movePlayer(roll1 + roll2)
                }
        }
        notifyObservers(UpdateListViewPlayersEvent())

    }

    def onEndTurn() = {
        var gameOver = false
        val endTurnButton = currentStage.scene().lookup("#endTurn").asInstanceOf[javafx.scene.control.Button]
        if (endTurnButton.getText == "Declare Bankrupt") {
            endTurnButton.setText("End turn")
            if (checkGameOver) {
                GameStates.handle(gameOverEvent())
                gameOver = true
            }
        }
        if (!gameOver) {
            // todo "its player x turn" notifyObservers(OpenNextPlayersTurnDialog())
            // init next round
            paschCount = 0
            // beim neuen zug spieler die kein geld mehr haben ueberspringen
            do {
                currentPlayer += 1
                if (currentPlayer == humanPlayers + npcPlayers) {
                    currentPlayer = 0 // erster spieler ist wieder dran
                    round += 1
                    //start next round
                    notifyObservers(newRoundEvent(round))
                }
            } while (players(currentPlayer).money <= 0)

            // update round label
            val lblPlayerTurn = currentStage.scene().lookup("#lblPlayerTurn").asInstanceOf[javafx.scene.text.Text]
            lblPlayerTurn.setText("It is " + players(currentPlayer).name + "´s turn")
            // Enable rollbutton and disable endturn button
            val rollDiceBUtton = currentStage.scene().lookup("#rollDice") //.asInstanceOf[javafx.scene.control.Button]
            rollDiceBUtton.setDisable(false)
            endTurnButton.setDisable(true)
            // todo hier iwo if botplayer run bot round -> roll ->  move -> end turn
            //start next turn
            //jailtime++
            if (players(currentPlayer).jailCount > 0) players = players.updated(currentPlayer, players(currentPlayer).incJailTime)
            // frei nach 3 runden
            if (players(currentPlayer).jailCount >= 4) {
                // player is free again notifyObservers(OpenInJailDialogEvent())
                players = players.updated(currentPlayer, players(currentPlayer).resetJailCount)
                notifyObservers(OpenPlayerFreeDialog())
            }
            // wenn spieler im jail jaildialog oeffnen
            if (players(currentPlayer).jailCount > 0) notifyObservers(OpenInJailDialogEvent())
            else notifyObservers(OpenNormalTurnDialogEvent(players(currentPlayer)))
            notifyObservers(UpdateListViewPlayersEvent())
            rollDiceBUtton.requestFocus() // zum durchentern
        }
    }

    object GameStates {

        var runState = initGameState

        def handle(e: GameStateEvent): Any = {
            e match {
                case e: InitGameEvent => runState = initGameState
                case e: getPlayersEvent => runState = getPlayersState(e)
                case e: rollForPositionsEvent => runState = rollForPositionsState
                case e: createBoardAndPlayersEvent => runState = createBoardAndPlayersState
                case e: gameOverEvent => runState = gameOverState
            }
            runState
        }

        def getPlayersState(e: getPlayersEvent) = {
            // spieler mit namen einlesensr
            for (i <- 0 until humanPlayers) {
                println("Enter name player" + (currentPlayer + 1) + ":")
                notifyObservers(OpenGetNameDialogEvent(i)) // adds player in tui/gui... dialog
            }
            for (i <- 0 until npcPlayers) {
                npcNames = npcNames :+ "NPC " + (i + 1)
                //todo
            }
            //todo
            // notifyObservers(askUndoGetPlayersEvent())
            //      if (answer == "yes") {
            //        undoManager.undoStep
            //      }
        }

        def rollForPositionsState = {
            println("rollforposstate")
            for (i <- 0 until humanPlayers + npcPlayers) {
                // jeden einmal wuerfeln lassen
                currentPlayer = i
                players(currentPlayer).strategy.execute("rollForPosition") match {
                    case (roll1: Int, roll2: Int, pasch: Boolean) => println(roll1, roll2, pasch)
                        //ergebnis speichern für jeden spieler
                        players = players.updated(i, players(i).setRollForPosition(roll1 + roll2))
                }
            }
            //nach reihenfolge sortieren
            players = players.sortBy(-_.rollForPosition) // - für reversed
            //reihenfolge erstmalig festlegen
            println("spieler mit reihenfolge:")
            for (i <- 0 until humanPlayers + npcPlayers) {
                players = players.updated(i, players(i).setTurnPosition(i))
                println(players(i))
            }
            // Spieler suchen die das gleiche gewuerfelt haben
            var rolledSame: Vector[PlayerInterface] = Vector[PlayerInterface]()
            for (i <- 0 until humanPlayers + npcPlayers) {
                for (j <- (i + 1) until humanPlayers + npcPlayers) {
                    if (players(i).rollForPosition == players(j).rollForPosition) {
                        // nur die die noch nicht drinnen sind hinzufuegen
                        if (!rolledSame.exists(_.name == players(i).name)) {
                            rolledSame = rolledSame :+ players(i)
                        }
                        if (!rolledSame.exists(_.name == players(j).name)) {
                            rolledSame = rolledSame :+ players(j)
                        }

                    }
                }
            }
            println("leute die nochmal würfeln dürfen ")
            for (player <- rolledSame) println(player)
            //todo lass alle die jeweils das gleiche gewuerfelt haben
            // so lange nochmal würfeln aber nur ihre position untereinander tauschen
            // bis keiner mehr das gleiche würfelt
            //
            //            var playersRollingAgain: Vector[Player] = Vector[Player]()
            //            var playersRollingAgainPositions: Vector[Int] = Vector[Int]()
            //            //while (!(rolledSame.isempty)) { // solange es spieler gibt die das gleiche gewuerfelt haben
            //            for (i <- 2 until 12) {
            //                //jede augenzahl durchgehen
            //                for (player <- players) {
            //                    // spieler die diese augenzahl gewürfelt haben raussuchen
            //                    // auch die positionen raussuchen
            //                    // z.b. a und b haben beide 9 und wuerfeln nochmal für pos 1 und 2
            //                    if (player.rollForPosition == i) {
            //                        playersRollingAgain :+ player
            //                        playersRollingAgainPositions :+ player.turnPosition
            //                    }
            //                }
            //                // spieler nochmal so lange wuerfeln lassen bis sie nichtmehr das gleiche haben
            //                // do
            //                for (player <- playersRollingAgain) {
            //                    //todo if player is npc or not
            //                    val rollDices = playerController.wuerfeln
            //                    println(player.name + " rolled " + (rollDices._1 + rollDices._2))
            //                    //ergebnis speichern für jeden spieler
            //                    players = players.updated(i, players(i).setRollForPosition(rollDices._1 + rollDices._2))
            //                }
            //                // while not das gleiche gewuerfelt
            //                // position untereinander bestimmten
            //                //sort players nach augenzahl absteigend
            //                // positionen sollten aufsteigend sein
            //                //var i = 0 ( i ist index von spielern sortiert, der mit höchsten ist 1.)
            //                // for (position <- positions)
            //                // players(i).setTurnPosition(position) ( und bekommt die 1. position )
            //                //i += 1
            //            }
            //
            //            //} END while
            //
            //
            //            // entgueltige reihenfolge festlegen
            // todo hier ist spielinit ab jetzt eventbasiert
            notifyObservers(UpdateListViewPlayersEvent())
            val lblPlayerTurn = currentStage.scene().lookup("#lblPlayerTurn").asInstanceOf[javafx.scene.text.Text]
            lblPlayerTurn.setText("It is " + players(currentPlayer).name + "´s turn")
            val lblDiceResult = currentStage.scene().lookup("#lblDiceResult").asInstanceOf[javafx.scene.text.Text]
            lblDiceResult.setText("Result dice roll: ")
            val rollDiceBUtton = currentStage.scene().lookup("#rollDice") //.asInstanceOf[javafx.scene.control.Button]
            rollDiceBUtton.setDisable(false)
            //currentStage.scene().lookup("#endTurn")setDisable(false)
            // todo gamestart
            notifyObservers(OpenNormalTurnDialogEvent(players(currentPlayer)))

        }

        def createBoardAndPlayersState = {
            println("createboardandplayersState")
            players = playerController.createPlayers(playerNames, npcNames)
            board = boardController.createBoard
            for (i <- 0 until humanPlayers + npcPlayers) {
                val stackpane = currentStage.scene().lookup("#stackpane").asInstanceOf[javafx.scene.layout.StackPane]
                stackpane.getChildren().add(players(i).figure)
                notifyObservers(MovePlayerFigureEvent(players(i).figure, 350, 350))
            }
            //todo notifyObservers(e: GuiPutPlayersOnTheBoardEvent)
            // todo game starts here
            val lblPlayerTurn = currentStage.scene().lookup("#lblPlayerTurn").asInstanceOf[javafx.scene.text.Text]
            lblPlayerTurn.setText("Roll for positions")
            val lblDiceResult = currentStage.scene().lookup("#lblDiceResult").asInstanceOf[javafx.scene.text.Text]
            lblDiceResult.setText("Result dice roll: ")
        }

        def gameOverState = {
            val rollDiceButton = currentStage.scene().lookup("#rollDice")
            rollDiceButton.setDisable(true) //.asInstanceOf[javafx.scene.control.Button]
            val endTurnButton = currentStage.scene().lookup("#endTurn") //.asInstanceOf[javafx.scene.control.Button]
            endTurnButton.setDisable(true)
            notifyObservers(openGameOverDialogEvent())
            val lblPlayerTurn = currentStage.scene().lookup("#lblPlayerTurn").asInstanceOf[javafx.scene.text.Text]
            lblPlayerTurn.setText("Game Over")
        }

        def initGameState: Unit = {
            humanPlayers = 0
            npcPlayers = 0
            board = Vector[Cell]()
            players = Vector[Player]()
            playerNames = Vector[String]()
            remainingFiguresToPick = List[String]("Fingerhut",
                "Hut",
                "Schubkarre",
                "Schuh",
                "Hund",
                "Auto",
                "Bügeleisen",
                "Schiff"
            )
            playerFigures = Vector[String]()
            npcNames = Vector[String]()
            round = 1
            answer = ""
            // delete everything on the board and board
            val stackpane = currentStage.scene().lookup("#stackpane").asInstanceOf[javafx.scene.layout.StackPane]
            stackpane.getChildren().removeAll()
        }

    }


}