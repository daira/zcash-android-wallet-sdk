package cash.z.ecc.android.sdk.model

import cash.z.ecc.android.sdk.internal.jni.RustBackend
import cash.z.ecc.android.sdk.internal.model.JniUnifiedSpendingKey

/**
 * A [ZIP 316](https://zips.z.cash/zip-0316) Unified Spending Key.
 *
 * This is the spend authority for an account under the wallet's seed.
 *
 * An instance of this class contains all of the per-pool spending keys that could be
 * derived at the time of its creation. As such, it is not suitable for long-term storage,
 * export/import, or backup purposes.
 */
class UnifiedSpendingKey private constructor(
    /**
     * The [ZIP 32](https://zips.z.cash/zip-0032) account index used to derive this key.
     */
    val account: Account,
    /**
     * The binary encoding of the [ZIP 316](https://zips.z.cash/zip-0316) Unified Spending
     * Key for [account].
     *
     * This encoding **MUST NOT** be exposed to users. It is an internal encoding that is
     * inherently unstable, and only intended to be passed between the SDK and the storage
     * backend. Wallets **MUST NOT** allow this encoding to be exported or imported.
     */
    private val bytes: FirstClassByteArray
) {
    internal constructor(uskJni: JniUnifiedSpendingKey) : this(
        Account(uskJni.account),
        FirstClassByteArray(uskJni.bytes.copyOf())
    )

    /**
     * The binary encoding of the [ZIP 316](https://zips.z.cash/zip-0316) Unified Spending
     * Key for [account].
     *
     * This encoding **MUST NOT** be exposed to users. It is an internal encoding that is
     * inherently unstable, and only intended to be passed between the SDK and the storage
     * backend. Wallets **MUST NOT** allow this encoding to be exported or imported.
     */
    fun copyBytes() = bytes.byteArray.copyOf()

    // Override to prevent leaking key to logs
    override fun toString() = "UnifiedSpendingKey(account=$account)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnifiedSpendingKey

        if (account != other.account) return false
        if (bytes != other.bytes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + bytes.hashCode()
        return result
    }

    companion object {
        /**
         * This method may fail if the [bytes] no longer represent a valid key.  A key could become invalid due to
         * network upgrades or other internal changes.  If a non-successful result is returned, clients are expected
         * to use [DerivationTool.deriveUnifiedSpendingKey] to regenerate the key from the seed.
         *
         * @return A validated UnifiedSpendingKey.
         */
        suspend fun new(
            account: Account,
            bytes: ByteArray
        ): Result<UnifiedSpendingKey> {
            val bytesCopy = bytes.copyOf()
            RustBackend.loadLibrary()
            return runCatching {
                require(RustBackend.validateUnifiedSpendingKey(bytesCopy))
                UnifiedSpendingKey(account, FirstClassByteArray(bytesCopy))
            }
        }
    }
}
