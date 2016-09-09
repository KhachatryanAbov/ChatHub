package com.example.abov.chathub.listViewAdapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.abov.chathub.R;
import com.example.abov.chathub.fragments.ChatFragment;
import com.example.abov.chathub.models.User;

import java.util.List;

public class UsersRecyclerViewAdapter extends RecyclerView.Adapter<UsersRecyclerViewAdapter.ViewHolder> {
    private List<User> mUsers;
    private OnUsersRecyclerViewItemsClickListener myItemClickListener;

    public UsersRecyclerViewAdapter(List<User> myDataset, ChatFragment chatFragment) {
        mUsers = myDataset;
        try {
            myItemClickListener  =  chatFragment;
        } catch (ClassCastException e) {
            throw new IllegalStateException("activity ... ");
        }
    }

    @Override
    public UsersRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_users, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.viewHolderName.setText(mUsers.get(position).getUserName());
        holder.usersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = mUsers.get(holder.getAdapterPosition());
                myItemClickListener.newUserChosen(user);
            }
        });
    }



    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout usersLayout;
        public TextView viewHolderName;

        public ViewHolder(View root) {
            super(root);
            viewHolderName = (TextView) root.findViewById(R.id.usersRecyclerViewNameTextView);
            usersLayout = (LinearLayout)root.findViewById(R.id.users_recycler_view_layout);
        }
    }

    public interface OnUsersRecyclerViewItemsClickListener {
        void newUserChosen(User user);
    }
}


