package com.ssajudn.tarsika.feature.gallery.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_albums")
data class UserAlbumEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtMillis: Long,
    @ColumnInfo(defaultValue = "'Pictures/Tarsika/'") val relativePath: String = "Pictures/Tarsika/",
)

@Entity(
    tableName = "user_album_photos",
    primaryKeys = ["albumId", "photoKey"],
    indices = [Index("albumId"), Index("photoKey")],
    foreignKeys = [
        ForeignKey(
            entity = UserAlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class UserAlbumPhotoEntity(
    val albumId: String,
    val photoKey: String,
    val addedAtMillis: Long,
)

@Entity(tableName = "vault_photos")
data class VaultPhotoEntity(
    @PrimaryKey val id: String,
    val sizeBytes: Long,
    val createdAtMillis: Long,
)

@Entity(tableName = "local_trash")
data class LocalTrashEntity(
    @PrimaryKey val id: String,
    val originalUri: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val trashedAtMillis: Long,
    val backupFileName: String,
)

@Dao
interface UserAlbumDao {
    @Query("SELECT * FROM user_albums ORDER BY createdAtMillis DESC")
    fun observeAlbums(): Flow<List<UserAlbumEntity>>

    @Query("SELECT * FROM user_album_photos")
    fun observeMemberships(): Flow<List<UserAlbumPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAlbum(album: UserAlbumEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlbumPhotos(memberships: List<UserAlbumPhotoEntity>)

    @androidx.room.Transaction
    suspend fun insertAlbumWithPhotos(
        album: UserAlbumEntity,
        memberships: List<UserAlbumPhotoEntity>,
    ) {
        insertAlbum(album)
        insertAlbumPhotos(memberships)
    }

    @Query("UPDATE user_albums SET name = :name WHERE id = :id")
    suspend fun renameAlbum(
        id: String,
        name: String,
    )

    @Query("DELETE FROM user_albums WHERE id = :id")
    suspend fun deleteAlbum(id: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPhotos(memberships: List<UserAlbumPhotoEntity>)

    @Query("DELETE FROM user_album_photos WHERE albumId = :albumId AND photoKey IN (:photoKeys)")
    suspend fun removePhotos(
        albumId: String,
        photoKeys: List<String>,
    )
}

@Dao
interface VaultPhotoDao {
    @Query("SELECT * FROM vault_photos ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<VaultPhotoEntity>>

    @Insert
    suspend fun insert(photo: VaultPhotoEntity)

    @Query("DELETE FROM vault_photos WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface LocalTrashDao {
    @Query("SELECT * FROM local_trash ORDER BY trashedAtMillis DESC")
    fun observeAll(): Flow<List<LocalTrashEntity>>

    @Insert
    suspend fun insert(entry: LocalTrashEntity)

    @Query("SELECT * FROM local_trash WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): LocalTrashEntity?

    @Query("SELECT * FROM local_trash")
    suspend fun findAll(): List<LocalTrashEntity>

    @Query("SELECT * FROM local_trash WHERE trashedAtMillis <= :trashedBeforeMillis")
    suspend fun findExpired(trashedBeforeMillis: Long): List<LocalTrashEntity>

    @Query("DELETE FROM local_trash WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM local_trash")
    suspend fun deleteAll()

    @Query("DELETE FROM local_trash WHERE trashedAtMillis <= :trashedBeforeMillis")
    suspend fun deleteExpired(trashedBeforeMillis: Long)
}

@Database(
    entities = [UserAlbumEntity::class, UserAlbumPhotoEntity::class, VaultPhotoEntity::class, LocalTrashEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class UserAlbumDatabase : RoomDatabase() {
    abstract fun albums(): UserAlbumDao

    abstract fun vaultPhotos(): VaultPhotoDao

    abstract fun localTrash(): LocalTrashDao

    companion object {
        @Volatile
        private var instance: UserAlbumDatabase? = null

        fun get(context: Context): UserAlbumDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    UserAlbumDatabase::class.java,
                    "user_photo_albums.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `vault_photos` " +
                            "(`id` TEXT NOT NULL, `sizeBytes` INTEGER NOT NULL, " +
                            "`createdAtMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))",
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `local_trash` " +
                            "(`id` TEXT NOT NULL, `originalUri` TEXT NOT NULL, `displayName` TEXT NOT NULL, " +
                            "`mimeType` TEXT NOT NULL, `sizeBytes` INTEGER NOT NULL, `trashedAtMillis` INTEGER NOT NULL, " +
                            "`backupFileName` TEXT NOT NULL, PRIMARY KEY(`id`))",
                    )
                }
            }

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE `user_albums` ADD COLUMN `relativePath` TEXT NOT NULL DEFAULT 'Pictures/Tarsika/'",
                    )
                }
            }
    }
}
