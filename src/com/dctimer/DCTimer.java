package com.dctimer;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.sina.weibo.sdk.auth.*;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.*;
import sina.weibo.*;
import solvers.*;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.*;
import android.hardware.*;
import android.net.Uri;
import android.os.*;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.TabSpec;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DCTimer extends Activity implements SensorEventListener {
	private Context context;
	private TabHost tabHost;
	private Button buttonSst;	// ����״̬
	public TextView tvTimer;	//��ʱ��
	private static TextView tvScr;	// ��ʾ����
	private Spinner[] spinner = new Spinner[7];	//�����б�
	static byte[] spSel = new byte[7];
	private ArrayAdapter<String> adapter;
	private ImageView iv;
	private GridView myGridView = null, gvTitle = null;
	private Button seMean, sesOpt;	//����ƽ��, ����ѡ��
	private Button reset, wbAuth;	//���ø�λ, ΢����¼/ע��
	private SeekBar[] skb = new SeekBar[7];	//�϶���
	private TextView[] stt = new TextView[56];	//����
	private int sttlen = stt.length;
	private TextView[] stSwitch = new TextView[13];
	private TextView[] std = new TextView[13];
	private LinearLayout[] llay = new LinearLayout[22];
	private TextView tvl;
	private CheckBox[] chkb = new CheckBox[13];

	private Timer timer;
	private Stackmat stm;
	private ColorPicker dialog;
	private DBHelper dbh;
	private TimesAdapter aryAdapter;
	private WeiboAuth mWeiboAuth;
	private Oauth2AccessToken mAccessToken;
	private SsoHandler mSsoHandler;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static SharedPreferences share;
	protected static SharedPreferences.Editor edit;
	private Cursor cursor;
	private Bitmap bitmap;
	private PowerManager.WakeLock wakeLock = null;
	private DisplayMetrics dm;
	private ProgressDialog proDlg = null;
	private ProgressDialog dlProg = null;
	private Vibrator vibrator;
	private SensorManager sensor;

	private boolean opnl, opnd, hidscr, conft, isMulp, canScr = true, simss, touchDown = false, isLogin, isShare,
			scrt = false, bgcolor, fulls, invs, usess, screenOn, selSes, isLongPress, isChScr, drop;
	private static boolean isNextScr = false;
	protected boolean canStart;
	protected static boolean isInScr = false;
	public boolean wca;
	public static boolean hidls, clkform, l1am, l2am;
	static boolean idnf = true;
	public static byte[] resp;	// �ͷ�
	public static byte[] listnum = {3, 5, 12, 50, 100};
	private static char[] srate = {48000, 44100, 22050, 16000, 11025, 8000};
	private float lowZ;
	private int dbLastId, ttsize, stsize, intv, insType, mulpCount, egoll, sensity;
	private int verc = 16;
	private int[] staid = {R.array.tiwStr, R.array.tupdStr, R.array.preStr, R.array.mulpStr, R.array.samprate, R.array.crsStr,
			R.array.c2lStr, R.array.mncStr, R.array.fontStr, R.array.soriStr, R.array.vibraStr, R.array.vibTimeStr, R.array.sq1sStr};
	private int[] screenOri = new int[] {2, 0, 8, 1, 4};
	private static int selold, scrType, inScrLen, bytesum;
	protected int frzTime;
	protected static int[][] mulp = null;
	public int[] cl = new int[5];
	public static int resl;
	public static int[] rest, stSel = new int[13];
	static int isp2 = 0, egtype;
	private long exitTime = 0;
	private long[] vibTime = new long[] {30, 50, 80, 150, 240};
	private static long[] multemp = null;
	public static short[] sestp = new short[15];

	private String picPath, selFilePath, newver, newupd;
	private String defPath = Environment.getExternalStorageDirectory().getPath()+"/DCTimer/";
	private String[] times = null, mItems;
	private String[][] itemStr = new String[13][];
	private static String nextScr = null, extsol, slist, outPath;
	private static String[] sesname = new String[15];
	public static String crntScr;	// ��ǰ����
	public static String[] scrst;	// �����б�
	static String egolls;
	//private static String addstr = "/data/data/com.dctimer/databases/main.png";
	public static final String APP_KEY = "3318942954";	// �滻Ϊ�����ߵ�appkey������"1646212960";
	private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
	public static final String SCOPE = 
            "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
	private static ArrayList<String> inScr = null;
	private List<String> items = null, paths = null;
	private ListView listView;

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			int msw = msg.what;
			switch(msw) {	//TODO
			case 0: tvScr.setText(crntScr);	break;
			case 1: tvScr.setText(crntScr + "\n\n" + getString(R.string.shape) + extsol);	break;
			case 2: tvScr.setText(getString(R.string.scrambling));	break;
			case 3: tvScr.setText(crntScr + extsol);	break;
			case 4: Toast.makeText(DCTimer.this, getString(R.string.outscr_failed), Toast.LENGTH_SHORT).show();	break;
			case 5: tvTimer.setText("IMPORT");	break;
			case 6: tvScr.setText(crntScr + "\n\n" + getString(R.string.solving));	break;
			case 7: Toast.makeText(DCTimer.this, getString(R.string.outscr_success), Toast.LENGTH_SHORT).show();	break;
			case 8: Toast.makeText(DCTimer.this, getString(R.string.conning), Toast.LENGTH_SHORT).show();	break;
			case 9: Toast.makeText(DCTimer.this, getString(R.string.net_error), Toast.LENGTH_LONG).show();	break;
			case 10: Toast.makeText(DCTimer.this, getString(R.string.lastest), Toast.LENGTH_LONG).show();	break;
			case 11:
				new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.havenew)+newver)
				.setMessage(newupd)
				.setPositiveButton(getString(R.string.btn_download), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						download("DCTimer"+newver+".apk");
					}
				})
				.setNegativeButton(getString(R.string.btn_cancel), null).show();
			case 12: dlProg.setProgress(bytesum / 1024);	break;
			case 13: Toast.makeText(DCTimer.this, getString(R.string.file_error), Toast.LENGTH_LONG).show();
			case 14: seMean.setText(getString(R.string.session_average) + Mi.sesMean());
				setGridView(true);	break;
			default: proDlg.setProgress(msw%100);	break;//Message(msw%100 + "/" + msw/100);
			}
		}
	};

	class TitleAdapter extends BaseAdapter {
		private Context context;
		private String[] times;
		private TextView tv;
		private int cl;
		public TitleAdapter(Context context, String[] times, int cl) {
			this.context = context;
			this.times = times;
			this.cl = cl;
		}
		public int getCount() {
			if(times != null) return times.length;
			return 0;
		}
		public Object getItem(int position) {return position;}
		public long getItemId(int position) {return position;}
		public View getView(int po, View convertView, ViewGroup parent) {
			if (convertView == null) {
				tv = new TextView(context);
				tv.setLayoutParams(new GridView.LayoutParams(-1, -2));
			}
			else tv = (TextView) convertView;
			tv.setTextSize(16);
			tv.setGravity(Gravity.CENTER);
			tv.setTextColor(cl);
			tv.setText(times[po]);
			return tv;
		}
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(!bgcolor) {
			try {
				dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				bitmap = BitmapFactory.decodeFile(picPath);
				bitmap = getBgPic(bitmap);
				setBgPic(bitmap, share.getInt("opac", 35));
			} catch (Exception e) { }
		}
//		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.setContentView(R.layout.tab);
		context = this;
		share = super.getSharedPreferences("dctimer", Activity.MODE_PRIVATE);
		selold = spSel[0] = (byte) share.getInt("sel", 1);	//��������
		cl[0] = share.getInt("cl0", 0xff66ccff);	// ������ɫ
		cl[1] = share.getInt("cl1", Color.BLACK);	// ������ɫ
		cl[2] = share.getInt("cl2", 0xffff00ff);	//��쵥����ɫ
		cl[3] = share.getInt("cl3", Color.RED);	//����������ɫ
		cl[4] = share.getInt("cl4", 0xff009900);	//���ƽ����ɫ
		wca = share.getBoolean("wca", false);	//WCA�۲�
		hidscr = share.getBoolean("hidscr", true);	//���ش���
		hidls = share.getBoolean("hidls", false);	//�ɼ��б����ش���
		conft = share.getBoolean("conft", true);	//��ʾȷ�ϳɼ�
		l1am = share.getBoolean("l1am", true);
		l2am = share.getBoolean("l2am", true);
		spSel[1] = (byte) share.getInt("cface", 0);	// ʮ��������
		spSel[2] = (byte) share.getInt("list2", 1);
		spSel[3] = (byte) share.getInt("cside", 1);	// ���������ɫ
		spSel[4] = (byte) share.getInt("list1", 1);
		spSel[5] = (byte) share.getInt("group", 0);	// ����
		spSel[6] = (byte) share.getInt("sel2", 0);	// ��������
		ttsize = share.getInt("ttsize", 60);	//��ʱ������
		stsize = share.getInt("stsize", 18);	//��������
		clkform = share.getBoolean("timmh", true);	//ʱ���ʽ
		stSel[0] = share.getInt("tiway", 0);	// ��ʱ��ʽ
		stSel[1] = share.getInt("timerupd", 0);	// ��ʱ������
		stSel[2] = share.getBoolean("prec", true) ? 1 : 0;	// ��ʱ����
		stSel[3] = share.getInt("multp", 0);	//�ֶμ�ʱ
		stSel[4] = share.getInt("srate", 1);	// ����Ƶ��
		Stackmat.samplingRate = srate[stSel[4]];
		stSel[5] = share.getInt("cxe", 0);	//�������
		stSel[6] = share.getInt("cube2l", 0);	// ���׵ײ����
		stSel[7] = share.getInt("minxc", 1);	//��ħ��ɫ
		stSel[8] = share.getInt("tfont", 3);	// ��ʱ������
		stSel[9] = share.getInt("screenori", 0);	// ��Ļ����
		stSel[10] = share.getInt("vibra", 0);	// �𶯷���
		stSel[11] = share.getInt("vibtime", 2);	// ��ʱ��
		stSel[12] = share.getInt("sq1s", 0);	//SQ1���μ���
		bgcolor = share.getBoolean("bgcolor", true);
		fulls = share.getBoolean("fulls", false);	// ȫ����ʾ
		usess = share.getBoolean("usess", false);	// ss��ʱ��
		Stackmat.inv = invs = share.getBoolean("invs", false);	// ��ת�ź�
		opnl = share.getBoolean("scron", false);	// ��Ļ����
		opnd = share.getBoolean("scrgry", true);
		selSes = share.getBoolean("selses", false);	//�Զ�ѡ�����
		picPath = share.getString("picpath", "");
		frzTime = share.getInt("tapt", 0);	//������ʱ
		isMulp = stSel[3] != 0;
		intv = share.getInt("intv", 30);	//�ɼ��б��о�
		drop = share.getBoolean("drop", false);
		outPath = share.getString("scrpath", defPath);
		edit = share.edit();
		for(int i=0; i<15; i++) {
			sestp[i] = (short) share.getInt("sestp" + i, -1);
			sesname[i] = share.getString("sesname" + i, "");
		}
		long sestype = share.getLong("sestype", -1);
		if(sestype != -1) {
			for(int i=0; i<9; i++) {
				int temp = Mi.getSessionType(sestype, i);
				if(temp != 0x7f)
					edit.putInt("sestp" + i, temp);
			}
			edit.remove("sestype");
			edit.commit();
		}
		egtype = share.getInt("egtype", 7);
		egoll = share.getInt("egoll", 254);
		simss = share.getBoolean("simss", false);
		setEgOll();

		if(fulls) getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if(opnl) {acquireWakeLock(); screenOn = true;}
		mItems = getResources().getStringArray(R.array.tabInd);
		tabHost = (TabHost) super.findViewById(R.id.tabhost);	//ȡ��TabHost����
		tabHost.setup();	//����TabHost����
		int[] ids = {R.id.tab_timer, R.id.tab_list, R.id.tab_setting};
		for (int x=0; x<3; x++) {	//ѭ��ȡ�����в��ֱ��
			TabSpec myTab = tabHost.newTabSpec("tab" + x);	//����TabSpec
			if(x == 0) myTab.setIndicator(mItems[x], getResources().getDrawable(R.drawable.img1));
			else if(x == 1) myTab.setIndicator(mItems[x], getResources().getDrawable(R.drawable.img2));
			else myTab.setIndicator(mItems[x], getResources().getDrawable(R.drawable.img3));
			myTab.setContent(ids[x]);	//������ʾ�����
			tabHost.addTab(myTab);	//���ӱ�ǩ
		}

		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		if(bgcolor) tabHost.setBackgroundColor(cl[0]);
		else {
			try {
				bitmap = BitmapFactory.decodeFile(picPath);
				bitmap = getBgPic(bitmap);
				setBgPic(bitmap, share.getInt("opac", 35));
			} catch (Exception e) {
				tabHost.setBackgroundColor(cl[0]);
				Toast.makeText(DCTimer.this, getString(R.string.not_exist), Toast.LENGTH_SHORT).show();
			}
		}
		tabHost.setCurrentTab(0);	// ���ÿ�ʼ����

		tvScr = (TextView) findViewById(R.id.myTextView1);
		tvTimer = (TextView) findViewById(R.id.myTextView2);
		buttonSst = (Button) findViewById(R.id.myButtonSst);
		ids = new int[] {R.id.std01, R.id.std02, R.id.std03, R.id.std04, R.id.std05, R.id.std06, R.id.std07,
				R.id.std08, R.id.std09, R.id.std10, R.id.std11, R.id.std12, R.id.std13};
		for(int i=0; i<13; i++) {
			itemStr[i] = getResources().getStringArray(staid[i]);
			std[i] = (TextView) findViewById(ids[i]);
			std[i].setText(itemStr[i][stSel[i]]);
			std[i].setTextColor(0x80000000|(cl[1]&0xffffff));
		}
		ids = new int[] {R.id.mySpinner, R.id.spinner2, R.id.spinner5, R.id.spinner3, R.id.spinner4, R.id.spinner6};
		for(int i=0; i<6; i++) {
			switch(i) {
			case 0: mItems = getResources().getStringArray(R.array.cubeStr); break;
			case 1: mItems = getResources().getStringArray(R.array.faceStr); break;
			case 2: mItems = getResources().getStringArray(R.array.list2Str); break;
			case 3: mItems = getResources().getStringArray(R.array.sideStr); break;
			case 4: mItems = getResources().getStringArray(R.array.list1Str); break;
			case 5:
				mItems = new String[15];
				for (int j = 0; j < 15; j++)
					mItems[j] = (j + 1) + (sesname[j].equals("") ? "" : " - "+sesname[j]);
				break;
			}
			spinner[i] = (Spinner) findViewById(ids[i]);
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mItems);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner[i].setAdapter(adapter);
			//adapter.notifyDataSetChanged();
			spinner[i].setSelection(spSel[i]);
		}
		if(stSel[5] == 0) {spinner[1].setEnabled(false); spinner[3].setEnabled(false);}
		spinner[6] = (Spinner) findViewById(R.id.sndscr);
		set2ndsel();
		tvScr.setTextSize(stsize);
		tvTimer.setTextSize(ttsize);
		setTimerFont(stSel[8]);
		if(stSel[9] > 0) this.setRequestedOrientation(screenOri[stSel[9]]);

		stm = new Stackmat(this);
		if(usess) {
			tvTimer.setText("OFF");
			if(stm.creatAudioRecord((int)srate[stSel[4]]));
			else {
				edit.putInt("srate", 1);
				edit.commit();
			}
		} else {
			if(stSel[0] == 0) {
				if(stSel[2] == 0) tvTimer.setText("0.00");
				else tvTimer.setText("0.000");
			}
			else if(stSel[0] == 1) tvTimer.setText("IMPORT");
		}

		timer = new Timer(this);
		dbh = new DBHelper(this);

		myGridView = (GridView) findViewById(R.id.myGridView);
		gvTitle = (GridView) findViewById(R.id.gv_title);
		seMean = (Button) findViewById(R.id.mButtonoa);
		sesOpt = (Button) findViewById(R.id.mButtonOpt);
		//rsauth = (Button) findViewById(R.id.auth_sina);
		ids = new int[] {R.id.checkeg2, R.id.checkcll, R.id.lcheck2, R.id.lcheck1, R.id.checkeg1, R.id.checkegn,
				R.id.checkegs, R.id.checkega, R.id.checkegpi, R.id.checkegl, R.id.checkegt, R.id.checkegu, R.id.checkegh};
		for(int i=0; i<13; i++) chkb[i] = (CheckBox) findViewById(ids[i]);
		ids = new int[] {R.id.seekb1, R.id.seekb2, R.id.seekb3, R.id.seekb4, R.id.seekb5, R.id.seekb6, R.id.seekb7};
		for(int i=0; i<ids.length; i++) skb[i] = (SeekBar) findViewById(ids[i]);
		tvl = (TextView) findViewById(R.id.tv4);
		ids = new int[] {
				R.id.stt00, R.id.stt01, R.id.stt02, R.id.stt08, R.id.stt09, R.id.stt05, R.id.stt21, R.id.stt07,
				R.id.stt03, R.id.stt22, R.id.stt17, R.id.stt11, R.id.stt12, R.id.stt13, R.id.stt14, R.id.stt15,
				R.id.stt16, R.id.stt10, R.id.stt18, R.id.stt19, R.id.stt23, R.id.stt04, R.id.stt06, R.id.stt20,
				R.id.stt26, R.id.stt27, R.id.stt28, R.id.stt29, R.id.stt30, R.id.stt31, R.id.stt33, R.id.stt33,
				R.id.stt34, R.id.stt35, R.id.stt36, R.id.stt37, R.id.stt38, R.id.stt39, R.id.stt40, R.id.stt41,
				R.id.stt42, R.id.stt43, R.id.stt44, R.id.stt45, R.id.stt46, R.id.stt47, R.id.stt48, R.id.stt49,
				R.id.stt50, R.id.stt51, R.id.stt52, R.id.stt53, R.id.stt54, R.id.stt55, R.id.stt24, R.id.stt25,
		};	//DOTO
		for(int i=0; i<sttlen; i++) stt[i] = (TextView) findViewById(ids[i]);
		
		ids = new int[] {R.id.stcheck1, R.id.stcheck2, R.id.stcheck3, R.id.stcheck4, R.id.stcheck5, R.id.stcheck6,
				R.id.stcheck7, R.id.stcheck8, R.id.stcheck9, R.id.stcheck11, R.id.stcheck12, R.id.stcheck13, R.id.stcheck10};
		for(int i=0; i<ids.length; i++) stSwitch[i] = (TextView) findViewById(ids[i]);
		ids = new int[] {
				R.id.lay01, R.id.lay02, R.id.lay03, R.id.lay04, R.id.lay05, R.id.lay06,
				R.id.lay07, R.id.lay08, R.id.lay09, R.id.lay10, R.id.lay11, R.id.lay12,
				R.id.lay23, R.id.lay14, R.id.lay15, R.id.lay16, R.id.lay17, R.id.lay22,
				R.id.lay19, R.id.lay20, R.id.lay21, R.id.lay13};//TODO
		for(int i=0; i<22; i++) llay[i] = (LinearLayout) findViewById(ids[i]);
		reset = (Button) findViewById(R.id.reset);
		for(int i=0; i<13; i++) llay[i].setOnTouchListener(comboListener);
		for(int i=13; i<22; i++) llay[i].setOnTouchListener(touchListener);
		ids = new int[] {95, 25, 41, 100, 20, 100, 50};
		for(int i=0; i<ids.length; i++) skb[i].setMax(ids[i]);
		int ssvalue = share.getInt("ssvalue", 50);
		ids = new int[] {share.getInt("ttsize", 60) - 50, share.getInt("stsize", 18) - 12, share.getInt("intv", 30) - 20,
				share.getInt("opac", 35), frzTime, ssvalue, share.getInt("sensity", 10)};
		for(int i=0; i<ids.length; i++) skb[i].setProgress(ids[i]);
		stt[3].setText(getString(R.string.timer_size) + share.getInt("ttsize", 60));
		stt[4].setText(getString(R.string.scrsize) + share.getInt("stsize", 18));
		stt[10].setText(getString(R.string.row_spacing) + share.getInt("intv", 30));
		stt[29].setText(getString(R.string.time_tap) + frzTime/20D);
		stt[37].setText(getString(R.string.stt_ssvalue) + ssvalue);
		Stackmat.switchThreshold = ssvalue;
		for(int i=0; i<7; i++) skb[i].setOnSeekBarChangeListener(new OnSeekBarChangeListener());
		stSwitch[0].setBackgroundResource(wca ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[1].setBackgroundResource(clkform ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[2].setBackgroundResource(simss ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[3].setBackgroundResource(usess ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[4].setBackgroundResource(invs ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[5].setBackgroundResource(hidscr ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[6].setBackgroundResource(conft ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[7].setBackgroundResource(hidls ? R.drawable.switchoff : R.drawable.switchon);
		stSwitch[8].setBackgroundResource(selSes ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[9].setBackgroundResource(fulls ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[10].setBackgroundResource(opnl ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[11].setBackgroundResource(opnd ? R.drawable.switchon : R.drawable.switchoff);
		stSwitch[12].setBackgroundResource(drop ? R.drawable.switchon : R.drawable.switchoff);
		for(int i=0; i<13; i++) stSwitch[i].setOnClickListener(new OnClickListener());

		if(l1am) chkb[3].setChecked(true);
		if(l2am) chkb[2].setChecked(true);
		getSession(spSel[5]);
		seMean.setText(getString(R.string.session_average) + Mi.sesMean());
		setGvTitle();
		if(isMulp) multemp = new long[7];
		setGridView(true);

		if(usess && !stm.isStart) stm.start();
		if((egtype & 4) != 0) chkb[1].setChecked(true);
		if((egtype & 2) != 0) chkb[4].setChecked(true);
		if((egtype & 1) != 0) chkb[0].setChecked(true);
		if((egoll & 128) != 0) chkb[8].setChecked(true);
		if((egoll & 64) != 0) chkb[12].setChecked(true);
		if((egoll & 32) != 0) chkb[11].setChecked(true);
		if((egoll & 16) != 0) chkb[10].setChecked(true);
		if((egoll & 8) != 0) chkb[9].setChecked(true);
		if((egoll & 4) != 0) chkb[6].setChecked(true);
		if((egoll & 2) != 0) chkb[7].setChecked(true);
		if((egoll & 1) != 0) chkb[5].setChecked(true);
		for(int i=0; i<chkb.length; i++)
			chkb[i].setOnCheckedChangeListener(listener);

		wbAuth = (Button) findViewById(R.id.auth_sina);

		tvl.setTextColor(cl[1]);
		for(int i=0; i<sttlen; i++) stt[i].setTextColor(cl[1]);
		for(int i=0; i<chkb.length; i++) chkb[i].setTextColor(cl[1]);
		tvScr.setTextColor(cl[1]);
		tvTimer.setTextColor(cl[1]);

		vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
		
		sensor = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		proDlg = new ProgressDialog(this);
		proDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		proDlg.setTitle(getString(R.string.menu_outscr));
		proDlg.setCancelable(false);
		dlProg = new ProgressDialog(this);
		dlProg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dlProg.setTitle(getString(R.string.downloading));
		dlProg.setCancelable(false);
		
		mWeiboAuth = new WeiboAuth(this, APP_KEY, REDIRECT_URL, SCOPE);
		mAccessToken = AccessTokenKeeper.readAccessToken(this);
		if(!mAccessToken.isSessionValid()) wbAuth.setText(R.string.login);
		else {
			isLogin = true;
			wbAuth.setText(R.string.logout);
		}
		
		//��������
		spinner[0].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() { 
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				spSel[0] = (byte) arg2;
				if(spSel[0] != selold) {spSel[6] = 0; selold = spSel[0];}
				set2ndsel();
				setScrType();
				newScr(true);
				if(selSes) searchSesType();
				if(inScr != null && inScr.size() != 0) inScr = null;
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		spinner[6].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (spSel[6] != arg2) {
					spSel[6] = (byte) arg2;
					//set2ndsel();
					setScrType();
					newScr(true);
					if (selSes) searchSesType();
					if (inScr != null && inScr.size() != 0) inScr = null;
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		//����ƽ��0
		spinner[4].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() { 
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(spSel[4] != arg2) {
					spSel[4] = (byte) arg2;
					if(!isMulp) {
						setGvTitle();
						setGridView(false);
					}
					edit.putInt("list1", spSel[4]);
					edit.commit();
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		//����ƽ��1
		spinner[2].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() { 
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(spSel[2] != arg2) {
					spSel[2] = (byte) arg2;
					if(!isMulp) {
						setGvTitle();
						setGridView(false);
					}
					edit.putInt("list2", spSel[2]);
					edit.commit();
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		//����
		spinner[5].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() { 
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(spSel[5] != arg2) {
					spSel[5] = (byte) arg2;
					getSession(arg2);
					seMean.setText(getString(R.string.session_average) + Mi.sesMean());
					setGridView(true);
					edit.putInt("group", spSel[5]);
					edit.commit();
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		//ʮ�ֵ���
		spinner[1].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() { 
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(spSel[1] != arg2) {
					spSel[1] = (byte) arg2;
					edit.putInt("cface", spSel[1]);
					edit.commit();
					if(spSel[0]==1 && (spSel[6]==0 || spSel[6]==1 || spSel[6]==5 || spSel[6]==19))
						new Thread() {
							public void run() {
								handler.sendEmptyMessage(6);
								extsol = "\n"+Cross.cross(crntScr, spSel[1], spSel[3]);
								handler.sendEmptyMessage(3);
								isNextScr = false;
								nextScr = Mi.SetScr((spSel[0]<<5)|spSel[6], false);
								isNextScr = true;
							}
						}.start();
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		//��ɫ
		spinner[3].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() { 
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(spSel[3] != arg2) {
					spSel[3] = (byte) arg2;
					edit.putInt("cside", spSel[3]);
					edit.commit();
					if(spSel[0]==1 && (spSel[6]==0 || spSel[6]==1 || spSel[6]==5 || spSel[6]==19))
						new Thread() {
							public void run() {
								handler.sendEmptyMessage(6);
								switch(stSel[5]) {
								case 1: extsol="\n"+Cross.cross(crntScr, spSel[1], spSel[3]); break;
								case 2: extsol="\n"+Cross.xcross(crntScr, spSel[3]); break;
								case 3: extsol="\n"+EOline.eoLine(crntScr, spSel[3]); break;
								case 4: extsol="\n"+PetrusxRoux.roux(crntScr, spSel[3]); break;
								case 5: extsol="\n"+PetrusxRoux.petrus(crntScr, spSel[3]); break;
								}
								handler.sendEmptyMessage(3);
								isNextScr=false;
								nextScr = Mi.SetScr((spSel[0]<<5)|spSel[6], false);
								isNextScr = true;
							}
						}.start();
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		//����״̬
		buttonSst.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(Mi.viewType > 0) {
					int width = dm.widthPixels;
					LayoutInflater inflater = LayoutInflater.from(DCTimer.this);	// ȡ��LayoutInflater����
					final View popView = inflater.inflate(R.layout.popwindow, null);	// ��ȡ���ֹ�����
					popView.setBackgroundColor(0xaaece9d8);
					iv = (ImageView) popView.findViewById(R.id.ImageView1);
					Bitmap bm = Bitmap.createBitmap(width, width*3/4, Config.ARGB_8888);
					Canvas c = new Canvas(bm);
					c.drawColor(0);
					Paint p = new Paint();
					p.setAntiAlias(true);
					Mi.drawScr(spSel[6], width, p, c);
					iv.setImageBitmap(bm);
					new AlertDialog.Builder(DCTimer.this).setView(popView)
					.setNegativeButton(getString(R.string.btn_close), null).show();
				} else Toast.makeText(DCTimer.this, getString(R.string.not_support), Toast.LENGTH_SHORT).show();
			}
		});

		//����
		tvScr.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				scrt = true;
				setTouch(event);
				return timer.state != 0;
			}
		});
		tvScr.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				if(timer.state == 0) {
					isLongPress = true;
					LayoutInflater factory = LayoutInflater.from(DCTimer.this);
					final View view = factory.inflate(R.layout.scr_layout, null);
					final EditText editText = (EditText)view.findViewById(R.id.etslen);
					final TextView tvScr = (TextView)view.findViewById(R.id.cnt_scr);
					tvScr.setText(crntScr);
					editText.setText(""+Mi.scrLen);
					if(Mi.scrLen==0)editText.setEnabled(false);
					new AlertDialog.Builder(DCTimer.this).setView(view)
					.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String et = editText.getText().toString();
							int len = et.equals("")?0:Integer.parseInt(editText.getText().toString());
							if(editText.isEnabled() && len>0) {
								if(len>180) len=180;
								if(len != Mi.scrLen) {
									Mi.scrLen = len;
									if((spSel[0]==1 && spSel[6]==19) || (spSel[0]==20 && spSel[6]==4)) isChScr = true;
									newScr(false);
								}
							}
						}
					}).setNeutralButton(getString(R.string.copy_scr), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							ClipboardManager clip=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
							clip.setText(crntScr);
							Toast.makeText(DCTimer.this, getString(R.string.copy_to_clip), Toast.LENGTH_SHORT).show();
						}
					}).show();
				}
				return true;
			}
		});

		//��ʱ��
		tvTimer.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				scrt = false;
				if(!usess) {
					if(stSel[0] == 0) setTouch(event);
					else if(stSel[0] == 1) inputTime(event.getAction());
				}
				return true;
			}
		});

		myGridView.setOnItemClickListener(new GridView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int p, long arg3) {
				if(isMulp) {
					if(p/(stSel[3]+2)<resl && p%(stSel[3]+2)==0) singTime(p, stSel[3]+2);
				}
				else if(p%3 == 0)
					singTime(p, 3);
				else if(p%3==1 && p/3>listnum[spSel[4]]-2)
					showAlertDialog(1, p/3);
				else if(p%3==2 && p/3>listnum[spSel[2]+1]-2)
					showAlertDialog(2, p/3);
			}
		});

		//����ƽ��
		seMean.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				for(int i=0; i<resl; i++)
					if(resp[i] != 2) {
						showAlertDialog(3, 0);
						return;
					}
			}
		});

		//����ѡ��
		sesOpt.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(DCTimer.this).
				setItems(R.array.optStr, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:	//��������
							LayoutInflater factory = LayoutInflater.from(DCTimer.this);
							final View view = factory.inflate(R.layout.ses_name, null);
							adapter = new ArrayAdapter<String>(DCTimer.this, android.R.layout.simple_spinner_item, items);
							adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							final EditText et= (EditText)view.findViewById(R.id.edit_ses);
							et.setText(sesname[spSel[5]]);
							new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.sesname)).setView(view)
							.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									sesname[spSel[5]]=et.getText().toString();
									edit.putString("sesname"+spSel[5], sesname[spSel[5]]);
									edit.commit();
									String[] mItems=new String[15];
									for(int j=0; j<15; j++)
										mItems[j]=j+1+(sesname[j].equals("")?"":" - "+sesname[j]);
									adapter = new ArrayAdapter<String>(DCTimer.this, android.R.layout.simple_spinner_item, mItems);
									adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
									spinner[5].setAdapter(adapter);
									adapter.notifyDataSetChanged();
									spinner[5].setSelection(spSel[5]);
								}
							}).setNegativeButton(getString(R.string.btn_cancel), null).show();
							break;
						case 1:	//��ճɼ�
							if(resl == 0) Toast.makeText(DCTimer.this, getString(R.string.no_times), Toast.LENGTH_SHORT).show();
							else {
								new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.confirm_clear_session))
								.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialoginterface, int j) {deleteAll();}
								}).setNegativeButton(getString(R.string.btn_cancel), null).show();
							}
							break;
						case 2:	//ʱ��ֲ�ֱ��ͼ
							int width = dm.widthPixels;
							int height = dm.heightPixels;
							if(height<width) width = height;
							width = (int) (width*0.9);
							LayoutInflater inflater = LayoutInflater.from(DCTimer.this);
							final View popView = inflater.inflate(R.layout.popwindow, null);
							popView.setBackgroundColor(0xddf0f0f0);
							iv = (ImageView) popView.findViewById(R.id.ImageView1);
							Bitmap bm = Bitmap.createBitmap(width, (int)(width*1.2), Config.ARGB_8888);
							Canvas c = new Canvas(bm);
							c.drawColor(0);
							Paint p = new Paint();
							p.setAntiAlias(true);
							Mi.drawHist(width, p, c);
							iv.setImageBitmap(bm);
							new AlertDialog.Builder(DCTimer.this).setView(popView)
							.setNegativeButton(getString(R.string.btn_close), null).show();
							break;
						case 3:	//����ͼ
							width = Math.min(dm.widthPixels, dm.heightPixels);
							int wid = (int) (width * 0.9);
							inflater = LayoutInflater.from(DCTimer.this);
							final View pView = inflater.inflate(R.layout.popwindow, null);
							pView.setBackgroundColor(0xddf0f0f0);
							iv = (ImageView) pView.findViewById(R.id.ImageView1);
							bm = Bitmap.createBitmap(wid, (int)(wid*0.8), Config.ARGB_8888);
							c = new Canvas(bm);
							//c.drawColor(0);
							p = new Paint();
							p.setAntiAlias(true);
							Mi.drawGraph(wid, p, c);
							iv.setImageBitmap(bm);
							new AlertDialog.Builder(DCTimer.this).setView(pView)
							.setNegativeButton(getString(R.string.btn_close), null).show();
							break;
						case 4:	//�������ݿ�
							try {
								BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(defPath+"Database.csv"), "UTF-8"));
								for(int i=0; i<15; i++) {
									Cursor cur = dbh.query(i);
									int count = cur.getCount();
									if(count == 0) continue;
									writer.write(i+1+"\r\n");
									cur.moveToFirst();
									for(int j=0; j<count; j++) {
										writer.write(cur.getInt(1)+","+cur.getInt(2)+","+cur.getInt(3)+","
												+cur.getString(4).replace("\n", "\\n")+","+cur.getString(5)+","+cur.getString(6)
												+","+cur.getInt(7)+","+cur.getInt(8)+","+cur.getInt(9)+","
												+cur.getInt(10)+","+cur.getInt(11)+","+cur.getInt(12)+"\r\n");
										cur.moveToNext();
									}
									cur.close();
								}
								writer.close();
								Toast.makeText(DCTimer.this, getString(R.string.saved)+"sdcard/DCTimer/Database.csv", Toast.LENGTH_LONG).show();
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(DCTimer.this, getString(R.string.save_failed), Toast.LENGTH_LONG).show();
							}
							break;
						case 5:	//�������ݿ�
							final ProgressDialog dlg = new ProgressDialog(DCTimer.this);
							dlg.setTitle(getString(R.string.importing));
							dlg.show();
							new Thread() {
								public void run() {
									try {
										int table = 1;
										BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(defPath+"Database.csv"), "UTF-8"));
										String line = "";
										ContentValues cv = new ContentValues();
										int count = 1;
										while (((line = reader.readLine()) != null)) {
											if(!line.contains(",")) {
												table = Integer.parseInt(line);
												count = 1;
											} else {
												String[] ts = line.split(",");
												cv.put("id", count++);
												cv.put("rest", Integer.parseInt(ts[0]));
												cv.put("resp", Integer.parseInt(ts[1]));
												cv.put("resd", Integer.parseInt(ts[2]));
												cv.put("scr", ts[3].replace("\\n", "\n"));
												if(!ts[4].equals("null")) cv.put("time", ts[4]);
												if(!ts[5].equals("null")) cv.put("note", ts[5]);
												for(int i=0; i<6; i++)
													cv.put("p"+(i+1), Integer.parseInt(ts[i+6]));
												dbh.insert(table-1, cv);
											}
										}
										reader.close();
									} catch (Exception e) {
										e.printStackTrace();
									}
									getSession(spSel[5]);
									handler.sendEmptyMessage(14);
									dlg.dismiss();
								}
							}.start();
						}
					}
				})
				.setNegativeButton(getString(R.string.btn_cancel), null).show();
			}
		});

		//�ָ�Ĭ������
		reset.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.confirm_reset))
				.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int j) {
						//TODO
						wca=false; clkform=true; simss=false; usess=false; invs=Stackmat.inv=false;
						hidscr=true; conft=true; hidls=false; selSes=false; fulls=false;
						l1am=true; l2am=true; bgcolor=true; opnl=false; opnd=true; isMulp=false;
						spSel[1]=0; spSel[2]=1; spSel[3]=1; spSel[4]=1;
						stSel[0]=0; stSel[1]=0; stSel[2]=1; stSel[3]=0; stSel[4]=1;
						stSel[5]=0; stSel[6]=0; stSel[7]=1; stSel[8]=3; stSel[9]=0;
						stSel[10]=0; stSel[11]=2; stSel[12]=0;
						tvTimer.setTextSize(60); tvScr.setTextSize(18);
						cl[0] = 0xff66ccff;	cl[1] = 0xff000000;	cl[2] = 0xffff00ff;
						cl[3] = 0xffff0000;	cl[4] = 0xff009900;
						int i;
						for(i=2;i<4;i++) chkb[i].setChecked(true);
						for(i=0; i<12; i++) std[i].setText(itemStr[i][stSel[i]]);
						for(i=1; i<5; i++) spinner[i].setSelection(spSel[i]);
						int is[] = {1, 5, 6, 7, 11};
						for(i=0; i<is.length; i++) stSwitch[is[i]].setBackgroundResource(R.drawable.switchon);
						is = new int[] {0, 2, 3, 4, 8, 9, 10, 12};
						for(i=0; i<is.length; i++) stSwitch[is[i]].setBackgroundResource(R.drawable.switchoff);
						is = new int[] {10, 6, 10, 35, 0, 50, 10};
						for(i=0; i<7; i++) skb[i].setProgress(is[i]);
						intv = 30; frzTime = 0;
						tabHost.setBackgroundColor(cl[0]);
						tvl.setTextColor(cl[1]);
						for(i=0; i<sttlen; i++) stt[i].setTextColor(cl[1]);
						for(i=0; i<chkb.length; i++) chkb[i].setTextColor(cl[1]);
						tvScr.setTextColor(cl[1]);
						tvTimer.setTextColor(cl[1]);
						setGridView(false);
						releaseWakeLock();
						screenOn=false;
						edit.remove("cl0");	edit.remove("cl1");	edit.remove("cl2");
						edit.remove("cl3");	edit.remove("cl4");	edit.remove("wca");
						edit.remove("cxe");
						edit.remove("l1am");	edit.remove("l2am");	edit.remove("mnxc");
						edit.remove("prec");	edit.remove("mulp");	edit.remove("invs");
						edit.remove("tapt");	edit.remove("intv");	edit.remove("opac");
						edit.remove("mclr");	edit.remove("prom");	edit.remove("sq1s");
						edit.remove("hidls");	edit.remove("conft");	edit.remove("list1");
						edit.remove("list2");	edit.remove("timmh");	edit.remove("tiway");
						edit.remove("cface");	edit.remove("cside");	edit.remove("srate");
						edit.remove("tfont");	edit.remove("vibra");	edit.remove("sqshp");
						edit.remove("fulls");	edit.remove("usess");	edit.remove("scron");
						edit.remove("multp");	edit.remove("minxc");
						edit.remove("hidscr");	edit.remove("ttsize");	edit.remove("stsize");
						edit.remove("cube2l");	edit.remove("scrgry");	edit.remove("selses");
						edit.remove("ismulp");
						edit.remove("vibtime");	edit.remove("bgcolor");	edit.remove("ssvalue");
						edit.remove("timerupd");
						edit.remove("screenori");
						edit.commit();
					}
				}).setNegativeButton(getString(R.string.btn_cancel), null).show();
			}
		});

		//΢����Ȩ
		wbAuth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isLogin) {
					new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.con_rsauth))
					.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int j) {
							AccessTokenKeeper.clear(DCTimer.this);
							isLogin = false;
							Toast.makeText(DCTimer.this, getString(R.string.rsauth), Toast.LENGTH_SHORT).show();
							wbAuth.setText(getString(R.string.login));
						}
					}).setNegativeButton(getString(R.string.btn_cancel), null).show();
				}
				else {
					isShare = false;
					auth();
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(opnd && screenOn) releaseWakeLock();
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(opnd && screenOn) acquireWakeLock();
		//TODO
		List<Sensor> ss = sensor.getSensorList(Sensor.TYPE_ACCELEROMETER);
		for(Sensor s : ss)
			sensor.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onStop() {
		super.onStop();
		sensor.unregisterListener(this);
	}
	
	private class OnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.stcheck1:	//WCA�۲�
				stSwitch[0].setBackgroundResource(wca ? R.drawable.switchoff : R.drawable.switchon);
				wca = !wca; edit.putBoolean("wca", wca);
				break;
			case R.id.stcheck2:	//ʱ���ʽ
				stSwitch[1].setBackgroundResource(clkform ? R.drawable.switchoff : R.drawable.switchon);
				clkform = !clkform; edit.putBoolean("timmh", clkform);
				if(resl>0) setGridView(false);
				break;
			case R.id.stcheck3:	//ģ��ss��ʱ
				stSwitch[2].setBackgroundResource(simss ? R.drawable.switchoff : R.drawable.switchon);
				simss = !simss; edit.putBoolean("simss", simss);
				break;
			case R.id.stcheck4:	//ʹ��ss��ʱ
				stSwitch[3].setBackgroundResource(usess ? R.drawable.switchoff : R.drawable.switchon);
				usess = !usess; edit.putBoolean("usess", usess);
				if(usess) {
					tvTimer.setText("OFF");
					if(!stm.isStart) stm.start();
				} else {
					if(stm.isStart) stm.stop();
					if(stSel[0]==0) tvTimer.setText(stSel[2]==0 ? "0.00" : "0.000");
					else if(stSel[0]==1) tvTimer.setText("IMPORT");
				}
				break;
			case R.id.stcheck5:	//�źŷ�ת
				stSwitch[4].setBackgroundResource(invs ? R.drawable.switchoff : R.drawable.switchon);
				invs = Stackmat.inv = !invs; edit.putBoolean("invs", invs);
				break;
			case R.id.stcheck6:	//���ش���
				stSwitch[5].setBackgroundResource(hidscr ? R.drawable.switchoff : R.drawable.switchon);
				hidscr = !hidscr; edit.putBoolean("hidscr", hidscr);
				break;
			case R.id.stcheck7:	//ȷ��ʱ��
				stSwitch[6].setBackgroundResource(conft ? R.drawable.switchoff : R.drawable.switchon);
				conft = !conft; edit.putBoolean("conft", conft);
				break;
			case R.id.stcheck8:	//�ɼ��б����ش���
				stSwitch[7].setBackgroundResource(hidls ? R.drawable.switchon : R.drawable.switchoff);
				hidls = !hidls; edit.putBoolean("hidls", hidls);
				break;
			case R.id.stcheck9:	//�Զ�ѡ�����
				stSwitch[8].setBackgroundResource(selSes ? R.drawable.switchoff : R.drawable.switchon);
				selSes = !selSes; edit.putBoolean("selses", selSes);
				break;
			case R.id.stcheck10:	//������ͣ��
				stSwitch[12].setBackgroundResource(drop ? R.drawable.switchoff : R.drawable.switchon);
				drop = !drop; edit.putBoolean("drop", drop);
//				stt[60].setBackgroundResource(sqshp ? R.drawable.switchoff : R.drawable.switchon);
//				sqshp = !sqshp; edit.putBoolean("sqshp", sqshp);
//				if(spSel[0]==8) {
//					if(sqshp) {
//						new Thread() {
//							public void run() {
//								handler.sendEmptyMessage(6);
//								extsol = " " + Sq1Shape.solveTrn(crntScr);
//								handler.sendEmptyMessage(1);
//								isNextScr = false;
//								nextScr = Mi.SetScr((spSel[0]<<5)|spSel[6], false);
//								isNextScr = true;
//							}
//						}.start();
//					}
//					else tvScr.setText(crntScr);
//				}
				break;
			case R.id.stcheck11:	//ȫ����ʾ
				stSwitch[9].setBackgroundResource(fulls ? R.drawable.switchoff : R.drawable.switchon);
				if(fulls) getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				else getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				fulls = !fulls; edit.putBoolean("fulls", fulls);
				break;
			case R.id.stcheck12:	//��Ļ����
				stSwitch[10].setBackgroundResource(opnl ? R.drawable.switchoff : R.drawable.switchon);
				if(opnl) {
					if(timer.state != 1) releaseWakeLock();
				} else acquireWakeLock();
				opnl = !opnl; edit.putBoolean("scron", opnl);
				break;
			case R.id.stcheck13:
				stSwitch[11].setBackgroundResource(opnd ? R.drawable.switchoff : R.drawable.switchon);
				if(screenOn)releaseWakeLock();
				opnd = !opnd;
				if(screenOn)acquireWakeLock();
				edit.putBoolean("scrgry", opnd);
				break;
			}
			edit.commit();
		}
	}

	private class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			if(seekBar.getId()==R.id.seekb1)stt[3].setText(getString(R.string.timer_size) + (seekBar.getProgress()+50));
			else if(seekBar.getId()==R.id.seekb2)stt[4].setText(getString(R.string.scrsize) + (seekBar.getProgress()+12));
			else if(seekBar.getId()==R.id.seekb3)stt[10].setText(getString(R.string.row_spacing) + (seekBar.getProgress()+20));
			else if(seekBar.getId()==R.id.seekb5)stt[29].setText(getString(R.string.time_tap) + (seekBar.getProgress()/20D));
			else if(seekBar.getId()==R.id.seekb6)stt[37].setText(getString(R.string.stt_ssvalue) + seekBar.getProgress());
		}
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if(seekBar.getId()==R.id.seekb1)stt[3].setText(getString(R.string.timer_size)+ (seekBar.getProgress()+50));
			else if(seekBar.getId()==R.id.seekb2)stt[4].setText(getString(R.string.scrsize)+ (seekBar.getProgress()+12));
			else if(seekBar.getId()==R.id.seekb3)stt[10].setText(getString(R.string.row_spacing)+ (seekBar.getProgress()+20));
			else if(seekBar.getId()==R.id.seekb5)stt[29].setText(getString(R.string.time_tap)+ (seekBar.getProgress()/20D));
			else if(seekBar.getId()==R.id.seekb6)stt[37].setText(getString(R.string.stt_ssvalue) + seekBar.getProgress());
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			switch (seekBar.getId()) {
			case R.id.seekb1:	//��ʱ������
				stt[3].setText(getString(R.string.timer_size)+ (seekBar.getProgress()+50));
				edit.putInt("ttsize", seekBar.getProgress()+50);
				tvTimer.setTextSize(seekBar.getProgress()+50);
				break;
			case R.id.seekb2:	//��������
				stt[4].setText(getString(R.string.scrsize)+ (seekBar.getProgress()+12));
				edit.putInt("stsize", seekBar.getProgress()+12);
				tvScr.setTextSize(seekBar.getProgress()+12);
				break;
			case R.id.seekb3:	//�ɼ��б��о�
				intv=seekBar.getProgress()+20;
				stt[10].setText(getString(R.string.row_spacing)+ intv);
				if(resl!=0) setGridView(false);
				edit.putInt("intv", seekBar.getProgress()+20);
				break;
			case R.id.seekb4:	//����ͼ��͸����
				if(!bgcolor) setBgPic(bitmap, seekBar.getProgress());
				edit.putInt("opac", seekBar.getProgress());
				break;
			case R.id.seekb5:	//������ʱ
				frzTime=seekBar.getProgress();
				stt[29].setText(getString(R.string.time_tap)+ (frzTime/20D));
				edit.putInt("tapt", frzTime);
				break;
			case R.id.seekb6:	//ss����
				int ssvalue = seekBar.getProgress();
				stt[37].setText(getString(R.string.stt_ssvalue) + ssvalue);
				Stackmat.switchThreshold = ssvalue;
				edit.putInt("ssvalue", ssvalue);
				break;
			case R.id.seekb7:	//������
				sensity = seekBar.getProgress();
				edit.putInt("sensity", sensity);
				break;
			}
			edit.commit();
		}
	}

	private OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			selFilePath = paths.get(arg2);
			getFileDir(selFilePath);
		}
	};

	private OnCheckedChangeListener listener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch (buttonView.getId()) {
			//RA0ȥβͳ��
			case R.id.lcheck1:
				l1am = isChecked; edit.putBoolean("l1am", isChecked);
				if(!isMulp) setGvTitle();
				if(resl>0 && !isMulp) setGridView(false);
				break;
			case R.id.lcheck2:
				l2am = isChecked; edit.putBoolean("l2am", isChecked);
				if(!isMulp)setGvTitle();
				if(resl>0 && !isMulp) setGridView(false);
				break;
			//EGѵ������
			case R.id.checkcll:
				if(isChecked) egtype |= 4;
				else egtype &= 3;
				edit.putInt("egtype", egtype);
				break;
			case R.id.checkeg1:
				if(isChecked) egtype |= 2;
				else egtype &= 5;
				edit.putInt("egtype", egtype);
				break;
			case R.id.checkeg2:
				if(isChecked) egtype |= 1;
				else egtype &= 6;
				edit.putInt("egtype", egtype);
				break;
			case R.id.checkegpi:
				if(isChecked) {
					egoll |= 128;
					if(chkb[5].isChecked()) {chkb[5].setChecked(false); egoll &= 254;}
				}
				else egoll &= 127;
				edit.putInt("egoll", egoll);
				setEgOll();
				break;
			case R.id.checkegh:
				if(isChecked) {
					egoll |= 64;
					if(chkb[5].isChecked()) {chkb[5].setChecked(false); egoll &= 254;}
				}
				else egoll &= 191;
				edit.putInt("egoll", egoll);
				setEgOll();
				break;
			case R.id.checkegu:
				if(isChecked) {
					egoll |= 32;
					if(chkb[5].isChecked()) {chkb[5].setChecked(false); egoll &= 254;}
				}
				else egoll &= 223;
				edit.putInt("egoll", egoll);
				setEgOll();
				break;
			case R.id.checkegt:
				if(isChecked) {
					egoll |= 16;
					if(chkb[5].isChecked()) {chkb[5].setChecked(false); egoll &= 254;}
				}
				else egoll &= 239;
				edit.putInt("egoll", egoll);
				setEgOll();
				break;
			case R.id.checkegl:
				if(isChecked) {
					egoll |= 8;
					if(chkb[5].isChecked()) {chkb[5].setChecked(false); egoll &= 254;}
				}
				else egoll &= 247;
				edit.putInt("egoll", egoll);
				setEgOll();
				break;
			case R.id.checkegs:
				if(isChecked) {
					egoll |= 4;
					if(chkb[5].isChecked()) {chkb[5].setChecked(false); egoll &= 254;}
				}
				else egoll &= 251;
				edit.putInt("egoll", egoll);
				setEgOll();
				break;
			case R.id.checkega:
				if(isChecked) {
					egoll |= 2;
					if(chkb[5].isChecked()) {chkb[5].setChecked(false); egoll &= 254;}
				}
				else egoll &= 253;
				edit.putInt("egoll", egoll);
				setEgOll();
				break;
			case R.id.checkegn:
				if(isChecked) {
					egoll |= 1;
					for(int i=6; i<13; i++)
						if(chkb[i].isChecked()) chkb[i].setChecked(false);
				}
				else egoll &= 254;
				edit.putInt("egoll", egoll);
				setEgOll();
				break;
			}
			edit.commit();
		}
	};

	private View.OnTouchListener comboListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int selt;
			switch (v.getId()) {
			case R.id.lay01: selt = 0; break;
			case R.id.lay02: selt = 1; break;
			case R.id.lay03: selt = 2; break;
			case R.id.lay04: selt = 3; break;
			case R.id.lay05: selt = 4; break;
			case R.id.lay06: selt = 5; break;
			case R.id.lay07: selt = 6; break;
			case R.id.lay08: selt = 7; break;
			case R.id.lay09: selt = 8; break;
			case R.id.lay10: selt = 9; break;
			case R.id.lay11: selt = 10; break;
			case R.id.lay12: selt = 11; break;
			case R.id.lay23: selt = 12; break;
			default: selt = -1; break;
			}
			final int sel = selt;
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				llay[sel].setBackgroundColor(0x80ffffff);
				break;
			case MotionEvent.ACTION_UP:
				new AlertDialog.Builder(DCTimer.this).setSingleChoiceItems(staid[sel], stSel[sel], new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if(stSel[sel] != which) {
							stSel[sel] = which;
							switch (sel) {
							case 0:	//��ʱ��ʽ
								if (!usess) {
									if (which == 0) tvTimer.setText(stSel[2]==0 ? "0.00" : "0.000");
									else tvTimer.setText("IMPORT");
								}
								edit.putInt("tiway", which);
								break;
							case 1:	//��ʱ�����·�ʽ
								edit.putInt("timerupd", which);
								break;
							case 2:	//��ʱ��ȷ��
								if(which == 0) {edit.putBoolean("prec", false); if(stSel[0]==0) tvTimer.setText("0.00");}
								else {edit.putBoolean("prec", true); if(stSel[0]==0) tvTimer.setText("0.000");}
								if(resl != 0) {
									setGridView(false);
									seMean.setText(getString(R.string.session_average) + Mi.sesMean());
								}
								break;
							case 3:	//�ֶμ�ʱ
								if(which == 0) {
									isMulp=false; mulp = null; multemp = null;
									System.gc();
									times = (resl!=0) ? new String[resl*3] : null;
								} else if(!isMulp) {
									isMulp=true;
									multemp = new long[7];
									mulp = new int[6][rest.length];
									if(resl>0) {
										cursor = dbh.query(spSel[5]);
										for(int i=0; i<resl; i++) {
											cursor.moveToPosition(i);
											for(int j=0; j<6; j++)
												mulp[j][i] = cursor.getInt(7+j);
										}
										//cursor.close();
									}
									times = (resl!=0) ? new String[(which+2)*(resl+1)]:null;
								}
								else {
									times = resl!=0 ? new String[(which+2)*(resl+1)] : null;
								}
								edit.putInt("multp", which);
								setGridView(false);
								setGvTitle();
								break;
							case 4:	//����Ƶ��
								if(stm.creatAudioRecord((int)srate[which]));
								else Toast.makeText(DCTimer.this, getString(R.string.sr_not_support), Toast.LENGTH_SHORT).show();
								edit.putInt("srate", which);
								break;
							case 5:	//�������
								edit.putInt("cxe", which);
								if(which == 0) {spinner[1].setEnabled(false); spinner[3].setEnabled(false);}
								else if(which == 1) {spinner[1].setEnabled(true); spinner[3].setEnabled(true);}
								else {spinner[1].setEnabled(false); spinner[3].setEnabled(true);}
								if(spSel[0]==1 && (spSel[6]==0 || spSel[6]==1 || spSel[6]==5 || spSel[6]==19)) {
									if(which==0)tvScr.setText(crntScr);
									else new Thread() {
										public void run() {
											handler.sendEmptyMessage(6);
											switch(stSel[5]) {
											case 1: extsol="\n"+Cross.cross(crntScr, spSel[1], spSel[3]); break;
											case 2: extsol="\n"+Cross.xcross(crntScr, spSel[3]); break;
											case 3: extsol="\n"+EOline.eoLine(crntScr, spSel[3]); break;
											case 4: extsol="\n"+PetrusxRoux.roux(crntScr, spSel[3]); break;
											case 5: extsol="\n"+PetrusxRoux.petrus(crntScr, spSel[3]); break;
											}
											handler.sendEmptyMessage(3);
											isNextScr = false;
											nextScr = Mi.SetScr((spSel[0]<<5)|spSel[6], false);
											isNextScr = true;
										}
									}.start();
								}
								break;
							case 6:	//���׵���
								edit.putInt("cube2l", which);
								if(spSel[0]==0) {
									if(which==0)tvScr.setText(crntScr);
									else if(spSel[6] < 3) new Thread() {
										public void run() {
											handler.sendEmptyMessage(6);
											extsol = "\n"+Cube2bl.cube2layer(crntScr, stSel[6]);
											handler.sendEmptyMessage(3);
											isNextScr=false;
											nextScr = Mi.SetScr((spSel[0]<<5)|spSel[6], false);
											isNextScr = true;
										}
									}.start();
								}
								break;
							case 7:	//��ħ��ɫ
								edit.putInt("minxc", which);
								break;
							case 8:	//��ʱ������
								setTimerFont(which);
								edit.putInt("tfont", which);
								break;
							case 9:	//��Ļ����
								DCTimer.this.setRequestedOrientation(screenOri[which]);
								edit.putInt("screenori", which);
								break;
							case 10:	//���з���
								edit.putInt("vibra", which);
								break;
							case 11:	//����ʱ��
								edit.putInt("vibtime", which);
								break;
							case 12:	//SQ������� TODO
								edit.putInt("sq1s", which);
								if(spSel[0]==8 && spSel[6]<3) {
									if(stSel[12] > 0) {
										new Thread() {
											public void run() {
												handler.sendEmptyMessage(6);
												extsol = " " + (stSel[12]==1 ? Sq1Shape.solveTrn(crntScr) : Sq1Shape.solveTws(crntScr));
												handler.sendEmptyMessage(1);
												isNextScr = false;
												nextScr = Mi.SetScr((spSel[0]<<5)|spSel[6], false);
												isNextScr = true;
											}
										}.start();
									}
									else tvScr.setText(crntScr);
								}
								break;
							}
							edit.commit();
							std[sel].setText(itemStr[sel][which]);
						}
						dialog.dismiss();
					}
				}).show();
			case MotionEvent.ACTION_CANCEL:
				llay[sel].setBackgroundColor(0);
				break;
			}
			return false;
		}
	};

	private View.OnTouchListener touchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) { //TODO
			int sel;
			switch (v.getId()) {
			case R.id.lay13: sel = 21; break;
			case R.id.lay14: sel = 13; break;
			case R.id.lay15: sel = 14; break;
			case R.id.lay16: sel = 15; break;
			case R.id.lay17: sel = 16; break;
			case R.id.lay22: sel = 17; break;
			case R.id.lay19: sel = 18; break;
			case R.id.lay20: sel = 19; break;
			case R.id.lay21: sel = 20; break;
			default: sel = -1; break;
			}
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				llay[sel].setBackgroundColor(0x80ffffff);
				break;
			case MotionEvent.ACTION_UP:
				switch (sel) {
				case 21:	//������ɫ
					dialog = new ColorPicker(context, cl[0], new ColorPicker.OnColorChangedListener() {
						@Override
						public void colorChanged(int color) {
							tabHost.setBackgroundColor(color); cl[0]=color; bgcolor=true;
							edit.putInt("cl0", color); edit.putBoolean("bgcolor", true);
							edit.commit();
						}
					});
					dialog.setTitle(getString(R.string.select_color));
					dialog.show();
					break;
				case 13:	//������ɫ
					dialog = new ColorPicker(context, cl[1], new ColorPicker.OnColorChangedListener() {
						@Override
						public void colorChanged(int color) {
							tvl.setTextColor(color);
							for(int i=0; i<sttlen; i++)stt[i].setTextColor(color);
							for(int i=0; i<chkb.length; i++)chkb[i].setTextColor(color);
							tvScr.setTextColor(color);
							tvTimer.setTextColor(color);
							cl[1]=color;
							if(resl!=0)
								setGridView(false);
							setGvTitle();
							edit.putInt("cl1", color);edit.commit();
						}
					});
					dialog.setTitle(getString(R.string.select_color));
					dialog.show();
					break;
				case 14:	//��쵥����ɫ
					dialog = new ColorPicker(context, cl[2], new ColorPicker.OnColorChangedListener() {
						@Override
						public void colorChanged(int color) {
							cl[2] = color;
							if(resl != 0)
								setGridView(false);
							edit.putInt("cl2", color);
							edit.commit();
						}
					});
					dialog.setTitle(getString(R.string.select_color));
					dialog.show();
					break;
				case 15:	//����������ɫ
					dialog = new ColorPicker(context, cl[3], new ColorPicker.OnColorChangedListener() {
						@Override
						public void colorChanged(int color) {
							cl[3]=color;
							if(resl!=0)
								setGridView(false);
							edit.putInt("cl3", color);
							edit.commit();
						}
					});
					dialog.setTitle(getString(R.string.select_color));
					dialog.show();
					break;
				case 16:	//���ƽ����ɫ
					dialog = new ColorPicker(context, cl[4], new ColorPicker.OnColorChangedListener() {
						@Override
						public void colorChanged(int color) {
							cl[4]=color;
							if(resl!=0 && !isMulp) setGridView(false);
							edit.putInt("cl4", color);
							edit.commit();
						}
					});
					dialog.setTitle(getString(R.string.select_color));
					dialog.show();
					break;
				case 17:	//����ͼƬ
					Intent intent = new Intent();
					intent.setType("image/*");	//����Pictures����Type�趨Ϊimage
					intent.setAction(Intent.ACTION_GET_CONTENT);	//ʹ��Intent.ACTION_GET_CONTENT���Action
					startActivityForResult(intent, 1);	//ȡ����Ƭ�󷵻ر�����
					break;
				case 18:	//��ɫ����
					int[] colors={share.getInt("csn1", Color.YELLOW), share.getInt("csn2", Color.BLUE), share.getInt("csn3", Color.RED),
							share.getInt("csn4", Color.WHITE), share.getInt("csn5", 0xff009900), share.getInt("csn6", 0xffff8026)};
					ColorScheme dialog = new ColorScheme(context, 1, colors, new ColorScheme.OnSchemeChangedListener() {
						@Override
						public void schemeChanged(int idx, int color) {
							edit.putInt("csn"+idx, color);
							edit.commit();
						}
					});
					dialog.setTitle(getString(R.string.scheme_cube));
					dialog.show();
					break;
				case 19:	//��������ɫ
					colors = new int[] {share.getInt("csp1", Color.RED), share.getInt("csp2", 0xff009900),
							share.getInt("csp3", Color.BLUE), share.getInt("csp4", Color.YELLOW)};
					dialog = new ColorScheme(context, 2, colors, new ColorScheme.OnSchemeChangedListener() {
						@Override
						public void schemeChanged(int idx, int color) {
							edit.putInt("csp"+idx, color);
							edit.commit();
						}
					});
					dialog.setTitle(getString(R.string.scheme_pyrm));
					dialog.show();
					break;
				case 20:	//SQ��ɫ
					colors = new int[] {share.getInt("csq1", Color.YELLOW), share.getInt("csq2", Color.BLUE), share.getInt("csq3", Color.RED),
							share.getInt("csq4", Color.WHITE), share.getInt("csq5", 0xff009900), share.getInt("csq6", 0xffff8026)};
					dialog = new ColorScheme(context, 3, colors, new ColorScheme.OnSchemeChangedListener() {
						@Override
						public void schemeChanged(int idx, int color) {
							edit.putInt("csq"+idx, color);
							edit.commit();
						}
					});
					dialog.setTitle(getString(R.string.scheme_sq));
					dialog.show();
					break;
				}
			case MotionEvent.ACTION_CANCEL:
				llay[sel].setBackgroundColor(0);
				break;
			}
			return false;
		}
	};

	private void setEgOll() {
		String ego = "PHUTLSAN";
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<8; i++)
			if((egoll & (1<<(7-i))) != 0)
				sb.append(ego.charAt(i));
		egolls = sb.toString();
	}

	private void viewsVisibility(boolean v) {
		int vi = v ? 0 : 8;
		tabHost.getTabWidget().setVisibility(vi);
		spinner[0].setVisibility(vi);
		spinner[6].setVisibility(vi);
		buttonSst.setVisibility(vi);
		if(hidscr)tvScr.setVisibility(vi);
	}

	private void set2ndsel() {
		String[] s = new String[0];
		switch(spSel[0]) {
		case 0:s=getResources().getStringArray(R.array.scr222);break;
		case 1:s=getResources().getStringArray(R.array.scr333);break;
		case 2:s=getResources().getStringArray(R.array.scr444);break;
		case 3:s=getResources().getStringArray(R.array.scr555);break;
		case 4:
		case 5:s=getResources().getStringArray(R.array.scr666);break;
		case 6:s=getResources().getStringArray(R.array.scrMinx);break;
		case 7:s=getResources().getStringArray(R.array.scrPrym);break;
		case 8:s=getResources().getStringArray(R.array.scrSq1);break;
		case 9:s=getResources().getStringArray(R.array.scrClk);break;
		case 10:s=getResources().getStringArray(R.array.scr15p);break;
		case 11:s=getResources().getStringArray(R.array.scrMxN);break;
		case 12:s=getResources().getStringArray(R.array.scrCmt);break;
		case 13:s=getResources().getStringArray(R.array.scrGear);break;
		case 14:s=getResources().getStringArray(R.array.scrSmc);break;
		case 15:s=getResources().getStringArray(R.array.scrSkw);break;
		case 16:s=getResources().getStringArray(R.array.scrOth);break;
		case 17:s=getResources().getStringArray(R.array.scr3sst);break;
		case 18:s=getResources().getStringArray(R.array.scrBdg);break;
		case 19:s=getResources().getStringArray(R.array.scrMsst);break;
		case 20:s=getResources().getStringArray(R.array.scrRly);break;
		}
		if(spSel[6] >= s.length) spSel[6] = 0;
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, s);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner[6].setAdapter(adapter);
		adapter.notifyDataSetChanged();
		spinner[6].setSelection(spSel[6]);
	}

	private void setInScr(String scrs) {
		String[] scr = scrs.split("\n");
		for(int i=0; i<scr.length; i++) {
			String cscr = scr[i].replaceFirst("^\\s*((\\(?\\d+\\))|(\\d+\\.))\\s*", "");
			if(!cscr.equals(""))inScr.add(cscr);
		}
	}

	private void outScr(final String path, final String fileName, final int num) {
		File fPath = new File(path);
		if(fPath.exists() || fPath.mkdir() || fPath.mkdirs()) {
			proDlg.setMax(num);
			proDlg.show();
			//proDlg = ProgressDialog.show(DCTimer.this, getString(R.string.menu_outscr), "");
			new Thread() {
				public void run() {
					try {
						OutputStream out = new BufferedOutputStream(new FileOutputStream(path+fileName));
						for(int i=0; i<num; i++) {
							String scr=(i+1)+". "+Mi.SetScr((spSel[0]<<5)|spSel[6], false)+"\r\n";
							handler.sendEmptyMessage(num*100+i);
							byte [] bytes = scr.toString().getBytes();
							out.write(bytes);
						}
						out.close();
						handler.sendEmptyMessage(7);
					} catch (IOException e) {
						handler.sendEmptyMessage(4);
					}
					proDlg.dismiss();
				}
			}.start();
		}
		else Toast.makeText(DCTimer.this, getString(R.string.path_not_exist), Toast.LENGTH_SHORT).show();
	}

	private void outStat(String path, String fileName, String stat) {
		File fPath = new File(path);
		if(fPath.exists() || fPath.mkdir() || fPath.mkdirs()) {
			try {
				OutputStream out = new BufferedOutputStream(new FileOutputStream(path+fileName));
				byte [] bytes = stat.toString().getBytes();
				out.write(bytes);
				out.close();
				Toast.makeText(DCTimer.this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				Toast.makeText(DCTimer.this, getString(R.string.save_failed), Toast.LENGTH_SHORT).show();
			}
		}
		else Toast.makeText(DCTimer.this, getString(R.string.path_not_exist), Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.clear();
		menu.add(Menu.NONE, 0, 0, getString(R.string.menu_inscr));
		menu.add(Menu.NONE, 1, 1, getString(R.string.menu_outscr));
		menu.add(Menu.NONE, 2, 2, getString(R.string.menu_share));
		menu.add(Menu.NONE, 3, 3, getString(R.string.menu_weibo));
		menu.add(Menu.NONE, 4, 4, getString(R.string.menu_about));
		menu.add(Menu.NONE, 5, 5, getString(R.string.menu_exit));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()) {
		case 0:
			LayoutInflater factory = LayoutInflater.from(DCTimer.this);
			final View view0 = factory.inflate(R.layout.inscr_layout, null);
			final Spinner sp = (Spinner) view0.findViewById(R.id.spnScrType);
			String[] items = getResources().getStringArray(R.array.inscrStr);
			ArrayAdapter<String> adap = new ArrayAdapter<String>(DCTimer.this, android.R.layout.simple_spinner_item, items);
			adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sp.setAdapter(adap);
			final EditText et0 = (EditText) view0.findViewById(R.id.edit_inscr);
			sp.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					insType = arg2;
				}
				public void onNothingSelected(AdapterView<?> arg0) {}
			});
			new AlertDialog.Builder(DCTimer.this).setView(view0).setTitle(getString(R.string.menu_inscr))
			.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int i) {
					final String scrs=et0.getText().toString();
					inScr = new ArrayList<String>();
					inScrLen = 0;
					setInScr(scrs);
					if(inScr.size()>0) newScr(false);
				}
			}).setNegativeButton(R.string.btn_cancel, null).show();
			break;
		case 1:
			final LayoutInflater factory1 = LayoutInflater.from(DCTimer.this);
			final View view1 = factory1.inflate(R.layout.outscr_layout, null);
			final EditText et1 = (EditText) view1.findViewById(R.id.edit_scrnum);
			final EditText et2 = (EditText) view1.findViewById(R.id.edit_scrpath);
			final Button btn = (Button)view1.findViewById(R.id.btn_browse);
			et2.setText(outPath);
			final EditText et3 = (EditText) view1.findViewById(R.id.edit_scrfile);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					selFilePath = et2.getText().toString();
					final View viewb = factory1.inflate(R.layout.file_selector, null);
					listView = (ListView)viewb.findViewById(R.id.list);
					File f = new File(selFilePath);
					selFilePath = f.exists()?selFilePath:Environment.getExternalStorageDirectory().getPath()+File.separator;
					getFileDir(selFilePath);
					listView.setOnItemClickListener(itemListener);
					new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.sel_path)).setView(viewb)
					.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int j) {
							et2.setText(selFilePath+"/");
						}
					}).setNegativeButton(getString(R.string.btn_cancel), null).show();
				}
			});
			new AlertDialog.Builder(DCTimer.this).setView(view1).setTitle(getString(R.string.menu_outscr)+"("+getScrName()+")")
			.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int i) {
					int numt = Integer.parseInt(et1.getText().toString());
					if(numt>100)numt=100;
					else if(numt<1)numt=5;
					final int num = numt;
					final String path=et2.getText().toString();
					if(!path.equals(outPath)) {
						outPath=path;
						edit.putString("scrpath", path);
						edit.commit();
					}
					final String fileName=et3.getText().toString();
					File file = new File(path+fileName);
					if(file.isDirectory())Toast.makeText(DCTimer.this, getString(R.string.path_illegal), Toast.LENGTH_SHORT).show();
					else if(file.exists()) {
						new AlertDialog.Builder(DCTimer.this).setMessage(getString(R.string.path_dupl))
						.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialoginterface, int j) {
								outScr(path, fileName, num);
							}
						}).setNegativeButton(getString(R.string.btn_cancel), null).show();
					} else {
						outScr(path, fileName, num);
					}
				}
			}).setNegativeButton(R.string.btn_cancel, null).show();
			break;
		case 2:
			Intent intent=new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");	//���ı�
			intent.putExtra(Intent.EXTRA_SUBJECT, "����");
			intent.putExtra(Intent.EXTRA_TEXT, getShareContext());
			startActivity(Intent.createChooser(intent, getTitle()));
			break;
		case 3:
			//savePic(takeScreenShot(DCTimer.this), addstr); TODO
			isShare = true;
			if(!isLogin) {
				auth();
			} else {
				WBShareActivity.text = getShareContext();
				WBShareActivity.bitmap = takeScreenShot(DCTimer.this);
				Intent it = new Intent(DCTimer.this, WBShareActivity.class);
				startActivity(it);
			}
			break;
		case 4:
			LayoutInflater factory2 = LayoutInflater.from(DCTimer.this);
			final View view = factory2.inflate(R.layout.dlg_about, null);
			new AlertDialog.Builder(DCTimer.this).setView(view)
			.setPositiveButton(getString(R.string.btn_upgrade), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					new Thread() {
						public void run() {
							handler.sendEmptyMessage(8);
							String ver = getContent("https://raw.github.com/MeigenChou/DCTimer/master/release/version.txt");
							if(ver.startsWith("error")) {
								handler.sendEmptyMessage(9);
							} else {
								String[] vers = ver.split("\t");
								int v = Integer.parseInt(vers[0]);
								if(v > verc) {
									newver = vers[1];
									StringBuilder sb = new StringBuilder();
									for(int i=2; i<vers.length; i++) sb.append(vers[i]+"\n");
									newupd = sb.toString();
									handler.sendEmptyMessage(11);
								}
								else handler.sendEmptyMessage(10);
							}
						}
					}.start();
				}
			})
			.setNegativeButton(getString(R.string.btn_close), null).show();
			break;
		case 5:
			cursor.close();
			dbh.close();
			edit.putInt("sel", spSel[0]);
			edit.putInt("sel2", spSel[6]);
			edit.commit();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		return true;
	}

	private String getContent(String strUrl) {
        try {
            URL url = new URL(strUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "GB2312"));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while((line = br.readLine()) != null) {
            	sb.append(line+"\t");
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            return "error open url:" + strUrl;
        }
    }
	
	private void download(final String fileName) {//TODO
		bytesum = 0;
		final File f = new File(defPath);
    	if(!f.exists()) f.mkdirs();
    	dlProg.show();
        new Thread() {
        	public void run() {
        		try {
                	URL url = new URL("https://raw.github.com/MeigenChou/DCTimer/master/release/"+fileName);
                	URLConnection conn = url.openConnection();
                	conn.connect();
                	InputStream is = conn.getInputStream();
                	int filesum = conn.getContentLength();
                	if(filesum == 0) {
                		handler.sendEmptyMessage(13);
                		dlProg.dismiss();
                		return;
                	}
                	dlProg.setMax(filesum / 1024);
                	FileOutputStream fs = new FileOutputStream(defPath+fileName);
                	byte[] buffer = new byte[2096];
                	int byteread;
                	while ((byteread = is.read(buffer)) != -1) {
                		bytesum += byteread;
                		fs.write(buffer, 0, byteread);
                		handler.sendEmptyMessage(12);
                	}
                	fs.close();
        		} catch (Exception e) {
        			handler.sendEmptyMessage(9);
        			dlProg.dismiss();
            		return;
        		}
        		dlProg.dismiss();
        		Intent intent = new Intent();
        		intent.setAction(android.content.Intent.ACTION_VIEW);
        		intent.setDataAndType(Uri.parse("file://"+defPath+fileName), "application/vnd.android.package-archive");
        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(intent);
        	}
        }.start();
	}

	private void getFileDir(String path) {
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File f = new File(path);
		File[] fs = f.listFiles();
		if(fs!=null && fs.length>0) Arrays.sort(fs);
		if(!path.equals("/")) {
			items.add("..");
			paths.add(f.getParent());
		}
		if(fs != null)
			for(int i=0; i<fs.length; i++) {
				File file = fs[i];
				if(file.isDirectory()) {
					items.add(file.getName());
					paths.add(file.getPath());
				}
			}
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		listView.setAdapter(fileList);
	}

	private void setGridView(boolean ch) {
		if(!isMulp) {
			aryAdapter = new TimesAdapter (DCTimer.this, times, new int[] {
					cl[1],cl[2],cl[3],cl[4]}, Mi.maxIdx, Mi.minIdx, intv);
			myGridView.setNumColumns(3);
		} else {
			aryAdapter = new TimesAdapter(DCTimer.this,	times, new int[]{cl[1],
					cl[2], cl[3], Mi.maxIdx, Mi.minIdx}, intv, stSel[3]+2);
			myGridView.setNumColumns(stSel[3]+2);
		}
		if(ch) myGridView.setStackFromBottom(false);
		else if(resl>40) myGridView.setStackFromBottom(true);
		else myGridView.setStackFromBottom(false);
		myGridView.setAdapter(aryAdapter);
	}

	private void setGvTitle() {
		if(isMulp) {
			String[] title = new String[stSel[3]+2];
			title[0] = getString(R.string.time);
			for(int i=1; i<stSel[3]+2; i++) title[i] = "P-"+i;
			TitleAdapter ta = new TitleAdapter(DCTimer.this, title, cl[1]);
			gvTitle.setNumColumns(stSel[3]+2);
			gvTitle.setAdapter(ta);
		}
		else {
			String[] title = {getString(R.string.time),
					(l1am ? "avg of " : "mean of ") + listnum[spSel[4]],
					(l2am ? "avg of " : "mean of ") + listnum[spSel[2]+1]};
			TitleAdapter ta = new TitleAdapter(DCTimer.this, title, cl[1]);
			gvTitle.setNumColumns(3);
			gvTitle.setAdapter(ta);
		}
	}

	private String getShareContext() {
		String s1 = getString(R.string.share_c1).replace("$len", ""+resl).replace("$scrtype", getScrName())
				.replace("$best", Mi.distime(Mi.minIdx, false)).replace("$mean", Mi.distime(Mi.sesMean));
		String s2 = (resl>listnum[spSel[4]])?getString(R.string.share_c2).replace("$flen", ""+listnum[spSel[4]]).
				replace("$favg", Mi.distime(Mi.bavg[0])):"";
		String s3 = (resl>listnum[spSel[2]+1])?getString(R.string.share_c2).replace("$flen", ""+listnum[spSel[2]+1]).
				replace("$favg", Mi.distime(Mi.bavg[1])):"";
		String s4 = getString(R.string.share_c3);
		return s1 + s2 + s3 + s4;
	}

	private String getScrName() {
		String[] mItems = getResources().getStringArray(R.array.cubeStr);
		String[] s = new String[0];
		switch(spSel[0]) {
		case 0:s=getResources().getStringArray(R.array.scr222);break;
		case 1:s=getResources().getStringArray(R.array.scr333);break;
		case 2:s=getResources().getStringArray(R.array.scr444);break;
		case 3:s=getResources().getStringArray(R.array.scr555);break;
		case 4:
		case 5:s=getResources().getStringArray(R.array.scr666);break;
		case 6:s=getResources().getStringArray(R.array.scrMinx);break;
		case 7:s=getResources().getStringArray(R.array.scrPrym);break;
		case 8:s=getResources().getStringArray(R.array.scrSq1);break;
		case 9:s=getResources().getStringArray(R.array.scrClk);break;
		case 10:s=getResources().getStringArray(R.array.scr15p);break;
		case 11:s=getResources().getStringArray(R.array.scrMxN);break;
		case 12:s=getResources().getStringArray(R.array.scrCmt);break;
		case 13:s=getResources().getStringArray(R.array.scrGear);break;
		case 14:s=getResources().getStringArray(R.array.scrSmc);break;
		case 15:s=getResources().getStringArray(R.array.scrSkw);break;
		case 16:s=getResources().getStringArray(R.array.scrOth);break;
		case 17:s=getResources().getStringArray(R.array.scr3sst);break;
		case 18:s=getResources().getStringArray(R.array.scrBdg);break;
		case 19:s=getResources().getStringArray(R.array.scrMsst);break;
		case 20:s=getResources().getStringArray(R.array.scrRly);break;
		}
		return mItems[spSel[0]] + "-" + s[spSel[6]];
	}

	private void searchSesType() {
		int type=0, idx=-1;
		for(int i=0; i<15; i++) {
			int s = sestp[i];
			if(type==0 && s==-1) {
				idx = i;
				type = 1;
			}
			if(s == scrType) {
				idx = i;
				type = 2;
				break;
			}
		}
		if(type==2 || (sestp[spSel[5]] != -1 && type == 1)) {
			spinner[5].setSelection(idx);
			spSel[5] = (byte) idx;
			getSession(idx);
			seMean.setText(getString(R.string.session_average) + Mi.sesMean());
			setGridView(true);
			spSel[5] = (byte) idx;
			edit.putInt("group", idx);
			edit.commit();
		}
	}

	private void setScrType() {
		switch(spSel[0]) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
			scrType = spSel[0]; break;
		case 11:
			if(spSel[6]<3) scrType=11;
			else if(spSel[6]<5) scrType=12;
			else if(spSel[6]<12) scrType=spSel[6]+8;
			else scrType = spSel[6]+37;
			break;
		case 12:
		case 13:
		case 14:
		case 15:
			scrType = spSel[0]+8; break;
		case 16:
			scrType = spSel[6]+24; break;
		case 17:
			scrType = 1; break;
		case 18:
			scrType = spSel[6]+30; break;
		case 19:
			scrType = 6; break;
		case 20:
			scrType = spSel[6]+32; break;
		}
	}

	private void setTimerFont(int f) {
		switch (f) {
		case 0: tvTimer.setTypeface(Typeface.create("monospace", 0)); break;
		case 1: tvTimer.setTypeface(Typeface.create("serif", 0)); break;
		case 2: tvTimer.setTypeface(Typeface.create("sans-serif", 0)); break;
		case 3: tvTimer.setTypeface(Typeface.createFromAsset(getAssets(), "Ds.ttf")); break;
		case 4: tvTimer.setTypeface(Typeface.createFromAsset(getAssets(), "Df.ttf")); break;
		case 5: tvTimer.setTypeface(Typeface.createFromAsset(getAssets(), "lcd.ttf")); break;
		}
	}

	private void auth() {
		mSsoHandler = new SsoHandler(DCTimer.this, mWeiboAuth);
        mSsoHandler.authorize(new AuthListener());
	}

	private void setTouch(MotionEvent e) {
		if(!simss || scrt) {
			switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touchDown();
				break;
			case MotionEvent.ACTION_UP:
				touchUp();
				break;
			}
		} else {
			int x1=0, x2=0;
			try {
				x1 = (int)e.getX(0)*2/tvTimer.getWidth();
				x2 = (int)e.getX(1)*2/tvTimer.getWidth();
			} catch (Exception ex) { }
			switch (e.getAction()) {
			case MotionEvent.ACTION_POINTER_1_DOWN:
			case MotionEvent.ACTION_POINTER_2_DOWN:
				if(e.getPointerCount()>1 && (x1^x2)==1) {
					touchDown();
					touchDown = true;
				}
				break;
			case MotionEvent.ACTION_POINTER_1_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
			case MotionEvent.ACTION_UP:
				if(touchDown) {
					touchDown = false;
					touchUp();
				}
				break;
			}
		}
	}

	private void touchDown() {
		if(timer.state == 1) {
			if(mulpCount != 0) {
				if(stSel[10]==1 || stSel[10]==3)
					vibrator.vibrate(vibTime[stSel[11]]);
				tvTimer.setTextColor(Color.GREEN);
				multemp[stSel[3]+1-mulpCount] = System.currentTimeMillis();
			}
			else {
				if(stSel[10]>1)
					vibrator.vibrate(vibTime[stSel[11]]);
				timer.count();
				if(isMulp) multemp[stSel[3]+1]=timer.time1;
				viewsVisibility(true);
			}
		} else if(timer.state != 3) {
			if(!scrt || timer.state==2) {
				if(frzTime == 0 || (wca && timer.state==0)) {
					tvTimer.setTextColor(Color.GREEN);
					canStart = true;
				} else {
					if(timer.state==0) tvTimer.setTextColor(Color.RED);
					else tvTimer.setTextColor(Color.YELLOW);
					timer.freeze();
				}
			}
		}
	}

	private void touchUp() {
		if(timer.state == 0) {
			if(isLongPress) isLongPress = false;
			else if(scrt) newScr(false);
			else {
				if(frzTime ==0 || canStart) {
					if(stSel[10]==1 || stSel[10]==3)
						vibrator.vibrate(vibTime[stSel[11]]);
					timer.count();
					if(isMulp) {
						mulpCount = stSel[3];
						multemp[0] = timer.time0;
					}
					else mulpCount = 0;
					acquireWakeLock(); screenOn=true;
					viewsVisibility(false);
				} else {
					timer.stopf();
					tvTimer.setTextColor(cl[1]);
				}
			}
		} else if(timer.state == 1) {	//TODO
			if(isLongPress) isLongPress = false;
			if(mulpCount!=0) {
				mulpCount--;
				tvTimer.setTextColor(cl[1]);
			}
		} else if(timer.state == 2) {
			if(isLongPress) isLongPress = false;
			if(frzTime ==0 || canStart) {
				isp2 = timer.insp==2 ? 2000 : 0;
				idnf = timer.insp != 3;
				if(stSel[10]==1 || stSel[10]==3)
					vibrator.vibrate(vibTime[stSel[11]]);
				timer.count();
				if(isMulp) multemp[0] = timer.time0;
				acquireWakeLock(); screenOn=true;
				viewsVisibility(false);
			} else {
				timer.stopf();
				tvTimer.setTextColor(Color.RED);
			}
		} else {
			if(isLongPress) isLongPress = false;
			if(!wca) {isp2=0; idnf=true;}
			//newScr(false);
			//mTextView2.setText(Mi.distime((int)timer.time));
			confirmTime((int)timer.time);
			timer.state = 0;
			if(!opnl) {releaseWakeLock(); screenOn=false;}
		}
	}

	private void inputTime(int action) {
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			tvTimer.setTextColor(Color.GREEN);
			break;
		case MotionEvent.ACTION_UP:
			tvTimer.setTextColor(cl[1]);
			LayoutInflater factory = LayoutInflater.from(DCTimer.this);
			final View view = factory.inflate(R.layout.editbox_layout, null);
			final EditText editText = (EditText) view.findViewById(R.id.editText1);
			editText.setFocusable(true);
			editText.requestFocus();
			new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.enter_time)).setView(view)
			.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String time = Mi.convStr(editText.getText().toString());
					if(time.equals("Error") || Mi.convTime(time)==0)
						Toast.makeText(DCTimer.this, getString(R.string.illegal), Toast.LENGTH_SHORT).show();
					save(Mi.convTime(time), (byte) 0);
					//setGridView(false);
					//seMean.setText(getString(R.string.session_average) + Mi.sesMean());
					//newScr(false);
				}
			}).setNegativeButton(getString(R.string.btn_cancel), null).show();
		}
	}

	private void save(int time, int p) {
		if(resl >= rest.length) {
			String[] scr2 = new String[scrst.length*2];
			byte[] rep2 = new byte[resp.length*2];
			int res2[] = new int[rest.length*2];
			for(int i=0; i<resl; i++) {
				scr2[i]=scrst[i]; rep2[i]=resp[i]; res2[i]=rest[i];
			}
			scrst=scr2; resp=rep2; rest=res2;
			if(isMulp) {
				int[][] mulp2 = new int[6][rest.length];
				for(int i=0;i<resl;i++)
					for(int j=0; j<6; j++)
						mulp2[j][i] = mulp[j][i];
				mulp = mulp2;
			}
			System.gc();
		}
		scrst[resl]=crntScr; resp[resl]=(byte) p; rest[resl++]=time;
		if(isMulp) {
			boolean temp = true;
			for(int i=0; i<stSel[3]+1; i++) {
				if(temp)
					mulp[i][resl-1] = (int)(multemp[i+1]-multemp[i]);
				else mulp[i][resl-1] = 0;
				if(mulp[i][resl-1]<0 || mulp[i][resl-1]>rest[resl-1]) {
					mulp[i][resl-1]=0; temp=false;
				}
			}
		}
		int d = 1;
		if(p==2) p=d=0;
		ContentValues cv = new ContentValues();
		cv.put("id", ++dbLastId);
		cv.put("rest", time);
		cv.put("resp", p);
		cv.put("resd", d);
		cv.put("scr", crntScr);
		cv.put("time", formatter.format(new Date()));
		if(isMulp)
			for(int i=0; i<6; i++)
				cv.put("p"+(i+1), mulp[i][resl-1]);
		dbh.insert(spSel[5], cv);
		if(isMulp) times = new String[(stSel[3]+2)*(resl+1)];
		else times = new String[resl*3];
		seMean.setText(getString(R.string.session_average) + Mi.sesMean());
		setGridView(false);
		if(selSes && sestp[spSel[5]] != scrType) {
			sestp[spSel[5]] = (short) scrType;
			edit.putInt("sestp"+spSel[5], scrType);
			edit.commit();
		}
		newScr(false);
	}
	private void update(int idx, byte p) {
		if(resp[idx] != p) {
			resp[idx] = p;
			byte d = 1;
			if(p==2) p=d=0;
			cursor = dbh.query(spSel[5]);
			cursor.moveToPosition(idx);
			int id=cursor.getInt(0);
			//cursor.close();
			dbh.update(spSel[5], id, p, d);
			seMean.setText(getString(R.string.session_average)+Mi.sesMean());
			setGridView(false);
		}
	}
	private void delete(int idx, int col) {
		int delId;
		if(idx != resl-1) {
			for(int i=idx; i<resl-1; i++) {
				rest[i]=rest[i+1]; resp[i]=resp[i+1]; scrst[i]=scrst[i+1];
				if(isMulp)
					for(int j=0; j<stSel[3]+1; j++)
						mulp[j][i] = mulp[j][i+1];
			}
			cursor.moveToPosition(idx);
			delId = cursor.getInt(0);
		} else {
			delId = dbLastId;
			if(resl > 1) {
				cursor.moveToPosition(resl-2);
				dbLastId = cursor.getInt(0);
			} else dbLastId = 0;
		}
		dbh.del(spSel[5], delId);
		//cursor.close();
		resl--;
		if(resl > 0) {
			if(isMulp) times = new String[(resl+1)*col];
			else times = new String[resl*col];
		}
		else {
			times = null;
			sestp[spSel[5]] = -1;
			edit.remove("sestp"+spSel[5]);
			edit.commit();
		}
		seMean.setText(getString(R.string.session_average) + Mi.sesMean());
		setGridView(false);
	}
	private void deleteAll() {
		dbh.clear(spSel[5]);
		resl = dbLastId = 0;
		times = null;
		seMean.setText(getString(R.string.session_average) + "0/0): N/A (N/A)");
		Mi.maxIdx = Mi.minIdx = -1;
		setGridView(false);
		if(sestp[spSel[5]] != -1) {
			sestp[spSel[5]] = -1;
			edit.remove("sestp"+spSel[5]);
			edit.commit();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(timer.state == 1) {
				timer.count();
				viewsVisibility(true);
				if(!wca) {isp2=0; idnf=true;}
				//newScr(false);
				confirmTime((int)timer.time);
				timer.state = 0;
				if(!opnl) {releaseWakeLock(); screenOn=false;}
			} else if(timer.state == 2) {
				timer.stopi();
				tvTimer.setText(stSel[2]==0 ? "0.00" : "0.000");
				viewsVisibility(true);
				if(!opnl) {releaseWakeLock(); screenOn=false;}
			} else if(event.getRepeatCount() == 0) {
				if((System.currentTimeMillis()-exitTime) > 2000) {
					Toast.makeText(DCTimer.this, getString(R.string.again_exit), Toast.LENGTH_SHORT).show();
					exitTime = System.currentTimeMillis();
				} else {
					edit.putInt("sel", spSel[0]);
					edit.putInt("sel2", spSel[6]);
					edit.commit();
		            finish();
		        }
			}
		}
		else if(keyCode == KeyEvent.KEYCODE_Q) chScr(8, 2);
		else if(keyCode == KeyEvent.KEYCODE_W) chScr(0, 0);
		else if(keyCode == KeyEvent.KEYCODE_E) chScr(1, 1);
		else if(keyCode == KeyEvent.KEYCODE_R) chScr(2, 0);
		else if(keyCode == KeyEvent.KEYCODE_T) chScr(3, 0);
		else if(keyCode == KeyEvent.KEYCODE_Y) chScr(4, 0);
		else if(keyCode == KeyEvent.KEYCODE_U) chScr(5, 0);
		else if(keyCode == KeyEvent.KEYCODE_M) chScr(6, 0);
		else if(keyCode == KeyEvent.KEYCODE_P) chScr(7, 0);
		else if(keyCode == KeyEvent.KEYCODE_K) chScr(9, 0);
		else if(keyCode == KeyEvent.KEYCODE_N) newScr(false);
		else if(keyCode == KeyEvent.KEYCODE_Z) {
			if(resl==0) Toast.makeText(DCTimer.this, getString(R.string.no_times), Toast.LENGTH_SHORT).show();
			else new AlertDialog.Builder(DCTimer.this).setMessage(getString(R.string.confirm_del_last))
			.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					cursor = dbh.query(spSel[5]);
					delete(resl-1, isMulp ? stSel[3]+2 : 3);
					dialog.dismiss();
				}
			}).setNegativeButton(R.string.btn_cancel, null).show();
		}
		else if(keyCode == KeyEvent.KEYCODE_A) {
			if(resl==0) Toast.makeText(DCTimer.this, getString(R.string.no_times), Toast.LENGTH_SHORT).show();
			else new AlertDialog.Builder(DCTimer.this).setMessage(getString(R.string.confirm_clear_session))
			.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {deleteAll();}
			}).setNegativeButton(R.string.btn_cancel, null).show();
		}
		else if(keyCode == KeyEvent.KEYCODE_D) {
			if(resl==0) Toast.makeText(DCTimer.this, getString(R.string.no_times), Toast.LENGTH_SHORT).show();
			else new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.show_time)+Mi.distime(resl-1, true)).setItems(R.array.rstcon,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0: update(resl-1, (byte) 0); break;
					case 1: update(resl-1, (byte) 1); break;
					case 2: update(resl-1, (byte) 2); break;
					}
				}
			}).setNegativeButton(getString(R.string.btn_cancel), null).show();
		}
		return false;
	}

	private void chScr(int s1, int s2) {
		boolean c1 = false, c2 = false;
		if(spSel[0] != s1) {
			c1 = true;
			spinner[0].setSelection(s1);
			spSel[0] = (byte) s1;
			if(spSel[0] != selold) selold = spSel[0];
		}
		if(spSel[6] != s2) {
			c2 = true;
			spSel[6] = (byte) s2;
		}
		if(c1 || c2) {
			set2ndsel();
			setScrType();
			if(selSes)searchSesType();
			if(inScr != null && inScr.size() != 0) inScr = null;
		}
	}

	private void showAlertDialog(int i, int j) {
		String t = null;
		switch(i) {
		case 1:
			t=(l1am?getString(R.string.sta_avg):getString(R.string.sta_mean)).replace("len", ""+listnum[spSel[4]]);
			slist=l1am?ao(listnum[spSel[4]], j):mo(listnum[spSel[4]], j);
			break;
		case 2:
			t=(l2am?getString(R.string.sta_avg):getString(R.string.sta_mean)).replace("len", ""+listnum[spSel[2]+1]);
			slist=l2am?ao(listnum[spSel[2]+1], j):mo(listnum[spSel[2]+1], j);
			break;
		case 3:
			t=getString(R.string.sta_session_mean);
			slist=sesMean();
			break;
		}
		new AlertDialog.Builder(DCTimer.this).setTitle(t).setMessage(slist)
		.setPositiveButton(getString(R.string.btn_copy), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				ClipboardManager clip=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
				clip.setText(slist);
				Toast.makeText(DCTimer.this, getString(R.string.copy_to_clip), Toast.LENGTH_SHORT).show();
			}
		}).setNeutralButton(getString(R.string.btn_save), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final LayoutInflater factory = LayoutInflater.from(DCTimer.this);
				final View view = factory.inflate(R.layout.save_stat, null);
				final EditText et1 = (EditText) view.findViewById(R.id.edit_scrpath);
				final Button btn = (Button)view.findViewById(R.id.btn_browse);
				et1.setText(outPath);
				final EditText et2 = (EditText) view.findViewById(R.id.edit_scrfile);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
				et2.setText(getString(R.string.def_sname).replace("$datetime", formatter.format(new Date())));
				btn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						selFilePath = et1.getText().toString();
						final View viewb = factory.inflate(R.layout.file_selector, null);
						listView = (ListView)viewb.findViewById(R.id.list);
						File f = new File(selFilePath);
						selFilePath = f.exists()?selFilePath:Environment.getExternalStorageDirectory().getPath()+File.separator;
						getFileDir(selFilePath);
						listView.setOnItemClickListener(itemListener);
						new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.sel_path)).setView(viewb)
						.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialoginterface, int j) {
								et1.setText(selFilePath+"/");
							}
						}).setNegativeButton(getString(R.string.btn_cancel), null).show();
					}
				});
				new AlertDialog.Builder(DCTimer.this).setView(view).setTitle(getString(R.string.stat_save))
				.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface di, int i) {
						final String path=et1.getText().toString();
						if(!path.equals(outPath)) {
							outPath=path;
							edit.putString("scrpath", path);
							edit.commit();
						}
						final String fileName=et2.getText().toString();
						File file = new File(path+fileName);
						if(file.isDirectory())Toast.makeText(DCTimer.this, getString(R.string.path_illegal), Toast.LENGTH_SHORT).show();
						else if(file.exists()) {
							new AlertDialog.Builder(DCTimer.this).setMessage(getString(R.string.path_dupl))
							.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialoginterface, int j) {
									outStat(path, fileName, slist);
								}
							}).setNegativeButton(getString(R.string.btn_cancel), null).show();
						} else outStat(path, fileName, slist);
					}
				}).setNegativeButton(R.string.btn_cancel, null).show();
			}
		}).setNegativeButton(getString(R.string.btn_close), null).show();
	}

	protected void newScr(final boolean ch) {
		if(!ch && inScr!=null && inScrLen<inScr.size()) {
			if(!isInScr)isInScr=true;
			crntScr=inScr.get(inScrLen++);
			switch (insType) {
			case 0:
				if(crntScr.matches("([FRU][2']?\\s*)+"))Mi.viewType=2;
				else if(crntScr.matches("([ULRBulrb]'?\\s*)+"))Mi.viewType=17;
				else if(crntScr.matches("([xFRUBLDMfrubld][2']?\\s*)+"))Mi.viewType=3;
				else if(crntScr.matches("(([FRUBLDfru]|[FRU]w)[2']?\\s*)+"))Mi.viewType=4;
				else if(crntScr.matches("(([FRUBLDfrubld]|([FRUBLD]w?))[2']?\\s*)+"))Mi.viewType=5;
				else if(crntScr.matches("(((2?[FRUBLD])|(3[FRU]w))[2']?\\s*)+"))Mi.viewType=6;
				else if(crntScr.matches("(((2|3)?[FRUBLD])[2']?\\s*)+"))Mi.viewType=7;
				else Mi.viewType=0;
				break;
			case 1:
				if(crntScr.matches("([FRUBLD][2']?\\s*)+"))Mi.viewType=2;
				else Mi.viewType=0;
				break;
			case 2:
				if(crntScr.matches("([xFRUBLDMfrubld][2']?\\s*)+"))Mi.viewType=3;
				else Mi.viewType=0;
				break;
			case 3:
				if(crntScr.matches("(([FRUBLDfru]|[FRU]w)[2']?\\s*)+"))Mi.viewType=4;
				else Mi.viewType=0;
				break;
			case 4:
				if(crntScr.matches("(([FRUBLDfrubld]|([FRUBLD]w?))[2']?\\s*)+"))Mi.viewType=5;
				else Mi.viewType=0;
				break;
			case 5:
				if(crntScr.matches("([ULRBulrb]'?\\s*)+"))Mi.viewType=17;
				else Mi.viewType=0;
			}
			if(Mi.viewType==3 && stSel[5]!=0) {
				new Thread() {
					public void run() {
						handler.sendEmptyMessage(6);
						if(stSel[5]==1)extsol="\n"+Cross.cross(crntScr, DCTimer.spSel[1], DCTimer.spSel[3]);
						else if(stSel[5]==2)extsol="\n"+Cross.xcross(crntScr, DCTimer.spSel[3]);
						else if(stSel[5]==3)extsol="\n"+EOline.eoLine(crntScr, DCTimer.spSel[3]);
						else if(stSel[5]==4)extsol="\n"+PetrusxRoux.roux(crntScr, DCTimer.spSel[3]);
						else if(stSel[5]==5)extsol="\n"+PetrusxRoux.petrus(crntScr, DCTimer.spSel[3]);
						handler.sendEmptyMessage(3);
					}
				}.start();
			}
			else tvScr.setText(crntScr);
		}
		else if((spSel[0]==0 && spSel[6]<3 && stSel[6]!=0) ||
			(spSel[0]==1 && (spSel[6]!=0 || (stSel[5]!=0 && (spSel[6]==0 || spSel[6]==1 || spSel[6]==5 || spSel[6]==19)))) ||
			(spSel[0]==8 && (spSel[6]>1 || (spSel[6]<3 && stSel[12]>0))) ||
			(spSel[0]==11 && (spSel[6]>3 && spSel[6]<7)) ||
			(spSel[0]==17 && (spSel[6]<3 || spSel[6]==6)) ||
			spSel[0]==20) {
			if(isInScr)isInScr=false;
			if(ch)canScr=true;
			if(canScr) {
				new Thread() {
					public void run() {
						canScr=false;
						handler.sendEmptyMessage(2);
						if(!ch) {
							//TODO
							if(nextScr==null || isChScr) {
								crntScr = Mi.SetScr((spSel[0]<<5)|spSel[6], ch);
								isChScr=false;
								nextScr="";
							} else {
								while (!isNextScr) {
									try {
										sleep(100);
									} catch (InterruptedException e) {}
								}
								crntScr = nextScr;
							}
						}
						else {
							crntScr = Mi.SetScr((spSel[0]<<5)|spSel[6], ch);
						}
						extsol = Mi.sc;
						//crntScr=(!ch && isNextScr)?nextScr:Mi.SetScr((spSel[0]<<5)|spSel[6]);
						if((spSel[0]==0 && stSel[6]!=0) ||
								(stSel[5]!=0 && spSel[0]==1 && (spSel[6]==0 || spSel[6]==1 || spSel[6]==5 || spSel[6]==19)))
							handler.sendEmptyMessage(3);
						else if(spSel[0]==8 && spSel[6]<3 && stSel[12]>0)handler.sendEmptyMessage(1);
						else handler.sendEmptyMessage(0);
						canScr=true;
						isNextScr = false;
						nextScr = Mi.SetScr((spSel[0]<<5)|spSel[6], ch);
						isNextScr = true;
						System.out.println(nextScr);
					}
				}.start();
			}
		} else {
			crntScr=Mi.SetScr(spSel[0]<<5|spSel[6], ch);
			tvScr.setText(crntScr);
		}
	}

	public void confirmTime(final int time) {
		if(idnf) {
			if(conft)
				new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.show_time)+Mi.distime(time + isp2)).
						setItems(R.array.rstcon, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:save(time + isp2, 0);break;
						case 1:save(time + isp2, 1);break;
						case 2:save(time + isp2, 2);break;
						}
					}
				})
				.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d,int which) {
						d.dismiss();
						newScr(false);
					}
				}).show();
			else save(time + isp2, 0);
		}
		else {
			if(conft)
				new AlertDialog.Builder(DCTimer.this).setTitle(getString(R.string.time_dnf)).setMessage(getString(R.string.confirm_adddnf))
				.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int j) {
						save((int)timer.time, 2);
					}
				}).setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d,int which) {
						d.dismiss();
						newScr(false);
					}
				}).show();
			else save((int)timer.time, 2);
		}
	}

	public String sesMean() {
		StringBuffer sb=new StringBuffer();
		int n=resl;
		for(int i=0;i<resl;i++)
			if(resp[i]==2) n--;
		sb.append(getString(R.string.stat_title)+new java.sql.Date(new Date().getTime())+"\r\n");
		sb.append(getString(R.string.stat_solve)+n+"/"+resl+"\r\n");
		sb.append(getString(R.string.ses_mean)+Mi.distime(Mi.sesMean)+" ");
		sb.append("(�� = "+Mi.standDev(Mi.sesSD)+")\r\n");
		sb.append(getString(R.string.ses_avg)+Mi.sesAvg()+"\r\n");
		if(resl >= listnum[spSel[4]] && Mi.bidx[0] != -1) 
			sb.append((l1am ? getString(R.string.stat_best_avg) : getString(R.string.stat_best_mean)).replace("len", ""+listnum[spSel[4]])+Mi.distime(Mi.bavg[0])+"\r\n");
		if(resl >= listnum[spSel[2]+1] && Mi.bidx[1] != -1) 
			sb.append((l2am ? getString(R.string.stat_best_avg) : getString(R.string.stat_best_mean)).replace("len", ""+listnum[spSel[2]+1])+Mi.distime(Mi.bavg[1])+"\r\n");
		sb.append(getString(R.string.stat_best)+Mi.distime(Mi.minIdx, false)+"\r\n");
		sb.append(getString(R.string.stat_worst)+Mi.distime(Mi.maxIdx, false)+"\r\n");
		sb.append(getString(R.string.stat_list));
		if(hidls)sb.append("\r\n");
		cursor = dbh.query(spSel[5]);
		for(int i=0;i<resl;i++) {
			if(!hidls)sb.append("\r\n"+(i+1)+". ");
			sb.append(Mi.distime(i, true));
			cursor.moveToPosition(i);
			String s = cursor.getString(6);
			if(s!=null && !s.equals(""))sb.append("["+s+"]");
			if(hidls && i<resl-1)sb.append(", ");
			if(!hidls)sb.append("  "+scrst[i]);
		}
		return sb.toString();
	}

	public String ao(int n, int i) {
		int cavg=0, csdv=-1, ind=1;
		int trim = (int) Math.ceil(n/20.0);
		int max, min;
		ArrayList<Integer> dnfIdx=new ArrayList<Integer>();
		ArrayList<Integer> midx = new ArrayList<Integer>();
		for(int j=i-n+1;j<=i;j++)
			if(resp[j]==2)
				dnfIdx.add(j);
		int dnf = dnfIdx.size();
		int[] data=new int[n-dnf];
		int[] idx=new int[n-dnf];
		int len=0;
		for(int j=i-n+1;j<=i;j++)
			if(resp[j]!=2) {
				data[len]=rest[j]+resp[j]*2000;
				idx[len++]=j;
			}
		quickSort(data, idx, 0, n-dnf-1);
		if(n-dnf >= trim) {
			for(int j=0; j<trim; j++) midx.add(idx[j]);
		} else {
			for(int j=0; j<data.length; j++) midx.add(idx[j]);
			for(int j=0; j<trim-n+dnf; j++) midx.add(dnfIdx.get(j));
		}
		boolean m = dnf>trim;
		min = midx.get(0);
		if(m) {
			for(int j=dnf-trim; j<dnf; j++) midx.add(dnfIdx.get(j));
		} else {
			for(int j=n-trim; j<n-dnf; j++) midx.add(idx[j]);
			for(int j=0; j<dnf; j++) midx.add(dnfIdx.get(j));
			double sum=0, sum2=0;
			for(int j=trim;j<n-trim;j++) {
				if(stSel[2]==1)sum+=data[j];
				else sum+=(data[j]+5)/10;
				if(stSel[2]==1)sum2+=Math.pow(data[j], 2);
				else sum2+=Math.pow((data[j]+5)/10, 2);
			}
			cavg=(int) (sum/(n-trim*2)+0.5);
			csdv=(int) Math.sqrt(sum2/(n-trim*2)-sum*sum/Math.pow(n-trim*2, 2));
			if(stSel[2]==0)cavg*=10;
		}
		max = midx.get(midx.size()-1);
		StringBuffer sb=new StringBuffer();
		sb.append(getString(R.string.stat_title)+new java.sql.Date(new Date().getTime())+"\r\n");
		sb.append(getString(R.string.stat_avg)+(m?"DNF":Mi.distime(cavg))+" ");
		sb.append("(�� = "+Mi.standDev(csdv)+")\r\n");
		sb.append(getString(R.string.stat_best)+Mi.distime(min,false)+"\r\n");
		sb.append(getString(R.string.stat_worst)+Mi.distime(max,false)+"\r\n");
		sb.append(getString(R.string.stat_list));
		if(hidls)sb.append("\r\n");
		cursor = dbh.query(spSel[5]);
		for(int j=i-n+1;j<=i;j++) {
			cursor.moveToPosition(j);
			String s = cursor.getString(6);
			if(!hidls)sb.append("\r\n"+(ind++)+". ");
			if(midx.indexOf(j)>-1)sb.append("(");
			sb.append(Mi.distime(j, false));
			if(s!=null && !s.equals(""))sb.append("["+s+"]");
			if(midx.indexOf(j)>-1)sb.append(")");
			if(hidls && j<i)sb.append(", ");
			if(!hidls)sb.append("  "+scrst[j]);
		}
		return sb.toString();
	}

	private void quickSort(int[] a, int[] idx, int lo, int hi) {
		if(lo >= hi) return;
		int pivot = a[lo], i = lo, j = hi;
		int temp = idx[lo];
		while(i < j) {
			while(i<j && a[j]>=pivot) j--;
			a[i] = a[j];
			idx[i] = idx[j];
			while(i<j && a[i]<=pivot) i++;
			a[j] = a[i];
			idx[j] = idx[i];
		}
		a[i] = pivot;
		idx[i] = temp;
		quickSort(a, idx, lo, i-1);
		quickSort(a, idx, i+1, hi);
	}

	public String mo(int n, int i) {
		StringBuffer sb=new StringBuffer();
		int max, min, dnf=0;
		int cavg=0, csdv=-1, ind=1;
		double sum=0, sum2=0;
		max=min=i-n+1;
		boolean m=false;
		for(int j=i-n+1; j<=i; j++) {
			if(resp[j]!=2 && !m) {min=j; m=true;}
			if(resp[j]==2) {max=j; dnf++;}
		}
		m = dnf > 0;
		if(!m) {
			for (int j=i-n+1;j<=i;j++) {
				if(rest[j]+resp[j]*2000>rest[max]+resp[max]*2000)max=j;
				if(rest[j]+resp[j]*2000<=rest[min]+resp[min]*2000)min=j;
				if(stSel[2]==1)sum+=(double)(rest[j]+resp[j]*2000);
				else sum+=(rest[j]+resp[j]*2000+5)/10;
				if(stSel[2]==1)sum2+=Math.pow(rest[j]+resp[j]*2000, 2);
				else sum2+=Math.pow((rest[j]+resp[j]*2000+5)/10, 2);
			}
			cavg=(int) (sum/n+0.5);
			csdv=(int) (Math.sqrt(sum2/n-sum*sum/n/n)+(stSel[2]==1?0:0.5));
		}
		if(stSel[2]==0)cavg*=10;
		sb.append(getString(R.string.stat_title)+new java.sql.Date(new Date().getTime())+"\r\n");
		sb.append(getString(R.string.stat_mean)+(m?"DNF":Mi.distime(cavg))+" ");
		sb.append("(�� = "+Mi.standDev(csdv)+")\r\n");
		sb.append(getString(R.string.stat_best)+Mi.distime(min,false)+"\r\n");
		sb.append(getString(R.string.stat_worst)+Mi.distime(max,false)+"\r\n");
		sb.append(getString(R.string.stat_list));
		if(hidls)sb.append("\r\n");
		cursor = dbh.query(spSel[5]);
		for(int j=i-n+1;j<=i;j++) {
			cursor.moveToPosition(j);
			if(!hidls)sb.append("\r\n"+(ind++)+". ");
			sb.append(Mi.distime(j, false));
			String s = cursor.getString(6);
			if(s!=null && !s.equals(""))sb.append("["+s+"]");
			if(hidls && j<i)sb.append(", ");
			if(!hidls)sb.append("  "+scrst[j]);
		}
		return sb.toString();
	}

	private Bitmap getBgPic(Bitmap bitmap) {
		int width = dm.widthPixels;
		int height=dm.heightPixels;
		float scaleWidth=(float)bitmap.getWidth()/width;
		float scaleHeight=(float)bitmap.getHeight()/height;
		float scale = Math.min(scaleWidth, scaleHeight);
		Matrix matrix = new Matrix();
		matrix.postScale(1/scale, 1/scale);
		return Bitmap.createBitmap(bitmap, (int)((bitmap.getWidth()-width*scale)/2),
				(int)((bitmap.getHeight()-height*scale)/2), (int)(width*scale), (int)(height*scale), matrix, true);
	}

	private void setBgPic(Bitmap scaleBitmap, int opa) {
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawColor(0);
		Paint paint = new Paint();
		canvas.drawBitmap(scaleBitmap, 0, 0, paint);
		paint.setColor(Color.WHITE);
		paint.setAlpha(255-255*opa/100);
		canvas.drawRect(0, 0, width, height, paint);
		//return newBitmap;
		Drawable drawable =new BitmapDrawable(newBitmap);
		tabHost.setBackgroundDrawable(drawable);
	}

	private void acquireWakeLock() {
		if(!opnd) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		else if (wakeLock ==null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
			wakeLock.acquire();
		}
	}

	private void releaseWakeLock() {
		if(!opnd) getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (wakeLock != null&& wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	private void getSession(int i) {
		cursor = dbh.query(i);
		resl = cursor.getCount();
		rest = new int[resl+12];
		resp = new byte[resl+12];
		scrst = new String[resl+12];
		if(isMulp) mulp = new int[6][resl+12];
		if(resl != 0) {
			cursor.moveToFirst();
			for(int k=0; k<resl; k++) {
				rest[k] = cursor.getInt(1);
				resp[k] = (byte) cursor.getInt(2);
				if(cursor.getInt(3) == 0) resp[k]=2;
				scrst[k] = cursor.getString(4);
				if(isMulp)
					for(int j=0; j<6; j++)
						mulp[j][k] = cursor.getInt(7+j);
				cursor.moveToNext();
			}
			cursor.moveToLast();
			dbLastId = cursor.getInt(0);
			times = isMulp ? new String[(stSel[3]+2)*(resl+1)] : new String[resl*3];
		} else {
			times = null;
			dbLastId = 0;
		}
		//cursor.close();
	}

	private void singTime(final int p, final int col) {
		cursor = dbh.query(spSel[5]);
		cursor.moveToPosition(p/col);
		final int id = cursor.getInt(0);
		String time=cursor.getString(5);
		String n=cursor.getString(6);
		if(n==null) n="";
		final String note = n;
		if(time!=null) time="\n("+time+")";
		else time = "";
		LayoutInflater factory = LayoutInflater.from(DCTimer.this);
		final View view = factory.inflate(R.layout.singtime, null);
		final EditText editText=(EditText) view.findViewById(R.id.etnote);
		final TextView tvTime=(TextView) view.findViewById(R.id.st_time);
		final TextView tvScr=(TextView) view.findViewById(R.id.st_scr);
		tvTime.setText(getString(R.string.show_time)+Mi.distime(p/col,true)+time);
		tvScr.setText(scrst[p/col]);
		if(resp[p/col]==2) {
			RadioButton rb = (RadioButton) view.findViewById(R.id.st_pe3);
			rb.setChecked(true);
		} else if(resp[p/col]==1) {
			RadioButton rb = (RadioButton) view.findViewById(R.id.st_pe2);
			rb.setChecked(true);
		} else {
			RadioButton rb = (RadioButton) view.findViewById(R.id.st_pe1);
			rb.setChecked(true);
		}
		if(!note.equals("")) editText.setText(note);
		new AlertDialog.Builder(DCTimer.this).setView(view)
		.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				RadioGroup rg = (RadioGroup) view.findViewById(R.id.st_penalty);
				int rgid = rg.getCheckedRadioButtonId();
				switch(rgid) {
				case R.id.st_pe1: update(p/col, (byte)0); break;
				case R.id.st_pe2: update(p/col, (byte)1); break;
				case R.id.st_pe3: update(p/col, (byte)2); break;
				}
				String text = editText.getText().toString();
				if(!text.equals(note)) {
					dbh.update(spSel[5], id, text);
					//setGridView(false);
				}
			}
		}).setNeutralButton(getString(R.string.copy_scr), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clip.setText(scrst[p/col]);
				Toast.makeText(DCTimer.this, getString(R.string.copy_to_clip), Toast.LENGTH_SHORT).show();
			}
		}).setNegativeButton(getString(R.string.delete_time), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int which) {
				delete(p/col, col);
				d.dismiss();
			}
		}).show();
	}

	private Bitmap takeScreenShot(Activity activity) {
		//View������Ҫ��ͼ��View
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();
		//��ȡ״̬���߶�
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		System.out.println(statusBarHeight);
		//��ȡ��Ļ���͸�
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		int height = activity.getWindowManager().getDefaultDisplay().getHeight();
		//ȥ��������
		//Bitmap bm = Bitmap.createBitmap(b1, 0, 25, 320, 455);
		Bitmap bm = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
		view.destroyDrawingCache();
		return bm;
	}

	void savePic(Bitmap b, String strFileName) {
		try {
			FileOutputStream fos = new FileOutputStream(strFileName);
			if (null != fos) {
				b.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
			}
		} catch (IOException e) {}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//System.out.println(requestCode);
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				try {
					Uri uri = data.getData();
					Cursor c = getContentResolver().query(uri, null, null, null, null);
					c.moveToFirst();
					picPath = c.getString(1);
					ContentResolver cr = this.getContentResolver();
					bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
					bitmap = getBgPic(bitmap);
					setBgPic(bitmap, share.getInt("opac", 35));
					bgcolor = false;
					edit.putString("picpath", picPath);
					edit.putBoolean("bgcolor", false); edit.commit();
					c.close();
				} catch (Exception e) {
				} catch (OutOfMemoryError e) {Toast.makeText(DCTimer.this, "Out of memory error: bitmap size exceeds VM budget", Toast.LENGTH_SHORT).show();}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
	        // SSO ��Ȩ�ص�
	        // ��Ҫ������ SSO ��½�� Activity ������д onActivityResult
	        if (mSsoHandler != null) {
	            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
	        }
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent e) {
		float z = e.values[SensorManager.DATA_Z];
		lowZ = z * 0.9f + lowZ * 0.1f;
		float highZ = z - lowZ;
		if(drop && timer.time > 300 && Math.abs(highZ*1000)-30 > sensity && timer.state == 1) {
			timer.count();
			viewsVisibility(true);
			if(!wca) {isp2=0; idnf=true;}
			confirmTime((int) timer.time);
			timer.state = 0;
			if(!opnl) {releaseWakeLock(); screenOn = false;}
		}
	}

	/*
     * ΢����֤��Ȩ�ص��ࡣ
     * 1. SSO ��Ȩʱ����Ҫ�� {@link #onActivityResult} �е��� {@link SsoHandler#authorizeCallBack} ��
     *    �ûص��Żᱻִ�С�
     * 2. �� SSO ��Ȩʱ������Ȩ�����󣬸ûص��ͻᱻִ�С�
     * ����Ȩ�ɹ����뱣��� access_token��expires_in��uid ����Ϣ�� SharedPreferences �С�
     */
    class AuthListener implements WeiboAuthListener {
        
        @Override
        public void onComplete(Bundle values) {
            // �� Bundle �н��� Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                // ��ʾ Token
            	String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                        new java.util.Date(mAccessToken.getExpiresTime()));
                String format = "Token��%1$s \n��Ч�ڣ�%2$s";
                Toast.makeText(DCTimer.this, String.format(format, mAccessToken.getToken(), date), Toast.LENGTH_LONG).show();
                
                // ���� Token �� SharedPreferences
                AccessTokenKeeper.writeAccessToken(DCTimer.this, mAccessToken);
                Toast.makeText(DCTimer.this, getString(R.string.auth_success), Toast.LENGTH_SHORT).show();
                if(isShare) {
                	WBShareActivity.text = getShareContext();
    				WBShareActivity.bitmap = takeScreenShot(DCTimer.this);
    				Intent it = new Intent(DCTimer.this, WBShareActivity.class);
    				startActivity(it);
                }
            } else {
                // ����ע���Ӧ�ó���ǩ������ȷʱ���ͻ��յ� Code����ȷ��ǩ����ȷ
                String code = values.getString("code");
                String message = getString(R.string.auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message += "\nObtained the code: " + code;
                }
                Toast.makeText(DCTimer.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(DCTimer.this, getString(R.string.auth_cancel), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(DCTimer.this, "Auth exception: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}