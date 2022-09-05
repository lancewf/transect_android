package com.finfrock.transect.network

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

private const val BASE_URL = "http://transect.whalecount.com/"

interface TransectApiService {
    @GET("transect/")
    suspend fun getTransects():List<RemoteTransect>

    @GET("transect/{id}")
    suspend fun getTransect(@Path(value="id") id: String):RemoteTransect

    @POST("transect/")
    suspend fun saveTransect(@Body transect: RemoteTransect)

    @GET("observer/")
    suspend fun getObservers():List<RemoteObserver>

    @GET("vessel/")
    suspend fun getVessels():List<RemoteVessel>
}

object TransectApi {

    fun build(context: Context):TransectApiService {

        val app: ApplicationInfo = context.packageManager.getApplicationInfo(
            context.packageName, PackageManager.GET_META_DATA)
        val bundle = app.metaData
        val key = bundle.getString("com.finfrock.transect.API_KEY")

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(object: Interceptor{
            override fun intercept(chain: Interceptor.Chain): Response {
                val requestBuilder = chain.request().newBuilder()
                requestBuilder.header("api-key", key)
                return chain.proceed(requestBuilder.build())
            }
        })

        val retrofit =  Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .client(httpClientBuilder.build())
            .build()
        return retrofit.create(TransectApiService::class.java)
    }
}