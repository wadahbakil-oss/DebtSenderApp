package com.example.debtsender

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    
    private lateinit var customerSpinner: Spinner
    private lateinit var debtAmountInput: EditText
    private lateinit var currentDebtText: TextView
    private lateinit var sendWhatsAppBtn: Button
    private lateinit var sendSmsBtn: Button
    
    private lateinit var customerManager: CustomerManager
    private var customers = mutableListOf<Customer>()
    private var selectedCustomer: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        customerManager = CustomerManager(this)
        customers = customerManager.getCustomers()
        
        // ربط العناصر
        customerSpinner = findViewById(R.id.customerSpinner)
        debtAmountInput = findViewById(R.id.debtAmountInput)
        currentDebtText = findViewById(R.id.currentDebtText)
        sendWhatsAppBtn = findViewById(R.id.sendWhatsAppBtn)
        sendSmsBtn = findViewById(R.id.sendSmsBtn)
        
        // تعبئة أسماء العملاء في القائمة المنسدلة
        val customerNames = customers.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, customerNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        customerSpinner.adapter = adapter
        
        // عند اختيار عميل
        customerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedCustomer = customers[position]
                currentDebtText.text = "الرصيد الحالي: ${selectedCustomer?.debt} ريال"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // زر إرسال عبر واتساب
        sendWhatsAppBtn.setOnClickListener {
            sendViaWhatsApp()
        }
        
        // زر إرسال عبر SMS
        sendSmsBtn.setOnClickListener {
            sendViaSms()
        }
    }
    
    private fun sendViaWhatsApp() {
        val customer = selectedCustomer ?: return
        val amount = debtAmountInput.text.toString().toDoubleOrNull() ?: 0.0
        
        if (amount == 0.0) {
            Toast.makeText(this, "الرجاء إدخال المبلغ", Toast.LENGTH_SHORT).show()
            return
        }
        
        val newTotal = customer.debt + amount
        val message = "الاخ ${customer.name}، نود اعلامكم بان تم تقيد مبلغ $amount ريال ليصبح اجمالي رصيدكم علينا $newTotal ريال."
        
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=+967${customer.phone}&text=${URLEncoder.encode(message, "UTF-8")}")
            }
            startActivity(intent)
            
            // تحديث الرصيد بعد الإرسال (اختياري)
            customerManager.updateCustomerDebt(customer.name, newTotal)
            debtAmountInput.text.clear()
            Toast.makeText(this, "تم فتح واتساب، اضغط إرسال", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "واتساب غير مثبت على جهازك", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sendViaSms() {
        val customer = selectedCustomer ?: return
        val amount = debtAmountInput.text.toString().toDoubleOrNull() ?: 0.0
        
        if (amount == 0.0) {
            Toast.makeText(this, "الرجاء إدخال المبلغ", Toast.LENGTH_SHORT).show()
            return
        }
        
        val newTotal = customer.debt + amount
        val message = "الاخ ${customer.name}، نود اعلامكم بان تم تقيد مبلغ $amount ريال ليصبح اجمالي رصيدكم علينا $newTotal ريال."
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:+967${customer.phone}")
            putExtra("sms_body", message)
        }
        startActivity(intent)
        
        // تحديث الرصيد
        customerManager.updateCustomerDebt(customer.name, newTotal)
        debtAmountInput.text.clear()
    }
}
