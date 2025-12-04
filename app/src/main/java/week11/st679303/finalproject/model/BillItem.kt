package week11.st679303.finalproject.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class BillItem(
    val useremail:String?=null,
    val cname: String? = null,
    val amount: String? = null,
    val category: String? = null,
    val pdate: Date?=null,
    @DocumentId
    val id : String=""
)