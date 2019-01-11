package uet.vnu.check_in.util.navigator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Objects;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import uet.vnu.check_in.R;

public class Navigator {

    @NonNull
    private FragmentActivity mActivity;

    @NonNull
    private Fragment mFragment;

    public Navigator(@NonNull AppCompatActivity activity) {
        mActivity = activity;
    }

    public Navigator(@NonNull Fragment fragment) {
        mActivity = Objects.requireNonNull(fragment.getActivity());
        mFragment = fragment;
    }

    public void disableUserInteraction() {
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void enableUserInteraction() {
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void startActivity(@NonNull Class<? extends Activity> clazz,
                              @Nullable Bundle arguments) {
        Intent intent = new Intent(mActivity, clazz);
        if (arguments != null) {
            intent.putExtras(arguments);
        }
        startActivity(intent);
    }

    public void startActivityAtRoot(@NonNull Class<? extends Activity> clazz,
                                    @Nullable Bundle arguments) {
        Intent intent = new Intent(mActivity, clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (arguments != null) {
            intent.putExtras(arguments);
        }
        startActivity(intent);
    }

    public void startActivityForResult(@NonNull Class<? extends Activity> clazz,
                                       @NonNull int requestCode, @Nullable Bundle arguments) {
        Intent intent = new Intent(mActivity, clazz);
        if (arguments != null) {
            intent.putExtras(arguments);
        }

        mActivity.startActivityForResult(intent, requestCode);
        setActivityTransactionAnimation(ActivityTransition.START);
    }

    public void startActivityForResultFromFragment(@NonNull Class<? extends Activity> clazz,
                                                   Bundle arguments, int requestCode) {
        Intent intent = new Intent(mActivity, clazz);
        if (arguments != null) {
            intent.putExtras(arguments);
        }

        mFragment.startActivityForResult(intent, requestCode);
        setActivityTransactionAnimation(ActivityTransition.START);
    }

    public void finishActivity() {
        mActivity.finish();
        setActivityTransactionAnimation(ActivityTransition.FINISH);
    }

    public void finishActivityWithResult(int resultCode) {
        mActivity.setResult(resultCode);
        finishActivity();
    }

    public void finishActivityWithResult(@NonNull Bundle args, int resultCode) {
        Intent intent = new Intent();
        intent.putExtras(args);
        mActivity.setResult(resultCode, intent);
        finishActivity();
    }

    public void openUrl(String url) {
        if (TextUtils.isEmpty(url) || !Patterns.WEB_URL.matcher(url).matches()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        startActivity(intent);
    }

    public void showToast(@StringRes int stringId) {
        Toast.makeText(mActivity, mActivity.getString(stringId), Toast.LENGTH_SHORT).show();
    }

    public void showToast(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
    }

    public void addFragmentIntoFragment(@IdRes int containerViewId, @NonNull Fragment fragment,
                                        boolean addToBackStack, @NavigateAnimation int animation,
                                        @Nullable String tag) {
        FragmentTransaction fragmentTransaction =
                mFragment.getChildFragmentManager().beginTransaction();
        setFragmentTransactionAnimation(fragmentTransaction, animation);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        fragmentTransaction.add(containerViewId, fragment, tag);
        fragmentTransaction.commit();
        mFragment.getChildFragmentManager().executePendingTransactions();
    }

    public void addFragmentIntoActivity(@IdRes int layoutId, @NonNull Fragment fragment,
                                        boolean addToBackStack,
                                        @NavigateAnimation int animation, @Nullable String tag) {
        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
        setFragmentTransactionAnimation(fragmentTransaction, animation);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(tag);
        }
        fragmentTransaction.add(layoutId, fragment);
        fragmentTransaction.commit();
        mActivity.getSupportFragmentManager().executePendingTransactions();
    }

    public void popFragmentInsideActivity(boolean onlyShowFirstFragment) {
        boolean isFirstFragment = mActivity.getSupportFragmentManager().getBackStackEntryCount() == 1;
        if (onlyShowFirstFragment && isFirstFragment) {
            return;
        }

        mActivity.getSupportFragmentManager().popBackStackImmediate();
    }


    public void popFragmentInsideFragment(boolean onlyShowFirstFragment) {
        boolean isFirstFragment = mFragment.getChildFragmentManager().getBackStackEntryCount() == 1;
        if (onlyShowFirstFragment && isFirstFragment) {
            return;
        }

        mFragment.getChildFragmentManager().popBackStackImmediate();
    }

    private void startActivity(@NonNull Intent intent) {
        mActivity.startActivity(intent);
        setActivityTransactionAnimation(ActivityTransition.START);
    }

    private void setFragmentTransactionAnimation(FragmentTransaction transaction,
                                                 @NavigateAnimation int animation) {
        switch (animation) {
            case NavigateAnimation.FADED:
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case NavigateAnimation.RIGHT_LEFT:
                transaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out,
                        R.anim.slide_left_in, R.anim.slide_right_out);
                break;
            case NavigateAnimation.LEFT_RIGHT:
                transaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_right_out,
                        R.anim.slide_right_in, R.anim.slide_left_out);
                break;
            case NavigateAnimation.BOTTOM_UP:
                transaction.setCustomAnimations(R.anim.slide_bottom_in, R.anim.slide_top_out,
                        R.anim.slide_top_in, R.anim.slide_bottom_out);
                break;
            case NavigateAnimation.NONE:
                break;
            default:
                break;
        }
    }

    private void setActivityTransactionAnimation(@ActivityTransition int animation) {
        switch (animation) {
            case ActivityTransition.START:
                mActivity.overridePendingTransition(R.anim.slide_bottom_in, R.anim.slide_top_out);
                break;
            case ActivityTransition.FINISH:
                mActivity.overridePendingTransition(R.anim.slide_top_out, R.anim.slide_bottom_in);
                break;
            case ActivityTransition.NONE:
                break;
            default:
                break;
        }
    }
}
