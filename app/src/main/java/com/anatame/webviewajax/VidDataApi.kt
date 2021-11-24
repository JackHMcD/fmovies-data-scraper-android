package com.anatame.webviewajax

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url


interface VidDataApi {
    @GET
    suspend fun getVidData(@Url url: String): Response<VidData>

}
