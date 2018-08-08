package com.example.lideadwi.patuhoat.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.lideadwi.patuhoat.FragmentsChats.ChatsFragments;
import com.example.lideadwi.patuhoat.FragmentsChats.FriendsFragments;
import com.example.lideadwi.patuhoat.FragmentsChats.RequestFragments;

public class SectionPageAdapter extends FragmentPagerAdapter {

    public SectionPageAdapter(FragmentManager fm) {
        super(fm);

    }


    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return new  ChatsFragments();

            case 1:
                return new FriendsFragments();

            case 2:
                return new  RequestFragments();

            default:
                return  null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "REQUEST";
            case 1:
                return "CHATS";
            case 2:
                return "Friends";
            default:
                return null;
        }

    }
}
