/*
 * Copyright 2017 The Android AVN Project
 *
 *      Korea Electronics Technology Institute
 *
 *      http://keti.re.kr/
 *
 */
package com.example.android.bluetoothlegatt.com.music;

/**
 * Created by GTO on 2017-08-11.
 */

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.android.bluetoothlegatt.com.AppUtils;
import com.example.android.bluetoothlegatt.com.CarouselDataItem;
import com.example.android.bluetoothlegatt.com.CarouselView;
import com.example.android.bluetoothlegatt.com.CarouselViewAdapter;
import com.example.android.bluetoothlegatt.com.Singleton;

public class carousel_activity extends Activity implements OnItemSelectedListener, TextWatcher{

    Singleton m_Inst 					= Singleton.getInstance();
    CarouselViewAdapter m_carouselAdapter		= null;
    private final int		m_nFirstItem			= 1000;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //no keyboard unless requested by user
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // compute screen size and scaling
        Singleton.getInstance().InitGUIFrame(this);

        int padding = m_Inst.Scale(10);
        // create the interface : full screen container
        RelativeLayout panel  = new RelativeLayout(this);
        panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        panel.setPadding(padding, padding, padding, padding);
        //panel.setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.WHITE, Color.GRAY}));
        setContentView(panel);

        // copy images from assets to sdcard		---------------- 2017.06.20 gto
        /*

        */

        //Create carousel view documents			---------------- 2017.06.20 gto
        ArrayList<CarouselDataItem> Docus = new ArrayList<CarouselDataItem>();
        for (int i=0;i<4;i++) {
            CarouselDataItem docu;
            if (i%4==0) {
                docu = new CarouselDataItem("@drawable/keti", 0, "Movie Image ");
                System.out.println("movie i =  " + i);
                //Toast.makeText(this, "movie i : "+ i , Toast.LENGTH_SHORT).show();
            }

            else if (i%4==1) {
                docu = new CarouselDataItem("@drawable/keti", 0, "MP3 Image ");
                System.out.println("mp3 i =  " + i);
                //Toast.makeText(this, "mp3 i : "+ i , Toast.LENGTH_SHORT).show();
            }
            else if (i%4==2) {
                docu = new CarouselDataItem("@drawable/keti", 0, "Picture Image ");
                System.out.println("picture i  = " + i);
                //Toast.makeText(this, "picture i : "+ i , Toast.LENGTH_SHORT).show();
            }
            else {
                docu = new CarouselDataItem("@drawable/keti", 0, "Setting Image ");
                System.out.println("setting i  = " + i);
                //Toast.makeText(this, "setting i : "+ i , Toast.LENGTH_SHORT).show();
            }
            Docus.add(docu);
        }

        // add the serach filter
	    /*
	    EditText etSearch = new EditText(this);
	    etSearch.setHint("Search your documents");
	    etSearch.setSingleLine();
	    etSearch.setTextColor(Color.BLACK);
	    etSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_search, 0, 0, 0);
	    AppUtils.AddView(panel, etSearch, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
	    		new int[][]{new int[]{RelativeLayout.CENTER_HORIZONTAL}, new int[]{RelativeLayout.ALIGN_PARENT_TOP}}, -1,-1);
	    etSearch.addTextChangedListener((TextWatcher) this);
		*/

        // add logo
        TextView tv = new TextView(this);
        tv.setTextColor(Color.BLACK);
        tv.setText("www.keti.re.kr");
        AppUtils.AddView(panel, tv, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                new int[][]{new int[]{RelativeLayout.CENTER_HORIZONTAL}, new int[]{RelativeLayout.ALIGN_PARENT_BOTTOM}}, -1,-1);

        // create the carousel
        CarouselView coverFlow = new CarouselView(this);

        // create adapter and specify device independent items size (scaling)
        // for more details see: http://www.pocketmagic.net/2013/04/how-to-scale-an-android-ui-on-multiple-screens/
        m_carouselAdapter =  new CarouselViewAdapter(this,Docus, m_Inst.Scale(400),m_Inst.Scale(300));
        coverFlow.setAdapter(m_carouselAdapter);
        coverFlow.setSpacing(-1*m_Inst.Scale(150));
        coverFlow.setSelection(Integer.MAX_VALUE / 2, true);
        coverFlow.setAnimationDuration(1000);
        coverFlow.setOnItemSelectedListener((OnItemSelectedListener) this);

        AppUtils.AddView(panel, coverFlow, LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT,
                new int[][]{new int[]{RelativeLayout.CENTER_IN_PARENT}},
                -1, -1);
    }

    public void afterTextChanged(Editable arg0) {}

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        m_carouselAdapter.getFilter().filter(s.toString());
    }

    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        CarouselDataItem docu =  (CarouselDataItem) m_carouselAdapter.getItem((int) arg3);
        if (docu!=null)
            Toast.makeText(this, "You've clicked on: "+docu.getDocText(), Toast.LENGTH_SHORT).show();

        System.out.println("douc ? = " + docu.getDocText());

        if (docu.m_szDocName == "Setting Image") {
            System.out.println("This is Setting Image : TRUE");
        }
        else {
            System.out.println("This is Setting Image : FAIL");
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {}


}
