package uet.vnu.check_in.screens;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import uet.vnu.check_in.util.navigator.Navigator;

public abstract class BaseActivity extends AppCompatActivity {

    protected Navigator mNavigator;

    @LayoutRes
    protected abstract int getLayoutResource();

    protected abstract void initComponents(Bundle savedInstanceState);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        mNavigator = new Navigator(this);
        initComponents(savedInstanceState);
    }
}
