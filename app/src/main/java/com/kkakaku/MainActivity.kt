package com.kkakaku

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    private var iWebEngine = 0
    private var bHistory = false
    private var bOption = false
    private var bBarcode = false
    private var toast0: Toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
    private var history: ArrayList<String> = ArrayList()

    // ユーザー領域
    private var pref: SharedPreferences = getSharedPreferences("pref", MODE_PRIVATE)
    private var prefedit: SharedPreferences.Editor? = pref.edit()
    private val thread: Thread? = null
    private val iSearchType = 0
    private val strProductName: String? = null
    private var bNetworkFailed = false
    private var bNetworking = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bNetworkFailed = true
        bNetworking = false
        history = ArrayList()

        // プログレスダイアログの設定
    //    waitDialog = ProgressDialog(this)
        // プログレスダイアログのメッセージを設定します
    //    waitDialog!!.setMessage(R.string.serching)
        // 円スタイル（くるくる回るタイプ）に設定します
    //    waitDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        // ユーザー領域を読み出す
        var iOptionSort = 0
        var iOptionMNP = 0
        var iOptionMXP = 0
        var iOptionCondition = 0
        var iOptionShipfree = 0
        var iOptionEndTime = 0
        var iOptionProductID = 0
        iWebEngine = pref.getInt("WebEngine", 0)
        bHistory = pref.getBoolean("History", true)
        if (bHistory) {
            for (i in 0 until iHistoryNum) {
                val strHistory = pref.getString("History" + String.format("%02d", i), "")
                if (strHistory != null && strHistory != "") {
                    history.add(strHistory)
                }
            }
        }
        bOption = pref.getBoolean("Option", true)
        if (bOption) {
            iOptionSort = pref.getInt("OptionSort", 0)
            iOptionMNP = pref.getInt("OptionMNP", 0)
            iOptionMXP = pref.getInt("OptionMXP", 0)
            iOptionCondition = pref.getInt("OptionCondition", 0)
            iOptionShipfree = pref.getInt("OptionShipfree", 0)
            iOptionEndTime = pref.getInt("OptionEndTime", 0)
            iOptionProductID = pref.getInt("OptionProductID", 0)
        }
        val btnClear = findViewById<View>(R.id.id_btnClear) as Button
        val btnHistory = findViewById<View>(R.id.id_btnHistory) as Button
        val btnGetBarcode = findViewById<View>(R.id.id_GetBarcode) as Button
        val btnSearch = findViewById<View>(R.id.id_btnSearch) as Button
        val btnWebSearch = findViewById<View>(R.id.id_btnWebSearch) as Button
        val btnShareText = findViewById<View>(R.id.id_btnShareText) as Button
        val edtText = findViewById<View>(R.id.id_Text) as EditText
        val edtMNP = findViewById<View>(R.id.id_edtMNP) as EditText
        val edtMXP = findViewById<View>(R.id.id_edtMXP) as EditText
        val radio0 = findViewById<View>(R.id.radio0) as RadioButton
        val radio1 = findViewById<View>(R.id.radio1) as RadioButton
        val radio2 = findViewById<View>(R.id.radio2) as RadioButton
        val radio3 = findViewById<View>(R.id.radio3) as RadioButton
        val radio4 = findViewById<View>(R.id.radio4) as RadioButton
//		val radio5 = (RadioButton)findViewById(R.id.radio5);
//		val radio6 = (RadioButton)findViewById(R.id.radio6);
//		val radio7 = (RadioButton)findViewById(R.id.radio7);
        val radio8 = findViewById<View>(R.id.radio8) as RadioButton
        val radio9 = findViewById<View>(R.id.radio9) as RadioButton
        val radio10 = findViewById<View>(R.id.radio10) as RadioButton
        val radio11 = findViewById<View>(R.id.radio11) as RadioButton
        when (iOptionSort) {
            1 -> radio1.isChecked = true
            2 -> radio2.isChecked = true
            else -> radio0.isChecked = true
        }
        edtMNP.setText(iOptionMNP.toString())
        edtMXP.setText(iOptionMXP.toString())
        if (iOptionCondition == 1) {
            radio4.isChecked = true
        } else {
            radio3.isChecked = true
        }
        if (iOptionShipfree == 1) {
            radio11.isChecked = true
        } else {
            radio10.isChecked = true
        }
