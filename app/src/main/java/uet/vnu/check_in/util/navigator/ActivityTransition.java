package uet.vnu.check_in.util.navigator;

import androidx.annotation.IntDef;

import static uet.vnu.check_in.util.navigator.ActivityTransition.FINISH;
import static uet.vnu.check_in.util.navigator.ActivityTransition.NONE;
import static uet.vnu.check_in.util.navigator.ActivityTransition.START;

@IntDef({NONE, START, FINISH})
@interface ActivityTransition {
    int NONE = 0;
    int START = 1;
    int FINISH = -1;
}
