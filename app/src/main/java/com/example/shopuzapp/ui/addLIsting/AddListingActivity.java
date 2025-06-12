package com.example.shopuzapp.ui.addLIsting;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shopuzapp.DB.DatabaseContract;
import com.example.shopuzapp.DB.DatabaseHelper;
import com.example.shopuzapp.Geocoding.GeocodingHelper;
import com.example.shopuzapp.R;
import com.example.shopuzapp.databinding.ActivityAddListingBinding;
import com.example.shopuzapp.models.Listing;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class AddListingActivity extends AppCompatActivity {
    private ActivityAddListingBinding binding;
    private ImageView LisingPictureImageView;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri imageUri;
    private Button addListingButton;
    private EditText listingTitleET, listingDescriptionET, listingPriceET, listingLocationET;
    private DatabaseHelper dh;
    private String imageBlob = null;
    private ProgressBar listingProgressBar;
    private Map<String,String> location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddListingBinding.inflate(getLayoutInflater());
        dh = new DatabaseHelper(this);
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                if (imageUri != null) {
                    LisingPictureImageView.setImageURI(imageUri);
                    try {
                        downscaleAndConvertToBlob(imageUri);
                    } catch (IOException e) {
                        Log.e("ImageConversion", "Error converting image: " + e.getMessage());
                        Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                        imageBlob = null;
                    }
                }
            } else {
                // Image capture failed.
            }
        });
        initWidgets();
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void initWidgets(){
        LisingPictureImageView = binding.ListingPhotoImageView;
        addListingButton = binding.addListingButton;
        listingTitleET = binding.listingTitleET;
        listingDescriptionET = binding.listingDescriptionET;
        listingPriceET = binding.listingPriceET;
        listingProgressBar = binding.listingProgressBar;
        listingLocationET = binding.listingLocationET;

        addListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateListing();


                finish();
            }
        });
        LisingPictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    imageUri = createImageUri();
                } catch (IOException e){
                    Log.d("image",e.getMessage());
                }
                if (imageUri != null) {
                    takePictureLauncher.launch(imageUri);
                }

            }
        });
    }







    private Uri createImageUri() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getFilesDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        return FileProvider.getUriForFile(this, "com.example.shopuzapp.fileprovider", image);
    }
    private void submitListing(){
        Listing l = new Listing();
        l.setTitle(listingTitleET.getText().toString());
        l.setDescription(listingDescriptionET.getText().toString());
        if(l.getTitle().isEmpty() || l.getDescription().isEmpty()) return;
        Map<String,String> newListing = new HashMap<String,String>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("listings")
                .add(l)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Zapisano w firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Nie udalo sie zapisac w firestore", Toast.LENGTH_SHORT).show();
                });

    }
    private void downscaleAndConvertToBlob(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        int width = options.outWidth;
        int height = options.outHeight;
        int targetWidth = 720;
        int targetHeight = (int) (((double) height / width) * targetWidth);

        int sampleSize = 1;
        while (width / sampleSize > targetWidth || height / sampleSize > targetHeight) {
            sampleSize *= 2;
        }

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = sampleSize;
        inputStream = getContentResolver().openInputStream(uri);
        Bitmap scaledBitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions);
        inputStream.close();

        if (scaledBitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            imageBlob = Base64.encodeToString(byteArray, Base64.DEFAULT);
        } else {
            imageBlob = null;
            Log.e("ImageConversion", "Failed to decode bitmap");
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateListing(){
        listingProgressBar.setVisibility(View.VISIBLE);
        GeocodingHelper.geocode(listingLocationET.getText().toString(), new GeocodingHelper.GeocodingCallback() {
            @Override
            public void onResult(Map<String, String> result) {
                runOnUiThread(() -> {
                    listingProgressBar.setVisibility(View.GONE);
                    addListingButton.setEnabled(true);
                    listingLocationET.setEnabled(true);

                    if (result != null) {
                        location = result;
                    } else {
                        Toast.makeText(AddListingActivity.this, "No location found for: " + listingLocationET.getText().toString(), Toast.LENGTH_LONG).show();
                        location = null;
                    }
                    submitListingToFirestore();
                });

            }

            @Override
            public void onError(String message) {

                Log.d("geo","blad geocoding: "+message);
            }
        });
    }
    private void submitListingToFirestore(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String title = listingTitleET.getText().toString();
        String description = listingDescriptionET.getText().toString();
        double price = Double.parseDouble(listingPriceET.getText().toString());

        if(title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in title and description", Toast.LENGTH_SHORT).show();
            return;
        }




        Listing newListing = new Listing();
        newListing.setTitle(title);
        newListing.setDescription(description);
        newListing.setPrice(price);
        newListing.setOwnerId(auth.getUid());
        newListing.setLocation(location);
        if (imageBlob != null) {

            newListing.setImageBlob(imageBlob);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("listings")
                .add(newListing)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Zapisano w Firestore", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "zapisano w firestore");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Nie udało się zapisać w Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding document", e);
                });
    }
}