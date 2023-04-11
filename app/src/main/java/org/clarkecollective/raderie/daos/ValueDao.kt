package org.clarkecollective.raderie.daos

import android.database.Cursor
import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.clarkecollective.raderie.models.HumanValue

@Dao
interface ValueDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertValue(humanValue: HumanValue): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAllValues(humanValues: List<HumanValue>): Completable

  @Insert
  @JvmSuppressWildcards
  fun insertList(valueList: List<HumanValue>): Completable


  @Query("SELECT * FROM values_table")
  fun getAllValues(): Single<List<HumanValue>>

  @Query("DELETE FROM values_table")
  fun nukeValues()

  @Update
  fun updateValue(humanValue: HumanValue): Completable

  @Delete
  suspend fun deleteValue(humanValue: HumanValue)

//  @Query("SELECT * FROM values")
//  fun getAll(): List<HumanValue>
//
//  @Query("SELECT * FROM values WHERE id IN (:valueIds)")
//  fun loadAllByIds(valueIds: IntArray): List<HumanValue>
//
//  @Query("SELECT * FROM values WHERE name LIKE :name LIMIT 1")
//  fun findByName(name: String): HumanValue
//
//
//  @Delete
//  fun delete(value: HumanValue)
//
//  @Update
//  fun updateValue(value: HumanValue)

}