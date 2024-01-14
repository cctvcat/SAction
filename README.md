# SAction

> SAction 是一种极为强大的上古传承秘法。
> 该宝术源于远古时期的超级凶兽 app_process ，它融合了鲲之广大无边与鹏之疾速凌厉的特点，拥有翻江倒海、翱翔九天之威能。
> 修炼此宝术者，能够借力于 Android 世界的天地自然，变化无穷，既能化为巨大的鲲鱼沉潜深渊，又能化身为鹏鸟展翅翱翔，其力量和速度都达到了极致。
> 此外，修炼者需心怀正义，不宜有邪恶阴毒想法。若心术不正者强行修炼次术必将遭到邪念反噬，修为尽毁。
>
> 
>
> ps：凶兽app_process虽不及原始古兽su那般强悍，也媲美不了xposed妖兽有着无尽的鬼魅手段，但在这大道残缺不全、被分为九天十地的Android世界中仍排得上十大凶兽名号，可见其实力非凡。



## 如何使用

**1.在 *settings.gradle* 中添加依赖源**

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

   

**2.添加依赖**

当前版本：![img](https://jitpack.io/v/cctvcat/saction.svg)

```groovy
dependencies {
    implementation 'com.github.cctvcat:saction:<version>'
}
```



**3.定义Entry类**

在这里使用某些系统隐藏的 api 和注册自己的 binder

```kotlin
class SaEntry : ISaEntry {

    override fun onBeforeConnect(register: SaBinderRegister, args: Array<String>) {
        register.registerBinder("TestService", TestService())
        Log.d("S_ACTION_LOG", "onBeforeConnect: registered")
    }

    override fun onConnected(register: SaBinderRegister, args: Array<String>) {
        Log.d("S_ACTION_LOG", "onConnected")
    }

}

class TestService : ITestService.Stub() {

    override fun getUserIds(): MutableList<Int> {
        return UserManagerApis
            .getUserIdsNoThrow(false, false, false)
            .toMutableList()
    }

}
```

   

**4.启动服务**

首先在 ***AndroidManifest.xml*** 中注册一个provider

```xml
<provider
    android:name="cctvcat.saction.SaProvider"
    android:authorities="${applicationId}.saction"
    android:enabled="true"
    android:exported="true"
    android:multiprocess="false"
    android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" 
/>
```



唤起服务进程

```kotlin
//使用root权限唤起
SAction.start(
    packageName = "cctvcat.saction.demo_app", //应用包名
    packageClassPath = packageCodePath, //应用的packageCodePath
    invokeClassName = SaEntry::class.java.name, //你定义的entry完整类名
    version = "1.0.0", //版本号，用于版本控制
    args = arrayOf("1", "2", "3"), //可选，传递参数，类型为字符串数组，可以在entry中接收
    processNameSuffix = "s_action_process", //可选，服务进程后缀名称，默认为s_action_process
    userId = 0, //可选，用户空间ID，默认为0
)

//shizuku-api在13.1.1及以上的，由于移除了Shizuku.newProcess，请使用Shizuku.UserServiceArgs相关内容
//Shizuku.UserServiceArgs自带daemon，实现类似于SAction的操作
//使用shizuku唤起，shizuku-api版本在13.1.0及以下
val cmd = SAction.getStartCommand(
    packageName = "cctvcat.saction.demo_app",
    packageClassPath = packageCodePath,
    invokeClassName = SaEntry::class.java.name,
    version = "1.0.0",
    args = arrayOf("1", "2", "3"),
    processNameSuffix = "s_action_process",
    userId = 0,
)
val ps = Shizuku.newProcess(arrayOf("sh"), null, null)
val os = DataOutputStream(ps.outputStream)
os.writeBytes(cmd)
os.writeBytes("exit\n")
os.flush()
os.close()
ps.waitFor()
return ps.exitValue() == 0
```



SAction 操作

```kotlin
//判断服务是否存活，返回值：Boolean
SAction.isAlive()

//退出服务
SAction.exit()

//获取服务版本，返回值String?，若服务存在，则返回注册时传递的version，不存在则返回null
SAction.getVersion()

//获取注册的Binder
SAction.getRegisterBinder(key: String)
//例子：
ITestService.Stub.asInterface(SAction.getRegisterBinder("TestService"))

//参考前面唤起服务进程内容
SAction.start(...)
SAction.getStartCommand(...)
```

