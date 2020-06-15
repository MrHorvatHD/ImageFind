package horvatApps.ImageScan.logic;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

public class OCRJob extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Intent service = new Intent(getApplicationContext(), MLService.class);
        getApplicationContext().startService(service);
        Util.scheduleJob(getApplicationContext()); // reschedule the job
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
