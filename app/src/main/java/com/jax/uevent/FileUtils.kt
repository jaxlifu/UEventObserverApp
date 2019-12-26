package com.jax.uevent

import java.io.*

object FileUtils {
    /**
     * 读取文件
     *
     * @param file
     * @return 字符串
     */
    fun readFromFile(file: File?): String? {
        if (file?.exists() != true) return ""
        return try {
            val fileInputStream = FileInputStream(file)
            val reader =
                BufferedReader(InputStreamReader(fileInputStream))
            val value = reader.readLine()
            fileInputStream.close()
            value
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }


    /**
     * 文件中写入字符串
     *
     * @param file
     * @param value
     */
    fun write2File(file: File?, value: String?): Boolean {
        if (file?.exists() != true) return false
        return try {
            val fileOutputStream = FileOutputStream(file)
            val pWriter = PrintWriter(fileOutputStream)
            pWriter.println(value)
            pWriter.flush()
            pWriter.close()
            fileOutputStream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}
