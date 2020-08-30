package com.github.xiantang.airplugin.http

import com.google.gson.JsonParser
import org.apache.http.HttpEntity
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils


class Client {
    companion object {
        fun getAirStatus(): AirState {
            val httpGet = HttpGet("http://localhost:6788")
            return doRequest(httpGet)
        }

        fun postAirStatus(): AirState {
            val httpPost = HttpPost("http://localhost:6788")
            return doRequest(httpPost)
        }

        private fun doRequest(req: HttpRequestBase): AirState {
            val client: HttpClient = HttpClients.createDefault()
            var pid: Int
            var state: String
            try {
                val response = client.execute(req)
                val entity: HttpEntity = response.entity
                val toString = EntityUtils.toString(entity)
                val jp = JsonParser() //from gson
                val parse = jp.parse(toString)
                val jsonObj = parse.asJsonObject
                pid = jsonObj.get("pid").asInt
                state = jsonObj.get("state").asString
            } catch (e: Exception) {
                pid = -1
                state = "dead"
            }
            return AirState(pid = pid, state = state)
        }
    }

}