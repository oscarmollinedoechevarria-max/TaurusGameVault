package com.example.taurusgamevault.Model.retrofit.igdb

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface IgdbApiService {
    @POST("games")
    suspend fun searchGames(
        @Body query: RequestBody
    ): List<IgdbGame>
}