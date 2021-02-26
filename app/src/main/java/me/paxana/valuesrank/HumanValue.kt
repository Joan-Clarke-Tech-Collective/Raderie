package me.paxana.valuesrank

data class HumanValue(val id: Int, val name: String, val gamesPlayed: Int, val gamesWon: Int, val gamesLost: Int, val gamesTied: Int, val rank: Double, val kFactor: Double) {
}