package com.caowj.lib_utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 *
 * @author wangqian
 * @date 2020/12/15
 * 兼容android10的文件存储机制时的公共媒体文件存储方法
 */
object PublicFileUtil {
    /**
     * 保存Bitmap到图片的公共目录
     *  @param fileName 文件名必须带扩展名，如xxx.png,xxx.jpg，如果不带，保存成功后，系统会自动添加.jpg的扩展名
     *  @Bitmap
     * @param subDir 公共目录文件夹路径，系统规定图片只能存到Environment.DIRECTORY_PICTURES
     * 和Environment.DIRECTORY_DCIM文件夹下，所以subDir要以这两个开头，否则，会默认存到Environment.DIRECTORY_DCIM文件夹
     * @return 保存成功Uri,保存失败返回null
     */
    @JvmStatic
    fun saveImage2Public(context: Context, fileName: String, bitmap: Bitmap, subDir: String): Uri? {
        var byteArray = BitmapUtil.bitmapToByte(bitmap)
        return saveImage2Public(context, fileName, byteArray, subDir)
    }

    /**
     * 保存ByteArray类型数据的图片到公共目录
     *  @param fileName 文件名必须带扩展名，如xxx.png,xxx.jpg，如果不带，保存成功后，系统会自动添加.jpg的扩展名
     *  @Bitmap
     * @param subDir 公共目录文件夹路径，系统规定图片只能存到Environment.DIRECTORY_PICTURES
     *           和Environment.DIRECTORY_DCIM文件夹下，所以subDir要以这两个开头，否则，会默认存到Environment.DIRECTORY_DCIM文件夹
     * @return 保存成功Uri,保存失败返回null
     */

    @JvmStatic
    fun saveImage2Public(context: Context, fileName: String, image: ByteArray, subDir: String): Uri? {
        val subDirectory: String
        subDirectory = if (!TextUtils.isEmpty(subDir)) {
            if (subDir.endsWith("/")) {
                subDir.substring(0, subDir.length - 1)
            }
            if (!(subDir.startsWith(Environment.DIRECTORY_PICTURES) ||
                            subDir.startsWith(Environment.DIRECTORY_DCIM))) {
                Environment.DIRECTORY_DCIM + File.separator + subDir
            }
            subDir
        } else {
            Environment.DIRECTORY_DCIM
        }

        var uri:Uri? =  searchOrNewUri(context,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,subDirectory,fileName,"image")
        if (uri != null) {
            var outputStream: OutputStream? = null
            try {
                outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    outputStream.write(image)
                    outputStream.flush()
                }
            } catch (e: IOException) {
                Log.e("caowj",e.message)
                return null
            } finally {
                outputStream?.close()
            }
        }

