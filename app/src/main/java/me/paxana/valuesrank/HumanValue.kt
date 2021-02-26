package me.paxana.valuesrank

data class HumanValue(val id: Int, val name: String, var gamesPlayed: Int, var gamesWon: Int, var gamesLost: Int, var gamesTied: Int, var rating: Int, var kFactor: Double) {
}