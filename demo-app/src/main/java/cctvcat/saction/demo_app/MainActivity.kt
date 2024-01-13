package cctvcat.saction.demo_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import cctvcat.saction.SAction
import cctvcat.saction.demo_app.remote.RemoteService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tv_status)
        val tvOutput = findViewById<TextView>(R.id.tv_output)
        val btnStart = findViewById<Button>(R.id.btn_start)

        if (SAction.isAlive()) {
            tvStatus.text = "服务状态：开"
            btnStart.text = "关闭服务"
        } else {
            tvStatus.text = "服务状态：关"
            btnStart.text = "开启服务"
        }

        btnStart.setOnClickListener {
            if (tvStatus.text == "服务状态：关") {
                tvStatus.text = "服务状态：开启服务中"
                btnStart.text = "..."

                SAction.start(
                    packageName = "cctvcat.saction.demo_app",
                    packageClassPath = packageCodePath,
                    invokeClassName = RemoteService::class.java.name,
                    version = "1.0.0",
                    args = arrayOf("1", "2", "3")
                )

                while (!SAction.isAlive()) {
                    Thread.sleep(100)
                }

                tvStatus.text = "服务状态：开"
                btnStart.text = "关闭服务"
            } else if (tvStatus.text == "服务状态：开") {
                SAction.exit()
                tvStatus.text = "服务状态：关"
                tvOutput.text = "输出内容：无"
                btnStart.text = "开启服务"
            }
        }

        val btnLog = findViewById<Button>(R.id.btn_log)
        btnLog.setOnClickListener {
            if (SAction.isAlive()) {
                val testService = ITestService.Stub.asInterface(SAction.getRegisterBinder("TestService"))
                tvOutput.text = "用户ID：${testService.userIds.joinToString("、")}"
            }
        }

    }
}