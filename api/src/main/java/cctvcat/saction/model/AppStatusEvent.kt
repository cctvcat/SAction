package cctvcat.saction.model

data class AppStatusEvent(
    val type: String,
    val uid: Int,
    val pid: Int = 0,
    val args: Array<Any> = arrayOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppStatusEvent

        if (type != other.type) return false
        if (uid != other.uid) return false
        if (pid != other.pid) return false
        if (!args.contentEquals(other.args)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + uid
        result = 31 * result + pid
        result = 31 * result + args.contentHashCode()
        return result
    }
}