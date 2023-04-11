package org.clarkecollective.raderie.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "values_table")
data class HumanValue(
    @PrimaryKey var id: Int = 0,
    @ColumnInfo(name = "name") val name: String? = "error",
    @ColumnInfo(name = "games_played") var gamesPlayed: Int = 0,
    @ColumnInfo(name = "games_won") var gamesWon: Int = 0,
    @ColumnInfo(name = "games_lost") var gamesLost: Int = 0,
    @ColumnInfo(name = "games_tied") var gamesTied: Int = 0,
    @ColumnInfo(name = "rating") var rating: Int = 0,
    @ColumnInfo(name = "k_factor") var kFactor: Double = 0.0)
    :java.io.Serializable {

    companion object {
        private const val serialVersionUID = 20221128L
    }

}