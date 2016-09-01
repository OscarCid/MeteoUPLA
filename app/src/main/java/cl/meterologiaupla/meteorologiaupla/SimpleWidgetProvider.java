package cl.meterologiaupla.meteorologiaupla;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Orsen on 10/05/2016.
 */
public class SimpleWidgetProvider extends AppWidgetProvider {
    private String json=null;
    public static String WIDGET_BUTTON = "cl.meterologiaupla.meteorologiaupla.WIDGET_BUTTON";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.simple_widget);

            BackgroundTask task = new BackgroundTask(context,getDefaults("estacion", context));
            task.execute();

            remoteViews.setOnClickPendingIntent(R.id.ver_mas, ver_mas(context));

            Intent intent = new Intent(context, SimpleWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);

            Intent intent0 = new Intent(WIDGET_BUTTON);
            PendingIntent pendingIntent0 = PendingIntent.getBroadcast(context, 0, intent0, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.cambio1, pendingIntent0 );
            remoteViews.setOnClickPendingIntent(R.id.cambio2, pendingIntent0 );


            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (WIDGET_BUTTON.equals(intent.getAction())) {
            // First handle your special intent action
            cambio_estacion(context);
            BackgroundTask task = new BackgroundTask(context,getDefaults("estacion", context));
            task.execute();
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        setDefaults("estacion", "yali", context);
        super.onEnabled(context);
    }

    public void cambio_estacion(Context context)
    {
        if (getDefaults("estacion", context).equals("yali"))
        {
            setDefaults("estacion", "campana", context);
        }
        else
        {
            if (getDefaults("estacion", context).equals("campana"))
            {
                setDefaults("estacion", "mantagua", context);
            }
            else
            {
                if (getDefaults("estacion", context).equals("mantagua"))
                {
                    setDefaults("estacion", "yali", context);
                }
            }
        }
    }

    private class BackgroundTask extends AsyncTask<String,String,String> {
        private Context mContext;
        private String estacion;

        public BackgroundTask(Context context, String string) {
            mContext = context;
            estacion = string;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            if (isOnline(mContext)) {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("http://sistema.meteorologiaupla.cl/Clima/scr/php/widget/appEstacion.php?estacion="+estacion)
                        .build();

                Response response = null;
                try {
                    response = client.newCall(request).execute();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {

            }
            return  null;
        }


        @Override
        protected void onPostExecute(String data) {
            if(data != null)
            {
                json = data;
                // Se crea el remote view del layout
                RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.simple_widget);
                // aqui van todas las mierdas para actualizar la info del widget
                remoteViews.setTextViewText(R.id.textView, datoSimple("temp")+"°C");
                remoteViews.setTextViewText(R.id.ultimo_registro, datoSimple("fecha")+" a las "+ datoSimple("hora"));
                remoteViews.setTextViewText(R.id.estacion, "Estación "+estacion);
                remoteViews.setTextViewText(R.id.humedad, datoSimple("humedad")+"%");
                remoteViews.setTextViewText(R.id.prec_Hoy, datoSimple("precHoy")+" mm");
                remoteViews.setTextViewText(R.id.r_solar, datoSimple("rSolar")+" W/m²");

                //las siguientes 3 mierdas actualizan el widget, sin esto no se actualiza la caga
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                ComponentName thisWidget = new ComponentName(mContext, SimpleWidgetProvider.class);
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);

                // lineas para colocar la info culia de que se actualizo :D
                // Toast toast1 = Toast.makeText(mContext, "Datos actualizados correctamente", Toast.LENGTH_SHORT);
                // toast1.show();
            }
        }
    }

    public String datoSimple(final String dato)
    {
        String datoRescatado = "";
        try {
            JSONObject json= (JSONObject) new JSONTokener(this.json).nextValue();
            JSONObject json2 = json.getJSONObject("data");
            datoRescatado = (String) json2.get(dato);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return datoRescatado;
    }


    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        }
        return false;
    }

    public static PendingIntent ver_mas(Context context) {

        // initiate widget update request
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return pendingIntent;
    }

    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
}