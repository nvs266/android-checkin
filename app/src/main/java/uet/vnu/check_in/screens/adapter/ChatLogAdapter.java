package uet.vnu.check_in.screens.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.HttpException;
import uet.vnu.check_in.R;
import uet.vnu.check_in.data.model.ChatLog;
import uet.vnu.check_in.screens.BaseRecyclerViewAdapter;
import uet.vnu.check_in.util.CircleTransform;

public class ChatLogAdapter extends BaseRecyclerViewAdapter<ChatLog, ChatLogAdapter.ViewHolder> {

    private int viewtype;

    public ChatLogAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View courseView;
        Log.d("cuonghx", "onCreateViewHolder: " + viewType);
        switch (viewType){
            case 1 :
                courseView = layoutInflater.inflate(R.layout.item_rv_chat, parent, false);
                break;
            case 2:
                courseView = layoutInflater.inflate(R.layout.item_rv_reply, parent, false);
                break;
            default:
                courseView = null;
                break;
        }

        // Return a new holder instance
        ChatLogAdapter.ViewHolder viewHolder = new ChatLogAdapter.ViewHolder(courseView);
        return viewHolder;
    }

    @Override
    public void addItem(ChatLog item) {
        super.addItem(item);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ChatLog chatLog = mCollection.get(position);
        holder.updateView(chatLog);
    }

    @Override
    public int getItemViewType(int position) {
        ChatLog chatLog = mCollection.get(position);
        Log.d("cuonghx", "getItemViewType: " + chatLog.isTeacher);
        if (chatLog.isTeacher){
            viewtype = 2;
            return 2;
        }else  {
            viewtype = 1;
            return 1;
        }
    }

    public class ViewHolder  extends RecyclerView.ViewHolder {

        private TextView textViewChat;
        private TextView textViewReply;
        private ImageView imageViewReply;
        private ImageView imageViewChat;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewChat = itemView.findViewById(R.id.tv_text_chat);
            textViewReply = itemView.findViewById(R.id.tv_reply);
            imageViewReply = itemView.findViewById(R.id.iv_avatar_reply);
            imageViewChat = itemView.findViewById(R.id.iv_avatar_chat);
        }
        public void updateView(ChatLog chatLog){
            if (chatLog.isTeacher){
                textViewReply.setText(chatLog.text);
                Picasso.get().load("https://wallpapertag.com/wallpaper/full/e/6/d/149828-fairy-tail-logo-wallpaper-1920x1200-pictures.jpg").transform(new CircleTransform()).into(imageViewReply);
            }else {
                textViewChat.setText(chatLog.text);
                Picasso.get().load("https://wallpapertag.com/wallpaper/full/e/6/d/149828-fairy-tail-logo-wallpaper-1920x1200-pictures.jpg").transform(new CircleTransform()).into(imageViewChat);
            }
        }

        private void handleErrors(Throwable throwable) {
            if (throwable instanceof HttpException) {
                handleHttpExceptions((HttpException) throwable);
                return;
            } else if (throwable instanceof UnknownHostException) {
                Toast.makeText(this.itemView.getContext(), R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this.itemView.getContext(),
                    R.string.msg_something_went_wrong,
                    Toast.LENGTH_SHORT)
                    .show();
        }

        private void handleHttpExceptions(HttpException httpException) {
            switch (httpException.code()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Toast.makeText(this.itemView.getContext(),
                            R.string.msg_wrong_email_or_password,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this.itemView.getContext(), httpException.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
