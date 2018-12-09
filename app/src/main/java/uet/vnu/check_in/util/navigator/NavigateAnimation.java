package uet.vnu.check_in.util.navigator;

import androidx.annotation.IntDef;

import static uet.vnu.check_in.util.navigator.NavigateAnimation.BOTTOM_UP;
import static uet.vnu.check_in.util.navigator.NavigateAnimation.FADED;
import static uet.vnu.check_in.util.navigator.NavigateAnimation.LEFT_RIGHT;
import static uet.vnu.check_in.util.navigator.NavigateAnimation.NONE;
import static uet.vnu.check_in.util.navigator.NavigateAnimation.RIGHT_LEFT;

@IntDef({NONE, RIGHT_LEFT, BOTTOM_UP, FADED, LEFT_RIGHT})
public @interface NavigateAnimation {
    int NONE = 0;
    int RIGHT_LEFT = 1;
    int BOTTOM_UP = 2;
    int FADED = 3;
    int LEFT_RIGHT = 4;
}
