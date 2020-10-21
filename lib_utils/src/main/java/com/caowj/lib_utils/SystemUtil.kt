package com.caowj.lib_utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.DeviceAdminReceiver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import java.io.File
import java.net.NetworkInterface
import java.util.*


/**
 * 平台有关信息，例如：density，分辨率等信息。
 * 包含：
 * 1. 密度density；
 * 2. Android平台ActionBar高度；
 * 3. Android平台状态栏(StatusBar)高度；
 */
object SystemUtil {
    /**
     * 密度比
     */
    @JvmField
    val density = Resources.getSystem().displayMetrics.density

    @JvmField
    val densityDpi = Resources.getSystem().displayMetrics.densityDpi

    private val ANDROID_ID = "kedacom_uuid.m4a"
    private val ANDROID_ID_PATH= "/sdcard/.kedacom_uuid"

    /**
     * 获取系统版本名——用户可见的系统版本。例如：P2-V0.46。
     */
    @JvmStatic
    fun getVersionName(): String {
        return Build.DISPLAY
    }

    /**
     * 获取系统Android版本，例如： Android 9。
     *
     * @return 设备使用的Android版本
     */
    @JvmStatic
    fun getRelease(): String {
        return Build.VERSION.RELEASE
    }

    /**
     * 基带版本
     *
     * @return 基带版本
     */
    @JvmStatic
    fun getRadioVersion(): String {
        return Build.getRadioVersion()
    }

    /**
     * 终端用户型号，例如：P2。
     *
     * @return 获取终端用户型号
     */
    @JvmStatic
    fun getModel(): String {
        return Build.MODEL
    }

    /**
     * 获取设备名，例如：ptwo。
     *
     * @return 设备名
     */
    @JvmStatic
    fun getDeviceName(): String {
        return Build.DEVICE
    }

    /**
     * 获取设备屏幕分辨率。
     * 宽度高度相对于横竖屏有关。
     *
     * 例如：1080p手机，竖屏：width=1080, height=1920
     *                  横屏：width=1920，height=1080
     *
     * @return 返回大小为2的数组，索引0为宽度，1为高度
     */
    @JvmStatic
    fun getScreenPixels(): Array<Int?> {
        val dimension = arrayOfNulls<Int>(2)
        val displayMetrics = Resources.getSystem().displayMetrics
        dimension[0] = displayMetrics.widthPixels
        dimension[1] = displayMetrics.heightPixels
        return dimension
    }

