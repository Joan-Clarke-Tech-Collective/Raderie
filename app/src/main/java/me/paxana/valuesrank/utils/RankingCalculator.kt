package me.paxana.valuesrank.utils

import me.paxana.valuesrank.HumanValue
import kotlin.math.pow
import kotlin.math.roundToInt

class RankingCalculator {
//TODO make this calculate both players
    fun calculateNewRanking(p1: HumanValue, p2: HumanValue, outcome: String): Int{
        //Calculate Expected Outcome
        val exponent = (p2.rating - p1.rating) / 400
        val expectedOutcome = 1 / (1 + 10.0.pow(exponent))

        val actualScore: Double = when (outcome) {
            "+" -> 1.0
            "-" -> 0.0
            "=" -> 0.5
            else -> p1.rating.toDouble()
        }

        val k = 32

        return (p1.rating + k * (actualScore - expectedOutcome)).roundToInt()
    }
}