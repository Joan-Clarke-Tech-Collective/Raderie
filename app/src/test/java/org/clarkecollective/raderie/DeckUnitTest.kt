package org.clarkecollective.raderie

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

  private fun runGame(list: List<HumanValue>): List<HumanValue> {
    val drawn = repo.drawTwo(list)
    list.find { it.id == drawn[0].id }?.let {
      println("Winner: ${it.name}")
      it.gamesPlayed++
      it.gamesWon++
      println("Winner: $it")
    }
    list.find { it.id == drawn[1].id }?.let {
      it.gamesPlayed++
      it.gamesLost++
      println("Loser: $it")
    }
    return list
  }

  @Test
  fun testDeck() {
    var userOne = repo.freshDeckObject()
    var userTwo = repo.freshDeckObject()
    runBlocking {
      (0..50).map {
        async {
          println("Ran $it games")
          userOne = runGame(userOne) as ArrayList<HumanValue>
        }
         async {
           userTwo = runGame(userTwo) as ArrayList<HumanValue>
         }

      }.awaitAll()


      println("Starting job 2")
      val existing1 = userOne.filter { it.gamesPlayed > 0 }.map { it.id }
      val existing2 = userTwo.filter { it.gamesPlayed > 0 }.map { it.id }
      val mutual = existing1.intersect(existing2.toSet())
      println(mutual.size)
      println("User 1: $userOne")
      println("User 1: $userTwo")

      assertTrue(existing1.isNotEmpty())
      assertTrue(existing2.isNotEmpty())
      assertTrue(mutual.isNotEmpty())
      assertNotEquals(userOne, userTwo)
    }
  }
}