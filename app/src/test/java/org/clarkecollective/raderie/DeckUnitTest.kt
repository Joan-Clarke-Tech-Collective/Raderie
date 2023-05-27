package org.clarkecollective.raderie

import com.orhanobut.logger.Logger
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
  This test is for testing the algorithm used to pick the next card in the deck.  The goal is to get
 the most useful results for comparing to friends in the shortest amount of time
 */

class DeckUnitTest {

  private val repo1 = ValueRepo()
  private val repo2 = ValueRepo()

  private fun runGame(list: List<HumanValue>): List<HumanValue> {
    val drawn = repo1.drawTwo(list)
    list.find { it.id == drawn[0].id }?.apply {
      this.gamesPlayed++
      this.gamesWon++
      println("Winner: $this")
    }
    list.find { it.id == drawn[1].id }?.apply {
      this.gamesPlayed++
      this.gamesLost++
      println("Loser: $this")
    }
    println("Returning this $list")
    return list
  }

  private fun oldRunGame(list: List<HumanValue>): List<HumanValue> {
    val drawn = repo2.oldDrawTwo(list)
    list.find { it.id == drawn[0].id }?.apply {
      this.gamesPlayed++
      this.gamesWon++
      println("Winner: $this")
    }
    list.find { it.id == drawn[1].id }?.apply {
      this.gamesPlayed++
      this.gamesLost++
      println("Loser: $this")
    }
    println("Returning this $list")
    return list
  }

  @Test
  fun testDeck() {
    var userOne = repo1.freshDeckObject()
    var userTwo = repo2.freshDeckObject()
    var count = 0

    runBlocking {
      async {
        while ((userOne.filter { it.gamesPlayed > 0 }.map { it.id }.intersect(userTwo.filter { it.gamesPlayed > 0 }.map { it.id }.toSet())).size < 10) {
              println("Ran $count games")
              println("Running User One")
              userOne = runGame(userOne) as ArrayList<HumanValue>
              println("User One: ${userOne.filter { it.gamesPlayed > 0 }.map { it.id }}")
              println("Running User Two")
              userTwo = runGame(userTwo) as ArrayList<HumanValue>
              println("User Two: ${userTwo.filter { it.gamesPlayed > 0 }.map { it.id }}")
              count++
          }
      }
      async {
        println("Starting job 2")
        val existing1 = userOne.filter { it.gamesPlayed > 0 }.map { it.id }
        println("Existing 1: $existing1")
        val existing2 = userTwo.filter { it.gamesPlayed > 0 }.map { it.id }
        println("Existing 2: $existing2")
        val mutual = existing1.intersect(existing2.toSet())
        println("Mutual: $mutual")
        println("User 1: $userOne")
        println("User 1: $userTwo")
        println("New Count: $count")
        assertTrue(existing1.isNotEmpty())
        assertTrue(existing2.isNotEmpty())
        assertTrue(mutual.isNotEmpty())
        assertNotEquals(userOne, userTwo)
      }

    }
  }

  @Test
  fun oldTestDeck() {
    var userOne = repo1.freshDeckObject().toMutableList()
    var userTwo = repo2.freshDeckObject().toMutableList()
    var count = 0

    runBlocking {
      while ((userOne.filter { it.gamesPlayed > 0 }.map { it.id }.intersect(userTwo.filter { it.gamesPlayed > 0 }.map { it.id }.toSet())).size < 10) {
        println("Ran $count games")
        userOne = oldRunGame(userOne) as ArrayList<HumanValue>
        userTwo = oldRunGame(userTwo) as ArrayList<HumanValue>
        count++
      }

      println("Starting job 2")
      val existing1 = userOne.filter { it.gamesPlayed > 0 }.map { it.id }
      val existing2 = userTwo.filter { it.gamesPlayed > 0 }.map { it.id }
      val mutual = existing1.intersect(existing2.toSet())
      println(mutual.size)
      println("User 1: $userOne")
      println("User 1: $userTwo")
      println("Old Count: $count")

      assertTrue(existing1.isNotEmpty())
      assertTrue(existing2.isNotEmpty())
      assertTrue(mutual.isNotEmpty())
      assertNotEquals(userOne, userTwo)
    }
  }
}