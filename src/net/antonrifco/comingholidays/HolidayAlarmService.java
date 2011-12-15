package net.antonrifco.comingholidays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.antonrifco.comingholidays.util.Holiday;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.IBinder;

public class HolidayAlarmService extends Service {

	private SharedPreferences settings;
    private List<Holiday> holidays;
	
    @Override
	public void onCreate() {
		settings = getSharedPreferences("net.antonrifco.comingholidays", MODE_PRIVATE);
    	String countryCode = settings.getString("countryCode", "sg");
		
		XmlResourceParser xpp = null;        
        try {
            int test = getResources().getIdentifier("holiday_"+countryCode, "xml", getPackageName());
            if (test != 0) xpp = getResources().getXml(test);
        } finally{
        	if(xpp != null){
        		try {
					holidays = ComingHolidaysActivity.getEventsFromAnXML(xpp);
					
					/* Add personal leaves from Preferences */
					String leaves = settings.getString("leaves", "");
					if(leaves != null && !leaves.equals("")){
						String[] tarr = leaves.split("#");
						for(String t : tarr){
							if(t.equals("")) continue;
							Holiday thol = new Holiday(t);
							holidays.add(thol);
						}
					}
					
					/* sort holidays */
					List<Holiday> hols = holidays;
			   		if(hols.isEmpty()) return;
			   		List<Holiday> ret = new ArrayList<Holiday>();
			   		while(!hols.isEmpty()){
			   			int z = 0;
					   	if(hols.size() > 1){
			   				Holiday temp = (Holiday) hols.get(z);
			   		   		
				   			for(int i=1; i < hols.size(); i++){
				   				if(hols.get(i).getDate().getTime() < temp.getDate().getTime()){
				   					z = i;
				   					temp = (Holiday) hols.get(z);
				   				}
				   			}
			   			}
			   			ret.add((Holiday)(hols.get(z)));
			   			hols.remove(z);
			   		}
			   		holidays = ret;
				} catch (XmlPullParserException e) {
				} catch (IOException e) {
				}
        	}
        }
        
        List<Holiday> th = getComingHolidays(holidays);
		if(!th.isEmpty())
			showNotification(th);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		//Toast.makeText(this, "MyAlarmService.onBind()", Toast.LENGTH_LONG).show();
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//Toast.makeText(this, "MyAlarmService.onDestroy()", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		//Toast.makeText(this, "MyAlarmService.onStart()", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		//Toast.makeText(this, "MyAlarmService.onUnbind()", Toast.LENGTH_LONG).show();
		return super.onUnbind(intent);
	}

	private void showNotification(List<Holiday> lh) {
        // look up the notification manager service
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // The details of our fake message
        CharSequence from = getString(R.string.notify_upcoming_holidays);
        String message = getString(R.string.notify_intro_upcoming_holidays);

        for(Holiday thol: lh){
        	message += thol.getShortenedName() + ", ";
        }
        if(!message.equals(getString(R.string.notify_intro_upcoming_holidays))){
        	message = message.substring(0, message.length() - 2);
        }
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HolidaysIntro.class), 0);

        // The ticker text, this uses a formatted string so our message could be localized
        String tickerText = getString(R.string.notify_upcoming_holidays, message);

        // construct the Notification object.
        Notification notif = new Notification(R.drawable.stat_sample, tickerText,
                System.currentTimeMillis());

        // Set the info for the views that show in the notification panel.
        notif.setLatestEventInfo(this, from, message, contentIntent);

        // after a 100ms delay, vibrate for 250ms, pause for 100 ms and
        // then vibrate for 500ms.
        notif.vibrate = new long[] { 100, 250, 100, 500};

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification.  It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification.  It will always be a unique number within your
        // application.
        nm.notify(R.string.notify_upcoming_holidays, notif);
    }   
	
	public static List<Holiday> getComingHolidays(List<Holiday> th){
		List<Holiday> Lret = new ArrayList<Holiday>();
		Calendar temp = Calendar.getInstance();
		temp.set(Calendar.AM_PM, Calendar.AM);
		temp.set(Calendar.HOUR, 0);			
		long threshold_down = temp.getTimeInMillis();
		long threshold_up = threshold_down + (86400000 * 3);
		
		for(Holiday ht : th){
    		long tdate = ht.getDate().getTime();
    		if(tdate >= threshold_down && tdate < threshold_up){
        		Lret.add(ht);
        	}
    	}
    	return Lret;
    }
}