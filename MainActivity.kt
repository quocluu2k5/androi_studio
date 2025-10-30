package com.example.productmanager

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productmanager.adapter.ProductAdapter
import com.example.productmanager.databinding.ActivityMainBinding
import com.example.productmanager.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var productAdapter: ProductAdapter
    private val productsList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Firestore
        db = Firebase.firestore

        // Setup RecyclerView
        setupRecyclerView()

        // Load products from Firestore
        loadProducts()

        // Add product button
        binding.fabAddProduct.setOnClickListener {
            showAddProductDialog()
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            productsList,
            onEditClick = { product -> showEditProductDialog(product) },
            onDeleteClick = { product -> deleteProduct(product) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = productAdapter
        }
    }

    private fun loadProducts() {
        db.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Product::class.java)?.apply {
                            id = doc.id
                        }
                    }
                    productsList.clear()
                    productsList.addAll(products)
                    productAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun showAddProductDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_product, null)
        val etName = dialogView.findViewById<EditText>(R.id.etProductName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etProductDescription)
        val etPrice = dialogView.findViewById<EditText>(R.id.etProductPrice)
        val etImageUrl = dialogView.findViewById<EditText>(R.id.etImageUrl)

        AlertDialog.Builder(this)
            .setTitle("Thêm Sản Phẩm")
            .setView(dialogView)
            .setPositiveButton("Thêm") { _, _ ->
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val priceText = etPrice.text.toString().trim()
                val imageUrl = etImageUrl.text.toString().trim()

                if (name.isEmpty() || priceText.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập tên và giá", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val price = priceText.toDoubleOrNull() ?: 0.0
                val product = Product(
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = imageUrl
                )

                addProduct(product)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditProductDialog(product: Product) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_product, null)
        val etName = dialogView.findViewById<EditText>(R.id.etProductName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etProductDescription)
        val etPrice = dialogView.findViewById<EditText>(R.id.etProductPrice)
        val etImageUrl = dialogView.findViewById<EditText>(R.id.etImageUrl)

        // Pre-fill with existing data
        etName.setText(product.name)
        etDescription.setText(product.description)
        etPrice.setText(product.price.toString())
        etImageUrl.setText(product.imageUrl)

        AlertDialog.Builder(this)
            .setTitle("Sửa Sản Phẩm")
            .setView(dialogView)
            .setPositiveButton("Cập Nhật") { _, _ ->
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val priceText = etPrice.text.toString().trim()
                val imageUrl = etImageUrl.text.toString().trim()

                if (name.isEmpty() || priceText.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập tên và giá", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val price = priceText.toDoubleOrNull() ?: 0.0
                val updatedProduct = Product(
                    id = product.id,
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = imageUrl
                )

                updateProduct(updatedProduct)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun addProduct(product: Product) {
        db.collection("products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProduct(product: Product) {
        val productMap = hashMapOf(
            "name" to product.name,
            "description" to product.description,
            "price" to product.price,
            "imageUrl" to product.imageUrl
        )

        db.collection("products")
            .document(product.id)
            .update(productMap as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteProduct(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Xác Nhận Xóa")
            .setMessage("Bạn có chắc muốn xóa sản phẩm '${product.name}'?")
            .setPositiveButton("Xóa") { _, _ ->
                db.collection("products")
                    .document(product.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Xóa thành công!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}