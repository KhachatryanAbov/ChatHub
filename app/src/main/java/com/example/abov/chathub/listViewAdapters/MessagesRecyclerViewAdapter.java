package com.example.abov.chathub.listViewAdapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.abov.chathub.R;
import com.example.abov.chathub.models.Message;

import java.util.List;


public class MessagesRecyclerViewAdapter extends RecyclerView.Adapter<MessagesRecyclerViewAdapter.ViewHolder> {
    private List<Message> mMessages;

    public MessagesRecyclerViewAdapter(List<Message> myDataset) {
        mMessages = myDataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_messages, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.userName.setText(mMessages.get(position).getUserName());
        holder.messageText.setText(mMessages.get(position).getMessageText());
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public TextView messageText;

        public ViewHolder(View root) {
            super(root);
            userName = (TextView) root.findViewById(R.id.message_recycler_view_user_name_txt);
            messageText = (TextView)root.findViewById(R.id.message_recycler_view_message_txt);
        }
    }
}

