package csv.a2b;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import java.util.List;

/**
 * Created by der_geiler on 25-05-2015.
 */
public class Activity_tripGroups extends Activity
{
    private FileHandler fileHandler = null;
    private TableLayout tripGroupsTableLayout = null;
    private Context context;
    final CharSequence[] groupProps = {"Edit Start/End point", "Rename", "Delete"};
    uiAction lastUiAction = null;
    Rect touchRect = null;
    static final int INDEX_START_END_POINT = 0;
    static final int INDEX_RENAME = 1;
    static final int INDEX_DELETE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_groups);
        context = getApplicationContext();
        tripGroupsTableLayout = (TableLayout) findViewById(R.id.tripGroupsTableLayout);
        fileHandler = FileHandler.GetInstance();
        fileHandler.Init(context);
        ShowTripGroups();

        if(BuildConfig.FLAVOR.equals("free") && (tripGroupsTableLayout.getChildCount() <5))
        {
            AdView mAdView = (AdView) findViewById(R.id.adViewTripGroups);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mAdView.bringToFront();
        }

        lastUiAction = new uiAction();
        touchRect = new Rect();
    }

    private int Dp2Px(float dp)
    {
        return (int)(context.getResources().getDisplayMetrics().density * dp);
    }

    private void SetRenameAlert(final String oldGroupname)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Rename " + oldGroupname + ":");
        final EditText input = new EditText(this);
        input.setText(oldGroupname);
        alert.setView(input);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override public void onClick(DialogInterface dialog, int which)
            {
                String groupName = input.getText().toString();
                if (groupName.equals("") == false)
                {
                    if (fileHandler.RenameTripGroup(oldGroupname, groupName) == false)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_tripGroups.this);
                        builder.setMessage("Group exists. Please choose another name.");
                        builder.setCancelable(true);
                        AlertDialog ad = builder.create();
                        ad.show();
                    }
                    else
                    {
                        Globals.GetInstance(null).renameDir(new A2BdirInfo(groupName));
                        new SQLiteHelperThread().execute(SQLiteHelperThread.ACTION_UPDATE, oldGroupname, groupName);
                    }
                }
                ShowTripGroups();
            }
        });
        alert.show();
    }

    private void SetDeleteAlert(final String group)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete: " + group + "? (All trips will be deleted!)");
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override public void onClick(DialogInterface dialog, int which){}
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                fileHandler.DeleteGroup(group);
                Globals.GetInstance(null).removeDir(group);
                new SQLiteHelperThread().execute(SQLiteHelperThread.ACTION_DELETE_DIR, group);
                ShowTripGroups();
            }
        });
        alert.show();
    }

    private void ShowTripGroups()
    {
        tripGroupsTableLayout.removeAllViewsInLayout();
        List<String> dirs = fileHandler.GetDirectories();
        if (dirs.size() != 0)
        {
            tripGroupsTableLayout.setVisibility(View.VISIBLE);
            for (String dir : dirs)
            {
                Context context = getApplicationContext();
                TableRow tr = new TableRow(context);
                tr.setGravity(Gravity.CENTER_HORIZONTAL);
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                tr.setLayoutParams(params);

                TextView tv = new TextView(context);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                int px = Dp2Px(18);
                tv.setPadding(px, px, px, px);
                tv.setText(dir);
                tv.setTextColor(getResources().getColor(android.R.color.black));
                tv.setBackgroundColor(Color.parseColor("#b0b0b0"));
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                px = Dp2Px(1);
                params.setMargins(px, px, px, px);
                tv.setLayoutParams(params);
                tv.setOnTouchListener(new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        final String pressedGroup = ((TextView) v).getText().toString();
                        int action = event.getAction();
                        switch (action)
                        {
                            case MotionEvent.ACTION_DOWN:
                            {
                                lastUiAction.lastAction = action;
                                lastUiAction.view = v;
                                v.setBackgroundColor(Color.parseColor("#000000"));
                                break;
                            }
                            case MotionEvent.ACTION_CANCEL:
                            {
                                lastUiAction.lastAction = action;
                                lastUiAction.view.setBackgroundColor(Color.parseColor("#b0b0b0"));
                                break;
                            }
                            case MotionEvent.ACTION_UP:
                            {
                                if (lastUiAction.lastAction == MotionEvent.ACTION_DOWN)
                                {
                                    lastUiAction.view.setBackgroundColor(Color.parseColor("#b0b0b0"));
                                    Intent i = new Intent(getApplicationContext(), Activity_trips.class);
                                    i.putExtra("group", pressedGroup);
                                    startActivity(i);
                                }
                                break;
                            }
                            case MotionEvent.ACTION_MOVE:
                            {
                                v.getHitRect(touchRect);
                                if (!touchRect.contains((int) event.getX(), (int) event.getY()))
                                {
                                    lastUiAction.view.setBackgroundColor(Color.parseColor("#b0b0b0"));
                                    v.cancelLongPress();
                                    lastUiAction.lastAction = MotionEvent.ACTION_CANCEL;
                                }
                            }
                        }
                        return false;
                    }
                });
                tv.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View v)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_tripGroups.this);
                        final String group = ((TextView) v).getText().toString();
                        builder.setTitle(group);
                        lastUiAction.view.setBackgroundColor(Color.parseColor("#b0b0b0"));
                        lastUiAction.lastAction = uiAction.ACTION_LONG_CSV;
                        CharSequence props[] = {"Rename","Delete"};
                        if(DelegGeofence.getInstance().getGeoFencesPersist().size() > 1)
                            props = groupProps;
                        builder.setItems(props , new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        // need to correct index for dynamic list geoFencing
                                        if(DelegGeofence.getInstance().getGeoFencesPersist().size() < 2)
                                            which++;
                                        switch(which)
                                        {
                                            case INDEX_RENAME: SetRenameAlert(group); break;
                                            case INDEX_DELETE: SetDeleteAlert(group); break;
                                            case INDEX_START_END_POINT:
                                            {
                                                Intent i = new Intent(getApplicationContext(), Activity_directoryGeo.class);
                                                i.putExtra("dir", group);
                                                startActivity(i);
                                                break;
                                            }
                                            default: break;
                                        }
                                    }
                                }
                        );
                        AlertDialog groupPropDialog = builder.create();
                        groupPropDialog.show();
                        return true;
                    }
                });
                tr.addView(tv);
                tripGroupsTableLayout.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            }
        }
        else
            tripGroupsTableLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trips, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_new_trip_group)
        {
            PromtUserNewGroup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void PromtUserNewGroup()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("New trip group:");
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String groupName = input.getText().toString();
                if (groupName.equals("") == false)
                {
                    if (fileHandler.CreateTripGroup(groupName) == false)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_tripGroups.this);
                        builder.setMessage("Group exists. Please choose another name.");
                        builder.setCancelable(true);
                        AlertDialog ad = builder.create();
                        ad.show();
                    } else
                        Globals.GetInstance(null).setDir(new A2BdirInfo(groupName));
                }
                ShowTripGroups();
            }
        });
        alert.show();
    }
}
