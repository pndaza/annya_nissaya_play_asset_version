package mm.pndaza.annyanissaya.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import mm.pndaza.annyanissaya.R;
import mm.pndaza.annyanissaya.adapter.PaliBookAdapter;
import mm.pndaza.annyanissaya.database.DBOpenHelper;
import mm.pndaza.annyanissaya.model.Category;
import mm.pndaza.annyanissaya.model.PaliBook;
import mm.pndaza.annyanissaya.utils.MDetect;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MDetect.init(this);
        String title = getResources().getString(R.string.app_name_mm);
        setTitle(MDetect.getDeviceEncodedText(title));

        ArrayList<Object> paliBookList = new ArrayList<>();
        ArrayList<Category> categories = DBOpenHelper.getInstance(this).getCategories();
        for (Category category : categories) {
            paliBookList.add(category);
            paliBookList.addAll(DBOpenHelper.getInstance(this).getBooks(category.getId()));
        }
        PaliBookAdapter adapter = new PaliBookAdapter(this, paliBookList);
        final ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if(item instanceof PaliBook){
                    Intent intent = new Intent(getApplicationContext(), PageSelectActivity.class);
                    intent.putExtra("pali_book", (PaliBook) item);
                    MainActivity.this.startActivity(intent);
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            ShowAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void ShowAboutDialog() {

        new AlertDialog.Builder(this)
                .setTitle(MDetect.getDeviceEncodedText("သိမှတ်ဖွယ်"))
                .setMessage(MDetect.getDeviceEncodedText(getString(R.string.info)))
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("OK", null)
                .show();

    }
}
