package tfg.taxicentral;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MenuActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_menu);

        String[] values = new String[] { "Go to", "Actual state", "Plan future travel",
                "Planned travels", "Cancel current travel", "History", "View taxi stands" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
        Intent intent = null;
        switch(item) {
            case "Go to":
                intent = new Intent(getApplicationContext(), GoToActivity.class);
                break;
            case "Actual state":
                intent = new Intent(getApplicationContext(), ActualStateActivity.class);
                break;
            case "Plan future travel":

                break;
            case "Planned travels":

                break;
            case "Cancel current travel":

                break;
            case "History":

                break;
            case "View taxi stands":

                break;
        }
        startActivity(intent);
    }
}
