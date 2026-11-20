package cl.duoc.paoliciassweet.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface CatalogoApi {
    @GET("api/productos")
    suspend fun listarProductos(): List<ProductoDto>

    @POST("api/cotizaciones")
    suspend fun cotizar(@Body request: CotizacionRequest): CotizacionDto
}

interface ReservasApi {
    @GET("api/reservas")
    suspend fun listarReservas(@Query("email") email: String? = null): List<ReservaDto>

    @POST("api/reservas")
    suspend fun crearReserva(@Body request: ReservaRequest): ReservaDto

    @PUT("api/reservas/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): ReservaDto
}

interface ComprobantesApi {
    @Multipart
    @POST("api/comprobantes")
    suspend fun subirComprobante(
        @Part("reservaId") reservaId: RequestBody,
        @Part("montoAbonado") montoAbonado: RequestBody?,
        @Part archivo: MultipartBody.Part
    ): ComprobanteDto

    @GET("api/comprobantes/reserva/{reservaId}")
    suspend fun listarPorReserva(@Path("reservaId") reservaId: Long): List<ComprobanteDto>
}

object RetrofitClient {

    // Cambia 10.0.2.2 por tu IP local (ej. 192.168.1.15) si usas un teléfono físico
    private const val HOST = "http://10.0.2.2"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private fun buildRetrofit(port: Int): Retrofit = Retrofit.Builder()
        .baseUrl("$HOST:$port/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val catalogoApi: CatalogoApi by lazy { buildRetrofit(8081).create(CatalogoApi::class.java) }
    val reservasApi: ReservasApi by lazy { buildRetrofit(8082).create(ReservasApi::class.java) }
    val comprobantesApi: ComprobantesApi by lazy { buildRetrofit(8083).create(ComprobantesApi::class.java) }
}
