package mm.pndaza.annyanissaya.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import mm.pndaza.annyanissaya.R;
import mm.pndaza.annyanissaya.adapter.NsyAdapter;
import mm.pndaza.annyanissaya.database.DBOpenHelper;
import mm.pndaza.annyanissaya.model.Nsy;
import mm.pndaza.annyanissaya.utils.MDetect;
import mm.pndaza.annyanissaya.utils.SharePref;

public class NsySelectActivity extends AppCompatActivity implements NsyAdapter.OnItemClickListener {
    private String bookId;
    private int pageNumber;
    private boolean isOpenedByDeepLink = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharePref sharePref = SharePref.getInstance(this);
        if (sharePref.getNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nsy_select);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isOpenedByDeepLink) {
                    finish();
                    // open main activity
                    Intent intent = new Intent(NsySelectActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {

                    finish();
                }
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MDetect.init(this);
        String title = getString(R.string.choice_nsy);
        setTitle(MDetect.getDeviceEncodedText(title));

        Intent intent = getIntent();
        if (intent.getData() != null) {
            handleDeepLink(intent);
        } else if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            bookId = bundle.getString("book_id");
            pageNumber = bundle.getInt("page_number");
            Log.d("onCreate: from intent", "bookId: " + bookId + " pageNumber: " + pageNumber);

        }

        ArrayList<Nsy> nsyArrayList = DBOpenHelper.getInstance(this).getNsyBooks(bookId, pageNumber);
        NsyAdapter adapter = new NsyAdapter(nsyArrayList, this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        TextView textView = findViewById(R.id.tv_empty);
        if (nsyArrayList.isEmpty()) {
            textView.setText(MDetect.getDeviceEncodedText(getString(R.string.empty_nsy)));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onItemClick(Nsy nsy) {

        Intent intent = new Intent(this, BookReaderActivity.class);
        intent.putExtra("nsyid", nsy.getId());
        intent.putExtra("nsypage", nsy.getPdfPageNumber());
        intent.putExtra("nsyname", nsy.getName());
        startActivity(intent);
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();

        if (data != null) {
//            String scheme = data.getScheme();
//            String host = data.getHost();
//            String path = data.getPath();

            // Parse the query parameters
            bookId = data.getQueryParameter("id");
            String pageStr = data.getQueryParameter("page");
            if (bookId != null && pageStr != null) {
                try {

                    pageNumber = Integer.parseInt(pageStr);
                    Log.d("onCreate:", "from deeplink- " + "bookId: " + bookId + " pageNumber: " + pageNumber);
                } catch (NumberFormatException e) {
                    Log.e("DeepLink", "Invalid page number: " + pageStr, e);
                    // Handle the error (e.g., show an error message to the user)
                }
            } else {
                Log.e("DeepLink", "Invalid deep link parameters");
                // Handle the error (e.g., show an error message to the user)
            }
            isOpenedByDeepLink = true;

        }
    }
}
