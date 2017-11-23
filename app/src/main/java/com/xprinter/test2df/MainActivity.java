package com.xprinter.test2df;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.IBinder;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos58;
import net.posprinter.utils.PosPrinterDev;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    public static String DISCONNECT="disconnetct";

    //IMyBinder interface, all methods for calling connections and sending data are encapsulated in this interface
    public static IMyBinder binder;

    //bindService  connection
    ServiceConnection conn= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //binde ok
            binder= (IMyBinder) iBinder;
            Log.e("binder","connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("disbinder","disconnected");
        }
    };

    public static boolean ISCONNECT;//Determine whether the connection is successful
    @BindView(R.id.buttonConnect)
    public Button BTCon ;// connect button

    @BindView(R.id.buttonDisconnect)
    public Button BTDisconnect;//disconnect button


    @BindView(R.id.buttonpos)
    public Button  BtSend;// send

    @BindView(R.id.showET)
    public EditText showET;//show

    @BindView(R.id.container)
    public CoordinatorLayout container;

    private String ipAdress ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bind the service，Getting imybinder objects
        Intent intent=new Intent(this,PosprinterService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);

        //using butteractivity
        ButterKnife.bind( this ) ;

        if (ipAdress!=null){
            showET.setText(ipAdress);
        }

        //set the listener
        setlistener();
    }

    public void setlistener(){
        BTCon.setOnClickListener(this);
        BTDisconnect.setOnClickListener(this);
        BtSend.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){

            case R.id.buttonConnect:
                connectPrinter();
                break;
            case R.id.buttonDisconnect:
                if (ISCONNECT){
                    binder.disconnectCurrentPort(new UiExecute() {
                        @Override
                        public void onsucess() {
                            showSnackbar(getString(R.string.toast_discon_success));
                            showET.setText("");
                            BTCon.setText(getString(R.string.connect));
                        }

                        @Override
                        public void onfailed() {
                            showSnackbar(getString(R.string.toast_discon_faile));

                        }
                    });
                }else {
                    showSnackbar(getString(R.string.toast_present_con));
                }
                break;
            case R.id.buttonpos:

                if (ISCONNECT){
                    Intent intent=new Intent(this,printActivity.class);
                    startActivity(intent);
                }else {
                    showSnackbar("please connect the printer first !");
                }

                break;


        }

    }

    /*
    connect the printer by wifi
     */
    private void connectPrinter(){
        String ipAddress = showET.getText().toString();
        if(ipAddress.equals(null)){
            showSnackbar("please input the ip addrress");
        }else {
            ipAddress=showET.getText().toString();
            binder.connectNetPort(ipAddress, 9100, new UiExecute() {
                @Override
                public void onsucess() {
                    ISCONNECT=true;
                    showSnackbar(getString(R.string.con_success));

                    binder.acceptdatafromprinter(new UiExecute() {
                        @Override
                        public void onsucess() {

                        }

                        @Override
                        public void onfailed() {
                            ISCONNECT=false;
                            showSnackbar(getString(R.string.con_failed));
                            Intent intent=new Intent();
                            intent.setAction(DISCONNECT);
                            sendBroadcast(intent);

                        }
                    });
                }

                @Override
                public void onfailed() {
                    ISCONNECT=false;
                    showSnackbar(getString(R.string.con_failed));
                    BTCon.setText(getString(R.string.con_failed));

                }
            });
        }
    }


    private TextView tv_usb;
    private List<String> usbList,usblist;
    private ListView lv_usb;
    private ArrayAdapter<String> adapter3;



    /**
     * display a message
     * @param showstring show the content
     */
    private void showSnackbar(String showstring){
        Snackbar.make(container, showstring,Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.button_unable)).show();
    }
}
