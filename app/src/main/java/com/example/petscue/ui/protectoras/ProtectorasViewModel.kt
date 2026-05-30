package com.example.petscue.ui.protectoras

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Protectora
import com.example.petscue.data.sources.local.ProtectorasLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.Locale

enum class OrdenProtectoras {
    ALFABETICO_AZ,
    ALFABETICO_ZA,
    MAS_CERCA,
    MAS_LEJOS
}

data class ProtectorasUiState(
    val todas: List<Protectora> = emptyList(),
    val filtradas: List<Protectora> = emptyList(),
    val comunidades: List<String> = emptyList(),
    val provincias: List<String> = emptyList(),
    val ciudades: List<String> = emptyList(),
    val busqueda: String = "",
    val comunidadSel: String = "Todas",
    val provinciaSel: String = "Todas",
    val ciudadSel: String = "Todas",
    val ordenSel: OrdenProtectoras = OrdenProtectoras.MAS_CERCA,
    val cargando: Boolean = true,
    val miProvincia: String = "",
    val miComunidad: String = "",
    val error: String? = null
)

class ProtectorasViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(ProtectorasUiState())
    val uiState: StateFlow<ProtectorasUiState> = _uiState.asStateFlow()

    init {
        cargarProtectoras()
    }

    private fun cargarProtectoras() {
        viewModelScope.launch {
            try {
                val lista = ProtectorasLoader.load(getApplication())

                val comunidades = listOf("Todas") + lista
                    .map { it.comunidad.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()

                _uiState.update {
                    it.copy(
                        todas = lista,
                        comunidades = comunidades,
                        cargando = false,
                        error = null
                    )
                }

                actualizarOpcionesDependientes()
                aplicarFiltros()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudieron cargar las protectoras"
                    )
                }
            }
        }
    }

    fun setUbicacion(provincia: String, comunidad: String) {
        _uiState.update {
            it.copy(
                miProvincia = provincia,
                miComunidad = comunidad
            )
        }
        aplicarFiltros()
    }

    fun onBusqueda(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
        aplicarFiltros()
    }

    fun onComunidad(comunidad: String) {
        _uiState.update {
            it.copy(
                comunidadSel = comunidad,
                provinciaSel = "Todas",
                ciudadSel = "Todas"
            )
        }
        actualizarOpcionesDependientes()
        aplicarFiltros()
    }

    fun onProvincia(provincia: String) {
        _uiState.update {
            it.copy(
                provinciaSel = provincia,
                ciudadSel = "Todas"
            )
        }
        actualizarOpcionesDependientes()
        aplicarFiltros()
    }

    fun onCiudad(ciudad: String) {
        _uiState.update { it.copy(ciudadSel = ciudad) }
        aplicarFiltros()
    }

    fun onOrdenChange(orden: OrdenProtectoras) {
        _uiState.update { it.copy(ordenSel = orden) }
        aplicarFiltros()
    }

    fun limpiarFiltros() {
        _uiState.update {
            it.copy(
                busqueda = "",
                comunidadSel = "Todas",
                provinciaSel = "Todas",
                ciudadSel = "Todas",
                ordenSel = OrdenProtectoras.MAS_CERCA
            )
        }
        actualizarOpcionesDependientes()
        aplicarFiltros()
    }

    private fun actualizarOpcionesDependientes() {
        val state = _uiState.value

        val baseProvincias = state.todas.filter {
            state.comunidadSel == "Todas" || it.comunidad.equals(state.comunidadSel, ignoreCase = true)
        }

        val provincias = listOf("Todas") + baseProvincias
            .map { it.provincia.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val baseCiudades = baseProvincias.filter {
            state.provinciaSel == "Todas" || it.provincia.equals(state.provinciaSel, ignoreCase = true)
        }

        val ciudades = listOf("Todas") + baseCiudades
            .map { it.ciudad.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        _uiState.update {
            it.copy(
                provincias = provincias,
                ciudades = ciudades
            )
        }
    }

    private fun aplicarFiltros() {
        val st = _uiState.value
        val q = normalizar(st.busqueda)

        var resultado = st.todas

        if (st.comunidadSel != "Todas") {
            resultado = resultado.filter {
                it.comunidad.equals(st.comunidadSel, ignoreCase = true)
            }
        }

        if (st.provinciaSel != "Todas") {
            resultado = resultado.filter {
                it.provincia.equals(st.provinciaSel, ignoreCase = true)
            }
        }

        if (st.ciudadSel != "Todas") {
            resultado = resultado.filter {
                it.ciudad.equals(st.ciudadSel, ignoreCase = true)
            }
        }

        if (q.isNotBlank()) {
            resultado = resultado.filter { protectora ->
                normalizar(protectora.nombre).contains(q) ||
                        normalizar(protectora.descripcion).contains(q) ||
                        normalizar(protectora.comunidad).contains(q) ||
                        normalizar(protectora.provincia).contains(q) ||
                        normalizar(protectora.ciudad).contains(q)
            }
        }

        resultado = when (st.ordenSel) {
            OrdenProtectoras.ALFABETICO_AZ -> {
                resultado.sortedBy { normalizar(it.nombre) }
            }
            OrdenProtectoras.ALFABETICO_ZA -> {
                resultado.sortedByDescending { normalizar(it.nombre) }
            }
            OrdenProtectoras.MAS_CERCA -> {
                resultado.sortedWith(
                    compareBy<Protectora> {
                        when {
                            it.provincia.equals(st.miProvincia, ignoreCase = true) -> 0
                            it.comunidad.equals(st.miComunidad, ignoreCase = true) -> 1
                            else -> 2
                        }
                    }.thenBy { normalizar(it.nombre) }
                )
            }
            OrdenProtectoras.MAS_LEJOS -> {
                resultado.sortedWith(
                    compareByDescending<Protectora> {
                        when {
                            it.provincia.equals(st.miProvincia, ignoreCase = true) -> 2
                            it.comunidad.equals(st.miComunidad, ignoreCase = true) -> 1
                            else -> 0
                        }
                    }.thenBy { normalizar(it.nombre) }
                )
            }
        }

        _uiState.update { it.copy(filtradas = resultado) }
    }

    private fun normalizar(texto: String): String {
        return Normalizer
            .normalize(texto.lowercase(Locale.getDefault()), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .trim()
    }
}