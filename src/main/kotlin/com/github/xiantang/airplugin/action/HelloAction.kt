package com.github.xiantang.airplugin.action

import com.google.gson.JsonParser
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.util.containers.ContainerUtil
import com.intellij.xdebugger.XDebuggerBundle
import com.intellij.xdebugger.impl.actions.AttachToProcessAction
import org.apache.http.HttpEntity
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.io.BufferedReader
import java.io.InputStreamReader


class AirAttachToProcessAction : AttachToProcessAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = getEventProject(event) ?: return
        object : Backgroundable(project,
                XDebuggerBundle.message("xdebugger.attach.action.collectingItems"), true,
                DEAF) {
            override fun run(indicator: ProgressIndicator) {
                val allItems: List<AttachItem<*>> = ContainerUtil.immutableList(getTopLevelItems(indicator, project))
                ApplicationManager.getApplication().invokeLater({
                    val httpGet = HttpGet("http://localhost:6788")
                    val client: HttpClient = HttpClients.createDefault()
                    val response = client.execute(httpGet)
                    val entity: HttpEntity = response.entity
                    val toString = EntityUtils.toString(entity)

                    val jp = JsonParser() //from gson
                    val parse = jp.parse(toString)
                    val asJsonObject = parse.asJsonObject
                    val asInt = asJsonObject.get("pid").asInt
                    val item = allItems.find {
                        it as AttachToProcessItem
                        val process = Runtime.getRuntime()
                                .exec(String.format("ps -o ppid= -p %d", it.processInfo.pid));
                        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

                        val readLine = bufferedReader.readLine().trim().toInt()
                        asInt == readLine

                    }
                    item as AttachToProcessItem
                    val host = item.host
                    val processInfo = item.processInfo
                    val debuggers = item.debuggers
                    debuggers[0].attachDebugSession(project, host, processInfo)
                }, project.disposed)

            }
        }.queue()
    }
}