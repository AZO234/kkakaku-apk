package com.kkakaku;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends Activity {

	private int iWebEngine;
	private boolean bHistory;
	private boolean bOption;
	
	private boolean bBarcode;
	private String strBarcode;

	private Button btnClear;
	private Button btnHistory;
	private Button btnGetBarcode;
	private Button btnGetEAN;
	private Button btnGetISBN;
	private Button btnBackToNum;
	private Button btnSearch;
	private Button btnWebSearch;
	private Button btnShareText;
	private EditText edtText;
	private EditText edtMNP;
	private EditText edtMXP;
	private RadioButton radio0;
	private RadioButton radio1;
	private RadioButton radio2;
	private RadioButton radio3;
	private RadioButton radio4;
	private RadioButton radio5;
	private RadioButton radio6;
	private RadioButton radio7;
	private RadioButton radio8;
	private RadioButton radio9;
	private RadioButton radio10;
	private RadioButton radio11;
	private Toast toast0;
	private ArrayList<String> history;

	// ユーザー領域
	private SharedPreferences pref;
	private Editor prefedit;
	private static final int iHistoryNum = 10;

	private static ProgressDialog waitDialog;
    private Thread thread;
    private int iSearchType;
    private String strProductName;
    private boolean bNetworkFailed;
    private boolean bNetworking;
	private static final int iNetworkTimeout = 30000;
	private static final int msgidSearched = 0;
	private static final int msgidTimeout = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bNetworkFailed = true;
		bNetworking = false;
		
		history = new ArrayList<String>();

		// プログレスダイアログの設定
		waitDialog = new ProgressDialog(this);
		// プログレスダイアログのメッセージを設定します
		waitDialog.setMessage("検索中・・・");
		// 円スタイル（くるくる回るタイプ）に設定します
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		// ユーザー領域を読み出す
		pref = getSharedPreferences("pref", MODE_PRIVATE);
		prefedit = pref.edit();

		iWebEngine = pref.getInt("WebEngine", 0);
		bHistory = pref.getBoolean("History", true);
		if(bHistory) {
			for(int i = 0; i < iHistoryNum; i++) {
				String strHistory = pref.getString("History" + String.format("%02d", i), "");
				if(!strHistory.equals("")) {
					history.add(strHistory);
				}
			}
		}
		bOption = pref.getBoolean("Option", true);
		int iOptionSort = 0;
		int iOptionMNP = 0;
		int iOptionMXP = 0;
		int iOptionCondition = 0;
		int iOptionShipfree = 0;
		int iOptionEndTime = 0;
		int iOptionProductID = 0;
		if(bOption) {
			iOptionSort = pref.getInt("OptionSort", 0);
			iOptionMNP = pref.getInt("OptionMNP", 0);
			iOptionMXP = pref.getInt("OptionMXP", 0);
			iOptionCondition = pref.getInt("OptionCondition", 0);
			iOptionShipfree = pref.getInt("OptionShipfree", 0);
			iOptionEndTime = pref.getInt("OptionEndTime", 0);
			iOptionProductID = pref.getInt("OptionProductID", 0);
		}

		btnClear = (Button)findViewById(R.id.id_btnClear);
		btnHistory = (Button)findViewById(R.id.id_btnHistory);
		btnGetBarcode = (Button)findViewById(R.id.id_GetBarcode);
		btnSearch = (Button)findViewById(R.id.id_btnSearch);
		btnWebSearch = (Button)findViewById(R.id.id_btnWebSearch);
		btnShareText = (Button)findViewById(R.id.id_btnShareText);
		edtText = (EditText)findViewById(R.id.id_Text);
		edtMNP = (EditText)findViewById(R.id.id_edtMNP);
		edtMXP = (EditText)findViewById(R.id.id_edtMXP);
		radio0 = (RadioButton)findViewById(R.id.radio0);
		radio1 = (RadioButton)findViewById(R.id.radio1);
		radio2 = (RadioButton)findViewById(R.id.radio2);
		radio3 = (RadioButton)findViewById(R.id.radio3);
		radio4 = (RadioButton)findViewById(R.id.radio4);
//		radio5 = (RadioButton)findViewById(R.id.radio5);
//		radio6 = (RadioButton)findViewById(R.id.radio6);
//		radio7 = (RadioButton)findViewById(R.id.radio7);
		radio8 = (RadioButton)findViewById(R.id.radio8);
		radio9 = (RadioButton)findViewById(R.id.radio9);
		radio10 = (RadioButton)findViewById(R.id.radio10);
		radio11 = (RadioButton)findViewById(R.id.radio11);
		toast0 = Toast.makeText(this,  "",  Toast.LENGTH_SHORT);

		switch(iOptionSort) {
		case 1:
			radio1.setChecked(true);
			break;
		case 2:
			radio2.setChecked(true);
			break;
		default:
			radio0.setChecked(true);
			break;
		}
		edtMNP.setText(String.valueOf(iOptionMNP));
		edtMXP.setText(String.valueOf(iOptionMXP));
		if (iOptionCondition == 1) {
			radio4.setChecked(true);
		} else {
			radio3.setChecked(true);
		}
		if (iOptionShipfree == 1) {
			radio11.setChecked(true);
		} else {
			radio10.setChecked(true);
		}
		switch(iOptionEndTime) {
		case 1:
			radio6.setChecked(true);
			break;
		case 2:
			radio7.setChecked(true);
			break;
		default:
			radio5.setChecked(true);
			break;
		}
		if (iOptionProductID == 1) {
			radio9.setChecked(true);
		} else {
			radio8.setChecked(true);
		}

		final AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(this);
		final AlertDialog.Builder HistoryDialog = new AlertDialog.Builder(this);

		prefcommit();

		edtText.setText("");
		clear();

		btnClear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				edtText.setText("");
				clear();
			}
		});

		edtText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO 自動生成されたメソッド・スタブ
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO 自動生成されたメソッド・スタブ
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO 自動生成されたメソッド・スタブ

				checkinput();
			}
		});
		
		edtText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO 自動生成されたメソッド・スタブ
				return event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER;
			}
			
		});

		edtMNP.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO 自動生成されたメソッド・スタブ
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO 自動生成されたメソッド・スタブ
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO 自動生成されたメソッド・スタブ
				checkinput();
			}
		});

		edtMXP.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO 自動生成されたメソッド・スタブ
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO 自動生成されたメソッド・スタブ
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO 自動生成されたメソッド・スタブ
				checkinput();
			}
		});

		btnGetBarcode.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				btnGetBarcode.setEnabled(false);

				Intent intentScan = new Intent("com.google.zxing.client.android.SCAN");
				intentScan.putExtra("SCAN_MODE", "PRODUCT_MODE");
				try {
					startActivityForResult(intentScan, 1);
				} catch(ActivityNotFoundException e) {
					toast0.setText("バーコードの読み出しに失敗しました。");
					toast0.show();
				}

				btnGetBarcode.setEnabled(true);
			}
		});

		btnHistory.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				HistoryDialog.setTitle("検索履歴");
				// 履歴を逆順に並べ替え
				ArrayList<String> backhistory = new ArrayList<String>();
				for(int i = history.size() - 1; i >= 0; i--) {
					backhistory.add(history.get(i));
				}
				CharSequence[] cs = backhistory.toArray(new CharSequence[0]);

				HistoryDialog.setSingleChoiceItems(cs, -1, new DialogInterface.OnClickListener() {
						
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自動生成されたメソッド・スタブ
						ArrayList<String> backhistory = new ArrayList<String>();
						for(int i = history.size() - 1; i >= 0; i--) {
							backhistory.add(history.get(i));
						}
						CharSequence[] cs = backhistory.toArray(new CharSequence[0]);
						edtText.setText(cs[which]);
							
						bBarcode = false;
					}
				});
				HistoryDialog.show();
			}
		});

		btnWebSearch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				// 入力チェック
				checkinput();
				
				// ボタン有効時
				if(btnWebSearch.isEnabled()) {
					// ボタンを無効化
					btnWebSearch.setEnabled(false);

					String strURI;
					switch(iWebEngine) {
					// Google.com
					case 1:
						strURI = "https://google.com/search?q=" + edtText.getText().toString() + "&ei=UTF-8&safe=off";
						break;
					// Yahooe.com
					case 2:
						strURI = "https://search.yahoo.com/search?q=" + edtText.getText().toString() + "&ei=UTF-8&safe=off";
						break;
					// Bing
					case 3:
						strURI = "https://www.bing.com/search?q=" + edtText.getText().toString();
						break;
					// DuckDuckGo
					case 4:
						strURI = "https://duckduckgo.com/?q=" + edtText.getText().toString();
						break;
					// Yandex
					case 5:
						strURI = "https://yandex.com/search/?q=" + edtText.getText().toString();
						break;
					// Yahoo! JAPAN
					default:
						strURI = "http://search.yahoo.co.jp/search?p=" + edtText.getText().toString() + "&ei=UTF-8&save=0";
						break;
					}

					// URLを指定してブラウザを開く
					Uri uri = Uri.parse(strURI);
					Intent i = new Intent( Intent.ACTION_VIEW, uri );
					startActivity( i );

					// ボタンを有効化
					btnWebSearch.setEnabled(true);
				}
			}
		});

		btnShareText.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				// 入力チェック
				checkinput();
				
				// ボタン有効時
				if(btnShareText.isEnabled()) {
					// ボタンを無効化
					btnShareText.setEnabled(false);

					// 検索ワードを共有
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_TEXT, edtText.getText().toString() + " (by " + R.string.app_base_url + "kkc.php #kkakaku)");
					startActivity( i );

					// ボタンを有効化
					btnShareText.setEnabled(true);
				}
			}
		});

		btnSearch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				// 入力チェック
				checkinput();
				
				// ボタン有効時
				if(btnSearch.isEnabled()) {
					// ボタンを無効化
					btnSearch.setEnabled(false);

					if(bHistory) {
						// 履歴の重複を消去
						for(int i = 0; i < history.size(); i++) {
							if(history.get(i).equals(edtText.getText().toString() + "")) {
								history.remove(i);
								break;
							}
						}
						// 履歴に追加
						history.add("" + edtText.getText().toString());
						if(history.size() > iHistoryNum) {
							history.remove(0);
						}
					}
				
					// 設定をユーザー領域に保存
					prefcommit();

					String strURI = R.string.app_base_url + "kkc.php?q=";
					strURI += edtText.getText().toString();

					// 表示順序
					if(radio1.isChecked()) {
						strURI += "&so=1";
					} else if(radio2.isChecked()) {
						strURI += "&so=2";
					} else {
						strURI += "&so=0";
					}
					// 最低値段
					strURI += "&mnp=" + edtMNP.getText().toString();
					// 最高値段
					strURI += "&mxp=" + edtMXP.getText().toString();
					// 商品状態
					if(radio4.isChecked()) {
						strURI += "&co=1";
					} else {
						strURI += "&co=0";
					}
					// 送料無料指定
					if(radio11.isChecked()) {
						strURI += "&sf=1";
					} else {
						strURI += "&sf=0";
					}
					// 出品終了表示
					if(radio6.isChecked()) {
						strURI += "&et=1";
					} else if(radio7.isChecked()) {
						strURI += "&et=2";
					} else {
						strURI += "&et=0";
					}
					// 商品ID表示
					if(radio9.isChecked()) {
						strURI += "&pi=1";
					} else {
						strURI += "&pi=0";
					}
					strURI += "&sh=0&pn=1";

					// URLを指定してブラウザで開く
					Uri uri = Uri.parse(strURI);
					Intent i = new Intent( Intent.ACTION_VIEW, uri );
					startActivity( i );
				
					// 入力チェック
					checkinput();

					// ボタンを有効化
					btnSearch.setEnabled(true);
				}
			}
		});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO 自動生成されたメソッド・スタブ
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				edtText.setText(data.getStringExtra("SCAN_RESULT"));
			}
		}
	}

	// 入力クリア
	private void clear() {
		// クリアボタンを無効化
		btnClear.setEnabled(false);

		bBarcode = false;
		strBarcode = "";
		btnGetEAN.setEnabled(false);
		btnGetISBN.setEnabled(false);
		btnBackToNum.setEnabled(false);
		btnWebSearch.setEnabled(false);
		btnShareText.setEnabled(false);
		btnSearch.setEnabled(false);
		
		// 履歴の設定で有効／無効を切り替える
		btnHistory.setEnabled(false);
		if(bHistory) {
			if(history.size() > 0) {
				btnHistory.setEnabled(true);
			}
		}

		// クリアボタンを有効化
		btnClear.setEnabled(true);
	}

	// 入力チェック
	private void checkinput() {
		if(edtText.getText().toString().equals("")) {
			// クリア
			clear();
		} else {
			// 数字8桁ならEAN変換を有効にする
			Pattern objPattern = Pattern.compile("^[0-9]{8,}$");
			Matcher objMatcher = objPattern.matcher(edtText.getText().toString());
			btnGetEAN.setEnabled(objMatcher.find());

			// 数字13桁ならISBN変換を有効にする
			objPattern = Pattern.compile("^[0-9]{13,}$");
			objMatcher = objPattern.matcher(edtText.getText().toString());
			btnGetISBN.setEnabled(objMatcher.find());

			// 価格調査を無効にする
			btnSearch.setEnabled(false);
			// Web検索を無効にする
			btnWebSearch.setEnabled(false);
			// 共有を無効にする
			btnShareText.setEnabled(false);

			// 最低値段が入力されている場合
			objPattern = Pattern.compile("^[0-9]+$");
			objMatcher = objPattern.matcher(edtMNP.getText().toString());
			if(objMatcher.find()) {
				// 最高値段が入力されている場合
				objMatcher = objPattern.matcher(edtMXP.getText().toString());
				if(objMatcher.find()) {
					// 価格調査を有効にする
					btnSearch.setEnabled(true);
					// Web検索を有効にする
					btnWebSearch.setEnabled(true);
					// 共有を有効にする
					btnShareText.setEnabled(true);
				}
			}
		}

		// 履歴の設定で有効／無効を切り替える
		btnHistory.setEnabled(false);
		if(bHistory) {
			if(history.size() > 0) {
				btnHistory.setEnabled(true);
			}
		}
	}

	// 設定をユーザー領域に保存
	private void prefcommit() {
		prefedit.putInt("WebEngine", iWebEngine);
		prefedit.putBoolean("History", bHistory);
		if(bHistory) {
			for(int i = 0; i < iHistoryNum; i++) {
				if(i < history.size()) {
					prefedit.putString("History" + String.format("%02d", i), history.get(i));
				} else {
					prefedit.putString("History" + String.format("%02d", i), "");
				}
			}
		} else {
			for(int i = 0; i < iHistoryNum; i++) {
				prefedit.putString("History" + String.format("%02d", i), "");
			}
		}
		prefedit.putBoolean("Option", bOption);
		if(bOption) {
			if(radio1.isChecked()) {
				prefedit.putInt("OptionSort", 1);
			} else if(radio2.isChecked()) {
				prefedit.putInt("OptionSort", 2);
			} else {
				prefedit.putInt("OptionSort", 0);
			}
			if(edtMNP.getText().toString().equals("")) {
				edtMNP.setText("0");
			}
			prefedit.putInt("OptionMNP", Integer.parseInt(edtMNP.getText().toString()));
			if(edtMXP.getText().toString().equals("")) {
				edtMXP.setText("0");
			}
			prefedit.putInt("OptionMXP", Integer.parseInt(edtMXP.getText().toString()));
			if(radio4.isChecked()) {
				prefedit.putInt("OptionCondition", 1);
			} else {
				prefedit.putInt("OptionCondition", 0);
			}
			if(radio11.isChecked()) {
				prefedit.putInt("OptionShipfree", 1);
			} else {
				prefedit.putInt("OptionShipfree", 0);
			}
			if(radio6.isChecked()) {
				prefedit.putInt("OptionEndTime", 1);
			} else if(radio7.isChecked()) {
				prefedit.putInt("OptionEndTime", 2);
			} else {
				prefedit.putInt("OptionEndTime", 0);
			}
			if(radio9.isChecked()) {
				prefedit.putInt("OptionProductID", 1);
			} else {
				prefedit.putInt("OptionProductID", 0);
			}
		} else {
			prefedit.putInt("OptionSort", 0);
			prefedit.putInt("OptionMNP", 0);
			prefedit.putInt("OptionMXP", 0);
			prefedit.putInt("OptionCondition", 0);
			prefedit.putInt("OptionEndTime", 0);
			prefedit.putInt("OptionProductID", 0);
		}

		prefedit.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO 自動生成されたメソッド・スタブ
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		switch(item.getItemId()) {
		case R.id.id_menu_Usage:
			alertDialogBuilder.setTitle(R.string.usage);
			alertDialogBuilder.setMessage(R.string.usage_detail);
			alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
					
				}
			});
			alertDialogBuilder.show();
			return true;
		case R.id.id_menu_HistorySettings:
			int iSetHistory;
			if(bHistory) {
				iSetHistory = 0;
			} else {
				iSetHistory = 1;
			}
			final CharSequence[] items_History = {getResources().getString(R.string.memory), getResources().getString(R.string.nomemory)};
			alertDialogBuilder.setTitle(R.string.setting_history);
			alertDialogBuilder.setSingleChoiceItems(items_History, iSetHistory, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ

				}
			});
			alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
					if(which == 0) {
						bHistory = true;
					} else {
						history.clear();
						bHistory = false;
					}
					// 設定をユーザー領域に保存
					prefcommit();
					// 入力チェック
					checkinput();
				}
			});
			alertDialogBuilder.show();
			return true;
		case R.id.id_menu_SearchOptionSettings:
			int iSetSearchOption;
			if(bOption) {
				iSetSearchOption = 0;
			} else {
				iSetSearchOption = 1;
			}
			final CharSequence[] items_SearchOption = {getResources().getString(R.string.memory), getResources().getString(R.string.nomemory)};
			alertDialogBuilder.setTitle(R.string.seting_web_search);
			alertDialogBuilder.setSingleChoiceItems(items_SearchOption, iSetSearchOption, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
					if(which == 0) {
						bOption = true;
					} else {
						bOption = false;
					}
					// 設定をユーザー領域に保存
					prefcommit();
				}
			});
			alertDialogBuilder.show();
			return true;
		case R.id.id_menu_HistoryClear:
			alertDialogBuilder.setTitle(R.string.clear_history);
			alertDialogBuilder.setMessage(R.string.delete_history);
			alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
					history.clear();
					// 設定をユーザー領域に保存
					prefcommit();
					// 入力チェック
					checkinput();
				}
			});
			alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
					
				}
			});
			alertDialogBuilder.show();
			return true;
		case R.id.id_menu_WebEngine:
			final CharSequence[] items_Web = {"Yahoo! JAPAN", "Google.com", "Yahoo!", "DuckDuckGo", "Bing", "Yandex"};
			alertDialogBuilder.setTitle(R.string.seting_web_search);
			alertDialogBuilder.setSingleChoiceItems(items_Web, iWebEngine, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
					iWebEngine = which;
					// 設定をユーザー領域に保存
					prefcommit();
				}
			});
			alertDialogBuilder.show();
			return true;
		case R.id.id_menu_About:
	        PackageManager packageManager = this.getPackageManager();
	        String strVersion = "";
	        try {
	               PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
	               strVersion = packageInfo.versionName;
	        } catch (NameNotFoundException e) {
	        }
			alertDialogBuilder.setTitle(R.string.about);
			alertDialogBuilder.setMessage(R.string.app_name + " Ver" + strVersion + "\n\n" + R.string.app_base_url + "kkc.php\n\nProgramed by AZO (for A.K)");
			alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
					
				}
			});
			alertDialogBuilder.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
