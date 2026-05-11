package com.example.debtsender

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CustomerManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("customers", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveCustomers(customers: List<Customer>) {
        val json = gson.toJson(customers)
        prefs.edit().putString("customers_list", json).apply()
    }

    fun getCustomers(): MutableList<Customer> {
        val json = prefs.getString("customers_list", "")
        val type = object : TypeToken<MutableList<Customer>>() {}.type
        return if (json.isNullOrEmpty()) {
            // العملاء الافتراضيون (من صورتك - أضف الباقين بنفس الطريقة)
            mutableListOf(
                Customer("ناصر الضبي", "770066128", 2500.00),
                Customer("وليد الزعيمي", "771234567", 2400.00),
                Customer("عبدالله شعبان", "772345678", 3950.00),
                Customer("محمد عطية اخبار", "773456789", 5200.00),
                Customer("مرجان سيف", "774567890", 5820.00)
            )
        } else {
            gson.fromJson(json, type)
        }
    }
    
    fun updateCustomerDebt(name: String, newDebt: Double) {
        val customers = getCustomers()
        val index = customers.indexOfFirst { it.name == name }
        if (index != -1) {
            customers[index].debt = newDebt
            saveCustomers(customers)
        }
    }
}
