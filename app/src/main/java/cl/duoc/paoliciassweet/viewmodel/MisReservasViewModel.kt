package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.data.Reserva
import cl.duoc.paoliciassweet.data.Sesion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class MisReservasViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(AppDatabase.get(app))

    val reservas: StateFlow<List<Reserva>> = Sesion.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.reservas(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
