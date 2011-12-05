package org.fox.ttrss;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.fox.ttrss.ArticleOps.RelativeArticle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class OfflineArticleFragment extends Fragment implements OnClickListener {
	@SuppressWarnings("unused")
	private final String TAG = this.getClass().getSimpleName();

	private SharedPreferences m_prefs;
	private int m_articleId;
	private GestureDetector m_gestureDetector;
	private View.OnTouchListener m_gestureListener;
	private Cursor m_cursor;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {    	

		if (savedInstanceState != null) {
			m_articleId = savedInstanceState.getInt("articleId");
		}
		
		View view = inflater.inflate(R.layout.article_fragment, container, false);

		m_gestureDetector = new GestureDetector(new MyGestureDetector());
		m_gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent aEvent) {
                if (m_gestureDetector.onTouchEvent(aEvent))
                    return true;
                else
                    return false;
                }
            };
            
		
		// TODO change to interface?
		OfflineActivity activity = (OfflineActivity)getActivity();
		
		if (activity != null) {		
			int orientation = activity.getWindowManager().getDefaultDisplay().getOrientation();
			
			if (!activity.isSmallScreen()) {			
				if (orientation % 2 == 0) {
					view.findViewById(R.id.splitter_horizontal).setVisibility(View.GONE);
				} else {
					view.findViewById(R.id.splitter_vertical).setVisibility(View.GONE);
				}
			} else {
				view.findViewById(R.id.splitter_vertical).setVisibility(View.GONE);
				view.findViewById(R.id.splitter_horizontal).setVisibility(View.GONE);
			}
		} else {
			view.findViewById(R.id.splitter_horizontal).setVisibility(View.GONE);
		}
		
		m_cursor = ((OfflineActivity)getActivity()).getReadableDb().query("articles", null, BaseColumns._ID + "=?", 
				new String[] { String.valueOf(m_articleId) }, null, null, null);

		m_cursor.moveToFirst();
		
		if (m_cursor.isFirst()) {
			
			TextView title = (TextView)view.findViewById(R.id.title);
			
			if (title != null) {
				
				String titleStr;
				
				if (m_cursor.getString(m_cursor.getColumnIndex("title")).length() > 200)
					titleStr = m_cursor.getString(m_cursor.getColumnIndex("title")).substring(0, 200) + "...";
				else
					titleStr = m_cursor.getString(m_cursor.getColumnIndex("title"));
				
				title.setMovementMethod(LinkMovementMethod.getInstance());
				title.setText(Html.fromHtml("<a href=\""+m_cursor.getString(m_cursor.getColumnIndex("link")).replace("\"", "\\\"")+"\">" + titleStr + "</a>"));
			}
			
			WebView web = (WebView)view.findViewById(R.id.content);
			
			if (web != null) {
				
				String content;
				String cssOverride = "";
				
				
				//WebSettings ws = web.getSettings();
				//ws.setSupportZoom(true);
				//ws.setBuiltInZoomControls(true);

				if (m_prefs.getString("theme", "THEME_DARK").equals("THEME_DARK")) {
					cssOverride = "body { background : black; color : #e0e0e0}\n";			
					web.setBackgroundColor(android.R.color.black);
				} else {
					cssOverride = "";
				}
				
				content = 
					"<html>" +
					"<head>" +
					"<meta content=\"text/html; charset=utf-8\" http-equiv=\"content-type\">" +
					//"<meta name=\"viewport\" content=\"target-densitydpi=device-dpi\" />" +
					"<style type=\"text/css\">" +
					cssOverride +
					"img { max-width : 98%; height : auto; }" +
					"body { text-align : justify; }" +
					"</style>" +
					"</head>" +
					"<body>" + m_cursor.getString(m_cursor.getColumnIndex("content")) + "</body></html>";
					
				web.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
				
				if (activity.isSmallScreen())
					web.setOnTouchListener(m_gestureListener);
			}
			
			TextView dv = (TextView)view.findViewById(R.id.date);
			
			if (dv != null) {
				Date d = new Date(m_cursor.getInt(m_cursor.getColumnIndex("updated")) * 1000L);
				SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy, HH:mm");
				dv.setText(df.format(d));
			}
			
			TextView tagv = (TextView)view.findViewById(R.id.tags);
						
			if (tagv != null) {
				String tagsStr = m_cursor.getString(m_cursor.getColumnIndex("tags"));
				tagv.setText(tagsStr);
			}			
			
			AdView av = (AdView)view.findViewById(R.id.ad);
			
			if (av != null) {
				av.setVisibility(View.GONE);
			}
			
			ImageView next = (ImageView)view.findViewById(R.id.next_article);
			
			if (next != null) {
//				if (m_nextArticle != null) {
//					next.setOnClickListener(this);
//				} else {
					next.setImageResource(R.drawable.ic_next_article_disabled);
//				}
			}
			
			ImageView prev = (ImageView)view.findViewById(R.id.prev_article);
			
			if (prev != null) {
//				if (m_prevArticle != null) {
//					prev.setOnClickListener(this);
//				} else {
					prev.setImageResource(R.drawable.ic_prev_article_disabled);
//				}
			}

		} 
		
		return view;    	
	}

	@Override
	public void onDestroy() {
		super.onDestroy();	
		
		m_cursor.close();
	}
	
	@Override
	public void onSaveInstanceState (Bundle out) {		
		super.onSaveInstanceState(out);
		
		out.putInt("articleId", m_articleId);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);		
		
		m_prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		
		m_articleId = ((OfflineActivity)activity).getSelectedOfflineArticleId();
		/* m_articleOps = (ArticleOps)activity;
		m_article = m_articleOps.getSelectedArticle(); 
		
		m_prevArticle = m_articleOps.getRelativeArticle(m_article, RelativeArticle.BEFORE);
		m_nextArticle = m_articleOps.getRelativeArticle(m_article, RelativeArticle.AFTER); */
	}

	@Override
	public void onClick(View v) {
		/* if (v.getId() == R.id.next_article) {
			m_articleOps.openArticle(m_nextArticle, 0);
		} else if (v.getId() == R.id.prev_article) {
			m_articleOps.openArticle(m_prevArticle, R.anim.slide_right);
		} */
	}
	
	// http://blog.blackmoonit.com/2010/07/gesture-detection-swipe-detection_4292.html
	class MyGestureDetector extends SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 100;
        private static final int SWIPE_MAX_OFF_PATH = 100;
        private static final int SWIPE_THRESHOLD_VELOCITY = 100;
 
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float dX = e2.getX()-e1.getX();
            float dY = e1.getY()-e2.getY();
            if (Math.abs(dY)<SWIPE_MAX_OFF_PATH &&
                Math.abs(velocityX)>=SWIPE_THRESHOLD_VELOCITY &&
                Math.abs(dX)>=SWIPE_MIN_DISTANCE ) {
                if (dX>0) {
                    //Log.d(TAG, "Right swipe");
                    
                    //if (m_prevArticle != null)
                    //	m_articleOps.openArticle(m_prevArticle, R.anim.slide_right);
                    
                } else {
                    //Log.d(TAG, "Left swipe");
                    
                    //if (m_nextArticle != null)
                    //	m_articleOps.openArticle(m_nextArticle, 0);

                }
                return true;
            /* } else if (Math.abs(dX)<SWIPE_MAX_OFF_PATH &&
                Math.abs(velocityY)>=SWIPE_THRESHOLD_VELOCITY &&
                Math.abs(dY)>=SWIPE_MIN_DISTANCE ) {
                if (dY>0) {
                    Log.d(TAG, "Up swipe");
                } else {
                    Log.d(TAG, "Down swipe");
                }
                return true; */
            }
            return false;
        }
    };
}