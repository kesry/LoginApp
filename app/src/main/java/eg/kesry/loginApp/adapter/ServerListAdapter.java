package eg.kesry.loginApp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import eg.kesry.loginApp.MainActivity;
import eg.kesry.loginApp.bean.LoginServer;
import androidx.appcompat.app.AlertDialog;
import java.util.List;

import eg.kesry.loginApp.R;

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
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bindData(loginServer, activity);
        return convertView;
    }

    private class ViewHolder {
        TextView textView;
        Button menuButton;
        LinearLayout menuContainer;
        Button editButton;
        Button scanButton;
        Button deleteButton;
        LoginServer currentServer;
        MainActivity activity;

        ViewHolder(View view) {
            textView = view.findViewById(R.id.text_username);
            menuButton = view.findViewById(R.id.button_menu);
            menuContainer = view.findViewById(R.id.menu_container);
            editButton = view.findViewById(R.id.button_edit);
            scanButton = view.findViewById(R.id.button_scan);
            deleteButton = view.findViewById(R.id.button_delete);
            
            // 设置按钮点击事件
            setupClickListeners();
        }

        void bindData(LoginServer server, MainActivity mainActivity) {
            this.currentServer = server;
            this.activity = mainActivity;
            textView.setText(server.getUsername());
        }

        private void setupClickListeners() {
            // 菜单按钮点击事件 - 切换菜单显示状态
            menuButton.setOnClickListener(v -> toggleMenu());

            // 编辑按钮点击事件
            editButton.setOnClickListener(v -> {
                hideMenu();
                activity.editServer(currentServer);
            });

            // 扫码按钮点击事件
            scanButton.setOnClickListener(v -> {
                hideMenu();
                if (currentServer.getServerStatus() == 1) {
                    activity.scan(currentServer);
                } else {
                    activity.makeText("请先登录", Toast.LENGTH_SHORT);
                }
            });

            // 删除按钮点击事件
            deleteButton.setOnClickListener(v -> {
                hideMenu();
                // 删除服务器 - 弹窗确认删除
                new AlertDialog.Builder(context)
                        .setTitle("确认删除")
                        .setMessage("确定要删除服务器 \"" + currentServer.getUsername() + "\" 吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            // 具体删除功能
                            activity.removeServer(currentServer);
                            activity.makeText("删除成功", Toast.LENGTH_SHORT);
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            });
        }

        private void toggleMenu() {
            if (menuContainer.getVisibility() == View.GONE) {
                menuContainer.setVisibility(View.VISIBLE);
                // menuButton.setText("收起");
            } else {
                menuContainer.setVisibility(View.GONE);
                // menuButton.setText("菜单");
            }
        }

        private void hideMenu() {
            menuContainer.setVisibility(View.GONE);
            // menuButton.setText("菜单");
        }
    }

}