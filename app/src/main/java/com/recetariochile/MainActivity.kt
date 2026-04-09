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
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

// Paleta de colores basada en la imagen (Dark Theme)
val DarkBg = Color(0xFF0F1115)
val SurfaceDark = Color(0xFF1C1F26)
val AccentPurple = Color(0xFFB0B0F0)
val OnSurfaceText = Color(0xFFFFFFFF)
val SecondaryGray = Color(0xFF949BA5)

private val AppDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color.Black,
    onPrimaryContainer = Color.White,
    background = DarkBg,
    onBackground = OnSurfaceText,
    surface = SurfaceDark,
    onSurface = OnSurfaceText,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = SecondaryGray
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar AdMob
        MobileAds.initialize(this) {}

        setContent {
            MaterialTheme(colorScheme = AppDarkColorScheme) {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        is Screen.Main -> MainRecipeScreen(onRecipeClick = { receta ->
                            currentScreen = when (receta.nombre) {
                                "Completos" -> Screen.CalculadoraCompletos
                                "Humitas" -> Screen.CalculadoraHumitas
                                else -> Screen.Main
                            }
                        })
                        is Screen.CalculadoraCompletos -> {
                            BackHandler { currentScreen = Screen.Main }
                            CalculadoraCompletosScreen(onBack = { currentScreen = Screen.Main })
                        }
                        is Screen.CalculadoraHumitas -> {
                            BackHandler { currentScreen = Screen.Main }
                            CalculadoraHumitasScreen(onBack = { currentScreen = Screen.Main })
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
    object CalculadoraHumitas : Screen()
}

data class Receta(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val imagenRes: Int
)

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    // ID de bloque de anuncios de prueba de Google
    // Reemplazar con tu ID real de AdMob cuando publiques la app
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainRecipeScreen(onRecipeClick: (Receta) -> Unit) {
    val recetas = remember {
        listOf(
            Receta(1, "Completos", "El clásico chileno con palta, tomate y mayo.", R.drawable.banner_completos),
            Receta(2, "Humitas", "Deliciosas humitas de choclo.", R.drawable.banner_humitas)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Al Ojo", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            AdBanner()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(DarkBg)
        ) {
            val pagerState = rememberPagerState(pageCount = { recetas.size })

            LaunchedEffect(Unit) {
                while (true) {
                    yield()
                    delay(3000)
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                                    .background(Color.Black.copy(alpha = 0.6f))
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = OnSurfaceText
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
            containerColor = SurfaceDark
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
                    color = OnSurfaceText
                )
                Text(
                    text = receta.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGray,
                    maxLines = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculadoraCompletosScreen(onBack: () -> Unit) {
    var personasInput by remember { mutableStateOf("1") }
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
                title = { Text("Completos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            AdBanner()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(DarkBg)
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner_completos),
                contentDescription = "Completos Italianos",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = personasInput,
                    onValueChange = { personasInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Personas", color = SecondaryGray) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = SecondaryGray,
                        unfocusedBorderColor = SecondaryGray.copy(alpha = 0.5f),
                        focusedLabelColor = OnSurfaceText,
                        cursorColor = AccentPurple
                    )
                )
                OutlinedTextField(
                    value = porPersonaInput,
                    onValueChange = { porPersonaInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Completos p/p", color = SecondaryGray) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = SecondaryGray,
                        unfocusedBorderColor = SecondaryGray.copy(alpha = 0.5f),
                        focusedLabelColor = OnSurfaceText,
                        cursorColor = AccentPurple
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Total: $totalCompletos completos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = OnSurfaceText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Lista de compras estimada:",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceText
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ingredientes) { ing ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ing.nombre, 
                                color = SecondaryGray,
                                fontSize = 16.sp
                            )
                            Text(
                                text = ing.cantidad,
                                color = AccentPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculadoraHumitasScreen(onBack: () -> Unit) {
    var personasInput by remember { mutableStateOf("1") }
    var porPersonaInput by remember { mutableStateOf("2") }

    val personas = personasInput.toIntOrNull() ?: 0
    val porPersona = porPersonaInput.toIntOrNull() ?: 0
    val totalHumitas = personas * porPersona

    val ingredientes = listOf(
        Ingrediente("Choclos (grandes)", "${totalHumitas} unidades"),
        Ingrediente("Cebolla (mediana)", "${Math.ceil(totalHumitas * 0.5).toInt()} unidades"),
        Ingrediente("Hojas de albahaca", "${totalHumitas * 4} unidades")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Humitas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            AdBanner()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(DarkBg)
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner_humitas),
                contentDescription = "Humitas",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = personasInput,
                    onValueChange = { personasInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Personas", color = SecondaryGray) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = SecondaryGray,
                        unfocusedBorderColor = SecondaryGray.copy(alpha = 0.5f),
                        focusedLabelColor = OnSurfaceText,
                        cursorColor = AccentPurple
                    )
                )
                OutlinedTextField(
                    value = porPersonaInput,
                    onValueChange = { porPersonaInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Humitas p/p", color = SecondaryGray) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = SecondaryGray,
                        unfocusedBorderColor = SecondaryGray.copy(alpha = 0.5f),
                        focusedLabelColor = OnSurfaceText,
                        cursorColor = AccentPurple
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Total: $totalHumitas humitas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = OnSurfaceText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Lista de compras estimada:",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceText
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ingredientes) { ing ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ing.nombre, 
                                color = SecondaryGray,
                                fontSize = 16.sp
                            )
                            Text(
                                text = ing.cantidad,
                                color = AccentPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class Ingrediente(val nombre: String, val cantidad: String)
