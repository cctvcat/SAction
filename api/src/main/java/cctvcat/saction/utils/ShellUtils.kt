package cctvcat.saction.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.StringBuilder

object ShellUtils {

    fun exec(commands: Array<String>, returnResult: Boolean = true): String? {
        var ps: Process? = null
        var os: OutputStream? = null
        var reader: BufferedReader? = null
        val builder = StringBuilder()

        try {
            ps = Runtime.getRuntime().exec(commands[0])
            if (commands.size > 1) {
                os = DataOutputStream(ps.outputStream)
                for (i in 1 until commands.size) {
                    os.writeBytes("${commands[i]}\n")
                }

                os.writeBytes("exit\n")
                os.flush()
            }

            if (returnResult) {
                reader = BufferedReader(InputStreamReader(ps.inputStream))
                var line: String? = reader.readLine()
                while (line != null) {
                    builder.append(line)
                    line = reader.readLine()
                }
                reader.close()

                reader = BufferedReader(InputStreamReader(ps.errorStream))
                line = reader.readLine()
                while (line != null) {
                    builder.append(line)
                    line = reader.readLine()
                }
                reader.close()
            }

            ps.waitFor()
        } catch (e: IOException) {
            e.printStackTrace()
            if (returnResult) {
                builder.append(e.message)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            if (returnResult) {
                builder.append(e.message)
            }
        } finally {
            os?.close()
            reader?.close()
            ps?.destroy()
        }

        return if (returnResult) {
            builder.toString()
        } else {
            null
        }
    }

    fun exec(command: String, returnResult: Boolean = true): String? {
        return exec(arrayOf(command), returnResult)
    }

    fun sudo(command: String, returnResult: Boolean = true): String? {
        return exec(arrayOf("su", command), returnResult)
    }

    fun isRoot() = sudo("echo 1")!!.trim() == "1"

}