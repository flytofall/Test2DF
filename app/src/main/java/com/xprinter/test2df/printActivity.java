package com.xprinter.test2df;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.suke.widget.SwitchButton;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.BitmapCallback;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos80;
import net.posprinter.utils.PosPrinterDev;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class printActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.print_pic)
    public ImageView imageView;
    @BindView(R.id.et1)
    public EditText editText;
    @BindView(R.id.switch_button)
    public SwitchButton cutpaper;
    @BindView(R.id.buttonprint)
    public Button print;
    @BindView(R.id.container)
    public CoordinatorLayout container ;
    @BindView(R.id.tv_net_disconnect)
    public TextView tip;
    @BindView(R.id.decrease)
    public Button decrease;
    @BindView(R.id.add)
    public Button add;
    @BindView(R.id.distenTxt)
    public TextView distenceTxt;

    private boolean changebmp = false;
    private int distence=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        //ButterKnife
        ButterKnife.bind(this);

        //setLisener
        setLisener();


    }
    private boolean cutPaper=false;
    private void setLisener(){
        imageView.setOnClickListener(this);
        editText.setOnClickListener(this);
        print.setOnClickListener(this);
        add.setOnClickListener(this);
        decrease.setOnClickListener(this);
        distenceTxt.setText(distence+"");


        //set the cutper swichbutton
        cutpaper.setChecked(true);
        cutpaper.isChecked();
        cutpaper.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (!isChecked){
                    cutPaper=true;
                }else {
                    cutPaper= false;
                }

            }
        });

    }




    @Override
    public void onClick(View v) {
        int id =v.getId();
        switch (id){
            case R.id.print_pic:
                changebmp=true;
                printPIC();
                break;
            case R.id.buttonprint:
//                sendData();
                check();
                break;
            case R.id.decrease:
                if (distence!=0){
                    distence=distence-1;

                }else {
                    distence=0;
                }
                distenceTxt.setText(distence+"");
                break;
            case R.id.add:
                if (distence<=10){
                    distence=distence+1;
                }
                distenceTxt.setText(distence+"");
                break;

        }

    }
    /*
    check
     */

    private void check(){
        MainActivity.binder.checkLinkedState(new UiExecute() {
            @Override
            public void onsucess() {
                sendData();
                Log.e("CHECK"," ok");
            }

            @Override
            public void onfailed() {
                showSnackbar("failed");
                Log.e("CHECK"," FAILED");

            }
        });
    }
    /*
    send data to the printer
     */
    private void sendData(){

        final Bitmap printBmp;
        if (changebmp){
            printBmp=b2;
        }else {
            Bitmap bitmap=convertGreyImg(BitmapFactory.decodeResource(getResources(),R.drawable.casino));
            printBmp= resizeImage(bitmap,120,false);
        }
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
                showSnackbar("send data ok ");
                Log.e("Send"," ok");

            }

            @Override
            public void onfailed() {
                showSnackbar("send data failed");
                Log.e("Send"," FAILED");

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
               List<byte[]> list = new ArrayList<>();
                list.add(DataForSendToPrinterPos80.initializePrinter());
                list.add(DataForSendToPrinterPos80.printRasterBmp
                        (0,printBmp, BitmapToByteData.BmpType.Threshold,BitmapToByteData.AlignType.Center,576));
                list.add(DataForSendToPrinterPos80.printAndFeedForward(distence));
                String str=editText.getText().toString();
                if (str.length()!=0){
                    list.add(DataForSendToPrinterPos80.selectCharacterSize(17));
                    list.add(StringUtils.strTobytes(str));
                }

                if (!cutPaper){
                    list.add(DataForSendToPrinterPos80.printAndFeedLine());
                    list.add(DataForSendToPrinterPos80.printAndFeedForward(2));
                    list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66,1));
                }
                return list;
            }
        });


    }

    private Bitmap b1;//grey-scale bitmap
    private  Bitmap b2;//compression bitmap

    /*
    * open camera get a picture
    * */
    private void printPIC(){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("test",requestCode+"  "+resultCode);
        if (requestCode==0&&resultCode==RESULT_OK){
            Log.e("test","test2");
            //Select the picture by going to the gallery, and then get the returned bitmap object
            try{
                Uri imagepath=data.getData();
                ContentResolver resolver = getContentResolver();
                Bitmap b= MediaStore.Images.Media.getBitmap(resolver,imagepath);
                b1=convertGreyImg(b);
                Message message=new Message();
                message.what=1;
                handler.handleMessage(message);

                //Compress pictures
                Tiny.BitmapCompressOptions options = new Tiny.BitmapCompressOptions();
                Tiny.getInstance().source(b1).asBitmap().withOptions(options).compress(new BitmapCallback() {
                    @Override
                    public void callback(boolean isSuccess, Bitmap bitmap) {
                        if (isSuccess){
//                            Toast.makeText(PosActivity.this,"bitmap: "+bitmap.getByteCount(),Toast.LENGTH_LONG).show();
                            b2=bitmap;
                            b2=resizeImage(b1,120,false);
                            Message message=new Message();
                            message.what=2;
                            handler.handleMessage(message);
                        }


                    }
                });
//                b2=resizeImage(b1,576,386,false);//576是80型号
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    /*
       The monochromatic diagram of the two value method
     * Grayscale, and then converted to monochromatic map
     * @param img bitmap
     * @return  data  Returns the image information of the converted monochromatic bitmap
     */
    public Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int[] pixels = new int[width * height];

        img.getPixels(pixels, 0, width, 0, 0, width, height);


        //grayscale arithmetic average, threshold
        double redSum=0,greenSum=0,blueSun=0;
        double total=width*height;

        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);



                redSum+=red;
                greenSum+=green;
                blueSun+=blue;


            }
        }
        int m=(int) (redSum/total);

        // two value method, monochromatic image conversion
        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int alpha1 = 0xFF << 24;
                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);


                if (red>=m) {
                    red=green=blue=255;
                }else{
                    red=green=blue=0;
                }
                grey = alpha1 | (red << 16) | (green << 8) | blue;
                pixels[width*i+j]=grey;


            }
        }
        Bitmap mBitmap=Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);



        return mBitmap;
    }


    /*
	 * Zoom in with Bitmap and Matrix
	 *   */
    public static Bitmap resizeImage(Bitmap bitmap, int w,boolean ischecked)
    {

        Bitmap BitmapOrg = bitmap;
        Bitmap resizedBitmap = null;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        if (width<=w) {
            return bitmap;
        }
        if (!ischecked) {
            int newWidth = w;
            int newHeight = height*w/width;

            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // if you want to rotate the Bitmap
            // matrix.postRotate(45);
            resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                    height, matrix, true);
        }else {
            resizedBitmap=Bitmap.createBitmap(BitmapOrg, 0, 0, w, height);
        }

        return resizedBitmap;
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if (action.equals(MainActivity.DISCONNECT)){
                Message message=new Message();
                message.what=4;
                handler.handleMessage(message);
            }
        }
    }

    /**
     * display a message
     * @param showstring Display content string
     */
    private void showSnackbar(String showstring){
        Snackbar.make(container, showstring,Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.button_unable)).show();
    }
    public Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    imageView.setImageBitmap(b1);
                    break;
                case 2:


                    tip.setVisibility(View.GONE);
                    break;
                case 3://disconnect
                    print.setEnabled(false);
                    tip.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    tip.setVisibility(View.VISIBLE);
                    break;


            }

        }
    };
}
