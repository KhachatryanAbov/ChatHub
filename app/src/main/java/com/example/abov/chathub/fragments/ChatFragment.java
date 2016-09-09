package com.example.abov.chathub.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.abov.chathub.listViewAdapters.MessagesRecyclerViewAdapter;
import com.example.abov.chathub.listViewAdapters.UsersRecyclerViewAdapter;
import com.example.abov.chathub.models.Message;
import com.example.abov.chathub.models.User;
import com.example.abov.chathub.R;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ChatFragment extends Fragment implements View.OnClickListener,
        UsersRecyclerViewAdapter.OnUsersRecyclerViewItemsClickListener {

    public static final String ARG_PARAM1 = "user";
    private static final String FIREBASE_URL = "https://chathub-9776c.firebaseio.com/";

    private boolean isMessagesListenerRegistered = false;

    private Firebase firebase;
    private Firebase messageBranchManager;
    private Firebase userBranchManager;

    private TreeMap<String, User> uniqueUsers;
    private List<User> mUsers;
    private List<Message> mMessages;

    private OnFragmentInteractionListener mListener;

    private User mRegisteredUser;
    private User mCurrentUser;
    private User mReceiver;

    private Button goBackButton;
    private ImageView sendButton;
    private EditText mMessageTxt;
    private TextView selectedUserName;

    private RecyclerView mOnlineUsersRecyclerView;
    private RecyclerView mCurrentChatRecyclerView;
    private RecyclerView.Adapter mAdapterForUsersList;
    private RecyclerView.Adapter mAdapterForMessagesList;

    public ChatFragment() {
    }

    public static ChatFragment newInstance(User user) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, user);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebase = new Firebase(FIREBASE_URL);
        messageBranchManager = firebase.child("messages");
        userBranchManager = firebase.child("users");
        if (getArguments() != null) {
            mRegisteredUser = (User) getArguments().getSerializable(ARG_PARAM1);
            mCurrentUser = mRegisteredUser;
            addNewUserInFirebase(mCurrentUser);
        }
        try {
            mListener = (OnFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new IllegalStateException("activiti ... ");
        }

        mMessages = new ArrayList<>();
        mUsers = new ArrayList<>();
        uniqueUsers = new TreeMap<>();

        mAdapterForUsersList = new UsersRecyclerViewAdapter(mUsers, this);
        mAdapterForMessagesList = new MessagesRecyclerViewAdapter(mMessages);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goBackButton = (Button) view.findViewById(R.id.finish_fragment_button);
        sendButton = (ImageView) view.findViewById(R.id.send_button);
        mMessageTxt = (EditText) view.findViewById(R.id.messageEditText);
        selectedUserName = (TextView) view.findViewById(R.id.current_chat_txt);

        goBackButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        mOnlineUsersRecyclerView = (RecyclerView) view.findViewById(R.id.now_online_recycler_view);
        mCurrentChatRecyclerView = (RecyclerView) view.findViewById(R.id.current_chat_recycler_view);

        mOnlineUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCurrentChatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mOnlineUsersRecyclerView.setAdapter(mAdapterForUsersList);
        mCurrentChatRecyclerView.setAdapter(mAdapterForMessagesList);

        userBranchManager.addChildEventListener(usersListener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        messageBranchManager.removeEventListener(messagesListener);
        isMessagesListenerRegistered = false;
        userBranchManager.removeEventListener(usersListener);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.finish_fragment_button:
                mListener.removeChatFragment();
                break;
            case R.id.send_button:
                sendNewMessage();
                break;
            default:
                break;
        }
    }

    private void sendNewMessage() {
        String messageText = mMessageTxt.getText().toString();
        if (!messageText.isEmpty()) {
            if (mReceiver == null) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Dear User");
                alertDialog.setMessage("Please Select A Message Receiver First.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else {
                addNewMessageInFirebase(new Message(messageText, mReceiver.getUserName(),
                        mReceiver.getUserId(), mCurrentUser.getUserName(), mCurrentUser.getUserId()));
                mMessageTxt.setText("");
            }
        }

    }


    @Override
    public void newUserChosen(User receiver) {
        this.mReceiver = receiver;
        selectedUserName.setText(mReceiver.getUserName());
        mMessages.clear();
        if (isMessagesListenerRegistered) {
            messageBranchManager.removeEventListener(messagesListener);
        }
        messageBranchManager.addChildEventListener(messagesListener);
        isMessagesListenerRegistered = true;
    }

    private void scrollRecyclerViewToBottom(RecyclerView recyclerView) {
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
    }

    ChildEventListener usersListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            User user = dataSnapshot.getValue(User.class);
            if (!user.getUserId().equals(mCurrentUser.getUserId())) {
                uniqueUsers.put(user.getUserId(), user);
                mUsers.clear();
                ArrayList<User> temp = new ArrayList<>(uniqueUsers.values());
                mUsers.addAll(temp);

                mAdapterForUsersList.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    ChildEventListener messagesListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Message message = dataSnapshot.getValue(Message.class);

            if ((message.getUserId().equals(mCurrentUser.getUserId()) && message.getReceiverId().equals(mReceiver.getUserId()))
                    || (message.getUserId().equals(mReceiver.getUserId()) && message.getReceiverId().equals(mCurrentUser.getUserId()))) {
                mMessages.add(message);
                mAdapterForMessagesList.notifyDataSetChanged();
                scrollRecyclerViewToBottom(mCurrentChatRecyclerView);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    private void addNewUserInFirebase(User user) {
        userBranchManager.push().setValue(user);
    }

    public void addNewMessageInFirebase(Message message) {
        messageBranchManager.push().setValue(message);
    }

    public interface OnFragmentInteractionListener {
        void removeChatFragment();
    }
}
