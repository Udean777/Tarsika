package com.ssajudn.tarsika.feature.gallery.data.local

import android.content.ContentResolver
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.ssajudn.tarsika.feature.gallery.domain.model.DecryptedVaultPhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.VaultPhoto
import com.ssajudn.tarsika.feature.gallery.domain.repository.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidEncryptedVaultRepository(
    private val resolver: ContentResolver,
    private val dao: VaultPhotoDao,
    private val vaultDirectory: File,
) : VaultRepository {
    override val photos: Flow<List<VaultPhoto>> =
        dao.observeAll().map {
                rows ->
            rows.map { VaultPhoto(it.id, it.sizeBytes, it.createdAtMillis) }
        }

    init {
        vaultDirectory.mkdirs()
    }

    override fun ensureKey() {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (store.containsAlias(KEY_ALIAS)) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            throw IllegalStateException("Vault requires Android 11 or later.")
        }
        generateKeyForAndroidRAndLater()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun generateKeyForAndroidRAndLater() {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(
                    KEY_AUTH_TIMEOUT_SECONDS,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL,
                ).build(),
        )
        generator.generateKey()
    }

    override suspend fun import(
        sourceUri: String,
        name: String,
        sizeBytes: Long,
    ) = withContext(Dispatchers.IO) {
        require(sizeBytes <= 0L || sizeBytes <= MAX_ITEM_BYTES) { "This image is larger than the vault import limit." }
        val uri = sourceUri.toUri()
        ensureKey()
        val input = resolver.openInputStream(uri) ?: error("The selected image is no longer available.")
        val id = UUID.randomUUID().toString()
        val target = File(vaultDirectory, "$id.vault")
        try {
            var actualSizeBytes = 0L
            input.use { source ->
                val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
                target.outputStream().use { raw ->
                    raw.write(cipher.iv)
                    CipherOutputStream(raw, cipher).use { encrypted ->
                        DataOutputStream(encrypted).use { output ->
                            output.writeUTF(name.take(MAX_NAME_CHARS))
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (true) {
                                val read = source.read(buffer)
                                if (read < 0) break
                                actualSizeBytes += read
                                require(actualSizeBytes <= MAX_ITEM_BYTES) { "This image is larger than the vault import limit." }
                                output.write(buffer, 0, read)
                            }
                        }
                    }
                }
            }
            dao.insert(VaultPhotoEntity(id, actualSizeBytes, System.currentTimeMillis()))
        } catch (error: Throwable) {
            target.delete()
            throw error
        }
    }

    override suspend fun read(id: String): DecryptedVaultPhoto =
        withContext(Dispatchers.IO) {
            val bytes = vaultFile(id).readBytes()
            require(bytes.size > IV_LENGTH) { "Vault item is incomplete." }
            val iv = bytes.copyOfRange(0, IV_LENGTH)
            val cipher =
                Cipher.getInstance(
                    TRANSFORMATION,
                ).apply { init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv)) }
            val input = CipherInputStream(ByteArrayInputStream(bytes, IV_LENGTH, bytes.size - IV_LENGTH), cipher)
            input.use { decrypted ->
                DataInputStream(decrypted).use { data ->
                    val name = data.readUTF()
                    val content =
                        ByteArrayOutputStream().use { output ->
                            data.copyTo(output)
                            output.toByteArray()
                        }
                    DecryptedVaultPhoto(id, name, content)
                }
            }
        }

    override suspend fun delete(id: String) =
        withContext(Dispatchers.IO) {
            dao.delete(id)
            vaultFile(id).delete()
            Unit
        }

    override suspend fun writePlainTo(
        id: String,
        output: java.io.OutputStream,
        maxBytes: Long,
    ) = withContext(Dispatchers.IO) {
        val decrypted = read(id)
        require(decrypted.bytes.size.toLong() <= maxBytes) { "This image exceeds the available export limit." }
        output.use { it.write(decrypted.bytes) }
    }

    override fun estimatedBytes(): Flow<Long> = dao.observeAll().map { rows -> rows.sumOf { it.sizeBytes } }

    private fun secretKey(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return store.getKey(KEY_ALIAS, null) as? SecretKey ?: error("Vault key is unavailable. Recreate the vault key to continue.")
    }

    private fun vaultFile(id: String): File {
        require(runCatching { UUID.fromString(id) }.isSuccess) { "Invalid vault item id." }
        return File(vaultDirectory, "$id.vault")
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "tarsika_hidden_album_aes_gcm_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
        const val TAG_LENGTH_BITS = 128
        const val KEY_AUTH_TIMEOUT_SECONDS = 300
        const val MAX_NAME_CHARS = 255
        const val MAX_ITEM_BYTES = 100L * 1024L * 1024L
    }
}
