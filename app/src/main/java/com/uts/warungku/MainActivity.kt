package com.uts.warungku

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ==========================================
// TEMA DAN COLOR PALETTE
// ==========================================
val PrimaryYellow = Color(0xFFFFEB00)
val SecondaryPurple = Color(0xFF362486)
val BackgroundBone = Color(0xFFF8F7F2)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF362486)
val TextSecondary = Color(0xFF777777)
val ErrorRed = Color(0xFFD94343)

// ==========================================
// TUGAS 1: LINGKUNGAN & KOTLIN (CPMK-2)
// ==========================================

// 2. Data Class Produk
data class Produk(
    val id: Int,
    val nama: String,
    val harga: Int,
    val stok: Int,
    val kategori: String?
)

// 3. Tiga fungsi wajib
fun hitungNilaiStok(daftar: List<Produk>): Int {
    return daftar.sumOf { it.harga * it.stok }
}

fun produkTersedia(daftar: List<Produk>): List<Produk> {
    return daftar.filter { it.stok > 0 }.sortedBy { it.nama }
}

fun labelKategori(p: Produk): String {
    return p.kategori ?: "Umum"
}

// 4. Data Contoh (5 data awal)
val dataAwalProduk = listOf(
    Produk(1, "Indomie Goreng", 3000, 50, "Makanan"),
    Produk(2, "Beras Maknyus 5kg", 65000, 10, "Kebutuhan Pokok"),
    Produk(3, "Aqua Botol 600ml", 4000, 9, "Minuman"), // Stok 0
    Produk(4, "Korek Api Gas", 2000, 15, null),        // Kategori null
    Produk(5, "Roti Aoka Coklat", 2500, 20, "Makanan")
)

// ==========================================
// TUGAS 3: STATE & VIEWMODEL (CPMK-4)
// ==========================================
class ProdukViewModel : ViewModel() {
    // Menyimpan state menggunakan StateFlow dengan data awal dari Tugas 1
    private val _produkList = MutableStateFlow<List<Produk>>(dataAwalProduk)
    val produkList: StateFlow<List<Produk>> = _produkList.asStateFlow()

    fun tambahProduk(nama: String, harga: Int, stok: Int, kategori: String?) {
        val currentList = _produkList.value
        val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        val produkBaru = Produk(id = newId, nama = nama, harga = harga, stok = stok, kategori = kategori)
        _produkList.value = currentList + produkBaru
    }

    fun hapusProduk(produk: Produk) {
        _produkList.value = _produkList.value - produk
    }
}

// Utility untuk format Rp
fun formatRupiah(number: Int): String = "Rp ${"%,d".format(number).replace(',', '.')}"

// ==========================================
// MAIN ACTIVITY
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WarungKuApp()
        }
    }
}

@Composable
fun WarungKuApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PrimaryYellow,
            onPrimary = SecondaryPurple,
            background = BackgroundBone,
            surface = SurfaceWhite,
            error = ErrorRed
        )
    ) {
        WarungKuScreen()
    }
}

