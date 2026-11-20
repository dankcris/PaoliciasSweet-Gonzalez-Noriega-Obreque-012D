package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Producto
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatalogoViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(AppDatabase.get(app))

    private val _filtro = MutableStateFlow("Todas")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    private val _productosRemotos = MutableStateFlow<List<Producto>>(emptyList())

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            try {
                val remotos = RetrofitClient.catalogoApi.listarProductos().map { dto ->
                    Producto(
                        id = dto.id,
                        nombre = dto.nombre,
                        descripcion = dto.descripcion,
                        categoria = dto.categoria,
                        precioPorPersona = dto.precioPorPersona
                    )
                }
                _productosRemotos.value = remotos
            } catch (_: Exception) {
                // Si Docker no está encendido, usa los datos locales de respaldo
            }
        }
    }

    val productos: StateFlow<List<Producto>> = combine(
        _productosRemotos,
        repo.productos(),
        _filtro
    ) { remotos, locales, filtro ->
        val base = remotos.ifEmpty { locales }
        if (filtro == "Todas") base else base.filter { it.categoria == filtro }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filtrar(categoria: String) {
        _filtro.value = categoria
    }
}
