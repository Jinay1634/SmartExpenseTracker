package week11.st679303.finalproject.model

import com.google.firebase.firestore.DocumentId

data class BillItem(
    val useremail:String?=null,
    val cname: String? = null,
    val amount: String? = null,
    val category: String? = null,
    @DocumentId
    val id : String=""
)