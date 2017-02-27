package com.example.hanson.startnews;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hanson.startnews.net.RSSParser;
import com.example.hanson.startnews.services.SyncUtils;
import com.example.hanson.startnews.utils.Connection;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "MainActivity";
    public Context nContext;
    private boolean mTwoPane;
    private RecyclerView mRecyclerView;
    boolean isConnected;
    private TextView noNetworkText;
    private ProgressDialog mProgress;
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_INVITE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(findViewById(R.id.drawer_layout)!=null){

            mTwoPane = false ;

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }else {
            mTwoPane = true;
        }

        RSSParser.link = "http://www.techrepublic.com/mediafed/articles/latest/";

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SyncUtils.CreateSyncAccount(this);
        mProgress = new ProgressDialog(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppInvite.API)
                .build();


        isConnected = Connection.isNetworkAvailable(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        noNetworkText = (TextView) findViewById(R.id.no_network_text_view);

        getSupportLoaderManager().initLoader(0, null, this);

        nContext = this;
        loadNews();



    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode +
                ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent.
                String[] ids = AppInviteInvitation
                        .getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Sending failed or it was canceled, show failure message to
                // the user
                Log.d(TAG, "Failed to send invitation.");
            }
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (id == R.id.invite_menu){

            sendInvitation();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_technology) {
            // Handle the camera action
            RSSParser.link = "http://www.techrepublic.com/mediafed/articles/latest/";
            loadNews();

        } else if (id == R.id.nav_sport) {


            RSSParser.link = "http://api.foxsports.com/v1/rss?partnerKey=zBaFxRyGKCfxBagJG9b8pqLyndmvo7UU&tag=soccer";
            loadNews();
        } else if (id == R.id.nav_business) {


            RSSParser.link = "http://businessnews.com.ng/feed/";
            loadNews();

        } else if (id == R.id.nav_local) {

            RSSParser.link = "http://thenationonlineng.net/feed/";
            loadNews();




        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }




    @Override
    public void onDestroy() {


        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return MyLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        LinearLayoutManager lm =
                new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(lm);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private void loadNews() {

        if (isConnected) {
            noNetworkText.setVisibility(View.GONE);
            mProgress.setMessage(getString(R.string.progress_dialogue));
            mProgress.show();
            progressDelay(6000,mProgress);
            SyncUtils.TriggerRefresh();
        } else {
            noNetworkText.setVisibility(View.VISIBLE);
            return;
        }
    }


// Recycler View Adapter

    private class Adapter extends RecyclerView.Adapter<ViewHolder>  {




        private Cursor mCursor ;


        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(MyLoader.Query.COLUMN_ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.news_item, parent, false);

            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(isConnected) {



                        long rowId = getItemId(vh.getAdapterPosition());


                        Intent feedDetail = new Intent(getApplicationContext(), NewsDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong("rowId", rowId);
                        feedDetail.putExtras(bundle);
                        startActivity(feedDetail);
                    }else {

                        Connection.showToastForDuration(getApplicationContext(), getString(R.string.offline_text), 5000,
                                Gravity.CENTER);
                    }
                }
            });



            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(MyLoader.Query.COLUMN_TITLE));
            holder.description.setText(mCursor.getString(MyLoader.Query.COLUMN_DESC));
            holder.pubDate.setText(mCursor.getString(MyLoader.Query.COLUMN_PUB_DATE));


            if(mCursor.getString(MyLoader.Query.COLUMN_PHOTO_URL)==null){
                Glide.with(holder.thumbnailView.getContext()).load(mCursor.getString(
                        MyLoader.Query.COLUMN_PHOTO_URL2))

                        //load images as bitmaps to get fixed dimensions
                        .asBitmap()

                        //set a placeholder image
                        .placeholder(R.mipmap.ic_launcher)

                        //disable cache to avoid garbage collection that may produce crashes
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(holder.thumbnailView);


            }else{

                Glide.with(holder.thumbnailView.getContext()).load(mCursor.getString(
                        MyLoader.Query.COLUMN_PHOTO_URL))

                        //load images as bitmaps to get fixed dimensions
                        .asBitmap()

                        //set a placeholder image
                        .placeholder(R.mipmap.ic_launcher)

                        //disable cache to avoid garbage collection that may produce crashes
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(holder.thumbnailView);

            }

        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView titleView;
        public TextView description;
        public TextView pubDate;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.image_view);
            titleView = (TextView) view.findViewById(R.id.title_view);
            description = (TextView) view.findViewById(R.id.description_view);
            pubDate = (TextView) view.findViewById(R.id.date_view);
        }
    }

    public void progressDelay(long time, final Dialog d){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                d.dismiss();
            }
        }, time);
    }


}
