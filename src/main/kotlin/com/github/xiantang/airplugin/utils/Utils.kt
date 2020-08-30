package com.github.xiantang.airplugin.utils

import java.io.BufferedReader
import java.io.InputStreamReader

class Utils {
    companion object {
        fun getFatherPid(pid: Int): Int {
            val process = Runtime.getRuntime()
                    .exec(String.format("ps -o ppid= -p %d", pid))
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            val readLine = bufferedReader.readLine() ?: return -1
            return readLine.trim().toInt()
        }
    }
}