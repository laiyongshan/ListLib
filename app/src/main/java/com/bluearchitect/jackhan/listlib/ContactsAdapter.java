package com.bluearchitect.jackhan.listlib;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bluearchitect.jackhan.sortlistviewlibrary.SortAdapter;
import com.bluearchitect.jackhan.sortlistviewlibrary.SortModel;

import java.util.List;

/**
 * @author hhz
 * @time 2016/11/13 16:46
 * @description 人员列表适配器
 */

public class ContactsAdapter extends SortAdapter<ContactsAdapter.ContactsViewHolder, Contacts> {
    public ContactsAdapter(Context mContext, List<Contacts> list) {
        super(mContext, list);
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(View parent) {
        ContactsViewHolder viewHolder = new ContactsViewHolder(parent, LayoutInflater.from(mContext)
                .inflate(R.layout.item_contacts, null));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ContactsViewHolder viewHolder, SortModel sortModel, int position) {

        viewHolder.titleTV.setText(sortModel.getSortName());
    }

    class ContactsViewHolder extends SortAdapter.ViewHolder {
        TextView titleTV;

        public ContactsViewHolder(View parentView, View childView) {
            super(parentView, childView);
            titleTV = (TextView) childView.findViewById(R.id.title);
        }
    }
}
