package phlux;

import java.util.LinkedHashMap;

/**
 * PhluxViewAdapter incorporates common view logic.
 * This should be a single place where view state is stored.
 */
public class PhluxViewAdapter<S extends PhluxState> {

    private PhluxScope<S> scope;
    private boolean registered;
    private PhluxStateCallback<S> stateCallback;
    private LinkedHashMap<String, Object> updated = new LinkedHashMap<>();
    private boolean updateAllOnResume;

    public PhluxViewAdapter(PhluxScope<S> scope, PhluxStateCallback<S> stateCallback) {
        this.scope = scope;
        this.stateCallback = stateCallback;
    }

    public void setUpdateAllOnResume(boolean update) {
        this.updateAllOnResume = update;
    }

    public PhluxScope<S> scope() {
        return scope;
    }

    public <T> void part(String name, T newValue, PhluxView.FieldUpdater<T> updater) {
        if (!updated.containsKey(name) || updated.get(name) != newValue) {
            updater.call(newValue);
            updated.put(name, newValue);
        }
    }

    public void onResume(boolean newView) {
        if (!registered) {
            scope.register(stateCallback);
            registered = true;
        }
        else if (newView || updateAllOnResume) {
            updated.clear();
            stateCallback.call(scope.state());
        }
    }

    public void onDestroy() {
        if (registered) {
            scope.unregister(stateCallback);
            registered = false;
        }
    }
}