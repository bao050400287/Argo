package com.sysu.bbs.argo;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sysu.bbs.argo.util.PostService;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.Splitter;
import com.sysu.bbs.argo.view.LeftMenuFragment;
//import com.sysu.bbs.argo.view.LoginDialog.Communicator;
import com.sysu.bbs.argo.view.LeftMenuFragment.BoardChangedListener;
import com.sysu.bbs.argo.view.LoginDialog;

public class AddPostActivity extends SwipeBackActivity implements BoardChangedListener {

	private EditText mEditTitle;
	private EditText mEditContent;
	private Bundle mNewPostBundle;
	
	private Button mChooseBoard;
	
	private BroadcastReceiver mLoginReceiver;

	// String mDraft;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_post);

	/*	getSwipeBackLayout().setEdgeSize(
				getWindowManager().getDefaultDisplay().getWidth());*/

		mEditTitle = (EditText) findViewById(R.id.new_post_title);
		mEditContent = (EditText) findViewById(R.id.new_post_content);
		mChooseBoard = (Button) findViewById(R.id.new_post_choose_board);
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {

			mEditContent.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
			mNewPostBundle = new Bundle();
			mNewPostBundle.putString("type", "new");
			mChooseBoard.setVisibility(View.VISIBLE);
			mChooseBoard.setText("选择版面");
			

			return;
		}
		mNewPostBundle = intent.getExtras();

		if (mNewPostBundle.getInt("_where_") == 1) { // reply button
			if (mNewPostBundle.getString("type").equals("reply")) {

				if (!mNewPostBundle.getString("title").startsWith("Re: "))
					mEditTitle.setText("Re: "
							+ mNewPostBundle.getString("title"));
				else
					mEditTitle.setText(mNewPostBundle.getString("title"));

				String tmp = mNewPostBundle.getString("content");

				tmp = tmp.substring(0, Math.min(200, tmp.length()));
				tmp = tmp.replaceAll("(?m)^", ": ").replace("\n", "<br/>");

				String quote = "<br/><font color=\"#888888\">【 在 %s (%s) 的大作中提到: 】<br/>%s</font>";
				quote = String.format(quote,
						mNewPostBundle.getString("userid"),
						mNewPostBundle.getString("username"), tmp);

				mEditContent.setText(Html.fromHtml(quote));
			}
		} else if (mNewPostBundle.getInt("_where_") == 2) {// draft

			mEditTitle.setText(mNewPostBundle.getString("title"));

			String content = "%s<br/><font color=\"#888888\">%s</font>";
			String parsedContent = "";
			String parsedQuote = "";

			Splitter splitter = new Splitter("【 在 .* 的大作中提到: 】", true);
			String[] tmp = splitter.split(mNewPostBundle.getString("content"));
			if (tmp != null) {
				parsedContent = tmp[0];
				if (tmp.length > 1) {
					parsedQuote = "";
					for (int i = 1; i < tmp.length; i++)
						parsedQuote += tmp[i] + "<br/>";
				}

			}
			content = String.format(content, parsedContent, parsedQuote);

			mEditContent.setText(Html.fromHtml(content));

			// mDraft = mNewPostBundle.getString("_draft_");
		}
		
		mLoginReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context con, Intent intent) {
				String action = intent.getAction();
				if (action.equals(SessionManager.BROADCAST_LOGIN)) {
					String userid = intent.getStringExtra("userid");
					if (userid != null && !userid.equals("")) {
						sendPost();
					} 
				}
				
			}
		};

	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SessionManager.BROADCAST_LOGIN);  
		registerReceiver(mLoginReceiver, intentFilter);
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterReceiver(mLoginReceiver);
	}
	public void onClick(View v) {
		LeftMenuFragment chooseBoard = null;
		switch (v.getId()) {
		case R.id.new_post_choose_board:
			chooseBoard = new LeftMenuFragment();
			chooseBoard.show(getSupportFragmentManager(), "chooseboard");
			break;
		case R.id.new_post_send:
			if (mNewPostBundle.getString("boardname") == null || 
				mNewPostBundle.getString("boardname").equals("")) {
				chooseBoard = new LeftMenuFragment();
				chooseBoard.show(getSupportFragmentManager(), "chooseboard");
				break;
			}
			if (SessionManager.isLoggedIn) {

				sendPost();

			} else {

				LoginDialog loginDialog = new LoginDialog();
				loginDialog.show(getSupportFragmentManager(), "loginDialog");

			}
			break;
		}

	}

	public void sendPost() {
		Intent service = new Intent(this, PostService.class);

		mNewPostBundle.putString("title", mEditTitle.getText().toString());
		mNewPostBundle.putString("content", mEditContent.getText().toString());

		service.putExtras(mNewPostBundle);

		startService(service);
		finish();
	}

	@Override
	public void changeBoard(String boardname) {
		mNewPostBundle.putString("boardname", boardname);
		mChooseBoard.setText(boardname);

	}

}
