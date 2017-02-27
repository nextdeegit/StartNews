package com.example.hanson.startnews.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.hanson.startnews.R;
import com.example.hanson.startnews.db.FeedContract;

/**
 * Created by hanson on 2/27/17.
 */

public class NewsWidgetServices extends RemoteViewsService {

    private Context context;

    String[] PROJECTION = {
            FeedContract.Entry._ID,
            FeedContract.Entry.COLUMN_NAME_GUID,
            FeedContract.Entry.COLUMN_NAME_TITLE,
            FeedContract.Entry.COLUMN_NAME_DESCRIPTION,
            FeedContract.Entry.COLUMN_NAME_LINK,
            FeedContract.Entry.COLUMN_NAME_PUBLISHED,
            FeedContract.Entry.COLUMN_NAME_IMAGE_URL,
            FeedContract.Entry.COLUMN_NAME_COMPLETE_DES,
            FeedContract.Entry.COLUMN_NAME_IMAGE_URL_2
    };


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                String order = FeedContract.Entry.COLUMN_NAME_PUBLISHED + " DESC";
                data = getContentResolver().query(FeedContract.Entry.CONTENT_URI,
                        PROJECTION,
                        null,
                        null,
                        order);
                Log.d("Widget data", data + "");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }



                String newsTitle = data.getString(data.getColumnIndex(FeedContract.Entry.COLUMN_NAME_TITLE));
                String newsDes = data.getString(data.getColumnIndex(FeedContract.Entry.COLUMN_NAME_DESCRIPTION));
                String newsDate = data.getString(data.getColumnIndex(FeedContract.Entry.COLUMN_NAME_PUBLISHED));

                long newsID = data.getLong(data.getColumnIndex(FeedContract.Entry._ID));
                final RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);


                views.setTextViewText(R.id.news_title, newsTitle);
                views.setTextViewText(R.id.widget_description_view, newsDes);
                views.setTextViewText(R.id.widget_date_view, newsDate);
                String imageUrl = data.getString(data.getColumnIndex(FeedContract.Entry.COLUMN_NAME_IMAGE_URL));
//                if (imageUrl != null && imageUrl.length() > 0) {
//                    try {
//                        views.setImageViewBitmap(R.id.widget_image_view, Picasso.with(context).load(imageUrl).get());
//                    } catch (IOException e) {
//                        Log.e(TAG, "Unable to get image: " + imageUrl, e);
//                    }
//                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra("rowId", newsID);

                views.setOnClickFillInIntent(R.id.news_list, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}
