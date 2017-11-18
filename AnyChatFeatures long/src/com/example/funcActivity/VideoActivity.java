package com.example.funcActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatRecordEvent;
import com.bairuitech.anychat.AnyChatVideoCallEvent;
import com.bairuitech.anychat.AnyChatTextMsgEvent;
import com.example.common.BaseMethod;
import com.example.common.CustomApplication;
import com.example.common.DialogFactory;
import com.example.anychatfeatures.FuncMenu;
import com.example.anychatfeatures.MessageListView;
import com.example.anychatfeatures.R;

import com.example.anychatfeatures.OnDismissCallback;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.test.btclient.BTClient;
import com.test.btclient.DeviceListActivity;
import com.topeet.serialtest.serial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;  

public class VideoActivity extends Activity implements AnyChatBaseEvent,
		AnyChatRecordEvent, AnyChatVideoCallEvent, AnyChatTextMsgEvent {

	// handle send msg 
	private static final int MSG_VIDEOGESPREK = 1;	// 视频对话时间刷新消息
	private static final int MSG_PREVIEWPIC = 2;    // 拍照预览倒计时刷新消息
	private final int UPDATEVIDEOBITDELAYMILLIS = 200; //监听音频视频的码率的间隔刷新时间（毫秒）
	
	private final String mStrBasePath = "/AnyChat"; 
	
	private TextView mPreviewFilePath;		   // 用于显示拍照的相片和本地录制视频的存储路径的显示
	
	int userID;
	private boolean bSelfVideoOpened = false;  // 本地视频是否已打开
	private boolean bOtherVideoOpened = false; // 对方视频是否已打开
	private int mVideogesprekSec = 0;		   // 音视频对话的时间
	private Boolean mFirstGetVideoBitrate = false; //"第一次"获得视频码率的标致
	private Boolean mFirstGetAudioBitrate = false; //"第一次"获得音频码率的标致
	
	private SurfaceView mOtherView;
	private SurfaceView mMyView;
	private ImageButton mImgBtnReturn;		  // 返回
	private TextView mTitleName;			  // 标题
	private ImageButton mImgSwitchVideo;	  // 切换设备前后摄像头
	private Button mEndCallBtn;
	
	
	private ImageButton upbtnButton;                //测试文字传输
	private ImageButton downbtnButton; 
	private ImageButton leftbtnButton; 
	private ImageButton rightbtnButton; 
	
	private Button mstop; 
	
	private ImageButton mbluetooth;
	
	private ImageButton mBtnCameraCtrl;		  // 控制视频的按钮
	private ImageButton mBtnSpeakCtrl; 		  // 控制音频的按钮
	private CustomApplication mCustomApplication;
	private Dialog mDialog;
	private TextView mVideogesprekTimeTV;  	  // 视频对话时间
	private Timer mVideogesprekTimer;	
	
	private TimerTask mTimerTask;
	private Handler mHandler;

	public AnyChatCoreSDK anyChatSDK;	
	
	//------------------------------拍照--
	private String mPreviewPicPathStr = "";  // 拍照图片存储路径
	private ImageView mPreviewPicIV;		 // 拍照图片预览view
	private ImageButton mIBTakePhotoSelf;	 // 自拍
	private ImageButton mIBTakePhotoOther;   // 拍照	
	private Timer mPreviewPicTimer = null;
	private int mPreviewPicSec = 0;			   // 预览图片的剩余时间
	//--拍照------------------------------
    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
	
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
	
	private InputStream is;    //输入流，用来接收蓝牙数据
    private TextView dis;       //接收数据显示句柄
    private ScrollView sv;      //翻页句柄
    private String smsg = "";    //显示用数据缓存
    private String fmsg = "";    //保存用数据缓存
    public String filename=""; //用来保存存储的文件名
    BluetoothDevice _device = null;     //蓝牙设备
    BluetoothSocket _socket = null;      //蓝牙通信socket
    boolean _discoveryFinished = false;    
    boolean bRun = true;
    boolean bThread = false;
   // serial com3 = new serial();
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();  
	
	private ArrayList<String> mMessageList = new ArrayList<String>(); // 存储已发送的信息
	
 private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();  //蓝牙
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.video_frame);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.titlebar);

		Intent intent = getIntent();
		userID = Integer.parseInt(intent.getStringExtra("UserID"));
        
        
       // com3.Open(3, 115200);//串口
		InitSDK();
		InitLayout();
		updateTime();
		//buttonstye();//悬浮按钮
		//如果视频流过来了，则把背景设置成透明的
		handler.postDelayed(runnable, UPDATEVIDEOBITDELAYMILLIS);
		
		if (_bluetooth == null){
        	Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // 设置设备可以被搜索  
       new Thread(){
    	   public void run(){
    		   if(_bluetooth.isEnabled()==false){
        		_bluetooth.enable();
    		   }
    	   }   	   
       }.start();      
		
	}

	private void InitSDK() {
		anyChatSDK = AnyChatCoreSDK.getInstance(this);
		anyChatSDK.SetBaseEvent(this);
		anyChatSDK.SetTextMessageEvent(this);
		anyChatSDK.SetVideoCallEvent(this);
		anyChatSDK.SetRecordSnapShotEvent(this);
		anyChatSDK.mSensorHelper.InitSensor(this);
		
		AnyChatCoreSDK.mCameraHelper.SetContext(this);

		// 拍照存储路径
		anyChatSDK.SetSDKOptionString(AnyChatDefine.BRAC_SO_SNAPSHOT_TMPDIR,
				Environment.getExternalStorageDirectory() + mStrBasePath
						+ "/Photo");
	}

	private void InitLayout() {
		mMyView = (SurfaceView) findViewById(R.id.surface_local);
		mOtherView = (SurfaceView) findViewById(R.id.surface_remote);
		mImgBtnReturn = (ImageButton) this.findViewById(R.id.returnImgBtn);
		mTitleName = (TextView) this.findViewById(R.id.titleName);
		mImgSwitchVideo = (ImageButton) findViewById(R.id.ImgSwichVideo);
		mEndCallBtn = (Button) findViewById(R.id.endCall);
		mBtnSpeakCtrl = (ImageButton) findViewById(R.id.btn_speakControl);
		mBtnCameraCtrl = (ImageButton) findViewById(R.id.btn_cameraControl);
		mVideogesprekTimeTV = (TextView) findViewById(R.id.videogesprekTime);
		mPreviewPicIV = (ImageView) findViewById(R.id.previewPhoto);
		mPreviewFilePath = (TextView) findViewById(R.id.previewFilePath);
		mIBTakePhotoSelf = (ImageButton) findViewById(R.id.btn_TakePhotoSelf);
		mIBTakePhotoOther = (ImageButton) findViewById(R.id.btn_TakePhotoOther);
		mbluetooth =(ImageButton) findViewById(R.id.bluetooth);
		
		leftbtnButton =(ImageButton) findViewById(R.id.leftbutton); //文字
		rightbtnButton=(ImageButton) findViewById(R.id.rightbutton);
	    upbtnButton=(ImageButton) findViewById(R.id.upbutton);
		downbtnButton=(ImageButton) findViewById(R.id.downbutton);
		mstop=(Button) findViewById(R.id.stop);
		
		mTitleName.setText("与 \"" + anyChatSDK.GetUserName(userID) + "\" 对话中");
		mImgSwitchVideo.setVisibility(View.VISIBLE);

		mCustomApplication = (CustomApplication) getApplication();
		if (mCustomApplication.getCurOpenFuncUI() == FuncMenu.FUNC_VOICEVIDEO) {
			mIBTakePhotoSelf.setVisibility(View.GONE);
			mIBTakePhotoOther.setVisibility(View.GONE);
		} else if (mCustomApplication.getCurOpenFuncUI() == FuncMenu.FUNC_PHOTOGRAPH) {
			mBtnSpeakCtrl.setVisibility(View.GONE);
			mBtnCameraCtrl.setVisibility(View.GONE);

		} else if (mCustomApplication.getCurOpenFuncUI() == FuncMenu.FUNC_VIDEOCALL) {
			mIBTakePhotoSelf.setVisibility(View.GONE);
			mIBTakePhotoOther.setVisibility(View.GONE);
		}
		leftbtnButton.setOnClickListener(onClickListener);  //绑定点击事件
		rightbtnButton.setOnClickListener(onClickListener);
		upbtnButton.setOnClickListener(onClickListener);
		downbtnButton.setOnClickListener(onClickListener);
		mbluetooth.setOnClickListener(onClickListener);
		mstop.setOnClickListener(onClickListener);
		
		mImgBtnReturn.setOnClickListener(onClickListener);
		mBtnSpeakCtrl.setOnClickListener(onClickListener);
		mBtnCameraCtrl.setOnClickListener(onClickListener);
		mImgSwitchVideo.setOnClickListener(onClickListener);
		mEndCallBtn.setOnClickListener(onClickListener);
		mIBTakePhotoSelf.setOnClickListener(onClickListener);
		mIBTakePhotoOther.setOnClickListener(onClickListener);
		mPreviewPicIV.setOnClickListener(onClickListener);
		// 如果是采用Java视频采集，则需要设置Surface的CallBack
		/*if (AnyChatCoreSDK
				.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
			mMyView.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);
		}*/

		// 如果是采用Java视频显示，则需要设置Surface的CallBack
		if (AnyChatCoreSDK
				.GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
			int index = anyChatSDK.mVideoHelper.bindVideo(mOtherView
					.getHolder());
			anyChatSDK.mVideoHelper.SetVideoUser(index, userID);
		}

		mMyView.setZOrderOnTop(true);

		anyChatSDK.UserCameraControl(userID, 1);
		anyChatSDK.UserSpeakControl(userID, 0);//语音控制

		// 判断是否显示本地摄像头切换图标
		if (AnyChatCoreSDK
				.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
			if (AnyChatCoreSDK.mCameraHelper.GetCameraNumber() > 1) {
				// 默认打开后置摄像头
				AnyChatCoreSDK.mCameraHelper
						.SelectVideoCapture(AnyChatCoreSDK.mCameraHelper.CAMERA_FACING_BACK);
			}
		} else {
			String[] strVideoCaptures = anyChatSDK.EnumVideoCapture();
			if (strVideoCaptures != null && strVideoCaptures.length > 1) {
				// 默认打开前置摄像头
				for (int i = 0; i < strVideoCaptures.length; i++) {
					String strDevices = strVideoCaptures[i];
					if (strDevices.indexOf("back") >= 0) {
						anyChatSDK.SelectVideoCapture(strDevices);
						break;
					}
				}
			}
		}

		// 根据屏幕方向改变本地surfaceview的宽高比
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			adjustLocalVideo(true);
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			adjustLocalVideo(false);
		}

		anyChatSDK.UserCameraControl(-1, 0);// -1表示对本地视频进行控制，打开本地视频
		anyChatSDK.UserSpeakControl(-1, 1);// -1表示对本地音频进行控制，打开本地音频

	}
	
	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			try {
				int videoBitrate = anyChatSDK.QueryUserStateInt(userID, AnyChatDefine.BRAC_USERSTATE_VIDEOBITRATE);
				int audioBitrate = anyChatSDK.QueryUserStateInt(userID, AnyChatDefine.BRAC_USERSTATE_AUDIOBITRATE);
				if (videoBitrate > 0)
				{
					//handler.removeCallbacks(runnable);
					mFirstGetVideoBitrate = true;
					mOtherView.setBackgroundColor(Color.TRANSPARENT);
				}
				
				if(audioBitrate > 0){
					mFirstGetAudioBitrate = true;
				}
				
				if (mFirstGetVideoBitrate)
				{
					if (videoBitrate <= 0){						
						Toast.makeText(VideoActivity.this, "对方视频中断了!", Toast.LENGTH_SHORT).show();
						// 重置下，如果对方退出了，有进去了的情况
					
						mFirstGetVideoBitrate = false;
					}
				}
				
				if (mFirstGetAudioBitrate){
					if (audioBitrate <= 0){
						Toast.makeText(VideoActivity.this, "对方音频中断了！", Toast.LENGTH_SHORT).show();
						// 重置下，如果对方退出了，有进去了的情况			
						mFirstGetAudioBitrate = false;
					}
				}
				handler.postDelayed(runnable, UPDATEVIDEOBITDELAYMILLIS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	

	private void updateTime() {
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				// 刷新视频对话时间
				case MSG_VIDEOGESPREK:
					mVideogesprekTimeTV.setText(BaseMethod.getTimeShowStr(mVideogesprekSec++));
					break;
				// 拍照预览10秒后隐藏
				case MSG_PREVIEWPIC:
					if (mPreviewPicSec <= 0){
						mPreviewPicTimer.cancel();
						mPreviewPicTimer = null;
						mPreviewPicIV.setVisibility(View.GONE);
						mPreviewFilePath.setVisibility(View.GONE);
					}
					else {
						mPreviewPicSec -= 1;
					}
					break;
				default:
					break;
				}
			}
			
		};
		
		initVideogesprekTimer();
	}
	
	// 初始化视频对话定时器
	private void initVideogesprekTimer()
	{
		if (mVideogesprekTimer == null)
			mVideogesprekTimer = new Timer();
		
		mTimerTask = new TimerTask() {		
			@Override
			public void run() {
				mHandler.sendEmptyMessage(MSG_VIDEOGESPREK);
			}
		};
		
		mVideogesprekTimer.schedule(mTimerTask, 1000, 1000);
	}
	
	
	// 初始化拍照图片预览定时器
	private void initPreviewPicTimer() {
		if (mPreviewPicTimer == null){
			mPreviewPicTimer = new Timer();
		}
		
		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(MSG_PREVIEWPIC);				
			}
		};
		
		mPreviewPicTimer.schedule(mTimerTask, 10, 1000);
		mPreviewPicIV.setVisibility(View.VISIBLE);
		mPreviewFilePath.setVisibility(View.VISIBLE);
	}
	
    // 点击事件
	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Intent intent;
			switch (view.getId()) {
			// 返回
			case (R.id.returnImgBtn): {	
				if (mCustomApplication.getCurOpenFuncUI() == FuncMenu.FUNC_VIDEOCALL) {
					CallingCenter.getInstance().VideoCallControl(
							AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH, userID,
							0, 0, -1, "");
				}

				destroyCurActivity();
				break;
			}
			
			//发生信息
			case R.id.upbutton:{			
				anyChatSDK.SendTextMessage(userID,1, "A");
				Toast.makeText(VideoActivity.this, "forward!", Toast.LENGTH_SHORT).show();
			}
			break;
			case R.id.downbutton:{			
				anyChatSDK.SendTextMessage(userID,1, "B");
				Toast.makeText(VideoActivity.this, "down!", Toast.LENGTH_SHORT).show();
			}
			break;
			case R.id.leftbutton:{			
				anyChatSDK.SendTextMessage(userID,1, "C");
				Toast.makeText(VideoActivity.this, "left!", Toast.LENGTH_SHORT).show();
			}
			break;
			case R.id.rightbutton:{			
				anyChatSDK.SendTextMessage(userID,1, "D");
				Toast.makeText(VideoActivity.this, "right!", Toast.LENGTH_SHORT).show();
			}
			break;
			case R.id.stop:{			
				anyChatSDK.SendTextMessage(userID,1, "E");
				Toast.makeText(VideoActivity.this, "stop!", Toast.LENGTH_SHORT).show();
			}
			break;
			case R.id.bluetooth:{
			    onConnectB();
				//btn1();
			}
			break;
					
			
			// 摄像头切换
			case (R.id.ImgSwichVideo): {	
				// 如果是采用Java视频采集，则在Java层进行摄像头切换
				if (AnyChatCoreSDK
						.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
					AnyChatCoreSDK.mCameraHelper.SwitchCamera();
					return;
				}

				String strVideoCaptures[] = anyChatSDK.EnumVideoCapture();
				String temp = anyChatSDK.GetCurVideoCapture();
				for (int i = 0; i < strVideoCaptures.length; i++) {
					if (!temp.equals(strVideoCaptures[i])) {
						anyChatSDK.UserCameraControl(-1, 0);
						bSelfVideoOpened = false;
						anyChatSDK.SelectVideoCapture(strVideoCaptures[i]);
						anyChatSDK.UserCameraControl(-1, 1);
						break;
					}
				}
			}
				break;
			// End Call
			case (R.id.endCall): {			
				if (mCustomApplication.getCurOpenFuncUI() == FuncMenu.FUNC_VIDEOCALL) {
					showEndVideoCallDialog();
				}else {
					showEndVideoDialog();
				}
	
				break;
			}
			// 控制自己语音的开关
			case R.id.btn_speakControl:		{
			/*	if ((anyChatSDK.GetSpeakState(-1) == 1)) {
					mBtnSpeakCtrl.setImageResource(R.drawable.speak_off);
					anyChatSDK.UserSpeakControl(-1, 0);
				} else {
					mBtnSpeakCtrl.setImageResource(R.drawable.speak_on);
					anyChatSDK.UserSpeakControl(-1, 1);
					*/
					anyChatSDK.SendTextMessage(userID,1, "speak");
					Toast.makeText(VideoActivity.this, "switch speak", Toast.LENGTH_SHORT).show();
				}

				break;
			// 控制自己视频的开关
			case R.id.btn_cameraControl:			
			/*	if ((anyChatSDK.GetCameraState(-1) == 2)) {
					mBtnCameraCtrl.setImageResource(R.drawable.camera_off);
					anyChatSDK.UserCameraControl(-1, 0);
				} else {
					mBtnCameraCtrl.setImageResource(R.drawable.camera_on);
					anyChatSDK.UserCameraControl(-1, 1);
				}*/
				anyChatSDK.SendTextMessage(userID,1, "video");
				Toast.makeText(VideoActivity.this, "video", Toast.LENGTH_SHORT).show();
				break;
			// 自拍
			case R.id.btn_TakePhotoSelf:
				/*anyChatSDK.SnapShot(-1, AnyChatDefine.ANYCHAT_RECORD_FLAGS_SNAPSHOT, 0);
				
				BaseMethod.playSound(VideoActivity.this, BaseMethod.PHOTOSSOUNDID);*/
				
				//anyChatSDK.SendTextMessage(userID,1, "photo");
				Toast.makeText(VideoActivity.this, "take photo", Toast.LENGTH_SHORT).show();
				break;
			// 拍照
			case R.id.btn_TakePhotoOther:
				anyChatSDK.SnapShot(userID, AnyChatDefine.ANYCHAT_RECORD_FLAGS_SNAPSHOT, 0);
				BaseMethod.playSound(VideoActivity.this, BaseMethod.PHOTOSSOUNDID);
				anyChatSDK.SendTextMessage(userID,1, "photo");
				Toast.makeText(VideoActivity.this, "take photo", Toast.LENGTH_SHORT).show();
				
				break;
			// 图片预览事件
			case R.id.previewPhoto:
			    intent = BaseMethod.getIntent(mPreviewPicPathStr, "image/*");
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};

	private void btn1(){
		
		Intent serverIntent1 = new Intent(this, BTClient.class); //跳转程序设置
		startActivityForResult(serverIntent1, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
		
		
	}
	
	// 关闭视频呼叫确认框
	private void showEndVideoCallDialog() {
		mDialog = DialogFactory.getDialog(DialogFactory.DIALOGID_ENDCALL,
				userID, this);
		mDialog.show();
	}

	// 关闭视频确认框
	private void showEndVideoDialog() {
		mDialog = DialogFactory.getDialog(DialogFactory.DIALOGID_EXIT, userID, this);
		mDialog.show();
	}

	private void refreshAV() {
		anyChatSDK.UserCameraControl(userID, 1);
		anyChatSDK.UserSpeakControl(userID, 1);
		anyChatSDK.UserCameraControl(-1, 1);
		anyChatSDK.UserSpeakControl(-1, 1);
		mBtnSpeakCtrl.setImageResource(R.drawable.speak_on);
		mBtnCameraCtrl.setImageResource(R.drawable.camera_on);
		bOtherVideoOpened = false;
		bSelfVideoOpened = false;
	}

	private void destroyCurActivity() {
		onPause();
		onDestroy();
		super.onDestroy();
		if(_socket!=null)  //关闭连接socket
	    	try{
	    		_socket.close();
	    	}catch(IOException e){}
	    //	_bluetooth.disable();  //关闭蓝牙服务
	}

	protected void onRestart() {
		super.onRestart();
		anyChatSDK.SetBaseEvent(this);
		anyChatSDK.SetVideoCallEvent(this);
		anyChatSDK.SetRecordSnapShotEvent(this);
		anyChatSDK.SetTextMessageEvent(this);
		// 如果是采用Java视频显示，则需要设置Surface的CallBack
		if (AnyChatCoreSDK
				.GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
			int index = anyChatSDK.mVideoHelper.bindVideo(mOtherView
					.getHolder());
			anyChatSDK.mVideoHelper.SetVideoUser(index, userID);
		}

		refreshAV();
	}

	protected void onResume() {
		super.onResume();
		CallingCenter.mContext = VideoActivity.this;
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onDestroy() {
		super.onDestroy();
		
		handler.removeCallbacks(runnable);
		anyChatSDK.UserCameraControl(userID, 0);
		anyChatSDK.UserSpeakControl(userID, 0);
		anyChatSDK.UserCameraControl(-1, 0);
		anyChatSDK.UserSpeakControl(-1, 0);	
		anyChatSDK.mSensorHelper.DestroySensor();
		
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if(mCustomApplication.getCurOpenFuncUI() == FuncMenu.FUNC_VIDEOCALL) {
			showEndVideoCallDialog();
			}else{
				showEndVideoDialog();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	public void adjustLocalVideo(boolean bLandScape) {
		float width;
		float height = 0;
		DisplayMetrics dMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
		width = (float) dMetrics.widthPixels / 4;
		LinearLayout layoutLocal = (LinearLayout) this
				.findViewById(R.id.frame_local_area);
		FrameLayout.LayoutParams layoutParams = (android.widget.FrameLayout.LayoutParams) layoutLocal
				.getLayoutParams();
		if (bLandScape) {

			if (AnyChatCoreSDK
					.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL) != 0){
				height = width
						* AnyChatCoreSDK
								.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL)
						/ AnyChatCoreSDK
								.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL)
						+ 5;
			}
			else{
				height = (float) 3 / 4 * width + 5;
			}
		} else {

			if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL) != 0){
				height = width* AnyChatCoreSDK
								.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL)
						/ AnyChatCoreSDK
								.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL)
						+ 5;
			}
			else{
				height = (float) 4 / 3 * width + 5;
			}
		}
		layoutParams.width = (int) width;
		layoutParams.height = (int) height;
		layoutLocal.setLayoutParams(layoutParams);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			adjustLocalVideo(true);
			AnyChatCoreSDK.mCameraHelper.setCameraDisplayOrientation();
		} else {
			adjustLocalVideo(false);
			AnyChatCoreSDK.mCameraHelper.setCameraDisplayOrientation();
		}

	}
	

	@Override
	public void OnAnyChatConnectMessage(boolean bSuccess) {

	}

	@Override
	public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {

	}

	@Override
	public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {

	}

	@Override
	public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {

	}
	
	


	@Override
	public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
		if (!bEnter) {
			if (dwUserId == userID) {
				Toast.makeText(VideoActivity.this, "对方已离开！", Toast.LENGTH_SHORT).show();
				userID = 0;
				anyChatSDK.UserCameraControl(dwUserId, 0);
				anyChatSDK.UserSpeakControl(dwUserId, 0);
				bOtherVideoOpened = false;
			}

		} else {
			if (userID != 0)
				return;

			int index = anyChatSDK.mVideoHelper.bindVideo(mOtherView.getHolder());
			anyChatSDK.mVideoHelper.SetVideoUser(index, dwUserId);

			anyChatSDK.UserCameraControl(dwUserId, 1);
			anyChatSDK.UserSpeakControl(dwUserId, 1);
			userID = dwUserId;
		}
	}

	@Override
	public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
		// 网络连接断开之后，上层需要主动关闭已经打开的音视频设备
		if (bOtherVideoOpened) {
			anyChatSDK.UserCameraControl(userID, 0);
			anyChatSDK.UserSpeakControl(userID, 0);
			bOtherVideoOpened = false;
		}
		if (bSelfVideoOpened) {
			anyChatSDK.UserCameraControl(-1, 0);
			anyChatSDK.UserSpeakControl(-1, 0);
			bSelfVideoOpened = false;
		}
		
		Intent mIntent = new Intent("NetworkDiscon");
		// 发送广播
		sendBroadcast(mIntent);

		// 销毁当前界面
		destroyCurActivity();
	}

	

	@Override
	public void OnAnyChatRecordEvent(int dwUserId, int dwErrorCode, String lpFileName,
			int dwElapse, int dwFlags, int dwParam, String lpUserStr) {
	}

	@Override
	public void OnAnyChatSnapShotEvent(int dwUserId, int dwErrorCode, String lpFileName,
			int dwFlags, int dwParam, String lpUserStr) {
		
		Log.d("AnyChatx", "拍照图片路径：" + lpFileName);
		mPreviewPicPathStr = lpFileName;
		
		File file = new File(lpFileName);
		if (file.exists()){
			mPreviewPicIV.setImageBitmap(BaseMethod.getImageThumbnail(lpFileName, 300, 400));
			mPreviewFilePath.setText(lpFileName);
			if (mPreviewPicTimer != null){
				mPreviewPicTimer.cancel();
				mPreviewPicTimer = null;
			}
			
			initPreviewPicTimer();
			mPreviewPicSec = 10;
		}		
	}
	
	@Override
	public void OnAnyChatVideoCallEvent(int dwEventType, int dwUserId,
			int dwErrorCode, int dwFlags, int dwParam, String userStr) {
		this.finish();
	}
 //接收
	@Override
	public void OnAnyChatTextMessage(int dwFromUserid, int dwToUserid,
			boolean bSecret, String message) {	
		int i;
		
		onSendB(message);  //蓝牙
		
		/*CharSequence tx = message;//RS232串口
			
			int[] text = new int[tx.length()];
			
        for (i=0; i<tx.length(); i++) 
        {
                text[i] = tx.charAt(i);
        }

			
			com3.Write(text, tx.length());
		
		Toast.makeText(VideoActivity.this, message, Toast.LENGTH_SHORT).show();*/
	}
	
	/*static {
        System.loadLibrary("serialtest");
	}*/

	
	 public void onSendB(String v){
	    	int i=0;
	    	int n=0;
	    	
	    	try{
	    		OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流
	    		byte[] bos = v.toString().getBytes();
	    		for(i=0;i<bos.length;i++){
	    			if(bos[i]==0x0a)n++;
	    		}
	    		byte[] bos_new = new byte[bos.length+n];
	    		n=0;
	    		for(i=0;i<bos.length;i++){ //手机中换行为0a,将其改为0d 0a后再发送
	    			if(bos[i]==0x0a){
	    				bos_new[n]=0x0d;
	    				n++;
	    				bos_new[n]=0x0a;
	    			}else{
	    				bos_new[n]=bos[i];
	    			}
	    			n++;
	    		}
	    		
	    		os.write(bos_new);	
	    	}catch(IOException e){  		
	    	}  	
	    }
	
	//接收活动结果，响应startActivityForResult()
	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	switch(requestCode){
	    	case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
	    		// 响应返回结果
	            if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
	                // MAC地址，由DeviceListActivity设置返回
	                String address = data.getExtras()
	                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	                // 得到蓝牙设备句柄      
	                _device = _bluetooth.getRemoteDevice(address);
	 
	                // 用服务号得到socket
	                try{
	                	_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
	                }catch(IOException e){
	                	Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
	                }
	                //连接socket
	    
	                try{
	                	_socket.connect();
	                	Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
	                }catch(IOException e){
	                	try{
	                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
	                		_socket.close();
	                		_socket = null;
	                	}catch(IOException ee){
	                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
	                	}
	                	
	                	return;
	                }
	                
	             
	            }
	    		break;
	    	default:break;
	    	}
	    }
	 
	  //连接按键响应函数
	    public void onConnectB(){ 
	    	
	    	 if (!bluetoothAdapter.isEnabled())  
	         {  
	                 bluetoothAdapter.enable();//异步的，不会等待结果，直接返回。  
	         }else{  
	                 bluetoothAdapter.startDiscovery();  
	              }  
	    	 
	    	if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
	    		Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
	    		Intent serverIntent = new Intent(this, DeviceListActivity.class); 
	    		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); 
	    		return;
	    	}
	    	
	    	
	        //如未连接设备则打开DeviceListActivity进行设备搜索
	   
	    	if(_socket==null){
	    		
	    		Intent serverIntent = new Intent(this, DeviceListActivity.class); //跳转程序设置
	    		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
	    	}
	    	else{
	    		 //关闭连接socket
	    	    try{
	    	    	
	    	    	is.close();
	    	    	_socket.close();
	    	    	_socket = null;
	    	    	bRun = false;
	    	    	
	    	    }catch(IOException e){}   
	    	}
	    	return;
	    }
	 
	
	 
	    
}
