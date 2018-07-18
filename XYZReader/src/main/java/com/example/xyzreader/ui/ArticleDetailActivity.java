package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.support.design.widget.FloatingActionButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";
    private Cursor mCursor;
    private FloatingActionButton floatingActionButton;
    private ImageView photoView;
    private TextView articleTitle;
    private TextView articleByline;
    private TextView articleBody;
    private int mSelectedItemId;
    private Toolbar mDetailToolbar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);
        photoView = (ImageView) findViewById(R.id.photo);
        articleTitle = (TextView) findViewById(R.id.article_title);
        articleByline = (TextView) findViewById(R.id.article_byline);
        articleBody = (TextView) findViewById(R.id.article_body);


        mDetailToolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(mDetailToolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);}
        getLoaderManager().initLoader(0, null, this);
        floatingActionButton =(FloatingActionButton)findViewById(R.id.share_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });






        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mSelectedItemId = getIntent().getIntExtra("object",0);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mCursor.moveToPosition(mSelectedItemId);
        // Select the start ID
        String articleTitle1 = mCursor.getString(cursor.getColumnIndex(ItemsContract.ItemsColumns.TITLE));
        String articleBody1 = mCursor.getString(cursor.getColumnIndex(ItemsContract.ItemsColumns.BODY));
        articleTitle.setText(articleTitle1);
        articleBody.setText(Html.fromHtml(articleBody1.replaceAll("(\r\n\r\n)", "<br /><br />")));

        String articleAuthor1 = mCursor.getString(cursor.getColumnIndex(ItemsContract.ItemsColumns.AUTHOR));
        String articleDate1 = mCursor.getString(cursor.getColumnIndex(ItemsContract.ItemsColumns.PUBLISHED_DATE));

        Date publishedDate = parsePublishedDate(articleDate1);
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            articleByline.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + articleAuthor1));


        } else {
            // If date is before 1902, just show the string
            articleByline.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                            + " by "
                            + articleAuthor1));

        }

        String imageUrl = mCursor.getString(cursor.getColumnIndex(ItemsContract.ItemsColumns.PHOTO_URL));
        Picasso.with(this)
                .load(imageUrl)
                .into(photoView);
    }
    private Date parsePublishedDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }


}
