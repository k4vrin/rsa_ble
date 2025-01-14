package dev.kavrin.rsable.domain.model


@JvmInline
value class MacAddress(val value: String) {

    init {
        require(isValid(value)) { "Invalid MAC address: $value" }
    }

    private fun isValid(macAddress: String): Boolean {
        if (macAddress.length != 17) return false

        val regex = Regex("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}\$")
        return regex.matches(macAddress)
    }

    fun uppercase(): MacAddress = MacAddress(value.uppercase())

    /**
     * Mac address without ":"
     */
    fun withoutColons(): String = value.replace(":", "")

    companion object {
        private const val TAG = "CustomMacAddress"
        val DUMMY = MacAddress("00:11:22:AA:BB:CC")

        /**
         * Factory for String Mac Address without ":"
         */
        fun create(str: String): MacAddress {
            require(str.length == 12) { "Invalid address size: $str" }
            return str.chunked(2)
                .zipWithNext { a, b -> "$a:$b" }
                .joinToString("")
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