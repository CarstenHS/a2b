package csv.a2b;

/**
 * Created by der_geiler on 25-11-2015.
 */
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;

    //constructor
    public DefaultExceptionHandler(Thread.UncaughtExceptionHandler pDefaultExceptionHandler)
    {
        mDefaultExceptionHandler= pDefaultExceptionHandler;
    }
    public void uncaughtException(Thread t, Throwable e)
    {
        /*
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        String errorDetail = writer.toString();

        FileHandler.GetInstance().saveStackTrace(errorDetail);

        // cleanup, don't know if really required
        t.getThreadGroup().destroy();
        */
    }
}