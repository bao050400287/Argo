package com.sysu.bbs.argo.view;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sysu.bbs.argo.DraftActivity;
import com.sysu.bbs.argo.R;
import com.sysu.bbs.argo.SettingsActivity;
import com.sysu.bbs.argo.util.SessionManager;
import com.sysu.bbs.argo.util.SessionManager.LoginListener;
import com.sysu.bbs.argo.util.SessionManager.LogoutListener;

public class RightMenuFragment extends ListFragment implements LoginListener, LogoutListener {
	
	private ArrayList<String> mList = new ArrayList<String>();
	private ArrayAdapter<String> mAdapter;
	private TextView mUserid;
	
	private ProgressDialog mLogoutProgressDialog;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frag_right_menu, container, false);
		mUserid = (TextView) v.findViewById(R.id.right_drawer_userid);
		
		mList.addAll(Arrays.asList(getResources().getStringArray(R.array.right_menu_items)));
		mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
				mList);		
		setListAdapter(mAdapter);
		
		if (SessionManager.isLoggedIn) {
			mList.remove(0);
			mList.add(0, "ע��");
			mAdapter.notifyDataSetChanged();

			mUserid.setText(SessionManager.getUsername());

		}
		
		SessionManager.loginListeners.add(this);
		SessionManager.logoutListeners.add(this);
		
		return v;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
		case 2:
			startActivity(new Intent(getActivity(), SettingsActivity.class));
			break;
		case 3:
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);

			PackageManager manager = getActivity().getPackageManager();
			PackageInfo info = null;
			try {
				info = manager.getPackageInfo(getActivity().getPackageName(), 0);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String version = info == null ? "unknown version" : info.versionCode + ", " + info.versionName;
			TextView message = (TextView)builder.setMessage(Html.fromHtml("Argo " + version + 
					"<br/>Author:<br/>&nbsp;&nbsp;վ��ID scim,<br/>"
					+ "&nbsp;&nbsp;΢�� <a href=\"http://weibo.com/crossie\">@���˾�ǿ��</a><br/>"))
					.setTitle("����").show().findViewById(android.R.id.message);
			message.setMovementMethod(LinkMovementMethod.getInstance());
					;
			break;
		case 1:
			Intent intent = new Intent(getActivity(), DraftActivity.class);
			startActivity(intent);
			break;
		case 0:
			if (SessionManager.isLoggedIn) {
				mLogoutProgressDialog = new ProgressDialog(getActivity());
				mLogoutProgressDialog.setMessage("ע����...");
				mLogoutProgressDialog.setCancelable(false);  
				mLogoutProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mLogoutProgressDialog.show();
				
				SessionManager sm = new SessionManager(getActivity(), null, null);				
				sm.logout();
			} else {
				LoginDialog loginDialog = new LoginDialog();
				loginDialog.show(getFragmentManager(), "loginDialog");
			}
			break;
		}
	}

	@Override
	public void succeeded(String userid) {
		mList.remove(0);
		mList.add(0, "ע��");
		mAdapter.notifyDataSetChanged();

		mUserid.setText(userid);

	}

	@Override
	public void failed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logout(boolean success) {
		if (success ) {
			mList.remove(0);
		
			mList.add(0, "��¼");
			mAdapter.notifyDataSetChanged();
	
			mUserid.setText("δ��¼");
		}
		if (mLogoutProgressDialog != null)
			mLogoutProgressDialog.dismiss();
		
	}

}