package com.example.taurusgamevault.Model.retrofit.igdb

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

// data class for igdb api response
interface IgdbApiService {
    @POST("games")
    suspend fun searchGames(
        @Body query: RequestBody
    ): List<IgdbGame>
}