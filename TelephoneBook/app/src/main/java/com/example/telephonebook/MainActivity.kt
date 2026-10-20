package com.example.telephonebook

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ContactAdapter
    private val fullContactList = mutableListOf<Contact>() // 原始完整清單
    private val displayedList = mutableListOf<Contact>()   // 畫面上顯示的清單（含過濾）

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 產生 30 筆以上的測試資料（對應作業要求）
        for (i in 1..35) {
            fullContactList.add(
                Contact(
                    name = "聯絡人 $i",
                    phone = "0912345${String.format("%03d", i)}",
                    email = "user$i@example.com"
                )
            )
        }
        displayedList.addAll(fullContactList)

        // 2. 設定 RecyclerView 與 Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ContactAdapter(displayedList) { contact ->
            // 刪除功能
            fullContactList.remove(contact)
            filterList(findViewById<EditText>(R.id.etSearch).text.toString())
        }
        recyclerView.adapter = adapter

        // 3. 點擊「新增聯絡人」按鈕
        findViewById<Button>(R.id.btnAddContact).setOnClickListener {
            showAddContactDialog()
        }

        // 4. 即時搜尋（過濾）功能
        findViewById<EditText>(R.id.etSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 彈出新增對話框
    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val etName = dialogView.findViewById<EditText>(R.id.etNameInput)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhoneInput)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmailInput)

        AlertDialog.Builder(this)
            .setTitle("新增聯絡人")
            .setView(dialogView)
            .setPositiveButton("確認新增") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val email = etEmail.text.toString().trim()

                if (name.isNotEmpty()) {
                    // 加入到完整清單最上方
                    fullContactList.add(0, Contact(name, phone, email))
                    // 重新整理目前的顯示清單（保留當前的搜尋條件）
                    filterList(findViewById<EditText>(R.id.etSearch).text.toString())
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 搜尋過濾邏輯（支援姓名、電話、Email 搜尋）
    private fun filterList(query: String) {
        val filtered = if (query.isEmpty()) {
            fullContactList
        } else {
            fullContactList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true)
            }
        }
        displayedList.clear()
        displayedList.addAll(filtered)
        adapter.updateList(displayedList)
    }
}