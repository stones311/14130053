package com.example.car

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var items = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbrw: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edBrand = findViewById<EditText>(R.id.edBrand)
        val edModel = findViewById<EditText>(R.id.edModel)
        val edYear = findViewById<EditText>(R.id.edYear)
        val edPrice = findViewById<EditText>(R.id.edPrice)
        val btnInsert = findViewById<Button>(R.id.btnInsert)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val btnQuery = findViewById<Button>(R.id.btnQuery)
        val listView = findViewById<ListView>(R.id.listView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        dbrw = MyDBHelper(this).writableDatabase

        // 點擊 ListView 項目：自動將資料帶入上方的輸入框中，方便選擇進行修改或刪除
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedText = items[position]
            val brandMatch = Regex("廠牌:\\s*([^\\s\t]+)").find(selectedText)
            val brand = brandMatch?.groupValues?.get(1) ?: ""
            if (brand.isNotEmpty()) {
                val c = dbrw.rawQuery("SELECT * FROM myTable WHERE brand = ?", arrayOf(brand))
                if (c.moveToFirst()) {
                    edBrand.setText(c.getString(0))
                    edModel.setText(c.getString(1))
                    edYear.setText(c.getString(2))
                    edPrice.setText(c.getString(3))
                }
                c.close()
            }
        }

        btnInsert.setOnClickListener {
            if (edBrand.text.isEmpty() || edPrice.text.isEmpty()) {
                Toast.makeText(this, "請輸入廠牌與價格", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    dbrw.execSQL(
                        "INSERT INTO myTable(brand, model, year, price) VALUES(?, ?, ?, ?)",
                        arrayOf(
                            edBrand.text.toString(),
                            edModel.text.toString(),
                            edYear.text.toString(),
                            edPrice.text.toString()
                        )
                    )
                    Toast.makeText(this, "新增廠牌:${edBrand.text}", Toast.LENGTH_SHORT).show()
                    edBrand.text.clear()
                    edModel.text.clear()
                    edYear.text.clear()
                    edPrice.text.clear()
                    btnQuery.performClick()
                } catch (e: Exception) {
                    Toast.makeText(this, "新增失敗:$e", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnUpdate.setOnClickListener {
            if (edBrand.text.isEmpty()) {
                Toast.makeText(this, "請輸入或點選要修改的廠牌", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    dbrw.execSQL(
                        "UPDATE myTable SET model = ?, year = ?, price = ? WHERE brand LIKE ?",
                        arrayOf(
                            edModel.text.toString(),
                            edYear.text.toString(),
                            edPrice.text.toString(),
                            edBrand.text.toString()
                        )
                    )
                    Toast.makeText(this, "修改廠牌:${edBrand.text}", Toast.LENGTH_SHORT).show()
                    edBrand.text.clear()
                    edModel.text.clear()
                    edYear.text.clear()
                    edPrice.text.clear()
                    btnQuery.performClick()
                } catch (e: Exception) {
                    Toast.makeText(this, "修改失敗:$e", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnDelete.setOnClickListener {
            if (edBrand.text.isEmpty()) {
                Toast.makeText(this, "請輸入或點選要刪除的廠牌", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    dbrw.execSQL(
                        "DELETE FROM myTable WHERE brand LIKE ?",
                        arrayOf(edBrand.text.toString())
                    )
                    Toast.makeText(this, "刪除廠牌:${edBrand.text}", Toast.LENGTH_SHORT).show()
                    edBrand.text.clear()
                    edModel.text.clear()
                    edYear.text.clear()
                    edPrice.text.clear()
                    btnQuery.performClick()
                } catch (e: Exception) {
                    Toast.makeText(this, "刪除失敗:$e", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnQuery.setOnClickListener {
            val brandInput = edBrand.text.toString().trim()
            val c = if (brandInput.isEmpty()) {
                dbrw.rawQuery("SELECT * FROM myTable", null)
            } else {
                dbrw.rawQuery("SELECT * FROM myTable WHERE brand LIKE ?", arrayOf("%$brandInput%"))
            }
            c.moveToFirst()
            items.clear()
            Toast.makeText(this, "共有 ${c.count} 筆資料", Toast.LENGTH_SHORT).show()
            repeat(c.count) {
                val brand = c.getString(0)
                val model = c.getString(1)
                val year = c.getString(2)
                val price = c.getString(3)
                var info = "廠牌: $brand"
                if (!model.isNullOrEmpty()) info += "  車型: $model"
                if (!year.isNullOrEmpty()) info += "  年份: $year"
                info += "  價格: $price"
                items.add(info)
                c.moveToNext()
            }
            adapter.notifyDataSetChanged()
            c.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbrw.close()
    }
}
