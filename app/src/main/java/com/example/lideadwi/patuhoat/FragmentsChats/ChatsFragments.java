package com.example.lideadwi.patuhoat.FragmentsChats;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lideadwi.patuhoat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragments extends Fragment {


    public ChatsFragments() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats_fragments, container, false);
    }

}
