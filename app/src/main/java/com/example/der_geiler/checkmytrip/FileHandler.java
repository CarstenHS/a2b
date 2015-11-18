package com.example.der_geiler.checkmytrip;

import android.app.Activity;
import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

/**
 * Created by der_geiler on 18-05-2015.
 */
public class FileHandler extends Activity
{
    private static FileHandler instance;

    final String filePrefix = "";
    final String fileExtension = ".a2b";
    final String strFolderPrefix = "app_";
    final int folderPrefixSize = strFolderPrefix.length();
    private String dataDir = null;
    private Context context = null;
    private final String strDirUnCategorized = "Uncategorized";
    private final String strGeofences = "Geofences";
    private final String strDirInfos = "dirInfos";

    public static FileHandler GetInstance()
    {
        instance = (instance == null) ? new FileHandler() : instance;
        return instance;
    }

    private FileHandler(){}

    public void Init(Context context)
    {
        dataDir = context.getFilesDir().getParent();
        this.context = context;
    }

    public String getUncategorizedString(){return strDirUnCategorized;}

    public Trip LoadTrip(String strGroup, String strTrip)
    {
        File folder = context.getDir(strGroup, Context.MODE_PRIVATE);
        File file = new File(folder, strTrip);
        Trip trip = null;
        if(file.exists() == true)
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(file);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            try
            {
                while ((line = bufferedReader.readLine()) != null)
                {
                    sb.append(line);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            String json = sb.toString();
            Gson gson = new Gson();
            trip = gson.fromJson(json, Trip.class);
        }
        return trip;
    }

    public List<String> LoadTrips(String groupPath)
    {
        //File file = new File(groupPath);
        File folder = context.getDir(groupPath, Context.MODE_PRIVATE);
        File[] files = folder.listFiles();

        List<String> fileNames = new ArrayList<>();

        for (File f : files)
        {
            fileNames.add(f.getName());
        }
        return fileNames;
    }

    private void DeleteRecursive(File fileOrDirectory)
    {
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
        File dir = context.getDir(group, Context.MODE_PRIVATE);
        DeleteRecursive(dir);
    }

    public void deleteTrip(String group, String trip)
    {
        File folder = context.getDir(group, Context.MODE_PRIVATE);
        File file = new File(folder, trip);
        if(file.exists() == true)
        {
            file.delete();
        }
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

        String folderName;
        for (File file : directories)
        {
            folderName = file.getName();
            if(folderName.length() >= folderPrefixSize)
            {
                if (folderName.substring(0, folderPrefixSize).equals(strFolderPrefix))
                    dirs.add(file.getName().substring(folderPrefixSize));
            }
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
            File folder = new File(dataDir + "/" + strFolderPrefix + name);
            boolean created = folder.mkdir();
            success = true;
        }
        return success;
    }

    public void SaveTrip(List dirs, Trip trip) throws IOException
    {
        if(dirs == null)    // for testing the save from options
        {
            dirs = new ArrayList<>();
            dirs.add(strDirUnCategorized);
        }

        for (String dir : (List<String>)dirs)
        {
            File folder = context.getDir(strDirUnCategorized, Context.MODE_PRIVATE);
            String fileName = filePrefix + trip.SetTimeEnd();

            File file = new File(folder, fileName);
            if (file.exists() == false)
            {
                try
                {
                    file.createNewFile();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            Gson gson = new Gson();
            String s = gson.toJson(trip);
            file.setWritable(true);

            FileOutputStream outputStream = new FileOutputStream(file);
            try
            {
                outputStream.write(s.getBytes());
                outputStream.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        LoadTrips(strDirUnCategorized);
    }

    public void SaveGeofences(List<A2BGeofence> gfsPersist)
    {
        String filename = strGeofences + fileExtension;
        File file = new File(context.getFilesDir(), filename);

        if(file.exists() == false)
        {
            try{ file.createNewFile();}
            catch (Exception e) {e.printStackTrace();}
        }

        Gson gson = new Gson();
        String s = gson.toJson(gfsPersist);
        file.setWritable(true);
        try
        {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(s.getBytes());
            outputStream.close();
        }
        catch (Exception e){e.printStackTrace();}
    }

    public List<A2BGeofence> LoadGeofences()
    {
        String filename = strGeofences + fileExtension;
        File file = new File(context.getFilesDir(), filename);
        List<A2BGeofence> gfPersist = null;
        if(file.exists() == true)
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(file);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            try
            {
                while ((line = bufferedReader.readLine()) != null)
                    sb.append(line);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            String json = sb.toString();
            Type listType = new TypeToken<ArrayList<A2BGeofence>>() {}.getType();
            Gson gson = new Gson();
            gfPersist = gson.fromJson(json, listType);
        }
        return gfPersist;
    }

    public List<A2BdirInfo> LoadDirInfos()
    {
        String filename = strDirInfos + fileExtension;
        File file = new File(context.getFilesDir(), filename);
        List<A2BdirInfo> dirInfos = null;
        if(file.exists() == true)
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(file);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            try
            {
                while ((line = bufferedReader.readLine()) != null)
                    sb.append(line);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            String json = sb.toString();
            Type listType = new TypeToken<ArrayList<A2BdirInfo>>() {}.getType();
            Gson gson = new Gson();
            dirInfos = gson.fromJson(json, listType);
        }
        return dirInfos;
    }

    public void SaveDirInfos(List<A2BdirInfo> dirInfos)
    {
        String filename = strDirInfos + fileExtension;
        File file = new File(context.getFilesDir(), filename);

        if(file.exists() == false)
        {
            try{ file.createNewFile();}
            catch (Exception e) {e.printStackTrace();}
        }

        Gson gson = new Gson();
        String s = gson.toJson(dirInfos);
        file.setWritable(true);
        try
        {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(s.getBytes());
            outputStream.close();
        }
        catch (Exception e){e.printStackTrace();}
    }
}
