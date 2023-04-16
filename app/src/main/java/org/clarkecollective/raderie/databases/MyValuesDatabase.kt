package org.clarkecollective.raderie.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import org.clarkecollective.raderie.daos.ValueDao
import org.clarkecollective.raderie.models.HumanValue

@Database(entities = [HumanValue::class], version = 2, exportSchema = false)
abstract class MyValuesDatabase: RoomDatabase() {
  abstract fun valueDao(): ValueDao
}