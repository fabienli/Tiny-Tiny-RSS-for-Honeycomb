package org.fox.ttrss;

import org.fox.ttrss.types.Article;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ArticlePager extends Fragment {

	private final String TAG = "ArticlePager";
	private PagerAdapter m_adapter;
	private OnlineServices m_onlineServices;
	private HeadlinesFragment m_hf; 
	private Article m_article;
	
	private class PagerAdapter extends FragmentStatePagerAdapter {
		
		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Article article = m_hf.getArticleAtPosition(position);
			
			if (article != null) {
				ArticleFragment af = new ArticleFragment(article);
				return af;
			}
			return null;
		}

		@Override
		public int getCount() {
			return m_hf.getAllArticles().size();
		}
		
	}
	
	public ArticlePager() {
		super();
	}
	
	public ArticlePager(Article article) {
		super();
		
		m_article = article;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {    	
		View view = inflater.inflate(R.layout.article_pager, container, false);
	
		m_adapter = new PagerAdapter(getActivity().getSupportFragmentManager());
		
		ViewPager pager = (ViewPager) view.findViewById(R.id.article_pager);
		
		int position = m_hf.getArticlePosition(m_article);
		
		pager.setAdapter(m_adapter);
		pager.setCurrentItem(position);
		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int position) {
				Article article = m_hf.getArticleAtPosition(position);
				
				if (article != null) {
					if (article.unread) {
						article.unread = false;
						m_onlineServices.saveArticleUnread(article);
					}
					m_onlineServices.setSelectedArticle(article);
					
					//Log.d(TAG, "Page #" + position + "/" + m_adapter.getCount());
					
					if (position == m_adapter.getCount() - 5) {
						m_hf.refresh(true);
						m_adapter.notifyDataSetChanged();
					}
				}
			}
		});
	
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);		
		
		m_hf = (HeadlinesFragment) getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.FRAG_HEADLINES);
		m_onlineServices = (OnlineServices)activity;
	}

}
