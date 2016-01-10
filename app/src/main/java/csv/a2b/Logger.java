package csv.a2b;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by der_geiler on 10-01-2016.
 */
public class Logger
{
    private static final String filename = "log.txt";
    private static File dir;
    private static Logger ourInstance = new Logger();

    public static Logger getInstance()
    {
        return ourInstance;
    }
    private Logger(){}

    public void init()
    {
        dir = Environment.getExternalStorageDirectory();
    }

    public void log(String text)
    {
        try
        {
            File f = new File(dir, filename);
            if(f.exists() == false)
                f.createNewFile();
            /*
            FileOutputStream fOut = new FileOutputStream(f);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(text);
            myOutWriter.flush();
            myOutWriter.close();
            fOut.flush();
            fOut.close();
            */
            String time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
            FileWriter fileWritter = new FileWriter(f,true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(time + "  " + text + "\r\n");
            bufferWritter.flush();
            bufferWritter.close();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
