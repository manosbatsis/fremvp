package com.cyberlogitec.freight9.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import io.reactivex.Single

@Dao
interface FeaturedRouteDao {

    @Query("SELECT * FROM featured_route")
    fun loadFeaturedRoutesAll(): List<FeaturedRoute>

    @Query("SELECT * FROM featured_route ORDER BY priority")
    fun getFeaturedRouteAll() : Single<List<FeaturedRoute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(route: FeaturedRoute)

    @Insert
    fun insertAll(routes : List<FeaturedRoute>)

    @Query("DELETE FROM featured_route WHERE from_code=:fromCode AND to_code=:toCode")
    fun deleteRoute(fromCode: String, toCode: String)

    @Query("DELETE FROM featured_route WHERE id=:target_id")
    fun deleteById(target_id: Long)

    @Query("DELETE FROM featured_route")
    fun deleteAll()

    @Query("UPDATE featured_route SET priority=:priority WHERE id=:target_id")
    fun updatePriority(target_id: Int, priority: Int)
}