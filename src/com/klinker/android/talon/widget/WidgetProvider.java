/*
 * Copyright 2013 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.talon.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import com.klinker.android.talon.R;
import com.klinker.android.talon.ui.ComposeActivity;
import com.klinker.android.talon.ui.MainActivity;
import com.klinker.android.talon.ui.TweetActivity;
import com.klinker.android.talon.ui.TweetActivityWidget;

public class WidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent updateWidget = new Intent(context, CardWidgetService2.class);
        context.startService(updateWidget);

        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.klinker.android.talon.UPDATE_WIDGET")) {
            Intent updateWidget = new Intent(context, CardWidgetService2.class);
            context.startService(updateWidget);
        } else if (intent.getAction().equals("OPEN_APP")) {

            Intent viewTweet = new Intent(context, TweetActivityWidget.class);
            viewTweet.putExtra("name", intent.getStringExtra("name"));
            viewTweet.putExtra("screenname", intent.getStringExtra("screenname"));
            viewTweet.putExtra("time", intent.getLongExtra("time", 0));
            viewTweet.putExtra("tweet", intent.getStringExtra("tweet"));
            viewTweet.putExtra("retweeter", intent.getStringExtra("retweeter"));
            viewTweet.putExtra("webpage", intent.getStringExtra("webpage"));
            viewTweet.putExtra("picture", false);
            viewTweet.putExtra("tweetid", intent.getStringExtra("tweetid"));
            viewTweet.putExtra("proPic", intent.getStringExtra("propic"));
            viewTweet.putExtra("from_widget", true);

            viewTweet.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(viewTweet);
        } else {
            super.onReceive(context, intent);
        }
    }

    public static class CardWidgetService2 extends IntentService {
        public CardWidgetService2() {
            super("card_widget_service");
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        @Override
        protected void onHandleIntent(Intent arg0) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            ComponentName thisAppWidget = new ComponentName(this.getPackageName(), WidgetProvider.class.getName());
            int[] appWidgetIds = mgr.getAppWidgetIds(thisAppWidget);

            boolean darkTheme = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "1")) != 0;
            RemoteViews views = new RemoteViews(this.getPackageName(), darkTheme ? R.layout.widget_dark : R.layout.widget_light);
            views.setViewVisibility(R.id.replyButton, View.VISIBLE);

            for (int i = 0; i < appWidgetIds.length; i++) {
                mgr.updateAppWidget(appWidgetIds[i], views);
            }

            for (int i = 0; i < appWidgetIds.length; i++) {
                int appWidgetId = appWidgetIds[i];

                Intent quickText = new Intent(this, ComposeActivity.class);
                quickText.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent quickPending = PendingIntent.getActivity(this, 0, quickText, 0);

                Intent intent2 = new Intent(this, WidgetService.class);
                intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent2.setData(Uri.parse(intent2.toUri(Intent.URI_INTENT_SCHEME)));

                Intent openApp = new Intent(this, MainActivity.class);
                PendingIntent openAppPending = PendingIntent.getActivity(this, 0, openApp, 0);

                views.setRemoteAdapter(R.id.widgetList, intent2);
                views.setEmptyView(R.id.widgetList, R.color.light_background);

                views.setOnClickPendingIntent(R.id.textView1, openAppPending);
                views.setOnClickPendingIntent(R.id.replyButton, quickPending);

                Intent openIntent = new Intent(this, WidgetProvider.class);
                openIntent.setAction("OPEN_APP");
                openIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
                openIntent.setData(Uri.parse(openIntent.toUri(Intent.URI_INTENT_SCHEME)));
                PendingIntent openPendingIntent = PendingIntent.getBroadcast(this, 0, openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                views.setPendingIntentTemplate(R.id.widgetList, openPendingIntent);

                mgr.updateAppWidget(appWidgetId, views);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetList);
            }

            stopSelf();
        }

    }
}