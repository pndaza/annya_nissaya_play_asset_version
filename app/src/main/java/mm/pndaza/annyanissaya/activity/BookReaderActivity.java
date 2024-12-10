package mm.pndaza.annyanissaya.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import mm.pndaza.annyanissaya.R;
import mm.pndaza.annyanissaya.utils.MDetect;
import mm.pndaza.annyanissaya.utils.ScrollMode;
import mm.pndaza.annyanissaya.utils.SharePref;

public class BookReaderActivity extends AppCompatActivity
        implements OnPageChangeListener, OnPageScrollListener {

    private static final String CURRENT_PAGE = "current_page";
    private boolean actionVisibleState = true;
    private ScrollMode scrollMode;
    private boolean nightMode;
    private SharePref sharePref;
    private String pdfFileName;
    private PDFView pdfView;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reader);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        String nsyId = intent.getStringExtra("nsyid");
        int nsyPage = intent.getIntExtra("nsypage", 1);
        String nsyName = intent.getStringExtra("nsyname");

        pdfFileName = nsyId + ".pdf";
        Log.d("filename", pdfFileName);

        MDetect.init(this);
        setTitle(MDetect.getDeviceEncodedText(nsyName));

        pdfView = findViewById(R.id.pdfView);

        sharePref = SharePref.getInstance(this);
        scrollMode = sharePref.getScrollMode();
        nightMode = sharePref.getNightMode();

        currentPage = nsyPage - 1;
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt(CURRENT_PAGE);
        }

        loadPdf();

        // show hide action bar
        pdfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionVisibleState = !actionVisibleState;
                if (getSupportActionBar() != null) {
                    return;
                }
                if (actionVisibleState) {
                    getSupportActionBar().show();
                } else {
                    getSupportActionBar().hide();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // save current page
        outState.putInt(CURRENT_PAGE, currentPage);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reader, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_scroll_mode:
                onScrollModeChanged(item);
                return true;
            case R.id.menu_night_mode:
                changeNightMode(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void swapIcon(MenuItem item) {
        if (scrollMode == ScrollMode.vertical) {
            item.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_swap_horiz_24));
        } else {
            item.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_swap_vert_24));
        }
    }

    private void changeNightMode(MenuItem item) {

        nightMode = !nightMode;
        sharePref.setNightMode(nightMode);

        currentPage = pdfView.getCurrentPage();

        // change theme
        AppCompatDelegate.setDefaultNightMode(nightMode ?
                AppCompatDelegate.MODE_NIGHT_YES :
                AppCompatDelegate.MODE_NIGHT_NO);

    }

    private void onScrollModeChanged(MenuItem menuItem) {
        if (scrollMode == ScrollMode.vertical) {
            scrollMode = ScrollMode.horizontal;
        } else {
            scrollMode = ScrollMode.vertical;
        }

        // change appbar icon
        swapIcon(menuItem);

        // persist to shared preference
        sharePref.setScrollMode(scrollMode);

        // change icon
        swapIcon(menuItem);

        // change pdf scroll mode
        currentPage = pdfView.getCurrentPage();
        loadPdf();

    }

    private void loadPdf() {

        try {
            Context context = createPackageContext("mm.pndaza.annyanissaya", 0);
            AssetManager assetManager = context.getAssets();

            InputStream inputStream = assetManager.open("books" + File.separator
                    + pdfFileName);

            pdfView.fromStream(inputStream)
                    .defaultPage(currentPage)
                    .enableSwipe(true)
                    .pageFitPolicy(scrollMode == ScrollMode.horizontal
                            ? FitPolicy.BOTH : FitPolicy.WIDTH)
                    .swipeHorizontal(scrollMode == ScrollMode.horizontal)
                    .pageSnap(scrollMode == ScrollMode.horizontal)
                    .autoSpacing(scrollMode == ScrollMode.horizontal)
                    .pageFling(scrollMode == ScrollMode.horizontal)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .nightMode(nightMode)
                    .load();


        } catch (PackageManager.NameNotFoundException | IOException e) {
            Log.d("loadPdf failed ", e.toString());
        }


    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        currentPage = page;
    }

    @Override
    public void onPageScrolled(int page, float positionOffset) {
        currentPage = page;

    }
}