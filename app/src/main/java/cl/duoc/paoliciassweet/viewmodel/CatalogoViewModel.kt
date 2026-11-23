package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Producto
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.domain.ResumenIndicadores
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

    private val _indicadores = MutableStateFlow<ResumenIndicadores?>(null)
    val indicadores: StateFlow<ResumenIndicadores?> = _indicadores.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            try {
                _productosRemotos.value = RetrofitClient.remoto.productos()
            } catch (_: Exception) {
                // Si el backend no responde, usa los datos locales de respaldo
            }
        }
        cargarIndicadores()
    }

    // API externa (mindicador.cl): independiente de los microservicios propios
    fun cargarIndicadores() {
        viewModelScope.launch {
            RetrofitClient.remoto.indicadores().onSuccess { _indicadores.value = it }
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