// ==========================================
// TUGAS 2: ANTARMUKA COMPOSE (CPMK-3)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarungKuScreen(viewModel: ProdukViewModel = viewModel()) {
    val produkList by viewModel.produkList.collectAsState()
    val totalNilaiStok = hitungNilaiStok(produkList)

    // 1. Tambahkan State untuk Snackbar dan Coroutine (wajib untuk pop-up)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { WarungKuTopBar() },
        // 2. Pasang SnackbarHost di dalam Scaffold
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // Mengecek isi pesan untuk menentukan warna
                val isHapus = data.visuals.message.contains("dihapus", ignoreCase = true)
                // Jika hapus = Merah (ErrorRed), jika tambah = Hijau (0xFF4CAF50)
                val backgroundColor = if (isHapus) ErrorRed else Color(0xFF4CAF50)

                Snackbar(
                    snackbarData = data,
                    containerColor = backgroundColor,
                    contentColor = Color.White, // Teks warna putih agar kontras
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        containerColor = BackgroundBone
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ringkasan Total
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryYellow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Nilai Stok", color = SecondaryPurple, fontSize = 14.sp)
                        Text(
                            text = formatRupiah(totalNilaiStok),
                            color = SecondaryPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                }
            }

            // Formulir Tambah
            item {
                FormProduk(
                    onTambah = { nama, harga, stok, kategori ->
                        viewModel.tambahProduk(nama, harga, stok, kategori)
                        // 3. Munculkan Notifikasi Berhasil Ditambah
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Produk berhasil ditambahkan ✅")
                        }
                    }
                )
            }

            // Daftar Produk
            item {
                Text(
                    text = "Daftar Produk",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            }

            items(produkList, key = { it.id }) { produk ->
                ProdukItem(
                    produk = produk,
                    onHapus = {
                        viewModel.hapusProduk(produk)
                        // 4. Munculkan Notifikasi Berhasil Dihapus
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Produk berhasil dihapus 🗑️")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarungKuTopBar() {
    TopAppBar(
        title = { Text("WarungKu", color = PrimaryYellow, fontWeight = FontWeight.Bold) },
        actions = { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = PrimaryYellow) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SecondaryPurple)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormProduk(onTambah: (String, Int, Int, String?) -> Unit) {
    // Menyimpan state isian formulir
    var nama by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var stok by remember { mutableStateOf("") }

    var kategoriExpanded by remember { mutableStateOf(false) }
    var kategoriTerpilih by remember { mutableStateOf("Makanan") }
    val kategoriOptions = listOf("Makanan", "Minuman", "Kebutuhan Pokok", "Umum (Null)")

    // State untuk Error Handling (Tugas 3.3)
    var isNamaError by remember { mutableStateOf(false) }
    var isHargaError by remember { mutableStateOf(false) }
    var isStokError by remember { mutableStateOf(false) }

    var errorHargaPesan by remember { mutableStateOf("") }
    var errorStokPesan by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tambah Produk", fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            // Field Nama
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it; isNamaError = false },
                label = { Text("Nama Produk") },
                isError = isNamaError,
                supportingText = { if (isNamaError) Text("Nama tidak boleh kosong") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryPurple,
                    focusedLabelColor = SecondaryPurple,
                    cursorColor = SecondaryPurple
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Field Harga
                OutlinedTextField(
                    value = harga,
                    onValueChange = { harga = it; isHargaError = false },
                    label = { Text("Harga") },
                    isError = isHargaError,
                    supportingText = { if (isHargaError) Text(errorHargaPesan) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryPurple,
                        focusedLabelColor = SecondaryPurple,
                        cursorColor = SecondaryPurple
                    )
                )
                // Field Stok
                OutlinedTextField(
                    value = stok,
                    onValueChange = { stok = it; isStokError = false },
                    label = { Text("Stok") },
                    isError = isStokError,
                    supportingText = { if (isStokError) Text(errorStokPesan) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryPurple,
                        focusedLabelColor = SecondaryPurple,
                        cursorColor = SecondaryPurple
                    )
                )
            }

            // Dropdown Kategori
            ExposedDropdownMenuBox(
                expanded = kategoriExpanded,
                onExpandedChange = { kategoriExpanded = !kategoriExpanded }
            ) {
                OutlinedTextField(
                    value = kategoriTerpilih,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = kategoriExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryPurple,
                        focusedLabelColor = SecondaryPurple,
                        cursorColor = SecondaryPurple
                    )
                )
                ExposedDropdownMenu(
                    expanded = kategoriExpanded,
                    onDismissRequest = { kategoriExpanded = false }
                ) {
                    kategoriOptions.forEach { pilihan ->
                        DropdownMenuItem(
                            text = { Text(pilihan) },
                            onClick = {
                                kategoriTerpilih = pilihan
                                kategoriExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Validasi (Tugas 3.3)
                    val hargaInt = harga.toIntOrNull()
                    val stokInt = stok.toIntOrNull()

                    isNamaError = nama.isBlank()

                    if (hargaInt == null || hargaInt <= 0) {
                        isHargaError = true
                        errorHargaPesan = "Angka > 0"
                    }

                    if (stokInt == null || stokInt < 0) {
                        isStokError = true
                        errorStokPesan = "Minimal 0"
                    }

                    if (!isNamaError && !isHargaError && !isStokError) {
                        // Jika memilih "Umum (Null)", kirimkan nilai null
                        val kategoriDikirim = if (kategoriTerpilih == "Umum (Null)") null else kategoriTerpilih
                        onTambah(nama, hargaInt!!, stokInt!!, kategoriDikirim)

                        // Kosongkan formulir
                        nama = ""
                        harga = ""
                        stok = ""
                        kategoriTerpilih = "Makanan"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = SecondaryPurple)
            ) {
                Text("Tambah", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Komponen Stateless
@Composable
fun ProdukItem(produk: Produk, onHapus: () -> Unit) {
    val isHabis = produk.stok == 0

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = produk.nama, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                // Memanggil fungsi labelKategori
                Text(text = labelKategori(produk), fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                // Format "Rp 15000"
                Text(text = "Harga: ${formatRupiah(produk.harga)}", fontSize = 14.sp)
                Text(text = "Stok: ${produk.stok}", fontSize = 14.sp)

                Spacer(modifier = Modifier.height(6.dp))
                // Penanda visual stok 0
                Surface(
                    color = if (isHabis) ErrorRed else SecondaryPurple,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isHabis) "Stok Habis" else "Tersedia",
                        color = SurfaceWhite,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(onClick = onHapus) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ErrorRed)
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun WarungKuPreview() {
    MaterialTheme {
        WarungKuScreen(viewModel = ProdukViewModel())
    }
}