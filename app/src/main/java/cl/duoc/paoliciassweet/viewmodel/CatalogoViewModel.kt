package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Producto
import cl.duoc.paoliciassweet.data.Repositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CatalogoViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(AppDatabase.get(app))

    private val _filtro = MutableStateFlow("Todas")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    val productos: StateFlow<List<Producto>> = combine(repo.productos(), _filtro) { lista, filtro ->
        if (filtro == "Todas") lista else lista.filter { it.categoria == filtro }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filtrar(categoria: String) {
        _filtro.value = categoria
    }
}