        return uri;
    }

    private fun getFileExtension(fileName: String): String {
        val lastPst = fileName.lastIndexOf('.')
        if (lastPst > -1) {
            return fileName.substring(lastPst)
        }
        return ""
    }


    private fun searchImageFromPublic(context: Context, subDir: String, fileName: String): Cursor? {
        var filePath = subDir
        if (TextUtils.isEmpty(fileName)) {
            return null
        }
        if (TextUtils.isEmpty(filePath)) {
            filePath = Environment.DIRECTORY_DCIM + File.separator
        } else {
            if (!filePath.endsWith(File.separator)) {
                filePath = "$filePath${File.separator}"
            }
        }

        //兼容androidQ和以下版本
        val queryPathKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.RELATIVE_PATH
        else MediaStore.Images.Media.DATA
        val selection = queryPathKey + "=? and " + MediaStore.Images.Media.DISPLAY_NAME + "=?"
        val value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(filePath, fileName)
        else arrayOf(SdCardUtil.getSDCardPathByEnvironment() + File.separator + filePath + fileName, fileName)
        return context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID, queryPathKey, MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media.DISPLAY_NAME),
                selection, value,
                null)
    }

    /**
     * 保存视频文件到公共目录
     * @param context
     * @param fileName
     * @param subDir
     * @param inputStream 视频文件输入流
     * @return 保存成功Uri,保存失败返回null
     */
    @JvmStatic
    fun savedVideo2Public(context: Context, fileName: String, subDir: String, inputStream: InputStream): Uri? {
        var uri = getVideoPublicUri(context, fileName, subDir);
        uri?.let {
            var outputStream: OutputStream? = null
            try {
                outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    var byteArray = ByteArray(1024 * 64)
                    while (inputStream.read(byteArray) != -1) {
                        outputStream.write(byteArray)
                    }

                }

            } catch (e: java.lang.Exception) {
                Log.e("caowj",e.message)
                return null;
            } finally {
                inputStream.close()
                outputStream?.close()
            }

        }
        return uri
    }

    /**
     * 根据文件名和存储相对路径，获取Video文件Uri
     * 先查询，如果不存在，就insert，生成Uri
     * @param context
     * @param fileName 文件名
     * @param subDir 相对文件夹，如${Environment.DIRECTORY_MOVIES}/project
     */
    @JvmStatic
    private fun getVideoPublicUri(context: Context, fileName: String, subDir: String): Uri? {
        val subDirectory: String
        subDirectory = if (!TextUtils.isEmpty(subDir)) {
            if (subDir.endsWith("/")) {
                subDir.substring(0, subDir.length - 1)
            }
            if (!(subDir.startsWith(Environment.DIRECTORY_MOVIES) ||
                            subDir.startsWith(Environment.DIRECTORY_DCIM))) {
                Environment.DIRECTORY_DCIM + File.separator + subDir
            }
            subDir
        } else {
            Environment.DIRECTORY_DCIM
        }
       return searchOrNewUri(context,MediaStore.Video.Media.EXTERNAL_CONTENT_URI,subDirectory,fileName,"video")
    }

    /**
     * 查询Uri，如果查询不到，就新增
     * @param externalContentUri MediaStore.Video.Media.EXTERNAL_CONTENT_URI
     * 或者MediaStore.Images.Media.EXTERNAL_CONTENT_URI或者MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
     * @param subDirectory 相对目录如 Environment.DIRECTORY_DCIM，注意前后都不能加"/"
     * @param fileName 文件名 如test.png
     * @param type 媒体类型：video、image、audio
     */
    fun searchOrNewUri(context: Context,externalContentUri: Uri, subDirectory: String, fileName: String,type:String ):Uri?{
        var searchDirectory = subDirectory
        if (!subDirectory.endsWith(File.separator)) {
            searchDirectory = "$subDirectory${File.separator}"
        }

        //兼容androidQ和以下版本
        val queryPathKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.MediaColumns.RELATIVE_PATH
        else MediaStore.MediaColumns.DATA
        val selection = queryPathKey + "=? and " + MediaStore.MediaColumns.DISPLAY_NAME + "=?"
        val value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(searchDirectory, fileName)
        else arrayOf(SdCardUtil.getSDCardPathByEnvironment() + File.separator + searchDirectory + fileName, fileName)

        val cursor: Cursor?  = context.contentResolver.query(externalContentUri,
                arrayOf(MediaStore.MediaColumns._ID, queryPathKey, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.DISPLAY_NAME),
                selection, value,
                null)
        if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID)) // uri的id，用于获取图片
            val uri = Uri.withAppendedPath(externalContentUri, "" + id)
