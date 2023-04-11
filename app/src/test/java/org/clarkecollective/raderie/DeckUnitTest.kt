package org.clarkecollective.raderie

import org.clarkecollective.raderie.models.HumanValue
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DeckUnitTest {
  val repo = ValueRepo()

  val localList = listOf(
    HumanValue(id = 0, gamesPlayed=7, gamesWon=4 ),
    HumanValue(id = 1, gamesPlayed=1, gamesWon=0 ),
    HumanValue(id = 2, gamesPlayed=6, gamesWon=3 ),
    HumanValue(id = 3, gamesPlayed=12, gamesWon=12)
  )

  val remoteList = listOf(
    HumanValue(id = 0, gamesPlayed=12, gamesWon=7),
    HumanValue(id = 1, gamesPlayed=1, gamesWon=0 ),
    HumanValue(id = 2, gamesPlayed=6, gamesWon=3),
    HumanValue(id = 3, gamesPlayed=12, gamesWon=12),
    HumanValue(id = 4, gamesPlayed=0, gamesWon=0)
  )

  @Test
  fun listMerge() {
    val mergedList = (localList + remoteList)
      .groupBy { humanValue -> humanValue.id }
      .values.map { it.maxByOrNull { it.gamesPlayed }!! }

    println(mergedList)
    assertEquals(0, (mergedList.size - mergedList.distinctBy { it.id }.size))
    assertEquals(5, mergedList.size)
  }

//  fun drawTwoAlgo1(deck: List<HumanValue>, value1: HumanValue, value2: HumanValue): Pair<HumanValue, HumanValue> {
//    deck.filter { it.id == value1.id }[0].gamesPlayed++
//    deck.filter { it.id == value2.id }[0].gamesPlayed++
//
//    if (value1.gamesPlayed > 5) {
//    var v1 = deck.random()
//    }
//    else { var v1 = value1 }
//    var v2 = deck.random()
//
//    return Pair(v1, v2)
//  }
//
//  @Test
//  fun playForMaxGamesPlayed() {
//    val deck = repo.freshDeck()
//    var v1 = deck[0]
//    var v2 = deck[1]
//
//    drawTwoAlgo1(deck, v1, v2)
//  }
}