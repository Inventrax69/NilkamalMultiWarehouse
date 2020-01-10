package com.inventrax.athome_multiwh.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inventrax.athome_multiwh.R;


public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final String classCode = "API_FRAG_HOME";
    private View rootView;

    Fragment fragment = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_home,container,false);
        //loadFormControls();

        return rootView;
    }

    private void loadFormControls() {


    }



    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

        }

    }
}