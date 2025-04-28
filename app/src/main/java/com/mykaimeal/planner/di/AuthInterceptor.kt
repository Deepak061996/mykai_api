package com.mykaimeal.planner.di

import android.annotation.SuppressLint
import android.content.Context
import com.mykaimeal.planner.basedata.SessionManagement
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(var context: Context) : Interceptor {

    @SuppressLint("SuspiciousIndentation")
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder: Request.Builder = chain.request().newBuilder()
        val token = getBearerToken()
        if (token != null && token.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
            requestBuilder.addHeader("Accept", "application/json") // Removed extra space after "Accept"
        }
        return chain.proceed(requestBuilder.build())
    }
    private fun getBearerToken(): String {
        val sessionManagement = SessionManagement(context)
        val token: String = sessionManagement.getAuthToken()!!

        return token
    }

}