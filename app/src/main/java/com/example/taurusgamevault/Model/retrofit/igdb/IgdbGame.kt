package com.example.taurusgamevault.Model.retrofit.igdb

// data class for igdb api response
data class IgdbGame(
    val id: Int,
    val name: String?,
    val summary: String?,
    val rating: Double?,
    val first_release_date: Long?,
    val cover: IgdbCover?,
    val genres: List<IgdbGenre>?,
    val platforms: List<IgdbPlatform>?,
    val screenshots: List<IgdbScreenshot>?,
    val tags: List<Int>?
)

fun igdbImageUrl(imageId: String, size: String = "cover_big"): String {
    return "https://images.igdb.com/igdb/image/upload/t_${size}/${imageId}.jpg"
}
