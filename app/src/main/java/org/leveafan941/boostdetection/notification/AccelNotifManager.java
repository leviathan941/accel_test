/*
 * Copyright (c) 2016 Alexey Kuzin <amkuzink@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.leveafan941.boostdetection.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.leveafan941.boostdetection.R;
import org.leveafan941.boostdetection.gui.MainActivity;

/**
 * @author Alexey Kuzin (amkuzink@gmail.com).
 */
public class AccelNotifManager {

    private static final String TAG = AccelNotifManager.class.getSimpleName();

    private static final int BOOST_LIMIT_EXCEED_NOTIF_ID = 143;
    private static final String EXCEED_NOTIF_CLICK_ACTION =
            "org.leveafan941.boostdetection.action.EXCEED_NOTIF_CLICKED";
    private static final String EXCEED_NOTIF_REMOVE_ACTION =
            "org.leveafan941.boostdetection.action.EXCEED_NOTIF_REMOVED";

    private final Context mContext;
    private final NotificationManager mNotificationMgr;

    public AccelNotifManager(Context context) {
        mContext = context;

        mNotificationMgr = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void showBoostLimitExceedNotification(int exceedNumber) {
        Log.d(TAG, "showBoostLimitExceedNotification with " + exceedNumber);

        final Notification notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mContext.getString(R.string.exceed_notification_content_title))
                .setContentText(String.valueOf(exceedNumber))
                .setContentIntent(createBoostLimitExceedContentIntent())
                .setDeleteIntent(createBoostLimitExceedDeleteIntent())
                .build();

        mNotificationMgr.notify(BOOST_LIMIT_EXCEED_NOTIF_ID, notif);
    }

    public void hideBoostLimitExceedNotification() {
        Log.d(TAG, "hideBoostLimitExceedNotification");

        mNotificationMgr.cancel(BOOST_LIMIT_EXCEED_NOTIF_ID);
    }

    private PendingIntent createBoostLimitExceedContentIntent() {
        final Intent notifyIntent = new Intent(mContext, MainActivity.class);
        notifyIntent.setAction(EXCEED_NOTIF_CLICK_ACTION);

        return PendingIntent.getActivity(mContext,
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createBoostLimitExceedDeleteIntent() {
        final Intent notifyIntent = new Intent(mContext, MainActivity.class);
        notifyIntent.setAction(EXCEED_NOTIF_REMOVE_ACTION);

        return PendingIntent.getActivity(mContext,
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
