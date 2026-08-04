package com.fitness.app.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.data.room.LoggedMeal;
import com.fitness.app.models.GroceryRecipe;
import com.fitness.app.models.ScannedIngredient;
import com.fitness.app.models.ShoppingListItem;
import com.fitness.app.models.User;
import com.fitness.app.models.WeeklyMealPlanDay;
import com.fitness.app.models.GroceryVoiceMessage;
import com.fitness.app.models.GroceryProductScan;
import com.fitness.app.utils.GroceryAiEngine;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class GroceryScannerActivity extends AppCompatActivity {

    private FitnessDao dao;
    private LocalDataManager localDb;
    private User currentUser;
    private Thread typingThread;

    // Navigation & Views
    private TabLayout tabLayout;
    private View loadingLayout;
    private TextView tvLoadingText;

    private View tabScannerLayout;
    private View tabRecipesLayout;
    private View tabShoppingLayout;
    private View tabPlannerLayout;
    private View tabVoiceLayout;

    private ImageView ivScannedPreview;
    private TextView tvVoiceAssistantResponse;
    private EditText etVoiceQuery;
    private View layoutImageActions;

    // RecyclerViews & Adapters
    private RecyclerView rvScannedIngredients;
    private RecyclerView rvRecipes;
    private RecyclerView rvShoppingList;
    private RecyclerView rvWeeklyPlanner;

    private IngredientAdapter ingredientAdapter;
    private RecipeAdapter recipeAdapter;
    private ShoppingAdapter shoppingAdapter;
    private PlannerAdapter plannerAdapter;

    private final List<ScannedIngredient> scannedIngredients = new ArrayList<>();
    private final List<GroceryRecipe> groceryRecipes = new ArrayList<>();
    private final List<ShoppingListItem> shoppingItems = new ArrayList<>();
    private final List<WeeklyMealPlanDay> weeklyPlanDays = new ArrayList<>();

    // Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> speechLauncher;
    private ActivityResultLauncher<Intent> cropLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    // Text to Speech
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_scanner);

        AppDatabase db = AppDatabase.getInstance(this);
        dao = db.fitnessDao();
        localDb = new LocalDataManager(this);
        currentUser = localDb.getUser();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Layouts
        tabLayout = findViewById(R.id.tabLayout);
        loadingLayout = findViewById(R.id.loadingLayout);
        tvLoadingText = findViewById(R.id.tvLoadingText);

        tabScannerLayout = findViewById(R.id.tabScannerLayout);
        tabRecipesLayout = findViewById(R.id.tabRecipesLayout);
        tabShoppingLayout = findViewById(R.id.tabShoppingLayout);
        tabPlannerLayout = findViewById(R.id.tabPlannerLayout);
        tabVoiceLayout = findViewById(R.id.tabVoiceLayout);

        ivScannedPreview = findViewById(R.id.ivScannedPreview);
        tvVoiceAssistantResponse = findViewById(R.id.tvVoiceAssistantResponse);
        etVoiceQuery = findViewById(R.id.etVoiceQuery);
        layoutImageActions = findViewById(R.id.layoutImageActions);

        // RecyclerViews
        rvScannedIngredients = findViewById(R.id.rvScannedIngredients);
        rvRecipes = findViewById(R.id.rvRecipes);
        rvShoppingList = findViewById(R.id.rvShoppingList);
        rvWeeklyPlanner = findViewById(R.id.rvWeeklyPlanner);

        setupRecyclerViews();
        setupLaunchers();
        setupTTS();

        // Triggers
        findViewById(R.id.btnCameraScan).setOnClickListener(v -> checkCameraPermissionAndLaunch());
        findViewById(R.id.btnGalleryScan).setOnClickListener(v -> openGallery());
        findViewById(R.id.btnAddShoppingItem).setOnClickListener(v -> showAddShoppingItemDialog());
        findViewById(R.id.btnVoiceMic).setOnClickListener(v -> startVoiceRecognition());
        findViewById(R.id.btnSendVoiceQuery).setOnClickListener(v -> processTypedVoiceQuery());

        findViewById(R.id.btnEditImage).setOnClickListener(v -> showEditImageOptionsDialog());
        findViewById(R.id.btnDeleteImage).setOnClickListener(v -> showDeleteImageConfirmationDialog());

        // Tab 2 Chips Setup
        findViewById(R.id.chipMealAll).setOnClickListener(v -> filterRecipesAndReload());
        findViewById(R.id.chipMealBreakfast).setOnClickListener(v -> filterRecipesAndReload());
        findViewById(R.id.chipMealLunch).setOnClickListener(v -> filterRecipesAndReload());
        findViewById(R.id.chipMealDinner).setOnClickListener(v -> filterRecipesAndReload());
        findViewById(R.id.chipMealSnack).setOnClickListener(v -> filterRecipesAndReload());

        findViewById(R.id.chipDietAll).setOnClickListener(v -> filterRecipesAndReload());
        findViewById(R.id.chipDietVeg).setOnClickListener(v -> filterRecipesAndReload());
        findViewById(R.id.chipDietVegan).setOnClickListener(v -> filterRecipesAndReload());
        findViewById(R.id.chipDietHighProtein).setOnClickListener(v -> filterRecipesAndReload());
        findViewById(R.id.chipDietLowCarb).setOnClickListener(v -> filterRecipesAndReload());
        findViewById(R.id.chipDietWeightLoss).setOnClickListener(v -> filterRecipesAndReload());
        findViewById(R.id.chipDietMuscleGain).setOnClickListener(v -> filterRecipesAndReload());

        // Tab 4 Regeneration Triggers
        findViewById(R.id.btnRegenerateFullWeek).setOnClickListener(v -> regenerateFullWeekPlanner());

        // Tab Switching
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTab(tab.getPosition());
                if (tab.getPosition() == 4) {
                    loadVoiceHistory();
                } else if (tab.getPosition() == 3) {
                    if (weeklyPlanDays.isEmpty()) {
                        regenerateFullWeekPlanner();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Initialize Image Action Visibility
        String activeUri = getActiveImageUri();
        if (activeUri != null) {
            ivScannedPreview.setImageURI(Uri.parse(activeUri));
            layoutImageActions.setVisibility(View.VISIBLE);
        } else {
            ivScannedPreview.setImageResource(R.drawable.onboarding_1);
            layoutImageActions.setVisibility(View.GONE);
        }

        // Load DB Data
        loadDataFromDatabase();
    }

    private void setupRecyclerViews() {
        ingredientAdapter = new IngredientAdapter(scannedIngredients);
        rvScannedIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvScannedIngredients.setAdapter(ingredientAdapter);

        recipeAdapter = new RecipeAdapter(groceryRecipes, this::showRecipeDetailDialog, this::toggleRecipeSavedState);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvRecipes.setAdapter(recipeAdapter);

        shoppingAdapter = new ShoppingAdapter(shoppingItems, this::onShoppingItemChanged, this::onShoppingItemDeleted, this::showEditShoppingItemDialog);
        rvShoppingList.setLayoutManager(new LinearLayoutManager(this));
        rvShoppingList.setAdapter(shoppingAdapter);

        plannerAdapter = new PlannerAdapter(weeklyPlanDays, this::showDayMealDetailDialog);
        rvWeeklyPlanner.setLayoutManager(new LinearLayoutManager(this));
        rvWeeklyPlanner.setAdapter(plannerAdapter);
    }

    private void setupLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) openCamera();
                    else Toast.makeText(this, "Camera permission is required to scan groceries.", Toast.LENGTH_SHORT).show();
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null && extras.get("data") instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            ivScannedPreview.setImageBitmap(bitmap);
                            
                            try {
                                File file = new File(getCacheDir(), "grocery_captured_" + System.currentTimeMillis() + ".jpg");
                                FileOutputStream out = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                out.flush();
                                out.close();
                                saveActiveImageUri(Uri.fromFile(file).toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            
                            runAiVisionScan(bitmap);
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // Validate MIME Type for Security
                            String mimeType = getContentResolver().getType(uri);
                            if (mimeType != null && !mimeType.startsWith("image/")) {
                                Toast.makeText(this, "Security Error: Invalid file format. Only image uploads are allowed.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            
                            ivScannedPreview.setImageURI(uri);
                            saveActiveImageUri(uri.toString());
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                                runAiVisionScan(bitmap);
                            } catch (Exception e) {
                                runAiVisionScan(null);
                            }
                        }
                    }
                }
        );

        cropLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String croppedUriStr = result.getData().getStringExtra("cropped_uri");
                        if (croppedUriStr != null) {
                            Uri uri = Uri.parse(croppedUriStr);
                            ivScannedPreview.setImageURI(uri);
                            saveActiveImageUri(croppedUriStr);
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                                runAiVisionScan(bitmap);
                            } catch (Exception e) {
                                runAiVisionScan(null);
                            }
                        }
                    }
                }
        );

        speechLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            String spoken = matches.get(0);
                            etVoiceQuery.setText(spoken);
                            processTypedVoiceQuery();
                        }
                    }
                }
        );
    }

    private void setupTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void speakAloud(String text) {
        if (tts != null) {
            String cleanText = text.replaceAll("\\*+", "").replaceAll("#+", "");
            tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Camera not available on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gallery launcher failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask what to cook with your groceries...");
        try {
            speechLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition is not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getActiveImageUri() {
        return getSharedPreferences("grocery_prefs", MODE_PRIVATE).getString("active_image_uri", null);
    }

    private void saveActiveImageUri(String uri) {
        getSharedPreferences("grocery_prefs", MODE_PRIVATE).edit().putString("active_image_uri", uri).apply();
        runOnUiThread(() -> {
            if (uri != null) {
                layoutImageActions.setVisibility(View.VISIBLE);
            } else {
                layoutImageActions.setVisibility(View.GONE);
            }
        });
    }

    private void showEditImageOptionsDialog() {
        String[] options = {"Rotate 90°", "Crop Image", "Replace from Camera", "Replace from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Edit Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        rotateActiveImage();
                    } else if (which == 1) {
                        String activeUri = getActiveImageUri();
                        if (activeUri != null) {
                            Intent intent = new Intent(this, CropActivity.class);
                            intent.putExtra("image_uri", activeUri);
                            cropLauncher.launch(intent);
                        } else {
                            Toast.makeText(this, "No image to crop", Toast.LENGTH_SHORT).show();
                        }
                    } else if (which == 2) {
                        checkCameraPermissionAndLaunch();
                    } else if (which == 3) {
                        openGallery();
                    }
                })
                .show();
    }

    private void rotateActiveImage() {
        String uriStr = getActiveImageUri();
        if (uriStr == null) return;
        try {
            Uri uri = Uri.parse(uriStr);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            File file = new File(getCacheDir(), "rotated_grocery_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            rotated.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            Uri rotatedUri = Uri.fromFile(file);
            ivScannedPreview.setImageURI(rotatedUri);
            saveActiveImageUri(rotatedUri.toString());
            runAiVisionScan(rotated);
        } catch (Exception e) {
            Toast.makeText(this, "Rotation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteImageConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to remove this photo and clear all scanned report data?")
                .setPositiveButton("Delete", (dialog, which) -> deleteActiveImageFlow())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteActiveImageFlow() {
        saveActiveImageUri(null);
        ivScannedPreview.setImageResource(R.drawable.onboarding_1);
        findViewById(R.id.cvProductReport).setVisibility(View.GONE);
        showLoading("Clearing scan database...");
        new Thread(() -> {
            dao.clearScannedIngredients();
            dao.clearGroceryRecipes();
            dao.clearProductScans();
            cleanUpTemporaryImages();
            loadDataFromDatabase();
            runOnUiThread(this::hideLoading);
        }).start();
    }

    private void cleanUpTemporaryImages() {
        try {
            File cacheDir = getCacheDir();
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith("rotated_grocery_") || file.getName().startsWith("grocery_captured_")) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runAiVisionScan(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "Error: Image bitmap is empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading("Analyzing grocery product package...");

        GroceryAiEngine.analyzeProductScan(this, bitmap, new GroceryAiEngine.ProductScanCallback() {
            @Override
            public void onSuccess(GroceryProductScan report) {
                hideLoading();
                cleanUpTemporaryImages();

                new Thread(() -> {
                    dao.insertProductScan(report);

                    // Map package ingredients dynamically to ScannedIngredient list
                    dao.clearScannedIngredients();
                    long now = System.currentTimeMillis();
                    String[] splitIngs = report.getIngredients().split(",");
                    for (String s : splitIngs) {
                        String trim = s.trim();
                        if (!trim.isEmpty()) {
                            dao.insertScannedIngredient(new ScannedIngredient(
                                    UUID.randomUUID().toString(),
                                    trim,
                                    report.getFoodCategory(),
                                    98,
                                    "1 unit",
                                    (int)(report.getCalories() / Math.max(1, splitIngs.length)),
                                    report.getProtein() / Math.max(1, splitIngs.length),
                                    report.getCarbs() / Math.max(1, splitIngs.length),
                                    report.getFat() / Math.max(1, splitIngs.length),
                                    report.getFiber() / Math.max(1, splitIngs.length),
                                    report.getSugar() / Math.max(1, splitIngs.length),
                                    report.getSodium() / Math.max(1, splitIngs.length),
                                    report.getVitaminsMinerals(),
                                    "None",
                                    "Fresh",
                                    "",
                                    now
                             ));
                        }
                    }

                    List<ScannedIngredient> activeIngs = dao.getAllScannedIngredients();

                    runOnUiThread(() -> {
                        displayProductReportCard(report);
                        triggerAiRecipeGeneration(activeIngs);
                    });
                }).start();
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                cleanUpTemporaryImages();
                new AlertDialog.Builder(GroceryScannerActivity.this)
                        .setTitle("Scan Failed")
                        .setMessage(errorMsg)
                        .setPositiveButton("Try Again", null)
                        .show();
            }
        });
    }

    private void displayProductReportCard(GroceryProductScan report) {
        findViewById(R.id.cvProductReport).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tvReportProductName)).setText(report.getProductName());
        ((TextView) findViewById(R.id.tvReportBrandCategory)).setText(report.getBrand() + " • " + report.getFoodCategory());
        
        TextView tvHealth = findViewById(R.id.tvReportHealthScore);
        tvHealth.setText(report.getHealthScore() + "/100");
        if (report.getHealthScore() >= 80) tvHealth.setTextColor(0xFF22C55E); // Green
        else if (report.getHealthScore() >= 50) tvHealth.setTextColor(0xFFF59E0B); // Yellow
        else tvHealth.setTextColor(0xFFEF4444); // Red
        
        TextView tvNutrition = findViewById(R.id.tvReportNutritionScore);
        tvNutrition.setText(report.getNutritionScore() + "/100");
        if (report.getNutritionScore() >= 80) tvNutrition.setTextColor(0xFF22C55E);
        else if (report.getNutritionScore() >= 50) tvNutrition.setTextColor(0xFFF59E0B);
        else tvNutrition.setTextColor(0xFFEF4444);
        
        TextView tvRec = findViewById(R.id.tvReportRecommendation);
        tvRec.setText(report.getAiRecommendation());
        if ("Highly Recommended".equalsIgnoreCase(report.getAiRecommendation()) || "Recommended".equalsIgnoreCase(report.getAiRecommendation())) {
            tvRec.setTextColor(0xFF22C55E);
        } else if ("Avoid".equalsIgnoreCase(report.getAiRecommendation())) {
            tvRec.setTextColor(0xFFEF4444);
        } else {
            tvRec.setTextColor(0xFFF59E0B);
        }
        
        ((TextView) findViewById(R.id.tvReportWhy)).setText(report.getWhyRecommended());
        ((TextView) findViewById(R.id.tvReportIngredients)).setText(report.getIngredients());
        
        StringBuilder props = new StringBuilder();
        if (report.isHealthy()) props.append("• Healthy ");
        else props.append("• Unhealthy ");
        if (report.isHighlyProcessed()) props.append("• Processed ");
        if (report.isOrganic()) props.append("• Organic ");
        if (report.isSuitableWeightLoss()) props.append("• Weight Loss ");
        if (report.isSuitableWeightGain()) props.append("• Weight Gain ");
        if (report.isSuitableMuscleGain()) props.append("• Muscle Gain ");
        if (report.isSuitableDiabetic()) props.append("• Diabetic Safe ");
        
        ((TextView) findViewById(R.id.tvReportClassifications)).setText(props.toString());
        ((TextView) findViewById(R.id.tvReportAlternatives)).setText(report.getAlternativeProducts());
    }

    private void triggerAiRecipeGeneration(List<ScannedIngredient> ingredients) {
        runOnUiThread(() -> showLoading("Generating recipes based on scanned items..."));

        String mealFilter = getActiveMealFilter();
        String dietFilter = getActiveDietFilter();

        GroceryAiEngine.generateRecipesAi(this, ingredients, currentUser, mealFilter, dietFilter, new GroceryAiEngine.RecipesCallback() {
            @Override
            public void onSuccess(List<GroceryRecipe> recipes) {
                hideLoading();
                new Thread(() -> {
                    dao.clearGroceryRecipes();
                    for (GroceryRecipe r : recipes) {
                        dao.insertGroceryRecipe(r);
                    }
                    loadDataFromDatabase();
                }).start();
                Toast.makeText(GroceryScannerActivity.this, "AI Suggested Recipes loaded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                Toast.makeText(GroceryScannerActivity.this, "Error generating recipes: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRecipesAndReload() {
        new Thread(() -> {
            List<ScannedIngredient> activeIngs = dao.getAllScannedIngredients();
            if (activeIngs.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Scan groceries first to filter recipes.", Toast.LENGTH_SHORT).show());
                return;
            }
            triggerAiRecipeGeneration(activeIngs);
        }).start();
    }

    private String getActiveMealFilter() {
        int checkedId = ((com.google.android.material.chip.ChipGroup) findViewById(R.id.cgRecipeMealType)).getCheckedChipId();
        if (checkedId == R.id.chipMealBreakfast) return "Breakfast";
        if (checkedId == R.id.chipMealLunch) return "Lunch";
        if (checkedId == R.id.chipMealDinner) return "Dinner";
        if (checkedId == R.id.chipMealSnack) return "Snack";
        return "All Meals";
    }

    private String getActiveDietFilter() {
        int checkedId = ((com.google.android.material.chip.ChipGroup) findViewById(R.id.cgRecipeDietTarget)).getCheckedChipId();
        if (checkedId == R.id.chipDietVeg) return "Vegetarian";
        if (checkedId == R.id.chipDietVegan) return "Vegan";
        if (checkedId == R.id.chipDietHighProtein) return "High Protein";
        if (checkedId == R.id.chipDietLowCarb) return "Low Carb";
        if (checkedId == R.id.chipDietWeightLoss) return "Weight Loss";
        if (checkedId == R.id.chipDietMuscleGain) return "Muscle Gain";
        return "All Diets";
    }

    private void toggleRecipeSavedState(GroceryRecipe recipe) {
        recipe.setSaved(!recipe.isSaved());
        new Thread(() -> {
            dao.insertGroceryRecipe(recipe);
            runOnUiThread(() -> Toast.makeText(this, recipe.isSaved() ? "Recipe saved to favorites! ❤️" : "Removed from favorites", Toast.LENGTH_SHORT).show());
        }).start();
    }

    private void regenerateFullWeekPlanner() {
        showLoading("Generating custom personalized 7-Day Meal Plan...");

        GroceryAiEngine.generateWeeklyMealPlanAi(this, currentUser, new GroceryAiEngine.MealPlanCallback() {
            @Override
            public void onSuccess(List<WeeklyMealPlanDay> mealPlan) {
                hideLoading();
                new Thread(() -> {
                    dao.clearWeeklyMealPlan();
                    for (WeeklyMealPlanDay day : mealPlan) {
                        dao.insertWeeklyMealPlanDay(day);
                    }
                    
                    // Sync generated plan with Daily Meal Schedule
                    populateLoggedMealsFromWeeklyPlan(mealPlan);
                    
                    loadDataFromDatabase();
                }).start();
                Toast.makeText(GroceryScannerActivity.this, "Weekly Meal Plan Calculated successfully! 📅", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMsg) {
                hideLoading();
                Toast.makeText(GroceryScannerActivity.this, "Error generating meal plan: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDayMealDetailDialog(WeeklyMealPlanDay dayPlan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.dialog_day_meal_detail, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        tvTitle.setText(dayPlan.getDayName() + " Meal Details");

        TextView tvTotals = view.findViewById(R.id.tvDialogTotals);
        tvTotals.setText(String.format(Locale.getDefault(), "Target: %d kcal • Water: %d ml", dayPlan.getTotalCalories(), dayPlan.getWaterIntakeMl()));

        TextView tvExpected = view.findViewById(R.id.tvDialogExpectedNutrition);
        tvExpected.setText(dayPlan.getExpectedNutrition());

        view.findViewById(R.id.btnDialogClose).setOnClickListener(v -> dialog.dismiss());

        // Bind Breakfast, snacks, dinner, lunch
        bindMealDetailView(view.findViewById(R.id.layoutBreakfast), "Breakfast", dayPlan, dialog);
        bindMealDetailView(view.findViewById(R.id.layoutMorningSnack), "Morning Snack", dayPlan, dialog);
        bindMealDetailView(view.findViewById(R.id.layoutLunch), "Lunch", dayPlan, dialog);
        bindMealDetailView(view.findViewById(R.id.layoutEveningSnack), "Evening Snack", dayPlan, dialog);
        bindMealDetailView(view.findViewById(R.id.layoutDinner), "Dinner", dayPlan, dialog);

        dialog.show();
    }

    private void bindMealDetailView(View mealView, String mealType, WeeklyMealPlanDay dayPlan, AlertDialog parentDialog) {
        String detailsJson = "";
        if ("Breakfast".equalsIgnoreCase(mealType)) detailsJson = dayPlan.getBreakfastDetailsJson();
        else if ("Morning Snack".equalsIgnoreCase(mealType)) detailsJson = dayPlan.getMorningSnackDetailsJson();
        else if ("Lunch".equalsIgnoreCase(mealType)) detailsJson = dayPlan.getLunchDetailsJson();
        else if ("Evening Snack".equalsIgnoreCase(mealType)) detailsJson = dayPlan.getEveningSnackDetailsJson();
        else if ("Dinner".equalsIgnoreCase(mealType)) detailsJson = dayPlan.getDinnerDetailsJson();

        TextView tvRecipeName = mealView.findViewById(R.id.tvMealRecipeName);
        TextView tvTimingServing = mealView.findViewById(R.id.tvMealTimingServing);
        TextView tvCalories = mealView.findViewById(R.id.tvMealCalories);
        TextView tvMacros = mealView.findViewById(R.id.tvMealMacros);
        TextView tvIngredients = mealView.findViewById(R.id.tvMealIngredients);
        TextView tvSteps = mealView.findViewById(R.id.tvMealSteps);
        TextView tvReplacements = mealView.findViewById(R.id.tvMealReplacements);
        ImageView ivIcon = mealView.findViewById(R.id.ivMealIcon);
        View btnRegen = mealView.findViewById(R.id.btnRegenerateSingleMeal);

        if ("Breakfast".equalsIgnoreCase(mealType)) ivIcon.setImageResource(R.drawable.onboarding_1);
        else if ("Lunch".equalsIgnoreCase(mealType)) ivIcon.setImageResource(R.drawable.onboarding_2);
        else if ("Dinner".equalsIgnoreCase(mealType)) ivIcon.setImageResource(R.drawable.onboarding_3);
        else ivIcon.setImageResource(R.drawable.onboarding_4);

        try {
            if (detailsJson != null && !detailsJson.trim().isEmpty() && !detailsJson.equals("{}")) {
                JSONObject obj = new JSONObject(detailsJson);
                String recipe = obj.optString("recipe", "Healthy Option");
                tvRecipeName.setText(recipe);
                
                int prep = obj.optInt("prepTimeMinutes", 10);
                int cook = obj.optInt("cookTimeMinutes", 15);
                String serving = obj.optString("servingSize", "1 serving");
                tvTimingServing.setText("Prep: " + prep + "m • Cook: " + cook + "m • Serving: " + serving);
                
                int cal = obj.optInt("calories", 300);
                tvCalories.setText(cal + " kcal");
                
                double prot = obj.optDouble("protein", 20.0);
                double carb = obj.optDouble("carbs", 20.0);
                double fat = obj.optDouble("fat", 10.0);
                tvMacros.setText(String.format(Locale.getDefault(), "Prot: %.0fg • Carbs: %.0fg • Fat: %.0fg", prot, carb, fat));

                JSONArray ingArr = obj.optJSONArray("ingredients");
                if (ingArr != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < ingArr.length(); i++) {
                        sb.append("• ").append(ingArr.getString(i)).append("\n");
                    }
                    tvIngredients.setText(sb.toString().trim());
                } else {
                    tvIngredients.setText(obj.optString("ingredients", "No ingredients listed"));
                }

                JSONArray stepsArr = obj.optJSONArray("steps");
                if (stepsArr != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < stepsArr.length(); i++) {
                        sb.append(i + 1).append(". ").append(stepsArr.getString(i)).append("\n");
                    }
                    tvSteps.setText(sb.toString().trim());
                } else {
                    tvSteps.setText(obj.optString("steps", "No preparation steps listed"));
                }

                StringBuilder tipsAndReplacements = new StringBuilder();
                tipsAndReplacements.append("Difficulty: ").append(obj.optString("difficulty", "Easy")).append("\n\n");
                
                String benefits = obj.optString("healthBenefits", "");
                if (!benefits.isEmpty()) {
                    tipsAndReplacements.append("Health Benefits:\n").append(benefits).append("\n\n");
                }
                
                String replacements = obj.optString("healthierReplacements", "");
                if (!replacements.isEmpty()) {
                    tipsAndReplacements.append("Healthier Alternatives:\n").append(replacements).append("\n\n");
                }
                
                String storage = obj.optString("storageTips", "");
                if (storage.isEmpty()) {
                    storage = "Keep in airtight containers in the fridge for up to 3 days.";
                }
                tipsAndReplacements.append("Storage Tips:\n").append(storage);
                
                tvReplacements.setText(tipsAndReplacements.toString());
            } else {
                String fallbackName = "";
                int fallbackCal = 300;
                if ("Breakfast".equalsIgnoreCase(mealType)) {
                    fallbackName = dayPlan.getBreakfastRecipeName();
                    fallbackCal = dayPlan.getBreakfastCalories();
                } else if ("Lunch".equalsIgnoreCase(mealType)) {
                    fallbackName = dayPlan.getLunchRecipeName();
                    fallbackCal = dayPlan.getLunchCalories();
                } else if ("Dinner".equalsIgnoreCase(mealType)) {
                    fallbackName = dayPlan.getDinnerRecipeName();
                    fallbackCal = dayPlan.getDinnerCalories();
                } else {
                    fallbackName = dayPlan.getSnackRecipeName();
                    fallbackCal = dayPlan.getSnackCalories();
                }
                tvRecipeName.setText(fallbackName);
                tvTimingServing.setText("Prep: 10m • Cook: 15m • Serving: 1 serving");
                tvCalories.setText(fallbackCal + " kcal");
                tvMacros.setText("Prot: 25g • Carbs: 35g • Fat: 10g");
                tvIngredients.setText("• Main grocery ingredients list");
                tvSteps.setText("1. Mix ingredients and sauté.\n2. Cook on medium heat and serve.");
                tvReplacements.setText("Difficulty: Easy\n\nHealthier Alternatives:\nUse olive oil instead of butter.\n\nStorage Tips:\nEat fresh or store in fridge for 24 hours.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnRegen.setOnClickListener(v -> {
            parentDialog.dismiss();
            regenerateSingleMeal(dayPlan, mealType);
        });
    }

    private void regenerateSingleMeal(WeeklyMealPlanDay dayPlan, String mealType) {
        showLoading("Regenerating " + mealType + " meal via AI...");

        new Thread(() -> {
            try {
                String apiKey = GroceryAiEngine.processVoiceQuery(this, "", null, null, ""); // Fetch API Key
                apiKey = GroceryAiEngine.generateRecipesFromIngredients(new ArrayList<>(), null).isEmpty() ? null : getSharedPreferences("ai_prefs", MODE_PRIVATE).getString("gemini_api_key", null);
                
                if (apiKey != null && !apiKey.trim().isEmpty()) {
                    String prompt = "You are an expert nutritionist. Provide a single replacement healthy meal suggestion for " + mealType + ". "
                            + "The user's goal is: " + currentUser.getGoal() + ". "
                            + "Return a single JSON object with these exact keys:\n"
                            + "- 'recipe' (string)\n"
                            + "- 'calories' (int)\n"
                            + "- 'prepTimeMinutes' (int)\n"
                            + "- 'cookTimeMinutes' (int)\n"
                            + "- 'servingSize' (string)\n"
                            + "- 'protein' (double)\n"
                            + "- 'carbs' (double)\n"
                            + "- 'fat' (double)\n"
                            + "- 'ingredients' (array of strings)\n"
                            + "- 'steps' (array of strings)\n"
                            + "- 'healthierReplacements' (string)";

                    String json = GroceryAiEngine.processVoiceQuery(this, prompt, null, null, ""); 
                    // Call API key retrieval directly
                    SharedPreferences prefs = getSharedPreferences("ai_prefs", Context.MODE_PRIVATE);
                    String directKey = prefs.getString("gemini_api_key", null);
                    
                    java.lang.reflect.Method method = GroceryAiEngine.class.getDeclaredMethod("callGeminiTextAPI", String.class, String.class, boolean.class);
                    method.setAccessible(true);
                    String jsonResp = (String) method.invoke(null, directKey, prompt, true);

                    if (jsonResp != null && !jsonResp.trim().isEmpty()) {
                        JSONObject obj = new JSONObject(jsonResp);
                        String recipeName = obj.optString("recipe", "Alternative Meal");
                        int calories = obj.optInt("calories", 300);

                        if ("Breakfast".equalsIgnoreCase(mealType)) {
                            dayPlan.setBreakfastRecipeName(recipeName);
                            dayPlan.setBreakfastCalories(calories);
                            dayPlan.setBreakfastDetailsJson(jsonResp);
                        } else if ("Morning Snack".equalsIgnoreCase(mealType)) {
                            dayPlan.setSnackRecipeName(recipeName);
                            dayPlan.setSnackCalories(calories);
                            dayPlan.setMorningSnackDetailsJson(jsonResp);
                        } else if ("Lunch".equalsIgnoreCase(mealType)) {
                            dayPlan.setLunchRecipeName(recipeName);
                            dayPlan.setLunchCalories(calories);
                            dayPlan.setLunchDetailsJson(jsonResp);
                        } else if ("Evening Snack".equalsIgnoreCase(mealType)) {
                            dayPlan.setSnackRecipeName(recipeName);
                            dayPlan.setSnackCalories(calories);
                            dayPlan.setEveningSnackDetailsJson(jsonResp);
                        } else if ("Dinner".equalsIgnoreCase(mealType)) {
                            dayPlan.setDinnerRecipeName(recipeName);
                            dayPlan.setDinnerCalories(calories);
                            dayPlan.setDinnerDetailsJson(jsonResp);
                        }

                        dao.insertWeeklyMealPlanDay(dayPlan);
                        syncSingleMealToLoggedMeals(dayPlan, mealType, recipeName, calories, jsonResp);
                        
                        runOnUiThread(() -> {
                            hideLoading();
                            loadDataFromDatabase();
                            showDayMealDetailDialog(dayPlan);
                            Toast.makeText(this, mealType + " regenerated successfully! 🎉", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                }
                
                // Dynamic Local Fallback for single meal regeneration
                String recipeName = "Healthy " + mealType + " Option";
                int calories = "Breakfast".equalsIgnoreCase(mealType) ? 300 : ("Lunch".equalsIgnoreCase(mealType) ? 550 : ("Dinner".equalsIgnoreCase(mealType) ? 500 : 150));
                
                String[] breakfastsPool = {"Avocado Toast with Eggs", "Blueberry Almond Oats", "Greek Yogurt Parfait"};
                String[] lunchesPool = {"Chicken Broccoli Bowl", "Turkey Sweet Potato Mash", "Quinoa Veggie Salad"};
                String[] dinnersPool = {"Baked Cod & Asparagus", "Garlic Butter Chicken Noodles", "Turkey Stuffed Pepper"};
                String[] snacksPool = {"Apple slices & Peanut Butter", "Mixed nuts power mix", "Cottage cheese & pineapple"};
                
                if ("Breakfast".equalsIgnoreCase(mealType)) {
                    recipeName = breakfastsPool[(int)(Math.random() * breakfastsPool.length)];
                } else if ("Lunch".equalsIgnoreCase(mealType)) {
                    recipeName = lunchesPool[(int)(Math.random() * lunchesPool.length)];
                } else if ("Dinner".equalsIgnoreCase(mealType)) {
                    recipeName = dinnersPool[(int)(Math.random() * dinnersPool.length)];
                } else {
                    recipeName = snacksPool[(int)(Math.random() * snacksPool.length)];
                }
                
                // Get a realistic detail JSON using the helper in GroceryAiEngine
                java.lang.reflect.Method detailMethod = GroceryAiEngine.class.getDeclaredMethod("buildMealDetailJson", String.class, int.class, String.class, User.class);
                detailMethod.setAccessible(true);
                String simulatedJson = (String) detailMethod.invoke(null, recipeName, calories, mealType, currentUser);
                
                if ("Breakfast".equalsIgnoreCase(mealType)) {
                    dayPlan.setBreakfastRecipeName(recipeName);
                    dayPlan.setBreakfastCalories(calories);
                    dayPlan.setBreakfastDetailsJson(simulatedJson);
                } else if ("Morning Snack".equalsIgnoreCase(mealType)) {
                    dayPlan.setSnackRecipeName(recipeName);
                    dayPlan.setSnackCalories(calories);
                    dayPlan.setMorningSnackDetailsJson(simulatedJson);
                } else if ("Lunch".equalsIgnoreCase(mealType)) {
                    dayPlan.setLunchRecipeName(recipeName);
                    dayPlan.setLunchCalories(calories);
                    dayPlan.setLunchDetailsJson(simulatedJson);
                } else if ("Evening Snack".equalsIgnoreCase(mealType)) {
                    dayPlan.setSnackRecipeName(recipeName);
                    dayPlan.setSnackCalories(calories);
                    dayPlan.setEveningSnackDetailsJson(simulatedJson);
                } else if ("Dinner".equalsIgnoreCase(mealType)) {
                    dayPlan.setDinnerRecipeName(recipeName);
                    dayPlan.setDinnerCalories(calories);
                    dayPlan.setDinnerDetailsJson(simulatedJson);
                }
                
                dao.insertWeeklyMealPlanDay(dayPlan);
                syncSingleMealToLoggedMeals(dayPlan, mealType, recipeName, calories, simulatedJson);
                
                runOnUiThread(() -> {
                    hideLoading();
                    loadDataFromDatabase();
                    showDayMealDetailDialog(dayPlan);
                    Toast.makeText(this, mealType + " regenerated successfully (Local Mode)! 🎉", Toast.LENGTH_SHORT).show();
                });
                return;

            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                hideLoading();
                Toast.makeText(this, "AI Regeneration failed. Reverting to original.", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showRecipeDetailDialog(GroceryRecipe recipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.item_dialog_meal_detail, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        TextView tvRecipeName = view.findViewById(R.id.tvMealRecipeName);
        TextView tvTimingServing = view.findViewById(R.id.tvMealTimingServing);
        TextView tvCalories = view.findViewById(R.id.tvMealCalories);
        TextView tvMacros = view.findViewById(R.id.tvMealMacros);
        TextView tvIngredients = view.findViewById(R.id.tvMealIngredients);
        TextView tvSteps = view.findViewById(R.id.tvMealSteps);
        TextView tvReplacements = view.findViewById(R.id.tvMealReplacements);
        ImageView ivIcon = view.findViewById(R.id.ivMealIcon);
        View btnRegen = view.findViewById(R.id.btnRegenerateSingleMeal);

        btnRegen.setVisibility(View.GONE);

        tvRecipeName.setText(recipe.getName());
        tvTimingServing.setText("Prep: " + recipe.getPrepTimeMinutes() + "m • Cook: " + recipe.getCookTimeMinutes() + "m • Serving: " + recipe.getServingSize());
        tvCalories.setText(recipe.getCalories() + " kcal");
        tvMacros.setText(String.format(Locale.getDefault(), "Prot: %.0fg • Carbs: %.0fg • Fat: %.0fg", recipe.getProtein(), recipe.getCarbs(), recipe.getFat()));

        if ("Breakfast".equalsIgnoreCase(recipe.getMealType())) ivIcon.setImageResource(R.drawable.onboarding_1);
        else if ("Lunch".equalsIgnoreCase(recipe.getMealType())) ivIcon.setImageResource(R.drawable.onboarding_2);
        else if ("Dinner".equalsIgnoreCase(recipe.getMealType())) ivIcon.setImageResource(R.drawable.onboarding_3);
        else ivIcon.setImageResource(R.drawable.onboarding_4);

        try {
            JSONArray ingArr = new JSONArray(recipe.getIngredientsJson());
            StringBuilder sbIng = new StringBuilder();
            for (int i = 0; i < ingArr.length(); i++) {
                sbIng.append("• ").append(ingArr.getString(i)).append("\n");
            }
            tvIngredients.setText(sbIng.toString().trim());
        } catch (Exception e) {
            tvIngredients.setText(recipe.getIngredientsJson());
        }

        try {
            JSONArray stepsArr = new JSONArray(recipe.getStepsJson());
            StringBuilder sbSteps = new StringBuilder();
            for (int i = 0; i < stepsArr.length(); i++) {
                sbSteps.append(i + 1).append(". ").append(stepsArr.getString(i)).append("\n");
            }
            tvSteps.setText(sbSteps.toString().trim());
        } catch (Exception e) {
            tvSteps.setText(recipe.getStepsJson());
        }

        StringBuilder tipsAndReplacements = new StringBuilder();
        tipsAndReplacements.append("Difficulty: ").append(recipe.getDifficulty() == null || recipe.getDifficulty().trim().isEmpty() ? "Easy" : recipe.getDifficulty()).append("\n\n");
        if (recipe.getHealthBenefits() != null && !recipe.getHealthBenefits().trim().isEmpty()) {
            tipsAndReplacements.append("Health Benefits:\n").append(recipe.getHealthBenefits()).append("\n\n");
        }
        if (recipe.getHealthierReplacements() != null && !recipe.getHealthierReplacements().trim().isEmpty()) {
            tipsAndReplacements.append("Healthier Alternatives:\n").append(recipe.getHealthierReplacements()).append("\n\n");
        }
        tipsAndReplacements.append("Storage Tips:\nKeep in an airtight container in the refrigerator for up to 3 days. Reheat on low heat.");
        
        tvReplacements.setText(tipsAndReplacements.toString());

        dialog.show();
    }

    private void loadDataFromDatabase() {
        new Thread(() -> {
            List<ScannedIngredient> dbIngs = dao.getAllScannedIngredients();
            List<GroceryRecipe> dbRecs = dao.getAllGroceryRecipes();
            List<ShoppingListItem> dbShop = dao.getAllShoppingListItems();
            List<WeeklyMealPlanDay> dbPlan = dao.getWeeklyMealPlan();

            // Load last scan report if available
            List<GroceryProductScan> productScans = dao.getAllProductScans();
            
            runOnUiThread(() -> {
                scannedIngredients.clear();
                scannedIngredients.addAll(dbIngs);
                ingredientAdapter.notifyDataSetChanged();

                groceryRecipes.clear();
                groceryRecipes.addAll(dbRecs);
                recipeAdapter.notifyDataSetChanged();

                shoppingItems.clear();
                shoppingItems.addAll(dbShop);
                shoppingAdapter.notifyDataSetChanged();

                weeklyPlanDays.clear();
                weeklyPlanDays.addAll(dbPlan);
                plannerAdapter.notifyDataSetChanged();

                if (!productScans.isEmpty()) {
                    displayProductReportCard(productScans.get(0));
                } else {
                    findViewById(R.id.cvProductReport).setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void switchTab(int position) {
        tabScannerLayout.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        tabRecipesLayout.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        tabShoppingLayout.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
        tabPlannerLayout.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
        tabVoiceLayout.setVisibility(position == 4 ? View.VISIBLE : View.GONE);
    }

    private void showLoading(String msg) {
        tvLoadingText.setText(msg);
        tabScannerLayout.setVisibility(View.GONE);
        tabRecipesLayout.setVisibility(View.GONE);
        tabShoppingLayout.setVisibility(View.GONE);
        tabPlannerLayout.setVisibility(View.GONE);
        tabVoiceLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loadingLayout.setVisibility(View.GONE);
        switchTab(tabLayout.getSelectedTabPosition());
    }

    private void showAddShoppingItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        EditText etName = new EditText(this);
        etName.setHint("Item name (e.g. Avocado)");
        layout.addView(etName);

        EditText etCategory = new EditText(this);
        etCategory.setHint("Category (e.g. Produce)");
        layout.addView(etCategory);

        EditText etQuantity = new EditText(this);
        etQuantity.setHint("Quantity (e.g. 2 items)");
        layout.addView(etQuantity);

        builder.setTitle("Add Shopping List Item")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String category = etCategory.getText().toString().trim();
                    String quantity = etQuantity.getText().toString().trim();
                    if (!name.isEmpty()) {
                        ShoppingListItem item = new ShoppingListItem(
                                UUID.randomUUID().toString(),
                                name, category.isEmpty() ? "General" : category,
                                quantity.isEmpty() ? "1 unit" : quantity,
                                false, null, System.currentTimeMillis()
                        );
                        new Thread(() -> {
                            dao.insertShoppingListItem(item);
                            loadDataFromDatabase();
                        }).start();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditShoppingItemDialog(ShoppingListItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        EditText etName = new EditText(this);
        etName.setHint("Item name");
        etName.setText(item.getName());
        layout.addView(etName);

        EditText etCategory = new EditText(this);
        etCategory.setHint("Category");
        etCategory.setText(item.getCategory());
        layout.addView(etCategory);

        EditText etQuantity = new EditText(this);
        etQuantity.setHint("Quantity");
        etQuantity.setText(item.getQuantity());
        layout.addView(etQuantity);

        builder.setTitle("Edit Shopping List Item")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String category = etCategory.getText().toString().trim();
                    String quantity = etQuantity.getText().toString().trim();
                    if (!name.isEmpty()) {
                        item.setName(name);
                        item.setCategory(category.isEmpty() ? "General" : category);
                        item.setQuantity(quantity.isEmpty() ? "1 unit" : quantity);
                        new Thread(() -> {
                            dao.insertShoppingListItem(item);
                            loadDataFromDatabase();
                        }).start();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onShoppingItemChanged(ShoppingListItem item) {
        new Thread(() -> dao.insertShoppingListItem(item)).start();
    }

    private void onShoppingItemDeleted(ShoppingListItem item) {
        new Thread(() -> {
            dao.deleteShoppingListItem(item);
            loadDataFromDatabase();
        }).start();
    }

    private void loadVoiceHistory() {
        new Thread(() -> {
            List<GroceryVoiceMessage> messages = dao.getAllVoiceMessages();
            StringBuilder sb = new StringBuilder();
            if (messages.isEmpty()) {
                sb.append("Tap the microphone or type below to ask your AI Grocery Assistant!");
            } else {
                for (GroceryVoiceMessage msg : messages) {
                    if ("USER".equalsIgnoreCase(msg.getSender())) {
                        sb.append("👤 **You**: ").append(msg.getText()).append("\n\n");
                    } else {
                        sb.append("🤖 **AI**: ").append(msg.getText()).append("\n\n");
                    }
                }
            }
            runOnUiThread(() -> {
                tvVoiceAssistantResponse.setText(sb.toString().trim());
                View parent = (View) tvVoiceAssistantResponse.getParent();
                if (parent instanceof android.widget.ScrollView) {
                    ((android.widget.ScrollView) parent).post(() ->
                            ((android.widget.ScrollView) parent).fullScroll(View.FOCUS_DOWN)
                    );
                }
            });
        }).start();
    }

    private void processTypedVoiceQuery() {
        String query = etVoiceQuery.getText().toString().trim();
        if (query.isEmpty()) return;

        etVoiceQuery.setText("");

        // Interrupt previous typing animation thread if running
        if (typingThread != null && typingThread.isAlive()) {
            typingThread.interrupt();
        }

        new Thread(() -> {
            // Save user query
            GroceryVoiceMessage userMsg = new GroceryVoiceMessage(
                    UUID.randomUUID().toString(), "USER", query, System.currentTimeMillis()
            );
            dao.insertVoiceMessage(userMsg);
            
            // Render user query and typing indicator
            runOnUiThread(() -> {
                loadVoiceHistory();
                tvVoiceAssistantResponse.append("\n\n🤖 **AI**: thinking... ▌");
            });

            // Gather memory history
            List<GroceryVoiceMessage> messages = dao.getAllVoiceMessages();
            StringBuilder historyContext = new StringBuilder();
            int start = Math.max(0, messages.size() - 6);
            for (int i = start; i < messages.size(); i++) {
                GroceryVoiceMessage m = messages.get(i);
                historyContext.append(m.getSender()).append(": ").append(m.getText()).append("\n");
            }

            // Get response
            String answer = GroceryAiEngine.processVoiceQuery(this, query, scannedIngredients, currentUser, historyContext.toString());

            // Save AI reply
            GroceryVoiceMessage aiMsg = new GroceryVoiceMessage(
                    UUID.randomUUID().toString(), "AI", answer, System.currentTimeMillis()
            );
            dao.insertVoiceMessage(aiMsg);

            runOnUiThread(() -> {
                speakAloud(answer);
                animateChatHistoryStreaming(answer);
            });
        }).start();
    }

    private void animateChatHistoryStreaming(String lastAiMessage) {
        if (typingThread != null && typingThread.isAlive()) {
            typingThread.interrupt();
        }

        typingThread = new Thread(() -> {
            List<GroceryVoiceMessage> messages = dao.getAllVoiceMessages();
            StringBuilder sbBase = new StringBuilder();
            // Append all messages except the last one (which is the new AI response)
            for (int i = 0; i < messages.size() - 1; i++) {
                GroceryVoiceMessage msg = messages.get(i);
                if ("USER".equalsIgnoreCase(msg.getSender())) {
                    sbBase.append("👤 **You**: ").append(msg.getText()).append("\n\n");
                } else {
                    sbBase.append("🤖 **AI**: ").append(msg.getText()).append("\n\n");
                }
            }

            final String baseText = sbBase.toString();
            final String textToType = "🤖 **AI**: " + lastAiMessage;
            final int delayMs = 12; // Premium responsive typing speed
            
            final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            
            for (int i = 0; i <= textToType.length(); i++) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                final int index = i;
                handler.post(() -> {
                    tvVoiceAssistantResponse.setText(baseText + textToType.substring(0, index) + "▌");
                    View parent = (View) tvVoiceAssistantResponse.getParent();
                    if (parent instanceof android.widget.ScrollView) {
                        ((android.widget.ScrollView) parent).post(() ->
                                ((android.widget.ScrollView) parent).fullScroll(View.FOCUS_DOWN)
                        );
                    }
                });
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (!Thread.currentThread().isInterrupted()) {
                handler.post(() -> {
                    tvVoiceAssistantResponse.setText(baseText + textToType);
                    View parent = (View) tvVoiceAssistantResponse.getParent();
                    if (parent instanceof android.widget.ScrollView) {
                        ((android.widget.ScrollView) parent).post(() ->
                                ((android.widget.ScrollView) parent).fullScroll(View.FOCUS_DOWN)
                        );
                    }
                });
            }
        });
        typingThread.start();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    // --- RECYCLERVIEW ADAPTERS ---

    // 1. Ingredients Adapter
    private static class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
        private final List<ScannedIngredient> list;

        IngredientAdapter(List<ScannedIngredient> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scanned_ingredient, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ScannedIngredient item = list.get(position);
            holder.tvIngredientName.setText(item.getName());
            holder.tvConfidenceBadge.setText(String.format(Locale.getDefault(), "✓ %d%%", item.getConfidence()));
            holder.tvCategoryAndQty.setText(String.format("%s • %s", item.getCategory(), item.getEstimatedQuantity()));
            holder.tvFreshnessBadge.setText(item.getFreshness());

            if ("Expiring Soon".equalsIgnoreCase(item.getFreshness()) || "Expired".equalsIgnoreCase(item.getFreshness())) {
                holder.tvFreshnessBadge.setTextColor(0xFFEF4444);
            } else {
                holder.tvFreshnessBadge.setTextColor(0xFF22C55E);
            }

            holder.tvIngredientCalories.setText(String.format(Locale.getDefault(), "%d kcal", item.getCalories()));
            holder.tvIngredientProtein.setText(String.format(Locale.getDefault(), "P: %.1fg", item.getProtein()));
            holder.tvIngredientCarbs.setText(String.format(Locale.getDefault(), "C: %.1fg", item.getCarbs()));
            holder.tvIngredientFat.setText(String.format(Locale.getDefault(), "F: %.1fg", item.getFat()));

            holder.tvIngredientMicros.setText(String.format(Locale.getDefault(), "Fiber: %.1fg • Sugar: %.1fg • Sodium: %.0fmg",
                    item.getFiber(), item.getSugar(), item.getSodium()));
            
            holder.tvIngredientVitsMins.setText(String.format("Vitamins & Minerals: %s / %s",
                    (item.getVitamins() == null || item.getVitamins().trim().isEmpty()) ? "None" : item.getVitamins(),
                    (item.getMinerals() == null || item.getMinerals().trim().isEmpty()) ? "None" : item.getMinerals()));
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIngredientName, tvConfidenceBadge, tvCategoryAndQty, tvFreshnessBadge;
            TextView tvIngredientCalories, tvIngredientProtein, tvIngredientCarbs, tvIngredientFat;
            TextView tvIngredientMicros, tvIngredientVitsMins;

            ViewHolder(View itemView) {
                super(itemView);
                tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
                tvConfidenceBadge = itemView.findViewById(R.id.tvConfidenceBadge);
                tvCategoryAndQty = itemView.findViewById(R.id.tvCategoryAndQty);
                tvFreshnessBadge = itemView.findViewById(R.id.tvFreshnessBadge);
                tvIngredientCalories = itemView.findViewById(R.id.tvIngredientCalories);
                tvIngredientProtein = itemView.findViewById(R.id.tvIngredientProtein);
                tvIngredientCarbs = itemView.findViewById(R.id.tvIngredientCarbs);
                tvIngredientFat = itemView.findViewById(R.id.tvIngredientFat);
                tvIngredientMicros = itemView.findViewById(R.id.tvIngredientMicros);
                tvIngredientVitsMins = itemView.findViewById(R.id.tvIngredientVitsMins);
            }
        }
    }

    // 2. Recipe Adapter
    private interface RecipeClickListener {
        void onRecipeClicked(GroceryRecipe recipe);
    }
    private interface RecipeSavedListener {
        void onRecipeSavedToggled(GroceryRecipe recipe);
    }

    private static class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
        private final List<GroceryRecipe> list;
        private final RecipeClickListener clickListener;
        private final RecipeSavedListener savedListener;

        RecipeAdapter(List<GroceryRecipe> list, RecipeClickListener clickListener, RecipeSavedListener savedListener) { 
            this.list = list; 
            this.clickListener = clickListener;
            this.savedListener = savedListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grocery_recipe, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GroceryRecipe item = list.get(position);
            holder.tvRecipeMealType.setText(item.getMealType());
            holder.tvRecipeTiming.setText(String.format(Locale.getDefault(), "Prep: %dm • Cook: %dm", item.getPrepTimeMinutes(), item.getCookTimeMinutes()));
            holder.tvRecipeName.setText(item.getName());
            holder.tvRecipeBenefits.setText(item.getHealthBenefits());
            holder.tvRecipeCalories.setText(String.format(Locale.getDefault(), "%d kcal", item.getCalories()));
            holder.tvRecipeProtein.setText(String.format(Locale.getDefault(), "Protein: %.0fg", item.getProtein()));
            holder.tvRecipeCarbs.setText(String.format(Locale.getDefault(), "Carbs: %.0fg", item.getCarbs()));
            holder.tvRecipeFat.setText(String.format(Locale.getDefault(), "Fat: %.0fg", item.getFat()));
            holder.tvRecipeMissing.setText(String.format("💡 %s", item.getMissingIngredients()));

            holder.ivRecipeFavorite.setImageResource(item.isSaved() ? R.drawable.ic_heart_fav_filled : R.drawable.ic_heart_fav);

            holder.ivRecipeFavorite.setOnClickListener(v -> {
                if (savedListener != null) savedListener.onRecipeSavedToggled(item);
                holder.ivRecipeFavorite.setImageResource(item.isSaved() ? R.drawable.ic_heart_fav_filled : R.drawable.ic_heart_fav);
            });

            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onRecipeClicked(item);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRecipeMealType, tvRecipeTiming, tvRecipeName, tvRecipeBenefits;
            TextView tvRecipeCalories, tvRecipeProtein, tvRecipeCarbs, tvRecipeFat, tvRecipeMissing;
            ImageView ivRecipeFavorite;

            ViewHolder(View itemView) {
                super(itemView);
                tvRecipeMealType = itemView.findViewById(R.id.tvRecipeMealType);
                tvRecipeTiming = itemView.findViewById(R.id.tvRecipeTiming);
                tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
                tvRecipeBenefits = itemView.findViewById(R.id.tvRecipeBenefits);
                tvRecipeCalories = itemView.findViewById(R.id.tvRecipeCalories);
                tvRecipeProtein = itemView.findViewById(R.id.tvRecipeProtein);
                tvRecipeCarbs = itemView.findViewById(R.id.tvRecipeCarbs);
                tvRecipeFat = itemView.findViewById(R.id.tvRecipeFat);
                tvRecipeMissing = itemView.findViewById(R.id.tvRecipeMissing);
                ivRecipeFavorite = itemView.findViewById(R.id.ivRecipeFavorite);
            }
        }
    }

    // 3. Shopping List Adapter
    private interface ShoppingItemListener {
        void onItemChanged(ShoppingListItem item);
    }
    private interface ShoppingDeleteListener {
        void onItemDeleted(ShoppingListItem item);
    }
    private interface ShoppingEditListener {
        void onItemEdited(ShoppingListItem item);
    }

    private static class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {
        private final List<ShoppingListItem> list;
        private final ShoppingItemListener changeListener;
        private final ShoppingDeleteListener deleteListener;
        private final ShoppingEditListener editListener;

        ShoppingAdapter(List<ShoppingListItem> list, ShoppingItemListener changeListener,
                        ShoppingDeleteListener deleteListener, ShoppingEditListener editListener) {
            this.list = list;
            this.changeListener = changeListener;
            this.deleteListener = deleteListener;
            this.editListener = editListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ShoppingListItem item = list.get(position);
            holder.tvShoppingItemName.setText(item.getName());
            holder.tvShoppingItemCategoryQty.setText(String.format("%s • %s", item.getCategory(), item.getQuantity()));
            holder.cbShoppingPurchased.setChecked(item.isPurchased());

            holder.cbShoppingPurchased.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setPurchased(isChecked);
                if (changeListener != null) changeListener.onItemChanged(item);
            });

            holder.btnDeleteShoppingItem.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onItemDeleted(item);
            });

            holder.itemView.setOnClickListener(v -> {
                if (editListener != null) editListener.onItemEdited(item);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox cbShoppingPurchased;
            TextView tvShoppingItemName, tvShoppingItemCategoryQty;
            ImageButton btnDeleteShoppingItem;

            ViewHolder(View itemView) {
                super(itemView);
                cbShoppingPurchased = itemView.findViewById(R.id.cbShoppingPurchased);
                tvShoppingItemName = itemView.findViewById(R.id.tvShoppingItemName);
                tvShoppingItemCategoryQty = itemView.findViewById(R.id.tvShoppingItemCategoryQty);
                btnDeleteShoppingItem = itemView.findViewById(R.id.btnDeleteShoppingItem);
            }
        }
    }

    // 4. Planner Adapter
    private interface DayPlanClickListener {
        void onDayPlanClicked(WeeklyMealPlanDay dayPlan);
    }

    private static class PlannerAdapter extends RecyclerView.Adapter<PlannerAdapter.ViewHolder> {
        private final List<WeeklyMealPlanDay> list;
        private final DayPlanClickListener clickListener;

        PlannerAdapter(List<WeeklyMealPlanDay> list, DayPlanClickListener clickListener) { 
            this.list = list; 
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weekly_meal_plan, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WeeklyMealPlanDay item = list.get(position);
            holder.tvPlanDayName.setText(item.getDayName());
            holder.tvPlanDayTotals.setText(String.format(Locale.getDefault(), "%d kcal • %.0fg Protein", item.getTotalCalories(), item.getTotalProtein()));
            holder.tvPlanBreakfast.setText(String.format("🍳 Breakfast: %s (%d kcal)", item.getBreakfastRecipeName(), item.getBreakfastCalories()));
            holder.tvPlanLunch.setText(String.format("🥗 Lunch: %s (%d kcal)", item.getLunchRecipeName(), item.getLunchCalories()));
            holder.tvPlanDinner.setText(String.format("🍲 Dinner: %s (%d kcal)", item.getDinnerRecipeName(), item.getDinnerCalories()));
            holder.tvPlanSnack.setText(String.format("🥤 Snack: %s (%d kcal)", item.getSnackRecipeName(), item.getSnackCalories()));

            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onDayPlanClicked(item);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPlanDayName, tvPlanDayTotals, tvPlanBreakfast, tvPlanLunch, tvPlanDinner, tvPlanSnack;

            ViewHolder(View itemView) {
                super(itemView);
                tvPlanDayName = itemView.findViewById(R.id.tvPlanDayName);
                tvPlanDayTotals = itemView.findViewById(R.id.tvPlanDayTotals);
                tvPlanBreakfast = itemView.findViewById(R.id.tvPlanBreakfast);
                tvPlanLunch = itemView.findViewById(R.id.tvPlanLunch);
                tvPlanDinner = itemView.findViewById(R.id.tvPlanDinner);
                tvPlanSnack = itemView.findViewById(R.id.tvPlanSnack);
            }
        }
    }

    // SQLite Meal Schedule Synchronization Helpers
    private String getDateForDayOfWeek(int dayOfWeek) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_YEAR, dayOfWeek - 1);
        return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }

    private void syncSingleMealToLoggedMeals(WeeklyMealPlanDay dayPlan, String mealType, String recipeName, int calories, String detailsJson) {
        new Thread(() -> {
            try {
                String dateStr = getDateForDayOfWeek(dayPlan.getDayOfWeek());
                
                // Remove existing logged meal of matching category for this date to prevent duplicate slots
                List<LoggedMeal> loggedMealsForDate = dao.getLoggedMealsForDate(dateStr);
                if (loggedMealsForDate != null) {
                    for (LoggedMeal lm : loggedMealsForDate) {
                        boolean match = false;
                        if ("Breakfast".equalsIgnoreCase(mealType) && "Breakfast".equalsIgnoreCase(lm.mealType)) match = true;
                        else if ("Lunch".equalsIgnoreCase(mealType) && "Lunch".equalsIgnoreCase(lm.mealType)) match = true;
                        else if ("Dinner".equalsIgnoreCase(mealType) && "Dinner".equalsIgnoreCase(lm.mealType)) match = true;
                        else if (("Snacks".equalsIgnoreCase(lm.mealType) || "Snack".equalsIgnoreCase(lm.mealType)) && mealType.contains("Snack")) match = true;
                        
                        if (match) {
                            dao.deleteLoggedMeal(lm);
                        }
                    }
                }

                int iconRes = R.drawable.ic_meal_breakfast;
                if ("Lunch".equalsIgnoreCase(mealType)) iconRes = R.drawable.ic_meal_lunch;
                else if ("Dinner".equalsIgnoreCase(mealType)) iconRes = R.drawable.ic_meal_dinner;
                else if (mealType.contains("Snack")) iconRes = R.drawable.ic_meal_snacks;

                LoggedMeal updatedMeal = createLoggedMealFromDetails(
                    "Breakfast".equalsIgnoreCase(mealType) ? "Breakfast" : ("Lunch".equalsIgnoreCase(mealType) ? "Lunch" : ("Dinner".equalsIgnoreCase(mealType) ? "Dinner" : "Snacks")),
                    recipeName,
                    calories,
                    detailsJson,
                    dateStr,
                    System.currentTimeMillis(),
                    "Breakfast".equalsIgnoreCase(mealType) ? "07:00 AM" : ("Lunch".equalsIgnoreCase(mealType) ? "01:00 PM" : ("Dinner".equalsIgnoreCase(mealType) ? "07:00 PM" : "04:00 PM")),
                    iconRes
                );
                dao.insertLoggedMeal(updatedMeal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populateLoggedMealsFromWeeklyPlan(List<WeeklyMealPlanDay> mealPlan) {
        if (mealPlan == null || mealPlan.isEmpty()) return;
        
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        long baseTimestamp = System.currentTimeMillis();
        
        for (int i = 0; i < 7; i++) {
            String dateStr = sdf.format(cal.getTime());
            
            // Clear existing logged meals for this date so we do not have duplicates
            dao.deleteLoggedMealsForDate(dateStr);
            
            WeeklyMealPlanDay dayPlan = null;
            for (WeeklyMealPlanDay d : mealPlan) {
                if (d.getDayOfWeek() == (i + 1)) {
                    dayPlan = d;
                    break;
                }
            }
            if (dayPlan == null && i < mealPlan.size()) {
                dayPlan = mealPlan.get(i);
            }
            
            if (dayPlan != null) {
                // Insert Breakfast
                LoggedMeal breakfast = createLoggedMealFromDetails(
                    "Breakfast",
                    dayPlan.getBreakfastRecipeName(),
                    dayPlan.getBreakfastCalories(),
                    dayPlan.getBreakfastDetailsJson(),
                    dateStr,
                    baseTimestamp + (i * 86400000L) + 3600000L * 7, // 7:00 AM
                    "07:00 AM",
                    R.drawable.ic_meal_breakfast
                );
                dao.insertLoggedMeal(breakfast);
                
                // Insert Lunch
                LoggedMeal lunch = createLoggedMealFromDetails(
                    "Lunch",
                    dayPlan.getLunchRecipeName(),
                    dayPlan.getLunchCalories(),
                    dayPlan.getLunchDetailsJson(),
                    dateStr,
                    baseTimestamp + (i * 86400000L) + 3600000L * 13, // 1:00 PM
                    "01:00 PM",
                    R.drawable.ic_meal_lunch
                );
                dao.insertLoggedMeal(lunch);
                
                // Insert Dinner
                LoggedMeal dinner = createLoggedMealFromDetails(
                    "Dinner",
                    dayPlan.getDinnerRecipeName(),
                    dayPlan.getDinnerCalories(),
                    dayPlan.getDinnerDetailsJson(),
                    dateStr,
                    baseTimestamp + (i * 86400000L) + 3600000L * 19, // 7:00 PM
                    "07:00 PM",
                    R.drawable.ic_meal_dinner
                );
                dao.insertLoggedMeal(dinner);
                
                // Insert Snack
                String snackName = dayPlan.getSnackRecipeName();
                int snackCal = dayPlan.getSnackCalories();
                String snackJson = dayPlan.getMorningSnackDetailsJson();
                if (snackJson == null || snackJson.equals("{}") || snackJson.trim().isEmpty()) {
                    snackJson = dayPlan.getEveningSnackDetailsJson();
                }
                
                LoggedMeal snack = createLoggedMealFromDetails(
                    "Snacks",
                    snackName != null ? snackName : "Healthy Snack",
                    snackCal > 0 ? snackCal : 150,
                    snackJson,
                    dateStr,
                    baseTimestamp + (i * 86400000L) + 3600000L * 16, // 4:00 PM
                    "04:00 PM",
                    R.drawable.ic_meal_snacks
                );
                dao.insertLoggedMeal(snack);
            }
            
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private LoggedMeal createLoggedMealFromDetails(String mealType, String fallbackName, int fallbackCalories, String detailsJson, String date, long timestamp, String mealTime, int iconRes) {
        String name = fallbackName;
        int calories = fallbackCalories;
        int protein = 25;
        int carbs = 35;
        int fat = 10;
        int fiber = 2;
        int sugar = 5;
        String servingSize = "1 serving";
        String ingredients = "Ingredients list";
        String steps = "Cooking steps";
        
        try {
            if (detailsJson != null && !detailsJson.trim().isEmpty() && !detailsJson.equals("{}")) {
                JSONObject obj = new JSONObject(detailsJson);
                name = obj.optString("recipe", name);
                if (name.isEmpty() || name.equals("null")) name = obj.optString("name", fallbackName);
                calories = obj.optInt("calories", calories);
                protein = (int) Math.round(obj.optDouble("protein", protein));
                carbs = (int) Math.round(obj.optDouble("carbs", carbs));
                fat = (int) Math.round(obj.optDouble("fat", fat));
                fiber = (int) Math.round(obj.optDouble("fiber", fiber));
                sugar = (int) Math.round(obj.optDouble("sugar", sugar));
                servingSize = obj.optString("servingSize", servingSize);
                
                JSONArray ingArr = obj.optJSONArray("ingredients");
                if (ingArr != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < ingArr.length(); i++) {
                        sb.append("• ").append(ingArr.getString(i)).append("\n");
                    }
                    ingredients = sb.toString().trim();
                } else {
                    ingredients = obj.optString("ingredients", ingredients);
                }
                
                JSONArray stepsArr = obj.optJSONArray("steps");
                if (stepsArr != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < stepsArr.length(); i++) {
                        sb.append(i + 1).append(". ").append(stepsArr.getString(i)).append("\n");
                    }
                    steps = sb.toString().trim();
                } else {
                    steps = obj.optString("steps", steps);
                }
            } else {
                protein = (int) Math.round(calories * 0.25 / 4.0);
                carbs = (int) Math.round(calories * 0.50 / 4.0);
                fat = (int) Math.round(calories * 0.25 / 9.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            protein = (int) Math.round(calories * 0.25 / 4.0);
            carbs = (int) Math.round(calories * 0.50 / 4.0);
            fat = (int) Math.round(calories * 0.25 / 9.0);
        }
        
        return new LoggedMeal(
            mealType,
            name,
            calories,
            protein,
            carbs,
            fat,
            date,
            timestamp,
            mealTime,
            "AI generated from scanned groceries context",
            true, // isChecked = true so it shows up in daily progress totals
            iconRes,
            fiber,
            sugar,
            servingSize,
            ingredients,
            steps
        );
    }
}
