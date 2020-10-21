package com.caowj.lib_utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.Base64
import org.apache.commons.validator.routines.IntegerValidator
import org.apache.commons.validator.routines.LongValidator
import java.io.*

/**
 * 方便SharedPreferences使用的封装类。
 *
 * 根据name创建对应SharedPreferences对象及存储的文件
 *
 * @param context 上下文对象
 * @param name 用于存储的SharePreferences文件名
 */
class PreferencesHandler constructor(context: Context, name: String) : SharedPreferences.OnSharedPreferenceChangeListener {
    private var mListener: OnPreferencesChangedListener? = null

    private var mPreferences: SharedPreferences = context.applicationContext.getSharedPreferences(name, MODE_PRIVATE)

    companion object {
        private const val HINT_KEY_NULL = "key不能是空字符串"
    }

    /**
     * 保存Int值到share文件。
     *
     * @param key key值，不能是null或空字符串
     * @param value 包保存的值
     */
    @Throws(IllegalArgumentException::class)
    fun putInt(key: String, value: Int?) {
        checkNull(key)

        val editor = mPreferences.edit()
        if (value == null) {
            editor.remove(key).apply()
            return
        }
        require(IntegerValidator.getInstance().isInRange(value, Integer.MIN_VALUE, Integer.MAX_VALUE)) { "$value 超出Int可表示的范围！可尝试方法putLong(long)" }
        editor.putInt(key, value).apply()
    }

    /**
     * 获取Int值。若key不存在返回null。
     * 若share中不存在key，则返回null。
     *
     * @param key 获取value的可以值
     *
     * @return key对应的Int值，null若不包含对应key
     */
    @Throws(IllegalArgumentException::class)
    fun getInt(key: String): Int? {
        checkNull(key)

        return if (mPreferences.contains(key)) mPreferences.getInt(key, -1) else null
    }

    /**
     * 获取Int值。若key不存在返回null。
     *
     * @param key 获取value的可以值
     *
     * @return key对应的Int值，null若不包含对应key
     */
    @Throws(IllegalArgumentException::class)
    fun getInt(key: String, default: Int): Int {
        checkNull(key)

        return mPreferences.getInt(key, default)
    }

    /**
     * 保存Long值到share文件。
     *
     * @param key key值，不能是null或空字符串
     * @param value 包保存的值。传入null则删除key对应value
     */
    @Throws(IllegalArgumentException::class)
    fun putLong(key: String, value: Long?) {
        checkNull(key)

        val editor = mPreferences.edit()
        if (value == null) {
            editor.remove(key).apply()
            return
        }

        require(LongValidator.getInstance().isInRange(value, Long.MIN_VALUE, Long.MAX_VALUE)) { "$value 超出Long可表示的范围！" }
        editor.putLong(key, value).apply()
    }

    /**
     * 获取Long值。若key不存在返回null。
     * 若share中不存在key，则返回null。
     *
     * @param key 获取value的可以值
     *
     * @return key对应的Long值，null若不包含对应key
     */
    @Throws(IllegalArgumentException::class)
    fun getLong(key: String): Long? {
        checkNull(key)

        return if (mPreferences.contains(key)) mPreferences.getLong(key, -1) else null
    }

    /**
     * 获取Long值。若key不存在返回null。
     *
     * @param key 获取value的可以值
     *
     * @return key对应的Long值，null若不包含对应key
     */
    @Throws(IllegalArgumentException::class)
    fun getLong(key: String, default: Long): Long {
        checkNull(key)

        return mPreferences.getLong(key, default)
    }


    /**
     * 保存String值到Share文件。
     *
     * @param key key值，不能是null或空字符串
     * @param value 包保存的值。若存入null，则删除key对应的值
     */
    @Throws(IllegalArgumentException::class)
    fun putString(key: String, value: String?) {
        checkNull(key)

        val editor = mPreferences.edit()
        if (value == null) {
            editor.remove(key).apply()
            return
        }

        editor.putString(key, value).apply()
    }

    /**
     * 获取String值。若key不存在返回null。
     * 若share问价中不存在key，则返回null。
     *
     * @param key 获取value的可以值
     *
     * @return key对应的Long值，null若不包含对应key
     */
    @Throws(IllegalArgumentException::class)
    fun getString(key: String): String? {
        checkNull(key)

        return if (mPreferences.contains(key)) mPreferences.getString(key, "") else null
    }

