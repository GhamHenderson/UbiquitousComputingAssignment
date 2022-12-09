package com.college.rssassignment;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.media.MediaPlayer;
import android.widget.Toast;

public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        // runs on the main thread, so this Toast will appear
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        // returning false means the work has been done, return true if the job is being run asynchronously
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
        // if the job is prematurely cancelled, do cleanup work heremyPlayer.stop();
        return false;
    }
}
