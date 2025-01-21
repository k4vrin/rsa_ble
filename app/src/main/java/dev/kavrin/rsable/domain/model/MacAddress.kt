package dev.kavrin.rsable.domain.model


@JvmInline
value class MacAddress private constructor(val value: String) {

    init {
        require(isValid(value)) { "Invalid MAC address: $value" }
    }

    private fun isValid(macAddress: String): Boolean {
        if (macAddress.length != 17) return false

        val regex = Regex("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}\$")
        return regex.matches(macAddress)
    }

    /**
     * Mac address without ":"
     */
    fun withoutColons(): String = value.replace(":", "")

    companion object {
        private const val TAG = "CustomMacAddress"
        val DUMMY = MacAddress("00:11:22:AA:BB:CC")

        fun create(str: String): MacAddress {
            require(str.length >= 12) { "Invalid address size: $str" }
            return str
                .replace(":", "")
                .chunked(2)
                .joinToString(":")
                .uppercase()
                .let { MacAddress(it) }
        }


        @OptIn(ExperimentalStdlibApi::class)
        fun create(bytes: ByteArray): MacAddress {
            require(bytes.size == 6) { "Invalid byte array size: ${bytes.size}" }
            return bytes.toHexString()
                .uppercase()
                .chunked(2)
                .joinToString(":")
                .uppercase()
                .let { MacAddress(it) }
        }
    }

}