    /**
     * 获取String值。若key不存在返回null。
     *
     * @param key 获取value的可以值
     *
     * @return key对应的Long值，null若不包含对应key
     */
    @Throws(IllegalArgumentException::class)
    fun getString(key: String, default: String?): String? {
        checkNull(key)

        return mPreferences.getString(key, default)
    }

    /**
     * 保存Bitmap值到share文件，bitmap会先被Base64编码值。
     *
     * @param key key值，不能是null或空字符串
     * @param bitmap 要保存的bitmap
     *
     * @throws IllegalArgumentException 获取图片的ke
     * @throws IOException 发生IO错误
     */
    @Throws(IllegalArgumentException::class)
    fun putBitmap(key: String, bitmap: Bitmap) {
        checkNull(key)

        val code = CodecUtil.base64Encode(bitmap, CodecUtil.FLAG_BASE64_DEFAULT)
        putString(key, code)
    }


    /**
     * 从Share获取保存的Bitmap。
     *
     * @param key 获取图片编码内容的key值
     *
     * @return 获取到的Bitmap或返回null若key对应的值为查找到
     *
     * @throws IllegalArgumentException 获取图片的key
     * @throws IOException 发生IO错误
     */
    @Throws(IllegalArgumentException::class, IOException::class)
    fun getBitmap(key: String): Bitmap? {
        checkNull(key)

        val code = getString(key)
        return if (code != null) CodecUtil.base64DecodeBitmap(code) else null
    }

    /**
     * 存储Boolean类型值。
     *
     * @param key 保存的key值，不能是null
     * @param value Boolean类型中
     *
     * @throws IllegalArgumentException key为空
     */
    @Throws(IllegalArgumentException::class)
    fun putBoolean(key: String, value: Boolean) {
        checkNull(key)

        mPreferences.edit().putBoolean(key, value).apply()
    }

    /**
     * 返回key对应的Boolean值，不不为null。
     * 若share中不存在key，则返回null。
     *
     * @param key 获取值的key
     *
     * @return Boolean值
     *
     * @throws IllegalArgumentException 出入key非法或为创建Preferences实例
     */
    @Throws(IllegalArgumentException::class)
    fun getBoolean(key: String): Boolean? {
        checkNull(key)

        return if (mPreferences.contains(key)) mPreferences.getBoolean(key, false) else null
    }

    /**
     * 返回key对应的Boolean值，传入默认值。
     *
     * @param key 获取值的key
     *
     * @return Boolean值
     *
     * @throws IllegalArgumentException 出入key非法或为创建Preferences实例
     */
    @Throws(IllegalArgumentException::class)
    fun getBoolean(key: String, default: Boolean): Boolean {
        checkNull(key)

        return mPreferences.getBoolean(key, default)
    }

    /**
     * 存储Float类性值。
     *
     * @param key 存储的key值
     * @param value key对应的value值
     *
     * @throws IllegalArgumentException key非法
     */
    @Throws(IllegalArgumentException::class)
    fun putFloat(key: String, value: Float) {
        checkNull(key)

        mPreferences.edit().putFloat(key, value).apply()
    }

    /**
     * 获取key对应存储的Float值。
     * 若share文件中不存在key，则返回null。
     *
     * @param key 存储的key值
     *
     * @throws IllegalArgumentException key非法
     */
    @Throws(IllegalArgumentException::class)
    fun getFloat(key: String): Float? {
        checkNull(key)

        return if (mPreferences.contains(key)) mPreferences.getFloat(key, 0.0f) else null
    }

    /**
     * 获取key对应存储的Float值。
     *
     * @param key 存储的key值
     *
     * @throws IllegalArgumentException key非法
     */
    @Throws(IllegalArgumentException::class)
    fun getFloat(key: String, default: Float): Float {
        checkNull(key)

        return mPreferences.getFloat(key, default)
    }

    /**
     * 存储Set<String>字符串集合。
     *
     * @param key 存入的key值
     * @param set String集合
     *
     * @throws IllegalArgumentException key非法
     */
    @Throws(IllegalArgumentException::class)
    fun putStringSet(key: String, set: MutableSet<String>?) {
        checkNull(key)

        mPreferences.edit().putStringSet(key, set).apply()
    }

    /**
     * 存储Set<String>字符串集合。
     * 若share中不存在key，则返回null。
     *
     * @param key 存入的key值
     *
     * @return String集合或null
     *
     * @throws IllegalArgumentException key非法
     */
    @Throws(IllegalArgumentException::class)
    fun getStringSet(key: String): MutableSet<String>? {
        checkNull(key)

        return if (mPreferences.contains(key)) mPreferences.getStringSet(key, null) else null
    }

