package org.clarkecollective.raderie.utils

import org.clarkecollective.raderie.models.HumanValue
import kotlin.math.pow
import kotlin.math.roundToInt

// Function to calculate the Probability

class RankingCalculator {
  //TODO make this calculate both players
  fun calculateNewRanking(winner: HumanValue, loser: HumanValue, tie: Boolean): Pair<Int, Int>{

    val exponent = (loser.rating - winner.rating) / 20
    val expectedOutcome = 1 / (1 + 10.0.pow(exponent))

    val k = 32
    val winnerMod: Double = if (tie) .5 else 1.0
    val loserMod: Double = if (tie) .5 else 0.0

    return Pair((loser.rating + k * (winnerMod - expectedOutcome).roundToInt()) , (loser.rating + k * (loserMod - expectedOutcome)).roundToInt())

  }
}