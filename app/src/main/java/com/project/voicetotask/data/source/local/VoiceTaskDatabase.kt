package com.project.voicetotask.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.project.voicetotask.data.source.local.dao.MeetingDao
import com.project.voicetotask.data.source.local.dao.TaskDao
import com.project.voicetotask.data.source.local.entity.MeetingEntity
import com.project.voicetotask.data.source.local.entity.MeetingFtsEntity
import com.project.voicetotask.data.source.local.entity.TaskEntity

@Database(
    entities = [MeetingEntity::class, MeetingFtsEntity::class, TaskEntity::class],
    version = 4,
    exportSchema = false
)
abstract class VoiceTaskDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
    abstract fun taskDao(): TaskDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE tasks ADD COLUMN assigneeName TEXT NOT NULL DEFAULT 'Me'"
                )
                db.execSQL(
                    "ALTER TABLE tasks ADD COLUMN dueAt INTEGER DEFAULT NULL"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE meetings ADD COLUMN isConfirmed INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE meetings ADD COLUMN summary TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE meetings ADD COLUMN decisionsText TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE meetings ADD COLUMN blockersText TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE meetings ADD COLUMN followUpsText TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
