package com.example.agrimall;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private EditText searchBar;
    private ImageButton micButton;
    private TextView txtWelcome;

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI elements
        searchBar = view.findViewById(R.id.edtSearch);
        micButton = view.findViewById(R.id.mic_button);
        txtWelcome = view.findViewById(R.id.txtWelcome);
        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        recyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();

        // Load current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("name");
                            if (fullName != null && !fullName.isEmpty()) {
                                String firstName = fullName.split(" ")[0];
                                txtWelcome.setText("Hi " + firstName + "! 👋");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                    });
        }

        // Text search
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && !s.toString().trim().isEmpty()) {
                    searchProductsInFirestore(s.toString());
                } else {
                    loadProductsFromFirestore();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = searchBar.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchProductsInFirestore(keyword);
            } else {
                Toast.makeText(requireContext(), "Enter keyword to search", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        // Voice search
        micButton.setOnClickListener(v -> startVoiceRecognition());

        // Call now button
        view.findViewById(R.id.btnCallNow).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+918421726034"));
            startActivity(intent);
        });

        loadProductsFromFirestore();
        return view;
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Voice recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == getActivity().RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String keyword = result.get(0);
                searchBar.setText(keyword);
                searchProductsInFirestore(keyword);
            }
        }
    }

    private void searchProductsInFirestore(String keyword) {
        String keywordLower = keyword.toLowerCase();
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        productList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String name = doc.getString("name");
                            Double price = doc.getDouble("price");
                            String description = doc.getString("description");
                            String imageUrl = doc.getString("imageURL");

                            if (name != null && name.toLowerCase().contains(keywordLower)) {
                                productList.add(new Product(name, price != null ? price.intValue() : 0, description, imageUrl));
                            }
                        }
                        if (productList.isEmpty()) {
                            Toast.makeText(requireContext(), "No matching products found!", Toast.LENGTH_SHORT).show();
                        }
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(), "Search failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadProductsFromFirestore() {
        db.collection("products").addSnapshotListener((value, error) -> {
            if (error != null || value == null) {
                Toast.makeText(requireContext(), "Error loading products", Toast.LENGTH_SHORT).show();
                return;
            }
            productList.clear();
            for (QueryDocumentSnapshot doc : value) {
                String name = doc.getString("name");
                Double price = doc.getDouble("price");
                String description = doc.getString("description");
                String imageUrl = doc.getString("imageURL");
                productList.add(new Product(name, price != null ? price.intValue() : 0, description, imageUrl));
            }
            productAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onProductClick(Product product) {}
    @Override
    public void onAddToCart(Product product) {}
}
