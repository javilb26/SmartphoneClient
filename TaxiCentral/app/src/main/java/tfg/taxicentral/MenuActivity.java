package tfg.taxicentral;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;

public class MenuActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new String[]{"Navigation", "Go to", "Actual state", "Plan future travel",
                "Planned travels", "Current travel info", "Cancel current travel", "History", "View taxi stands", "Locate", "Change password"});
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        Intent intent = null;
        switch (item) {
            case "Navigation":
                intent = new Intent(getApplicationContext(), NavigationActivity.class);
                break;
            case "Go to":
                intent = new Intent(getApplicationContext(), GoToActivity.class);
                break;
            case "Actual state":
                intent = new Intent(getApplicationContext(), ActualStateActivity.class);
                break;
            case "Plan future travel":
                intent = new Intent(getApplicationContext(), PlanFutureTravelOriginActivity.class);
                break;
            case "Planned travels":
                intent = new Intent(getApplicationContext(), FutureTravelsActivity.class);
                break;
            case "Current travel info":
                intent = new Intent(getApplicationContext(), CurrentTravelActivity.class);
                break;
            case "Cancel current travel":
                intent = new Intent(getApplicationContext(), CancelTravelActivity.class);
                break;
            case "History":
                intent = new Intent(getApplicationContext(), HistoryActivity.class);
                break;
            case "View taxi stands":
                intent = new Intent(getApplicationContext(), NearestStandsActivity.class);
                break;
            case "Locate":
                intent = new Intent(getApplicationContext(), LocateActivity.class);
                break;
            case "Change password":
                intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                break;
        }
        startActivity(intent);
    }
}
