package com.caowj.lib_utils

import android.util.Log
import java.lang.reflect.Field
import java.lang.reflect.Method


/**
 * 反射工具类
 */
object RefUtil {

    private var TAG = RefUtil::class.java.simpleName

    @JvmStatic
    @Throws(Exception::class)
    fun getFieldValue(clazzName: String, target: Any, name: String): Any {
        return getFieldValue(Class.forName(clazzName), target, name)
    }

    @JvmStatic
    @Throws(Exception::class)
    fun getFieldValue(clazz: Class<*>, target: Any, name: String): Any {
        val field = clazz.getDeclaredField(name)
        field.isAccessible = true
        return field.get(target)
    }

    @JvmStatic
    fun getFieldValueNoException(clazzName: String, target: Any, name: String): Any? {
        return getFieldNoException(Class.forName(clazzName), target, name)
    }

    @JvmStatic
    fun getFieldNoException(clazz: Class<*>, target: Any, name: String): Any? {
        try {
            return getFieldValue(clazz, target, name)
        } catch (e: Exception) {
            Log.w(TAG, "can't find the field of  ${clazz.name}#${name} ")
        }

        return null
    }

    @JvmStatic
    @Throws(Exception::class)
    fun setFieldValue(clazzName: String, target: Any, name: String, value: Any) {
        setFieldValue(Class.forName(clazzName), target, name, value)
    }

    @JvmStatic
    @Throws(Exception::class)
    fun setFieldValue(clazz: Class<*>, target: Any, name: String, value: Any) {
        val field = clazz.getDeclaredField(name)
        field.isAccessible = true
        field.set(target, value)
    }

    @JvmStatic
    fun setFieldValueNoException(clazzName: String, target: Any, name: String, value: Any) {
        try {
            setFieldValue(Class.forName(clazzName), target, name, value)
        } catch (e: Exception) {
            Log.e(TAG, "can't set the field value of  ${clazzName}#${name} ", e)
        }

    }

    @JvmStatic
    fun setFieldValueNoException(clazz: Class<*>, target: Any, name: String, value: Any) {
        try {
            setFieldValue(clazz, target, name, value)
        } catch (e: Exception) {
            Log.e(TAG, "can't set the field value of  ${clazz.name}#${name} ", e)
        }

    }

    @JvmStatic
    @Throws(Exception::class)
    operator fun invoke(clazzName: String, target: Any, name: String, vararg args: Any): Any {
        return invoke(Class.forName(clazzName), target, name, *args)
    }

    @JvmStatic
    @Throws(Exception::class)
    operator fun invoke(clazz: Class<*>, target: Any, name: String, vararg args: Any): Any {
        val parameterTypes: Array<Class<*>?> = arrayOfNulls(args.size)
        for (i in args.indices) {
            parameterTypes[i] = args[i].javaClass
        }

        val method = clazz.getDeclaredMethod(name, *parameterTypes)
        method.isAccessible = true
        return method.invoke(target, *args)
    }

    @JvmStatic
    @Throws(Exception::class)
    operator fun invoke(clazzName: String, target: Any, name: String,
                        parameterTypes: Array<Class<*>>, vararg args: Any): Any {
        return invoke(Class.forName(clazzName), target, name, parameterTypes, *args)
    }

    @JvmStatic
    @Throws(Exception::class)
    operator fun invoke(clazz: Class<*>, target: Any, name: String,
                        parameterTypes: Array<Class<*>>, vararg args: Any): Any {
        val method = clazz.getDeclaredMethod(name, *parameterTypes)
        method.isAccessible = true
        return method.invoke(target, *args)
    }

    @JvmStatic
    fun invokeNoException(clazzName: String, target: Any, name: String,
                          parameterTypes: Array<Class<*>>, vararg args: Any): Any? {
        return invokeNoException(Class.forName(clazzName), target, name, parameterTypes, *args)
    }

    @JvmStatic
    fun invokeNoException(clazz: Class<*>, target: Any, name: String,
                          parameterTypes: Array<Class<*>>, vararg args: Any): Any? {
        try {
            return invoke(clazz, target, name, parameterTypes, *args)
        } catch (e: Exception) {
            Log.e(TAG, "can't invoke the method of  ${clazz.name}#${name} ", e)
        }

        return null
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     * @param object : 子类对象
     * @param methodName : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @return 父类中的方法对象
     */
    @JvmStatic
    fun getDeclaredMethod(`object`: Any, methodName: String, vararg parameterTypes: Class<*>): Method? {
        var method: Method? = null
        var clazz: Class<*> = `object`.javaClass
        while (clazz != Any::class.java) {
            try {
                method = clazz.getDeclaredMethod(methodName, *parameterTypes)
                return method
            } catch (e: Exception) {
                Log.w(TAG, "can't find the method of  ${clazz.name}#${methodName} ")
            }

            clazz = clazz.superclass as Class<*>
        }
        return null
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     * @param object : 子类对象
     * @param fieldName : 父类中的属性名
     * @return 父类中的属性对象
     */
    @JvmStatic
    fun getDeclaredField(`object`: Any, fieldName: String): Field? {
        var field: Field? = null
        var clazz: Class<*> = `object`.javaClass
        while (clazz != Any::class.java) {
            try {
                field = clazz.getDeclaredField(fieldName)
                return field
            } catch (e: Exception) {
                Log.w(TAG, "can't find the field of  ${clazz.name}#${fieldName} ")
            }

            clazz = clazz.superclass as Class<*>
        }
        return null
    }

}
