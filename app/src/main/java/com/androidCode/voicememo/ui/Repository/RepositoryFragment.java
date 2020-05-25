package com.androidCode.voicememo.ui.Repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidCode.voicememo.R;
import com.androidCode.voicememo.db.DBHelper;
import com.androidCode.voicememo.recyclerView.RecyclerViewCustomAdapter;
import com.androidCode.voicememo.recyclerView.RecyclerViewDictionary;

import java.util.ArrayList;

public class RepositoryFragment extends Fragment {

    private ArrayList<RecyclerViewDictionary> mArrayList;
    private RecyclerViewCustomAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_repository, container, false);

        // Recycler View
        RecyclerView mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerview_main_list);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mArrayList = new ArrayList<>();

        mAdapter = new RecyclerViewCustomAdapter(getActivity(), mArrayList);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLinearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        // SQLite
        DBHelper helper = new DBHelper(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select _id, date, content from tb_memo order by _id desc", null);

        while(cursor.moveToNext()){
            RecyclerViewDictionary data = new RecyclerViewDictionary(cursor. getInt(0), cursor.getString(1), cursor.getString(2));
            mArrayList.add(data);
        }
        db.close();
        mAdapter.notifyDataSetChanged();

        return root;
    }
}