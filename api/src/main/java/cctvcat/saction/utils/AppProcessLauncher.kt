package cctvcat.saction.utils

class AppProcessLauncher(
    private val packageName: String,
    private val packageClassPath: String,
    private val className: String,
    private val processNameSuffix: String = "app_process",
    private val args: Array<String> = arrayOf(),
) {

    fun getCommandString() = "(CLASSPATH=$packageClassPath /system/bin/app_process /system/bin " +
            "--nice-name=$packageName:$processNameSuffix $className " +
            "${args.joinToString(" ")})&"

    fun launch(useRoot: Boolean = true) {
        if (useRoot) {
            ShellUtils.sudo(getCommandString(), false)
        } else {
            ShellUtils.exec(getCommandString(), false)
        }
    }

}