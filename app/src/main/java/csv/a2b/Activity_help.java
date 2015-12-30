package csv.a2b;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class Activity_help extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_help);
        TextView tv = (TextView) findViewById(R.id.linkHelp);
        tv.setText(Html.fromHtml("<a href=\"http://play.google.com/store/apps/details?id=csv.a2b\">A2B at google play</a>"));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setLinksClickable(true);
    }

}
