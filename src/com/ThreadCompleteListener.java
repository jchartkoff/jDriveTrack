package com;

public interface ThreadCompleteListener {
    void notifyOfThreadComplete(final Runnable runnable);
}
