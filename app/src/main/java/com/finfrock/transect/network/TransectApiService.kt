package com.finfrock.transect.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

private const val BASE_URL = "http://transect.whalecount.com/"
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

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
    val retrofitService: TransectApiService by lazy {
        retrofit.create(TransectApiService::class.java)
    }
}