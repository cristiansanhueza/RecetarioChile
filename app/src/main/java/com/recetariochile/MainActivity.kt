package com.recetariochile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Main -> MainRecipeScreen(onRecipeClick = { receta ->
                            if (receta.nombre == "Completos") {
                                currentScreen = Screen.CalculadoraCompletos
                            }
                        })
                        is Screen.CalculadoraCompletos -> {
                            BackHandler { currentScreen = Screen.Main }
                            CalculadoraCompletosScreen(onBack = { currentScreen = Screen.Main })
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Main : Screen()
    object CalculadoraCompletos : Screen()
}

data class Receta(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val imagenRes: Int
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainRecipeScreen(onRecipeClick: (Receta) -> Unit) {
    val recetas = remember {
        listOf(
            Receta(1, "Completos", "El clásico chileno con palta, tomate y mayo.", R.drawable.banner_completos),
            Receta(2, "Empanadas de Pino", "Tradicionales empanadas al horno.", R.drawable.banner_completos),
            Receta(3, "Cazuela de Vacuno", "Sopa nutritiva con carne y verduras.", R.drawable.banner_completos),
            Receta(4, "Pastel de Choclo", "Delicioso pastel de maíz con pino.", R.drawable.banner_completos)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recetario Chile 🇨🇱", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val pagerState = rememberPagerState(pageCount = { recetas.size })

            // Efecto para el movimiento automático del carrusel
            LaunchedEffect(Unit) {
                while (true) {
                    yield()
                    delay(3000) // Cambia cada 3 segundos
                    val nextPage = (pagerState.currentPage + 1) % recetas.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 16.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    val receta = recetas[page]
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onRecipeClick(receta) }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = receta.imagenRes),
                                contentDescription = receta.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = receta.nombre,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Nuestras Recetas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recetas) { receta ->
                    RecipeListItem(receta, onRecipeClick)
                }
            }
        }
    }
}

@Composable
fun RecipeListItem(receta: Receta, onRecipeClick: (Receta) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRecipeClick(receta) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = receta.imagenRes),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = receta.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = receta.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculadoraCompletosScreen(onBack: () -> Unit) {
    var personasInput by remember { mutableStateOf("5") }
    var porPersonaInput by remember { mutableStateOf("2") }

    val personas = personasInput.toIntOrNull() ?: 0
    val porPersona = porPersonaInput.toIntOrNull() ?: 0
    val totalCompletos = personas * porPersona

    val ingredientes = listOf(
        Ingrediente("Pan de Completo", "$totalCompletos unidades"),
        Ingrediente("Vienesas", "$totalCompletos unidades"),
        Ingrediente("Paltas", "${Math.ceil(totalCompletos * 0.3).toInt()} unidades"),
        Ingrediente("Tomates", "${Math.ceil(totalCompletos * 0.25).toInt()} unidades")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calculadora de Completos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner_completos),
                contentDescription = "Completos Italianos",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = personasInput,
                    onValueChange = { if (it.length <= 3) personasInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Personas") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = porPersonaInput,
                    onValueChange = { if (it.length <= 2) porPersonaInput = it.filter { c -> c.isDigit() } },
                    label = { Text("C/U") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total: $totalCompletos completos",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Lista de Compras:",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ingredientes) { ing ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ing.nombre, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = ing.cantidad,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class Ingrediente(val nombre: String, val cantidad: String)
