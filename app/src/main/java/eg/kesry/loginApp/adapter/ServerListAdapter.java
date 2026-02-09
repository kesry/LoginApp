package eg.kesry.loginApp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import eg.kesry.loginApp.CaptureAct;
import eg.kesry.loginApp.MainActivity;
import eg.kesry.loginApp.bean.LoginServer;
import androidx.appcompat.app.AlertDialog;
import java.util.List;

import eg.kesry.loginApp.R;
import java.util.Objects;

public class ServerListAdapter extends BaseAdapter {

    private List<LoginServer> loginServers;

    private Context context;

    private MainActivity activity;

    public ServerListAdapter(MainActivity context, List<LoginServer> data) {
        this.loginServers = data;
        this.activity = context;
        this.context = context;
    }

    @Override
    public int getCount() {
        return loginServers.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LoginServer loginServer = loginServers.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.text_username);
            holder.imageButton = convertView.findViewById(R.id.button_scan);
            holder.editButton = convertView.findViewById(R.id.button_edit);
            holder.deleteButton = convertView.findViewById(R.id.button_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(loginServer.getUsername());

        holder.imageButton.setOnClickListener(v -> {
            // 打开摄像头扫码
            activity.scan(loginServer);
        });

        holder.editButton.setOnClickListener(v -> {
            // 编辑服务器信息
            activity.editServer(loginServer);
        });

        holder.deleteButton.setOnClickListener(v -> {
            // 删除服务器
            // 弹窗确认删除
            new AlertDialog.Builder(context)
                    .setTitle("确认删除")
                    .setMessage("确定要删除服务器 \"" + loginServer.getUsername() + "\" 吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        // 具体删除功能
                        activity.removeServer(loginServer);
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        return convertView;
    }

    private class ViewHolder {
        TextView textView;

        ImageButton imageButton;
        
        ImageButton editButton;

        ImageButton deleteButton;
    }

}
