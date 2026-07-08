package com.shopverse.cmp.database

import androidx.compose.runtime.Composable
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.shopverse.cmp.database.dao.CartItemDAO
import com.shopverse.cmp.database.model.CartItemEntity

@Database(
    version = 1,
    exportSchema = true,
    entities = [CartItemEntity::class],
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartItemDAO(): CartItemDAO

    companion object {
        const val DATABASE_FILE_NAME = "shopverse.db"
    }
}

// The Room KSP compiler generates the `actual` implementation of this object per target.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** Builds the platform database (per-target driver + file path). */
@Composable
expect fun getShopVerseDatabase(): AppDatabase
