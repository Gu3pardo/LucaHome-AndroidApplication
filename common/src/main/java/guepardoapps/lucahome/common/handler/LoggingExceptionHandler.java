package guepardoapps.lucahome.common.handler;

import guepardoapps.lucahome.common.utils.Logger;

public class LoggingExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String Tag = LoggingExceptionHandler.class.getSimpleName();

    private final Thread.UncaughtExceptionHandler _rootUncaughtExceptionHandler;

    public LoggingExceptionHandler() {
        Logger.getInstance().Debug(Tag, "Constructor");
        // we should store the current exception handler -- to invoke it for all not handled exceptions ...
        _rootUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable exception) {
        Logger.getInstance().Error(Tag, exception.toString());
        _rootUncaughtExceptionHandler.uncaughtException(thread, exception);
    }
}
