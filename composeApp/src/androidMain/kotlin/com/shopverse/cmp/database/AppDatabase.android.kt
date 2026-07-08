package com.shopverse.cmp.database

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Composable
actual fun getShopVerseDatabase(): AppDatabase {
    val context = LocalContext.current
    val dbFile = context.getDatabasePath(AppDatabase.DATABASE_FILE_NAME)
    return Room
        .databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath,
        )
        .setDriver(BundledSQLiteDriver())
        .build()
}
