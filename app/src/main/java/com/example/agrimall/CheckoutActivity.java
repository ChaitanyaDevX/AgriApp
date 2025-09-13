package com.example.agrimall;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity implements PaymentResultListener {

    private String amountInPaise;
    private TextView tvTotalPrice;
    private EditText etName, etPhone, etAddress;
    private Button btnPlaceOrder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Preload Razorpay checkout
        Checkout.preload(getApplicationContext());

        // Initialize views
        tvTotalPrice = findViewById(R.id.tv_total_price);
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        btnPlaceOrder = findViewById(R.id.btn_place_order);

        // Get total price from intent
        int totalPrice = getIntent().getIntExtra("TOTAL_PRICE", 0);
        tvTotalPrice.setText("Total: ₹ " + totalPrice);

        // Razorpay requires amount in paise
        amountInPaise = String.valueOf(totalPrice * 100);

        // Start payment on button click
        btnPlaceOrder.setOnClickListener(v -> startPayment());
    }

    private void startPayment() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_IJUuv4ZM1Qs42V"); // ⚠️ Replace with your Razorpay Key

        try {
            JSONObject options = new JSONObject();
            options.put("name", "AgriMall");
            options.put("description", "Order Payment");
            options.put("currency", "INR");
            options.put("amount", amountInPaise);

            JSONObject prefill = new JSONObject();
            prefill.put("email", "test@agrimall.com");
            prefill.put("contact", phone);
            options.put("prefill", prefill);

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Payment error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Toast.makeText(this, "Payment Successful! ID: " + razorpayPaymentID, Toast.LENGTH_LONG).show();
        saveOrderToFirestore();
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();
    }

    private void saveOrderToFirestore() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Order details
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("fullName", name);
        orderData.put("phone", phone);
        orderData.put("address", address);
        orderData.put("totalAmount", amountInPaise);
        orderData.put("timestamp", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));

        // Cart items
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        List<Map<String, Object>> productList = new ArrayList<>();

        for (CartItem item : cartItems) {
            Map<String, Object> product = new HashMap<>();
            product.put("name", item.getProductName());
            product.put("price", item.getProductPrice());
            product.put("quantity", item.getQuantity());
            product.put("total", item.getTotalPrice());
            product.put("image", item.getProductImage());
            product.put("status", "delivered"); // Test purpose
            productList.add(product);
        }

        orderData.put("products", productList);

        // Save order in Firestore
        db.collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Order Saved in Firebase!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save order", Toast.LENGTH_LONG).show();
                });
    }
}
