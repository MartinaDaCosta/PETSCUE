package com.example.petscue.ui.novedades.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationPickerUiState(
    val query: String = "",
    val selectedLocation: SelectedLocation? = null,
    val suggestions: List<SelectedLocation> = emptyList()
)

class LocationPickerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LocationPickerUiState())
    val uiState: StateFlow<LocationPickerUiState> = _uiState.asStateFlow()

    fun updateQuery(value: String) {
        _uiState.value = _uiState.value.copy(query = value)
    }

    fun setSuggestions(items: List<SelectedLocation>) {
        _uiState.value = _uiState.value.copy(suggestions = items)
    }

    fun selectLocation(location: SelectedLocation) {
        _uiState.value = _uiState.value.copy(
            selectedLocation = location,
            query = location.address,
            suggestions = emptyList()
        )
    }

    fun setCurrentLocation(address: String, latLng: LatLng) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedLocation = SelectedLocation(
                    address = address,
                    lat = latLng.latitude,
                    lng = latLng.longitude
                ),
                query = address
            )
        }
    }
}