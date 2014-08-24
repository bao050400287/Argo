package com.sysu.bbs.argo.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.TopicListActivity;
import com.sysu.bbs.argo.adapter.PostAdapter;
import com.sysu.bbs.argo.api.dao.Post;
import com.sysu.bbs.argo.api.dao.PostHead;

public class NormalFragment extends AbstractBoardFragment<PostHead> implements
		OnItemClickListener {

	PostAdapter mPostAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_normal, container, false);

		mListView = (PullToRefreshListView) v.findViewById(android.R.id.list);
		mListView.setOnRefreshListener(this);
		mListView.setMode(Mode.BOTH);
		mListView.setEmptyView(v.findViewById(android.R.id.empty));

		mType = "normal";

		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPostAdapter = new PostAdapter(getActivity(),
				android.R.layout.simple_list_item_1, mDataList, mCurrBoard);
		mAdapter = mPostAdapter;
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		registerForContextMenu(mListView.getRefreshableView());
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	protected void add2DataList(PostHead postHead, boolean head) {
		//String filename = postHead.getFilename();
		if (head)
			mDataList.add(0, postHead);
		else
			mDataList.add(postHead);

	}

	@Override
	protected void setAdapterBoard(String board) {
		mPostAdapter.setBoardName(board);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		// 
		//getActivity().getMenuInflater().inflate(R.menu.post_popup, menu);
		//super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(R.layout.frag_normal, R.string.menu_title_share, 0, R.string.menu_title_share);
		menu.add(R.layout.frag_normal, R.string.menu_title_copy, 0, R.string.menu_title_copy);
		menu.add(R.layout.frag_normal, R.string.menu_title_topic, 0, R.string.menu_title_topic);
		
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View listItem, int pos,
			long id) {

		mListView.getRefreshableView().showContextMenuForChild(listItem);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() != R.layout.frag_normal)
			return false;

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Post post = mPostAdapter.getPost(mPostAdapter.getItem(info.position - 1).getFilename());
		
		String link = String.format("http://bbs.sysu.edu.cn/bbscon?board=%s&file=%s", post.getBoard(), post.getFilename());
		String content = "������: %s (%s), ����: %s\n" 
						 + "��  ��: %s\n"	
						 + "����ʱ��: %s\n"
						 + "ԭ������: %s\n"
						 + "%s";
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMM HH:mm   ", Locale.US);
		Calendar update = Calendar.getInstance();
		update.setTimeInMillis(1000*Long.valueOf(post.getPost_time()));
		Date date = update.getTime();
		
		content = String.format(content, post.getUserid(),
				post.getUsername(),
				post.getBoard(),
				post.getTitle(),
				sdf.format(date),
				link,
				post.getRawcontent());
		
		Intent intent = null;
		
		switch (item.getItemId()) {
		case R.string.menu_title_copy:		
			ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("post", content);
			cm.setPrimaryClip(clip);
			Toast.makeText(getActivity(), "���Ƴɹ�", Toast.LENGTH_SHORT).show();
			return true;
		case R.string.menu_title_topic:
			intent = new Intent(getActivity(), TopicListActivity.class);
			intent.putExtra("boardname", post.getBoard());
			intent.putExtra("filename", post.getFilename());
			
			startActivity(intent);
			return true;
		case R.string.menu_title_share:
			intent = new Intent(Intent.ACTION_SEND);

			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "�������ݺ�����");
			intent.putExtra(Intent.EXTRA_TEXT, content);
			startActivity(Intent.createChooser(intent, "����..."));
			return true;
		default:
			break;
		}
		return super.onContextItemSelected(item);

	}
}
