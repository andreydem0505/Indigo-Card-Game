package indigo

import kotlin.random.Random

class CardDeck {
    private var mainDeck: MutableList<String> = mutableListOf()
    private var userDeck: MutableList<String> = mutableListOf()
    private var botDeck: MutableList<String> = mutableListOf()
    private var tableDeck: MutableList<String> = mutableListOf()
    private var userWonCards = 0
    private var botWonCards = 0
    private var userScore = 0
    private var botScore = 0
    private var botGotCardsFromTable: Boolean = false
    private var userWonLast: Boolean? = null
    var userPlayedFirst: Boolean? = null

    init {
        reset()
        tableDeck.addAll(get(4)!!)
        userDeck.addAll(get(6)!!)
        botDeck.addAll(get(6)!!)
    }

    private fun reset() {
        mainDeck.clear()
        val ranks: List<String> = listOfNotNull(
            "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"
        )
        val suits: List<String> = listOfNotNull(
            "♦", "♥", "♠", "♣"
        )
        suits.forEach { suit ->
            ranks.forEach {
                mainDeck.add("$it$suit")
            }
        }
        mainDeck.shuffle()
    }

    fun get(n: Int): List<String>? {
        if (n !in 1..52) {
            throw NumberFormatException()
        }
        if (mainDeck.size < n) {
            return null
        }
        val result = mainDeck.slice(0 until n)
        mainDeck = mainDeck.drop(n).toMutableList()
        return result
    }

    fun getTableDeck() = tableDeck

    fun getUserDeck() = userDeck

    fun getBotDeck() = botDeck

    fun countCardsInTableDeck() = tableDeck.size

    private fun countCardsInMainDeck() = mainDeck.size

    fun getTopCardInTableDeck(): String? {
        return if (countCardsInTableDeck() == 0) {
            null
        } else {
            tableDeck.last()
        }
    }

    private fun takeCardsFromTable(userWon: Boolean) {
        var score = 0
        val cards = countCardsInTableDeck()
        tableDeck.forEach {
            if (it.substring(0 until it.lastIndex) in listOf( "A", "10", "J", "Q", "K")) {
                score++
            }
        }
        tableDeck.clear()
        if (userWon) {
            userScore += score
            userWonCards += cards
        } else {
            botScore += score
            botWonCards += cards
        }
    }

    /**
     * @return true if user takes all cards from the table (else false)
     */
    fun putUserCardOnTable(n: Int): Boolean {
        val card = userDeck.removeAt(n)
        val top = getTopCardInTableDeck()
        tableDeck.add(card)
        if (userDeck.size == 0) {
            get(6)?.let { userDeck.addAll(it) }
        }
        if (top?.let { areSimilarCards(it, card) } == true) {
            takeCardsFromTable(true)
            return true
        }
        return false
    }

    fun putBotCardOnTable(): String {
        val card = if (countCardsInTableDeck() == 0) {
            getCardAmongCardsWithSameSuitsOrRanks(botDeck)
        } else {
            val candidates = getCandidates(botDeck)
            if (candidates.isNotEmpty()) {
                getCardAmongCardsWithSameSuitsOrRanks(candidates)
            } else {
                getCardAmongCardsWithSameSuitsOrRanks(botDeck)
            }
        }
        botDeck.remove(card)
        val top = getTopCardInTableDeck()
        tableDeck.add(card)
        if (botDeck.size == 0) {
            get(6)?.let { botDeck.addAll(it) }
        }
        if (top?.let { areSimilarCards(it, card) } == true) {
            takeCardsFromTable(false)
            botGotCardsFromTable = true
            return card
        }
        return card
    }

    fun isBotGotCardsFromTable(): Boolean {
        if (botGotCardsFromTable) {
            botGotCardsFromTable = false
            return true
        }
        return false
    }

    fun getStatistics() = Statistics(userScore, botScore, userWonCards, botWonCards)

    /**
     * if game is over the cards go to the player who won last
     */
    fun gameIsOver(): Boolean {
        val addExtraPoints = {
            val extraPoints = 3
            if (userWonCards > botWonCards) {
                userScore += extraPoints
            } else if (userWonCards < botWonCards) {
                botScore += extraPoints
            } else {
                if (userPlayedFirst!!) {
                    userScore += extraPoints
                } else {
                    botScore += extraPoints
                }
            }
        }
        if (countCardsInMainDeck() == 0 && userDeck.size == 0 && botDeck.size == 0) {
            if (userWonLast == null) {
                takeCardsFromTable(userPlayedFirst!!)
            } else {
                takeCardsFromTable(userWonLast!!)
            }
            addExtraPoints()
            return true
        }
        return false
    }

    private fun getCardAmongCardsWithSameSuitsOrRanks(cards: List<String>): String {
        val cardsWithSameSuit = getCardsWithSameProperty(cards) { getCardSuit(it) }
        return if (cardsWithSameSuit.isNotEmpty()) {
            getRandomCard(cardsWithSameSuit)
        } else {
            val cardsWithSameRank = getCardsWithSameProperty(cards) { getCardRank(it) }
            if (cardsWithSameRank.isNotEmpty()) {
                getRandomCard(cardsWithSameRank)
            } else {
                getRandomCard(cards)
            }
        }
    }

    private fun getCandidates(deck: List<String>): List<String> {
        val top = getTopCardInTableDeck()!!
        return deck.filter { areSimilarCards(top, it) }
    }

    private fun areSimilarCards(firstCard: String, secondCard: String) =
        getCardRank(firstCard) == getCardRank(secondCard) || getCardSuit(firstCard) == getCardSuit(secondCard)

    private fun getCardsWithSameProperty(deck: List<String>, method: (String) -> Any): List<String> {
        val properties = mutableMapOf<Any, Int>()
        var property: Any
        deck.forEach {
            property = method(it)
            if (properties.contains(property)) {
                properties[property] = properties[property]!! + 1
            } else {
                properties[property] = 1
            }
        }
        return deck.filter { properties[method(it)]!! > 1 }
    }

    private fun getRandomCard(deck: List<String>) = deck[Random.nextInt(0, deck.size)]

    private fun getCardRank(card: String) = card.substring(0 until card.lastIndex)

    private fun getCardSuit(card: String) = card.last()
}