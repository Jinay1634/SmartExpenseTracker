package week11.st679303.finalproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st679303.finalproject.data.BillRepository
import week11.st679303.finalproject.model.BillItem
import week7.st991662903.midpractice.utils.UiState
import java.util.Date


class MainViewModel: ViewModel() {
    public val auth = FirebaseAuth.getInstance()
    private val repo = BillRepository()
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState
    private val _results = MutableStateFlow<List<BillItem>>(emptyList())
    val results: StateFlow<List<BillItem>> = _results
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user == null) {
                _uiState.value = UiState.AuthRequired
                _results.value = emptyList()
            } else {
                _uiState.value = UiState.Authenticated
                getBills()
            }
        }
    }
    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { _uiState.value = UiState.Authenticated }
            .addOnFailureListener { e ->
                _uiState.value = UiState.AuthRequired
                _message.value = e.localizedMessage ?: "Login failed"
            }
    }

    fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { _uiState.value = UiState.AuthRequired }
            .addOnFailureListener { e ->
                _uiState.value = UiState.AuthRequired
                _message.value = e.localizedMessage ?: "Sign up failed"
            }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = UiState.AuthRequired
    }

    fun addBill(cname: String,amount: String,category: String,pdate: Date){
        viewModelScope.launch {
            repo.addBillItem(BillItem(cname =cname,amount=amount, category = category, pdate = pdate))
            _uiState.value = UiState.ReportList
        }
    }
    private fun getBills() {
        viewModelScope.launch {
            repo.getBills().collect { list ->
                _results.value = list
            }
        }
    }

}