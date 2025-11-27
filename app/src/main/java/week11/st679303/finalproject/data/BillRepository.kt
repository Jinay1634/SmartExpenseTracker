package week11.st679303.finalproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st679303.finalproject.model.BillItem

class BillRepository{
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun addBillItem(bill: BillItem){
        db.collection("report").add(
            BillItem(useremail = auth.currentUser?.email, cname = bill.cname, amount = bill.amount, category = bill.category)
        ).await()
    }

    fun getBills(): Flow<List<BillItem>> = callbackFlow{
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val reg = db.collection("report").addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.toObjects(BillItem::class.java) ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

}