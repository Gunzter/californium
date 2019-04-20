/*******************************************************************************
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - Initial creation
 ******************************************************************************/
package org.eclipse.californium.examples;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import static org.eclipse.californium.examples.PreferenceIDs.PREF_TIMED_REQUEST;

public final class JobManager {
    private static final String LOG_TAG = "job";
    private static final int JOB_ID = 1;

    @TargetApi(Build.VERSION_CODES.M)
    private static long createJob(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String time = preferences.getString(PREF_TIMED_REQUEST, "0");
        long interval = 0;
        try {
            interval = Long.parseLong(time);
            interval = TimeUnit.MINUTES.toMillis(interval);
        } catch (NumberFormatException e) {
        }
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (interval > 0) {
            ComponentName component = new ComponentName(context, TimedRequestJob.class);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setPeriodic(interval);
            JobInfo jobInfo = builder.build();
            jobScheduler.schedule(jobInfo);
            Log.i(LOG_TAG, "create job for timed requests with interval " + TimeUnit.MILLISECONDS.toMinutes(interval) + " min.");
        } else {
            jobScheduler.cancel(JOB_ID);
            Log.i(LOG_TAG, "removed job for timed requests.");
        }
        return interval;
    }

    public static long supportedMinimumInterval(TimeUnit unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return unit.convert(15, TimeUnit.MINUTES);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return unit.convert(5, TimeUnit.MINUTES);
        }
        return 0L;
    }

    public static boolean isSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static int initialize(Context context) {
        if (isSupported()) {
            if (createJob(context) > 0) {
                return 2;
            } else {
                return 1;
            }
        }
        return 0;
    }
}
