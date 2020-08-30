package com.github.xiantang.airplugin.action

import com.github.xiantang.airplugin.http.AirState
import com.github.xiantang.airplugin.http.Client
import com.github.xiantang.airplugin.utils.Utils
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.util.containers.ContainerUtil
import com.intellij.xdebugger.XDebuggerBundle
import com.intellij.xdebugger.impl.actions.AttachToProcessAction
import java.util.concurrent.TimeUnit


class AirAttachToProcessAction : AttachToProcessAction() {
    private lateinit var state: AirState
    override fun actionPerformed(event: AnActionEvent) {
        val project = getEventProject(event) ?: return
        object : Backgroundable(project,
                XDebuggerBundle.message("xdebugger.attach.action.collectingItems"), true,
                DEAF) {
            override fun run(indicator: ProgressIndicator) {
                if (state.state == "dead") {
                    println("dead")
                    return
                }
                val allItems: List<AttachItem<*>> = ContainerUtil.immutableList(getTopLevelItems(indicator, project))
                ApplicationManager.getApplication().invokeLater({
                    val item = allItems.find {
                        it as AttachToProcessItem
                        val fatherPid = Utils.getFatherPid(it.processInfo.pid)
                        state.pid == fatherPid
                    }
                    item as AttachToProcessItem
                    val debugger = item.debuggers[0]
                    item.processInfo
                    debugger.attachDebugSession(project, item.host, item.processInfo)
                    val airState = Client.postAirStatus()
                    state = airState
                    ApplicationManager.getApplication().executeOnPooledThread {
                        while (true) {
                            TimeUnit.SECONDS.sleep(2)
                            if (state.state == "running") {
                                ApplicationManager.getApplication().invokeLater {

                                    val items: List<AttachItem<*>> = ContainerUtil.immutableList(getTopLevelItems(indicator, project))
                                    val item = items.find {
                                        it as AttachToProcessItem
                                        val fatherPid = Utils.getFatherPid(it.processInfo.pid)
                                        state.pid == fatherPid
                                    } ?: return@invokeLater
                                    item as AttachToProcessItem
                                    val debugger = item.debuggers[0]
                                    println("attaching")
                                    debugger.attachDebugSession(project, item.host, item.processInfo)
                                    val airState = Client.postAirStatus()
                                    state = airState
                                }
                            } else if (state.state == "dead") {
                                println("is dead")
                                return@executeOnPooledThread
                            }
                        }
                    }

                }, project.disposed)

            }
        }.queue()
    }

    override fun update(e: AnActionEvent) {
        val airState = Client.getAirStatus()
        state = airState
        super.update(e)

    }
}
