package alex.nfcshift;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import alex.nfcshift.Util.FileHelper;
import alex.nfcshift.Util.NFCRestart;
import alex.nfcshift.Util.Shifter;
import alex.nfcshift.View.MyListView;

import static alex.nfcshift.Util.DataHelper.getHex;
import static alex.nfcshift.Util.DataHelper.getIDfromet;
import static alex.nfcshift.Util.DataHelper.getNewName;
import static alex.nfcshift.Util.DataHelper.getdata;
import static alex.nfcshift.Util.DataHelper.savedata;
import static alex.nfcshift.Util.FileHelper.readSDFile;

public class MainActivity extends AppCompatActivity {
    public List<String> name;
    public List<String> ID;
    private String libnfc_temp;
    private int myposition;
    private MyListView ListView;
    private ArrayAdapter<String> adapter;
    private EditText name_et, ID_et;
    private NfcAdapter mNfcAdapter;

    final int WRITE_PERMISSION_CODE = 1;
    private String regex = "NXP_CORE_CONF=(\\{[^\\}]+\\})";
    private String nameFilename = "NFCdata_name.txt";
    private String IDFilename = "NFCdata_ID.txt";

    public static boolean result = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
            onPermissionRequest(WRITE_PERMISSION_CODE);
        }
        try {
            Process process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
        }

        if(!FileHelper.isSupported("/etc/libnfc-nxp.conf")){
            Toast.makeText(MainActivity.this, "不支持此设备！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        name = getdata(nameFilename);
        ID = getdata(IDFilename);

        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_single_choice, name);
        ListView = (MyListView) findViewById(R.id.ListView);
        ListView.setAdapter(adapter);
        ListView.setChoiceMode(MyListView.CHOICE_MODE_SINGLE);
        ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str;
                if (position >= ID.size() || ID.get(position).toString() == null) {
                    str = "null";
                } else {
                    str = ID.get(position).toString();
                }
                showSnackbar(str);
                myposition = position;
            }
        });
        ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("编辑")
                        .setView(R.layout.add_dialog)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editCard(name_et.getText().toString(), ID_et.getText().toString(), position);
                            }
                        }).setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCard(position);
                    }
                }).setNegativeButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                name_et = (EditText) dialog.findViewById(R.id.name_et);
                name_et.setText(name.get(position));
                ID_et = (EditText) dialog.findViewById(R.id.ID_et);
                ID_et.setText(ID.get(position));
                return true;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                libnfc_temp = readSDFile("/etc/libnfc-nxp.conf");
                libnfc_temp = Shifter.replace(regex, libnfc_temp, ID.get(myposition).toString());
                if (result) {
                    result = false;
                    FileHelper filehelper = new FileHelper(getApplicationContext());
                    filehelper.writeSDFile(libnfc_temp, "libnfc-nxp.conf");
                    if (result) {
                        result = false;
                        FileHelper.configreplace();
                        if (result) {
                            showSnackbar("替换成功！");
                            result = false;
                            NFCRestart.restartNFC();
                        }
                    }
                } else {
                    showSnackbar("替换失败！");
                }
            }
        });
        resolveIntent(getIntent());
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_PERMISSION_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onResume();
                } else {
                    Toast.makeText(MainActivity.this, "所需权限未被允许，程序退出！", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter == null) {
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(MainActivity.this, "该设备不支持NFC！", Toast.LENGTH_LONG).show();
                finish();
            }
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(MainActivity.this, "请先在系统设置中启用NFC！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    public void onPermissionRequest(int CODE) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.NFC)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.NFC}, CODE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                AlertDialog.Builder addbuilder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("添加新卡")
                        .setView(R.layout.add_dialog)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name_input = name_et.getText().toString();
                                String ID_input = ID_et.getText().toString();
                                addCard(name_input,ID_input);
                            }
                        }).setNegativeButton("Cancel", null);
                AlertDialog adddialog = addbuilder.create();
                adddialog.show();
                name_et = (EditText) adddialog.findViewById(R.id.name_et);
                ID_et = (EditText) adddialog.findViewById(R.id.ID_et);
                break;
            case R.id.action_help:
                AlertDialog.Builder helpbuilder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("帮助")
                        .setMessage(R.string.help_text)
                        .setPositiveButton("OK",null);
                AlertDialog helpdialog = helpbuilder.create();
                helpdialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            addCard(getNewName(nameFilename), getHex(id));
        }
    }

    public void showSnackbar(String str) {
        Snackbar.make(getCurrentFocus(), str, Snackbar.LENGTH_LONG)
                .show();
    }

    public void addCard(String name_input, String ID_input) {
        if(!name_input.isEmpty() && !ID_input.isEmpty()){
            if(getIDfromet(ID_input) != "error"){
                ID.add(getIDfromet(ID_input));
            }else{
                showSnackbar("输入格式有误，请检查！");
                return;
            }
            name.add(name_input);
            savedata(name, nameFilename);
            savedata(ID, IDFilename);
            adapter.notifyDataSetChanged();
            return;
        }
        showSnackbar("名称或ID不能为空！");
    }

    public void deleteCard(int p) {
        final int thisposition = p;
        if (name.size() == 1) {
            showSnackbar("无法删除，至少保留一张卡！");
        } else {
            name.remove(thisposition);
            ID.remove(thisposition);
            savedata(name, nameFilename);
            savedata(ID, IDFilename);
            adapter.notifyDataSetChanged();
        }
    }

    public void editCard(String newname, String newID, int p) {
        if(!newname.isEmpty() && !newID.isEmpty()){
            if(getIDfromet(newID) != "error"){
                ID.set(p, getIDfromet(newID));
            }else{
            showSnackbar("输入格式有误，请检查！");
            return;
        }
            name.set(p, newname);
            savedata(name, nameFilename);
            savedata(ID, IDFilename);
            adapter.notifyDataSetChanged();
            return;
        }
        showSnackbar("名称或ID不能为空！");
    }
}
