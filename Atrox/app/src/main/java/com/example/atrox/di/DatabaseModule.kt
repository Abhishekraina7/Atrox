package com.example.atrox.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.atrox.data.local.db.AppDatabase
import com.example.atrox.data.local.db.MIGRATION_2_3
import com.example.atrox.data.local.db.MIGRATION_3_4
import com.example.atrox.data.local.db.MIGRATION_4_5
import com.example.atrox.data.local.db.MIGRATION_5_6
import com.example.atrox.data.local.db.MIGRATION_6_7
import com.example.atrox.data.local.db.NoteDao
import com.example.atrox.data.local.db.TaskDao
import com.example.atrox.data.local.db.DeletedItemDao
import com.example.atrox.data.local.db.PreferenceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        System.loadLibrary("sqlcipher")
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        val sharedPrefs = EncryptedSharedPreferences.create(
            context,
            "db_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        var passphrase = sharedPrefs.getString("db_passphrase", null)
        if (passphrase == null) {
            passphrase = UUID.randomUUID().toString()
            sharedPrefs.edit().putString("db_passphrase", passphrase).apply()
        }
        
        val factory = SupportOpenHelperFactory(passphrase.toByteArray())

        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "atrox_db_encrypted"
            )
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideDeletedItemDao(database: AppDatabase): DeletedItemDao {
        return database.deletedItemDao()
    }

    @Provides
    @Singleton
    fun providePreferenceDao(database: AppDatabase): PreferenceDao {
        return database.preferenceDao()
    }
}
