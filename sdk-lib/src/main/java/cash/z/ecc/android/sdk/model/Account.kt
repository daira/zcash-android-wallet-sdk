package cash.z.ecc.android.sdk.model

/**
 * A [ZIP 32](https://zips.z.cash/zip-0032) account index.
 *
 * @param value A 0-based account index.  Must be >= 0.
 */
data class Account(val value: Int) {
    init {
        require(value >= 0) { "Account index must be >= 0 but actually is $value" }
    }

    companion object {
        val DEFAULT = Account(0)
    }
}