    /**
     * 获取设备IMEI。设备若有两个或多个卡槽，IMEI可能有多个。
     *
     * @param context 上下文对象
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getIMEI(context: Context): Array<String?> {
        val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // api >= 23
            val phoneCount = telephonyManager.phoneCount // api >= 23
//            println("count of phone: $phoneCount")
            if (phoneCount == 0) {
                return emptyArray()
            }
            val imeiArray = arrayOfNulls<String>(phoneCount)
//            println("phoneType: ${telephonyManager.phoneType}")
            if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val listImei = arrayListOf<String>()
                val versionInfo =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) "Equal and Above Android 8.0" else "Blow Android 8.0"
//                println(versionInfo)
                for (slot in 0 until phoneCount) {
                    val valImei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        telephonyManager.getImei(slot)
                    } else {
                        @Suppress("DEPRECATION")
                        telephonyManager.getDeviceId(slot)
                    }
                    listImei.add(valImei)
                }
                listImei.toArray(imeiArray)
            } else {
                Log.w("SystemUtil", "Permission READ_PHONE_STATE is not granted!!!")
            }
            imeiArray
        } else {
//            println("Blow Android 6.0")
            val imeiArray = arrayOfNulls<String>(1)
            @Suppress("DEPRECATION")
            imeiArray[0] = telephonyManager.deviceId
            imeiArray
        }
    }

    /**
     * 获取MEID(Mobile Equipment Identifier)值——移动设备识别码
     *
     * 此方法需要在Android 8.0设备上运行
     *
     * @param context 上下文对象
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getMEID(context: Context): Array<String?> {
        val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // api >= 26
            val phoneCount = telephonyManager.phoneCount
//            println("count of phone: $phoneCount")
            if (phoneCount == 0) {
                return emptyArray()
            }
            val meidArray = arrayOfNulls<String>(phoneCount)
//            println("phoneType: ${telephonyManager.phoneType}")
            if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                val versionInfo =
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) "Equal and Above Android 8.0" else "Blow Android 8.0"
//                println(versionInfo)
                val listMeid = arrayListOf<String>()
                for (slot in 0 until phoneCount) {
                    listMeid.add(telephonyManager.getMeid(slot))
                }
                listMeid.toArray(meidArray)
            } else {
                Log.w("SystemUtil", "Permission READ_PHONE_STATE is not granted!!!")
            }
            meidArray
        } else emptyArray()
    }

    /**
     * 获取应用层唯一的identity，用于app进行接口请求时传入些唯一值。
     *
     * 在Android ApiLevel 26以下，直接返回deviceId，在26及以上时，优选获取ANDROID_ID，无法获取成功的前提下
     * 生成UUID作为标识码。
     *
     * ANDROID_ID发生改变的情况：
     * 1. 设备恢复出厂设置；
     * 2. 在设备系统版本在8.0之前，ANDROID_ID在设备系统升级到8.0及以上设备时不发生改变，但在app卸载后重新再安装后改变；
     * 3. 设备系统在8.0以上，app签名的不同会导致ANDROID_ID。
     *
     * GUID(生成的UUID)：
     * 在无法正常获取到ANDROID_ID值前提下，生成UUID，并将其保存在路径 app_process_name/files/deviceId/ 下，正常
     * 使用情况下不会发生改变，app清理缓存时不会被移除。只在清理数据的情况下，会随账户的数据被一同清理掉。
     *
     *
     * 方法不作为获取设备唯一标识码的方法。获得的标识码可以用于请求服务器token使用。
     *
     * @param context 上下文对象
     *
     * @return 得到的标识码
     */
    @JvmStatic
    @SuppressLint("MissingPermission", "HardwareIds")
    fun retrieveIdentifier(context: Context): String {
        val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                telephonyManager.deviceId
            } else {
                retrieveIdentifierAbove26(context)
            }
        }
        return ""
    }

    /**
     * 在apiLevel>=26(Android 8)上获取唯一identifier。
     *
     * 在Android 10上，因此安全限制 Environment.getExternalStorageDirectory() 已经过时，
     * 无法在根目录下进行静默的文件创建。
     *
     * @param context 上下文对象
     *
     * @return 得到的identifier
     */
    @SuppressLint("HardwareIds")
    private fun retrieveIdentifierAbove26(context: Context): String {
        var resultId = ""
        if (Build.VERSION.SDK_INT < 29) {
            try {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                resultId = when (telephonyManager.networkType) {
                    TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> getMEID(context)[0] ?: ""
                    else -> getIMEI(context)[0] ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (resultId.isEmpty()) {
            //借助MediaStore接口，将唯一标识号伪装成图片公共文件
            resultId = getUUIDKey(context)
            if (resultId.isEmpty()) {
                resultId = UUID.randomUUID().toString()
                saveUUIDKey(context, resultId)
            }
        }

        return resultId
    }

    private fun saveUUIDKey(context: Context, key: String) {
        // 如果ANDROID Q支持 sdcard根目录写，则将key存储到sdcard根目录下
        try {
            if(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED){
                var file = File(ANDROID_ID_PATH)
                if(!file.exists()){
                   file.createNewFile()
                }
                file.writeText(key)
                return
            }
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }


        try {
            var contentValues = ContentValues()
            contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, ANDROID_ID)
            contentValues.put(MediaStore.Audio.Media.TITLE, ANDROID_ID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_NOTIFICATIONS + "/kedacom")
            } else {
                contentValues.put(MediaStore.Audio.Media.DATA, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path)
            }
            contentValues.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4a-latm")
            val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    outputStream.write(key.toByteArray())
                    outputStream.flush()
                    outputStream.close()
                }

                var cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Audio.Media.RELATIVE_PATH), "", null, null, null)
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        var index = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                        if (index > -1) {
                            var path = cursor.getString(index)

                          var file =   File( Environment.getExternalStorageDirectory(),path+".nomedia");
                            if(!file.exists()){
                                file.createNewFile()
                            }
                        }
                    }
                    cursor.close()
                }
            }else{
//                println("insert failed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getUUIDKey(context: Context): String {
        // 如果ANDROID Q支持 sdcard根目录写，则将key存储到sdcard根目录下
        try {
            if(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED){
                var file = File(ANDROID_ID_PATH)
                if(file.exists()){
                   return file.readText()
                }
            }
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }


        try {
            val queryPathKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.RELATIVE_PATH else MediaStore.Audio.Media.DATA
            var cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Media._ID, queryPathKey, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.DISPLAY_NAME),
                    null,
                    arrayOf(),
                    null)
            if(cursor == null){
//                println("cursor null")
            }

            cursor?.let {
                if (cursor.moveToFirst()) {
                    do {
                        val id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                        val name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))

//                        println("cursor name :" + name)
                        if (ANDROID_ID == name) {
                            //根据图片id获取uri，这里的操作是拼接uri
                            val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id)
                            //val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toLong())
                            if (uri != null) {
                                //通过流转化成bitmap对象
                                val inputStream = context.contentResolver.openInputStream(uri)
                                return String(inputStream!!.readBytes())
                            }
                        }

                    } while (cursor.moveToNext())
                }else{
//                    println("getUUIDKey,cursor not found")
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    /**
     * 获取制造商信息。
     *
     * @return 制造商信息
     */
    @JvmStatic
    fun getManufacturer(): String {
        return Build.MANUFACTURER
    }

    /**
     * 获取序列号
     *
     * @return 序列号
     */
    @Suppress("DEPRECATION")
    @SuppressLint("HardwareIds", "MissingPermission")
    @JvmStatic
    fun getSerial(): String {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) Build.SERIAL else Build.getSerial()
    }

    /**
     * 获取设备IP地址。
     *
     * @param context 上下文对象
     * @return 返回IP地址数组，索引0位置标示IPv6表示，索引1位置为IPv4表示。若无网络返回null
     */
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getIPs(context: Context): Array<String?>? {
        val connMgr =
                context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> { // 6.0 getActiveNetwork
                val lp = connMgr.getLinkProperties(connMgr.activeNetwork)
                return formatIpAddresses(lp)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val networks = connMgr.allNetworks
                networks.forEach { network ->
                    val info = connMgr.getNetworkInfo(network)
                    if (info.isAvailable && info.isConnected) {
                        val lp = connMgr.getLinkProperties(network)
                        return formatIpAddresses(lp)
                    }
                }
            }

            else -> {
                val networks = NetworkInterface.getNetworkInterfaces().toList()
                return formatIpAddresses(networks)
            }
        }
        return null
    }

    /**
     * 格式化IP地址，此方法在API 21以下调用。
     *
     * @param networks 获取NetworkInterface列表
     *
     * @return 获取格式化后的IP地址
     */
    @JvmStatic
    private fun formatIpAddresses(networks: List<NetworkInterface>): Array<String?>? {
        val ipList = mutableListOf<String?>()
        networks.forEach { network ->
            val netAddresses = network.inetAddresses.toList()
            netAddresses.forEach flagAddresses@{ address ->
                if (!address.isLoopbackAddress) {
                    val hostIp = address.hostAddress
                    if (hostIp.indexOf("%", 0, true) != -1) {
                        ipList.add(hostIp.split("%")[0])
                        return@flagAddresses
                    }
                    ipList.add(hostIp)
                }
            }
        }
        return ipList.toTypedArray()
    }

    /**
     * 格式化IP地址，此方法在API 21(含21)以上调用。
     *
     * @param prop 链接属性
     *
     * @return 获取格式化后的IP地址
     */
    @JvmStatic
    private fun formatIpAddresses(prop: LinkProperties?): Array<String?>? {
        return prop?.let {
            val ipAddressArray = mutableListOf<String?>()
            val iter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.linkAddresses.iterator()
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP")
            }
            // If there are no entries, return null
            if (!iter.hasNext()) return null
            while (iter.hasNext()) {
                ipAddressArray.add(iter.next().address.hostAddress)
            }
            ipAddressArray.toTypedArray()
        }
    }

    /**
     * 获取设备MAC地址。
     * 从6.0开始通过调用WifiInfo.getMacAddress()和BluetoothAdapter.getAddress()方法获取到值会是02:00:00:00:00:00。
     *
     * 在设备4.4.2， 5.1.1， 7.1.2测试有效，在9.0设备有所改变，待测试。
     *
     * @param context 上下文对象
     *
     * @return mac地址
     */
    @SuppressLint("HardwareIds", "DefaultLocale")
    @JvmStatic
    fun getMac(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val admin = DeviceAdminReceiver()
            val devicePolicyMgr = admin.getManager(context.applicationContext)
            val componentName = admin.getWho(context.applicationContext)
            if (devicePolicyMgr.isAdminActive(componentName)) {
                return devicePolicyMgr.getWifiMacAddress(componentName) ?: ""
            }
            return ""
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networks = NetworkInterface.getNetworkInterfaces().toList()
            networks.forEach { network ->
//                println("network name=> ${network.name}")
                if (TextUtils.equals(network.name, "wlan0")) {
                    val byteArray = network.hardwareAddress ?: return ""
                    val macHex = StringBuilder()
                    byteArray.forEach { b ->
                        macHex.append(String.format("%02X:", b))
                    }
                    if (macHex.isNotEmpty()) {
                        macHex.deleteCharAt(macHex.length - 1)
                    }
                    return macHex.toString().toLowerCase()
                }
            }
        }

        val wifiMgr =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiMgr.connectionInfo.macAddress.toLowerCase()
    }

    /**
     * 获取SIM卡序列码。当设备中没有SIM卡时，返回Null。
     *
     * @param context 上下文对象
     *
     * @return sim卡序列码，若设备内没有sim卡，返回null
     */
    @SuppressLint("MissingPermission", "HardwareIds")
    @JvmStatic
    fun getSimSerial(context: Context): String? {
        val telMgr =
                context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telMgr.simSerialNumber
    }

    /**
     * 获取平台的ActionBar的高度。
     *
     * @return 平台ActionBar高度
     */
    @JvmStatic
    fun getActionBarHeight(context: Context): Int {
        var actionBarHeight = 0
        val tv = TypedValue()
        if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue
                    .complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
        }
        return actionBarHeight
    }

    /**
     * 获取状态栏高度。
     *
     * @param context 上下文环境变量
     * @return StatusBar高度
     */
    @JvmStatic
    fun getStatusBarHeight(context: Context): Int {
        val res = context.resources
        val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) res.getDimensionPixelSize(resourceId) else 0
    }

    /**
     * px转dp
     */
    @JvmStatic
    fun px2dp(pxValue: Float): Int {
        val scale = density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * dp转为px
     *
     * @param dipValue dp数值
     */
    @JvmStatic
    fun dp2px(dipValue: Float): Int {
        val scale = density
        return (dipValue * scale + 0.5f).toInt()
    }

    /**
     * px转sp
     */
    @JvmStatic
    fun px2sp(pxValue: Float): Int {
        val fontScale = density
        return (pxValue / fontScale + 0.5f).toInt()
    }

    /**
     * sp转px
     */
    @JvmStatic
    fun sp2px(spValue: Float): Int {
        val fontScale = density
        return (spValue * fontScale + 0.5f).toInt()
    }


    /**
     * 获取网络连接状态，返回false-未连接，true-已连接
     *
     * @param connectivityManager
     * @return
     */
//    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getNetState(connectivityManager: ConnectivityManager): Boolean {
        val activeInfo = connectivityManager.activeNetworkInfo
        return if (activeInfo != null && activeInfo.isConnected) {
            activeInfo.type == ConnectivityManager.TYPE_WIFI || activeInfo.type == ConnectivityManager.TYPE_MOBILE

        } else {
            false
        }
    }

}
