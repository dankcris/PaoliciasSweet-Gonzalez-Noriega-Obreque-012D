package cl.duoc.paoliciassweet.network

import cl.duoc.paoliciassweet.BuildConfig
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface CatalogoApi {
    @GET("api/productos")
    suspend fun listarProductos(): List<ProductoDto>

    @POST("api/cotizaciones")
    suspend fun cotizar(@Body request: CotizacionRequest): CotizacionDto
}

interface ReservasApi {
    @GET("api/reservas")
    suspend fun listarReservas(@Query("email") email: String? = null): List<ReservaDto>

    @GET("api/reservas/{id}")
    suspend fun obtenerReserva(@Path("id") id: Long): ReservaDto

    @POST("api/reservas")
    suspend fun crearReserva(@Body request: ReservaRequest): ReservaDto

    @PUT("api/reservas/{id}")
    suspend fun actualizarReserva(@Path("id") id: Long, @Body request: ReservaRequest): ReservaDto

    @PUT("api/reservas/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): ReservaDto

    @DELETE("api/reservas/{id}")
    suspend fun eliminarReserva(@Path("id") id: Long)
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

/** API externa pública (Chile): https://mindicador.cl */
interface IndicadoresApi {
    @GET("api")
    suspend fun indicadores(): IndicadoresDto
}

object RetrofitClient {

    private const val URL_INDICADORES = "https://mindicador.cl/"

    // Render (plan gratuito) puede tardar en "despertar" el servicio: timeouts amplios.
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private fun buildRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val catalogoApi: CatalogoApi by lazy { buildRetrofit(BuildConfig.CATALOGO_URL).create(CatalogoApi::class.java) }
    val reservasApi: ReservasApi by lazy { buildRetrofit(BuildConfig.RESERVAS_URL).create(ReservasApi::class.java) }
    val comprobantesApi: ComprobantesApi by lazy {
        buildRetrofit(BuildConfig.COMPROBANTES_URL).create(ComprobantesApi::class.java)
    }
    val indicadoresApi: IndicadoresApi by lazy { buildRetrofit(URL_INDICADORES).create(IndicadoresApi::class.java) }

    val remoto: RemoteRepositorio by lazy {
        RemoteRepositorio(catalogoApi, reservasApi, comprobantesApi, indicadoresApi)
    }
}
