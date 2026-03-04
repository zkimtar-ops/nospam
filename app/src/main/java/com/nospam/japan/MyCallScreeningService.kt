package com.nospam.japan

import android.telecom.Call
import android.telecom.CallScreeningService
import com.google.firebase.firestore.FirebaseFirestore

/**
 * خدمة فحص المكالمات التي تعمل في الخلفية.
 * تقوم هذه الخدمة بمقارنة الأرقام الواردة مع القائمة السوداء في Firestore.
 */
class MyCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        // استخراج الرقم الوارد
        val phoneNumber = callDetails.handle.schemeSpecificPart
        
        // الاتصال بقاعدة بيانات Firestore
        val db = FirebaseFirestore.getInstance()
        
        // تحديد المسار حسب القواعد المتفق عليها
        val appId = "com-nospam-japan"
        val blockedCollection = db.collection("artifacts")
            .document(appId)
            .collection("public")
            .document("data")
            .collection("blocked_numbers")

        // البحث عن الرقم في القائمة السوداء
        blockedCollection.whereEqualTo("phone", phoneNumber).get()
            .addOnSuccessListener { documents ->
                val response = CallResponse.Builder()
                
                if (!documents.isEmpty) {
                    // إذا وجد الرقم، يتم حظر المكالمة ورفضها تماماً
                    response.setDisallowCall(true)
                    response.setRejectCall(true)
                    response.setSkipCallLog(false) // إظهارها في سجل المكالمات كـ محظورة
                    response.setSkipNotification(false) // إظهار إشعار بالحظر
                }
                
                // إرسال القرار للنظام
                respondToCall(callDetails, response.build())
            }
            .addOnFailureListener {
                // في حال وجود خطأ تقني، يتم السماح بالمكالمة لضمان عدم ضياع اتصالات هامة
                respondToCall(callDetails, CallResponse.Builder().build())
            }
    }
}