    /**
     * 存储Set<String>字符串集合。
     *
     * @param key 存入的key值
     *
     * @return String集合或null
     *
     * @throws IllegalArgumentException key非法
     */
    @Throws(IllegalArgumentException::class)
    fun getStringSet(key: String, default: MutableSet<String>): MutableSet<String> {
        checkNull(key)

        return mPreferences.getStringSet(key, default) ?: mutableSetOf()
    }


    /**
     * 存 对象。存入的对象数据进行Base64编码存入。
     *
     * @param key 唯一key值
     * @param object 对象值，需要实现Serializable接口
     */
    @Throws(IOException::class)
    fun putObject(key: String, `object`: Serializable?) {
        checkNull(key)

        val editor = mPreferences.edit()
        if (`object` == null) {
            editor.remove(key).apply()
            return
        }

        ByteArrayOutputStream().use { baos ->
            ObjectOutputStream(baos).use { oos ->
                oos.writeObject(`object`)
                val objectVal = String(Base64.encode(baos.toByteArray(), Base64.DEFAULT))
                editor.putString(key, objectVal).apply()
            }
        }

    }
    fun putObject(key: String?, `object`: Parcelable?) {
        val bos = ByteArrayOutputStream()
        val editor = mPreferences.edit()
        try {
            val parcel = Parcel.obtain()
            parcel.writeParcelable(`object`, 0)
            bos.write(parcel.marshall())
            val objectVal = String(Base64.encode(bos.toByteArray(), Base64.DEFAULT))
            editor.putString(key, objectVal).apply()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 取 对象
     *
     * @param key
     * @param <T>
     * @return
    </T> */
    fun <T : Parcelable> getObject(key: String?): T? {
        if (mPreferences.contains(key)) {
            val objectVal = mPreferences.getString(key, null)
            val buffer = Base64.decode(objectVal, Base64.DEFAULT)
            val parcel = Parcel.obtain()
            parcel.unmarshall(buffer, 0, buffer.size)
            parcel.setDataPosition(0)
            return parcel.readParcelable(Thread.currentThread().contextClassLoader) as T?
        }
        return null
    }

    /**
     * 取 对象。 需要给出目标对象类型，若不符合可能导致异常。
     *
     * @param key 唯一key值
     * @param clazz 目标对象类型
     *
     * @return 目标对象类型的实例
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    fun <T : Serializable> getObject(key: String, clazz: Class<T>): T? {
        if (!mPreferences.contains(key)) {
            return null
        }
        val objectVal = mPreferences.getString(key, null)
        val buffer = Base64.decode(objectVal, Base64.DEFAULT)

        return ByteArrayInputStream(buffer).use { bais ->
            ObjectInputStream(bais).use { ois ->
                ois.readObject() as T
            }
        }
    }

    /**
     * 获取所有的存储值。
     *
     * @return Map组织形式的key-value对
     */
    fun getAll(): Map<String, *> {
        return mPreferences.all
    }


    operator fun contains(key: String): Boolean {
        return mPreferences.contains(key)
    }

    fun remove(key: String) {
        remove(key, false)
    }

    @SuppressLint("ApplySharedPref")
    fun remove(key: String, isCommit: Boolean) {
        if (isCommit) {
            mPreferences.edit().remove(key).commit()
        } else {
            mPreferences.edit().remove(key).apply()
        }
    }


    /**
     * 设置Preferences修改监听器。
     */
    fun setOnPreferencesChangedListener(listener: OnPreferencesChangedListener) {
        mPreferences.registerOnSharedPreferenceChangeListener(this)
        mListener = listener
    }

    /**
     * 移除Preferences监听。
     */
    fun removeOnPreferencesChangedListener() {
        mListener = null
        mPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * 检查SharePreferences及key值是否是空。
     *
     * @param key 检查的key
     */
    @Throws(IllegalArgumentException::class)
    private fun checkNull(key: String) {
        require(!TextUtils.isEmpty(key)) { HINT_KEY_NULL }
    }

    interface OnPreferencesChangedListener {
        fun onPreferencesChanged(handler: PreferencesHandler, key: String)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
        mListener?.apply {
            onPreferencesChanged(this@PreferencesHandler, key)
        }
    }
}