//        when (iOptionEndTime) {
//            1 -> radio6.isChecked = true
//            2 -> radio7.isChecked = true
//            else -> radio5.isChecked = true
//        }
        if (iOptionProductID == 1) {
            radio9.isChecked = true
        } else {
            radio8.isChecked = true
        }

        val HistoryDialog = AlertDialog.Builder(this)
        prefcommit()
        edtText.setText("")
        clear()

        btnClear.setOnClickListener {
            edtText.setText("")
            clear()
        }

        edtText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                checkinput()
            }
        })
        edtText.setOnKeyListener { v, keyCode, event ->
            event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER
        }

        edtMNP.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                checkinput()
            }
        })

        edtMXP.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                checkinput()
            }
        })

        btnGetBarcode.setOnClickListener {
            btnGetBarcode.isEnabled = false
            val intentScan = Intent("com.google.zxing.client.android.SCAN")
            intentScan.putExtra("SCAN_MODE", "PRODUCT_MODE")
            try {
                startActivityForResult(intentScan, 1)
            } catch (e: ActivityNotFoundException) {
                toast0.setText("バーコードの読み出しに失敗しました。")
                toast0.show()
            }
            btnGetBarcode.isEnabled = true
        }

        btnHistory.setOnClickListener {
            HistoryDialog.setTitle("検索履歴")
            // 履歴を逆順に並べ替え
            val backhistory = ArrayList<String?>()
            for (i in history.indices.reversed()) {
                backhistory.add(history[i])
            }
            val cs = backhistory.toTypedArray<CharSequence?>()
            HistoryDialog.setSingleChoiceItems(cs, -1) { dialog, which ->
                val bhwork = ArrayList<String?>()
                for (i in history.indices.reversed()) {
                    bhwork.add(history[i])
                }
                val cswork = bhwork.toTypedArray<CharSequence?>()
                edtText.setText(cswork[which])
                bBarcode = false
            }
            HistoryDialog.show()
        }

        btnWebSearch.setOnClickListener {
            // 入力チェック
            checkinput()

            // ボタン有効時
            if (btnWebSearch.isEnabled) {
                // ボタンを無効化
                btnWebSearch.isEnabled = false
                val strURI: String
                strURI = when (iWebEngine) {
                    1 -> "https://google.com/search?q=" + edtText.text.toString() + "&ei=UTF-8&safe=off"
                    2 -> "https://search.yahoo.com/search?q=" + edtText.text.toString() + "&ei=UTF-8&safe=off"
                    3 -> "https://www.bing.com/search?q=" + edtText.text.toString()
                    4 -> "https://duckduckgo.com/?q=" + edtText.text.toString()
                    5 -> "https://yandex.com/search/?q=" + edtText.text.toString()
                    else -> "http://search.yahoo.co.jp/search?p=" + edtText.text.toString() + "&ei=UTF-8&save=0"
                }

                // URLを指定してブラウザを開く
                val uri = Uri.parse(strURI)
                val i = Intent(Intent.ACTION_VIEW, uri)
                startActivity(i)

                // ボタンを有効化
                btnWebSearch.isEnabled = true
            }
        }

        btnShareText.setOnClickListener {
            // 入力チェック
            checkinput()

            // ボタン有効時
            if (btnShareText.isEnabled) {
                // ボタンを無効化
                btnShareText.isEnabled = false

                // 検索ワードを共有
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_TEXT, edtText.text.toString() + " (by " + R.string.app_base_url + "kkc.php #kkakaku)")
                startActivity(i)

                // ボタンを有効化
                btnShareText.isEnabled = true
            }
        }

        btnSearch.setOnClickListener {
            // 入力チェック
            checkinput()

            // ボタン有効時
            if (btnSearch.isEnabled) {
                // ボタンを無効化
                btnSearch.isEnabled = false
                if (bHistory) {
                    // 履歴の重複を消去
                    for (i in history.indices) {
                        if (history[i] == edtText.text.toString() + "") {
                            history.removeAt(i)
                            break
                        }
                    }
                    // 履歴に追加
                    history.add("" + edtText.text.toString())
                    if (history.size > iHistoryNum) {
                        history.removeAt(0)
                    }
                }

                // 設定をユーザー領域に保存
                prefcommit()
                var strURI = R.string.app_base_url.toString() + "kkc.php?q="
                strURI += edtText.text.toString()

                // 表示順序
                strURI += if (radio1.isChecked) {
                    "&so=1"
                } else if (radio2.isChecked) {
                    "&so=2"
                } else {
                    "&so=0"
                }
                // 最低値段
                strURI += "&mnp=" + edtMNP.text.toString()
                // 最高値段
                strURI += "&mxp=" + edtMXP.text.toString()
                // 商品状態
                strURI += if (radio4.isChecked) {
                    "&co=1"
                } else {
                    "&co=0"
                }
                // 送料無料指定
                strURI += if (radio11.isChecked) {
                    "&sf=1"
                } else {
                    "&sf=0"
                }
                // 出品終了表示
//                strURI += if (radio6!!.isChecked) {
//                    "&et=1"
//                } else if (radio7!!.isChecked) {
//                    "&et=2"
//                } else {
//                    "&et=0"
//                }
                strURI += "&et=0"
                // 商品ID表示
                strURI += if (radio9.isChecked) {
                    "&pi=1"
                } else {
                    "&pi=0"
                }
                strURI += "&sh=0&pn=1"

                // URLを指定してブラウザで開く
                val uri = Uri.parse(strURI)
                val i = Intent(Intent.ACTION_VIEW, uri)
                startActivity(i)

                // 入力チェック
                checkinput()

                // ボタンを有効化
                btnSearch.isEnabled = true
            }
        }
    }

    fun clear() {
        val btnClear = findViewById<View>(R.id.id_btnClear) as Button
        val btnHistory = findViewById<View>(R.id.id_btnHistory) as Button
//        val btnGetBarcode = findViewById<View>(R.id.id_GetBarcode) as Button
        val btnSearch = findViewById<View>(R.id.id_btnSearch) as Button
        val btnWebSearch = findViewById<View>(R.id.id_btnWebSearch) as Button
        val btnShareText = findViewById<View>(R.id.id_btnShareText) as Button

        // クリアボタンを無効化
        btnClear.isEnabled = false

        bBarcode = false
//            strBarcode = ""
//            btnGetEAN.isEnabled = false
//            btnGetISBN.isEnabled = false
//            btnBackToNum.isEnabled = false
        btnWebSearch.isEnabled = false
        btnShareText.isEnabled = false
        btnSearch.isEnabled = false

        // 履歴の設定で有効／無効を切り替える
        btnHistory.isEnabled = false
        if (bHistory) {
            if (history.size > 0) {
                btnHistory.isEnabled = true
            }
        }

        // クリアボタンを有効化
        btnClear.isEnabled = true
    }

    fun checkinput() {
        val edtText = findViewById<View>(R.id.id_Text) as EditText
        val edtMNP = findViewById<View>(R.id.id_edtMNP) as EditText
        val edtMXP = findViewById<View>(R.id.id_edtMXP) as EditText
        val btnHistory = findViewById<View>(R.id.id_btnHistory) as Button
//        val btnGetBarcode = findViewById<View>(R.id.id_GetBarcode) as Button
        val btnSearch = findViewById<View>(R.id.id_btnSearch) as Button
        val btnWebSearch = findViewById<View>(R.id.id_btnWebSearch) as Button
        val btnShareText = findViewById<View>(R.id.id_btnShareText) as Button

        if (edtText.text.toString() == "") {
            // クリア
            clear()
        } else {
            // 数字8桁ならEAN変換を有効にする
//                var objPattern = Pattern.compile("^[0-9]{8,}$")
//                var objMatcher = objPattern.matcher(edtText.text.toString())
//                btnGetEAN.isEnabled = objMatcher.find()

            // 数字13桁ならISBN変換を有効にする
//                objPattern = Pattern.compile("^[0-9]{13,}$")
//                objMatcher = objPattern.matcher(edtText.text.toString())
//                btnGetISBN.isEnabled = objMatcher.find()

            // 価格調査を無効にする
            btnSearch.isEnabled = false
            // Web検索を無効にする
            btnWebSearch.isEnabled = false
            // 共有を無効にする
            btnShareText.isEnabled = false

            // 最低値段が入力されている場合
            val objPattern = Pattern.compile("^[0-9]+$")
            var objMatcher = objPattern.matcher(edtMNP.text.toString())
            if (objMatcher.find()) {
                // 最高値段が入力されている場合
                objMatcher = objPattern.matcher(edtMXP.text.toString())
                if (objMatcher.find()) {
                    // 価格調査を有効にする
                    btnSearch.isEnabled = true
                    // Web検索を有効にする
                    btnWebSearch.isEnabled = true
                    // 共有を有効にする
                    btnShareText.isEnabled = true
                }
            }

            // 履歴の設定で有効／無効を切り替える
            btnHistory.isEnabled = false
            if (bHistory) {
                if (history.size > 0) {
                    btnHistory.isEnabled = true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val edtText = findViewById<View>(R.id.id_Text) as EditText
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                edtText.setText(data.getStringExtra("SCAN_RESULT"))
            }
        }
    }

    // 設定をユーザー領域に保存
    private fun prefcommit() {
        val edtText = findViewById<View>(R.id.id_Text) as EditText
        val edtMNP = findViewById<View>(R.id.id_edtMNP) as EditText
        val edtMXP = findViewById<View>(R.id.id_edtMXP) as EditText
        val radio0 = findViewById<View>(R.id.radio0) as RadioButton
        val radio1 = findViewById<View>(R.id.radio1) as RadioButton
        val radio2 = findViewById<View>(R.id.radio2) as RadioButton
        val radio3 = findViewById<View>(R.id.radio3) as RadioButton
        val radio4 = findViewById<View>(R.id.radio4) as RadioButton
//		val radio5 = (RadioButton)findViewById(R.id.radio5);
//		val radio6 = (RadioButton)findViewById(R.id.radio6);
//		val radio7 = (RadioButton)findViewById(R.id.radio7);
        val radio8 = findViewById<View>(R.id.radio8) as RadioButton
        val radio9 = findViewById<View>(R.id.radio9) as RadioButton
        val radio10 = findViewById<View>(R.id.radio10) as RadioButton
        val radio11 = findViewById<View>(R.id.radio11) as RadioButton

        prefedit!!.putInt("WebEngine", iWebEngine)
        prefedit!!.putBoolean("History", bHistory)
        if (bHistory) {
            for (i in 0 until iHistoryNum) {
                if (i < history.size) {
                    prefedit!!.putString("History" + String.format("%02d", i), history[i])
                } else {
                    prefedit!!.putString("History" + String.format("%02d", i), "")
                }
            }
        } else {
            for (i in 0 until iHistoryNum) {
                prefedit!!.putString("History" + String.format("%02d", i), "")
            }
        }
        prefedit!!.putBoolean("Option", bOption)
        if (bOption) {
            if (radio1.isChecked) {
                prefedit!!.putInt("OptionSort", 1)
            } else if (radio2.isChecked) {
                prefedit!!.putInt("OptionSort", 2)
            } else {
                prefedit!!.putInt("OptionSort", 0)
            }
            if (edtMNP.text.toString() == "") {
                edtMNP.setText("0")
            }
            prefedit!!.putInt("OptionMNP", edtMNP!!.text.toString().toInt())
            if (edtMXP.text.toString() == "") {
                edtMXP.setText("0")
            }
            prefedit!!.putInt("OptionMXP", edtMXP!!.text.toString().toInt())
            if (radio4.isChecked) {
                prefedit!!.putInt("OptionCondition", 1)
            } else {
                prefedit!!.putInt("OptionCondition", 0)
            }
            if (radio11.isChecked) {
                prefedit!!.putInt("OptionShipfree", 1)
            } else {
                prefedit!!.putInt("OptionShipfree", 0)
            }
//            if (radio6.isChecked) {
//                prefedit.putInt("OptionEndTime", 1)
//            } else if (radio7.isChecked) {
//                prefedit.putInt("OptionEndTime", 2)
//            } else {
//                prefedit.putInt("OptionEndTime", 0)
//            }
            prefedit!!.putInt("OptionEndTime", 0)
            if (radio9.isChecked) {
                prefedit!!.putInt("OptionProductID", 1)
            } else {
                prefedit!!.putInt("OptionProductID", 0)
            }
        } else {
            prefedit!!.putInt("OptionSort", 0)
            prefedit!!.putInt("OptionMNP", 0)
            prefedit!!.putInt("OptionMXP", 0)
            prefedit!!.putInt("OptionCondition", 0)
            prefedit!!.putInt("OptionEndTime", 0)
            prefedit!!.putInt("OptionProductID", 0)
        }
        prefedit!!.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // TODO 自動生成されたメソッド・スタブ
        val alertDialogBuilder = AlertDialog.Builder(this)
        return when (item.itemId) {
            R.id.id_menu_Usage -> {
                alertDialogBuilder.setTitle(R.string.usage)
                alertDialogBuilder.setMessage(R.string.usage_detail)
                alertDialogBuilder.setPositiveButton(R.string.ok) { dialog, which ->
                    // TODO 自動生成されたメソッド・スタブ
                }
                alertDialogBuilder.show()
                true
            }

            R.id.id_menu_HistorySettings -> {
                val iSetHistory: Int
                iSetHistory = if (bHistory) {
                    0
                } else {
                    1
                }
                val items_History = arrayOf<CharSequence>(resources.getString(R.string.memory), resources.getString(R.string.nomemory))
                alertDialogBuilder.setTitle(R.string.setting_history)
                alertDialogBuilder.setSingleChoiceItems(items_History, iSetHistory) { dialog, which ->
                    // TODO 自動生成されたメソッド・スタブ
                }
                alertDialogBuilder.setPositiveButton(R.string.ok) { dialog, which -> // TODO 自動生成されたメソッド・スタブ
                    bHistory = if (which == 0) {
                        true
                    } else {
                        history.clear()
                        false
                    }
                    // 設定をユーザー領域に保存
                    prefcommit()
                    // 入力チェック
                    checkinput()
                }
                alertDialogBuilder.show()
                true
            }

            R.id.id_menu_SearchOptionSettings -> {
                val iSetSearchOption: Int
                iSetSearchOption = if (bOption) {
                    0
                } else {
                    1
                }
                val items_SearchOption = arrayOf<CharSequence>(resources.getString(R.string.memory), resources.getString(R.string.nomemory))
                alertDialogBuilder.setTitle(R.string.seting_web_search)
                alertDialogBuilder.setSingleChoiceItems(items_SearchOption, iSetSearchOption) { dialog, which -> // TODO 自動生成されたメソッド・スタブ
                    bOption = if (which == 0) {
                        true
                    } else {
                        false
                    }
                    // 設定をユーザー領域に保存
                    prefcommit()
                }
                alertDialogBuilder.show()
                true
            }

            R.id.id_menu_HistoryClear -> {
                alertDialogBuilder.setTitle(R.string.clear_history)
                alertDialogBuilder.setMessage(R.string.delete_history)
                alertDialogBuilder.setPositiveButton(R.string.yes) { dialog, which -> // TODO 自動生成されたメソッド・スタブ
                    history.clear()
                    // 設定をユーザー領域に保存
                    prefcommit()
                    // 入力チェック
                    checkinput()
                }
                alertDialogBuilder.setNegativeButton(R.string.no) { dialog, which ->
                    // TODO 自動生成されたメソッド・スタブ
                }
                alertDialogBuilder.show()
                true
            }

            R.id.id_menu_WebEngine -> {
                val items_Web = arrayOf<CharSequence>("Yahoo! JAPAN", "Google.com", "Yahoo!", "DuckDuckGo", "Bing", "Yandex")
                alertDialogBuilder.setTitle(R.string.seting_web_search)
                alertDialogBuilder.setSingleChoiceItems(items_Web, iWebEngine) { dialog, which -> // TODO 自動生成されたメソッド・スタブ
                    iWebEngine = which
                    // 設定をユーザー領域に保存
                    prefcommit()
                }
                alertDialogBuilder.show()
                true
            }

            R.id.id_menu_About -> {
                val packageManager = this.packageManager
                var strVersion = ""
                try {
                    val packageInfo = packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
                    strVersion = packageInfo.versionName
                } catch (e: PackageManager.NameNotFoundException) {
                }
                alertDialogBuilder.setTitle(R.string.about)
                alertDialogBuilder.setMessage("""${R.string.app_name} Ver$strVersion

${R.string.app_base_url}kkc.php

Programed by AZO (for A.K)""")
                alertDialogBuilder.setPositiveButton(R.string.ok) { dialog, which ->
                    // TODO 自動生成されたメソッド・スタブ
                }
                alertDialogBuilder.show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val iHistoryNum = 10
        private var waitDialog: ProgressDialog? = null
        private const val iNetworkTimeout = 30000
        private const val msgidSearched = 0
        private const val msgidTimeout = 1
    }
}