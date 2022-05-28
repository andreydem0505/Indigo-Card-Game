package indigo

import kotlin.system.exitProcess

fun main() {
    val cardDeck = CardDeck()
    println("Indigo Card Game")
    var action: String
    var userTurn: Boolean
    while (true) {
        println("Play first?")
        action = readLine()!!
        if (action == "yes") {
            userTurn = true
            cardDeck.userPlayedFirst = true
            break
        } else if (action == "no") {
            userTurn = false
            cardDeck.userPlayedFirst = false
            break
        }
    }
    print("Initial cards on the table:")
    cardDeck.getTableDeck().forEach {
        print(" $it")
    }
    println()
    println()
    while (true) {
        val numberOfCardsOnTheTable = cardDeck.countCardsInTableDeck()
        println(
            if (numberOfCardsOnTheTable > 0)
                "$numberOfCardsOnTheTable cards on the table, and the top card is " +
                    cardDeck.getTopCardInTableDeck()
            else "No cards on the table"
        )
        if (cardDeck.gameIsOver()) {
            printStatistics(cardDeck)
            exit()
        }
        if (userTurn) {
            print("Cards in hand:")
            val deck = cardDeck.getUserDeck()
            for (i in 1..deck.size) {
                print(" $i)${deck[i-1]}")
            }
            println()
            var inputNumber: Int
            while (true) {
                println("Choose a card to play (1-${deck.size}):")
                action = readLine()!!
                if (action == "exit") {
                    exit()
                }
                try {
                    inputNumber = action.toInt()
                    if (inputNumber in 1..deck.size) {
                        break
                    }
                } catch (_: NumberFormatException) {}
            }
            if (cardDeck.putUserCardOnTable(inputNumber-1)) {
                println("Player wins cards")
                printStatistics(cardDeck)
            }
        } else {
            cardDeck.getBotDeck().forEach {
                print("$it ")
            }
            println()
            println("Computer plays ${cardDeck.putBotCardOnTable()}")
            if (cardDeck.isBotGotCardsFromTable()) {
                println("Computer wins cards")
                printStatistics(cardDeck)
            }
        }
        userTurn = !userTurn
        println()
    }
}

fun printStatistics(cardDeck: CardDeck) {
    val statistics = cardDeck.getStatistics()
    println("Score: Player ${statistics.userScore} - Computer ${statistics.botScore}")
    println("Cards: Player ${statistics.userCards} - Computer ${statistics.botCards}")
}

fun exit() {
    println("Game Over")
    exitProcess(0)
}