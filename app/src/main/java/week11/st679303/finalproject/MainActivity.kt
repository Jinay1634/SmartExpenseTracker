package week11.st679303.finalproject

import android.net.Uri
import android.os.Bundle
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.engage.common.datamodel.Image
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import week11.st679303.finalproject.model.BillItem
import week11.st679303.finalproject.ui.theme.SmartExpenseTrackerTheme
import week11.st679303.finalproject.viewmodel.MainViewModel
import week7.st991662903.midpractice.utils.UiState
import java.io.IOException


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val uiState by vm.uiState.collectAsState()
            val results by vm.results.collectAsState()
            when (uiState) {
                UiState.Loading -> Text("Loading...")
                UiState.AuthRequired -> LoginScreen(vm)
                UiState.Authenticated -> BillScreen(vm)
                UiState.ReportList ->BillList(vm,results)
            }
        }
    }
}
@Composable
fun LoginScreen(vm: MainViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    val context = LocalContext.current
    val message by vm.message.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            vm.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Smart Expense Tracker",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE)
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            TextButton(
                onClick = {
                    resetEmail = email
                    showForgotPasswordDialog = true
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp)
            ) {
                Text("Forgot Password?", color = Color(0xFF6200EE))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { vm.login(email, password) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Login")
                }

                Button(
                    onClick = {
                        vm.signUp(email, password)
                        Toast.makeText(context, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                        email=""
                        password=""
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5))
                ) {
                    Text("Sign Up", color = Color.Black)
                }
            }
        }

        if (showForgotPasswordDialog) {
            Dialog(onDismissRequest = { showForgotPasswordDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Reset Password",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            "Enter your email address to receive a password reset link.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showForgotPasswordDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    vm.forgotPassword(resetEmail)
                                    showForgotPasswordDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Send Link")
                            }
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val extractedText = remember { mutableStateOf("") }
    val company = remember { mutableStateOf("") }
    val total = remember { mutableStateOf("") }
    val date = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("Other") }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val showCategoryDropdown = remember { mutableStateOf(false) }
    val currentUser = vm.auth.currentUser

    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val scope = rememberCoroutineScope()

    val categories = listOf(
        "Food & Dining",
        "Groceries",
        "Gas & Fuel",
        "Shopping",
        "Pharmacy",
        "Electronics",
        "Home Improvement",
        "Other"
    )

    fun parseDateString(dateString: String): Date? {
        val dateFormats = listOf(
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
            SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()),
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()),
            SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()),
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        )

        for (format in dateFormats) {
            try {
                format.isLenient = false
                return format.parse(dateString)
            } catch (e: Exception) {
            }
        }
        return null
    }

    fun inferCategoryFromCompany(companyName: String): String {
        val keywords = mapOf(
            "Food & Dining" to listOf("restaurant", "cafe", "coffee", "pizza", "burger", "grill", "diner", "bistro", "kitchen", "bar"),
            "Groceries" to listOf("grocery", "supermarket", "market", "food", "walmart", "target", "costco", "aldi", "kroger"),
            "Gas & Fuel" to listOf("gas", "fuel", "petro", "shell", "exxon", "chevron", "bp", "mobil", "station"),
            "Shopping" to listOf("store", "shop", "retail", "mall", "boutique", "outlet", "fashion"),
            "Pharmacy" to listOf("pharmacy", "drug", "cvs", "walgreens", "rite aid", "medical"),
            "Electronics" to listOf("electronics", "tech", "computer", "best buy", "apple"),
            "Home Improvement" to listOf("hardware", "depot", "lowes", "home improvement")
        )

        val lowerCompany = companyName.lowercase()
        return keywords.entries.firstOrNull { (_, words) ->
            words.any { lowerCompany.contains(it) }
        }?.key ?: "Other"
    }

    fun extractDateFromText(text: String): String {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        val datePatterns = listOf(
            Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{4})"""),
            Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})"""),
            Regex("""(\d{4})[/-](\d{1,2})[/-](\d{1,2})"""),
            Regex("""(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\s+(\d{1,2}),?\s+(\d{4})"""),
            Regex("""(?i)(\d{1,2})\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\s+(\d{4})""")
        )

        for (line in lines) {
            if (line.lowercase().contains("date") ||
                line.lowercase().contains("invoice") ||
                line.lowercase().contains("receipt")) {

                for (pattern in datePatterns) {
                    val match = pattern.find(line)
                    if (match != null) {
                        return match.value
                    }
                }
            }

            for (pattern in datePatterns) {
                val match = pattern.find(line)
                if (match != null) {
                    return match.value
                }
            }
        }

        val currentDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            .format(Date())
        return currentDate
    }

    fun extractDetailsFromText(text: String) {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var foundCompany = false
        var foundTotal = false
        var largestAmount = 0.0

        date.value = extractDateFromText(text)

        for ((index, line) in lines.withIndex()) {
            if (!foundCompany && index < 10) {
                val skipPatterns = listOf(
                    Regex("(?i)^receipt$"),
                    Regex("(?i)address[:\\s]"),
                    Regex("(?i)tel[:\\s]"),
                    Regex("(?i)phone[:\\s]"),
                    Regex("(?i)date[:\\s]"),
                    Regex("^[0-9]{2}[-/][0-9]{2}[-/][0-9]{4}"),
                    Regex("^[0-9]{2}:[0-9]{2}"),
                    Regex("^[-=.]+$"),
                    Regex("^[0-9]+\\.[0-9]{2}$")
                )

                if (skipPatterns.none { it.containsMatchIn(line) } &&
                    line.length in 3..50 &&
                    line.count { it.isLetter() } >= 2) {
                    company.value = line
                    foundCompany = true
                }
            }

            val amountMatch = Regex("""([0-9]{1,3}(?:,?[0-9]{3})*\.[0-9]{2})""").findAll(line)
            for (match in amountMatch) {
                val amount = match.value.replace(",", "").toDoubleOrNull() ?: 0.0
                if (amount > largestAmount) {
                    largestAmount = amount
                }
            }

            val totalMatch = Regex("""(?i)(?:total|amount|balance)[:\s]*\$?\s*([0-9]{1,3}(?:,?[0-9]{3})*\.[0-9]{2})""")
                .find(line)
            if (totalMatch != null) {
                val amount = totalMatch.groups[1]?.value?.replace(",", "") ?: "0"
                total.value = amount
                foundTotal = true
            }
        }

        if (!foundTotal && largestAmount > 0) {
            total.value = String.format("%.2f", largestAmount)
        }

        if (!foundCompany) {
            company.value = "Unknown"
        }

        if (total.value.isEmpty() || total.value == "0" || total.value == "0.00") {
            total.value = if (largestAmount > 0) String.format("%.2f", largestAmount) else "0.00"
        }

        category.value = inferCategoryFromCompany(company.value)
    }

    fun runOCRAndExtract() {
        val uri = imageUri.value ?: return
        isLoading.value = true
        errorMessage.value = null
        scope.launch {
            try {
                val image = withContext(Dispatchers.IO) {
                    InputImage.fromFilePath(context, uri)
                }
                val result = withContext(Dispatchers.IO) {
                    recognizer.process(image).await()
                }

                val cleanedText = result.text.trim()
                extractedText.value = cleanedText

                if (cleanedText.isNotEmpty()) {
                    extractDetailsFromText(cleanedText)
                } else {
                    errorMessage.value = "No text detected in the image. Please try another receipt."
                    company.value = ""
                    total.value = ""
                    date.value = ""
                    category.value = "Other"
                }
            } catch (e: Exception) {
                errorMessage.value = "Failed to process image: ${e.localizedMessage ?: "Unknown error"}"
                extractedText.value = ""
                company.value = ""
                total.value = ""
                date.value = ""
                category.value = "Other"
            } finally {
                isLoading.value = false
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri.value = uri
            runOCRAndExtract()
        } else {
            errorMessage.value = "No image selected"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Receipt Scanner",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Scan and manage your receipts easily",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    currentUser?.email?.let { email ->
                        Text(
                            "Logged in as: $email",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(
                        onClick = { vm.goToBillList() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "View All",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    TextButton(
                        onClick = { vm.logout() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            "Logout",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    imageUri.value?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = "Selected receipt",
                            modifier = Modifier
                                .height(240.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentScale = ContentScale.Fit
                        )
                    } ?: Box(
                        modifier = Modifier
                            .height(240.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No receipt selected",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading.value
                    ) {
                        Text("Select Receipt Image", fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (isLoading.value) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Extracting details from receipt...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            errorMessage.value?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            if (company.value.isNotEmpty() || total.value.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Receipt Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        OutlinedTextField(
                            value = company.value,
                            onValueChange = { company.value = it },
                            label = { Text("Company / Store Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = date.value,
                            onValueChange = { date.value = it },
                            label = { Text("Date") },
                            placeholder = { Text("MM/DD/YYYY") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = total.value,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    total.value = it
                                }
                            },
                            label = { Text("Total Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = showCategoryDropdown.value,
                            onExpandedChange = { showCategoryDropdown.value = it }
                        ) {
                            OutlinedTextField(
                                value = category.value,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown.value)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = showCategoryDropdown.value,
                                onDismissRequest = { showCategoryDropdown.value = false }
                            ) {
                                categories.forEach { categoryOption ->
                                    DropdownMenuItem(
                                        text = { Text(categoryOption) },
                                        onClick = {
                                            category.value = categoryOption
                                            showCategoryDropdown.value = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                when {
                                    company.value.isBlank() -> {
                                        errorMessage.value = "Please enter a company name"
                                    }
                                    date.value.isBlank() -> {
                                        errorMessage.value = "Please enter a date"
                                    }
                                    total.value.isBlank() || total.value.toDoubleOrNull() == null -> {
                                        errorMessage.value = "Please enter a valid amount"
                                    }
                                    else -> {
                                        val parsedDate = parseDateString(date.value)

                                        if (parsedDate != null) {

                                            vm.addBill(
                                                cname = company.value,
                                                amount = total.value,
                                                pdate = parsedDate,
                                                category = category.value
                                            )

                                            Toast.makeText(
                                                context,
                                                "Receipt saved successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            company.value = ""
                                            total.value = ""
                                            date.value = ""
                                            category.value = "Other"
                                            imageUri.value = null
                                            extractedText.value = ""
                                            errorMessage.value = null
                                        } else {
                                            errorMessage.value = "Invalid date format. Please use MM/DD/YYYY"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = company.value.isNotBlank() && total.value.isNotBlank() && date.value.isNotBlank()
                        ) {
                            Text("Save Receipt", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun BillList(vm: MainViewModel, Bills: List<BillItem>) {
    val categories = listOf("All", "Food & Dining", "Groceries", "Gas & Fuel", "Shopping", "Pharmacy", "Electronics", "Home Improvement", "Other")
    val selectedCategory = remember { mutableStateOf("All") }
    val filteredBills = remember(Bills, selectedCategory.value) {
        if (selectedCategory.value == "All") Bills
        else Bills.filter { it.category == selectedCategory.value }
    }

    val totalAmount = remember(filteredBills) {
        filteredBills.sumOf { it.amount?.toDoubleOrNull() ?: 0.0 }
    }

    val editingBill = remember { mutableStateOf<BillItem?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "My Receipts",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "${filteredBills.size} receipts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                "$${String.format("%.2f", totalAmount)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory.value == category,
                        onClick = { selectedCategory.value = category },
                        label = {
                            Text(
                                category,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedCategory.value == category) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            if (filteredBills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "📄",
                            style = MaterialTheme.typography.displayLarge,
                            fontSize = 80.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No receipts yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Start scanning to add your receipts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBills) { bill ->
                        BillItemCard(
                            bill = bill,
                            vm = vm,
                            isEditing = editingBill.value?.id == bill.id,
                            onEditClick = { editingBill.value = bill },
                            onCancelEdit = { editingBill.value = null }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { vm.backToAddBill() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Add Bill",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillItemCard(
    bill: BillItem,
    vm: MainViewModel,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val categoryColors = mapOf(
        "Food & Dining" to Color(0xFFFF6B6B),
        "Groceries" to Color(0xFF4ECDC4),
        "Gas & Fuel" to Color(0xFFFFA726),
        "Shopping" to Color(0xFFAB47BC),
        "Pharmacy" to Color(0xFF26C6DA),
        "Electronics" to Color(0xFF5C6BC0),
        "Home Improvement" to Color(0xFF8D6E63),
        "Other" to Color(0xFF78909C)
    )

    val categoryEmojis = mapOf(
        "Food & Dining" to "🍽️",
        "Groceries" to "🛒",
        "Gas & Fuel" to "⛽",
        "Shopping" to "🛍️",
        "Pharmacy" to "💊",
        "Electronics" to "📱",
        "Home Improvement" to "🏠",
        "Other" to "📦"
    )

    val categoryColor = categoryColors[bill.category] ?: Color.Gray
    val categoryEmoji = categoryEmojis[bill.category] ?: "📦"

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = bill.pdate?.let { dateFormat.format(it) } ?: "Unknown date"

    val editedCname = remember(bill) { mutableStateOf(bill.cname ?: "") }
    val editedAmount = remember(bill) { mutableStateOf(bill.amount ?: "") }
    val editedCategory = remember(bill) { mutableStateOf(bill.category ?: "Other") }
    val editedDate = remember(bill) { mutableStateOf(bill.pdate ?: Date()) }
    val showDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = editedDate.value.time)

    val categories = listOf("Food & Dining", "Groceries", "Gas & Fuel", "Shopping", "Pharmacy", "Electronics", "Home Improvement", "Other")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isEditing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = editedCname.value,
                    onValueChange = { editedCname.value = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = editedAmount.value,
                    onValueChange = { editedAmount.value = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                var expandedCategory by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = editedCategory.value,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    editedCategory.value = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = dateFormat.format(editedDate.value),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Purchase Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker.value = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .clickable { showDatePicker.value = true }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "📅",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                )

                if (showDatePicker.value) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker.value = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    editedDate.value = Date(it)
                                }
                                showDatePicker.value = false
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker.value = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onCancelEdit,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            vm.updateBill(
                                billId = bill.id,
                                cname = editedCname.value,
                                amount = editedAmount.value,
                                category = editedCategory.value,
                                pdate = editedDate.value
                            )
                            onCancelEdit()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = categoryColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = categoryEmoji,
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 32.sp
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = bill.cname ?: "Unknown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(8.dp))

                        Surface(
                            color = categoryColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = bill.category ?: "Other",
                                style = MaterialTheme.typography.bodyMedium,
                                color = categoryColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📅",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${bill.amount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row {
                        TextButton(
                            onClick = onEditClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "✏️",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }

                        Spacer(Modifier.width(4.dp))

                        TextButton(
                            onClick = { vm.deleteBill(bill.id) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = "🗑️",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            }
        }
    }
}