//            val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toLong())
//            var type = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE))
            try {

                if (uri != null) {
                    // Android10以下，只调用openOutputStream不会自动创建文件夹
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        var path = SdCardUtil.getSDCardPathByEnvironment() + File.separator + subDirectory + File.separator + fileName
                        if (!File(path).parentFile.exists()) {
                            File(path).parentFile.mkdirs()
                        }
                    }
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        outputStream.close()
                    }
                }

            } catch (e: IOException) {
                Log.e("caowj",e.message)
            }
            return uri
        }
        //设置保存参数到ContentValues中
        val contentValues = ContentValues()
        //设置文件名
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        //兼容Android Q和以下版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
            //RELATIVE_PATH是相对路径不是绝对路径
            //关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, subDirectory)
            //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Music/sample");
        } else {
            contentValues.put(MediaStore.MediaColumns.DATA,
                    SdCardUtil.getSDCardPathByEnvironment() + File.separator + subDirectory + File.separator + fileName)
        }

        //设置文件类型
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "${type}/${getFileExtension(fileName)}")
        //执行insert操作，向系统文件夹中添加文件
        //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
        val uri = context.contentResolver.insert(externalContentUri, contentValues)
        try {
            if (uri != null) {
                // Android10以下，只调用openOutputStream不会自动创建文件夹
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    var path = contentValues.get(MediaStore.MediaColumns.DATA) as String;
                    if (!File(path).parentFile.exists()) {
                        File(path).parentFile.mkdirs()
                    }
                }
                // 调用openOutputStream才会创建文件
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    outputStream.close()
                }
            }
        } catch (e: IOException) {
            Log.e("caowj",e.message)
        }
        return uri;
    }

    @JvmStatic
    fun searchVideoFromPublic(context: Context, filePath: String, fileName: String): Cursor? {
        var filePath = filePath
        if (TextUtils.isEmpty(fileName)) {
            return null
        }
        if (TextUtils.isEmpty(filePath)) {
            filePath = Environment.DIRECTORY_DCIM + File.separator
        } else {
            if (!filePath.endsWith(File.separator)) {
                filePath = "$filePath${File.separator}"
            }
        }

        //兼容androidQ和以下版本
        val queryPathKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Video.Media.RELATIVE_PATH
        else MediaStore.Video.Media.DATA
        val selection = queryPathKey + "=? and " + MediaStore.Video.Media.DISPLAY_NAME + "=?"
        val value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(filePath, fileName)
        else arrayOf(SdCardUtil.getSDCardPathByEnvironment() + File.separator + filePath + fileName, fileName)

        return context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Video.Media._ID, queryPathKey, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DISPLAY_NAME),
                selection, value,
                null)
    }

    @JvmStatic
    fun searchVideoInfoFromPublic(context: Context, uri: Uri): Array<Any> {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID)) // uri的id，用于获取图片
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
            val fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
            val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
            return arrayOf(path, fileName, size)

        }
        return arrayOf()
    }

    /**
     * 根据Uri,从公共目录下查询图片的全路径，文件名和大小等信息
     * @param context
     * @param uri
     */
    @JvmStatic
    fun searchImageInfoFromPublic(context: Context, uri: Uri): Array<Any> {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)) // uri的id，用于获取图片
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            val fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
            val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
            return arrayOf(path, fileName, size)

        }
        return arrayOf()
    }

    /**
     * 保存音频文件到公共目录,目录默认为 Environment.DIRECTORY_MUSIC
     * @param context
     * @param fileName 文件名
     * @param audio 音频文件字节数组
     * @param subDir 文件夹 如Environment.DIRECTORY_MUSIC/record
     * @return uri 返回存储的uri
     */
    @JvmStatic
    fun saveAudio2Public(context: Context, fileName: String, audio: ByteArray, subDir: String): Uri? {
        val subDirectory: String
        subDirectory = if (!TextUtils.isEmpty(subDir)) {
            if (subDir.endsWith("/")) {
                subDir.substring(0, subDir.length - 1)
            }
            subDir
        } else {
            Environment.DIRECTORY_MUSIC
        }
        var uri:Uri? =  searchOrNewUri(context,MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,subDirectory,fileName,"audio")

        if (uri != null) {
            var outputStream: OutputStream? = null
            try {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                // 调用openOutputStream才会创建文件
                outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    outputStream.write(audio)
                    outputStream.flush()

                }

            } catch (e: IOException) {
                Log.e("caowj",e.message)
                return null
            } finally {
                outputStream?.close()
            }
        }
        return uri;
    }

    /**
     * 保存音频文件到公共目录,目录默认为 Environment.DIRECTORY_MUSIC
     * @param context
     * @param fileName 文件名
     * @param audio 音频文件字节数组
     * @param subDir 文件夹 如Environment.DIRECTORY_MUSIC/record
     * @return uri 返回存储的uri
     */
    @JvmStatic
    fun saveAudio2Public(context: Context, fileName: String, inputStream: InputStream, subDir: String): Uri? {
        val subDirectory: String
        subDirectory = if (!TextUtils.isEmpty(subDir)) {
            if (subDir.endsWith("/")) {
                subDir.substring(0, subDir.length - 1)
            }

            subDir
        } else {
            Environment.DIRECTORY_MUSIC
        }
        var uri:Uri? =  searchOrNewUri(context,MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,subDirectory,fileName,"audio")
        if (uri != null) {
            var outputStream: OutputStream? = null
            try {
                outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    var byteArray = ByteArray(1024 * 64)
                    while (inputStream.read(byteArray) != -1) {
                        outputStream.write(byteArray)
                    }
                    outputStream.flush()
                }

            } catch (e: IOException) {
                Log.e("caowj",e.message)
                return null
            } finally {
                inputStream.close()
                outputStream?.close()
            }
        }
        return uri;

    }

    @JvmStatic
    fun searchAudioFromPublic(context: Context, filePath: String, fileName: String): Cursor? {
        var filePath = filePath
        if (TextUtils.isEmpty(fileName)) {
            return null
        }
        if (TextUtils.isEmpty(filePath)) {
            filePath = Environment.DIRECTORY_MUSIC + File.separator
        } else {
            if (!filePath.endsWith(File.separator)) {
                filePath = "$filePath${File.separator}"
            }
        }

        //兼容androidQ和以下版本
        val queryPathKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.RELATIVE_PATH
        else MediaStore.Audio.Media.DATA
        val selection = queryPathKey + "=? and " + MediaStore.Audio.Media.DISPLAY_NAME + "=?"
        val value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(filePath, fileName)
        else arrayOf(SdCardUtil.getSDCardPathByEnvironment() + File.separator + filePath + fileName, fileName)
        return context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media._ID, queryPathKey, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.DISPLAY_NAME),
                selection, value,
                null)
    }

    /**
     * 根据Uri删除文件
     *  @return The number of rows deleted.
     */
    @JvmStatic
    fun deleteFile(context: Context, uri: Uri): Int {
        return context.contentResolver.delete(uri, null, null)
    }

    /**
     * 根据Uri删除文件
     *  @return The number of rows deleted.
     */
    @JvmStatic
    fun deleteFile(uri: Uri): Int {
        AppUtil.getApp()?.let {
            return it.applicationContext.contentResolver.delete(uri, null, null)
        } ?: return 0

    }

    /**
     * 删除Image类型的文件
     * @param context
     * @param subDir  文件存储的相对sdcard的目录，如Environment.DIRECTORY_DCIM 开头，不要加上/sdcard
     * @param fileName 文件名，如test.mp3
     * @return The number of rows deleted.
     */
    fun deleteImageFromPublic(context: Context, subDir: String, fileName: String): Int {
        val cursor: Cursor? = searchImageFromPublic(context, subDir, fileName)
        if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)) // uri的id，用于获取图片
            val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id)
            return deleteFile(context, uri)
        }
        return 0
    }

    /**
     * 删除Video类型的文件
     * @param context
     * @param subDir  文件存储的相对sdcard的目录，如Environment.DIRECTORY_DCIM 开头，不要加上/sdcard
     * @param fileName 文件名，如test.mp3
     * @return The number of rows deleted.
     */
    fun deleteVideoFromPublic(context: Context, subDir: String, fileName: String): Int {
        val cursor: Cursor? = searchVideoFromPublic(context, subDir, fileName)
        if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID)) // uri的id，用于获取图片
            val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id)
            return deleteFile(context, uri)
        }
        return 0
    }

    /**
     * 删除audio类型的文件
     * @param context
     * @param subDir  文件存储的相对sdcard的目录，如${Environment.DIRECTORY_MUSIC}开头，不要加上/sdcard
     * @param fileName 文件名，如test.mp3
     * @return The number of rows deleted.
     */
    fun deleteAudioFromPublic(context: Context, subDir: String, fileName: String): Int {
        val cursor: Cursor? = searchAudioFromPublic(context, subDir, fileName)
        if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)) // uri的id，用于获取图片
            val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id)
            return deleteFile(context, uri)
        }
        return 0
    }
}