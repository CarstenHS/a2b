package com.example.der_geiler.checkmytrip;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import 

/**
 * Created by der_geiler on 18-05-2015.
 */
public class FileHandler extends Activity
{
    private static FileHandler instance;

    final String filePrefix = "a2b_";
    final String fileExtension = ".a2b";
    private String dataDir = null;
    private Context context = null;
    private final String unCategorized = "Uncategorized";
    private String unCategorizedPath = null;

    public static FileHandler GetInstance()
    {
        instance = (instance == null) ? new FileHandler() : instance;
        return instance;
    }

    private FileHandler(){}

    public void Init(Context context)
    {
        dataDir = context.getFilesDir().toString() + "/";// file.getPath();
        unCategorizedPath = dataDir + unCategorized + "/";
        this.context = context;
    }

    /*
    public ArrayList<String> LoadTripObjects(String group)
    {
        try
        {
            FileInputStream fis = openFileInput(dataDir + group);
            ObjectInputStream is = new ObjectInputStream(fis);
            Trip trip = (Trip) is.readObject();
            is.close();
            fis.close();
        }catch (IOException | ClassNotFoundException e)
        {
        }
    }
*/
    public List<String> LoadTrips(String group)
    {
        File file = new File(dataDir + group + "//");
        File[] files = file.listFiles();

        List<String> fileNames = new ArrayList<>();

        for (File f : files)
        {
            fileNames.add(f.getName());
        }
        return fileNames;
    }

    private void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
            {
                child.delete();
                DeleteRecursive(child);
            }

        fileOrDirectory.delete();
    }

    public void DeleteGroup(String group)
    {
        File dir = new File(dataDir + group);
        DeleteRecursive(dir);
    }

    public List<String> GetDirectories()
    {
        File myDirectory = new File(dataDir);
        File[] directories = myDirectory.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.isDirectory();
            }
        });

        List<String> dirs = new ArrayList<>();

        for (File file : directories)
        {
            dirs.add(file.getName());
        }

        return dirs;
    }

    private boolean DirectoryExist(String dir)
    {
        List<String> dirs = GetDirectories();
        for(String name : dirs)
        {
            if(name.equals(dir))
                return true;
        }
        return false;
    }

    public boolean CreateTripGroup(String name)
    {
        boolean success = false;
        if(DirectoryExist(name) == false)
        {
            File folder = new File(dataDir + name);
            folder.mkdir();
            success = true;
        }
        return success;
    }

    public void SaveTrip(Trip trip)
    {
        if(DirectoryExist(unCategorized) == false)
        {
            File folder = new File(unCategorizedPath);
            folder.mkdir();
        }
        String fileName = filePrefix + String.valueOf(trip.GetTimeStart().getTime()) + fileExtension;

        String fullPath = "unCategorized/" + fileName;
        //File file = new File(unCategorizedPath, fileName);

        Gson gson = new Gson();

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(fullPath, Context.MODE_PRIVATE);
            outputStream.write(s.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        try
        {
            //File file = new File();
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(trip);
            os.close();
            fos.close();
        } catch (IOException e) {
            e = e;
        }
*/
       LoadTrips(unCategorized);
        /*
        if(file.exists())
        {
            try
            {
                //File file = new File();
                FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(trip);
                os.close();
                fos.close();
            } catch (IOException e) {
            }
        }
        else
        {

        }
        */
    }
}
