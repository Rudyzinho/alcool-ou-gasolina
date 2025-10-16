package com.rudy.lcoolougasolina

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.DecimalFormat


// declaração correta do DataStore (top-level)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stations_prefs")

// ------------------------------
// Data classes & helpers
// ------------------------------
data class Station(
    val id: Long,
    val name: String,
    val alcoholPrice: Double,
    val gasPrice: Double
)

private val gson = Gson()
private val STATIONS_PREF_KEY = stringPreferencesKey("stations_json")
private val THRESHOLD_PREF_KEY = stringPreferencesKey("threshold_str")

// ------------------------------
// Repository using DataStore + Gson
// ------------------------------
class StationPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private val listType = object : TypeToken<List<Station>>() {}.type

    val stationsFlow: Flow<List<Station>> = dataStore.data.map { prefs ->
        val json = prefs[STATIONS_PREF_KEY] ?: "[]"
        try {
            gson.fromJson<List<Station>>(json, listType) ?: emptyList()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    val thresholdFlow: Flow<Double> = dataStore.data.map { prefs ->
        val s = prefs[THRESHOLD_PREF_KEY] ?: "0.7"
        s.toDoubleOrNull() ?: 0.7
    }

    suspend fun readStationsOnce(): List<Station> {
        val prefs = dataStore.data.first()
        val json = prefs[STATIONS_PREF_KEY] ?: "[]"
        return try {
            gson.fromJson<List<Station>>(json, listType) ?: emptyList()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun saveStations(list: List<Station>) {
        val json = gson.toJson(list)
        dataStore.edit { prefs -> prefs[STATIONS_PREF_KEY] = json }
    }

    suspend fun saveThreshold(th: Double) {
        dataStore.edit { prefs -> prefs[THRESHOLD_PREF_KEY] = th.toString() }
    }
}

// ------------------------------
// ViewModel
// ------------------------------
class FuelViewModel(private val repo: StationPreferencesRepository) : ViewModel() {
    val stationsFlow = repo.stationsFlow
    val thresholdFlow = repo.thresholdFlow

    fun addStation(name: String, alcohol: Double, gas: Double) {
        viewModelScope.launch {
            val list = repo.readStationsOnce().toMutableList()
            val id = System.currentTimeMillis()
            list.add(0, Station(id = id, name = name, alcoholPrice = alcohol, gasPrice = gas))
            repo.saveStations(list)
        }
    }

    fun updateStation(updated: Station) {
        viewModelScope.launch {
            val list = repo.readStationsOnce().map { if (it.id == updated.id) updated else it }
            repo.saveStations(list)
        }
    }

    fun deleteStation(toDelete: Station) {
        viewModelScope.launch {
            val list = repo.readStationsOnce().filter { it.id != toDelete.id }
            repo.saveStations(list)
        }
    }

    fun setThreshold(v: Double) {
        viewModelScope.launch { repo.saveThreshold(v) }
    }
}

class FuelViewModelFactory(private val repo: StationPreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FuelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FuelViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

// ------------------------------
// UI / Activity
// ------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // create DataStore-based repo
        val repo = StationPreferencesRepository(this.dataStore)

        setContent {
            val vm: FuelViewModel = viewModel(factory = FuelViewModelFactory(repo))
            FuelAppWithPersistence(vm)
        }
    }
}

@Composable
fun FuelAppWithPersistence(vm: FuelViewModel) {
    val darkTheme = isSystemInDarkTheme()
    val colors = if (darkTheme) darkColors(
        primary = Color(0xFF90CAF9),
        onPrimary = Color.Black,
        background = Color(0xFF0F1720),
        surface = Color(0xFF111827),
        onSurface = Color.White
    ) else lightColors(
        primary = Color(0xFF1565C0),
        onPrimary = Color.White,
        background = Color(0xFFF3F6FB),
        surface = Color.White,
        onSurface = Color.Black
    )

    MaterialTheme(colors = colors) {
        Surface(modifier = Modifier.fillMaxSize()) {
            FuelScreenWithPersistence(vm)
        }
    }
}

@Composable
fun FuelScreenWithPersistence(vm: FuelViewModel) {
    val stations by vm.stationsFlow.collectAsState(initial = emptyList())
    val threshold by vm.thresholdFlow.collectAsState(initial = 0.7)

    var name by rememberSaveable { mutableStateOf("") }
    var alcohol by rememberSaveable { mutableStateOf("") }
    var gas by rememberSaveable { mutableStateOf("") }

    var editingStation by remember { mutableStateOf<Station?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Álcool ou Gasolina?", fontSize = 18.sp) },
                navigationIcon = {
                    Icon(Icons.Default.LocalGasStation, contentDescription = "ícone", modifier = Modifier.padding(12.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val a = alcohol.replace(',', '.').toDoubleOrNull()
                val g = gas.replace(',', '.').toDoubleOrNull()
                val nm = name.trim()
                if (nm.isNotBlank() && a != null && g != null && g > 0.0) {
                    vm.addStation(nm, a, g)
                    name = ""
                    alcohol = ""
                    gas = ""
                }
            }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(12.dp)) {

            Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp, shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Posto (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = alcohol,
                        onValueChange = { alcohol = it },
                        label = { Text("Preço do Álcool (R$)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = gas,
                        onValueChange = { gas = it },
                        label = { Text("Preço da Gasolina (R$)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Critério (percentual)", fontSize = 12.sp)
                            Text(if (threshold == 0.7) "70%" else "75%", style = MaterialTheme.typography.caption)
                        }
                        Switch(checked = threshold == 0.75, onCheckedChange = { vm.setThreshold(if (it) 0.75 else 0.7) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Comparações Salvas:", style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.height(8.dp))

            if (stations.isEmpty()) {
                Text("Nenhum posto salvo ainda.", style = MaterialTheme.typography.body2)
            } else {
                LazyColumn {
                    items(stations) { s ->
                        StationRow(s, threshold, onEdit = {
                            editingStation = s
                            showEditDialog = true
                        }, onDelete = {
                            vm.deleteStation(s)
                        })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        if (showEditDialog && editingStation != null) {
            EditStationDialog(editingStation!!, onDismiss = { showEditDialog = false }, onSave = { updated ->
                vm.updateStation(updated)
                showEditDialog = false
            })
        }
    }
}

@Composable
fun StationRow(station: Station, threshold: Double, onEdit: () -> Unit, onDelete: () -> Unit) {
    val df = remember { DecimalFormat("#,##0.00") }
    val ratio = if (station.gasPrice != 0.0) (station.alcoholPrice / station.gasPrice) * 100.0 else 0.0
    val decision = ratio <= threshold * 100.0
    Card(modifier = Modifier.fillMaxWidth(), elevation = 3.dp, shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {

            Column {
                Text(station.name.ifBlank { "Posto sem nome" }, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Álcool: R$ ${df.format(station.alcoholPrice)}   Gasolina: R$ ${df.format(station.gasPrice)}", style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Razão: ${df.format(ratio)}% — ${if (decision) "USE ÁLCOOL" else "USE GASOLINA"}", color = if (decision) Color(0xFF2E7D32) else Color(0xFFC62828))
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
            }
        }
    }
}

@Composable
fun EditStationDialog(station: Station, onDismiss: () -> Unit, onSave: (Station) -> Unit) {
    var name by rememberSaveable { mutableStateOf(station.name) }
    var alcohol by rememberSaveable { mutableStateOf(station.alcoholPrice.toString()) }
    var gas by rememberSaveable { mutableStateOf(station.gasPrice.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Posto") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = alcohol, onValueChange = { alcohol = it }, label = { Text("Álcool (R$)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = gas, onValueChange = { gas = it }, label = { Text("Gasolina (R$)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val a = alcohol.replace(',', '.').toDoubleOrNull()
                val g = gas.replace(',', '.').toDoubleOrNull()
                if (a != null && g != null) {
                    onSave(station.copy(name = name, alcoholPrice = a, gasPrice = g))
                }
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
